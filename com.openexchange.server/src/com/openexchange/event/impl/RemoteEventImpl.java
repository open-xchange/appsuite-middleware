/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
