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

package com.openexchange.push.dovecot.locking;

import java.util.concurrent.atomic.AtomicBoolean;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link HzDovecotPushClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HzDovecotPushClusterLock extends AbstractDovecotPushClusterLock {

    private volatile String mapName;
    private final AtomicBoolean notActive;

    /**
     * Initializes a new {@link HzDovecotPushClusterLock}.
     */
    public HzDovecotPushClusterLock(ServiceLookup services) {
        super(services);
        notActive = new AtomicBoolean();
    }

    /**
     * Sets the map name
     *
     * @param mapName The map name to set
     */
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    private IMap<String, String> map(HazelcastInstance hzInstance) throws OXException {
        String mapName = this.mapName;
        if (null == mapName) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create("Missing map name");
        }
        try {
            return hzInstance.getMap(mapName);
        } catch (HazelcastInstanceNotActiveException e) {
            handleNotActiveException(e);
            // Obviously Hazelcast is absent
            return null;
        } catch (HazelcastException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void handleNotActiveException(HazelcastInstanceNotActiveException e) {
        notActive.set(true);
    }

    private String generateKey(SessionInfo sessionInfo) {
        return new StringBuilder(16).append(sessionInfo.getUserId()).append('@').append(sessionInfo.getContextId()).toString();
    }

    @Override
    public Type getType() {
        return Type.HAZELCAST;
    }

    @Override
    public boolean acquireLock(SessionInfo sessionInfo) throws OXException {
        if (notActive.get()) {
            return true;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        String key = generateKey(sessionInfo);
        IMap<String, String> map = map(hzInstance);
        if (null == map) {
            // Hazelcast is absent
            return true;
        }

        long now = System.nanoTime();
        String previous = map.putIfAbsent(key, generateValue(now, sessionInfo));

        if (null == previous) {
            // Not present before
            return true;
        }

        // Check if valid
        if (validValue(previous, now, hzInstance)) {
            // Locked
            return false;
        }

        // Invalid entry - try to replace it mutually exclusive
        return map.replace(key, previous, generateValue(now, sessionInfo));
    }

    @Override
    public void refreshLock(SessionInfo sessionInfo) throws OXException {
        if (notActive.get()) {
            return;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        IMap<String, String> map = map(hzInstance);
        if (null == map) {
            // Hazelcast is absent
            return;
        }

        map.put(generateKey(sessionInfo), generateValue(System.nanoTime(), sessionInfo));
    }

    @Override
    public void releaseLock(SessionInfo sessionInfo) throws OXException {
        if (notActive.get()) {
            return;
        }
        HazelcastInstance hzInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hzInstance) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        IMap<String, String> map = map(hzInstance);
        if (null == map) {
            // Hazelcast is absent
            return;
        }

        map.remove(generateKey(sessionInfo));
    }

}
