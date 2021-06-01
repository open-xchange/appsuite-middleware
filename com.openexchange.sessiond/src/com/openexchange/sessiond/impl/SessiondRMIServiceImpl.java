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

import static com.openexchange.java.Autoboxing.I;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.osgi.Services;
import com.openexchange.sessiond.rmi.SessiondRMIService;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link SessiondRMIServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class SessiondRMIServiceImpl implements SessiondRMIService {

    private static final Logger LOG = LoggerFactory.getLogger(SessiondRMIServiceImpl.class);

    /**
     * Initialises a new {@link SessiondRMIServiceImpl}.
     */
    public SessiondRMIServiceImpl() {
        super();
    }

    @Override
    public boolean clearUserSession(String sessionId) throws RemoteException {
        return clearUserSession(sessionId, false);
    }

    @Override
    public boolean clearUserSession(String sessionId, boolean global) throws RemoteException {
        StringBuilder sb = new StringBuilder(64);
        sb.append("(&(").append(SessionFilter.SESSION_ID).append("=").append(sessionId).append("))");
        List<String> removedSessions = SessionHandler.removeLocalSessions(SessionFilter.create(sb.toString()));
        if (global) {
            try {
                SessionHandler.removeRemoteSessions(SessionFilter.create(sb.toString()));
            } catch (IllegalArgumentException e) {
                throw new RemoteException(e.getMessage(), e);
            }
        }
        return false == removedSessions.isEmpty();
    }

    @Override
    public void clearContextSessions(int contextId) throws RemoteException {
        SessionHandler.removeContextSessions(contextId);
    }

    @Override
    public void clearContextSessionsGlobally(int contextId) throws RemoteException {
        clearContextSessionsGlobally(Collections.singleton(I(contextId)));
    }

    @Override
    public void clearContextSessionsGlobally(Set<Integer> contextIds) throws RemoteException {
        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        try {
            sessiondService.removeContextSessionsGlobal(contextIds);
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public int clearUserSessions(int userId, int contextId) throws RemoteException {
        return SessionHandler.removeUserSessions(userId, contextId).length;
    }

    @Override
    public void clearUserSessionsGlobally(int userId, int contextId) throws RemoteException {
        try {
            SessionHandler.removeUserSessionsGlobal(userId, contextId);
        } catch (Exception e) {
            LOG.error("", e);
            String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        }
    }

    @Override
    public void clearSessionStorage() throws RemoteException {
        SessionStorageService storageService = Services.getService(SessionStorageService.class);
        try {
            storageService.cleanUp();
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
