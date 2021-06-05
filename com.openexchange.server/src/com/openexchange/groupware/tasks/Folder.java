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

package com.openexchange.groupware.tasks;

import java.util.Collections;
import java.util.Set;

/**
 * Stores a folder that contains a task.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Folder {

    /**
     * An empty set of folders.
     */
    static final Set<Folder> EMPTY = Collections.emptySet();

    /**
     * Unique identifier of the folder.
     */
    private final int identifier;

    /**
     * Unique identifier of the user or <code>MAIN_FOLDER</code> if the folder
     * is the main folder of the task.
     */
    private final int user;

    /**
     * Default constructor.
     * @param folder unique identifier of the folder the task must appear in.
     * @param user unique identifier of the user or <code>0</code> if the
     * folder is the main folder of the task.
     */
    public Folder(final int folder, final int user) {
        super();
        this.identifier = folder;
        this.user = user;
    }

    /**
     * @return Returns the folder.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * @return Returns the user.
     */
    int getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Folder)) {
            return false;
        }
        return identifier == ((Folder) obj).identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TaskFolder: " + getIdentifier() + ", User: " + getUser();
    }
}
