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
 * {@link ClientDevice} - The client device, which is the target for the on-boarding action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface ClientDevice {

    /** The special fall-back client device implying all devices */
    public static final ClientDevice IMPLIES_ALL = new ClientDevice() {

        @Override
        public boolean implies(Device device) {
            return true;
        }

        @Override
        public boolean matches(Device device) {
            return false;
        }

        @Override
        public String toString() {
            return "IMPLIES_ALL";
        }
    };

    /**
     * Checks if this client device implies the specified device.
     *
     * @param device The device that might be implied
     * @return <code>true</code> if specified device is implied; otherwise <code>false</code>
     */
    boolean implies(Device device);

    /**
     * Checks if this client device matches the specified device.
     *
     * @param device The device that might match
     * @return <code>true</code> if specified device matches; otherwise <code>false</code>
     */
    boolean matches(Device device);

}
