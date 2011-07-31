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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.concurrent.TimeoutListener;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

/**
 * {@link JSONMessageCache} - A JSON message cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONMessageCache {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(JSONMessageCache.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * The cache instance.
     */
    private static JSONMessageCache instance;

    /**
     * Gets the cache instance.
     *
     * @return The cache instance or <code>null</code>
     */
    public static JSONMessageCache getInstance() {
        return instance;
    }

    /**
     * Initializes the cache instance.
     *
     * @return The cache instance
     * @throws OXException If initialization fails
     */
    public static void initInstance() throws OXException {
        if (!JSONMessageCacheConfiguration.getInstance().isEnabled()) {
            return;
        }
        synchronized (JSONMessageCache.class) {
            if (null == instance) {
                instance = new JSONMessageCache();
            }
        }
    }

    /**
     * Releases the cache instance.
     */
    public static void releaseInstance() {
        synchronized (JSONMessageCache.class) {
            if (null != instance) {
                instance.clear();
                instance.superMap.dispose();
                instance = null;
            }
        }
    }

    /*-
     * ####################################### MEMBER STUFF #######################################
     */

    private final TimeoutConcurrentMap<UserKey, TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>>> superMap;

    /**
     * Initializes a new {@link JSONMessageCache}.
     *
     * @throws OXException If initialization fails
     */
    private JSONMessageCache() throws OXException {
        super();
        try {
            // Check every N seconds for timed out user maps
            superMap =
                new TimeoutConcurrentMap<UserKey, TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>>>(
                    JSONMessageCacheConfiguration.getInstance().getShrinkerIntervalUserMap());
            superMap.setDefaultTimeoutListener(new TimeoutListener<TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>>>() {

                @Override
                public void onTimeout(final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> element) {
                    // Perform time out on all entries contained in timed out folder map
                    element.timeoutAll();
                    // Dispose folder map
                    element.dispose();
                }

            });
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }

    /**
     * Associates given JSON mail object with specified account ID, folder fullname and mail ID. If the cache previously contained a mapping
     * for this key, the old value is replaced by the specified value.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param jsonMailObject The JSON mail object
     * @param session The session providing user and context information
     * @throws OXException If put fails
     */
    public void put(final int accountId, final String fullname, final String id, final FutureTask<JSONObject> jsonMailObject, final Session session) throws OXException {
        put(accountId, fullname, id, jsonMailObject, session.getUserId(), session.getContextId());
    }

    /**
     * Associates given JSON mail object with specified account ID, folder fullname and mail ID. If the cache previously contained a mapping
     * for this key, the old value is replaced by the specified value.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param jsonMailObject The JSON mail object
     * @param userId The user ID
     * @param cid The context ID
     * @throws OXException If put fails
     */
    public void put(final int accountId, final String fullname, final String id, final FutureTask<JSONObject> jsonMailObject, final int userId, final int cid) throws OXException {
        final JSONMessageCacheConfiguration cacheConfiguration = JSONMessageCacheConfiguration.getInstance();
        final UserKey userKey = new UserKey(userId, cid);
        TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap = superMap.get(userKey);
        if (null == timeoutConcurrentMap) {
            final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> newMap;
            try {
                // Check every N seconds for timed out folder maps
                newMap =
                    new TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>>(
                        cacheConfiguration.getShrinkerIntervalFolderMap());
                if (DEBUG) {
                    newMap.setDefaultTimeoutListener(new FolderRemovalLogger(LOG, accountId, fullname, userId, cid));
                }
            } catch (final OXException e) {
                throw new OXException(e);
            }
            // A user map is valid for one hour
            timeoutConcurrentMap = superMap.putIfAbsent(userKey, newMap, cacheConfiguration.getTTLUserMap());
            if (null == timeoutConcurrentMap) {
                timeoutConcurrentMap = newMap;
            }
        }
        /*
         * Folder map
         */
        final FolderKey key = new FolderKey(accountId, fullname);
        /*
         * Get JSON object map
         */
        ConcurrentMap<String, FutureTask<JSONObject>> objectMap = timeoutConcurrentMap.get(key);
        if (null == objectMap) {
            final ConcurrentMap<String, FutureTask<JSONObject>> newMap = new ConcurrentHashMap<String, FutureTask<JSONObject>>();
            // A folder map is valid for 5 minutes
            objectMap = timeoutConcurrentMap.putIfAbsent(key, newMap, cacheConfiguration.getTTLFolderMap());
            if (null == objectMap) {
                objectMap = newMap;
            }
        }
        /*
         * Put JSON object
         */
        objectMap.put(id, jsonMailObject);
    }

    /**
     * Checks if a JSON mail object is associated with specified account ID, folder fullname and mail ID.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param session The session providing user and context information
     * @return <code>true</code> If this cache contains a mapping for given key; otherwise <code>false</code>
     */
    public boolean containsKey(final int accountId, final String fullname, final String id, final Session session) {
        return containsKey(accountId, fullname, id, session.getUserId(), session.getContextId());
    }

    /**
     * Checks if a JSON mail object is associated with specified account ID, folder fullname and mail ID.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param userId The user ID
     * @param cid The context ID
     * @return <code>true</code> If this cache contains a mapping for given key; otherwise <code>false</code>
     */
    public boolean containsKey(final int accountId, final String fullname, final String id, final int userId, final int cid) {
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.get(new UserKey(userId, cid));
        if (null == timeoutConcurrentMap) {
            return false;
        }
        final FolderKey key = new FolderKey(accountId, fullname);
        /*
         * Get JSON object map
         */
        final ConcurrentMap<String, FutureTask<JSONObject>> objectMap = timeoutConcurrentMap.get(key);
        if (null == objectMap) {
            return false;
        }
        /*
         * Get from map
         */
        return objectMap.containsKey(id);
    }

    /**
     * Checks if a JSON mail object is associated with specified account ID, folder fullname and mail ID.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param session The session providing user and context information
     * @return <code>true</code> If this cache contains a mapping for given key; otherwise <code>false</code>
     */
    public boolean containsFolder(final int accountId, final String fullname, final Session session) {
        return containsFolder(accountId, fullname, session.getUserId(), session.getContextId());
    }

    /**
     * Checks if a JSON mail object is associated with specified account ID, folder fullname and mail ID.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param userId The user ID
     * @param cid The context ID
     * @return <code>true</code> If this cache contains a mapping for given key; otherwise <code>false</code>
     */
    public boolean containsFolder(final int accountId, final String fullname, final int userId, final int cid) {
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.get(new UserKey(userId, cid));
        if (null == timeoutConcurrentMap) {
            return false;
        }
        return timeoutConcurrentMap.containsKey(new FolderKey(accountId, fullname));
    }

    /**
     * Gets the <b>cloned</b> JSON mail object associated with specified account ID, folder fullname and mail ID.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param session The session providing user and context information
     * @return The <b>cloned</b> JSON mail object or <code>null</code>
     * @throws OXException If JSON mail object cannot be returned
     */
    public JSONObject get(final int accountId, final String fullname, final String id, final Session session) throws OXException {
        return get(accountId, fullname, id, session.getUserId(), session.getContextId());
    }

    /**
     * Gets the <b>cloned</b> JSON mail object associated with specified account ID, folder fullname and mail ID.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param userId The user ID
     * @param cid The context ID
     * @return The <b>cloned</b> JSON mail object or <code>null</code>
     * @throws OXException If JSON mail object cannot be returned
     */
    public JSONObject get(final int accountId, final String fullname, final String id, final int userId, final int cid) throws OXException {
        return getInternal(accountId, fullname, id, false, userId, cid);
    }

    /**
     * Gets the <b>cloned</b> JSON mail object associated with specified account ID, folder fullname and mail ID.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param remove <code>true</code> to remove on presence; otherwise <code>false</code>
     * @param userId The user ID
     * @param cid The context ID
     * @return The <b>cloned</b> JSON mail object or <code>null</code>
     * @throws OXException If JSON mail object cannot be returned
     */
    private JSONObject getInternal(final int accountId, final String fullname, final String id, final boolean remove, final int userId, final int cid) throws OXException {
        final UserKey userKey = new UserKey(userId, cid);
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap = superMap.get(userKey);
        if (null == timeoutConcurrentMap) {
            return null;
        }
        final FolderKey key = new FolderKey(accountId, fullname);
        /*
         * Get JSON object map
         */
        final ConcurrentMap<String, FutureTask<JSONObject>> objectMap = timeoutConcurrentMap.get(key);
        if (null == objectMap) {
            return null;
        }
        /*
         * Get/Remove from map
         */
        final FutureTask<JSONObject> future = remove ? objectMap.remove(id) : objectMap.get(id);
        if (null == future) {
            if (remove && objectMap.isEmpty()) {
                timeoutConcurrentMap.remove(key);
                if (timeoutConcurrentMap.isEmpty()) {
                    superMap.remove(userKey);
                    timeoutConcurrentMap.dispose();
                }
            }
            return null;
        }
        /*-
         * A future is already present; meaning associated message's JSON representation is at least planned for being put into cache, but
         * does not guarantee being already present.
         *
         * Therefore:
         * 1. Invoke Future.get() with a time out and return if return value is present within time out range
         * 2. Otherwise catch possible TimeoutException and handle it by performing Future's task with calling thread
         */
        JSONObject retval = null;
        try {
            final int waitTimeMillis = JSONMessageCacheConfiguration.getInstance().getMaxWaitTimeMillis();
            try {
                retval = remove ? getFromFuture(future, waitTimeMillis) : clone(getFromFuture(future, waitTimeMillis));
            } catch (final TimeoutException e) {
                // Not yet available
                if (DEBUG) {
                    final StringBuilder builder = new StringBuilder(64);
                    builder.append("Wait time of ");
                    builder.append(waitTimeMillis);
                    builder.append("millis elapsed. Fetch message with calling thread.");
                    LOG.debug(builder.toString());

                    final long s = System.currentTimeMillis();
                    future.run();

                    builder.setLength(0);
                    builder.append("Fetched message with calling thread in ");
                    builder.append((System.currentTimeMillis() - s));
                    builder.append("msec");
                    LOG.debug(builder.toString());
                } else {
                    future.run();
                }
                retval = remove ? getFromFuture(future) : clone(getFromFuture(future));
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
        if (remove && objectMap.isEmpty()) {
            timeoutConcurrentMap.remove(key);
            if (timeoutConcurrentMap.isEmpty()) {
                superMap.remove(userKey);
                timeoutConcurrentMap.dispose();
            }
        }
        return retval;
    }

    /**
     * Switch the \Seen flag for specified mails' JSON representations. Decrements unread message counter for each message.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param id The mail ID
     * @param seen <code>true</code> to set \Seen flag; otherwise <code>false</code>
     * @param unread The unread count
     * @param session The session providing user and context information
     * @throws OXException If an error occurs
     */
    public void switchSeenFlag(final int accountId, final String fullname, final String[] ids, final boolean seen, final int unread, final Session session) throws OXException {
        switchSeenFlag(accountId, fullname, ids, seen, unread, session.getUserId(), session.getContextId());
    }

    /**
     * Switch the \Seen flag for specified mails' JSON representations. Decrements unread message counter for each message.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param id The mail ID
     * @param seen <code>true</code> to set \Seen flag; otherwise <code>false</code>
     * @param unread The unread count for specified folder
     * @param userId The user ID
     * @param cid The context ID
     * @throws OXException If an error occurs
     */
    public void switchSeenFlag(final int accountId, final String fullname, final String[] ids, final boolean seen, final int unread, final int userId, final int cid) throws OXException {
        if (null == ids) {
            switchSeenFlag(accountId, fullname, seen, unread, userId, cid);
            return;
        }
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.get(new UserKey(userId, cid));
        if (null == timeoutConcurrentMap) {
            return;
        }
        final FolderKey key = new FolderKey(accountId, fullname);
        /*
         * Get JSON object map
         */
        final ConcurrentMap<String, FutureTask<JSONObject>> objectMap = timeoutConcurrentMap.get(key);
        if (null == objectMap) {
            return;
        }
        /*
         * Get from map
         */
        final List<FutureTask<JSONObject>> futures = new ArrayList<FutureTask<JSONObject>>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            final FutureTask<JSONObject> future = objectMap.get(ids[i]);
            if (null != future) {
                futures.add(future);
            }
        }
        /*
         * Iterate
         */
        try {
            /*
             * Switch seen flag in messages
             */
            final int waitTimeMillis = JSONMessageCacheConfiguration.getInstance().getMaxWaitTimeMillis();
            final String flagsKey = MailJSONField.FLAGS.getKey();
            for (final FutureTask<JSONObject> ft : futures) {
                try {
                    final JSONObject jsonObject = getFromFuture(ft, waitTimeMillis);
                    if (null != jsonObject) {
                        final int flags = jsonObject.optInt(flagsKey);
                        jsonObject.put(flagsKey, seen ? (flags | MailMessage.FLAG_SEEN) : (flags & ~MailMessage.FLAG_SEEN));
                    }
                } catch (final TimeoutException e) {
                    // Not yet available
                }
            }
            /*
             * Apply new unread count to folder's cached JSON messages
             */
            final String unreadKey = MailJSONField.UNREAD.getKey();
            for (final FutureTask<JSONObject> ft : objectMap.values()) {
                try {
                    final JSONObject jsonObject = getFromFuture(ft, waitTimeMillis);
                    if (null != jsonObject) {
                        jsonObject.put(unreadKey, unread);
                    }
                } catch (final TimeoutException e) {
                    // Not yet available
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Switch the \Seen flag for specified mails' JSON representations. Decrements unread message counter for each message.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param seen <code>true</code> to set \Seen flag; otherwise <code>false</code>
     * @param unread The unread count
     * @param session The session providing user and context information
     * @throws OXException If an error occurs
     */
    public void switchSeenFlag(final int accountId, final String fullname, final boolean seen, final int unread, final Session session) throws OXException {
        switchSeenFlag(accountId, fullname, seen, unread, session.getUserId(), session.getContextId());
    }

    /**
     * Switch the \Seen flag for specified mails' JSON representations. Decrements unread message counter for each message.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param seen <code>true</code> to set \Seen flag; otherwise <code>false</code>
     * @param unread The unread count for specified folder
     * @param userId The user ID
     * @param cid The context ID
     * @throws OXException If an error occurs
     */
    public void switchSeenFlag(final int accountId, final String fullname, final boolean seen, final int unread, final int userId, final int cid) throws OXException {
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.get(new UserKey(userId, cid));
        if (null == timeoutConcurrentMap) {
            return;
        }
        final FolderKey key = new FolderKey(accountId, fullname);
        /*
         * Get JSON object map
         */
        final ConcurrentMap<String, FutureTask<JSONObject>> objectMap = timeoutConcurrentMap.get(key);
        if (null == objectMap) {
            return;
        }
        /*
         * Iterate
         */
        try {
            /*
             * Switch seen flag in messages
             */
            final int waitTimeMillis = JSONMessageCacheConfiguration.getInstance().getMaxWaitTimeMillis();
            final String flagsKey = MailJSONField.FLAGS.getKey();
            for (final FutureTask<JSONObject> ft : objectMap.values()) {
                try {
                    final JSONObject jsonObject = getFromFuture(ft, waitTimeMillis);
                    if (null != jsonObject) {
                        final int flags = jsonObject.optInt(flagsKey);
                        jsonObject.put(flagsKey, seen ? (flags | MailMessage.FLAG_SEEN) : (flags & ~MailMessage.FLAG_SEEN));
                    }
                } catch (final TimeoutException e) {
                    // Not yet available
                }
            }
            /*
             * Apply new unread count to folder's cached JSON messages
             */
            final String unreadKey = MailJSONField.UNREAD.getKey();
            for (final FutureTask<JSONObject> ft : objectMap.values()) {
                try {
                    final JSONObject jsonObject = getFromFuture(ft, waitTimeMillis);
                    if (null != jsonObject) {
                        jsonObject.put(unreadKey, unread);
                    }
                } catch (final TimeoutException e) {
                    // Not yet available
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Updates flags for specified mails' JSON representations.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param id The mail ID
     * @param flags The flags bit mask
     * @param set <code>true</code> to set; otherwise <code>false</code>
     * @param session The session providing user and context information
     * @throws OXException If an error occurs
     */
    public void updateFlags(final int accountId, final String fullname, final String[] ids, final int newFlags, final boolean set, final Session session) throws OXException {
        updateFlags(accountId, fullname, ids, newFlags, set, session.getUserId(), session.getContextId());
    }

    /**
     * Updates flags for specified mails' JSON representations.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param id The mail ID
     * @param flags The flags bit mask
     * @param set <code>true</code> to set; otherwise <code>false</code>
     * @param userId The user ID
     * @param cid The context ID
     * @throws OXException If an error occurs
     */
    public void updateFlags(final int accountId, final String fullname, final String[] ids, final int newFlags, final boolean set, final int userId, final int cid) throws OXException {
        if (null == ids) {
            updateFlags(accountId, fullname, newFlags, set, userId, cid);
            return;
        }
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.get(new UserKey(userId, cid));
        if (null == timeoutConcurrentMap) {
            return;
        }
        final FolderKey key = new FolderKey(accountId, fullname);
        /*
         * Get JSON object map
         */
        final ConcurrentMap<String, FutureTask<JSONObject>> objectMap = timeoutConcurrentMap.get(key);
        if (null == objectMap) {
            return;
        }
        /*
         * Get from map
         */
        final List<FutureTask<JSONObject>> futures = new ArrayList<FutureTask<JSONObject>>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            final FutureTask<JSONObject> future = objectMap.get(ids[i]);
            if (null != future) {
                futures.add(future);
            }
        }
        /*
         * Iterate
         */
        try {
            /*
             * Switch seen flag in messages
             */
            final String flagsKey = MailJSONField.FLAGS.getKey();
            final int waitTimeMillis = JSONMessageCacheConfiguration.getInstance().getMaxWaitTimeMillis();
            for (final FutureTask<JSONObject> ft : futures) {
                try {
                    final JSONObject jsonObject = getFromFuture(ft, waitTimeMillis);
                    final int flags = jsonObject.optInt(flagsKey);
                    jsonObject.put(flagsKey, set ? (flags | newFlags) : (flags & ~newFlags));
                } catch (final TimeoutException e) {
                    // Not yet available
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Updates flags for specified mails' JSON representations.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param flags The flags bit mask
     * @param set <code>true</code> to set; otherwise <code>false</code>
     * @param session The session providing user and context information
     * @throws OXException If an error occurs
     */
    public void updateFlags(final int accountId, final String fullname, final int newFlags, final boolean set, final Session session) throws OXException {
        updateFlags(accountId, fullname, newFlags, set, session.getUserId(), session.getContextId());
    }

    /**
     * Updates flags for specified mails' JSON representations.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param flags The flags bit mask
     * @param set <code>true</code> to set; otherwise <code>false</code>
     * @param userId The user ID
     * @param cid The context ID
     * @throws OXException If an error occurs
     */
    public void updateFlags(final int accountId, final String fullname, final int newFlags, final boolean set, final int userId, final int cid) throws OXException {
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.get(new UserKey(userId, cid));
        if (null == timeoutConcurrentMap) {
            return;
        }
        final FolderKey key = new FolderKey(accountId, fullname);
        /*
         * Get JSON object map
         */
        final ConcurrentMap<String, FutureTask<JSONObject>> objectMap = timeoutConcurrentMap.get(key);
        if (null == objectMap) {
            return;
        }
        /*
         * Get from map
         */
        /*
         * Iterate
         */
        try {
            /*
             * Switch seen flag in messages
             */
            final String flagsKey = MailJSONField.FLAGS.getKey();
            final int waitTimeMillis = JSONMessageCacheConfiguration.getInstance().getMaxWaitTimeMillis();
            for (final FutureTask<JSONObject> ft : objectMap.values()) {
                try {
                    final JSONObject jsonObject = getFromFuture(ft, waitTimeMillis);
                    final int flags = jsonObject.optInt(flagsKey);
                    jsonObject.put(flagsKey, set ? (flags | newFlags) : (flags & ~newFlags));
                } catch (final TimeoutException e) {
                    // Not yet available
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Updates the color flag for specified mails' JSON representations.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param ids The mail IDs
     * @param colorFlag The color flag to set
     * @param session The session providing user and context information
     * @throws OXException If an error occurs
     */
    public void updateColorFlag(final int accountId, final String fullname, final String[] ids, final int colorFlag, final Session session) throws OXException {
        updateColorFlag(accountId, fullname, ids, colorFlag, session.getUserId(), session.getContextId());
    }

    /**
     * Updates the color flag for specified mails' JSON representations.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param ids The mail IDs
     * @param colorFlag The color flag to set
     * @param userId The user ID
     * @param cid The context ID
     * @throws OXException If an error occurs
     */
    public void updateColorFlag(final int accountId, final String fullname, final String[] ids, final int colorFlag, final int userId, final int cid) throws OXException {
        if (null == ids) {
            updateColorFlag(accountId, fullname, colorFlag, userId, cid);
            return;
        }
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.get(new UserKey(userId, cid));
        if (null == timeoutConcurrentMap) {
            return;
        }
        final FolderKey key = new FolderKey(accountId, fullname);
        /*
         * Get JSON object map
         */
        final ConcurrentMap<String, FutureTask<JSONObject>> objectMap = timeoutConcurrentMap.get(key);
        if (null == objectMap) {
            return;
        }
        /*
         * Get from map
         */
        final List<FutureTask<JSONObject>> futures = new ArrayList<FutureTask<JSONObject>>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            final FutureTask<JSONObject> future = objectMap.get(ids[i]);
            if (null != future) {
                futures.add(future);
            }
        }
        /*
         * Iterate
         */
        try {
            final String clKey = MailJSONField.COLOR_LABEL.getKey();
            final int waitTimeMillis = JSONMessageCacheConfiguration.getInstance().getMaxWaitTimeMillis();
            for (final FutureTask<JSONObject> ft : futures) {
                /*
                 * Update color flag in message
                 */
                try {
                    final JSONObject jsonObject = getFromFuture(ft, waitTimeMillis);
                    jsonObject.put(clKey, colorFlag);
                } catch (final TimeoutException e) {
                    // Not yet available
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Updates the color flag for specified mails' JSON representations.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param colorFlag The color flag to set
     * @param session The session providing user and context information
     * @throws OXException If an error occurs
     */
    public void updateColorFlag(final int accountId, final String fullname, final int colorFlag, final Session session) throws OXException {
        updateColorFlag(accountId, fullname, colorFlag, session.getUserId(), session.getContextId());
    }

    /**
     * Updates the color flag for specified mails' JSON representations.
     *
     * @param accountId The account ID
     * @param fullname The fullname
     * @param colorFlag The color flag to set
     * @param userId The user ID
     * @param cid The context ID
     * @throws OXException If an error occurs
     */
    public void updateColorFlag(final int accountId, final String fullname, final int colorFlag, final int userId, final int cid) throws OXException {
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.get(new UserKey(userId, cid));
        if (null == timeoutConcurrentMap) {
            return;
        }
        final FolderKey key = new FolderKey(accountId, fullname);
        /*
         * Get JSON object map
         */
        final ConcurrentMap<String, FutureTask<JSONObject>> objectMap = timeoutConcurrentMap.get(key);
        if (null == objectMap) {
            return;
        }
        /*
         * Get from map
         */
        try {
            final String clKey = MailJSONField.COLOR_LABEL.getKey();
            final int waitTimeMillis = JSONMessageCacheConfiguration.getInstance().getMaxWaitTimeMillis();
            for (final FutureTask<JSONObject> ft : objectMap.values()) {
                /*
                 * Update color flag in message
                 */
                try {
                    final JSONObject jsonObject = getFromFuture(ft, waitTimeMillis);
                    jsonObject.put(clKey, colorFlag);
                } catch (final TimeoutException e) {
                    // Not yet available
                }
            }
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Removes the JSON mail object associated with specified account ID, folder fullname and mail ID.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param session The session providing user and context information
     * @return The removed JSON mail object or <code>null</code>
     * @throws OXException If JSON mail object cannot be removed
     */
    public JSONObject remove(final int accountId, final String fullname, final String id, final Session session) throws OXException {
        return remove(accountId, fullname, id, session.getUserId(), session.getContextId());
    }

    /**
     * Removes the JSON mail object associated with specified account ID, folder fullname and mail ID.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param id The mail ID
     * @param userId The user ID
     * @param cid The context ID
     * @return The removed JSON mail object or <code>null</code>
     * @throws OXException If JSON mail object cannot be removed
     */
    public JSONObject remove(final int accountId, final String fullname, final String id, final int userId, final int cid) throws OXException {
        return getInternal(accountId, fullname, id, true, userId, cid);
    }

    /**
     * Removes all JSON mail objects associated with specified account ID and folder fullname.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param session The session providing user and context information
     */
    public void removeFolder(final int accountId, final String fullname, final Session session) {
        removeFolder(accountId, fullname, session.getUserId(), session.getContextId());
    }

    /**
     * Removes all JSON mail objects associated with specified account ID and folder fullname.
     *
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @param userId The user ID
     * @param cid The context ID
     */
    public void removeFolder(final int accountId, final String fullname, final int userId, final int cid) {
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.get(new UserKey(userId, cid));
        if (null == timeoutConcurrentMap) {
            return;
        }
        timeoutConcurrentMap.timeout(new FolderKey(accountId, fullname));
        if (timeoutConcurrentMap.isEmpty()) {
            superMap.remove(new UserKey(userId, cid));
            timeoutConcurrentMap.dispose();
        }
    }

    /**
     * Removes all JSON mail objects except the ones associated with specified account ID and folder fullname.
     *
     * @param accountId The account ID
     * @param fullname The fullname of the folder whose mails shall be kept
     * @param session The session providing user and context information
     */
    public void removeAllFoldersExcept(final int accountId, final String fullname, final Session session) {
        removeAllFoldersExcept(accountId, fullname, session.getUserId(), session.getContextId());
    }

    /**
     * Removes all JSON mail objects except the ones associated with specified account ID and folder fullname.
     *
     * @param accountId The account ID
     * @param fullname The fullname of the folder whose mails shall be kept
     * @param userId The user ID
     * @param cid The context ID
     */
    public void removeAllFoldersExcept(final int accountId, final String fullname, final int userId, final int cid) {
        final UserKey userKey = new UserKey(userId, cid);
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap = superMap.get(userKey);
        if (null == timeoutConcurrentMap) {
            return;
        }
        final FolderKey exceptKey = new FolderKey(accountId, fullname);
        for (final FolderKey folderKey : timeoutConcurrentMap.keySet()) {
            if (!exceptKey.equals(folderKey)) {
                timeoutConcurrentMap.timeout(folderKey);
            }
        }
        if (timeoutConcurrentMap.isEmpty()) {
            superMap.remove(userKey);
            timeoutConcurrentMap.dispose();
        }
    }

    /**
     * Removes all JSON mail objects associated with specified user ID and context ID.
     *
     * @param session The session providing user and context information
     */
    public void removeUser(final Session session) {
        removeUser(session.getUserId(), session.getContextId());
    }

    /**
     * Removes all JSON mail objects associated with specified user ID and context ID.
     *
     * @param userId The user ID
     * @param cid The context ID
     */
    public void removeUser(final int userId, final int cid) {
        final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
            superMap.remove(new UserKey(userId, cid));
        if (null != timeoutConcurrentMap) {
            timeoutConcurrentMap.timeoutAll();
            timeoutConcurrentMap.dispose();
        }
    }

    /**
     * Clears this cache.
     */
    public void clear() {
        for (final UserKey userKey : superMap.keySet()) {
            final TimeoutConcurrentMap<FolderKey, ConcurrentMap<String, FutureTask<JSONObject>>> timeoutConcurrentMap =
                superMap.get(userKey);
            if (null != timeoutConcurrentMap) {
                timeoutConcurrentMap.timeoutAll();
                timeoutConcurrentMap.dispose();
            }
        }
        superMap.clear();
    }

    /**
     * Invokes {@link Future#get()} in a safe manner. Throwing an appropriate {@link OXException} if invocation fails.
     *
     * @param future The future whose <tt>get()</tt> method is supposed to be invoked
     * @return The result object
     * @throws OXException If invocation fails
     */
    private static <V> V getFromFuture(final FutureTask<V> future) throws OXException {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            if (t instanceof OXException) {
                throw (OXException) t;
            }
            if (t instanceof Exception) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(t, t.getMessage());
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw new IllegalStateException(t);
        }
    }

    /**
     * Invokes {@link Future#get()} in a safe manner. Throwing an appropriate {@link OXException} if invocation fails.
     *
     * @param future The future whose <tt>get()</tt> method is supposed to be invoked
     * @param timeout The timeout millis
     * @return The result object
     * @throws OXException If invocation fails
     * @throws TimeoutException If wait timed out
     */
    private static <V> V getFromFuture(final FutureTask<V> future, final long timeout) throws OXException, TimeoutException {
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            if (t instanceof OXException) {
                throw (OXException) t;
            }
            if (t instanceof Exception) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(t, t.getMessage());
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw new IllegalStateException(t);
        }
    }

    /*-
     * ####################################### KEY CLASSES #########################################
     */

    private static final class FolderKey {

        public final int accountId;

        public final String fullname;

        private final int hash;

        public FolderKey(final int accountId, final String fullname) {
            super();
            this.accountId = accountId;
            this.fullname = fullname;
            hash = hashCode0();
        }

        private int hashCode0() {
            final int prime = 31;
            int result = 1;
            result = prime * result + accountId;
            result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
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
            if (!(obj instanceof FolderKey)) {
                return false;
            }
            final FolderKey other = (FolderKey) obj;
            if (accountId != other.accountId) {
                return false;
            }
            if (fullname == null) {
                if (other.fullname != null) {
                    return false;
                }
            } else if (!fullname.equals(other.fullname)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return new StringBuilder(32).append("accountId=").append(accountId).append(", fullname=").append(fullname).toString();
        }

    } // End of class FolderKey

    private static final class UserKey {

        public final int cid;

        public final int userId;

        private final int hash;

        public UserKey(final int userId, final int cid) {
            super();
            this.cid = cid;
            this.userId = userId;
            hash = hashCode0();
        }

        private int hashCode0() {
            final int prime = 31;
            int result = 1;
            result = prime * result + cid;
            result = prime * result + userId;
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
            if (!(obj instanceof UserKey)) {
                return false;
            }
            final UserKey other = (UserKey) obj;
            if (cid != other.cid) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return new StringBuilder(32).append("userId=").append(userId).append(", cid=").append(cid).toString();
        }

    } // End of class Key

    private static final class FolderRemovalLogger implements TimeoutListener<ConcurrentMap<String, FutureTask<JSONObject>>> {

        private final org.apache.commons.logging.Log logger;

        private final int accountId;

        private final String fullname;

        private final int user;

        private final int cid;

        FolderRemovalLogger(final org.apache.commons.logging.Log logger, final int accountId, final String fullname, final int user, final int cid) {
            super();
            this.logger = logger;
            this.cid = cid;
            this.user = user;
            this.accountId = accountId;
            this.fullname = fullname;
        }

        @Override
        public void onTimeout(final ConcurrentMap<String, FutureTask<JSONObject>> element) {
            final StringBuilder sb = new StringBuilder(64);
            sb.append("Removed ").append(element.size()).append(" messages from JSON message cache belonging to folder ");
            sb.append(fullname).append(" in account ").append(accountId);
            sb.append(" for user ").append(user).append(" in context ").append(cid).append('.');
            logger.debug(sb.toString());
        }

    }

    /**
     * Generates a deep-clone of specified JSONObject instance.
     *
     * @param source The JSONObject instance to clone
     * @return The deep-clone of specified JSONObject instance
     * @throws JSONException If a JSON error occurs
     */
    private static JSONObject clone(final JSONObject source) throws JSONException {
        final JSONObject clone = new JSONObject();
        for (final Entry<String, Object> entry : source.entrySet()) {
            clone.put(entry.getKey(), cloneObject(entry.getValue()));
        }
        return clone;
    }

    /**
     * Generates a deep-clone of specified JSONArray instance.
     *
     * @param source The JSONArray instance to clone
     * @return The deep-clone of specified JSONArray instance
     * @throws JSONException If a JSON error occurs
     */
    private static JSONArray clone(final JSONArray source) throws JSONException {
        final JSONArray clone = new JSONArray();
        final int length = source.length();
        for (int i = 0; i < length; i++) {
            clone.put(cloneObject(source.get(i)));
        }
        return clone;
    }

    /**
     * Clones given object. If the object is an immutable instance of <code>Boolean</code>, <code>Double</code>, <code>Integer</code>,
     * <code>Long</code>, <code>String</code>, or the {@link JSONObject#NULL} object, the object itself is returned. Otherwise the object is
     * an instance of <code>JSONObject</code> or <code>JSONArray</code>, then a deep clone is returned.
     *
     * @param source The object to clone
     * @return The cloned object
     * @throws JSONException If a JSON error occurs
     */
    private static Object cloneObject(final Object source) throws JSONException {
        if (source instanceof JSONObject) {
            return clone((JSONObject) source);
        }
        if (source instanceof JSONArray) {
            return clone((JSONArray) source);
        }
        // Immutable object needs not to be cloned: Boolean, Double, Integer, Long, String, or the JSONObject.NULL object
        return source;
    }

}
