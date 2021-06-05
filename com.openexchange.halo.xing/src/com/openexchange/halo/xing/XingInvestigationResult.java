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

package com.openexchange.halo.xing;

import com.openexchange.xing.Contacts;
import com.openexchange.xing.Path;
import com.openexchange.xing.User;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class XingInvestigationResult {

    private final User user;

    private Path shortestPath;

    private Contacts sharedContacts;

    /**
     * Initializes a new {@link XingInvestigationResult}.
     * @param user The user found in XING. Possibly <code>null</code>.
     *
     */
    public XingInvestigationResult(User user) {
        super();
        this.user = user;
    }

    /**
     * The found user.
     *
     * @return the {@link User} or <code>null</code>.
     */
    public User getUser() {
        return user;
    }

    /**
     * The shortest path from the session
     * users XING contact to the found one.
     *
     * @return The path or <code>null</code>.
     */
    public Path getShortestPath() {
        return shortestPath;
    }

    /**
     * Sets the shortest path from the session users XING contact to the found one.
     *
     * @param shortestPath The path.
     */
    public void setShortestPath(Path shortestPath) {
        this.shortestPath = shortestPath;
    }

    /**
     * Gets the contacts shared between the session user and the target user.
     *
     * @return The contacts or <code>null</code>.
     */
    public Contacts getSharedContacts() {
        return sharedContacts;
    }

    /**
     * Sets the contacts shared between the session user and the target user.
     *
     * @param sharedContacts The shared contacts.
     */
    public void setSharedContacts(Contacts sharedContacts) {
        this.sharedContacts = sharedContacts;
    }

}
