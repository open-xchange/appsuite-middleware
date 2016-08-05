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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocketException;
import com.openexchange.exception.OXException;
import com.openexchange.websockets.ConnectionId;
import com.openexchange.websockets.MessageHandler;
import com.openexchange.websockets.WebSocket;
import com.openexchange.websockets.WebSocketExceptionCodes;
import com.openexchange.websockets.WebSocketSession;


/**
 * {@link WebSocketImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketImpl implements WebSocket {

    private final SessionBoundWebSocket grizzlySocket;

    /**
     * Initializes a new {@link WebSocketImpl}.
     */
    public WebSocketImpl(SessionBoundWebSocket grizzlySocket) {
        super();
        this.grizzlySocket = grizzlySocket;
    }

    @Override
    public String getPath() {
        return grizzlySocket.getPath();
    }

    @Override
    public WebSocketSession getWebSocketSession() {
        return grizzlySocket.getWebSocketSession();
    }

    @Override
    public ConnectionId getConnectionId() {
        return grizzlySocket.getConnectionId();
    }

    @Override
    public String getSessionId() {
        return grizzlySocket.getSessionId();
    }

    @Override
    public int getUserId() {
        return grizzlySocket.getUserId();
    }

    @Override
    public int getContextId() {
        return grizzlySocket.getContextId();
    }

    @Override
    public void sendMessage(String message) throws OXException {
        GrizzlyFuture<DataFrame> grizzlyFuture = grizzlySocket.send(message);
        try {
            grizzlyFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw WebSocketExceptionCodes.UNEXPECTED_ERROR.create(e, "Interrupted");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            if (cause instanceof IOException) {
                throw WebSocketExceptionCodes.IO_ERROR.create(cause, cause.getMessage());
            }
            if (cause instanceof WebSocketException) {
                throw WebSocketExceptionCodes.PROTOCOL_ERROR.create(cause, cause.getMessage());
            }
            throw WebSocketExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

    @Override
    public MessageHandler sendMessageAsync(String message) throws OXException {
        grizzlySocket.send(message);
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((grizzlySocket == null) ? 0 : grizzlySocket.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WebSocketImpl)) {
            return false;
        }
        WebSocketImpl other = (WebSocketImpl) obj;
        if (grizzlySocket == null) {
            if (other.grizzlySocket != null) {
                return false;
            }
        } else if (!grizzlySocket.equals(other.grizzlySocket)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return grizzlySocket.toString();
    }

}
