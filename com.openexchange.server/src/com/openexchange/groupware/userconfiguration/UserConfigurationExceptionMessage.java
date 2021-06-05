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

package com.openexchange.groupware.userconfiguration;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link UserConfigurationExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class UserConfigurationExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link UserConfigurationExceptionMessage}.
     */
    private UserConfigurationExceptionMessage() {
        super();
    }

    /**
     * Configuration for user %1$s could not be found in context %2$d
     */
    public final static String NOT_FOUND_MSG = "Configuration for user %1$s could not be found in context %2$d.";

    /**
     * User configuration could not be put into cache: %1$s
     */
    public final static String CACHE_PUT_ERROR_MSG = "User configuration could not be put into cache: \"%1$s\".";

    /**
     * User configuration cache could not be cleared: %1$s
     */
    public final static String CACHE_CLEAR_ERROR_MSG = "User configuration cache could not be cleared: \"%1$s\".";

    /**
     * User configuration could not be removed from cache: %1$s
     */
    public final static String CACHE_REMOVE_ERROR_MSG = "User configuration could not be removed from cache: \"%1$s\".";

    /**
     * Mail settings for user %1$s could not be found in context %2$d
     */
    public final static String MAIL_SETTING_NOT_FOUND_MSG = "Mail settings for user \"%1$s\" could not be found in context %2$d.";

}
