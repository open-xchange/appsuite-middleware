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

package com.openexchange.client.onboarding.service;

import java.util.Collection;
import java.util.List;
import com.openexchange.client.onboarding.ClientDevice;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.DeviceAwareScenario;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingRequest;
import com.openexchange.client.onboarding.ResultObject;
import com.openexchange.client.onboarding.Scenario;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link OnboardingService} - The on-boarding service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
@SingletonService
public interface OnboardingService {

    /**
     * Gets all currently registered {@link OnboardingProvider providers}.
     *
     * @return All providers
     * @throws OXException If providers cannot be returned
     */
    Collection<OnboardingProvider> getAllProviders() throws OXException;

    /**
     * Gets the specified {@link OnboardingProvider provider}.
     *
     * @return The specified provider
     * @throws OXException If provider cannot be returned
     */
    OnboardingProvider getProvider(String id) throws OXException;

    /**
     * Gets the currently available {@link OnboardingProvider providers} for the session-associated user.
     *
     * @param session The session
     * @return The currently available providers
     * @throws OXException If providers cannot be returned
     */
    Collection<OnboardingProvider> getAvailableProvidersFor(Session session) throws OXException;

    /**
     * Gets the on-boarding view for specified session
     *
     * @param clientDevice The target device
     * @param session The session
     * @return The on-boarding view
     * @throws OXException If view cannot be returned
     */
    OnboardingView getViewFor(ClientDevice clientDevice, Session session) throws OXException;

    /**
     * Gets the scenario by specified identifier
     *
     * @param scenarioId The scenario identifier
     * @param session The session
     * @return The scenario
     * @throws OXException If scenario cannot be returned
     */
    Scenario getScenario(String scenarioId, Session session) throws OXException;

    /**
     * Checks if specified scenario is capability- / permission-wise available to given user.
     *
     * @param scenarioId The scenario identifier
     * @param session The session
     * @return <code>true</code> if available; otherwise <code>false</code>
     * @throws OXException If availability cannot be checked
     */
    boolean isAvailableFor(String scenarioId, Session session) throws OXException;

    /**
     * Gets the device-aware scenario by specified identifier
     *
     * @param scenarioId The scenario identifier
     * @param clientDevice The client device, which is the target for the on-boarding action
     * @param device The associated device
     * @param session The session
     * @return The device-aware scenario
     * @throws OXException If scenario cannot be returned
     */
    DeviceAwareScenario getScenario(String scenarioId, ClientDevice clientDevice, Device device, Session session) throws OXException;

    /**
     * Gets the device-aware scenario by specified identifier
     *
     * @param scenarioId The scenario identifier
     * @param clientDevice The client device, which is the target for the on-boarding action
     * @param device The associated device
     * @param userId The user id
     * @param contextId The context id
     * @return The device-aware scenario
     * @throws OXException If scenario cannot be returned
     */
    DeviceAwareScenario getScenario(String scenarioId, ClientDevice clientDevice, Device device, int userId, int contextId) throws OXException;

    /**
     * Gets the device-aware scenarios for specified device.
     *
     * @param clientDevice The client device, which is the target for the on-boarding action
     * @param device The device
     * @param session The session
     * @return The scenarios for specified device
     * @throws OXException If scenarios cannot be returned
     */
    List<DeviceAwareScenario> getScenariosFor(ClientDevice clientDevice, Device device, Session session) throws OXException;

    /**
     * Executes the denoted scenario
     *
     * @param request The on-boarding request
     * @param session The session
     * @return The result object
     * @throws OXException If scenario cannot be returned
     */
    ResultObject execute(OnboardingRequest request, Session session) throws OXException;

}
