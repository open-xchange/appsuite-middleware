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

package com.openexchange.groupware.ldap;

/**
 * This interface is used to resolve credentials for directory service
 * connections.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface Credentials {

    /**
     * Name of the value that contains the login name of the user.
     */
    final String LOGIN_NAME = "username";

    /**
     * Name of the value that contains the password of the user.
     */
    final String PASSWORD = "password";

    /**
     * Name of the value that contains the full distinguished name of the user
     * object in the directory service.
     */
    final String USER_DN = "userDN";

    /**
     * Unique ID of the user
     */
	final String USER_ID = "userID";

    /**
     * This method is used to resolve some credentials for directory service
     * connections. This method should be able to resolve the following names:
     * <ul>
     * <li>username: The login name of the user.</li>
     * <li>password: The password of the user.</li>
     * <li>userDN: The full distinguished name of the user that should be used
     * if users aren't located under the same baseDN.</li>
     * </ul>
     * @param valueName Name of the value that should be returned.
     * @return the value of the attribute that name has been given.
     */
    String getValue(String valueName);

}
