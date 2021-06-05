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

package com.openexchange.contact;

import com.openexchange.mail.MailSessionParameterNames;

/**
 * {@link MailSessionParameterNames}
 *
 * Constants used as keys for session parameters.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class ContactSessionParameterNames {

    private static final String PARAM_READONLY_CONNECTION = "contact.rconn";
    private static final String PARAM_WRITABLE_CONNECTION = "contact.wconn";

    /**
     * Prevent instantiation.
     */
    private ContactSessionParameterNames() {
        super();
    }

    /**
     * Gets the parameter name for a read-only database connection
     *
     * @return The parameter name
     */
    public static String getParamReadOnlyConnection() {
        return PARAM_READONLY_CONNECTION;
    }

    /**
     * Gets the parameter name for a writable database connection
     *
     * @return The parameter name
     */
    public static String getParamWritableConnection() {
        return PARAM_WRITABLE_CONNECTION;
    }

}
