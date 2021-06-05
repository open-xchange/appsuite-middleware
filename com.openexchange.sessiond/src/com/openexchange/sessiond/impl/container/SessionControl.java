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
 * {@link SessionControl} - Holds a {@link Session} instance and remembers life-cycle time stamps such as last-accessed, creation-time, etc.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessionControl {

    /** The container type; either short-term or long-term */
    public static enum ContainerType {
        /** Instance is managed in short-term container */
        SHORT_TERM,
        /** Instance is managed in long-term container */
        LONG_TERM;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the type of the container, in which this instance is managed.
     *
     * @return The container type
     */
    ContainerType geContainerType();

    /**
     * Gets the session identifier
     *
     * @return The session identifier
     * @see com.openexchange.sessiond.impl.SessionImpl#getSessionID()
     */
    String getSessionID();

    /**
     * Gets the stored session
     *
     * @return The stored session
     */
    SessionImpl getSession();

    /**
     * Gets the creation-time time stamp
     *
     * @return The creation-time time stamp
     */
    long getCreationTime();

    /**
     * Checks if the session associated with this control holds specified context identifier
     *
     * @param contextId The context identifier to check against
     * @return <code>true</code> if session holds specified context identifier; otherwise <code>false</code>
     */
    boolean equalsContext(int contextId);

    /**
     * Checks if the session associated with this control holds specified user and context identifier
     *
     * @param userId The user identifier to check against
     * @param contextId The context identifier to check against
     * @return <code>true</code> if session holds specified user and context identifier; otherwise <code>false</code>
     */
    boolean equalsUserAndContext(int userId, int contextId);

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    int getUserId();

}
