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

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.client.onboarding.ClientDevice;
import com.openexchange.client.onboarding.CompositeId;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.Platform;

/**
 * {@link OnboardingView} - Represents a certain on-boarding view associated with a certain session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface OnboardingView {

    /**
     * Gets the client device associated with this view
     *
     * @return The client device
     */
    ClientDevice getClientDevice();

    /**
     * Gets the available platforms for this view
     *
     * @return The platforms
     */
    Set<Platform> getPlatforms();

    /**
     * Gets the available devices for this view
     *
     * @return The devices
     */
    Map<Device, List<CompositeId>> getDevices();

}
