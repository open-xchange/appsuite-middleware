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

package com.openexchange.find.common;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link CommonStrings} - Provides common i18n strings for find module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CommonStrings implements LocalizableStrings {

    /**
     * Initializes a new {@link CommonStrings}.
     */
    private CommonStrings() {
        super();
    }

    // ------------------------- i18n strings for facet types -------------------------------------- //

    public static final String FACET_TYPE_FOLDER_TYPE = "Folder type";

    public static final String FACET_TYPE_FOLDER = "Folder";

    // Search for items by their creation date
    public static final String DATE = "Date";

    // Search within a certain account of a module
    public static final String ACCOUNT = "Account";

    // ------------------------- i18n strings for folder types ------------------------------------- //

    public static final String FOLDER_TYPE_PRIVATE = "Private";

    public static final String FOLDER_TYPE_PUBLIC = "Public";

    public static final String FOLDER_TYPE_SHARED = "Shared";

    // ------------------------- i18n strings for facet values -------------------------------------- //

    // Search mails from last week
    public static final String LAST_WEEK = "last week";

    // Search mails from last month
    public static final String LAST_MONTH = "last month";

    // Search mails from last year
    public static final String LAST_YEAR = "last year";


    public static final String FACET_VALUE_TRUE = "true";

    public static final String FACET_VALUE_FALSE = "false";
}
