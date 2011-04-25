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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.mail.cache;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.concurrent.TimeoutConcurrentSet;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link MailAccessCache2} - A very volatile cache for already connected instances of {@link MailAccess}.
 * <p>
 * Only one mail access can be cached per user and is dedicated to fasten sequential mail requests<br>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessCache2 {

    private static volatile MailAccessCache2 singleton;

    /*
     * Field members
     */
    private ConcurrentMap<Key, TimeoutConcurrentSet<MailAccess<?, ?>>> map;

    private int defaultIdleSeconds;

    /**
     * Prevent instantiation.
     * 
     * @throws MailException If initialization fails
     */
    private MailAccessCache2() throws MailException {
        super();
        initCache();
    }

    /**
     * Gets the singleton instance.
     * 
     * @return The singleton instance
     * @throws MailException If instance initialization fails
     */
    public static MailAccessCache2 getInstance() throws MailException {
        if (null == singleton) {
            synchronized (MailAccessCache2.class) {
                if (null == singleton) {
                    singleton = new MailAccessCache2();
                    singleton.initCache();
                }
            }
        }
        return singleton;
    }

    /**
     * Releases the singleton instance.
     */
    public static void releaseInstance() {
        if (null != singleton) {
            synchronized (MailAccessCache2.class) {
                if (null != singleton) {
                    singleton.releaseCache();
                    singleton = null;
                }
            }
        }
    }

    /**
     * Initializes cache reference.
     * 
     * @throws MailException If initializing the time-out map reference fails
     */
    public void initCache() throws MailException {
        /*
         * Check for proper started mail cache configuration
         */
        if (!MailCacheConfiguration.getInstance().isStarted()) {
            throw new MailException(MailException.Code.INITIALIZATION_PROBLEM);
        }
        if (map != null) {
            return;
        }
        map = new ConcurrentHashMap<Key, TimeoutConcurrentSet<MailAccess<?,?>>>();
        defaultIdleSeconds = MailProperties.getInstance().getMailAccessCacheIdleSeconds();
        
//        try {
//            map = new TimeoutConcurrentMap<Key, MailAccess<?, ?>>(MailProperties.getInstance().getMailAccessCacheShrinkerSeconds());
//        } catch (final ServiceException e) {
//            throw new MailException(e);
//        }
//        timeoutMap.setDefaultTimeoutListener(new MailAccessTimeoutListener());
//        defaultIdleSeconds = MailProperties.getInstance().getMailAccessCacheIdleSeconds();
    }

    /**
     * Releases cache reference.
     */
    public void releaseCache() {
        if (map == null) {
            return;
        }
        for (final Iterator<TimeoutConcurrentSet<MailAccess<?,?>>> it = map.values().iterator(); it.hasNext();) {
            final TimeoutConcurrentSet<MailAccess<?, ?>> set = it.next();
            set.timeoutAll();
            set.dispose();
            it.remove();
        }
        defaultIdleSeconds = 0;
    }

    /**
     * Removes and returns a mail access from cache.
     * 
     * @param session The session
     * @param accountId The account ID
     * @return An active instance of {@link MailAccess} or <code>null</code>
     */
    public MailAccess<?, ?> removeMailAccess(final Session session, final int accountId) {
        final TimeoutConcurrentSet<MailAccess<?, ?>> set = map.get(getUserKey(session.getUserId(), accountId, session.getContextId()));
        if (null == set) {
            return null;
        }
        while (!set.isEmpty()) {
            final MailAccess<?, ?> mailAccess = set.getAny();
            if (null != mailAccess && set.remove(mailAccess)) {
                return mailAccess;
            }
        }
        return null;
    }

    /**
     * Puts given mail access into cache if none user-bound connection is already contained in cache.
     * 
     * @param session The session
     * @param accountId The account ID
     * @param mailAccess The mail access to put into cache
     * @return <code>true</code> if mail access could be successfully cached; otherwise <code>false</code>
     */
    public boolean putMailAccess(final Session session, final int accountId, final MailAccess<?, ?> mailAccess) {
        int idleTime = mailAccess.getCacheIdleSeconds();
        if (idleTime <= 0) {
            idleTime = defaultIdleSeconds;
        }
        final Key key = getUserKey(session.getUserId(), accountId, session.getContextId());
        TimeoutConcurrentSet<MailAccess<?, ?>> set = map.get(key);
        if (null == set) {
            final TimeoutConcurrentSet<MailAccess<?, ?>> newset;
            try {
                newset = new TimeoutConcurrentSet<MailAccess<?, ?>>(MailProperties.getInstance().getMailAccessCacheShrinkerSeconds());
            } catch (final ServiceException e) {
                return false;
            }
            newset.setDefaultTimeoutListener(new MailAccessTimeoutListener2(key, newset, map));
            set = map.putIfAbsent(key, newset);
            if (null == set) {
                set = newset;
            } else {
                newset.dispose();
            }
        }
        return set.add(mailAccess, idleTime);
    }

    /**
     * Checks if cache already holds a user-bound mail access for specified account.
     * 
     * @param session The session
     * @param accountId The account ID
     * @return <code>true</code> if a user-bound mail access is already present in cache; otherwise <code>false</code>
     */
    public boolean containsMailAccess(final Session session, final int accountId) {
        final TimeoutConcurrentSet<MailAccess<?, ?>> set = map.get(getUserKey(session.getUserId(), accountId, session.getContextId()));
        if (null == set) {
            return false;
        }
        return !set.isEmpty();
    }

    /**
     * Clears the cache entries kept for specified user.
     * 
     * @param session The session
     * @throws MailException If clearing user entries fails
     */
    public void clearUserEntries(final Session session) throws MailException {
        try {
            final int user = session.getUserId();
            final int cid = session.getContextId();
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);
            final MailAccount[] accounts = storageService.getUserMailAccounts(user, cid);
            for (final MailAccount mailAccount : accounts) {
                final TimeoutConcurrentSet<MailAccess<?, ?>> set = map.remove(getUserKey(user, mailAccount.getId(), cid));
                if (null != set) {
                    set.timeoutAll();
                    set.dispose();
                }
            }
        } catch (final ServiceException e) {
            throw new MailException(e);
        } catch (final MailAccountException e) {
            throw new MailException(e);
        }
    }

    private Key getUserKey(final int user, final int accountId, final int cid) {
        return new Key(user, cid, accountId);
    }

    private static final class Key {

        private final int user;

        private final int cid;

        private final int accountId;

        private final int hash;

        public Key(final int user, final int cid, final int accountId) {
            super();
            this.user = user;
            this.cid = cid;
            this.accountId = accountId;
            hash = hashCode0();
        }

        private int hashCode0() {
            final int prime = 31;
            int result = 1;
            result = prime * result + accountId;
            result = prime * result + cid;
            result = prime * result + user;
            return result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (accountId != other.accountId) {
                return false;
            }
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }

    }

}
