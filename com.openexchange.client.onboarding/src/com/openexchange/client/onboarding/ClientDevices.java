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


/**
 * {@link ClientDevices} - Helper class for client devices.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class ClientDevices {

    /**
     * Initializes a new {@link ClientDevices}.
     */
    private ClientDevices() {
        super();
    }

    /**
     * Gets the client device for specified identifier with fall-back to {@link ClientDevice#IMPLIES_ALL}.
     *
     * @param id The identifier to resolve
     * @return The resolved client device or given {@code ClientDevice#IMPLIES_ALL}
     */
    public static ClientDevice getClientDeviceFor(String id) {
        return getClientDeviceFor(id, ClientDevice.IMPLIES_ALL);
    }

    /**
     * Gets the client device for specified identifier.
     *
     * @param id The identifier to resolve
     * @param def The default client device to return if passed identifier cannot be resolved
     * @return The resolved client device or given <code>def</code>
     */
    public static ClientDevice getClientDeviceFor(String id, ClientDevice def) {
        if (null == id) {
            return def;
        }

        Device device = Device.deviceFor(id);
        if (null != device) {
            return ConcreteClientDevice.concreteClientDeviceFor(device);
        }

        GenericClientDevice genericClientDevice = GenericClientDevice.clientDeviceFor(id);
        return null == genericClientDevice ? def : genericClientDevice;
    }

}
