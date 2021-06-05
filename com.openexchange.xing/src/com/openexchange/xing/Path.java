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

package com.openexchange.xing;

import java.util.List;


/**
 * Encapsulates an inclusive path between two XING users.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class Path {

    private final User from;

    private final User to;

    private final List<User> inBetween;

    /**
     * Initializes a new {@link Path}.
     *
     * @param from The start user; never <code>null</code>.
     * @param to The end user; never <code>null</code>.
     * @param inBetween The users in between; never <code>null</code>.
     */
    public Path(User from, User to, List<User> inBetween) {
        super();
        this.from = from;
        this.to = to;
        this.inBetween = inBetween;
    }

    /**
     * Gets the start user of the path.
     *
     * @return The user; never <code>null</code>.
     */
    public User getFrom() {
        return from;
    }

    /**
     * Gets the final user of the path.
     *
     * @return The user; never <code>null</code>.
     */
    public User getTo() {
        return to;
    }

    /**
     * All users in between.
     *
     * @return A list of users, that may be empty. Never <code>null</code>.
     */
    public List<User> getInBetween() {
        return inBetween;
    }

    /**
     * Returns if both users are connected directly with each other.
     *
     * @return <code>true</code> if they are, otherwise <code>false</code>.
     */
    public boolean isDirectConnection() {
        return inBetween.isEmpty();
    }

}
