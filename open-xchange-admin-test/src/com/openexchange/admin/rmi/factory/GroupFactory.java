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

package com.openexchange.admin.rmi.factory;

import com.openexchange.admin.rmi.dataobjects.Group;

/**
 * {@link GroupFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public final class GroupFactory {

    /**
     * Creates a group with the specified identity
     * 
     * @param identity The identity of the group
     * @return The new {@link Group}
     */
    public static final Group createGroup(String identity) {
        return createGroup("display name " + identity, identity);
    }

    /**
     * Creates a new group object with the specified name and display name
     * 
     * @param displayName The display name of the group
     * @param name The name of the group
     * @return The new {@link Group} object
     */
    public static final Group createGroup(String displayName, String name) {
        Group group = new Group();
        group.setDisplayname(displayName);
        group.setName(name);
        return group;
    }
}
