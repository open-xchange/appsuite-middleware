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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.cache.OXCachingException;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.mail.MailException;
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

    /**
     * Constant for the {@link SuppressWarnings} annotation to suppress unchecked type conversion.
     */
    private static final String ANNOT_UNCHECKED = "unchecked";

    private static final class DoubleKeyMap<K1, K2, V> implements Serializable {

        private static final long serialVersionUID = 4691428774420782654L;

        private final transient Map<K1, Map<K2, V>> map;

        private final transient Class<V> clazz;

        /**
         * Constructor.
         * 
         * @param clazz The class of the values
         */
        public DoubleKeyMap(final Class<V> clazz) {
            map = new ConcurrentHashMap<K1, Map<K2, V>>();
            this.clazz = clazz;
        }

        /**
         * Detects if first key is contained in this map.
         * 
         * @param k1 The first key
         * @return <code>true</code> if first key is contained in this map; otherwise <code>false</code>
         */
        public boolean containsKey(final K1 k1) {
            return map.containsKey(k1);
        }

        /**
         * Detects if key pair is contained in this map.
         * 
         * @param k1 The first key
         * @param k2 The second key
         * @return <code>true</code> if key pair is contained in this map; otherwise <code>false</code>
         */
        public boolean containsKeypair(final K1 k1, final K2 k2) {
            final Map<K2, V> innerMap = map.get(k1);
            if (null == innerMap) {
                return false;
            }
            return innerMap.containsKey(k2);
        }

        /**
         * Gets all values associated with given first key.
         * 
         * @param k1 The first key
         * @return All values associated with given first key or <code>null</code> if none found
         */
        @SuppressWarnings(ANNOT_UNCHECKED)
        public V[] getValues(final K1 k1) {
            final Map<K2, V> innerMap = map.get(k1);
            if (innerMap == null) {
                return null;
            }
            return innerMap.values().toArray((V[]) Array.newInstance(clazz, innerMap.size()));
        }

        /**
         * Gets the values associated with given first key and given second keys.
         * 
         * @param k1 The first key
         * @param keys The second keys
         * @return The values associated with given first key and given second keys
         */
        @SuppressWarnings(ANNOT_UNCHECKED)
        public V[] getValues(final K1 k1, final K2[] keys) {
            final Map<K2, V> innerMap = map.get(k1);
            if (innerMap == null) {
                return null;
            }
            final List<V> tmp = new ArrayList<V>(keys.length);
            for (int i = 0; i < keys.length; i++) {
                tmp.add(innerMap.get(keys[i]));
            }
            return tmp.toArray((V[]) Array.newInstance(clazz, tmp.size()));
        }

        /**
         * Gets the single value associated with given key pair.
         * 
         * @param k1 The first key
         * @param k2 The second key
         * @return The single value associated with given key pair or <code>null</code> if not present
         */
        public V getValue(final K1 k1, final K2 k2) {
            final Map<K2, V> innerMap = map.get(k1);
            if (null == innerMap) {
                return null;
            }
            return innerMap.get(k2);
        }

        /**
         * Puts given values into map.
         * 
         * @param k1 The first key
         * @param keys The second keys
         * @param values The values to insert
         */
        public void putValues(final K1 k1, final K2[] keys, final V[] values) {
            if ((k1 == null) || (keys == null) || (values == null)) {
                throw new IllegalArgumentException("Argument must not be null");
            }
            Map<K2, V> innerMap = this.map.get(k1);
            if (innerMap == null) {
                innerMap = new ConcurrentHashMap<K2, V>(values.length);
                this.map.put(k1, innerMap);
            }
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    innerMap.put(keys[i], values[i]);
                }
            }
        }

        /**
         * Puts a single value into map.
         * 
         * @param k1 The first key
         * @param k2 The second key
         * @param value The value to insert
         * @return The value formerly bound to given key pair or <code>null</code> if none was bound before
         */
        public V putValue(final K1 k1, final K2 k2, final V value) {
            if ((k1 == null) || (k2 == null) || (value == null)) {
                throw new IllegalArgumentException("Argument must not be null");
            }
            Map<K2, V> innerMap = this.map.get(k1);
            if (innerMap == null) {
                innerMap = new ConcurrentHashMap<K2, V>();
                this.map.put(k1, innerMap);
            }
            return innerMap.put(k2, value);
        }

        /**
         * Removes all values associated with given first key.
         * 
         * @param k1 The first key
         */
        public void removeValues(final K1 k1) {
            map.remove(k1);
        }

        /**
         * Removes the values associated with given first key and is in list of second keys.
         * 
         * @param k1 The first key
         * @param keys The second keys
         */
        public void removeValues(final K1 k1, final K2[] keys) {
            final Map<K2, V> innerMap = map.get(k1);
            if (null == innerMap) {
                return;
            }
            for (int i = 0; i < keys.length; i++) {
                innerMap.remove(keys[i]);
            }
            if (innerMap.isEmpty()) {
                /*
                 * Remove empty inner map
                 */
                map.remove(k1);
            }
        }

        /**
         * Removes the single value associated with given key pair.
         * 
         * @param k1 The first key
         * @param k2 The second key
         * @return The removed value or <code>null</code> if not present
         */
        public V removeValue(final K1 k1, final K2 k2) {
            final Map<K2, V> innerMap = map.get(k1);
            if (null == innerMap) {
                return null;
            }
            final V retval = innerMap.remove(k2);
            if ((retval != null) && innerMap.isEmpty()) {
                /*
                 * Remove empty inner map
                 */
                map.remove(k1);
            }
            return retval;
        }

        /**
         * Checks if no values are bound to given first key.
         * 
         * @param k1 The first key
         * @return <code>true</code> if no values are bound to given first key; otherwise <code>false</code>
         */
        public boolean isEmpty(final K1 k1) {
            final Map<K2, V> innerMap = map.get(k1);
            if (null == innerMap) {
                return true;
            } else if (innerMap.isEmpty()) {
                map.remove(k1);
                return true;
            }
            return false;
        }

        /**
         * Checks if whole map is empty.
         * 
         * @return <code>true</code> if whole map is empty; otherwise <code>false</code>
         */
        public boolean isEmpty() {
            return map.isEmpty();
        }

        /**
         * Clears whole map.
         */
        public void clear() {
            map.clear();
        }
    }

    private static interface MailFieldUpdater {

        public void updateField(MailMessage mail, Object newValue);
    }

    private static final MailFieldUpdater flagsUpdater = new MailFieldUpdater() {

        public void updateField(final MailMessage mail, final Object newValue) {
            int newFlags = mail.getFlags();
            int flags = ((Integer) newValue).intValue();
            final boolean set = flags > 0;
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

        public void updateField(final MailMessage mail, final Object newValue) {
            mail.setColorLabel(((Integer) newValue).intValue());
        }
    };

    private static MailFieldUpdater[] createMailFieldUpdater(final MailListField[] changedFields) {
        final MailFieldUpdater[] updaters = new MailFieldUpdater[changedFields.length];
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

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailMessageCache.class);

    private static final Object[] EMPTY_ARGS = new Object[0];

    static final String REGION_NAME = "MailMessageCache";

    private static final AtomicBoolean initialized = new AtomicBoolean();

    private static final ConcurrentMap<CacheKey, ReadWriteLock> contextLocks = new ConcurrentHashMap<CacheKey, ReadWriteLock>();

    private static MailMessageCache singleton;

    /*-
     * Field members
     */
    private Cache cache;

    /**
     * Singleton instantiation.
     * 
     * @throws OXCachingException If cache instantiation fails
     */
    private MailMessageCache() throws OXCachingException {
        super();
        initCache();
    }

    /**
     * Initializes cache reference.
     * 
     * @throws OXCachingException If initializing the cache reference fails
     */
    public void initCache() throws OXCachingException {
        /*
         * Check for proper started mail cache configuration
         */
        if (!MailCacheConfiguration.getInstance().isStarted()) {
            throw new OXCachingException(new MailException(MailException.Code.INITIALIZATION_PROBLEM));
        }
        if (cache != null) {
            return;
        }
        try {
            cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(REGION_NAME);
        } catch (final CacheException e) {
            LOG.error(e.getMessage(), e);
            throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, REGION_NAME, e.getMessage());
        }
    }

    /**
     * Releases cache reference.
     * 
     * @throws OXCachingException If clearing cache fails
     */
    public void releaseCache() throws OXCachingException {
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
            final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                cacheService.freeCache(REGION_NAME);
            }
        } catch (final CacheException e) {
            throw new OXCachingException(OXCachingException.Code.FAILED_REMOVE, e, e.getMessage());
        } finally {
            cache = null;
        }
    }

    /**
     * Fetches the appropriate lock.
     * 
     * @param key The lock's key
     * @return The appropriate lock
     */
    private static ReadWriteLock getLock(final CacheKey key) {
        ReadWriteLock l = contextLocks.get(key);
        if (l == null) {
            final ReentrantReadWriteLock tmp = new ReentrantReadWriteLock();
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
     * @throws OXCachingException If instance initialization failed
     */
    public static MailMessageCache getInstance() throws OXCachingException {
        if (!initialized.get()) {
            synchronized (initialized) {
                if (null == singleton) {
                    singleton = new MailMessageCache();
                    initialized.set(true);
                }
            }
        }
        return singleton;
    }

    /**
     * Releases the singleton instance.
     */
    public static void releaseInstance() {
        if (initialized.get()) {
            synchronized (initialized) {
                if (null != singleton) {
                    singleton = null;
                    initialized.set(false);
                }
            }
        }
    }

    @SuppressWarnings(ANNOT_UNCHECKED)
    public void updateCachedMessages(final String[] uids, final int accountId, final String fullname, final int userId, final int cid, final MailListField[] changedFields, final Object[] newValues) {
        if (null == cache) {
            return;
        }
        final CacheKey mapKey = getMapKey(userId, cid);
        final Lock writeLock = getLock(mapKey).writeLock();
        writeLock.lock();
        try {
            final DoubleKeyMap<CacheKey, String, MailMessage> map = (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (map == null) {
                return;
            }
            final MailMessage[] mails = map.getValues(getEntryKey(accountId, fullname), uids);
            if ((mails != null) && (mails.length > 0)) {
                final MailFieldUpdater[] updaters = createMailFieldUpdater(changedFields);
                for (final MailMessage mail : mails) {
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
     * @param userId The user ID
     * @param cid The context ID
     * @return <code>true</code> if messages are present; otherwise <code>false</code>
     */
    public boolean containsUserMessages(final int userId, final int cid) {
        if (null == cache) {
            return false;
        }
        return cache.get(getMapKey(userId, cid)) != null;
    }

    /**
     * Detects if cache holds messages belonging to a certain folder.
     * 
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param userId The user ID
     * @param cid The context ID
     * @return <code>true</code> if cache holds messages belonging to a certain folder; otherwise <code>false</code>
     */
    @SuppressWarnings(ANNOT_UNCHECKED)
    public boolean containsFolderMessages(final int accountId, final String fullname, final int userId, final int cid) {
        if (null == cache) {
            return false;
        }
        final CacheKey mapKey = getMapKey(userId, cid);
        final DoubleKeyMap<CacheKey, String, MailMessage> map = (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
        if (map == null) {
            return false;
        }
        return map.containsKey(getEntryKey(accountId, fullname));
    }

    /**
     * Removes the messages cached for a user.
     * 
     * @param userId The user ID
     * @param cid The context ID
     * @throws OXCachingException
     */
    public void removeUserMessages(final int userId, final int cid) throws OXCachingException {
        if (null == cache) {
            return;
        }
        try {
            final CacheKey mapKey = getMapKey(userId, cid);
            final Lock writeLock = getLock(mapKey).writeLock();
            writeLock.lock();
            try {
                cache.remove(mapKey);
            } finally {
                writeLock.unlock();
            }
        } catch (final CacheException e) {
            throw new OXCachingException(OXCachingException.Code.FAILED_REMOVE, e, EMPTY_ARGS);
        }
    }

    /**
     * Removes cached messages belonging to a certain folder.
     * 
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param userId The user ID
     * @param cid The context ID
     */
    @SuppressWarnings(ANNOT_UNCHECKED)
    public void removeFolderMessages(final int accountId, final String fullname, final int userId, final int cid) {
        if (null == cache) {
            return;
        }
        final CacheKey mapKey = getMapKey(userId, cid);
        final Lock writeLock = getLock(mapKey).writeLock();
        writeLock.lock();
        try {
            final DoubleKeyMap<CacheKey, String, MailMessage> map = (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (map == null) {
                return;
            }
            map.removeValues(getEntryKey(accountId, fullname));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes the messages appearing in given UIDs belonging to a certain folder.
     * 
     * @param uids The mail IDs
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param userId The user ID
     * @param cid The context ID
     */
    @SuppressWarnings(ANNOT_UNCHECKED)
    public void removeMessages(final String[] uids, final int accountId, final String fullname, final int userId, final int cid) {
        if (null == cache) {
            return;
        }
        final CacheKey mapKey = getMapKey(userId, cid);
        final Lock writeLock = getLock(mapKey).writeLock();
        writeLock.lock();
        try {
            final DoubleKeyMap<CacheKey, String, MailMessage> map = (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (map == null) {
                return;
            }
            map.removeValues(getEntryKey(accountId, fullname), uids);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Gets the corresponding messages from cache. If a cache entry could not be found <code>null</code> is returned to force a reload from
     * mail server.
     * 
     * @param uids The UIDs
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param userId The user ID
     * @param cid The context ID
     * @return An array of {@link MailMessage} containing the fetched messages or <code>null</code>
     */
    @SuppressWarnings(ANNOT_UNCHECKED)
    public MailMessage[] getMessages(final String[] uids, final int accountId, final String fullname, final int userId, final int cid) {
        if (null == cache) {
            return null;
        }
        final CacheKey mapKey = getMapKey(userId, cid);
        final Lock readLock = getLock(mapKey).readLock();
        readLock.lock();
        try {
            final DoubleKeyMap<CacheKey, String, MailMessage> map = (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
            if (null == map) {
                return null;
            }
            final CacheKey entryKey = getEntryKey(accountId, fullname);
            if (!map.containsKey(entryKey)) {
                return null;
            }
            final MailMessage[] retval = new MailMessage[uids.length];
            for (int i = 0; i < retval.length; i++) {
                /*
                 * TODO: Return cloned version ???
                 */
                retval[i] = map.getValue(entryKey, uids[i]);
                if (retval[i] == null) {
                    /*
                     * Not all desired messages can be served from cache
                     */
                    return null;
                }
            }
            return retval;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Puts given messages into cache.
     * 
     * @param accountId The account ID
     * @param mails The messages to cache
     * @param userId The user ID
     * @param cid The context ID
     * @throws OXCachingException If cache put fails
     */
    @SuppressWarnings(ANNOT_UNCHECKED)
    public void putMessages(final int accountId, final MailMessage[] mails, final int userId, final int cid) throws OXCachingException {
        if (null == cache) {
            return;
        } else if ((mails == null) || (mails.length == 0)) {
            return;
        }
        try {
            final CacheKey mapKey = getMapKey(userId, cid);
            final Lock writeLock = getLock(mapKey).writeLock();
            writeLock.lock();
            try {
                DoubleKeyMap<CacheKey, String, MailMessage> map = (DoubleKeyMap<CacheKey, String, MailMessage>) cache.get(mapKey);
                if (null == map) {
                    map = new DoubleKeyMap<CacheKey, String, MailMessage>(MailMessage.class);
                    cache.put(mapKey, map);
                }
                for (final MailMessage mail : mails) {
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
        } catch (final CacheException e) {
            throw new OXCachingException(OXCachingException.Code.FAILED_PUT, e, EMPTY_ARGS);
        }
    }

    private CacheKey getMapKey(final int userId, final int cid) {
        return cache.newCacheKey(cid, userId);
    }

    private CacheKey getEntryKey(final int accountId, final String fullname) {
        return cache.newCacheKey(accountId, fullname);
    }

}
