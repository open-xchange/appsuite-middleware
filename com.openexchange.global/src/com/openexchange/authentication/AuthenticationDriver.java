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

package com.openexchange.authentication;

/**
 * A authentication driver defines the methods for handling the login information.
 * <p>
 * E.g. the login information <code>user@domain.tld</code> is split into <code>user</code> and <code>domain.tld</code> and the context part
 * will be used to resolve the context while the user part will be used to authenticate the user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface AuthenticationDriver extends AuthenticationService {

    /** The property name for the service identifier */
    public static final String PROPERTY_ID = "driver.id";

    /**
     * Gets this driver's identifier.
     *
     * @return The identifier
     */
    String getId();

}
