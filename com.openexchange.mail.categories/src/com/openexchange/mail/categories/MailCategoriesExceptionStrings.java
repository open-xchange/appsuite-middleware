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

package com.openexchange.mail.categories;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MailCategoriesExceptionStrings}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesExceptionStrings implements LocalizableStrings {

    /**
     * Prevents instantiation
     */
    private MailCategoriesExceptionStrings() {}

    // The user category %1$s does not exist.
    public static final String USER_CATEGORY_DOES_NOT_EXIST = "The user category %1$s does not exist.";

    // Invalid configuration: %1$s
    public static final String INVALID_CONFIGURATION_EXTENDED = "Invalid configuration: %1$s";

    // Invalid configuration: %1$s
    public static final String INVALID_CONFIGURATION = "Invalid configuration: %1$s";

    // The required service %1$s is temporary not available. Please try again later.
    public static final String SERVICE_UNAVAILABLE_MSG = "The required service %1$s is temporary not available. Please try again later.";

    // You are not allowed to change the name of the category %1$s.
    public static final String CHANGE_NAME_NOT_ALLOWED = "You are not allowed to change the name of the category %1$s.";

    // You are not allowed to enable or disable the category %1$s.
    public static final String SWITCH_NOT_ALLOWED = "You are not allowed to enable or disable the category %1$s.";
}
