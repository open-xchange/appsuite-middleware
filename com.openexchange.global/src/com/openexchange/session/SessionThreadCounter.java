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

package com.openexchange.session;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


/**
 * {@link SessionThreadCounter} - Manages tracked thread counts for active sessions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessionThreadCounter {

    /**
     * The reference for registered singleton service.
     */
    public static final AtomicReference<SessionThreadCounter> REFERENCE = new AtomicReference<SessionThreadCounter>();

    /**
     * The event topic for sessions exceeding configured threshold: <code>"com/openexchange/session/exceededThreshold"</code>.
     */
    public static final String EVENT_TOPIC = "com/openexchange/session/exceededThreshold".intern();

    /**
     * The event property providing session identifier. <code>java.lang.String</code>
     */
    public static final String EVENT_PROP_SESSION_ID = "__sessionId".intern();

    /**
     * The event property providing thread count entry. <code>com.openexchange.session.ThreadCountEntry</code>
     */
    public static final String EVENT_PROP_ENTRY = "__entry".intern();

    /**
     * Gets those sessions' available threads whose total number is equal to or greater than specified threshold at invocation time.
     *
     * @param threshold The threshold
     * @return The available sessions' threads exceeding respectively equal to given threshold
     */
    Map<String, Set<Thread>> getThreads(int threshold);

    /**
     * (Atomically) Increments the tracked thread count for denoted session.
     *
     * @param sessionId The session identifier
     * @return The updated value
     */
    int increment(String sessionId);

    /**
     * (Atomically) Decrements the tracked thread count for denoted session.
     *
     * @param sessionId The session identifier
     * @return The updated value
     */
    int decrement(String sessionId);

    /**
     * Clears all tracked thread counts.
     */
    void clear();

    /**
     * Removes the tracked thread count for denoted session.
     *
     * @param sessionId The session identifier
     */
    void remove(String sessionId);

}
