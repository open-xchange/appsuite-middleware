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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.websockets.grizzly;

import java.util.concurrent.atomic.AtomicReference;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;
import com.openexchange.session.Session;

/**
 * {@link SessionBoundWebSocket} - The Web Socket bound to a certain session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SessionBoundWebSocket extends DefaultWebSocket {

    private final AtomicReference<String> sessionIdReference;

    /**
     * Initializes a new {@link SessionBoundWebSocket}.
     */
    public SessionBoundWebSocket(ProtocolHandler protocolHandler, HttpRequestPacket request, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
        this.sessionIdReference = new AtomicReference<String>(null);
    }

    /**
     * Gets the identifier of the session currently associated with this Web Socket.
     *
     * @return The session identifier or <code>null</code>
     */
    public String getSessionId() {
        return sessionIdReference.get();
    }

    /**
     * (Atomically) Binds the specified session to this Web Socket if not already bound to another session.
     *
     * @param session The session to bind to
     * @return <code>true</code> if session could be successfully bounded to this Web Socket; otherwise <code>false</code>
     */
    public boolean bindSession(Session session) {
        String sessionID = session.getSessionID();
        String state;
        do {
            state = sessionIdReference.get();
            if (null != state) {
                // Already set
                return false;
            }
        } while (!sessionIdReference.compareAndSet(state, sessionID));
        return true;
    }

}
