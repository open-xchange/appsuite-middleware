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

package com.openexchange.sessiond.impl;

import static com.openexchange.sessiond.impl.SessionHandler.getRemoteParameterNames;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.sessiond.osgi.Services;
import com.openexchange.sessiond.portable.PortableTokenSessionControl;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSession;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * This container stores the sessions created by the token login. These sessions either die after 60 seconds or they are moved over to the
 * normal session container if the session becomes active. The session will not become active if the browser still has the cookies for an
 * already existing session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added support for distributed Hazelcast map
 */
public final class TokenSessionContainer {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(TokenSessionContainer.class);

    private static volatile TokenSessionContainer INSTANCE = new TokenSessionContainer();

    /**
     * Gets the {@link TokenSessionContainer} instance.
     *
     * @return The {@link TokenSessionContainer} instance
     */
    public static TokenSessionContainer getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    private final String serverTokenMapName;
    private final Map<String, TokenSessionControl> localServerTokenMap;
    private final Map<String, ScheduledTimerTask> removerMap;
    private final Lock lock;
    private final AtomicBoolean useHazelcast;
    private volatile HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler;
    private volatile TimerService timerService;

    /**
     * Initializes a new {@link TokenSessionContainer}.
     */
    private TokenSessionContainer() {
        super();
        serverTokenMapName = "serverTokenMap";
        this.useHazelcast = new AtomicBoolean(false);
        localServerTokenMap = new HashMap<String, TokenSessionControl>();
        removerMap = new ConcurrentHashMap<String, ScheduledTimerTask>();
        lock = new ReentrantLock();
    }

    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        LOG.warn("Encountered a {} error.", HazelcastInstanceNotActiveException.class.getSimpleName());
        changeBackingMapToLocalMap();

        HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler = this.notActiveExceptionHandler;
        if (null != notActiveExceptionHandler) {
            notActiveExceptionHandler.propagateNotActive(e);
        }
    }

    /**
     * Gets the name for the token-session map
     *
     * @return The name
     */
    public String getServerTokenMapName() {
        return serverTokenMapName;
    }

    /**
     * Sets the not-active exception handler
     *
     * @param notActiveExceptionHandler The handler to set
     */
    public void setNotActiveExceptionHandler(HazelcastInstanceNotActiveExceptionHandler notActiveExceptionHandler) {
        this.notActiveExceptionHandler = notActiveExceptionHandler;
    }

    /**
     * Gets the Hazelcast map or <code>null</code> if unavailable.
     */
    private IMap<String, PortableTokenSessionControl> hzMap(String mapIdentifier) {
        if (null == mapIdentifier) {
            LOG.trace("Name of Hazelcast map is missing for token-session service.");
            return null;
        }
        final HazelcastInstance hazelcastInstance = Services.getService(HazelcastInstance.class);
        if (hazelcastInstance == null) {
            LOG.trace("Hazelcast instance is not available.");
            return null;
        }
        try {
            return hazelcastInstance.getMap(mapIdentifier);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            return null;
        }
    }

    /**
     * Changes the backing maps from distributed Hazelcast ones to local ones.
     */
    public void changeBackingMapToLocalMap() {
        Lock lock = getLock();
        lock.lock();
        try {
            // This happens if Hazelcast is removed in the meantime. We cannot copy any information back to the local map.
            useHazelcast.set(false);
            LOG.info("Token-session backing map changed to local");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Changes the backing maps from local ones to distributed Hazelcast ones.
     */
    public void changeBackingMapToHz() {
        Lock lock = getLock();
        lock.lock();
        try {
            if (useHazelcast.get()) {
                return;
            }

            IMap<String, PortableTokenSessionControl> serverTokenHzMap = hzMap(serverTokenMapName);
            if (null == serverTokenHzMap) {
                LOG.trace("Hazelcast map for remote token-session is not available.");
            } else {
                // This MUST be synchronous!
                for (Map.Entry<String, TokenSessionControl> entry : localServerTokenMap.entrySet()) {
                    TokenSessionControl tsc = entry.getValue();
                    serverTokenHzMap.put(entry.getKey(), new PortableTokenSessionControl(new PortableSession(tsc.getSession()), tsc.getClientToken(), tsc.getServerToken(), tsc.getCreationStamp()));
                }
                localServerTokenMap.clear();

                for (Iterator<ScheduledTimerTask> iter = removerMap.values().iterator(); iter.hasNext();) {
                    ScheduledTimerTask timerTask = iter.next();
                    timerTask.cancel();
                    iter.remove();
                }
            }

            useHazelcast.set(true);
            LOG.info("Token-session backing map changed to hazelcast");
        } finally {
            lock.unlock();
        }
    }

    private void putIntoMap(String serverToken, TokenSessionControl control) {
        if (useHazelcast.get()) {
            IMap<String, PortableTokenSessionControl> serverTokenHzMap = hzMap(serverTokenMapName);

            // Generate a portable session from token's session...
            PortableSession portableSession = new PortableSession(SessionHandler.getObfuscator().wrap(control.getSession(), getRemoteParameterNames()));

            // ... and put into HZ map
            serverTokenHzMap.put(serverToken, new PortableTokenSessionControl(portableSession, control.getClientToken(), control.getServerToken(), control.getCreationStamp()));
        } else {
            localServerTokenMap.put(serverToken, control);
        }
    }

    private TokenSessionControl removeFromMap(String serverToken) {
        if (false == useHazelcast.get()) {
            return localServerTokenMap.remove(serverToken);
        }

        IMap<String, PortableTokenSessionControl> serverTokenHzMap = hzMap(serverTokenMapName);
        PortableTokenSessionControl removed = serverTokenHzMap.remove(serverToken);
        if (null == removed) {
            return null;
        }

        // Create the session instance from its portable representation
        SessionImpl unwrappedSession = SessionHandler.getObfuscator().unwrap(removed.getSession(), getRemoteParameterNames());

        // Return appropriate TokenSessionControl
        return new TokenSessionControl(unwrappedSession, removed.getClientToken(), removed.getServerToken(), removed.getCreationStamp());
    }

    private Map<String, ScheduledTimerTask> getRemoverMap() {
        return useHazelcast.get() ? null : removerMap;
    }

    private Lock getLock() {
        return useHazelcast.get() ? Session.EMPTY_LOCK : lock;
    }

    /**
     * Applies the given timer service to this {@link TokenSessionContainer} instance.
     *
     * @param service The time service
     */
    public void addTimerService(TimerService service) {
        timerService = service;
    }

    /**
     * Removes the timer service from this {@link TokenSessionContainer} instance.
     */
    public void removeTimerService() {
        timerService = null;
    }

    TokenSessionControl addSession(SessionImpl session, String clientToken, String serverToken) {
        TokenSessionControl control;

        Lock lock = getLock();
        lock.lock();
        try {
            control = new TokenSessionControl(session, clientToken, serverToken, System.currentTimeMillis());
            putIntoMap(serverToken, control);
        } finally {
            lock.unlock();
        }

        scheduleRemover(control);
        return control;
    }

    TokenSessionControl getSession(String clientToken, String serverToken) throws OXException {
        TokenSessionControl control;

        Lock lock = getLock();
        lock.lock();
        try {
            control = removeFromMap(serverToken);
            if ((null == control) || (System.currentTimeMillis() - control.getCreationStamp() > 60000)) {
                // No such token-session or token-session already elapsed
                throw com.openexchange.sessiond.SessionExceptionCodes.NO_SESSION_FOR_SERVER_TOKEN.create(serverToken, clientToken);
            }
            if (!control.getServerToken().equals(serverToken)) {
                throw com.openexchange.sessiond.SessionExceptionCodes.NO_SESSION_FOR_SERVER_TOKEN.create(serverToken, clientToken);
            }
            if (!control.getClientToken().equals(clientToken)) {
                throw com.openexchange.sessiond.SessionExceptionCodes.NO_SESSION_FOR_CLIENT_TOKEN.create(serverToken, clientToken);
            }
        } finally {
            lock.unlock();
        }

        unscheduleRemover(control);
        return control;
    }

    TokenSessionControl removeSession(TokenSessionControl control) {
        TokenSessionControl removed;

        Lock lock = getLock();
        lock.lock();
        try {
            removed = removeFromMap(control.getServerToken());
        } finally {
            lock.unlock();
        }

        return removed;
    }

    private void scheduleRemover(TokenSessionControl control) {
        Map<String, ScheduledTimerTask> removerMap = getRemoverMap();
        if (null != removerMap) {
            TimerService timerService = this.timerService;
            if (null == timerService) {
                return;
            }
            ScheduledTimerTask task = timerService.schedule(new TokenSessionTimerRemover(control), 60, TimeUnit.SECONDS);
            removerMap.put(control.getSession().getSessionID(), task);
        }
    }

    private void unscheduleRemover(TokenSessionControl control) {
        Map<String, ScheduledTimerTask> removerMap = getRemoverMap();
        if (null != removerMap) {
            ScheduledTimerTask task = removerMap.get(control.getSession().getSessionID());
            if (null != task) {
                task.cancel();
            }
        }
    }

}
