/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.osgi.DependentServiceStarter;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.saml.SAMLProperties;
import com.openexchange.saml.impl.DefaultLoginConfigurationLookup;
import com.openexchange.saml.impl.VeryDangerousSAMLBackend;
import com.openexchange.saml.impl.SAMLSessionInspector;
import com.openexchange.saml.impl.SAMLSessionSsoProvider;
import com.openexchange.saml.impl.SAMLSessionStorageParameterNamesProvider;
import com.openexchange.saml.impl.hz.PortableAuthnRequestInfoFactory;
import com.openexchange.saml.impl.hz.PortableLogoutRequestInfoFactory;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService;
import com.openexchange.saml.spi.SAMLBackend;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.SessionSsoProvider;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.reservation.SessionReservationService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;
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

    private static final Logger LOG = LoggerFactory.getLogger(SAMLFeature.class);

    private final static Class<?>[] NEEDED_SERVICES = new Class[] {
        HttpService.class,
        LeanConfigurationService.class,
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

    // -------------------------------------------------------------------------------------------------------------------------------------

    private SAMLBackendRegistry samlBackends;

    private final Stack<ServiceRegistration<?>> serviceRegistrations = new Stack<ServiceRegistration<?>>();

    private final Stack<String> servlets = new Stack<String>();

    public SAMLFeature(BundleContext context) throws InvalidSyntaxException {
        super(context, NEEDED_SERVICES, OPTIONAL_SERVICES);
    }

    @Override
    protected synchronized void start(ServiceLookup services) throws Exception {
        LeanConfigurationService configService = services.getServiceSafe(LeanConfigurationService.class);
        boolean enabled = configService.getBooleanProperty(SAMLProperties.ENABLED);
        if (enabled) {
            LOG.info("Starting SAML 2.0 support...");
            SessiondService sessiondService = services.getServiceSafe(SessiondService.class);
            serviceRegistrations.push(context.registerService(SessionInspectorService.class, new SAMLSessionInspector(sessiondService), null));
            serviceRegistrations.push(context.registerService(CustomPortableFactory.class, new PortableAuthnRequestInfoFactory(), null));
            serviceRegistrations.push(context.registerService(CustomPortableFactory.class, new PortableLogoutRequestInfoFactory(), null));
            serviceRegistrations.push(context.registerService(SessionStorageParameterNamesProvider.class, new SAMLSessionStorageParameterNamesProvider(), null));
            serviceRegistrations.push(context.registerService(SessionSsoProvider.class, new SAMLSessionSsoProvider(new DefaultLoginConfigurationLookup(), sessiondService), null));

            getSamlBackend(services);

            if (configService.getBooleanProperty(SAMLProperties.DEBUG_BACKEND)) {
                serviceRegistrations.push(context.registerService(SAMLBackend.class, new VeryDangerousSAMLBackend(services.getServiceSafe(UserService.class), services.getServiceSafe(ContextService.class), configService), null));
            }
        } else {
            LOG.info("SAML 2.0 support is disabled by configuration. Skipping initialization...");
        }
    }

    /**
     * Helper method to initialize the <code>SAMLBackendRegistry</code>.
     *
     * @param services The service look-up
     * @throws BundleException If initialization fails
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
    protected synchronized void stop(ServiceLookup services) {
        LOG.info("Stopping SAML 2.0 support...");
        HttpService httpService = services.getService(HttpService.class);
        while (!servlets.isEmpty()) {
            HttpServices.unregister(servlets.pop(), httpService);
        }

        while (!serviceRegistrations.isEmpty()) {
            serviceRegistrations.pop().unregister();
        }
        if (samlBackends != null) {
            samlBackends.close();
            samlBackends.stop();
            samlBackends = null;
        }
    }

}
