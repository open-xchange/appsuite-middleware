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

package com.openexchange.session.inspector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;

/**
 * {@link SessionInspectorService} - A session inspector is called to possibly perform certain actions/operations
 * on various session-related triggers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public interface SessionInspectorService {

    /**
     * Called when an existing session was fetched from session container.
     *
     * @param session The fetched session
     * @param request The HTTP request
     * @param response The HTTP response
     * @return The reply; either {@link Reply#NEUTRAL} to select next in chain, {@link Reply#CONTINUE} to leave chain and signal to
     *         continue further processing or {@link Reply#STOP} to leave chain and signal to stop further processing
     * @throws OXException If inspector raises an exception
     */
    Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException;

    /**
     * Called when no such session for specified identifier exists in session container.
     *
     * @param sessionId The session identifier
     * @param request The HTTP request
     * @param response The HTTP response
     * @return The reply; either {@link Reply#NEUTRAL} to select next in chain, {@link Reply#CONTINUE} to leave chain and signal to
     *         continue further processing or {@link Reply#STOP} to leave chain and signal to stop further processing
     * @throws OXException If inspector raises an exception
     */
    Reply onSessionMiss(String sessionId, HttpServletRequest request, HttpServletResponse response) throws OXException;

    /**
     * Called when auto-login failed.
     * <p>
     * This call-back is invoked during login procedure, hence forcing a redirect needs to be initiated through throwing special
     * error code {@link com.openexchange.authentication.LoginExceptionCodes#REDIRECT} (<code>"LGI-0016"</code>), which is only used
     * as a workaround for a redirection, and is no real error.
     *
     * @param reason The reason for the failed auto-login
     * @param request The associated HTTP request
     * @param response The associated HTTP response
     * @return The reply; either {@link Reply#NEUTRAL} to select next in chain, {@link Reply#CONTINUE} to leave chain and signal to
     *         continue further processing or {@link Reply#STOP} to leave chain and signal to stop further processing
     * @throws OXException If inspector raises an exception
     */
    Reply onAutoLoginFailed(Reason reason, HttpServletRequest request, HttpServletResponse response) throws OXException;

}
