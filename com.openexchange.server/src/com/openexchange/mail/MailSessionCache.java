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

package com.openexchange.mail;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link MailSessionCache} - The main session-bound cache for mail module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailSessionCache {

    /**
     * The region name.
     */
    public static final String REGION = "MailSessionCache";

    private static final Object SENDER = new Object() { @Override public String toString() { return REGION; } };

    /**
     * Gets the session-bound mail cache.
     *
     * @param session The session whose mail cache shall be returned
     * @return The session-bound mail cache.
     */
    public static MailSessionCache getInstance(final Session session) {
        if (null == session) {
            return null;
        }
        final String key = MailSessionParameterNames.getParamMainCache();
        MailSessionCache mailCache = null;
        try {
            mailCache = (MailSessionCache) session.getParameter(key);
        } catch (final ClassCastException e) {
            /*
             * Class version does not match; just renew session cache.
             */
            mailCache = null;
            session.setParameter(key, null);
        }
        if (null == mailCache) {
            Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
            if (null == lock) {
                lock = Session.EMPTY_LOCK;
            }
            lock.lock();
            try {
                mailCache = (MailSessionCache) session.getParameter(key);
                if (null == mailCache) {
                    mailCache = new MailSessionCache();
                    session.setParameter(key, mailCache);
                }
            } finally {
                lock.unlock();
            }
        }
        return mailCache;
    }

    /**
     * Gets the session-bound mail cache.
     *
     * @param session The session whose mail cache shall be returned
     * @return The session-bound mail cache or <code>null</code>
     */
    public static MailSessionCache optInstance(final Session session) {
        if (null == session) {
            return null;
        }
        final String key = MailSessionParameterNames.getParamMainCache();
        MailSessionCache mailCache = null;
        try {
            mailCache = (MailSessionCache) session.getParameter(key);
        } catch (final ClassCastException e) {
            /*
             * Class version does not match; just renew session cache.
             */
            mailCache = null;
            session.setParameter(key, null);
        }
        return mailCache;
    }

    /**
     * Drops the session-bound mail cache.
     *
     * @param session The session whose mail cache shall be dropped
     */
    public static void dropInstance(final Session session) {
        final String key = MailSessionParameterNames.getParamMainCache();
        MailSessionCache mailCache = null;
        try {
            mailCache = (MailSessionCache) session.getParameter(key);
        } catch (final ClassCastException e) {
            /*
             * Class version does not match
             */
            mailCache = null;
            session.setParameter(key, null);
            return;
        }
        if (null != mailCache) {
            Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
            if (null == lock) {
                lock = Session.EMPTY_LOCK;
            }
            lock.lock();
            try {
                mailCache = (MailSessionCache) session.getParameter(key);
                if (null != mailCache) {
                    mailCache.clear();
                    session.setParameter(key, null);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Drops the session-bound mail cache.
     *
     * @param session The session whose mail cache shall be dropped
     * @return <code>true</code> if session-associated instance was cleared; otherwise <code>false</code>
     */
    public static void clearInstance(final Session session) {
        final String key = MailSessionParameterNames.getParamMainCache();
        MailSessionCache mailCache = null;
        try {
            mailCache = (MailSessionCache) session.getParameter(key);
        } catch (final ClassCastException e) {
            /*
             * Class version does not match
             */
            mailCache = null;
            session.setParameter(key, null);
            return;
        }

        if (null == mailCache) {
            return;
        }

        mailCache.clear();
    }

    /**
     * Clears cache for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void clearFor(int userId, int contextId) {
        clearFor(userId, contextId, true);
    }

    /**
     * Clears cache for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param notify Whether to notify
     */
    public static void clearFor(int userId, int contextId, boolean notify) {
        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        if (null != sessiondService) {
            boolean somethingCleared = false;

            Collection<Session> sessions = sessiondService.getSessions(userId, contextId);
            for (Session session : sessions) {
                MailSessionCache mailSessionCache = MailSessionCache.optInstance(session);
                if (null != mailSessionCache) {
                    mailSessionCache.clear();
                    somethingCleared = true;
                }
            }

            if (somethingCleared && notify) {
                fireInvalidateCacheEvent(userId, contextId);
            }
        }
    }

    private static void fireInvalidateCacheEvent(int userId, int contextId) {
        CacheEventService cacheEventService = ServerServiceRegistry.getInstance().getService(CacheEventService.class);
        if (null != cacheEventService && cacheEventService.getConfiguration().remoteInvalidationForPersonalFolders()) {
            CacheEvent event = newCacheEventFor(userId, contextId);
            if (null != event) {
                cacheEventService.notify(SENDER, event, false);
            }
        }
    }

    private static CacheEvent newCacheEventFor(int userId, int contextId) {
        CacheService service = ServerServiceRegistry.getInstance().getService(CacheService.class);
        return null == service ? null : CacheEvent.INVALIDATE(REGION, Integer.toString(contextId), service.newCacheKey(contextId, userId));
    }

    /**
     * Removes cached standard folder information from user-associated caches.
     *
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void removeDefaultFolderInformationFrom(final int accountId, final int userId, final int contextId) {
        final SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        if (null != sessiondService) {
            final Collection<Session> sessions = sessiondService.getSessions(userId, contextId);
            for (final Session s : sessions) {
                final MailSessionCache sessionCache = optInstance(s);
                if (null != sessionCache) {
                    sessionCache.removeParameter(accountId, MailSessionParameterNames.getParamDefaultFolderChecked());
                    sessionCache.removeParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
                }
            }
        }
    }

    /*-
     * ##################################### MEMBER STUFF #####################################
     */

    private final TIntObjectMap<ConcurrentMap<String, Object>> map;

    /**
     * Initializes a new {@link MailSessionCache}.
     */
    private MailSessionCache() {
        super();
        map = new TIntObjectHashMap<ConcurrentMap<String, Object>>();
    }

    /**
     * Gets the parameter associated with given account ID and parameter name.
     *
     * @param accountId The account ID
     * @param parameterName The parameter name
     * @param clazz The parameter value's class
     * @return The parameter or <code>null</code>
     */
    public <T extends Object> T getParameter(final int accountId, final String parameterName) {
        final ConcurrentMap<String, Object> accountMap = map.get(accountId);
        if (null == accountMap) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked") final T retval = (T) accountMap.get(parameterName);
            return retval;
        } catch (final ClassCastException e) {
            return null;
        }
    }

    /**
     * Checks if a parameter is associated with given account ID and parameter name.
     *
     * @param accountId The account ID
     * @param parameterName The parameter name
     * @return <code>true</code> if a parameter is associated with given account ID and parameter name; otherwise <code>false</code>
     */
    public boolean containsParameter(final int accountId, final String parameterName) {
        final ConcurrentMap<String, Object> accountMap = map.get(accountId);
        if (null == accountMap) {
            return false;
        }
        return accountMap.containsKey(parameterName);
    }

    /**
     * Puts parameter into this cache. A <code>null</code> value removes the parameter.
     *
     * @param accountId The account ID
     * @param parameterName The parameter name
     * @param parameterValue The parameter value
     */
    public void putParameter(final int accountId, final String parameterName, final Object parameterValue) {
        ConcurrentMap<String, Object> accountMap = map.get(accountId);
        if (null == accountMap) {
            final ConcurrentMap<String, Object> newInst = new NonBlockingHashMap<String, Object>();
            accountMap = map.putIfAbsent(accountId, newInst);
            if (null == accountMap) {
                accountMap = newInst;
            }
        }
        if (null == parameterValue) {
            accountMap.remove(parameterName);
        } else {
            accountMap.put(parameterName, parameterValue);
        }
    }

    /**
     * (Atomically) Puts parameter into this cache only if no other parameter is associated with given account ID and parameter name. A
     * <code>null</code> value removes the parameter.
     *
     * @param accountId The account ID
     * @param parameterName The parameter name
     * @param parameterValue The parameter value
     * @return The parameter value previously associated with given account ID and parameter name
     */
    public Object putParameterIfAbsent(final int accountId, final String parameterName, final Object parameterValue) {
        ConcurrentMap<String, Object> accountMap = map.get(accountId);
        if (null == accountMap) {
            final ConcurrentMap<String, Object> newInst = new NonBlockingHashMap<String, Object>();
            accountMap = map.putIfAbsent(accountId, newInst);
            if (null == accountMap) {
                accountMap = newInst;
            }
        }
        if (null == parameterValue) {
            return accountMap.remove(parameterName);
        }
        return accountMap.putIfAbsent(parameterName, parameterValue);
    }

    /**
     * Removes the parameter associated with given account ID and parameter name.
     *
     * @param accountId The account ID
     * @param parameterName The parameter name
     * @return The parameter previously associated with given account ID and parameter name or <code>null</code>
     */
    public Object removeParameter(final int accountId, final String parameterName) {
        final ConcurrentMap<String, Object> accountMap = map.get(accountId);
        if (null == accountMap) {
            return null;
        }
        return accountMap.remove(parameterName);
    }

    /**
     * Removes the parameters associated with given account ID
     *
     * @param accountId The account ID
     */
    public void removeAccountParameters(final int accountId) {
        final ConcurrentMap<String, Object> removed = map.remove(accountId);
        if (null != removed) {
            removed.clear();
        }
    }

    /**
     * Clears this cache.
     */
    public void clear() {
        map.clear();
    }

}
