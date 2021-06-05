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
 * {@link TaskContentType} - The folder storage content type for tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TaskContentType implements ContentType {

    private static final TaskContentType instance = new TaskContentType();

    /**
     * Gets the {@link TaskContentType} instance.
     *
     * @return The {@link TaskContentType} instance
     */
    public static TaskContentType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link TaskContentType}.
     */
    private TaskContentType() {
        super();
    }

    @Override
    public String toString() {
        return "tasks";
    }

    @Override
    public int getModule() {
        // From FolderObject.TASK
        return 1;
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
