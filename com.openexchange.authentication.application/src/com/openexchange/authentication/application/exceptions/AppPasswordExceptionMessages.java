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

package com.openexchange.authentication.application.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link AppPasswordExceptionMessages}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordExceptionMessages implements LocalizableStrings {

    // The account is not authorized to perform this action.  Displayed when a login without full permission tries to perform action requiring full priv
    public static String NOT_AUTHORIZED = "This account isn't authorized to perform this action.";
    // The type of application isn't recognized
    public static String UNKNOWN_APPLICATION_TYPE = "Unknown application type $1%s";
    // Unable to update application passwords during a password change action.
    public static String UNABLE_TO_UPDATE_APP_PASSWORD = "Unable to update application passwords during password change. You may need to remove then recreate the application passwords.";

    public static final String MISSING_CAPABILITY_MSG = "The operation could not be completed due to missing capabilities.";

    public static String UNSUPPORTED_CLIENT_MSG = "This client cannot be used with an application-specific password.";

}
