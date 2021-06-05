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

package com.openexchange.guest;

import java.io.Serializable;

/**
 * This class handles an assignment of a guest (identified by the mail address) to a context and user.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class GuestAssignment implements Serializable {

    private static final long serialVersionUID = 622650365568736720L;

    /**
     * The context the guest is assigned to.
     */
    private final int contextId;

    /**
     * The user id within the given context;
     */
    private final int userId;

    /**
     * The mail address the user is registered with
     */
    private final long guestId;

    /**
     * The password of the user
     */
    private final String password;

    /**
     * The mechanism the password is encrypted with
     */
    private final String passwordMech;

    /**
     * The salt used for the password
     */
    private final byte[] salt;

    /**
     * Initializes a new {@link GuestAssignment}.
     *
     * @param guestId - internal guest id of the user
     * @param contextId - context id the user is in
     * @param userId - user id in the context
     */
    public GuestAssignment(long guestId, int contextId, int userId, String password, String passwordMech, byte[] salt) {
        this.guestId = guestId;
        this.contextId = contextId;
        this.userId = userId;
        this.password = password;
        this.passwordMech = passwordMech;
        this.salt = salt;
    }

    /**
     * Gets the contextId
     *
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the guestId
     *
     * @return The guestId
     */
    public long getGuestId() {
        return guestId;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the passwordMech
     *
     * @return The passwordMech
     */
    public String getPasswordMech() {
        return passwordMech;
    }

    /**
     * Gets the salt
     *
     * @return The salt
     */
    public byte[] getSalt() {
        return salt;
    }
}
