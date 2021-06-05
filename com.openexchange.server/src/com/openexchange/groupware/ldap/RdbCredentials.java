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
 * This class implements the interface for user specific values if a relational
 * database is used instead of a directory service.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbCredentials implements Credentials {

    /**
     * The unique identifier of the user.
     */
    private final int userID;

    /**
     * Default constructor.
     * @param userID Unique identifier of the user.
     */
    RdbCredentials(final int userID) {
        super();
        this.userID = userID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(final String valueName) {
        String retval = null;
        if (USER_ID.equals(valueName)) {
            retval = Integer.toString(userID);
        }
        return retval;
    }
}
