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

package com.openexchange.folderstorage.calendar;

import com.openexchange.folderstorage.FolderField;

/**
 * {@link CalendarConfigField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarConfigField extends FolderField {

    /** The column identifier of the field as used in the HTTP API */
    private static final int COLUMN_ID = 3205;

    /** The column name of the field as used in the HTTP API */
    private static final String COLUMN_NAME = "com.openexchange.calendar.config";

    private static final long serialVersionUID = 9084693144002186075L;
    private static final CalendarConfigField INSTANCE = new CalendarConfigField();

    /**
     * Gets the extended properties field instance.
     *
     * @return The instance
     */
    public static CalendarConfigField getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link CalendarConfigField}.
     */
    private CalendarConfigField() {
        super(COLUMN_ID, COLUMN_NAME, null);
    }

}
