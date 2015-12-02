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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.onboarding.OnboardingProvider;
import com.openexchange.onboarding.OnboardingRequest;
import com.openexchange.onboarding.Result;
import com.openexchange.onboarding.ResultReply;
import com.openexchange.onboarding.Scenario;
import com.openexchange.onboarding.DefaultScenario;
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.DeviceAwareScenario;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.service.OnboardingService;
import com.openexchange.onboarding.service.OnboardingView;
import com.openexchange.session.Session;

/**
 * {@link OnboardingServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingServiceImpl extends ServiceTracker<OnboardingProvider, OnboardingProvider> implements OnboardingService, Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OnboardingServiceImpl.class);

    private final ConcurrentMap<String, OnboardingProvider> providers;
    private final AtomicReference<Map<String, ConfiguredScenario>> configuredScenariosReference;

    /**
     * Initializes a new {@link OnboardingServiceImpl}.
     *
     * @param context The bundle context
     * @param configuredScenarios The configured scenarios
     */
    public OnboardingServiceImpl(BundleContext context, Map<String, ConfiguredScenario> configuredScenarios) {
        super(context, OnboardingProvider.class, null);
        providers = new ConcurrentHashMap<String, OnboardingProvider>(32, 0.9F, 1);
        configuredScenariosReference = new AtomicReference<>(configuredScenarios);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            Map<String, ConfiguredScenario> scenarios = OnboardingInit.initScenarios(configService);
            configuredScenariosReference.set(scenarios);
        } catch (OXException e) {
            LOG.error("Failed to reload on-boarding scenarios", e);
        }
    }

    @Override
    public Map<String, String[]> getConfigFileNames() {
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    @Override
    public Collection<OnboardingProvider> getAllProviders() throws OXException {
        return Collections.unmodifiableCollection(providers.values());
    }

    @Override
    public OnboardingProvider getProvider(String id) throws OXException {
        OnboardingProvider configuration = null == id ? null : providers.get(id);
        if (null == configuration) {
            throw OnboardingExceptionCodes.NOT_FOUND.create(null == id ? "null" : id);
        }
        return configuration;
    }

    @Override
    public Collection<OnboardingProvider> getAvailableProvidersFor(Session session) throws OXException {
        return Collections.unmodifiableCollection(providers.values());
    }

    @Override
    public Result execute(OnboardingRequest request, Session session) throws OXException {
        if (null == request) {
            return null;
        }

        // Invocation chain - looping through providers
        Scenario scenario = request.getScenario();
        Result result = null;
        for (OnboardingProvider provider : scenario.getProviders(session)) {
            Result currentResult = provider.execute(request, result, session);
            ResultReply reply = currentResult.getReply();
            if (ResultReply.ACCEPT.equals(reply)) {
                // Return
                return currentResult;
            }
            if (ResultReply.DENY.equals(reply)) {
                // No further processing allowed
                throw OnboardingExceptionCodes.EXECUTION_DENIED.create(provider.getId(), scenario.getId());
            }
            // Otherwise NEUTRAL reply; next in chain
            result = currentResult;
        }

        return result;
    }

    @Override
    public Scenario getScenario(String scenarioId) throws OXException {
        if (null == scenarioId) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create("null");
        }

        Map<String, ConfiguredScenario> configuredScenarios = configuredScenariosReference.get();
        return getScenario(scenarioId, configuredScenarios);
    }

    @Override
    public DeviceAwareScenario getScenario(String scenarioId, Device device, Session session) throws OXException {
        if (null == scenarioId) {
            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create("null");
        }

        Map<String, ConfiguredScenario> configuredScenarios = configuredScenariosReference.get();
        Scenario scenario = getScenario(scenarioId, configuredScenarios);
        return new DeviceAwareScenarionImpl(scenario, device, Device.getActionsFor(device, scenario.getType(), session));
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
                Scenario scenario = getScenario(configuredScenario, configuredScenarios);
                scenarios.add(new DeviceAwareScenarionImpl(scenario, device, Device.getActionsFor(device, scenario.getType(), session)));
            }
        }
        return scenarios;
    }

    private Scenario getScenario(String scenarioId, Map<String, ConfiguredScenario> configuredScenarios) throws OXException {
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

        return getScenario(configuredScenario, configuredScenarios);
    }

    private Scenario getScenario(ConfiguredScenario configuredScenario, Map<String, ConfiguredScenario> configuredScenarios) throws OXException {
        DefaultScenario scenario = DefaultScenario.newInstance(configuredScenario.getId(), configuredScenario.getType(), configuredScenario.getIcon(), configuredScenario.getDisplayName(), configuredScenario.getDescription());

        for (String providerId : configuredScenario.getProviderIds()) {
            scenario.addProvider(getProvider(providerId));
        }

        for (String alternativeId : configuredScenario.getAlternativeIds()) {
            scenario.addAlternative(getScenario(alternativeId, configuredScenarios));
        }

        return scenario;
    }

    @Override
    public OnboardingView getViewFor(Session session) throws OXException {
        Map<Device, List<String>> availableDevices;

        {
            Map<String, ConfiguredScenario> configuredScenarios = configuredScenariosReference.get();
            availableDevices = new EnumMap<Device, List<String>>(Device.class);
            for (Device device : Device.values()) {
                List<String> availableScenarios = device.getScenarios(session);
                if (null != availableScenarios && !availableScenarios.isEmpty()) {

                    List<String> compositeIds = availableDevices.get(device);
                    if (null == compositeIds) {
                        compositeIds = new ArrayList<String>(8);
                        availableDevices.put(device, compositeIds);
                    }

                    for (String scenarioId : availableScenarios) {
                        ConfiguredScenario configuredScenario = configuredScenarios.get(scenarioId);
                        if (null == configuredScenario) {
                            throw OnboardingExceptionCodes.NO_SUCH_SCENARIO.create(scenarioId);
                        }

                        if (configuredScenario.isEnabled()) {
                            compositeIds.add(new StringBuilder(32).append(device.getId()).append('/').append(scenarioId).toString());
                        }
                    }
                }
            }
        }

        OnboardingViewImpl view = new OnboardingViewImpl();
        view.add(availableDevices);
        return view;
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    @Override
    public OnboardingProvider addingService(ServiceReference<OnboardingProvider> reference) {
        OnboardingProvider provider = context.getService(reference);
        if (null == providers.putIfAbsent(provider.getId(), provider)) {
            return provider;
        }

        LOG.warn("An on-boarding provider already exists with identifier {}. Ignoring {}", provider.getId(), provider.getClass().getName());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(ServiceReference<OnboardingProvider> reference, OnboardingProvider configuration) {
        providers.remove(configuration.getId());
        context.ungetService(reference);
    }

}
