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

import com.openexchange.admin.rmi.dataobjects.Resource;

/**
 * {@link ResourceFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ResourceFactory {

    /**
     * Creates a new {@link Resource} object
     * 
     * @param name The resource's name
     * @param displayName The resource's display name
     * @param email The resource's e-mail
     * @return The newly created {@link Resource} object
     */
    public static Resource createResource(String name, String displayName, String email) {
        Resource res = new Resource();
        res.setName(name);
        res.setDisplayname(displayName);
        res.setEmail(email);
        return res;
    }

    /**
     * Creates a new {@link Resource} object
     * 
     * @param name The name of the resource
     * @param displayName The display name of the resource
     * @param description The description of the resource
     * @param email The e-mail of the resource
     * @return The newly created {@link Resource} obejct
     */
    public static Resource createResource(String name, String displayName, String description, String email) {
        Resource resource = createResource(name, displayName, email);
        resource.setDescription(description);
        return resource;
    }

    /**
     * Creates a new {@link Resource} object
     * 
     * @param name The resource's name
     * @return The newly created resource
     */
    public static Resource createResource(String name) {
        return createResource(name, "displayname of resource " + name, "description of resource " + name, "resource-email-" + name + "@example.org");
    }
}
