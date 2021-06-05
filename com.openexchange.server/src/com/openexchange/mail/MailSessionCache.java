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

package com.openexchange.mail;

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
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
    public static MailSessionCache getInstance(Session session) {
        if (null == session) {
            return null;
        }
        final String key = MailSessionParameterNames.getParamMainCache();
        MailSessionCache mailCache = null;
        try {
            mailCache = (MailSessionCache) session.getParameter(key);
        } catch (ClassCastException e) {
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
    public static MailSessionCache optInstance(Session session) {
        if (null == session) {
            return null;
        }
        final String key = MailSessionParameterNames.getParamMainCache();
        MailSessionCache mailCache = null;
        try {
            mailCache = (MailSessionCache) session.getParameter(key);
        } catch (ClassCastException e) {
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
    public static void dropInstance(Session session) {
        final String key = MailSessionParameterNames.getParamMainCache();
        MailSessionCache mailCache = null;
        try {
            mailCache = (MailSessionCache) session.getParameter(key);
        } catch (ClassCastException e) {
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
    public static void clearInstance(Session session) {
        final String key = MailSessionParameterNames.getParamMainCache();
        MailSessionCache mailCache = null;
        try {
            mailCache = (MailSessionCache) session.getParameter(key);
        } catch (ClassCastException e) {
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
    public static void removeDefaultFolderInformationFrom(int accountId, int userId, int contextId) {
        final SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        if (null != sessiondService) {
            final Collection<Session> sessions = sessiondService.getSessions(userId, contextId);
            for (Session s : sessions) {
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
     * @return The parameter or <code>null</code>
     */
    public <T extends Object> T getParameter(int accountId, String parameterName) {
        final ConcurrentMap<String, Object> accountMap = map.get(accountId);
        if (null == accountMap) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked") final T retval = (T) accountMap.get(parameterName);
            return retval;
        } catch (ClassCastException e) {
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
    public boolean containsParameter(int accountId, String parameterName) {
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
    public void putParameter(int accountId, String parameterName, Object parameterValue) {
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
    public Object putParameterIfAbsent(int accountId, String parameterName, Object parameterValue) {
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
    public Object removeParameter(int accountId, String parameterName) {
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
    public void removeAccountParameters(int accountId) {
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
