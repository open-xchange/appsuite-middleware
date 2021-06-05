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

package com.openexchange.sessiond.impl;

import com.openexchange.exception.OXException;

/**
 * {@link SessionIdGenerator} - The session ID generator
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public abstract class SessionIdGenerator {

    /**
     * Initializes a new {@link SessionIdGenerator}
     */
    protected SessionIdGenerator() {
        super();
    }

    /**
     * Gets the session ID generator.
     *
     * @return The session ID generator.
     */
    public static SessionIdGenerator getInstance() {
        return UUIDSessionIdGenerator.getInstance();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new session identifier.
     *
     * @param loginName The login name associated with the new session
     * @return The session identifier
     * @throws OXException If creating the session identifier fails
     */
    public abstract String createSessionId(String loginName) throws OXException;

    /**
     * Creates a new secret identifier.
     *
     * @param loginName The login name associated with the new session
     * @return The secret identifier
     * @throws OXException If creating the secret identifier fails
     */
    public abstract String createSecretId(String loginName) throws OXException;

    /**
     * Creates a random identifier.
     *
     * @return The random identifier
     * @throws OXException If creating the random identifier fails
     */
    public abstract String createRandomId() throws OXException;

}
