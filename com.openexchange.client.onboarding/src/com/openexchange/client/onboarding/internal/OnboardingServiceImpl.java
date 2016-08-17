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

package com.openexchange.client.onboarding.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.client.onboarding.AvailabilityResult;
import com.openexchange.client.onboarding.BuiltInProvider;
import com.openexchange.client.onboarding.CompositeId;
import com.openexchange.client.onboarding.DefaultScenario;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.DeviceAwareScenario;
import com.openexchange.client.onboarding.Link;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingRequest;
import com.openexchange.client.onboarding.OnboardingUtility;
import com.openexchange.client.onboarding.Result;
import com.openexchange.client.onboarding.ResultObject;
import com.openexchange.client.onboarding.ResultReply;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.client.onboarding.service.OnboardingView;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link OnboardingServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingServiceImpl implements OnboardingService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OnboardingServiceImpl.class);

    /** The identifier for generic app provider: <code>"app"</code> */
    private static final String GENERIC_APP_ID = BuiltInProvider.GENERIC_APP.getId();

    // --------------------------------------------------------------------------------------------------------------------------- //

    private final ServiceLookup services;
    private final ConcurrentMap<String, OnboardingProvider> providers;
    private final AtomicReference<Map<String, ConfiguredScenario>> configuredScenariosReference;
    private final AtomicBoolean warningLogged;

    /**
     * Initializes a new {@link OnboardingServiceImpl}.
     */
    public OnboardingServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
        providers = new ConcurrentHashMap<String, OnboardingProvider>(32, 0.9F, 1);
        configuredScenariosReference = new AtomicReference<>(null);
        warningLogged = new AtomicBoolean(false);
    }

    /**
     * Sets the specified configured scenarios.
     *
     * @param configuredScenarios The configured scenarios
     */
    public void setConfiguredScenarios(Map<String, ConfiguredScenario> configuredScenarios) {
        configuredScenariosReference.set(configuredScenarios);
    }

    /**
     * Adds the specified provider if no such provider is already contained.
     *
     * @param provider The provider to ass
     * @return <code>true</code> if given provider has been successfully added; otherwise <code>false</code>
     */
    public boolean addProviderIfAbsent(OnboardingProvider provider) {
        if (null == provider) {
            return false;
        }
        return null == providers.putIfAbsent(provider.getId(), provider);
    }

    /**
     * Removes the denoted provider
     *
     * @param providerId The identifier of the provider to remove
     * @return <code>true</code> if provider was removed; otherwise <code>false</code>
     */
    public boolean removeProvider(String providerId) {
        return null != providers.remove(providerId);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    @Override
    public Collection<OnboardingProvider> getAllProviders() throws OXException {
        return Collections.unmodifiableCollection(providers.values());
    }

    @Override
    public OnboardingProvider getProvider(String id) throws OXException {
        OnboardingProvider provider = null == id ? null : providers.get(id);
        if (null == provider) {
            throw OnboardingExceptionCodes.NOT_FOUND.create(null == id ? "null" : id);
        }
        return provider;
    }

    @Override
    public Collection<OnboardingProvider> getAvailableProvidersFor(Session session) throws OXException {
        return Collections.unmodifiableCollection(providers.values());
    }

    @Override
    public ResultObject execute(OnboardingRequest request, Session session) throws OXException {
        if (null == request) {
            return null;
        }

        // Invocation chain - looping through providers
        Scenario scenario = request.getScenario();
        Result result = null;
        for (OnboardingProvider provider : scenario.getProviders(session)) {
            Result currentResult = provider.execute(request, result, session);
            ResultReply reply = currentResult.getReply();
            if (ResultReply.ACCEPT == reply) {
                // Return
                return currentResult.getResultObject(request, session);
            }
            if (ResultReply.DENY == reply) {
                // No further processing allowed
                throw OnboardingExceptionCodes.EXECUTION_DENIED.create(provider.getId(), scenario.getId());
            }
            // Otherwise NEUTRAL reply; next in chain
            result = currentResult;
        }

        // Return the result object
        if (null == result) {
            throw OnboardingExceptionCodes.EXECUTION_FAILED.create(scenario.getId());
        }
        return result.getResultObject(request, session);
    }

    @Override
    public Scenario getScenario(String scenarioId, Session session) throws OXException {
        if (null == scenarioId) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create("null");
        }

        Map<String, ConfiguredScenario> configuredScenarios = configuredScenariosReference.get();
        Scenario scenario = getScenario(scenarioId, configuredScenarios, session);
        if (null == scenario) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(scenarioId);
        }
        return scenario;
    }

    @Override
    public boolean isAvailableFor(String scenarioId, Session session) throws OXException {
        if (null == scenarioId) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create("null");
        }

        Map<String, ConfiguredScenario> configuredScenarios = configuredScenariosReference.get();
        ConfiguredScenario configuredScenario = configuredScenarios.get(scenarioId);
        if (null == configuredScenario) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(scenarioId);
        }

        if (!configuredScenario.isEnabled()) {
            throw OnboardingExceptionCodes.DISABLED_SCENARIO.create(scenarioId);
        }

        // Iterate & check its providers
        for (String providerId : configuredScenario.getProviderIds()) {
            OnboardingProvider provider = providers.get(providerId);
            if (null == provider) {
                if (configuredScenario.logWarningForAbsentProvider()) {
                    LOG.warn("No such provider '{}' available for configured scenario '{}'. Hence, scenario will not be available. Please check \"{}\" configuration file and align it to available/installed providers.", providerId, configuredScenario.getId(), OnboardingConfig.getScenariosConfigFileName());
                }
                return false;
            } else if (!isAvailable(provider, configuredScenario, session).isAvailable()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DeviceAwareScenario getScenario(String scenarioId, Device device, Session session) throws OXException {
        if (null == scenarioId) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create("null");
        }

        Map<String, ConfiguredScenario> configuredScenarios = configuredScenariosReference.get();
        Scenario scenario = getScenario(scenarioId, configuredScenarios, session);
        if (null == scenario || !scenario.isEnabled(session)) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(scenarioId);
        }


        boolean enabled = true;
        Set<String> missingCapabilities = new LinkedHashSet<String>(4);
        for (Iterator<OnboardingProvider> it = scenario.getProviders(session).iterator(); enabled && it.hasNext();) {
            OnboardingProvider provider = it.next();
            AvailabilityResult result = isAvailable(provider, scenario, session);
            enabled &= result.isAvailable();
            missingCapabilities.addAll(result.getMissingCapabilities());
        }

        return new DeviceAwareScenarionImpl(scenario, enabled, missingCapabilities, device, Device.getActionsFor(device, scenario.getType(), session));
    }

    @Override
    public DeviceAwareScenario getScenario(String scenarioId, Device device, int userId, int contextId) throws OXException {
        if (null == scenarioId) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create("null");
        }

        Map<String, ConfiguredScenario> configuredScenarios = configuredScenariosReference.get();
        Scenario scenario = getScenario(scenarioId, configuredScenarios, userId, contextId);
        if (null == scenario || !scenario.isEnabled(userId, contextId)) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(scenarioId);
        }

        boolean enabled = true;
        Set<String> missingCapabilities = new LinkedHashSet<String>(4);
        for (Iterator<OnboardingProvider> it = scenario.getProviders(userId, contextId).iterator(); enabled && it.hasNext();) {
            OnboardingProvider provider = it.next();
            AvailabilityResult result = isAvailable(provider, scenario, userId, contextId);
            enabled &= result.isAvailable();
            missingCapabilities.addAll(result.getMissingCapabilities());
        }
        return new DeviceAwareScenarionImpl(scenario, enabled, missingCapabilities, device, Device.getActionsFor(device, scenario.getType(), userId, contextId));
    }

    @Override
    public List<DeviceAwareScenario> getScenariosFor(Device device, Session session) throws OXException {
        List<String> availableScenarios = device.getScenarios(session);
        if (null == availableScenarios || availableScenarios.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, ConfiguredScenario> configuredScenarios = configuredScenariosReference.get();

        List<DeviceAwareScenario> scenarios = new ArrayList<DeviceAwareScenario>(availableScenarios.size());
        for (String scenarioId : availableScenarios) {
            ConfiguredScenario configuredScenario = configuredScenarios.get(scenarioId);
            if (null == configuredScenario) {
                throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(scenarioId);
            }

            if (configuredScenario.isEnabled()) {
                Scenario scenario = getScenario(configuredScenario, configuredScenarios, false, session);
                if (null != scenario && scenario.isEnabled(session)) {
                    boolean enabled = true;
                    Set<String> missingCapabilities = new LinkedHashSet<String>(4);
                    for (Iterator<OnboardingProvider> it = scenario.getProviders(session).iterator(); enabled && it.hasNext();) {
                        OnboardingProvider provider = it.next();
                        AvailabilityResult result = isAvailable(provider, scenario, session);
                        enabled &= result.isAvailable();
                        missingCapabilities.addAll(result.getMissingCapabilities());
                    }
                    scenarios.add(new DeviceAwareScenarionImpl(scenario, enabled, missingCapabilities, device, Device.getActionsFor(device, scenario.getType(), session)));
                }
            }
        }
        return scenarios;
    }

    private AvailabilityResult isAvailable(OnboardingProvider provider, Scenario scenario, Session session) throws OXException {
        if (!GENERIC_APP_ID.equals(provider.getId())) {
            // Not the special provider for generic apps
            return provider.isAvailable(session);
        }

        // Check for statically specified capabilities
        return isAvailable0(provider, scenario.getCapabilities(session), session, session.getUserId(), session.getContextId());
    }

    private AvailabilityResult isAvailable(OnboardingProvider provider, ConfiguredScenario scenario, Session session) throws OXException {
        if (!GENERIC_APP_ID.equals(provider.getId())) {
            // Not the special provider for generic apps
            return provider.isAvailable(session);
        }

        return isAvailable0(provider, scenario.getCapabilities(), session, session.getUserId(), session.getContextId());
    }

    private AvailabilityResult isAvailable(OnboardingProvider provider, Scenario scenario, int userId, int contextId) throws OXException {
        if (!GENERIC_APP_ID.equals(provider.getId())) {
            // Not the special provider for generic apps
            return provider.isAvailable(userId, contextId);
        }

        return isAvailable0(provider, scenario.getCapabilities(userId, contextId), null, userId, contextId);
    }

    private AvailabilityResult isAvailable0(OnboardingProvider provider, List<String> requiredCapabilities, Session optSession, int userId, int contextId) throws OXException {
        // Check for statically specified capabilities
        if (null == requiredCapabilities || requiredCapabilities.isEmpty()) {
            return null == optSession ? provider.isAvailable(userId, contextId) : provider.isAvailable(optSession);
        }

        CapabilitySet capabilities;
        {
            CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
            if (null == capabilityService) {
                return null == optSession ? provider.isAvailable(userId, contextId) : provider.isAvailable(optSession);
            }
            capabilities = null == optSession ? capabilityService.getCapabilities(userId, contextId) : capabilityService.getCapabilities(optSession);
        }

        Set<String> missingCaps = new LinkedHashSet<String>(requiredCapabilities.size());
        boolean enabled = true;
        for (String requiredCapability : requiredCapabilities) {
            requiredCapability = Strings.asciiLowerCase(requiredCapability);
            boolean contained = capabilities.contains(requiredCapability);
            enabled &= contained;
            if (false == contained) {
                missingCaps.add(requiredCapability);
            }
        }
        return enabled ? AvailabilityResult.available() : new AvailabilityResult(false, new ArrayList<String>(missingCaps));
    }

    private Scenario getScenario(String scenarioId, Map<String, ConfiguredScenario> configuredScenarios, Session session) throws OXException {
        if (null == scenarioId) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create("null");
        }

        ConfiguredScenario configuredScenario = configuredScenarios.get(scenarioId);
        if (null == configuredScenario) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(scenarioId);
        }

        if (!configuredScenario.isEnabled()) {
            throw OnboardingExceptionCodes.DISABLED_SCENARIO.create(scenarioId);
        }

        return getScenario(configuredScenario, configuredScenarios, true, session);
    }

    private Scenario getScenario(String scenarioId, Map<String, ConfiguredScenario> configuredScenarios, int userId, int contextId) throws OXException {
        if (null == scenarioId) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create("null");
        }

        ConfiguredScenario configuredScenario = configuredScenarios.get(scenarioId);
        if (null == configuredScenario) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(scenarioId);
        }

        if (!configuredScenario.isEnabled()) {
            throw OnboardingExceptionCodes.DISABLED_SCENARIO.create(scenarioId);
        }

        return getScenario(configuredScenario, configuredScenarios, true, userId, contextId);
    }

    private Scenario getScenario(ConfiguredScenario configuredScenario, Map<String, ConfiguredScenario> configuredScenarios, boolean errorOnProviderAbsence, Session session) throws OXException {
        // Resolve link (if any)
        Link link;
        {
            ConfiguredLink configuredLink = configuredScenario.getLink();
            link = resolveLink(configuredLink, session);
        }

        // Create scenario instance
        DefaultScenario scenario = DefaultScenario.newInstance(configuredScenario.getId(), configuredScenario.getType(), link, configuredScenario.getIcon(), configuredScenario.getCapabilities(), configuredScenario.getDisplayName(), configuredScenario.getDescription());

        // Iterate & check its providers
        for (String providerId : configuredScenario.getProviderIds()) {
            OnboardingProvider provider = providers.get(providerId);
            if (null == provider) {
                if (errorOnProviderAbsence) {
                    throw OnboardingExceptionCodes.INVALID_SCENARIO.create(configuredScenario.getId(), providerId);
                }
                if (configuredScenario.logWarningForAbsentProvider()) {
                    LOG.warn("No such provider '{}' available for configured scenario '{}'. Hence, scenario will not be available. Please check \"{}\" configuration file and align it to available/installed providers.", providerId, configuredScenario.getId(), OnboardingConfig.getScenariosConfigFileName());
                }
                return null;
            }
            scenario.addProvider(getProvider(providerId));
        }

        // Iterate & load its alternatives
        for (String alternativeId : configuredScenario.getAlternativeIds()) {
            ConfiguredScenario alternativeConfiguredScenario = configuredScenarios.get(alternativeId);
            if (null == alternativeConfiguredScenario) {
                LOG.warn("Alternative scenario '{}' does not exist in configured scenarios for '{}'", alternativeId, configuredScenario.getId());
            } else if (!alternativeConfiguredScenario.isEnabled()) {
                LOG.warn("Alternative scenario '{}' is not enabled in configured scenarios for '{}'", alternativeId, configuredScenario.getId());
            } else {
                Scenario alternative = getScenario(alternativeConfiguredScenario, configuredScenarios, false, session);
                if (null != alternative) {
                    scenario.addAlternative(alternative);
                }
            }
        }

        return scenario;
    }

    private Scenario getScenario(ConfiguredScenario configuredScenario, Map<String, ConfiguredScenario> configuredScenarios, boolean errorOnProviderAbsence, int userId, int contextId) throws OXException {
        // Resolve link (if any)
        Link link;
        {
            ConfiguredLink configuredLink = configuredScenario.getLink();
            link = resolveLink(configuredLink, userId, contextId);
        }

        // Create scenario instance
        DefaultScenario scenario = DefaultScenario.newInstance(configuredScenario.getId(), configuredScenario.getType(), link, configuredScenario.getIcon(), configuredScenario.getCapabilities(), configuredScenario.getDisplayName(), configuredScenario.getDescription());

        // Iterate & check its providers
        for (String providerId : configuredScenario.getProviderIds()) {
            OnboardingProvider provider = providers.get(providerId);
            if (null == provider) {
                if (errorOnProviderAbsence) {
                    throw OnboardingExceptionCodes.INVALID_SCENARIO.create(configuredScenario.getId(), providerId);
                }
                if (configuredScenario.logWarningForAbsentProvider()) {
                    LOG.warn("No such provider '{}' available for configured scenario '{}'. Hence, scenario will not be available. Please check \"{}\" configuration file and align it to available/installed providers.", providerId, configuredScenario.getId(), OnboardingConfig.getScenariosConfigFileName());
                }
                return null;
            }
            scenario.addProvider(getProvider(providerId));
        }

        // Iterate & load its alternatives
        for (String alternativeId : configuredScenario.getAlternativeIds()) {
            ConfiguredScenario alternativeConfiguredScenario = configuredScenarios.get(alternativeId);
            if (null == alternativeConfiguredScenario) {
                LOG.warn("Alternative scenario '{}' does not exist in configured scenarios for '{}'", alternativeId, configuredScenario.getId());
            } else if (!alternativeConfiguredScenario.isEnabled()) {
                LOG.warn("Alternative scenario '{}' is not enabled in configured scenarios for '{}'", alternativeId, configuredScenario.getId());
            } else {
                Scenario alternative = getScenario(alternativeConfiguredScenario, configuredScenarios, false, userId, contextId);
                if (null != alternative) {
                    scenario.addAlternative(alternative);
                }
            }
        }

        return scenario;
    }

    private Link resolveLink(ConfiguredLink configuredLink, Session session) throws OXException {
        if (null == configuredLink) {
            return null;
        }

        if (false == configuredLink.isProperty()) {
            return new Link(configuredLink.getUrl(), configuredLink.getType());
        }

        // Look up the actual link by retrieving the denoted property
        String url = OnboardingUtility.getValueFromProperty(configuredLink.getUrl(), null, session);
        if (null == url) {
            // Property not defined
            LOG.warn("No such property providing the link: {}", configuredLink.getUrl());
            return null;
        }
        return new Link(url, configuredLink.getType());
    }

    private Link resolveLink(ConfiguredLink configuredLink, int userId, int contextId) throws OXException {
        if (null == configuredLink) {
            return null;
        }

        if (false == configuredLink.isProperty()) {
            return new Link(configuredLink.getUrl(), configuredLink.getType());
        }

        // Look up the actual link by retrieving the denoted property
        String url = OnboardingUtility.getValueFromProperty(configuredLink.getUrl(), null, userId, contextId);
        if (null == url) {
            // Property not defined
            LOG.warn("No such property providing the link: {}", configuredLink.getUrl());
            return null;
        }
        return new Link(url, configuredLink.getType());
    }

    @Override
    public OnboardingView getViewFor(Session session) throws OXException {
        Map<Device, List<CompositeId>> availableDevices;

        {
            Map<String, ConfiguredScenario> configuredScenarios = configuredScenariosReference.get();
            availableDevices = new EnumMap<Device, List<CompositeId>>(Device.class);
            for (Device device : Device.values()) {
                List<String> availableScenarios = device.getScenarios(session);
                if (null != availableScenarios && !availableScenarios.isEmpty()) {

                    List<CompositeId> compositeIds = availableDevices.get(device);
                    if (null == compositeIds) {
                        compositeIds = new ArrayList<CompositeId>(8);
                        availableDevices.put(device, compositeIds);
                    }

                    for (String scenarioId : availableScenarios) {
                        ConfiguredScenario configuredScenario = configuredScenarios.get(scenarioId);
                        if (null != configuredScenario && configuredScenario.isEnabled()) {
                            Scenario scenario = getScenario(configuredScenario, configuredScenarios, false, session);
                            if (null != scenario) {
                                compositeIds.add(new CompositeId(device, scenarioId));
                            }
                        }
                    }
                }
            }
        }

        OnboardingViewImpl view = new OnboardingViewImpl();
        view.add(availableDevices);
        return view;
    }

}
