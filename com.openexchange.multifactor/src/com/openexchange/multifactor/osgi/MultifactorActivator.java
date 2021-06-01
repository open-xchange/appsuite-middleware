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

package com.openexchange.multifactor.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.DispatcherListener;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.FailureAwareCapabilityChecker;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.login.multifactor.MultifactorLoginService;
import com.openexchange.multifactor.MultifactorAuthenticatorFactory;
import com.openexchange.multifactor.MultifactorLockoutService;
import com.openexchange.multifactor.MultifactorManagementService;
import com.openexchange.multifactor.MultifactorProperties;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.MultifactorProviderRegistryImpl;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.MultifactorSessionInspector;
import com.openexchange.multifactor.MultifactorSessionStorage;
import com.openexchange.multifactor.impl.MultifactorAuthenticatorFactoryImpl;
import com.openexchange.multifactor.impl.MultifactorConfigTreeItem;
import com.openexchange.multifactor.impl.MultifactorLoginServiceImpl;
import com.openexchange.multifactor.impl.MultifactorManagementServiceImpl;
import com.openexchange.multifactor.listener.MultifactorDeleteDeviceListener;
import com.openexchange.multifactor.listener.MultifactorDeleteListener;
import com.openexchange.multifactor.listener.MultifactorDispatcherListener;
import com.openexchange.multifactor.listener.MultifactorListener;
import com.openexchange.multifactor.listener.MultifactorListenerChain;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.osgi.RegistryCustomizer;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;

/**
 * {@link MultifactorActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MultifactorActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { LeanConfigurationService.class, MultifactorLockoutService.class, CapabilityService.class, SessiondService.class };
    }

    /**
     * Pull from configuration list of modules/actions to protect, then create
     * needed MultifactorDispatcherListeners for it
     *
     * @param leanConfig LeanConfigurationService
     * @param mfService MultifactorLoginService
     * @return A collection of MultifactorDispatcherListeners
     */
    private Collection<MultifactorDispatcherListener> getListeners(LeanConfigurationService leanConfig, MultifactorLoginService mfService) {
        Objects.requireNonNull(leanConfig, "LeanConfigurationService required");
        Objects.requireNonNull(mfService, "MultifactorLoginService required");
        ArrayList<MultifactorDispatcherListener> listeners = new ArrayList<MultifactorDispatcherListener>();
        String protectedUrls = leanConfig.getProperty(MultifactorProperties.recentAuthRequired);

        String[] urls = protectedUrls.split(",");
        for (String url : urls) {
            String[] components = url.split("\\?");
            if (components.length >= 1) {
                if (components[0] != null) {  // Module must not be null
                    String action = "";
                    if (components.length > 1) {  // Specifying action is optional
                        action = components[1].contains("=") ? components[1].substring(components[1].indexOf('=') + 1) : components[1];
                    }
                    listeners.add(new MultifactorDispatcherListener(mfService, components[0].trim(), action.trim()));
                }
            }
        }

        return listeners;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle {}", context.getBundle().getSymbolicName());

        final LeanConfigurationService configurationService = getServiceSafe(LeanConfigurationService.class);
        if (configurationService.getBooleanProperty(MultifactorProperties.demo)) {
            LOG.warn("WARNING: The property \"{}\" is set to true. Multifactor authentication is currently in DEMO MODE and provides NO SECURITY! Please set it to 'false' if this is not intended!", MultifactorProperties.demo.getFQPropertyName());
        }

        final MultifactorProviderRegistryImpl registry = new MultifactorProviderRegistryImpl();
        registerService(MultifactorProviderRegistry.class, registry);
        track(MultifactorProvider.class, new MultifactorProviderCustomizer(context, registry));
        //Multifactor authentication
        track(MultifactorProviderRegistry.class, new RegistryCustomizer<MultifactorProviderRegistry>(context, MultifactorProviderRegistry.class));

        registerService(SessionInspectorService.class, new MultifactorSessionInspector());
        registerService(SessionStorageParameterNamesProvider.class, new MultifactorSessionStorage());
        RankingAwareNearRegistryServiceTracker<MultifactorListener> listeners = new RankingAwareNearRegistryServiceTracker<MultifactorListener>(context, MultifactorListener.class);
        registerService(MultifactorListener.class, new MultifactorDeleteDeviceListener(getServiceSafe(SessiondService.class)), Integer.MAX_VALUE);
        MultifactorListenerChain listenerChain = new MultifactorListenerChain(listeners);
        MultifactorAuthenticatorFactory factory = new MultifactorAuthenticatorFactoryImpl(
            getServiceSafe(MultifactorLockoutService.class),
            getServiceSafe(SessiondService.class),
            listenerChain,
            registry);
        MultifactorLoginService mfLoginService = new MultifactorLoginServiceImpl(registry, factory, configurationService);
        registerService(MultifactorLoginService.class, mfLoginService);
        registerService(MultifactorAuthenticatorFactory.class, factory);
        registerService(MultifactorManagementService.class, new MultifactorManagementServiceImpl(registry, factory));

        // Handle user and context deletion
        registerService(DeleteListener.class, new MultifactorDeleteListener(registry));

        registerService (MultifactorListenerChain.class, listenerChain);
        rememberTracker(listeners);

        MultifactorConfigTreeItem item = new MultifactorConfigTreeItem ();
        registerService(ConfigTreeEquivalent.class, item);
        registerService(PreferencesItemService.class, item);

        // Announce multifactor service available and some providers exist
        final String sCapability = "multifactor_service";
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
        properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
        registerService(CapabilityChecker.class, new FailureAwareCapabilityChecker() {

            @Override
            public FailureAwareCapabilityChecker.Result checkEnabled(String capability, Session session) {
                if (sCapability.equals(capability)) {
                    if (session == null || registry.getProviders(new MultifactorRequest(session, null)).isEmpty()) {
                        return FailureAwareCapabilityChecker.Result.DISABLED;  // No providers registered or enabled
                    }
                }

                return FailureAwareCapabilityChecker.Result.ENABLED;
            }
        }, properties);
        getService(CapabilityService.class).declareCapability(sCapability);

        // Register listeners to monitor authentication requirements
        Collection<MultifactorDispatcherListener> dispatcherListeners = getListeners(getServiceSafe(LeanConfigurationService.class), mfLoginService);
        for (MultifactorDispatcherListener listener : dispatcherListeners) {
            registerService(DispatcherListener.class, listener);
        }

        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle {}", context.getBundle().getSymbolicName());
        super.stopBundle();
    }
}
