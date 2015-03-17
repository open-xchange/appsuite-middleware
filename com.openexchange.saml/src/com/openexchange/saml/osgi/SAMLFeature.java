/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.saml.osgi;

import java.security.Provider;
import java.security.Security;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilderFactory;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.osgi.DependentServiceStarter;
import com.openexchange.saml.DefaultConfig;
import com.openexchange.saml.OpenSAML;
import com.openexchange.saml.SAMLProperties;
import com.openexchange.saml.WebSSOProvider;
import com.openexchange.saml.http.AssertionConsumerService;
import com.openexchange.saml.http.SingleLogoutService;
import com.openexchange.saml.impl.HzStateManagement;
import com.openexchange.saml.impl.SAMLSessionInspector;
import com.openexchange.saml.impl.SAMLWebSSOProviderImpl;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link SAMLFeature}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLFeature extends DependentServiceStarter {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLFeature.class);

    private final static Class<?>[] NEEDED_SERVICES = new Class[] {
        HttpService.class,
        ConfigurationService.class,
        DispatcherPrefixService.class,
        SessionReservationService.class,
        SAMLBackend.class,
        HazelcastInstance.class,
        SessiondService.class
    };

    private final static Class<?>[] OPTIONAL_SERVICES = new Class[] {
        HostnameService.class
    };

    private final Stack<ServiceRegistration<?>> serviceRegistrations = new Stack<ServiceRegistration<?>>();

    private final Stack<String> servlets = new Stack<String>();

    public SAMLFeature(BundleContext context) throws InvalidSyntaxException {
        super(context, NEEDED_SERVICES, OPTIONAL_SERVICES);
    }

    @Override
    protected void start(ServiceLookup services) throws Exception {
        ConfigurationService configService = services.getService(ConfigurationService.class);
        boolean enabled = configService.getBoolProperty(SAMLProperties.ENABLED, false);
        if (enabled) {
            LOG.info("Starting SAML 2.0 support...");
            OpenSAML openSAML = initOpenSAML();
            DefaultConfig config = DefaultConfig.init(configService);

            HzStateManagement hzStateManagement = new HzStateManagement(services.getService(HazelcastInstance.class));
            WebSSOProvider serviceProvider = new SAMLWebSSOProviderImpl(config, openSAML, hzStateManagement, services);
            serviceRegistrations.push(context.registerService(SessionInspectorService.class, new SAMLSessionInspector(serviceProvider), null));

            SAMLBackend samlBackend = services.getService(SAMLBackend.class);
            HttpService httpService = services.getService(HttpService.class);
            String acsServletPath = services.getService(DispatcherPrefixService.class).getPrefix() + "saml/acs";
            httpService.registerServlet(acsServletPath, new AssertionConsumerService(serviceProvider, samlBackend.getExceptionHandler()), null, null);
            servlets.push(acsServletPath);

            if (config.supportSingleLogout()) {
                String slsServletPath = services.getService(DispatcherPrefixService.class).getPrefix() + "saml/sls";
                httpService.registerServlet(slsServletPath, new SingleLogoutService(serviceProvider), null, null);
                servlets.push(slsServletPath);
            }
        } else {
            LOG.info("SAML 2.0 support is disabled by configuration. Skipping initialization...");
        }
    }

    @Override
    protected void stop(ServiceLookup services) {
        LOG.info("Stopping SAML 2.0 support...");
        HttpService httpService = services.getService(HttpService.class);
        while (!servlets.isEmpty()) {
            httpService.unregister(servlets.pop());
        }

        while (!serviceRegistrations.isEmpty()) {
            serviceRegistrations.pop().unregister();
        }
    }

    private OpenSAML initOpenSAML() throws BundleException {
        if (!Configuration.validateJCEProviders()) {
            LOG.error("The necessary JCE providers for OpenSAML could not be found. SAML 2.0 integration will be disabled!");
            throw new BundleException("The necessary JCE providers for OpenSAML could not be found.", BundleException.ACTIVATOR_ERROR);
        }

        LOG.info("OpenSAML will use {} as API for XML processing", DocumentBuilderFactory.newInstance().getClass().getName());
        for (Provider jceProvider : Security.getProviders()) {
            LOG.info("OpenSAML found {} as potential JCE provider", jceProvider.getInfo());
        }

        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            LOG.error("Error while bootstrapping OpenSAML library", e);
            throw new BundleException("Error while bootstrapping OpenSAML library", BundleException.ACTIVATOR_ERROR, e);
        }
        return new OpenSAML();
    }

}
