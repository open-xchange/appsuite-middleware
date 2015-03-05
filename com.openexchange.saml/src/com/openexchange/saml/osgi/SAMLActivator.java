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
import javax.xml.parsers.DocumentBuilderFactory;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleServiceTrackerCustomizer;
import com.openexchange.saml.DefaultConfig;
import com.openexchange.saml.OpenSAML;
import com.openexchange.saml.SAMLServiceProvider;
import com.openexchange.saml.Services;
import com.openexchange.saml.spi.AuthnResponseHandler;
import com.openexchange.saml.spi.ServiceProviderCustomizer;
import com.openexchange.session.reservation.SessionReservationService;

/**
 * {@link SAMLActivator} - The activator for <i>com.openexchange.saml</i> bundle.
 */
public class SAMLActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLActivator.class);

    private SAMLServiceProvider serviceProvider;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, DispatcherPrefixService.class, AuthnResponseHandler.class, SessionReservationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle com.openexchange.saml...");
        Services.setServiceLookup(this);

        ConfigurationService configService = getService(ConfigurationService.class);
        boolean enabled = configService.getBoolProperty("com.openexchange.saml.sp.enabled", false);
        if (enabled) {
            OpenSAML openSAML = initOpenSAML();
            serviceProvider = new SAMLServiceProvider(DefaultConfig.init(configService), openSAML, getService(AuthnResponseHandler.class), getService(SessionReservationService.class));
            serviceProvider.init();

            trackService(HostnameService.class);
            track(ServiceProviderCustomizer.class, new SimpleServiceTrackerCustomizer<ServiceProviderCustomizer>() {
                @Override
                public ServiceProviderCustomizer addingService(ServiceReference<ServiceProviderCustomizer> reference) {
                    ServiceProviderCustomizer customizer = context.getService(reference);
                    if (customizer != null) {
                        serviceProvider.setCustomizer(customizer);
                    }

                    return customizer;
                }

                @Override
                public void removedService(ServiceReference<ServiceProviderCustomizer> reference, ServiceProviderCustomizer service) {
                    serviceProvider.setCustomizer(null);
                }

                @Override
                public void modifiedService(ServiceReference<ServiceProviderCustomizer> reference, ServiceProviderCustomizer service) {}
            });

            openTrackers();
        } else {
            LOG.info("SAML 2.0 support is disabled by configuration. Skipping initialization...");
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

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle com.openexchange.saml...");
        serviceProvider = null;
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
