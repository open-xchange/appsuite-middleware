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

package com.openexchange.imap.threader.references;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.imap.services.Services;
import com.openexchange.java.Charsets;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link ConversationCache}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ConversationCache {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ConversationCache.class);

    /** The cache region name */
    public static final String REGION_NAME = "IMAPConversations";

    private static volatile ConversationCache instance;

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ConversationCache getInstance() {
        return instance;
    }

    /**
     * Initializes the instance.
     *
     * @param services The service look-up
     * @throws OXException If operation fails
     */
    public static void initInstance(ServiceLookup services) throws OXException {
        if (null == instance) {
            synchronized (ConversationCache.class) {
                if (null == instance) {
                    instance = new ConversationCache(services);
                }
            }
        }
    }

    /**
     * Releases the instance.
     *
     * @throws OXException If operation fails
     */
    public static void releaseInstance() throws OXException {
        ConversationCache tmp = instance;
        if (null != tmp) {
            synchronized (ConversationCache.class) {
                tmp = instance;
                if (null != tmp) {
                    instance = null;
                    tmp.releaseCache();
                }
            }
        }
    }

    /**
     * Gets the calculated hash string for specified arguments.
     *
     * @return The calculated hash string
     */
    public static String getArgsHash(MailSortField sortField, OrderDirection order, int lookAhead, boolean mergeWithSent, MailFields usedFields, String[] headerNames, int total, long uidnext, int sentTotal, long sentUidNext) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sortField.getKey().getBytes(Charsets.UTF_8));
            md.update(order.name().getBytes(Charsets.UTF_8));
            md.update(intToByteArray(lookAhead));
            md.update(mergeWithSent ? (byte) 1 : 0);
            md.update(usedFields.toByteArray());
            if (null != headerNames && headerNames.length > 0) {
                for (String headerName : headerNames) {
                    md.update(Charsets.getBytes(headerName, Charsets.UTF_8));
                }
            }
            md.update(intToByteArray(total));
            md.update(longToByteArray(uidnext));
            if (mergeWithSent) {
                md.update(intToByteArray(sentTotal));
                md.update(longToByteArray(sentUidNext));
            }
            return asHex(md.digest());
        } catch (final NoSuchAlgorithmException e) {
            LOG.error("", e);
            throw new IllegalStateException(e);
        }
    }


    /**
     * Gets the calculated hash string for specified arguments.
     *
     * @param args The arguments to include in the hash
     * @return The calculated hash string
     */
    public static String getArgsHash(String... args) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (null != args) {
                for (String value : args) {
                    md.update(value.getBytes(Charsets.UTF_8));
                }
            }
            return asHex(md.digest());
        } catch (final NoSuchAlgorithmException e) {
            LOG.error("", e);
            throw new IllegalStateException(e);
        }
    }

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Turns array of bytes into string representing each byte as unsigned hex number.
     *
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    private static String asHex(byte[] hash) {
        int length = hash.length;
        char[] buf = new char[length << 1];
        for (int i = 0, x = 0; i < length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

    private static byte[] intToByteArray(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    private static byte[] longToByteArray(long value) {
        return new byte[] { (byte) (value >> 56), (byte) (value >> 48), (byte) (value >> 40), (byte) (value >> 32), (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private final Cache cache;

    /**
     * Initializes a new {@link ConversationCache}.
     *
     * @throws OXException If initialization fails
     */
    private ConversationCache(ServiceLookup services) throws OXException {
        super();
        cache = services.getService(CacheService.class).getCache(REGION_NAME);
    }

    /**
     * Releases cache reference.
     *
     * @throws OXException If clearing cache fails
     */
    public void releaseCache() throws OXException {
        cache.clear();
        CacheService cacheService = Services.getService(CacheService.class);
        if (null != cacheService) {
            cacheService.freeCache(REGION_NAME);
        }
    }

    private CacheKey getMapKey(int userId, int cid) {
        Cache cache = this.cache;
        return cache.newCacheKey(cid, userId);
    }

    /**
     * Checks if a cached conversation is contained for given arguments
     *
     * @param fullName The full name
     * @param accountId The account identifier
     * @param session The associated session
     * @return <code>true</code> if contaoined; othrewise <code>false</code>
     */
    public boolean containsCachedConversations(String fullName, int accountId, Session session) {
        CacheKey mapKey = getMapKey(session.getUserId(), session.getContextId());
        Object obj = cache.get(mapKey);
        if (!(obj instanceof ConcurrentMap)) {
            return false;
        }

        ConcurrentMap<Integer, ConcurrentMap<String, CacheEntry>> accounts = (ConcurrentMap<Integer, ConcurrentMap<String, CacheEntry>>) obj;
        ConcurrentMap<String, CacheEntry> folders = accounts.get(Integer.valueOf(accountId));
        if (null == folders) {
            return false;
        }

        return folders.containsKey(fullName);
    }

    /**
     * Removes the cached conversations for a user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void removeUserConversations(int userId, int contextId) {
        CacheKey mapKey = getMapKey(userId, contextId);
        Object obj = cache.get(mapKey);
        if (!(obj instanceof ConcurrentMap)) {
            return;
        }

        ((ConcurrentMap<Integer, ConcurrentMap<String, CacheEntry>>) obj).clear();
    }

    /**
     * Removes the cached conversations for an account.
     *
     * @param session The associated session
     */
    public void removeAccountConversations(int accountId, Session session) {
        CacheKey mapKey = getMapKey(session.getUserId(), session.getContextId());
        Object obj = cache.get(mapKey);
        if (!(obj instanceof ConcurrentMap)) {
            return;
        }

        ((ConcurrentMap<Integer, ConcurrentMap<String, CacheEntry>>) obj).remove(Integer.valueOf(accountId));
    }

    /**
     * Gets the cached conversations
     *
     * @param fullName The full name
     * @param accountId The account identifier
     * @param argsHash The arguments hash
     * @param session The associated session
     * @return The cached conversations or <code>null</code>
     */
    public List<List<MailMessage>> getCachedConversations(String fullName, int accountId, String argsHash, Session session) {
        CacheKey mapKey = getMapKey(session.getUserId(), session.getContextId());
        Object obj = cache.get(mapKey);
        if (!(obj instanceof ConcurrentMap)) {
            return null;
        }

        ConcurrentMap<Integer, ConcurrentMap<String, CacheEntry>> accounts = (ConcurrentMap<Integer, ConcurrentMap<String, CacheEntry>>) obj;
        ConcurrentMap<String, CacheEntry> cacheEntries = accounts.get(Integer.valueOf(accountId));
        if (null == cacheEntries) {
            return null;
        }

        CacheEntry cacheEntry = cacheEntries.get(fullName);
        if (null == cacheEntry) {
            return null;
        }

        if (false == argsHash.equals(cacheEntry.argsHash)) {
            cacheEntries.remove(fullName, cacheEntry);
            return null;
        }

        return cacheEntry.conversations;
    }

    /**
     * Puts the conversations into cache.
     *
     * @param conversations The conversations to put
     * @param fullName The full name
     * @param accountId The account identifier
     * @param argsHash The arguments hash
     * @param session The associated session
     */
    public void putCachedConversations(List<List<MailMessage>> conversations, String fullName, int accountId, String argsHash, Session session) {
        CacheKey mapKey = getMapKey(session.getUserId(), session.getContextId());

        ConcurrentMap<Integer, ConcurrentMap<String, CacheEntry>> accounts;
        Object obj = cache.get(mapKey);
        if (obj instanceof ConcurrentMap) {
            accounts = (ConcurrentMap<Integer, ConcurrentMap<String, CacheEntry>>) obj;
        } else {
            try {
                ConcurrentHashMap<Integer, ConcurrentMap<String, CacheEntry>> newAccountEntries = new ConcurrentHashMap<Integer, ConcurrentMap<String, CacheEntry>>(8, 0.9F, 1);
                accounts = newAccountEntries;
                cache.putSafe(mapKey, newAccountEntries);
            } catch (OXException e) {
                obj = cache.get(mapKey);
                accounts = (ConcurrentMap<Integer, ConcurrentMap<String, CacheEntry>>) obj;
            }
        }

        ConcurrentMap<String, CacheEntry> cacheEntries = accounts.get(Integer.valueOf(accountId));
        if (null == cacheEntries) {
            ConcurrentMap<String, CacheEntry> newCacheEntries = new ConcurrentHashMap<String, CacheEntry>(16, 0.9F, 1);
            cacheEntries = accounts.putIfAbsent(Integer.valueOf(accountId), newCacheEntries);
            if (null == cacheEntries) {
                cacheEntries = newCacheEntries;
            }
        }

        // Put the given one
        cacheEntries.put(fullName, new CacheEntry(conversations, argsHash));
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private static class CacheEntry {
        final String argsHash;
        final List<List<MailMessage>> conversations;

        CacheEntry(List<List<MailMessage>> conversations, String argsHash) {
            super();
            this.conversations = conversations;
            this.argsHash = argsHash;
        }
    }

}
