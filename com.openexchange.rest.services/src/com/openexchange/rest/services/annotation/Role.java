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

package com.openexchange.rest.services.annotation;

/**
 * {@link Role} - An enumeration for known security roles for REST end-points. A security role specifies how access to a certain REST
 * end-point is granted.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum Role {

    /**
     * The role identifier for {@link com.openexchange.rest.services.annotation.RoleAllowed} annotation and
     * {@link javax.annotation.security.RolesAllowed} annotation respectively signalling to perform basic-auth.
     * <p>
     * Properties <code>"com.openexchange.rest.services.basic-auth.login"</code> and
     * <code>"com.openexchange.rest.services.basic-auth.password"</code> are required to be set.
     */
    BASIC_AUTHENTICATED("Basic-Authenticated"),
    /**
     * The role identifier for {@link com.openexchange.rest.services.annotation.RoleAllowed} annotation and
     * {@link javax.annotation.security.RolesAllowed} annotation respectively allowing end-point-specific basic-auth.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * The concrete REST end-point is supposed to implement interface <code>com.openexchange.rest.services.EndpointAuthenticator</code>.
     * </div>
     */
    INDIVIDUAL_BASIC_AUTHENTICATED("Individual-Basic-Authenticated"),
    /**
     * The role identifier for {@link com.openexchange.rest.services.annotation.RoleAllowed} annotation and
     * {@link javax.annotation.security.RolesAllowed} annotation respectively signalling to perform basic-auth
     * against the Open-Xchange Server's master administrator credentials (the ones specified in <code>"mpasswd"</code> file).
     */
    MASTER_ADMIN_AUTHENTICATED("Master-Admin-Basic-Authenticated"),

    ;

    private final String id;

    private Role(String id) {
        this.id = id;
    }

    /**
     * Gets the role identifier.
     *
     * @return The role identifier
     */
    public String getId() {
        return id;
    }
}
