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

package com.openexchange.mail.compose;

/**
 * {@link Address} - The address representation consisting of a personal and an address string.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class Address {

    private final String personal;
    private final String address;

    /**
     * Initializes a new {@link Address}.
     *
     * @param personal The personal part
     * @param address The address
     */
    public Address(String personal, String address) {
        super();
        this.personal = personal;
        this.address = address;
    }

    /**
     * Gets the personal
     *
     * @return The personal or <code>null</code>
     */
    public String getPersonal() {
        return personal;
    }

    /**
     * Gets the address
     *
     * @return The address
     */
    public String getAddress() {
        return address;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((personal == null) ? 0 : personal.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Address)) {
            return false;
        }
        Address other = (Address) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (personal == null) {
            if (other.personal != null) {
                return false;
            }
        } else if (!personal.equals(other.personal)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (personal == null) {
            return address == null ? "<empty>" : address;
        }
        if (address == null) {
            return personal;
        }
        return new StringBuilder(personal.length() + address.length() + 5).append('"').append(personal).append("\" <").append(address).append('>').toString();
    }

}
