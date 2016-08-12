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

package com.openexchange.websockets.grizzly;

import java.util.concurrent.Future;
import com.openexchange.exception.OXException;
import com.openexchange.websockets.SendControl;
import com.openexchange.websockets.WebSocketExceptionCodes;
import com.openexchange.websockets.WebSocketService;
import com.openexchange.websockets.WebSockets;
import com.openexchange.websockets.grizzly.remote.RemoteWebSocketDistributor;

/**
 * {@link GrizzlyWebSocketService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class GrizzlyWebSocketService implements WebSocketService {

    private final GrizzlyWebSocketApplication localApp;
    private final RemoteWebSocketDistributor remoteDistributor;
    private final boolean asyncRemoteDistribution;

    /**
     * Initializes a new {@link GrizzlyWebSocketService}.
     */
    public GrizzlyWebSocketService(GrizzlyWebSocketApplication app, RemoteWebSocketDistributor remoteDistributor) {
        super();
        this.localApp = app;
        this.remoteDistributor = remoteDistributor;
        asyncRemoteDistribution = true;
    }

    @Override
    public boolean exists(int userId, int contextId) throws OXException {
        if (localApp.existsAny(null, userId, contextId)) {
            return true;
        }

        return remoteDistributor.existsAnyRemote(null, userId, contextId);
    }

    @Override
    public boolean exists(String pathFilter, int userId, int contextId) {
        if (localApp.existsAny(pathFilter, userId, contextId)) {
            return true;
        }

        return remoteDistributor.existsAnyRemote(pathFilter, userId, contextId);
    }

    @Override
    public void sendMessage(String message, int userId, int contextId) throws OXException {
        sendMessage(message, null, userId, contextId);
    }

    @Override
    public SendControl sendMessageAsync(String message, int userId, int contextId) throws OXException {
        return sendMessageAsync(message, null, userId, contextId);
    }

    // -------------------------------------------------------------------------------------------------------------

    @Override
    public void sendMessage(String message, String pathFilter, int userId, int contextId) throws OXException {
        if (false == WebSockets.validatePath(pathFilter)) {
            throw WebSocketExceptionCodes.INVALID_PATH_FILTER.create(pathFilter);
        }

        localApp.sendToUser(message, pathFilter, userId, contextId);
        remoteDistributor.sendRemote(message, pathFilter, userId, contextId, asyncRemoteDistribution);
    }

    @Override
    public SendControl sendMessageAsync(String message, String pathFilter, int userId, int contextId) throws OXException {
        if (false == WebSockets.validatePath(pathFilter)) {
            throw WebSocketExceptionCodes.INVALID_PATH_FILTER.create(pathFilter);
        }

        Future<Void> f = localApp.sendToUserAsync(message, pathFilter, userId, contextId);
        remoteDistributor.sendRemote(message, pathFilter, userId, contextId, true);
        return new SendControlImpl<Void>(f);
    }
}
