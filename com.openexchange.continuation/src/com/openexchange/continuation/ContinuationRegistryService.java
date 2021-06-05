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

package com.openexchange.continuation;

import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ContinuationRegistryService} - The continuation registry service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
@SingletonService
public interface ContinuationRegistryService {

    /**
     * Gets the continuation for given UUID and session.
     *
     * @param uuid The UUID
     * @param session The session
     * @return The continuation or <code>null</code> if there is no such continuation
     * @throws OXException If continuation cannot be returned for any reason
     */
    <V> Continuation<V> getContinuation(UUID uuid, Session session) throws OXException;

    /**
     * Puts given continuation into registry using specified session.
     *
     * @param continuation The continuation
     * @param session The session
     * @throws OXException If put into registry fails
     */
    <V> void putContinuation(Continuation<V> continuation, Session session) throws OXException;

    /**
     * Removes specified continuation from registry using specified session.
     *
     * @param uuid The UUID
     * @param session The session
     * @throws OXException If put into registry fails
     */
    void removeContinuation(UUID uuid, Session session) throws OXException;

}
