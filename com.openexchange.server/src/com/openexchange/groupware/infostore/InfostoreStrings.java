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

package com.openexchange.groupware.infostore;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link InfostoreStrings} - Localizable strings for InfoStore module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class InfostoreStrings implements LocalizableStrings {

    /**
     * Initializes a new {@link InfostoreStrings}.
     */
    public InfostoreStrings() {
        super();
    }

    // A document's title
    public static final String FIELD_TITLE = "Title";

    // A document's description
    public static final String FIELD_DESCRIPTION = "Description";

    // A document's file name
    public static final String FIELD_FILE_NAME = "File name";

    // A document's version comment
    public static final String FIELD_VERSION_COMMENT = "Version comment";

}
