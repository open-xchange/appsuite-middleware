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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.caching.events.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.caching.events.CacheListener;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link CacheEventServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class CacheEventServiceImpl implements CacheEventService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CacheEventServiceImpl.class);

    private final ConcurrentMap<String, List<CacheListener>> cacheRegionListeners;
    private final List<CacheListener> cacheListeners;

    /**
     * Initializes a new {@link CacheEventServiceImpl}.
     */
    public CacheEventServiceImpl() {
        super();
        cacheRegionListeners = new ConcurrentHashMap<String, List<CacheListener>>();
        cacheListeners = new ArrayList<CacheListener>();
    }

    @Override
    public void addListener(CacheListener listener) {
        if (cacheListeners.add(listener) && LOG.isDebugEnabled()) {
            LOG.debug("Added cache listener: " + listener);
        }
    }

    @Override
    public void removeListener(CacheListener listener) {
        if (cacheListeners.remove(listener) && LOG.isDebugEnabled()) {
            LOG.debug("Removed cache listener for region: " + listener);
        }
    }

    @Override
    public void addListener(String region, CacheListener listener) {
        if (getListeners(region).add(listener) && LOG.isDebugEnabled()) {
            LOG.debug("Added cache listener for region '" + region + "': " + listener);
        }
    }

    @Override
    public void removeListener(String region, CacheListener listener) {
        if (getListeners(region).remove(listener) && LOG.isDebugEnabled()) {
            LOG.debug("Removed cache listener for region '" + region + "': " + listener);
        }
    }

    @Override
    public void notify(Object sender, CacheEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("notify: " + event);
        }
        List<Runnable> notificationRunnables = new ArrayList<Runnable>();
        if (null != event.getRegion()) {
            for (CacheListener listener : getListeners(event.getRegion())) {
                if (listener != sender) {
                    notificationRunnables.add(getNotificationRunnable(listener, sender, event));
                }
            }
        }
        for (CacheListener listener : cacheListeners) {
            if (listener != sender) {
                notificationRunnables.add(getNotificationRunnable(listener, sender, event));
            }
        }
        if (0 < notificationRunnables.size()) {
            ExecutorService executorService = getExecutorService();
            if (null == executorService) {
                for (Runnable runnable : notificationRunnables) {
                    runnable.run();
                }
            } else {
                for (Runnable runnable : notificationRunnables) {
                    executorService.execute(runnable);
                }
            }
        }
    }

    /**
     * Gets the registered cache listeners for a region.
     *
     * @param region The cache region name
     * @return The cache listeners, or an empty list if no listeners are registered
     */
    private List<CacheListener> getListeners(String region) {
        List<CacheListener> listeners = cacheRegionListeners.get(region);
        if (null == listeners) {
            listeners = new CopyOnWriteArrayList<CacheListener>();
            List<CacheListener> exitingListeners = cacheRegionListeners.putIfAbsent(region, listeners);
            if (null != exitingListeners) {
                return exitingListeners;
            }
        }
        return listeners;
    }

    private static Runnable getNotificationRunnable(final CacheListener listener, final Object sender, final CacheEvent event) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    listener.onEvent(sender, event);
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    LOG.error("Error while excuting event listener: " + t.getMessage(), t);
                }
            }
        };
    }

    private static ExecutorService getExecutorService() {
        ThreadPoolService threadPoolService = CacheEventServiceLookup.getService(ThreadPoolService.class);
        return null != threadPoolService ? threadPoolService.getExecutor() : null;
    }

}
