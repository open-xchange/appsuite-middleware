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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.exception.OXException;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * This container stores the sessions created by the token login. These sessions either die after 60 seconds or they are moved over to the
 * normal session container if the session becomes active. The session will not become active if the browser still has the cookies for an
 * already existing session.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class TokenSessionContainer {

    private static final TokenSessionContainer SINGLETON = new TokenSessionContainer();

    private final Map<String, TokenSessionControl> serverTokenMap = new HashMap<String, TokenSessionControl>();
    private final Map<String, ScheduledTimerTask> removerMap = new ConcurrentHashMap<String, ScheduledTimerTask>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private TimerService timerService;

    private TokenSessionContainer() {
        super();
    }

    public static TokenSessionContainer getInstance() {
        return SINGLETON;
    }

    public void addTimerService(TimerService service) {
        timerService = service;
    }

    public void removeTimerService() {
        timerService = null;
    }

    TokenSessionControl addSession(SessionImpl session, String clientToken, String serverToken) {
        Lock wLock = lock.writeLock();
        final TokenSessionControl control;
        wLock.lock();
        try {
            control = new TokenSessionControl(session, clientToken, serverToken);
            serverTokenMap.put(serverToken, control);
        } finally {
            wLock.unlock();
        }
        scheduleRemover(control);
        return control;
    }

    TokenSessionControl getSession(String clientToken, String serverToken) throws OXException {
        Lock rLock = lock.readLock();
        final TokenSessionControl control;
        rLock.lock();
        try {
            control = serverTokenMap.get(serverToken);
        } finally {
            rLock.unlock();
        }
        if (null == control) {
            throw com.openexchange.sessiond.SessionExceptionCodes.NO_SESSION_FOR_SERVER_TOKEN.create(serverToken, clientToken);
        }
        if (!control.getServerToken().equals(serverToken)) {
            throw com.openexchange.sessiond.SessionExceptionCodes.NO_SESSION_FOR_SERVER_TOKEN.create(serverToken, clientToken);
        }
        if (!control.getClientToken().equals(clientToken)) {
            throw com.openexchange.sessiond.SessionExceptionCodes.NO_SESSION_FOR_CLIENT_TOKEN.create(serverToken, clientToken);
        }
        Lock wLock = lock.writeLock();
        wLock.lock();
        try {
            serverTokenMap.remove(control.getServerToken());
        } finally {
            wLock.unlock();
        }
        unscheduleRemover(control);
        return control;
    }

    TokenSessionControl removeSession(TokenSessionControl control) {
        Lock wLock = lock.writeLock();
        final TokenSessionControl removed;
        wLock.lock();
        try {
            removed = serverTokenMap.remove(control.getServerToken());
        } finally {
            wLock.unlock();
        }
        return removed;
    }

    private void scheduleRemover(TokenSessionControl control) {
        if (null == timerService) {
            return;
        }
        ScheduledTimerTask task = timerService.schedule(new TokenSessionTimerRemover(control), 60, TimeUnit.SECONDS);
        removerMap.put(control.getSession().getSessionID(), task);
    }

    private void unscheduleRemover(TokenSessionControl control) {
        ScheduledTimerTask task = removerMap.get(control.getSession().getSessionID());
        if (null != task) {
            task.cancel();
        }
    }
}
