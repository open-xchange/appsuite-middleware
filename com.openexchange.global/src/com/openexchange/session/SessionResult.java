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

/**
 * {@link SessionResult} - Result of a session look-up attempt.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class SessionResult<S extends Session> {

    private final Reply reply;
    private final S session;

    /**
     * Initializes a new {@link SessionResult}.
     *
     * @param reply The reply
     * @param session The associated session
     */
    public SessionResult(Reply reply, S session) {
        super();
        this.reply = reply;
        this.session = session;
    }

    /**
     * Gets the reply
     *
     * @return The reply
     */
    public Reply getReply() {
        return reply;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public S getSession() {
        return session;
    }

}
