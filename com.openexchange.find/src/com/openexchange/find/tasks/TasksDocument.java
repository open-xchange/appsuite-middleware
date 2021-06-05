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

package com.openexchange.find.tasks;

import com.openexchange.find.Document;
import com.openexchange.find.DocumentVisitor;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link TasksDocument} - The document for a file.
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class TasksDocument implements Document {

    private static final long serialVersionUID = 8600470657595326510L;

    private final Task task;

    /**
     * Initializes a new {@link TasksDocument}.
     *
     * @param task
     */
    public TasksDocument(final Task task) {
        super();
        this.task = task;
    }

    /**
     * Gets the file
     *
     * @return The file
     */
    public Task getTask() {
        return task;
    }

    @Override
    public void accept(DocumentVisitor visitor) {
        visitor.visit(this);
    }

}
