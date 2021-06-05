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

package com.openexchange.client.onboarding.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.client.onboarding.internal.OnboardingConfig;
import com.openexchange.client.onboarding.internal.OnboardingImageDataSource;
import com.openexchange.client.onboarding.internal.OnboardingServiceImpl;
import com.openexchange.client.onboarding.rmi.RemoteOnboardingService;
import com.openexchange.client.onboarding.rmi.impl.RemoteOnboardingServiceImpl;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sms.SMSServiceSPI;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.uadetector.UserAgentParser;
import com.openexchange.user.UserService;

/**
 * {@link OnboardingActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingActivator extends HousekeepingActivator {

    private OnboardingServiceImpl registry;

    /**
     * Initializes a new {@link OnboardingActivator}.
     */
    public OnboardingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, ConfigViewFactory.class, ConfigurationService.class, MimeTypeMap.class, UserAgentParser.class, ContextService.class,
            TranslatorFactory.class, ServerConfigService.class, CapabilityService.class, NotificationMailFactory.class, SessiondService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Services.setServiceLookup(this);
        OnboardingImageDataSource.setBundle(context.getBundle());

        // Create service instance
        OnboardingServiceImpl serviceImpl = new OnboardingServiceImpl(this);
        this.registry = serviceImpl;
        serviceImpl.setConfiguredScenarios(OnboardingConfig.parseScenarios(getService(ConfigurationService.class)));
        addService(OnboardingService.class, serviceImpl);

        // Track services needed for SMS transport
        trackService(SMSServiceSPI.class);
        trackService(DownloadLinkProvider.class);

        // Initialize & open provider tracker
        OnboardingProviderTracker providerTracker = new OnboardingProviderTracker(context, serviceImpl);
        rememberTracker(providerTracker);
        openTrackers();

        // Capability stuff
        {
            final String sCapability = "client-onboarding";
            Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
            registerService(CapabilityChecker.class, new CapabilityChecker() {
                @Override
                public boolean isEnabled(String capability, Session ses) throws OXException {
                    if (sCapability.equals(capability)) {
                        ServerSession session = ServerSessionAdapter.valueOf(ses);
                        if (session.isAnonymous() || session.getUser().isGuest()) {
                            return false;
                        }

                        Boolean bool = OnboardingUtility.getBoolFromProperty("com.openexchange.client.onboarding.enabled", Boolean.TRUE, session);
                        return bool.booleanValue();
                    }

                    return true;
                }
            }, properties);

            getService(CapabilityService.class).declareCapability(sCapability);
        }

        // Register services
        registerService(OnboardingService.class, serviceImpl);
        registerService(Reloadable.class, new OnboardingReloadable(serviceImpl));

        // Register appropriate RMI stub
        {
            Dictionary<String, Object> props = new Hashtable<String, Object>(2);
            props.put("RMIName", RemoteOnboardingService.RMI_NAME);
            registerService(Remote.class, new RemoteOnboardingServiceImpl(serviceImpl), props);
        }

        // Register image data source
        {
            OnboardingImageDataSource imageDataSource = OnboardingImageDataSource.getInstance();
            Dictionary<String, Object> props = new Hashtable<String, Object>(1);
            props.put("identifier", imageDataSource.getRegistrationName());
            registerService(DataSource.class, imageDataSource, props);
            ImageActionFactory.addMapping(imageDataSource.getRegistrationName(), imageDataSource.getAlias());
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        super.stopBundle();
        OnboardingServiceImpl registry = this.registry;
        if (null != registry) {
            this.registry = null;
            removeService(OnboardingService.class);
        }

        OnboardingImageDataSource.setBundle(null);
        Services.setServiceLookup(null);
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

}
