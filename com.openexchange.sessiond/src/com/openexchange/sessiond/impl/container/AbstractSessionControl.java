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

package com.openexchange.sessiond.impl.container;

import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionImpl;

/**
 * {@link AbstractSessionControl} - Holds a {@link Session} instance and remembers life-cycle time stamps such as last-accessed, creation-time, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractSessionControl implements SessionControl {

    /**
     * Time stamp when this session control was created.
     */
    protected final long creationTime;

    /**
     * The associated session.
     */
    protected final SessionImpl session;

    /**
     * Initializes a new {@link AbstractSessionControl}
     *
     * @param session The stored session
     * @param creationTime The creation time to apply
     */
    protected AbstractSessionControl(SessionImpl session, long creationTime) {
        super();
        this.session = session;
        this.creationTime = creationTime;
    }

    @Override
    public String getSessionID() {
        return session.getSessionID();
    }

    @Override
    public SessionImpl getSession() {
        return session;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equalsContext(int contextId) {
        return session.getContextId() == contextId;
    }

    @Override
    public boolean equalsUserAndContext(int userId, int contextId) {
        return session.getContextId() == contextId && session.getUserId() == userId;
    }

    @Override
    public int getContextId() {
        return session.getContextId();
    }

    @Override
    public int getUserId() {
        return session.getUserId();
    }

}
