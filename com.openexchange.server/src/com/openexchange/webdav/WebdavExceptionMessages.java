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

package com.openexchange.webdav;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link WebdavExceptionMessages}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class WebdavExceptionMessages implements LocalizableStrings {

    /**
     * Initializes a new {@link WebdavExceptionMessages}.
     */
    private WebdavExceptionMessages() {
        super();
    }
    
    /**
     * %1$s is not a number.
     */
    public final static String NOT_A_NUMBER_MSG = "%1$s is not a number.";

    /**
     * Empty passwords are not allowed.
     */
    public final static String EMPTY_PASSWORD_MSG = "Empty passwords are not allowed.";

    /**
     * Authentication failed for user name: %1$s
     */
    public final static String AUTH_FAILED_MSG = "Authentication failed for user name: %1$s";

}
