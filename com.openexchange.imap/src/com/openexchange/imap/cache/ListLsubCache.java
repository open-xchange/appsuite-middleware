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

package com.openexchange.imap.cache;

import static com.openexchange.java.Strings.isEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.mail.MessagingException;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.slf4j.Logger;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link ListLsubCache} - A user-bound cache for LIST/LSUB entries.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ListLsubCache {

    /**
     * The logger
     */
    protected static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ListLsubCache.class);

    private static final ListLsubCache INSTANCE = new ListLsubCache();

    private static final class Key {

        private final int cid;
        private final int user;
        private final int hash;

        protected Key(int user, int cid) {
            super();
            this.user = user;
            this.cid = cid;
            int prime = 31;
            int result = 1;
            result = prime * result + cid;
            result = prime * result + user;
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
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }

    } // End of class Key

    private static final class KeyedCache {

        private final Key key;

        KeyedCache(Key key) {
            super();
            this.key = key;
        }

        /**
         * Gets the associated object from cache
         *
         * @return The object or <code>null</code>
         */
        ConcurrentMap<Integer, Future<ListLsubCollection>> get() {
            return CACHE.get(key);
        }

        /**
         * Safely puts given object into cache
         *
         * @param value The object to put
         * @return The object previously in cache or <code>null</code>
         * @throws OXException If there is already such an entry in cache
         */
        ConcurrentMap<Integer, Future<ListLsubCollection>> putIfAbsent(ConcurrentMap<Integer, Future<ListLsubCollection>> map) {
            return CACHE.putIfAbsent(key, map);
        }

        /**
         * Removes the object from cache
         */
        void remove() {
            CACHE.remove(key);
        }

    } // End of class KeyedCache

    /**
     * The region name.
     */
    public static final String REGION = "ListLsubCache";

    private static KeyedCache getCache(Session session) {
        return getCache(session.getUserId(), session.getContextId());
    }

    private static KeyedCache getCache(int userId, int contextId) {
        return new KeyedCache(new Key(userId, contextId));
    }

    /** The default timeout for LIST/LSUB cache (6 minutes) */
    private static final long DEFAULT_TIMEOUT = 360000;

    private static final String INBOX = "INBOX";

    private static final boolean DO_STATUS = false;

    private static final boolean DO_GETACL = false;

    /** The cache */
    static final ConcurrentMap<Key, ConcurrentMap<Integer, Future<ListLsubCollection>>> CACHE = new NonBlockingHashMap<Key, ConcurrentMap<Integer, Future<ListLsubCollection>>>();

    /**
     * No instance
     */
    private ListLsubCache() {
        super();
    }

    /**
     * Drop caches for given session's user.
     *
     * @param session The session providing user information
     */
    public static void dropFor(Session session) {
        if (null != session) {
            dropFor(session.getUserId(), session.getContextId());
        }
    }

    /**
     * Drop caches for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void dropFor(int userId, int contextId) {
        dropFor(userId, contextId, true, false);
    }

    /**
     * Drop caches for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param notify Whether to notify
     * @param enforceNewConnection Whether a new connection is supposed to be used for cache initialization on subsequent calls
     */
    public static void dropFor(int userId, int contextId, boolean notify, boolean enforceNewConnection) {
        if (enforceNewConnection) {
            // Get the associated map
            ConcurrentMap<Integer, Future<ListLsubCollection>> map = getCache(userId, contextId).get();
            if (null != map) {
                for (Future<ListLsubCollection> f : map.values()) {
                    try {
                        ListLsubCollection collection = getFrom(f);
                        synchronized (collection) {
                            collection.clear(enforceNewConnection);
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                }

            }
        } else {
            getCache(userId, contextId).remove();
        }

        if (notify) {
            fireInvalidateCacheEvent(userId, contextId);
        }

        LOG.debug("Cleaned user-sensitive LIST/LSUB cache for user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId));
    }

    /**
     * Removes cached LIST/LSUB entry.
     *
     * @param fullName The full name
     * @param accountId The account ID
     * @param session The session
     */
    public static void removeCachedEntry(String fullName, int accountId, Session session) {
        ConcurrentMap<Integer, Future<ListLsubCollection>> map = getCache(session).get();
        if (null == map) {
            return;
        }
        ListLsubCollection collection = getSafeFrom(map.get(Integer.valueOf(accountId)));
        if (null != collection) {
            synchronized (collection) {
                collection.remove(fullName);
            }

            fireInvalidateCacheEvent(session);
        }
    }

    /**
     * Checks if associated mailbox is considered as MBox format.
     *
     * @param accountId The account ID
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return {@link Boolean#TRUE} for MBox format, {@link Boolean#FALSE} for no MBOX format or <code>null</code> if undetermined
     * @throws OXException if a mail error occurs
     * @throws MessagingException If a messaging error occurs
     */
    public static Boolean consideredAsMBox(int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
            return collection.consideredAsMBox();
        }
    }

    /**
     * Clears the cache.
     *
     * @param accountId The account ID
     * @param session The session
     */
    public static void clearCache(int accountId, Session session) {
        clearCache(accountId, session.getUserId(), session.getContextId());
    }

    /**
     * Clears the cache.
     *
     * @param accountId The account ID
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public static void clearCache(int accountId, int userId, int contextId) {
        ConcurrentMap<Integer, Future<ListLsubCollection>> map = getCache(userId, contextId).get();
        if (null == map) {
            return;
        }
        ListLsubCollection collection = getSafeFrom(map.get(Integer.valueOf(accountId)));
        if (null != collection) {
            synchronized (collection) {
                collection.clear(false);
            }

            fireInvalidateCacheEvent(userId, contextId);
        }
    }

    /**
     * Adds single entry to cache. Replaces any existing entry.
     *
     * @param fullName The entry's full name
     * @param accountId The account ID
     * @param imapFolder The IMAP folder providing connected protocol
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @throws OXException If entry could not be added
     * @throws MessagingException If a messaging error occurs
     */
    public static void addSingle(String fullName, int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            if (checkTimeStamp(imapFolder, collection, ignoreSubscriptions)) {
                return;
            }
            collection.addSingle(fullName, imapFolder, DO_STATUS, DO_GETACL);

            fireInvalidateCacheEvent(session);
        }
    }

    /**
     * Adds single entry to cache. Replaces any existing entry.
     *
     * @param imapFolder The IMAP folder to add
     * @param subscribed Whether IMAP folder is subscribed
     * @param fullName The entry's full name
     * @param accountId The account ID
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @throws OXException If entry could not be added
     * @throws MessagingException If a messaging error occurs
     */
    public static void addSingle(IMAPFolder imapFolder, boolean subscribed, int accountId, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            if (checkTimeStamp(imapFolder, collection, ignoreSubscriptions)) {
                return;
            }
            collection.addSingle(imapFolder, subscribed, DO_STATUS, DO_GETACL);

            fireInvalidateCacheEvent(session);
        }
    }

    /**
     * Adds single entry to cache. Replaces any existing entry.
     *
     * @param accountId The account ID
     * @param imapFolder The IMAP folder providing connected protocol
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @throws OXException If entry could not be added
     * @throws MessagingException If a messaging error occurs
     */
    public static ListLsubEntry addSingleByFolder(int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            boolean addIt = !checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
            return collection.addSingleByFolder(imapFolder, addIt);
        }
    }

    /**
     * Gets the separator character.
     *
     * @param accountId The account ID
     * @param imapStore The connected IMAP store instance
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The separator
     * @throws OXException If a mail error occurs
     */
    public static char getSeparator(int accountId, IMAPStore imapStore, Session session, boolean ignoreSubscriptions) throws OXException {
        try {
            return getSeparator(accountId, (IMAPFolder) imapStore.getFolder(INBOX), session, ignoreSubscriptions);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the separator character.
     *
     * @param accountId The account ID
     * @param imapFolder An IMAP folder
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The separator
     * @throws OXException If a mail error occurs
     * @throws MessagingException If a messaging error occurs
     */
    public static char getSeparator(int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        return getCachedLISTEntry(INBOX, accountId, imapFolder, session, ignoreSubscriptions).getSeparator();
    }

    private static boolean seemsValid(ListLsubEntry entry) {
        return (null != entry) && (entry.canOpen() || entry.isNamespace() || entry.hasChildren());
    }

    /**
     * Gets cached LSUB entry for specified full name.
     *
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The cached LSUB entry
     * @throws OXException If loading the entry fails
     * @throws MessagingException If a messaging error occurs
     */
    public static ListLsubEntry getCachedLSUBEntry(String fullName, int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        if (isAccessible(collection)) {
            ListLsubEntry entry = collection.getLsubIgnoreDeprecated(fullName);
            if (seemsValid(entry)) {
                return entry;
            }
        }
        synchronized (collection) {
            if (checkTimeStamp(imapFolder, collection, ignoreSubscriptions)) {
                ListLsubEntry entry = collection.getLsub(fullName);
                return null == entry ? ListLsubCollection.emptyEntryFor(fullName) : entry;
            }
            /*
             * Return
             */
            ListLsubEntry entry = collection.getLsub(fullName);
            if (seemsValid(entry)) {
                return entry;
            }
            /*
             * Update & re-check
             */
            boolean exists = IMAPCommandsCollection.exists(fullName, imapFolder);
            if (false == exists) {
                return ListLsubCollection.emptyEntryFor(fullName);
            }
            collection.update(fullName, imapFolder, DO_STATUS, DO_GETACL, ignoreSubscriptions);
            fireInvalidateCacheEvent(session);
            entry = collection.getLsub(fullName);
            return null == entry ? ListLsubCollection.emptyEntryFor(fullName) : entry;
        }
    }

    /**
     * Gets cached LIST entry for specified full name.
     *
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapStore The IMAP store
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The cached LIST entry
     * @throws OXException If loading the entry fails
     */
    public static ListLsubEntry getCachedLISTEntry(String fullName, int accountId, IMAPStore imapStore, Session session, boolean ignoreSubscriptions) throws OXException {
        try {
            IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(INBOX);
            ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
            if (isAccessible(collection)) {
                ListLsubEntry entry = collection.getListIgnoreDeprecated(fullName);
                if (seemsValid(entry)) {
                    return entry;
                }
            }
            synchronized (collection) {
                if (checkTimeStamp(imapFolder, collection, ignoreSubscriptions)) {
                    ListLsubEntry entry = collection.getList(fullName);
                    return null == entry ? ListLsubCollection.emptyEntryFor(fullName) : entry;
                }
                /*
                 * Return
                 */
                ListLsubEntry entry = collection.getList(fullName);
                if (seemsValid(entry)) {
                    return entry;
                }
                /*
                 * Update & re-check
                 */
                boolean exists = IMAPCommandsCollection.exists(fullName, imapFolder);
                if (false == exists) {
                    return ListLsubCollection.emptyEntryFor(fullName);
                }
                collection.update(fullName, imapFolder, DO_STATUS, DO_GETACL, ignoreSubscriptions);
                fireInvalidateCacheEvent(session);
                entry = collection.getList(fullName);
                return null == entry ? ListLsubCollection.emptyEntryFor(fullName) : entry;
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Initializes ACL list
     *
     * @param accountId The account identifier
     * @param imapStore The IMAP store
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @throws OXException If initialization fails
     */
    public static void initACLs(int accountId, IMAPStore imapStore, Session session, boolean ignoreSubscriptions) throws OXException {
        if (DO_GETACL) {
            // Already perform during initialization
            return;
        }
        try {
            IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(INBOX);
            ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
            synchronized (collection) {
                collection.initACLs(imapFolder);
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets up-to-date LIST entry for specified full name.
     *
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapStore The IMAP store
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The cached LIST entry
     * @throws MailException If loading the entry fails
     */
    public static ListLsubEntry getActualLISTEntry(String fullName, int accountId, IMAPStore imapStore, Session session, boolean ignoreSubscriptions) throws OXException {
        try {
            IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(INBOX);
            ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
            synchronized (collection) {
                return collection.getActualEntry(fullName, imapFolder);
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the pretty-printed cache content
     *
     * @param accountId The account identifier
     * @param session The associated session
     * @return The pretty-printed content or <code>null</code>
     */
    public static String prettyPrintCache(int accountId, Session session) {
        return prettyPrintCache(accountId, session.getUserId(), session.getContextId());
    }

    /**
     * Gets the pretty-printed cache content
     *
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The pretty-printed content or <code>null</code>
     */
    public static String prettyPrintCache(int accountId, int userId, int contextId) {
        try {
            KeyedCache cache = getCache(userId, contextId);

            // Get the associated map
            ConcurrentMap<Integer, Future<ListLsubCollection>> map = cache.get();
            if (null == map) {
                return null;
            }

            // Submit task
            Future<ListLsubCollection> f = map.get(Integer.valueOf(accountId));
            if (null == f) {
                return null;
            }

            ListLsubCollection collection = getFrom(f);
            return collection.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tries to gets cached LIST entry for specified full name.
     * <p>
     * Performs no initializations if cache or entry is absent
     *
     * @param fullName The full name
     * @param accountId The account ID
     * @param session The session
     * @return The cached LIST entry or <code>null</code>
     * @throws OXException If loading the entry fails
     * @throws MessagingException If a messaging error occurs
     */
    public static ListLsubEntry tryCachedLISTEntry(String fullName, int accountId, Session session) throws OXException, MessagingException {
        KeyedCache cache = getCache(session);

        // Get the associated map
        ConcurrentMap<Integer, Future<ListLsubCollection>> map = cache.get();
        if (null == map) {
            return null;
        }

        Future<ListLsubCollection> f = map.get(Integer.valueOf(accountId));
        if (null == f) {
            return null;
        }

        try {
            ListLsubCollection collection = getFrom(f);
            return collection.getListIgnoreDeprecated(fullName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets cached LIST entry for specified full name.
     *
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The cached LIST entry
     * @throws OXException If loading the entry fails
     * @throws MessagingException If a messaging error occurs
     */
    public static ListLsubEntry getCachedLISTEntry(String fullName, int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        return getCachedLISTEntry(fullName, accountId, imapFolder, session, ignoreSubscriptions, false);
    }

    /**
     * Gets cached LIST entry for specified full name.
     *
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @param reinitSpecialUseIfLoaded <code>true</code> to re-initialize SPECIAL-USE folders in case cache is already loaded; otherwise <code>false</code>
     * @return The cached LIST entry
     * @throws OXException If loading the entry fails
     * @throws MessagingException If a messaging error occurs
     */
    public static ListLsubEntry getCachedLISTEntry(String fullName, int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions, boolean reinitSpecialUseIfLoaded) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        if (isAccessible(collection)) {
            ListLsubEntry entry = collection.getListIgnoreDeprecated(fullName);
            if (seemsValid(entry)) {
                if (reinitSpecialUseIfLoaded) {
                    collection.reinitSpecialUseFolders(imapFolder);
                }
                return entry;
            }
        }
        synchronized (collection) {
            if (checkTimeStamp(imapFolder, collection, ignoreSubscriptions)) {
                if (reinitSpecialUseIfLoaded) {
                    collection.reinitSpecialUseFolders(imapFolder);
                }
                ListLsubEntry entry = collection.getList(fullName);
                return null == entry ? ListLsubCollection.emptyEntryFor(fullName) : entry;
            }
            /*
             * Return
             */
            ListLsubEntry entry = collection.getList(fullName);
            if (seemsValid(entry)) {
                if (reinitSpecialUseIfLoaded) {
                    collection.reinitSpecialUseFolders(imapFolder);
                }
                return entry;
            }
            /*
             * Update & re-check
             */
            boolean exists = IMAPCommandsCollection.exists(fullName, imapFolder);
            if (false == exists) {
                return ListLsubCollection.emptyEntryFor(fullName);
            }
            collection.update(fullName, imapFolder, DO_STATUS, DO_GETACL, ignoreSubscriptions);
            fireInvalidateCacheEvent(session);
            entry = collection.getList(fullName);
            return null == entry ? ListLsubCollection.emptyEntryFor(fullName) : entry;
        }
    }

    /**
     * Gets cached LIST entry for specified full name.
     *
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The cached LIST entry or an empty entry
     * @throws OXException If loading the entry fails
     * @throws MessagingException If a messaging error occurs
     */
    public static ListLsubEntry optCachedLISTEntry(String fullName, int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        if (isAccessible(collection)) {
            ListLsubEntry entry = collection.getListIgnoreDeprecated(fullName);
            return null == entry ? ListLsubCollection.emptyEntryFor(fullName) : entry;
        }
        synchronized (collection) {
            checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
            ListLsubEntry entry = collection.getList(fullName);
            return null == entry ? ListLsubCollection.emptyEntryFor(fullName) : entry;
        }
    }

    private static boolean checkTimeStamp(IMAPFolder imapFolder, ListLsubCollection collection, boolean ignoreSubscriptions) throws MessagingException {
        /*
         * Check collection's deprecation status and stamp
         */
        if (collection.isDeprecated()) {
            collection.reinit(imapFolder, DO_STATUS, DO_GETACL, ignoreSubscriptions);
            return true;
        }
        if ((System.currentTimeMillis() - collection.getStamp()) > getTimeout()) {
            collection.reinit(imapFolder, DO_STATUS, DO_GETACL, ignoreSubscriptions);
            return true;
        }
        return false;
    }

    private static boolean isAccessible(ListLsubCollection collection) {
        return !collection.isDeprecated() && ((System.currentTimeMillis() - collection.getStamp()) <= getTimeout());
    }

    private static long getTimeout() {
        return IMAPProperties.getInstance().allowFolderCaches() ? DEFAULT_TIMEOUT : 20000L;
    }

    /**
     * Gets all LIST/LSUB entries.
     *
     * @param optParentFullName The optional full name of the parent
     * @param accountId The account identifier
     * @param subscribedOnly <code>false</code> for LIST entries; otherwise <code>true</code> for LSUB ones
     * @param imapStore The IMAP store
     * @param session The session
     * @return All LSUB entries
     * @throws OXException If loading the entry fails
     * @throws MessagingException If a messaging error occurs
     */
    public static List<ListLsubEntry> getAllEntries(String optParentFullName, int accountId, boolean subscribedOnly, IMAPStore imapStore, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        IMAPFolder imapFolder = (IMAPFolder) imapStore.getDefaultFolder();
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        if (isAccessible(collection)) {
            if (null == optParentFullName) {
                return subscribedOnly ? collection.getLsubsIgnoreDeprecated() : collection.getLists();
            }

            ListLsubEntry entry = subscribedOnly ? collection.getLsubIgnoreDeprecated(optParentFullName) : collection.getListIgnoreDeprecated(optParentFullName);
            if (null != entry) {
                return entry.getChildren();
            }
        }
        synchronized (collection) {
            if (checkTimeStamp(imapFolder, collection, ignoreSubscriptions)) {
                if (null == optParentFullName) {
                    return subscribedOnly ? collection.getLsubs() : collection.getLists();
                }

                ListLsubEntry entry = subscribedOnly ? collection.getLsub(optParentFullName) : collection.getList(optParentFullName);
                if (null != entry) {
                    return entry.getChildren();
                }
            }
            /*
             * Update & re-check
             */
            collection.reinit(imapStore, DO_STATUS, DO_GETACL, ignoreSubscriptions);
            fireInvalidateCacheEvent(session);
            if (null == optParentFullName) {
                return subscribedOnly ? collection.getLsubs() : collection.getLists();
            }

            ListLsubEntry entry = subscribedOnly ? collection.getLsub(optParentFullName) : collection.getList(optParentFullName);
            if (null != entry) {
                return entry.getChildren();
            }
            return Collections.emptyList();
        }
    }

    /**
     * Gets cached LIST/LSUB entry for specified full name.
     *
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The cached LIST/LSUB entry
     * @throws OXException If loading the entry fails
     * @throws MessagingException If a messaging error occurs
     */
    public static ListLsubEntry[] getCachedEntries(String fullName, int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        final ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        if (isAccessible(collection)) {
            ListLsubEntry listEntry = collection.getListIgnoreDeprecated(fullName);
            if (seemsValid(listEntry)) {
                ListLsubEntry lsubEntry = collection.getLsubIgnoreDeprecated(fullName);
                ListLsubEntry emptyEntryFor = ListLsubCollection.emptyEntryFor(fullName);
                return new ListLsubEntry[] { listEntry, lsubEntry == null ? emptyEntryFor : lsubEntry };
            }
        }
        synchronized (collection) {
            if (checkTimeStamp(imapFolder, collection, ignoreSubscriptions)) {
                ListLsubEntry listEntry = collection.getList(fullName);
                ListLsubEntry lsubEntry = collection.getLsub(fullName);
                ListLsubEntry emptyEntryFor = ListLsubCollection.emptyEntryFor(fullName);
                return new ListLsubEntry[] { listEntry == null ? emptyEntryFor : listEntry, lsubEntry == null ? emptyEntryFor : lsubEntry };
            }
            /*
             * Return
             */
            ListLsubEntry listEntry = collection.getList(fullName);
            if (!seemsValid(listEntry)) {
                /*
                 * Update & re-check
                 */
                collection.update(fullName, imapFolder, DO_STATUS, DO_GETACL, ignoreSubscriptions);
                fireInvalidateCacheEvent(session);
                listEntry = collection.getList(fullName);
            }
            ListLsubEntry lsubEntry = collection.getLsub(fullName);
            ListLsubEntry emptyEntryFor = ListLsubCollection.emptyEntryFor(fullName);
            return new ListLsubEntry[] { listEntry == null ? emptyEntryFor : listEntry, lsubEntry == null ? emptyEntryFor : lsubEntry };
        }
    }

    /**
     * Re-Initializes the SPECIAL-USE folders (only if the IMAP store advertises support for <code>"SPECIAL-USE"</code> capability)
     *
     * @param accountId The account identifier
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @throws OXException If re-initialization fails
     * @throws MessagingException If a messaging error occurs
     */
    public static void reinitSpecialUseFolders(int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            if (isAccessible(collection)) {
                collection.reinitSpecialUseFolders(imapFolder);
            } else {
                checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
                collection.reinitSpecialUseFolders(imapFolder);
            }
        }
    }

    /**
     * Gets the LIST entries marked with "\Drafts" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @param accountId The account identifier
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The entries
     * @throws OXException If loading the entries fails
     * @throws MessagingException If a messaging error occurs
     */
    public static Collection<ListLsubEntry> getDraftsEntry(int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
            return collection.getDraftsEntry();
        }
    }

    /**
     * Gets the LIST entries marked with "\Junk" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @param accountId The account identifier
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The entries
     * @throws OXException If loading the entries fails
     * @throws MessagingException If a messaging error occurs
     */
    public static Collection<ListLsubEntry> getJunkEntry(int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
            return collection.getJunkEntry();
        }
    }

    /**
     * Gets the LIST entries marked with "\Sent" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @param accountId The account identifier
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The entries
     * @throws OXException If loading the entries fails
     * @throws MessagingException If a messaging error occurs
     */
    public static Collection<ListLsubEntry> getSentEntry(int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
            return collection.getSentEntry();
        }
    }

    /**
     * Gets the LIST entries marked with "\Trash" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @param accountId The account identifier
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The entries
     * @throws OXException If loading the entries fails
     * @throws MessagingException If a messaging error occurs
     */
    public static Collection<ListLsubEntry> getTrashEntry(int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
            return collection.getTrashEntry();
        }
    }

    /**
     * Gets the LIST entries marked with "\Archive" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @param accountId The account identifier
     * @param imapFolder The IMAP folder providing the protocol to use
     * @param session The session
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @return The entries
     * @throws OXException If loading the entries fails
     * @throws MessagingException If a messaging error occurs
     */
    public static Collection<ListLsubEntry> getArchiveEntry(int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
            return collection.getArchiveEntry();
        }
    }

    private static ListLsubCollection getCollection(final int accountId, final IMAPFolder imapFolder, final Session session, final boolean ignoreSubscriptions) throws OXException, MessagingException {
        KeyedCache cache = getCache(session);

        // Get the associated map
        ConcurrentMap<Integer, Future<ListLsubCollection>> map = cache.get();
        if (null == map) {
            ConcurrentHashMap<Integer, Future<ListLsubCollection>> newmap = new ConcurrentHashMap<Integer, Future<ListLsubCollection>>();
            map = cache.putIfAbsent(newmap);
            if (null == map) {
                map = newmap;
            }
        }

        // Submit task
        Future<ListLsubCollection> f = map.get(Integer.valueOf(accountId));
        boolean caller = false;
        if (null == f) {
            FutureTask<ListLsubCollection> ft = new FutureTask<ListLsubCollection>(new Callable<ListLsubCollection>() {

                @Override
                public ListLsubCollection call() throws OXException, MessagingException {
                    String[] shared;
                    String[] user;
                    try {
                        IMAPStore imapStore = (IMAPStore) imapFolder.getStore();
                        try {
                            shared = check(NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session, accountId));
                        } catch (MessagingException e) {
                            if (imapStore.hasCapability("NAMESPACE")) {
                                LOG.warn("Couldn't get shared namespaces.", e);
                            } else {
                                LOG.debug("Couldn't get shared namespaces.", e);
                            }
                            shared = new String[0];
                        } catch (RuntimeException e) {
                            LOG.warn("Couldn't get shared namespaces.", e);
                            shared = new String[0];
                        }
                        try {
                            user = check(NamespaceFoldersCache.getUserNamespaces(imapStore, true, session, accountId));
                        } catch (MessagingException e) {
                            if (imapStore.hasCapability("NAMESPACE")) {
                                LOG.warn("Couldn't get user namespaces.", e);
                            } else {
                                LOG.debug("Couldn't get user namespaces.", e);
                            }
                            user = new String[0];
                        } catch (RuntimeException e) {
                            LOG.warn("Couldn't get user namespaces.", e);
                            user = new String[0];
                        }
                    } catch (MessagingException e) {
                        throw MimeMailException.handleMessagingException(e);
                    }
                    return new ListLsubCollection(imapFolder, shared, user, DO_STATUS, DO_GETACL, ignoreSubscriptions);
                }
            });
            f = map.putIfAbsent(Integer.valueOf(accountId), ft);
            if (null == f) {
                f = ft;
                ft.run();
                caller = true;
            }
        }
        try {
            return getFrom(f);
        } catch (OXException e) {
            if (caller) {
                cache.remove();
            }
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }
    }

    static String[] check(String[] array) {
        List<String> list = new ArrayList<String>(array.length);
        for (int i = 0; i < array.length; i++) {
            String s = array[i];
            if (!isEmpty(s)) {
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Checks for any subscribed subfolder.
     *
     * @param fullName The full name
     * @return <code>true</code> if a subscribed subfolder exists; otherwise <code>false</code>
     * @throws OXException If a mail error occurs
     * @throws MessagingException If a messaging error occurs
     */
    public static boolean hasAnySubscribedSubfolder(String fullName, int accountId, IMAPFolder imapFolder, Session session, boolean ignoreSubscriptions) throws OXException, MessagingException {
        ListLsubCollection collection = getCollection(accountId, imapFolder, session, ignoreSubscriptions);
        synchronized (collection) {
            checkTimeStamp(imapFolder, collection, ignoreSubscriptions);
            return collection.hasAnySubscribedSubfolder(fullName);
        }
    }

    private static ListLsubCollection getFrom(Future<ListLsubCollection> future) throws OXException, InterruptedException, MessagingException {
        if (null == future) {
            return null;
        }
        try {
            return future.get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof OXException) {
                throw (OXException) t;
            }
            if (t instanceof MessagingException) {
                throw (MessagingException) t;
            }
            if (t instanceof RuntimeException) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(t, t.getMessage());
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw MailExceptionCode.UNEXPECTED_ERROR.create(t, t.getMessage());
        }
    }

    private static ListLsubCollection getSafeFrom(Future<ListLsubCollection> future) {
        if (null == future) {
            return null;
        }
        try {
            return future.get();
        } catch (InterruptedException e) {
            // Cannot occur
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new IllegalStateException("Not unchecked", t);
            }
        }
    }

    private static void fireInvalidateCacheEvent(Session session) {
        fireInvalidateCacheEvent(session.getUserId(), session.getContextId());
    }

    private static void fireInvalidateCacheEvent(int userId, int contextId) {
        CacheEventService cacheEventService = Services.optService(CacheEventService.class);
        if (null != cacheEventService && cacheEventService.getConfiguration().remoteInvalidationForPersonalFolders()) {
            CacheEvent event = newCacheEventFor(userId, contextId);
            if (null != event) {
                cacheEventService.notify(INSTANCE, event, false);
            }
        }
    }

    /**
     * Creates a new cache event
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The cache event
     */
    private static CacheEvent newCacheEventFor(int userId, int contextId) {
        CacheService service = Services.optService(CacheService.class);
        return null == service ? null : CacheEvent.INVALIDATE(REGION, Integer.toString(contextId), service.newCacheKey(contextId, userId));
    }

}
