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

package com.openexchange.folderstorage.database.contentType;

import com.openexchange.folderstorage.ContentType;

/**
 * {@link CalendarContentType} - The folder storage content type for calendar.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CalendarContentType implements ContentType {

    private static final CalendarContentType instance = new CalendarContentType();

    /**
     * Gets the {@link CalendarContentType} instance.
     *
     * @return The {@link CalendarContentType} instance
     */
    public static CalendarContentType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link CalendarContentType}.
     */
    private CalendarContentType() {
        super();
    }

    @Override
    public String toString() {
        return "calendar";
    }

    @Override
    public int getModule() {
        // From FolderObject.CALENDAR
        return 2;
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
