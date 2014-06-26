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

package com.openexchange.file.storage.cifs.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import com.openexchange.file.storage.cifs.CIFSFileStorageService;
import com.openexchange.file.storage.cifs.CIFSServices;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link SmbFileMapManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmbFileMapManagement {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SmbFileMapManagement.class);

    private static final SmbFileMapManagement INSTANCE = new SmbFileMapManagement();

    /**
     * Gets the {@link SmbFileMapManagement management} instance.
     *
     * @return The management instance
     */
    public static SmbFileMapManagement getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, SmbFileMap>> map;
    private volatile ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link SmbFileMapManagement}.
     */
    private SmbFileMapManagement() {
        super();
        map = new ConcurrentLinkedHashMap.Builder<Integer, ConcurrentMap<Integer, SmbFileMap>>().initialCapacity(64).maximumWeightedCapacity(5000).weigher(Weighers.entrySingleton()).build();
    }

    /**
     * Starts the shrinker.
     */
    public void startShrinker() {
        ScheduledTimerTask tmp = timerTask;
        if (null == tmp) {
            synchronized (this) {
                tmp = timerTask;
                if (null == tmp) {
                    final ConcurrentMap<Integer, ConcurrentMap<Integer, SmbFileMap>> map = this.map;
                    final Runnable task = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                for (final ConcurrentMap<Integer, SmbFileMap> m : map.values()) {
                                    for (final SmbFileMap smbFileMap : m.values()) {
                                        smbFileMap.shrink();
                                    }
                                }
                            } catch (final Exception e) {
                                // Ignore
                            }
                        }
                    };
                    tmp = CIFSServices.getService(TimerService.class).scheduleWithFixedDelay(task, 60000L, 60000L);
                    timerTask = tmp;
                }
            }
        }
    }

    /**
     * Stops the shrinker.
     */
    public void stopShrinker() {
        final ScheduledTimerTask tmp = timerTask;
        if (null != tmp) {
            tmp.cancel();
            final TimerService timerService = CIFSServices.getOptionalService(TimerService.class);
            if (null != timerService) {
                timerService.purge();
            }
            this.timerTask = null;
        }
    }

    /**
     * Clears the SMB file management.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Drop caches for given context.
     *
     * @param contextId The context identifier
     */
    public void dropFor(final int contextId) {
        map.get(Integer.valueOf(contextId));
        LOG.debug("Cleaned user-sensitive SMB file cache for context {}", contextId);
    }

    /**
     * Drop caches for given session's user.
     *
     * @param session The session
     */
    public void dropFor(final Session session) {
        final ConcurrentMap<Integer, SmbFileMap> contextMap = map.get(Integer.valueOf(session.getContextId()));
        if (null != contextMap) {
            final SmbFileMap smbFileMap = contextMap.remove(Integer.valueOf(session.getUserId()));
            if (null != smbFileMap) {
                smbFileMap.clear();
            }
        }
        LOG.debug("Cleaned user-sensitive SMB file cache for user {} in context {}", session.getUserId(), session.getContextId());
    }

    /**
     * Drop caches for given session's user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void dropFor(final int userId, final int contextId) {
        final ConcurrentMap<Integer, SmbFileMap> contextMap = map.get(Integer.valueOf(contextId));
        if (null != contextMap) {
            final SmbFileMap smbFileMap = contextMap.remove(Integer.valueOf(userId));
            if (null != smbFileMap) {
                smbFileMap.clear();
            }
        }
        LOG.debug("Cleaned user-sensitive SMB file cache for user {} in context {}", userId, contextId);
    }

    /**
     * Gets the SMB file map for specified session.
     *
     * @param session The session
     * @return The SMB file map
     */
    public SmbFileMap getFor(final Session session) {
        final Integer cid = Integer.valueOf(session.getContextId());
        ConcurrentMap<Integer, SmbFileMap> contextMap = map.get(cid);
        if (null == contextMap) {
            final ConcurrentMap<Integer, SmbFileMap> newMap = new ConcurrentHashMap<Integer, SmbFileMap>(256);
            contextMap = map.putIfAbsent(cid, newMap);
            if (null == contextMap) {
                contextMap = newMap;
            }
        }
        final Integer user = Integer.valueOf(session.getUserId());
        SmbFileMap smbFileMap = contextMap.get(user);
        if (null == smbFileMap) {
            final SmbFileMap newFileMap = new SmbFileMap(1024, CIFSFileStorageService.DEFAULT_ATTR_EXPIRATION_PERIOD - 1000, TimeUnit.MILLISECONDS);
            smbFileMap = contextMap.putIfAbsent(user, newFileMap);
            if (null == smbFileMap) {
                smbFileMap = newFileMap;
            }
        }
        return smbFileMap;
    }

    /**
     * Optionally gets the SMB file map for specified session.
     *
     * @param session The session
     * @return The SMB file map or <code>null</code> if absent
     */
    public SmbFileMap optFor(final Session session) {
        final ConcurrentMap<Integer, SmbFileMap> contextMap = map.get(Integer.valueOf(session.getContextId()));
        if (null == contextMap) {
            return null;
        }
        return contextMap.get(Integer.valueOf(session.getUserId()));
    }

    /**
     * Optionally gets the SMB file map for specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The SMB file map or <code>null</code> if absent
     */
    public SmbFileMap optFor(final int userId, final int contextId) {
        final ConcurrentMap<Integer, SmbFileMap> contextMap = map.get(Integer.valueOf(contextId));
        if (null == contextMap) {
            return null;
        }
        return contextMap.get(Integer.valueOf(userId));
    }

}
