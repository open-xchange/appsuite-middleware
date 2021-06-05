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

package com.openexchange.mail;

/**
 * A simple wrapper class for archive actions.
 *
 * @since v7.10.4
 */
public class ArchiveDataWrapper {

    private final String id;
    private final boolean created;

    /**
     * Initializes a new {@link ArchiveDataWrapper}.
     *
     * @param id The identifier of the (sub-)archive folder
     * @param created <code>true</code> if folder has been created; otherwise <code>false</code>
     */
    public ArchiveDataWrapper(String id, boolean created) {
        this.id = id;
        this.created = created;
    }

    /**
     * Gets the identifier of the (sub-)archive folder.
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Signals whether that folder has been created or not.
     *
     * @return <code>true</code> if created; otherwise <code>false</code>
     */
    public boolean isCreated() {
        return created;
    }
}