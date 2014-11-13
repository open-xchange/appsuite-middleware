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

package com.openexchange.push.imapidle.locking;

import java.util.concurrent.atomic.AtomicBoolean;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link HzImapIdleClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HzImapIdleClusterLock extends AbstractImapIdleClusterLock {

    private final String mapName;
    private final AtomicBoolean notActive;

    /**
     * Initializes a new {@link HzImapIdleClusterLock}.
     */
    public HzImapIdleClusterLock(String mapName, ServiceLookup services) {
        super(services);
        this.mapName = mapName;
        notActive = new AtomicBoolean();
    }

    private IMap<String, String> map(HazelcastInstance hzInstance) throws OXException {
        try {
            return hzInstance.getMap(mapName);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (HazelcastException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        notActive.set(true);
    }

    @Override
    public Type getType() {
        return Type.HAZELCAST;
    }

    @Override
    public boolean acquireLock(Session session) throws OXException {
        if (notActive.get()) {
            return true;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        int contextId = session.getContextId();
        int userId = session.getUserId();
        String key = new StringBuilder(16).append(userId).append('@').append(contextId).toString();
        IMap<String, String> map = map(hzInstance);

        long now = System.nanoTime();
        String previous = map.putIfAbsent(key, generateValue(now, session.getSessionID()));

        if (null == previous) {
            // Not present before
            return true;
        }

        // Check if valid
        if (validValue(previous, now)) {
            // Locked
            return false;
        }

        // Invalid entry - try to replace it mutually exclusive
        return map.replace(key, previous, generateValue(now, session.getSessionID()));
    }

    @Override
    public void refreshLock(Session session) throws OXException {
        if (notActive.get()) {
            return;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        int contextId = session.getContextId();
        int userId = session.getUserId();
        String key = new StringBuilder(16).append(userId).append('@').append(contextId).toString();
        map(hzInstance).put(key, generateValue(System.nanoTime(), session.getSessionID()));
    }

    @Override
    public void releaseLock(Session session) throws OXException {
        if (notActive.get()) {
            return;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        int contextId = session.getContextId();
        int userId = session.getUserId();
        String key = new StringBuilder(16).append(userId).append('@').append(contextId).toString();
        map(hzInstance).remove(key);
    }

}
