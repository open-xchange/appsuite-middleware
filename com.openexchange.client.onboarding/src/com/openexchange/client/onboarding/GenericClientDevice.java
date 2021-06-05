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

import java.util.EnumSet;
import java.util.Set;

/**
 * {@link GenericClientDevice} - The client device, which is the target for the on-boarding action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public enum GenericClientDevice implements ClientDevice {

    /**
     * <code>"desktop"</code> - The client is a Desktop PC
     */
    DESKTOP("desktop", EnumSet.of(Device.APPLE_MAC, Device.WINDOWS_DESKTOP_8_10)),
    /**
     * <code>"tablet"</code> - The client is a tablet device (possibly not supporting SMS)
     */
    TABLET("tablet", EnumSet.of(Device.ANDROID_TABLET, Device.APPLE_IPAD)),
    /**
     * <code>"smartphone"</code> - The client is a smart phone device
     */
    SMARTPHONE("smartphone", EnumSet.of(Device.ANDROID_PHONE, Device.APPLE_IPHONE)),
    ;

    private final String id;
    private final Set<Device> impliedDevices;

    private GenericClientDevice(String id, Set<Device> impliedDevices) {
        this.id = id;
        this.impliedDevices = impliedDevices;
    }

    @Override
    public boolean implies(Device device) {
        return null != device && impliedDevices.contains(device);
    }

    @Override
    public boolean matches(Device device) {
        return implies(device);
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the client device associated with specified identifier
     *
     * @param id The identifier to resolve
     * @return The client device or <code>null</code>
     */
    public static GenericClientDevice clientDeviceFor(String id) {
        if (null != id) {
            for (GenericClientDevice clientDevice : GenericClientDevice.values()) {
                if (id.equalsIgnoreCase(clientDevice.id)) {
                    return clientDevice;
                }
            }
        }
        return null;
    }

}
