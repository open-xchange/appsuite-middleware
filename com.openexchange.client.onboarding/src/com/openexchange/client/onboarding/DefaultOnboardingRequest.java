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

package com.openexchange.client.onboarding;

import java.util.Map;
import com.openexchange.groupware.notify.hostname.HostData;

/**
 * {@link DefaultOnboardingRequest} - The default <code>OnboardingRequest</code> implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DefaultOnboardingRequest implements OnboardingRequest {

    private final Scenario scenario;
    private final OnboardingAction action;
    private final ClientDevice clientDevice;
    private final Device device;
    private final HostData hostData;
    private final Map<String, Object> input;

    /**
     * Initializes a new {@link DefaultOnboardingRequest}.
     *
     * @param scenario The scenario to execute
     * @param action The action to perform
     * @param clientDevice The client device, which is the target for the on-boarding action
     * @param device The associated device
     * @param hostData The host data
     * @param input The optional input or <code>null</code>
     */
    public DefaultOnboardingRequest(Scenario scenario, OnboardingAction action, ClientDevice clientDevice, Device device, HostData hostData, Map<String, Object> input) {
        super();
        this.scenario = scenario;
        this.action = action;
        this.clientDevice = clientDevice;
        this.device = device;
        this.hostData = hostData;
        this.input = input;
    }

    @Override
    public Scenario getScenario() {
        return scenario;
    }

    @Override
    public OnboardingAction getAction() {
        return action;
    }

    @Override
    public ClientDevice getClientDevice() {
        return clientDevice;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public HostData getHostData() {
        return hostData;
    }

    @Override
    public Map<String, Object> getInput() {
        return input;
    }

}
