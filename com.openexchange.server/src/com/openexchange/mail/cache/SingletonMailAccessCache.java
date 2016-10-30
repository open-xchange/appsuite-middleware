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

package com.openexchange.mail.cache;

import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link SingletonMailAccessCache} - A very volatile cache for already connected instances of {@link MailAccess}.
 * <p>
 * Only one mail access can be cached per user and is dedicated to fasten sequential mail requests<br>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SingletonMailAccessCache implements IMailAccessCache {

    private static volatile SingletonMailAccessCache singleton;

    /**
     * Creates a new {@link SingletonMailAccessCache}
     *
     * @return A new {@link SingletonMailAccessCache}
     * @throws OXException If initialization fails
     */
    public static SingletonMailAccessCache newInstance() throws OXException {
        final SingletonMailAccessCache singletonCache = new SingletonMailAccessCache();
        singletonCache.initCache();
        return singletonCache;
    }

    /**
     * Gets the singleton instance.
     *
     * @return The singleton instance
     * @throws OXException If instance initialization fails
     */
    public static SingletonMailAccessCache getInstance() throws OXException {
        if (null == singleton) {
            synchronized (SingletonMailAccessCache.class) {
                if (null == singleton) {
                    SingletonMailAccessCache tmp = new SingletonMailAccessCache();
                    tmp.initCache();
                    singleton = tmp;
                }
            }
        }
        return singleton;
    }

    /**
     * (Optionally) Gets the singleton instance.
     *
     * @return The singleton instance or <code>null</code>
     */
    public static SingletonMailAccessCache optInstance() {
        return singleton;
    }

    /**
     * Releases the singleton instance.
     */
    public static void releaseInstance() {
        if (null != singleton) {
            synchronized (SingletonMailAccessCache.class) {
                if (null != singleton) {
                    singleton.releaseCache();
                    singleton = null;
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private TimeoutConcurrentMap<Key, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> timeoutMap;
    private int defaultIdleSeconds;

    /**
     * Prevent instantiation.
     *
     * @throws OXException If initialization fails
     */
    private SingletonMailAccessCache() throws OXException {
        super();
        initCache();
    }

    /**
     * Initializes cache reference.
     *
     * @throws OXException If initializing the time-out map reference fails
     */
    public void initCache() throws OXException {
        /*
         * Check for proper started mail cache configuration
         */
        if (!MailCacheConfiguration.getInstance().isStarted()) {
            throw MailExceptionCode.INITIALIZATION_PROBLEM.create();
        }
        if (timeoutMap != null) {
            return;
        }
        timeoutMap = new TimeoutConcurrentMap<Key, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>>(MailProperties.getInstance().getMailAccessCacheShrinkerSeconds());
        timeoutMap.setDefaultTimeoutListener(new MailAccessTimeoutListener());
        defaultIdleSeconds = MailProperties.getInstance().getMailAccessCacheIdleSeconds();
    }

    @Override
    public void close() {
        releaseCache();
    }

    /**
     * Releases cache reference.
     */
    public void releaseCache() {
        if (timeoutMap == null) {
            return;
        }
        timeoutMap.timeoutAll();
        timeoutMap.dispose();
        timeoutMap = null;
        defaultIdleSeconds = 0;
    }

    @Override
    public int numberOfMailAccesses(final Session session, final int accountId) throws OXException {
        return null == timeoutMap.get(getUserKey(session.getUserId(), accountId, session.getContextId())) ? 0 : 1;
    }

    /**
     * Removes and returns a mail access from cache.
     *
     * @param session The session
     * @param accountId The account ID
     * @return An active instance of {@link MailAccess} or <code>null</code>
     */
    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> removeMailAccess(final Session session, final int accountId) {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = timeoutMap.remove(getUserKey(session.getUserId(), accountId, session.getContextId()));
        if (null == mailAccess) {
            return null;
        }
        mailAccess.setCached(false);
        return mailAccess;
    }

    /**
     * Puts given mail access into cache if none user-bound connection is already contained in cache.
     *
     * @param session The session
     * @param accountId The account ID
     * @param mailAccess The mail access to put into cache
     * @return <code>true</code> if mail access could be successfully cached; otherwise <code>false</code>
     */
    @Override
    public boolean putMailAccess(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        int idleTime = mailAccess.getCacheIdleSeconds();
        if (idleTime <= 0) {
            idleTime = defaultIdleSeconds;
        }
        if (null == timeoutMap.putIfAbsent(getUserKey(session.getUserId(), accountId, session.getContextId()), mailAccess, idleTime)) {
            mailAccess.setCached(true);
            return true;
        }
        return false;
    }

    /**
     * Checks if cache already holds a user-bound mail access for specified account.
     *
     * @param session The session
     * @param accountId The account ID
     * @return <code>true</code> if a user-bound mail access is already present in cache; otherwise <code>false</code>
     */
    @Override
    public boolean containsMailAccess(final Session session, final int accountId) {
        return (timeoutMap.get(getUserKey(session.getUserId(), accountId, session.getContextId())) != null);
    }

    /**
     * Clears the cache entries kept for specified user.
     *
     * @param session The session
     * @throws OXException If clearing user entries fails
     */
    @Override
    public void clearUserEntries(final Session session) throws OXException {
        final int user = session.getUserId();
        final int cid = session.getContextId();
        final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
            MailAccountStorageService.class,
            true);
        final MailAccount[] accounts = storageService.getUserMailAccounts(user, cid);
        for (final MailAccount mailAccount : accounts) {
            timeoutMap.timeout(getUserKey(user, mailAccount.getId(), cid));
        }
    }

    private static Key getUserKey(final int user, final int accountId, final int cid) {
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
