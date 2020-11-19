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

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.queue.MailAccessQueue;
import com.openexchange.mail.cache.queue.MailAccessQueueImpl;
import com.openexchange.mail.cache.queue.SingletonMailAccessQueue;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link EnqueueingMailAccessCache} - A very volatile cache for already connected instances of {@link MailAccess}.
 * <p>
 * A bounded {@link Queue} is used to store {@link MailAccess} instances to improve subsequent mail requests.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EnqueueingMailAccessCache implements IMailAccessCache {

    /**
     * The logger instance.
     */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EnqueueingMailAccessCache.class);

    /**
     * Drop those queues of which all elements timed-out.
     */
    private static final boolean DROP_TIMED_OUT_QUEUES = false;

    private static volatile EnqueueingMailAccessCache singleton;

    /**
     * Gets the singleton instance.
     *
     * @queueCapacity The max. queue capacity
     * @return The singleton instance
     * @throws OXException If instance initialization fails
     */
    public static EnqueueingMailAccessCache getInstance(final int queueCapacity) throws OXException {
        EnqueueingMailAccessCache tmp = singleton;
        if (null == tmp) {
            synchronized (EnqueueingMailAccessCache.class) {
                tmp = singleton;
                if (null == tmp) {
                    singleton = tmp = new EnqueueingMailAccessCache(queueCapacity);
                }
            }
        }
        return tmp;
    }

    /**
     * (Optionally) Gets the singleton instance.
     *
     * @return The singleton instance
     */
    public static EnqueueingMailAccessCache optInstance() {
        return singleton;
    }

    /**
     * Releases the singleton instance.
     */
    public static void releaseInstance() {
        if (null != singleton) {
            synchronized (EnqueueingMailAccessCache.class) {
                if (null != singleton) {
                    singleton.dispose();
                    singleton = null;
                }
            }
        }
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                EnqueueingMailAccessCache tmp = singleton;
                if (null != tmp) {
                    try {
                        int shrinkerSeconds = configService.getIntProperty("com.openexchange.mail.mailAccessCacheShrinkerSeconds", 3);
                        int idleSeconds = configService.getIntProperty("com.openexchange.mail.mailAccessCacheIdleSeconds", 4);

                        tmp.setIdleAndShrinkerSeconds(idleSeconds, shrinkerSeconds, false);
                    } catch (OXException e) {
                        LOG.error("Failed to re-initialize singleton mail-access cache", e);
                    }
                }
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.mail.mailAccessCacheShrinkerSeconds", "com.openexchange.mail.mailAccessCacheIdleSeconds");
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<UserAndContext, AccountMap> map;
    private volatile int defaultIdleSeconds;
    private volatile ScheduledTimerTask timerTask;

    /**
     * The number of {@link MailAccess} instances which may be concurrently active/opened per account for a user.
     */
    private final int fallbackQueueCapacity;

    /**
     * Prevent instantiation.
     *
     * @param fallbackQueueCapacity The max. queue capacity
     * @throws OXException If an error occurs
     */
    private EnqueueingMailAccessCache(final int fallbackQueueCapacity) throws OXException {
        super();
        this.fallbackQueueCapacity = fallbackQueueCapacity;
        map = new NonBlockingHashMap<UserAndContext, AccountMap>();
        setIdleAndShrinkerSeconds(MailProperties.getInstance().getMailAccessCacheIdleSeconds(), MailProperties.getInstance().getMailAccessCacheShrinkerSeconds(), true);
    }

    void setIdleAndShrinkerSeconds(int idleSeconds, int shrinkerSeconds, boolean withInitialDelay) throws OXException {
        TimerService service = ServerServiceRegistry.getInstance().getService(TimerService.class, true);

        ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            this.timerTask = null;
            timerTask.cancel();
            service.purge();
        }

        defaultIdleSeconds = idleSeconds <= 0 ? 7 : idleSeconds;
        int shrinkerMillis = (shrinkerSeconds <= 0 ? 3 : shrinkerSeconds) * 1000;
        this.timerTask = service.scheduleWithFixedDelay(new PurgeExpiredRunnable(map), withInitialDelay ? shrinkerMillis : 0L, shrinkerMillis);
    }


    @Override
    public int numberOfMailAccesses(Session session, int accountId) throws OXException {
        AccountMap accounts = map.get(UserAndContext.newInstance(session));
        if (accounts == null) {
            return 0;
        }

        synchronized (accounts) {
            if (accounts.deprecated) {
                return 0;
            }

            MailAccessQueue accessQueue = accounts.map.get(I(accountId));
            if (null == accessQueue) {
                return 0;
            }
            synchronized (accessQueue) {
                return accessQueue.size();
            }
        }
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
        AccountMap accounts = map.get(UserAndContext.newInstance(session));
        if (accounts == null) {
            return null;
        }

        synchronized (accounts) {
            if (accounts.deprecated) {
                return null;
            }

            MailAccessQueue accessQueue = accounts.map.get(I(accountId));
            if (null == accessQueue) {
                return null;
            }

            synchronized (accessQueue) {
                if (accessQueue.isDeprecated()) {
                    return null;
                }
                final PooledMailAccess pooledMailAccess = accessQueue.poll();
                if (null == pooledMailAccess) {
                    return null;
                }
                final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = pooledMailAccess.getMailAccess();
                mailAccess.setCached(false);
                LOG.debug("Remove&Get for account {} of user {} in context {}", I(accountId), I(session.getUserId()), I(session.getContextId()));
                return mailAccess;
            }
        }
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
        int idleSeconds = mailAccess.getCacheIdleSeconds();
        if (idleSeconds <= 0) {
            idleSeconds = defaultIdleSeconds;
        }

        UserAndContext userAndContext = UserAndContext.newInstance(session);
        AccountMap accounts = map.get(userAndContext);
        if (accounts == null) {
            AccountMap newAccounts = new AccountMap();
            accounts = map.putIfAbsent(userAndContext, newAccounts);
            if (accounts == null) {
                accounts = newAccounts;
            }
        }

        synchronized (accounts) {
            if (accounts.deprecated) {
                return putMailAccess(session, accountId, mailAccess);
            }

            MailAccessQueue accessQueue = accounts.map.get(I(accountId));
            if (null == accessQueue || accessQueue.isDeprecated()) {
                // First check capacity boundary per mail provider and fall back to default if check fails
                int capacity;
                try {
                    capacity = MailProviderRegistry.getMailProviderBySession(session, accountId).getProtocol().getMaxCount(mailAccess.getMailConfig().getServer(), MailAccount.DEFAULT_ID == accountId);
                } catch (OXException e) {
                    capacity = fallbackQueueCapacity;
                }
                final MailAccessQueue tmp = capacity > 0 ? (1 == capacity ? new SingletonMailAccessQueue() : new MailAccessQueueImpl(capacity)) : new MailAccessQueueImpl(-1);
                accessQueue = accounts.map.putIfAbsent(I(accountId), tmp);
                if (null == accessQueue) {
                    accessQueue = tmp;
                }
            }
            synchronized (accessQueue) {
                if (accessQueue.isDeprecated()) {
                    return false;
                }
                /*
                 * Insert subsequent MailAccess instances with halved time-to-live seconds
                 */
                idleSeconds = accessQueue.isEmpty() ? idleSeconds : (idleSeconds >> 1);
                if (accessQueue.offer(PooledMailAccess.valueFor(mailAccess, idleSeconds * 1000L))) {
                    mailAccess.setCached(true);
                    return true;
                }
                return false;
            }
        }
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
        AccountMap accounts = map.get(UserAndContext.newInstance(session));
        if (accounts == null) {
            return false;
        }

        synchronized (accounts) {
            if (accounts.deprecated) {
                return false;
            }

            MailAccessQueue accessQueue = accounts.map.get(I(accountId));
            if (null == accessQueue) {
                return false;
            }
            synchronized (accessQueue) {
                return !accessQueue.isDeprecated() && !accessQueue.isEmpty();
            }
        }
    }

    @Override
    public void close() {
        dispose();
    }

    /**
     * Disposes this cache.
     */
    protected void dispose() {
        ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            this.timerTask = null;
            timerTask.cancel(false);
        }

        for (Iterator<Entry<UserAndContext, AccountMap>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UserAndContext, AccountMap> mapEntry = it.next();
            AccountMap accounts = mapEntry.getValue();
            synchronized (accounts) {
                if (accounts.deprecated) {
                    continue;
                }

                accounts.deprecated = true;
                it.remove();

                for (Map.Entry<Integer, MailAccessQueue> entry : accounts.map.entrySet()) {
                    MailAccessQueue accessQueue = entry.getValue();
                    synchronized (accessQueue) {
                        accessQueue.markDeprecated();
                        PooledMailAccess pooledMailAccess;
                        while (null != (pooledMailAccess = accessQueue.poll())) {
                            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = pooledMailAccess.getMailAccess();
                            LOG.debug("Dropping: {}", mailAccess);
                            mailAccess.setCached(false);
                            mailAccess.close(false);
                        }
                    }
                }
                accounts.map.clear();
            }
        }
    }

    @Override
    public void clearUserEntries(int userId, int contextId) throws OXException {
        UserAndContext userAndContext = UserAndContext.newInstance(userId, contextId);
        AccountMap accounts = map.get(userAndContext);
        if (accounts == null) {
            return;
        }

        synchronized (accounts) {
            if (accounts.deprecated) {
                return;
            }

            accounts.deprecated = true;
            map.remove(userAndContext);

            for (Map.Entry<Integer, MailAccessQueue> entry : accounts.map.entrySet()) {
                MailAccessQueue accessQueue = entry.getValue();
                synchronized (accessQueue) {
                    accessQueue.markDeprecated();
                    PooledMailAccess pooledMailAccess;
                    while (null != (pooledMailAccess = accessQueue.poll())) {
                        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = pooledMailAccess.getMailAccess();
                        LOG.debug("Dropping: {}", mailAccess);
                        mailAccess.setCached(false);
                        mailAccess.close(false);
                    }
                }
            }
            accounts.map.clear();
        }
    }

    private static class AccountMap {

        final Map<Integer, MailAccessQueue> map;
        boolean deprecated;

        AccountMap() {
            super();
            map = new HashMap<>(6);
        }
    }

    private static class PurgeExpiredRunnable implements Runnable {

        private final ConcurrentMap<UserAndContext, AccountMap> map;

        PurgeExpiredRunnable(final ConcurrentMap<UserAndContext, AccountMap> map) {
            super();
            this.map = map;
        }

        @Override
        public void run() {
            try {
                /*
                 * Shall timed-out queues be removed from mapping?
                 */
                boolean dropQueue = isDropQueue();
                /*
                 * Iterate mapping
                 */
                for (Iterator<Map.Entry<UserAndContext, AccountMap>> it = map.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<UserAndContext, AccountMap> mapEntry = it.next();
                    UserAndContext userAndContext = mapEntry.getKey();
                    AccountMap accounts = mapEntry.getValue();

                    synchronized (accounts) {
                        for (Iterator<Map.Entry<Integer, MailAccessQueue>> iterator = accounts.map.entrySet().iterator(); iterator.hasNext();) {
                            Map.Entry<Integer, MailAccessQueue> entry = iterator.next();
                            MailAccessQueue accessQueue = entry.getValue();
                            synchronized (accessQueue) {
                                PooledMailAccess pooledMailAccess;
                                while (null != (pooledMailAccess = accessQueue.pollDelayed())) {
                                    final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = pooledMailAccess.getMailAccess();
                                    mailAccess.setCached(false);
                                    LOG.debug("Timed-out mail access for account {} of user {} in context {}", entry.getKey(), I(userAndContext.getUserId()), I(userAndContext.getContextId()));
                                    mailAccess.close(false);
                                }
                                if (dropQueue && accessQueue.isEmpty()) {
                                    /*
                                     * Current queue is empty. Mark as deprecated
                                     */
                                    accessQueue.markDeprecated();
                                    LOG.debug("Dropped queue for account {} of user {} in context {}", entry.getKey(), I(userAndContext.getUserId()), I(userAndContext.getContextId()));
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
                // A runtime exception
                LOG.warn("Purge-expired run failed", e);
            }
        }

        private boolean isDropQueue() {
            return DROP_TIMED_OUT_QUEUES;
        }
    }

}
