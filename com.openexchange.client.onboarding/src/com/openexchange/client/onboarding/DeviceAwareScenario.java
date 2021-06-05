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

import java.util.Collection;
import java.util.List;
import com.openexchange.session.Session;

/**
 * {@link DeviceAwareScenario} - An on-boarding scenario parameterized with a certain device.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public interface DeviceAwareScenario extends Scenario {

    /**
     * Gets the associated device for this scenario.
     *
     * @return The device
     */
    Device getDevice();

    /**
     * Gets the associated on-boarding actions for this device-aware scenario.
     *
     * @return The actions
     */
    List<OnboardingAction> getActions();

    /**
     * Gets the composite identifier for this device-aware scenario
     *
     * @return The composite identifier
     */
    CompositeId getCompositeId();

    /**
     * Gets the list of missing capabilities, which are required in order to apply this scenario.
     *
     * @param session The session providing user information
     * @return The list of missing capabilities or an empty list if all are satisfied
     */
    Collection<String> getMissingCapabilities(Session session);

}
