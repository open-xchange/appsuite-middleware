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

package com.openexchange.event;

/**
 * {@link RemoteEvent} - Interface for remote event distributed by OSGi's event admin.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public interface RemoteEvent {

    /**
     * The event key for remote events.
     */
    public static final String EVENT_KEY = "OX_REMOTE_EVENT";

    /**
     * Constant for folder-changed action.
     */
    public static final int FOLDER_CHANGED = 1;

    /**
     * Constant for folder-content-changed action.
     */
    public static final int FOLDER_CONTENT_CHANGED = 2;

    /**
     * Gets the action.
     *
     * @return The action
     */
    public int getAction();

    /**
     * Gets the context ID.
     *
     * @return The context ID
     */
    public int getContextId();

    /**
     * Gets the folder ID.
     *
     * @return The folder ID
     */
    public int getFolderId();

    /**
     * Gets the user ID.
     *
     * @return The user ID
     */
    public int getUserId();

    /**
     * Gets the module.
     *
     * @return The module
     */
    public int getModule();

    /**
     * Gets the time stamp of the modification (if not available, <code>0</code> is returned).
     *
     * @return The time stamp of the modification (if not available, <code>0</code> is returned)
     */
    public long getTimestamp();

}
