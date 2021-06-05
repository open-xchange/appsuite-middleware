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

package com.openexchange.client.onboarding.internal;

import java.util.List;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.Platform;

/**
 * {@link ConfiguredDevice}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ConfiguredDevice {

    private final Platform platform;
    private final Device device;
    private final List<String> scenarios;

    /**
     * Initializes a new {@link ConfiguredDevice}.
     */
    public ConfiguredDevice(Platform platform, Device device, List<String> scenarios) {
        super();
        this.platform = platform;
        this.device = device;
        this.scenarios = scenarios;
    }

    /**
     * Gets the platform
     *
     * @return The platform
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Gets the device
     *
     * @return The device
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Gets the scenarios associated with this device
     *
     * @return The scenarios
     */
    public List<String> getScenarios() {
        return scenarios;
    }

}
