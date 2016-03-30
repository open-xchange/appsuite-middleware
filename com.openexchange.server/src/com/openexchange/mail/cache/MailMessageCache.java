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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailMessageCache} - Caches instances of {@link MailMessage} which are prepared for caching by invoking
 * {@link MailMessage#prepareForCaching()}; meaning to release all kept content references. Thus only message's header data is going to be
 * cached.
 * <p>
 * This cache is highly volatile. With every new list request all caches entries belonging to requesting user are removed. See this cache
 * region's configuration settings in file "@conf-path@/mailcache.ccf" for further information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessageCache {

    private static interface MailFieldUpdater {

        public void updateField(MailMessage mail, Object newValue);
    }

    private static final MailFieldUpdater flagsUpdater = new MailFieldUpdater() {

        @Override
        public void updateField(MailMessage mail, Object newValue) {
            int newFlags = mail.getFlags();
            int flags = ((Integer) newValue).intValue();
            boolean set = flags > 0;
            flags = set ? flags : flags * -1;
            if (((flags & MailMessage.FLAG_ANSWERED) > 0)) {
                newFlags = set ? (newFlags | MailMessage.FLAG_ANSWERED) : (newFlags & ~MailMessage.FLAG_ANSWERED);
            }
            if (((flags & MailMessage.FLAG_DELETED) > 0)) {
                newFlags = set ? (newFlags | MailMessage.FLAG_DELETED) : (newFlags & ~MailMessage.FLAG_DELETED);
            }
            if (((flags & MailMessage.FLAG_DRAFT) > 0)) {
                newFlags = set ? (newFlags | MailMessage.FLAG_DRAFT) : (newFlags & ~MailMessage.FLAG_DRAFT);
            }
            if (((flags & MailMessage.FLAG_FLAGGED) > 0)) {
                newFlags = set ? (newFlags | MailMessage.FLAG_FLAGGED) : (newFlags & ~MailMessage.FLAG_FLAGGED);
            }
            if (((flags & MailMessage.FLAG_SEEN) > 0)) {
                newFlags = set ? (newFlags | MailMessage.FLAG_SEEN) : (newFlags & ~MailMessage.FLAG_SEEN);
            }
            if (((flags & MailMessage.FLAG_USER) > 0)) {
                newFlags = set ? (newFlags | MailMessage.FLAG_USER) : (newFlags & ~MailMessage.FLAG_USER);
            }
            if (((flags & MailMessage.FLAG_SPAM) > 0)) {
                newFlags = set ? (newFlags | MailMessage.FLAG_SPAM) : (newFlags & ~MailMessage.FLAG_SPAM);
            }
            if (((flags & MailMessage.FLAG_FORWARDED) > 0)) {
                newFlags = set ? (newFlags | MailMessage.FLAG_FORWARDED) : (newFlags & ~MailMessage.FLAG_FORWARDED);
            }
            if (((flags & MailMessage.FLAG_READ_ACK) > 0)) {
                newFlags = set ? (newFlags | MailMessage.FLAG_READ_ACK) : (newFlags & ~MailMessage.FLAG_READ_ACK);
            }
            mail.setFlags(newFlags);
        }
    };

    private static final MailFieldUpdater colorFlagUpdater = new MailFieldUpdater() {

        @Override
        public void updateField(MailMessage mail, Object newValue) {
            mail.setColorLabel(((Integer) newValue).intValue());
        }
    };

    private static MailFieldUpdater[] createMailFieldUpdater(MailListField[] changedFields) {
        MailFieldUpdater[] updaters = new MailFieldUpdater[changedFields.length];
        for (int i = 0; i < changedFields.length; i++) {
            switch (changedFields[i]) {
            case FLAGS:
                updaters[i] = flagsUpdater;
                break;
            case COLOR_LABEL:
                updaters[i] = colorFlagUpdater;
                break;
            default:
                throw new IllegalStateException("No Updater for MailListField." + changedFields[i].toString());
            }
        }
        return updaters;
    }

    static final String REGION_NAME = "OXMessageCache";

    private static final ConcurrentMap<CacheKey, ReadWriteLock> contextLocks = new ConcurrentHashMap<CacheKey, ReadWriteLock>();

    private static volatile MailMessageCache singleton;

    /*-
     * ############################################## Field members ##############################################
     */

    private volatile Cache cache;

    /**
     * Singleton instantiation.
     *
     * @throws OXException If cache instantiation fails
     */
    private MailMessageCache() throws OXException {
        super();
        initCache();
    }

    /**
     * Initializes cache reference.
     *
     * @throws OXException If initializing the cache reference fails
     */
    public void initCache() throws OXException {
        /*
         * Check for proper started mail cache configuration
         */
        if (!MailCacheConfiguration.getInstance().isStarted()) {
            throw MailExceptionCode.INITIALIZATION_PROBLEM.create();
        }
        if (cache != null) {
            return;
        }
        cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(REGION_NAME);
    }

    /**
     * Releases cache reference.
     *
     * @throws OXException If clearing cache fails
     */
    public void releaseCache() throws OXException {
        Cache cache = this.cache;
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
            CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                cacheService.freeCache(REGION_NAME);
            }
        } finally {
            this.cache = null;
        }
    }

    /**
     * Fetches the appropriate lock.
     *
     * @param key The lock's key
     * @return The appropriate lock
     */
    private static ReadWriteLock getLock(CacheKey key) {
        ReadWriteLock l = contextLocks.get(key);
        if (l == null) {
            ReentrantReadWriteLock tmp = new ReentrantReadWriteLock();
            l = contextLocks.putIfAbsent(key, tmp);
            if (null == l) {
                l = tmp;
            }
        }
        return l;
    }

    /**
     * Gets the singleton instance.
     *
     * @return The singleton instance
     * @throws OXException If instance initialization failed
     */
    public static MailMessageCache getInstance() throws OXException {
        MailMessageCache tmp = singleton;
        if (null == tmp) {
            synchronized (MailMessageCache.class) {
                tmp = singleton;
                if (null == tmp) {
                    tmp = singleton = new MailMessageCache();
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the singleton instance.
     */
    public static void releaseInstance() {
        if (null != singleton) {
            synchronized (MailMessageCache.class) {
                if (null != singleton) {
                    singleton = null;
                }
            }
        }
    }

    /**
     * Updates cached message
     *
     * @param uids The messages' identifiers; pass <code>null</code>  update all cached message of given folder
     * @param accountId The account identifier
     * @param fullName The full name
     * @param userId The user identifier
     * @param cid The context identifier
     * @param changedFields The changed fields
     * @param newValues The new values
     */
    public void updateCachedMessages(String[] uids, int accountId, String fullName, int userId, int cid, MailListField[] changedFields, Object[] newValues) {
        Cache cache = this.cache;
        if (null == cache) {
            return;
        }
        if (null == uids) {
            updateCachedMessages(accountId, fullName, userId, cid, changedFields, newValues);
            return;
        }
        CacheKey mapKey = getMapKey(userId, cid);
        Lock writeLock = getLock(mapKey).writeLock();
        writeLock.lock();
        try {
             @SuppressWarnings("unchecked")
            DoubleKeyMap<CacheKey, String, MailMessage> map = (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (map == null) {
                return;
            }
            MailMessage[] mails = map.getValues(getEntryKey(accountId, fullName), uids);
            if ((mails != null) && (mails.length > 0)) {
                MailFieldUpdater[] updaters = createMailFieldUpdater(changedFields);
                for (MailMessage mail : mails) {
                    if (mail != null) {
                        for (int i = 0; i < updaters.length; i++) {
                            updaters[i].updateField(mail, newValues[i]);
                        }
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates cached message
     *
     * @param accountId The account identifier
     * @param fullName The full name
     * @param userId The user identifier
     * @param cid The context identifier
     * @param changedFields The changed fields
     * @param newValues The new values
     */
    public void updateCachedMessages(int accountId, String fullName, int userId, int cid, MailListField[] changedFields, Object[] newValues) {
        Cache cache = this.cache;
        if (null == cache) {
            return;
        }
        CacheKey mapKey = getMapKey(userId, cid);
        Lock writeLock = getLock(mapKey).writeLock();
        writeLock.lock();
        try {
             @SuppressWarnings("unchecked")
            DoubleKeyMap<CacheKey, String, MailMessage> map = (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (map == null) {
                return;
            }
            MailMessage[] mails = map.getValues(getEntryKey(accountId, fullName));
            if ((mails != null) && (mails.length > 0)) {
                MailFieldUpdater[] updaters = createMailFieldUpdater(changedFields);
                for (MailMessage mail : mails) {
                    if (mail != null) {
                        for (int i = 0; i < updaters.length; i++) {
                            updaters[i].updateField(mail, newValues[i]);
                        }
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Detects if cache holds messages belonging to given user.
     *
     * @param userId The user identifier
     * @param cid The context identifier
     * @return <code>true</code> if messages are present; otherwise <code>false</code>
     */
    public boolean containsUserMessages(int userId, int cid) {
        Cache cache = this.cache;
        if (null == cache) {
            return false;
        }
        return cache.get(getMapKey(userId, cid)) != null;
    }

    /**
     * Detects if cache holds messages belonging to a certain folder.
     *
     * @param accountId The account identifier
     * @param fullName The folder full name
     * @param userId The user identifier
     * @param cid The context identifier
     * @return <code>true</code> if cache holds messages belonging to a certain folder; otherwise <code>false</code>
     */
    public boolean containsFolderMessages(int accountId, String fullName, int userId, int cid) {
        Cache cache = this.cache;
        if (null == cache) {
            return false;
        }
        CacheKey mapKey = getMapKey(userId, cid);
        @SuppressWarnings("unchecked") DoubleKeyMap<CacheKey, String, MailMessage> map =
            (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
        if (map == null) {
            return false;
        }
        return map.containsKey(getEntryKey(accountId, fullName));
    }

    /**
     * Removes the messages cached for a user.
     *
     * @param userId The user identifier
     * @param cid The context identifier
     * @throws OXException
     */
    public void removeUserMessages(int userId, int cid) throws OXException {
        Cache cache = this.cache;
        if (null == cache) {
            return;
        }
        CacheKey mapKey = getMapKey(userId, cid);
        if (cache.get(mapKey) == null) {
            return;
        }
        Lock writeLock = getLock(mapKey).writeLock();
        writeLock.lock();
        try {
            cache.remove(mapKey);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes cached messages belonging to a certain folder.
     *
     * @param accountId The account identifier
     * @param fullName The folder full name
     * @param userId The user identifier
     * @param cid The context identifier
     */
    public void removeFolderMessages(int accountId, String fullName, int userId, int cid) {
        Cache cache = this.cache;
        if (null == cache) {
            return;
        }
        CacheKey mapKey = getMapKey(userId, cid);
        Lock writeLock = getLock(mapKey).writeLock();
        writeLock.lock();
        try {
            @SuppressWarnings("unchecked") DoubleKeyMap<CacheKey, String, MailMessage> map =
                (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (map == null) {
                return;
            }
            map.removeValues(getEntryKey(accountId, fullName));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes the messages appearing in given UIDs belonging to a certain folder.
     *
     * @param uids The mail identifiers; pass <code>null</code> to remove all associated with folder
     * @param accountId The account identifier
     * @param fullName The folder full name
     * @param userId The user identifier
     * @param cid The context identifier
     */
    public void removeMessages(String[] uids, int accountId, String fullName, int userId, int cid) {
        Cache cache = this.cache;
        if (null == cache) {
            return;
        }
        CacheKey mapKey = getMapKey(userId, cid);
        Lock writeLock = getLock(mapKey).writeLock();
        writeLock.lock();
        try {
            @SuppressWarnings("unchecked") DoubleKeyMap<CacheKey, String, MailMessage> map =
                (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (map == null) {
                return;
            }
            if (null == uids) {
                map.removeValues(getEntryKey(accountId, fullName));
            } else {
                map.removeValues(getEntryKey(accountId, fullName), uids);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Gets the corresponding messages from cache. If a cache entry could not be found <code>null</code> is returned to force a reload from
     * mail server.
     *
     * @param uids The UIDs
     * @param accountId The account identifier
     * @param fullName The folder full name
     * @param userId The user identifier
     * @param cid The context identifier
     * @return An array of {@link MailMessage} containing the fetched messages or <code>null</code>
     */
    public MailMessage[] getMessages(String[] uids, int accountId, String fullName, int userId, int cid) {
        Cache cache = this.cache;
        if (null == cache) {
            return null;
        }
        CacheKey mapKey = getMapKey(userId, cid);
        Lock readLock = getLock(mapKey).readLock();
        readLock.lock();
        try {
            @SuppressWarnings("unchecked")
            DoubleKeyMap<CacheKey, String, MailMessage> map = (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (null == map) {
                return null;
            }
            CacheKey entryKey = getEntryKey(accountId, fullName);
            if (!map.containsKey(entryKey)) {
                return null;
            }
            MailMessage[] retval = new MailMessage[uids.length];
            for (int i = 0; i < retval.length; i++) {
                MailMessage fromCache = map.getValue(entryKey, uids[i]);
                if (null == fromCache) {
                    // Not all desired messages can be served from cache
                    return null;
                }
                retval[i] = fromCache;
            }
            return retval;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Puts given messages into cache.
     *
     * @param accountId The account identifier
     * @param mails The messages to cache
     * @param userId The user identifier
     * @param cid The context identifier
     * @throws OXException If cache put fails
     */
    public void putMessages(int accountId, MailMessage[] mails, int userId, int cid) throws OXException {
        Cache cache = this.cache;
        if (null == cache) {
            return;
        }
        if ((mails == null) || (mails.length == 0)) {
            return;
        }
        CacheKey mapKey = getMapKey(userId, cid);
        Lock writeLock = getLock(mapKey).writeLock();
        writeLock.lock();
        try {
            @SuppressWarnings("unchecked") DoubleKeyMap<CacheKey, String, MailMessage> map =
                (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (null == map) {
                map = new DoubleKeyMap<CacheKey, String, MailMessage>(MailMessage.class);
                cache.put(mapKey, map, false);
            }
            for (MailMessage mail : mails) {
                if (mail != null) {
                    mail.prepareForCaching();
                    /*
                     * TODO: Put cloned version into cache ???
                     */
                    map.putValue(getEntryKey(accountId, mail.getFolder()), mail.getMailId(), mail);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    private CacheKey getMapKey(int userId, int cid) {
        Cache cache = this.cache;
        return cache.newCacheKey(cid, userId);
    }

    private CacheKey getEntryKey(int accountId, String fullName) {
        Cache cache = this.cache;
        return cache.newCacheKey(accountId, fullName);
    }

}
