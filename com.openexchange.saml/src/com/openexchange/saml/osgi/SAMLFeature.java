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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import java.util.Stack;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.osgi.DependentServiceStarter;
import com.openexchange.saml.SAMLProperties;
import com.openexchange.saml.impl.SAMLConfigRegistryImpl;
import com.openexchange.saml.impl.SAMLSessionInspector;
import com.openexchange.saml.impl.hz.PortableAuthnRequestInfoFactory;
import com.openexchange.saml.impl.hz.PortableLogoutRequestInfoFactory;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

/**
 * Tracks service dependencies, initializes the SAML core and registers all SAML-specific
 * services and servlets.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLFeature extends DependentServiceStarter {

    private SAMLBackendRegistry samlBackends;

    private static final Logger LOG = LoggerFactory.getLogger(SAMLFeature.class);

    private final static Class<?>[] NEEDED_SERVICES = new Class[] {
        HttpService.class,
        ConfigurationService.class,
        DispatcherPrefixService.class,
        SessionReservationService.class,
        HazelcastInstance.class,
        SessiondService.class,
        CapabilityService.class,
        TemplateService.class,
        ContextService.class,
        UserService.class,
        OAuthAccessTokenService.class
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
            SessiondService sessiondService = services.getService(SessiondService.class);
            serviceRegistrations.push(context.registerService(SessionInspectorService.class, new SAMLSessionInspector(sessiondService), null));
            serviceRegistrations.push(context.registerService(CustomPortableFactory.class, new PortableAuthnRequestInfoFactory(), null));
            serviceRegistrations.push(context.registerService(CustomPortableFactory.class, new PortableLogoutRequestInfoFactory(), null));
            getSamlBackend(services);
        } else {
            LOG.info("SAML 2.0 support is disabled by configuration. Skipping initialization...");
        }
    }

    /**
     * Helper method to get the SAMLBackendRegistry
     * @param services the ServiceLookup
     * @throws BundleException if start fails
     */
    private void getSamlBackend(ServiceLookup services) throws BundleException {
        SAMLBackendRegistry samlBackends = this.samlBackends;
        if (null == samlBackends) {
            samlBackends = new SAMLBackendRegistry(context, services);
        }
        samlBackends.open();
        this.samlBackends = samlBackends;
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
        if (samlBackends != null) {
            samlBackends.close();
            samlBackends.stop();
            samlBackends = null;
        }
        SAMLConfigRegistryImpl.getInstance().clear();
    }

}
