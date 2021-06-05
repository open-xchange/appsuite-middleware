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

package com.openexchange.mail.cache;

import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;
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
     * Gets the singleton instance.
     *
     * @return The singleton instance
     * @throws OXException If instance initialization fails
     */
    public static SingletonMailAccessCache getInstance() throws OXException {
        SingletonMailAccessCache tmp = singleton;
        if (null == tmp) {
            synchronized (SingletonMailAccessCache.class) {
                tmp = singleton;
                if (null == tmp) {
                    tmp = new SingletonMailAccessCache();
                    singleton = tmp;
                }
            }
        }
        return tmp;
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
        SingletonMailAccessCache tmp = singleton;
        if (null != tmp) {
            synchronized (SingletonMailAccessCache.class) {
                tmp = singleton;
                if (null != tmp) {
                    tmp.releaseCache();
                    singleton = null;
                }
            }
        }
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                SingletonMailAccessCache tmp = singleton;
                if (null != tmp) {
                    try {
                        int shrinkerSeconds = configService.getIntProperty("com.openexchange.mail.mailAccessCacheShrinkerSeconds", 3);
                        int idleSeconds = configService.getIntProperty("com.openexchange.mail.mailAccessCacheIdleSeconds", 4);

                        tmp.defaultIdleSeconds = idleSeconds;
                        tmp.timeoutMap.setShrinkerIntervalSeconds(shrinkerSeconds);
                    } catch (OXException e) {
                        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SingletonMailAccessCache.class);
                        logger.error("Failed to re-initialize singleton mail-access cache", e);
                    }
                }
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.mail.mailAccessCacheShrinkerSeconds", "com.openexchange.mail.mailAccessCacheIdleSeconds");
            }
        });
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final TimeoutConcurrentMap<Key, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>> timeoutMap;
    private volatile int defaultIdleSeconds;

    /**
     * Prevent instantiation.
     *
     * @throws OXException If initialization fails
     */
    private SingletonMailAccessCache() throws OXException {
        super();
        /*
         * Check for proper started mail cache configuration
         */
        if (!MailCacheConfiguration.getInstance().isStarted()) {
            throw MailExceptionCode.INITIALIZATION_PROBLEM.create();
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
        defaultIdleSeconds = 0;
    }

    @Override
    public int numberOfMailAccesses(Session session, int accountId) throws OXException {
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
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> removeMailAccess(Session session, int accountId) {
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
    public boolean putMailAccess(Session session, int accountId, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
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
    public boolean containsMailAccess(Session session, int accountId) {
        return (timeoutMap.get(getUserKey(session.getUserId(), accountId, session.getContextId())) != null);
    }

    @Override
    public void clearUserEntries(int userId, int contextId) throws OXException {
        MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
        final MailAccount[] accounts = storageService.getUserMailAccounts(userId, contextId);
        for (MailAccount mailAccount : accounts) {
            timeoutMap.timeout(getUserKey(userId, mailAccount.getId(), contextId));
        }
    }

    private static Key getUserKey(int user, int accountId, int cid) {
        return new Key(user, cid, accountId);
    }

    private static final class Key {

        private final int user;
        private final int cid;
        private final int accountId;
        private final int hash;

        Key(int user, int cid, int accountId) {
            super();
            this.user = user;
            this.cid = cid;
            this.accountId = accountId;

            int prime = 31;
            int result = 1;
            result = prime * result + cid;
            result = prime * result + user;
            result = prime * result + accountId;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
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
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            if (accountId != other.accountId) {
                return false;
            }
            return true;
        }

    }

}
