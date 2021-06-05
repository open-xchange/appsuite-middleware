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
 * {@link OnboardingRequest} - Represents an on-boarding request from a certain selection a user has chosen from configuration tree.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface OnboardingRequest {

    /**
     * Gets the on-boarding scenario to execute
     *
     * @return The on-boarding scenario
     */
    Scenario getScenario();

    /**
     * Gets the action to perform
     *
     * @return The action
     */
    OnboardingAction getAction();

    /**
     * Gets the client device to which the actions apply
     *
     * @return The client device
     */
    ClientDevice getClientDevice();

    /**
     * Gets the device to which the scenario applies
     *
     * @return The device
     */
    Device getDevice();

    /**
     * Gets the associated host data
     *
     * @return The host data
     */
    HostData getHostData();

    /**
     * Gets the optional form content
     *
     * @return The form content or <code>null</code>
     */
    Map<String, Object> getInput();
}
