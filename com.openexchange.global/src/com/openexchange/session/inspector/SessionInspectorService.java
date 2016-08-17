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
