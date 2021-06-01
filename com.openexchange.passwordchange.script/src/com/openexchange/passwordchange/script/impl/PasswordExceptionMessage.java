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

package com.openexchange.passwordchange.script.impl;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link PasswordExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class PasswordExceptionMessage implements LocalizableStrings {

    // Failed to change password for any reason. E.g. the script used to change a user's password advertised an error (return code)
    public final static String PASSWORD_FAILED_MSG = "Failed to change password for any reason.";

    // New password too short.
    public final static String PASSWORD_SHORT_MSG = "The entered password is too short. Please try again using a longer password.";

    // New password too weak.
    public final static String PASSWORD_WEAK_MSG = "The entered password is too weak. Please try again using a more complex password.";


    /**
     * Initializes a new {@link PasswordExceptionMessage}.
     */
    private PasswordExceptionMessage() {
        super();
    }

}
