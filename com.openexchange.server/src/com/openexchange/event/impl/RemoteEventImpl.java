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

package com.openexchange.event.impl;

import com.openexchange.event.RemoteEvent;

/**
 * {@link RemoteEventImpl} - Implementation of {@link RemoteEvent}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RemoteEventImpl implements RemoteEvent {

    private final int contextId;
    private final int userId;
    private final int module;
    private final int action;
    private final int folderId;
    private final long timestamp;

    /**
     * Initializes a new {@link RemoteEventImpl}.
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param contextId The context ID
     * @param action The action; either {@link RemoteEvent#FOLDER_CHANGED} or {@link RemoteEvent#FOLDER_CONTENT_CHANGED}
     * @param module The module
     * @param timestamp The time stamp of the modification or <code>0</code> if not available
     */
    public RemoteEventImpl(final int folderId, final int userId, final int contextId, final int action, final int module, final long timestamp) {
        super();
        this.folderId = folderId;
        this.userId = userId;
        this.contextId = contextId;
        this.action = action;
        this.module = module;
        this.timestamp = timestamp;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getModule() {
        return module;
    }

    @Override
    public int getAction() {
        return action;
    }

    @Override
    public int getFolderId() {
        return folderId;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(256);
        builder.append("RemoteEventImpl [contextId=").append(contextId).append(", userId=").append(userId);
        builder.append(", module=").append(module).append(", action=").append(action).append(", folderId=").append(folderId);
        builder.append(", timestamp=").append(timestamp).append(']');
        return builder.toString();
    }

}
