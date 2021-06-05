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

package com.openexchange.multifactor.lockoutService.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MultifactorLockoutExceptionMessages}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorLockoutExceptionMessages implements LocalizableStrings {

    /**
     * The user is locked out of the account for specified minutes
     */
    public static final String MULTIFACTOR_LOCKOUT = "This account has been locked out. Please wait %1$d minutes then try again.";
    /**
     * Another authentication is already being processed, but in delay due to bad attempts.  User needs to wait
     */
    public static final String IN_DELAY = "There is another authentication attempt for this user in process.  Please wait.";

    private MultifactorLockoutExceptionMessages () {
        super();
    }

}
