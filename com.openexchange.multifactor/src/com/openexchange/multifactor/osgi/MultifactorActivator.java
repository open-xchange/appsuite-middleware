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
import com.openexchange.multifactor.impl.MultifactorLoginServiceImpl;
import com.openexchange.multifactor.impl.MultifactorManagementServiceImpl;
import com.openexchange.multifactor.listener.MultifactorDispatcherListener;
import com.openexchange.multifactor.listener.MultifactorListener;
import com.openexchange.multifactor.listener.MultifactorListenerChain;
import com.openexchange.multifactor.listener.MultifactorDeleteDeviceListener;
import com.openexchange.multifactor.listener.MultifactorDeleteListener;
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
                        action = components[1].contains("=") ? components[1].substring(components[1].indexOf("=") + 1) : components[1];
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
