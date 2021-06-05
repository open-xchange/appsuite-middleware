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

package com.openexchange.mail.json.compose.share;

import com.openexchange.user.User;

/**
 * {@link Recipient}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Recipient {

    /**
     * Creates a recipient for an internal user.
     *
     * @param personal The personal
     * @param address The address
     * @param user The user
     * @return The created recipient
     */
    public static Recipient createInternalRecipient(String personal, String address, User user) {
        return new Recipient(personal, address, user);
    }

    /**
     * Creates a recipient for an external address.
     *
     * @param personal The personal
     * @param address The address
     * @return The created recipient
     */
    public static Recipient createExternalRecipient(String personal, String address) {
        return new Recipient(personal, address, null);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final String personal;
    private final String address;
    private final int hash;
    private final User user;

    /**
     * Initializes a new {@link Recipient}.
     */
    private Recipient(String personal, String address, User user) {
        super();
        this.personal = personal;
        this.address = address;
        this.user = user;

        int prime = 31;
        int result = prime * 1 + ((address == null) ? 0 : address.hashCode());
        hash = result;
    }

    /**
     * Gets the personal
     *
     * @return The personal
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

    /**
     * Checks if this recipient denotes an internal user.
     *
     * @return <code>true</code> if this recipient denotes an internal user; otherwise <code>false</code>
     */
    public boolean isUser() {
        return null != user;
    }

    /**
     * Gets the user
     *
     * @return The user or <code>null</code> if this recipient denotes an external contact
     */
    public User getUser() {
        return user;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Recipient)) {
            return false;
        }
        Recipient other = (Recipient) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        return true;
    }

}
