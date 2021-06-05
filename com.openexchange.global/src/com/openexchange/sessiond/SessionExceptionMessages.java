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

package com.openexchange.sessiond;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SessionExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class SessionExceptionMessages implements LocalizableStrings {

    public static final String MAX_SESSION_EXCEPTION_MSG = "The maximum number of sessions is exceeded. Please try again later.";
    public static final String PASSWORD_UPDATE_FAILED_MSG = "The password could not be changed in the current session. Please login again.";
    public static final String MAX_SESSION_PER_USER_EXCEPTION_MSG = "The maximum number of sessions is exceeded for your account. Please logout from other clients and try again.";
    public static final String SESSION_EXPIRED_MSG = "Your session expired. Please login again.";
    public static final String CONTEXT_LOCKED_MSG = "The account \"%2$s\" is currently not enabled. Please try again later.";
    public static final String MAX_SESSION_PER_CLIENT_EXCEPTION_MSG = "The maximum number of sessions is exceeded for client %1$s. Please logout from other clients and try again.";
    public static final String NO_SESSION_FOR_TOKENS_MSG = "The session is no longer available. Please try again.";
    public static final String KERBEROS_TICKET_MISSING_MSG = "Kerberos ticket in session is missing. Pleasy try again.";

    private SessionExceptionMessages() {
        super();
    }
}
