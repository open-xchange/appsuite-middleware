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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import javax.mail.MessagingException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.imap.cache.util.FolderMap;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.imap.converters.IMAPFolderConverter;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.cache.SessionMailCacheEntry;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link FolderCache} - A session-bound cache for IMAP folders converted to a {@link MailFolder} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderCache {

    private static final int MAX_CAPACITY_DEFAULT_ACCOUNT = 128;

    private static final int MAX_CAPACITY_PER_ACCOUNT = 16;

    /**
     * No instance
     */
    private FolderCache() {
        super();
    }

    /**
     * Gets cached IMAP folder.
     *
     * @param fullName The IMAP folder full name
     * @param folderStorage The connected IMAP folder storage
     * @return The cached IMAP folder or <code>null</code>
     */
    public static MailFolder optCachedFolder(final String fullName, final IMAPFolderStorage folderStorage) {
        if (!IMAPProperties.getInstance().allowFolderCaches()) {
            return null;
        }
        final Session session = folderStorage.getSession();
        /*
         * Initialize appropriate cache entry
         */
        final FolderCacheEntry entry = new FolderCacheEntry();
        final int accountId = folderStorage.getAccountId();
        /*
         * Get entry from session cache
         */
        SessionMailCache.getInstance(session, accountId).get(entry);
        final FolderMap folderMap = entry.getValue();
        if (null == folderMap) {
            return null;
        }
        /*
         * Check for folder
         */
        final MailFolder mailFolder = folderMap.get(fullName);
        return (MailFolder) (null == mailFolder ? null : mailFolder.clone());
    }

    /**
     * Updates the cached IMAP folder
     *
     * @param fullName The full name
     * @param folderStorage The folder storage
     * @throws OXException If loading the folder fails
     */
    public static void updateCachedFolder(final String fullName, final IMAPFolderStorage folderStorage) throws OXException {
        updateCachedFolder(fullName, folderStorage, null);
    }

    /**
     * Updates the cached IMAP folder
     *
     * @param fullName The full name
     * @param folderStorage The folder storage
     * @param imapFolder The optional IMAP folder
     * @throws OXException If loading the folder fails
     */
    public static void updateCachedFolder(final String fullName, final IMAPFolderStorage folderStorage, final IMAPFolder imapFolder) throws OXException {
        if (!IMAPProperties.getInstance().allowFolderCaches()) {
            return;
        }
        final Session session = folderStorage.getSession();
        /*
         * Initialize appropriate cache entry
         */
        final FolderCacheEntry entry = new FolderCacheEntry();
        final int accountId = folderStorage.getAccountId();
        SessionMailCache.getInstance(session, accountId).get(entry);
        final FolderMap folderMap = entry.getValue();
        if (null == folderMap) {
            return;
        }
        synchronized (folderMap) {
            folderMap.remove(fullName);
            folderMap.put(fullName, loadFolder(fullName, folderStorage, imapFolder));
        }
    }

    /**
     * Gets cached IMAP folder.
     *
     * @param fullName The IMAP folder full name
     * @param folderStorage The connected IMAP folder storage
     * @return The cached IMAP folder
     * @throws OXException If loading the folder fails
     */
    public static MailFolder getCachedFolder(final String fullName, final IMAPFolderStorage folderStorage) throws OXException {
        return getCachedFolder(fullName, folderStorage, null);
    }

    /**
     * Gets cached IMAP folder.
     *
     * @param fullName The IMAP folder full name
     * @param folderStorage The connected IMAP folder storage
     * @param The possibly loaded IMAP folder; may be <code>null</code>
     * @return The cached IMAP folder
     * @throws OXException If loading the folder fails
     */
    public static MailFolder getCachedFolder(final String fullName, final IMAPFolderStorage folderStorage, final IMAPFolder imapFolder) throws OXException {
        if (!IMAPProperties.getInstance().allowFolderCaches()) {
            return loadFolder(fullName, folderStorage, imapFolder);
        }
        final Session session = folderStorage.getSession();
        /*
         * Initialize appropriate cache entry
         */
        final FolderCacheEntry entry = new FolderCacheEntry();
        final int accountId = folderStorage.getAccountId();
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accountId);
        mailCache.get(entry);
        FolderMap folderMap = entry.getValue();
        if (null == folderMap) {
            Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
            if (null == lock) {
                lock = Session.EMPTY_LOCK;
            }
            lock.lock();
            try {
                mailCache.get(entry);
                folderMap = entry.getValue();
                if (null == folderMap) {
                    final FolderMap newMap = new FolderMap(MailAccount.DEFAULT_ID == accountId ? MAX_CAPACITY_DEFAULT_ACCOUNT : MAX_CAPACITY_PER_ACCOUNT);
                    entry.setValue(newMap);
                    mailCache.put(entry);
                    folderMap = newMap;
                }
            } finally {
                lock.unlock();
            }
        }
        /*
         * Check for folder
         */
        MailFolder mailFolder = folderMap.get(fullName);
        if (null == mailFolder) {
            try {
                final MailFolder newFld = loadFolder(fullName, folderStorage, imapFolder);
                if (isNotCacheable(fullName, session, accountId, folderStorage.getImapStore())) {
                    /*
                     * Don't cache
                     */
                    return newFld;
                }
                mailFolder = folderMap.putIfAbsent(fullName, newFld);
                if (null == mailFolder) {
                    mailFolder = newFld;
                }
            } catch (final MessagingException e) {
                throw IMAPException.handleMessagingException(e, folderStorage.getImapConfig(), session, accountId, mapFor("fullName", fullName));
            }
        }
        /*
         * Return
         */
        return (MailFolder) mailFolder.clone();
    }

    private static boolean isNotCacheable(final String fullName, final Session session, final int accountId, final IMAPStore imapStore) throws MessagingException {
        return NamespaceFoldersCache.startsWithAnyOfSharedNamespaces(fullName, imapStore, true, session, accountId) || NamespaceFoldersCache.startsWithAnyOfUserNamespaces(fullName, imapStore, true, session, accountId);
    }

    private static final MailFolder loadFolder(final String fullName, final IMAPFolderStorage folderStorage, final IMAPFolder imapFolder) throws OXException {
        if (null != imapFolder) {
            return IMAPFolderConverter.convertFolder(imapFolder, folderStorage.getSession(), folderStorage.getImapAccess(), folderStorage.getContext());
        }
        final Session session = folderStorage.getSession();
        final IMAPConfig imapConfig = folderStorage.getImapConfig();
        try {
            final IMAPStore imapStore = folderStorage.getImapStore();
            final String imapFullName;
            IMAPFolder f;
            if (MailFolder.DEFAULT_FOLDER_ID.equals(fullName) || 0 == fullName.length()) {
                f = (IMAPFolder) imapStore.getDefaultFolder();
                imapFullName = "";
            } else {
                f = (IMAPFolder) imapStore.getFolder(fullName);
                imapFullName = fullName;
            }
            if (!"INBOX".equals(imapFullName) && !ListLsubCache.getCachedLISTEntry(imapFullName, folderStorage.getAccountId(), f, session).exists()) {
                f = folderStorage.checkForNamespaceFolder(imapFullName);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
            }
            return IMAPFolderConverter.convertFolder(f, session, folderStorage.getImapAccess(), folderStorage.getContext());
        } catch (final MessagingException e) {
            throw IMAPException.handleMessagingException(e, imapConfig, session, folderStorage.getAccountId(), mapFor("fullName", fullName));
        }
    }

    /**
     * Removes all cached IMAP folders.
     *
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     */
    public static void removeCachedFolders(final Session session, final int accountId) {
        final FolderCacheEntry entry = new FolderCacheEntry();
        SessionMailCache.getInstance(session, accountId).get(entry);
        final FolderMap folderMap = entry.getValue();
        if (null != folderMap) {
            folderMap.clear();
        }
    }

    /**
     * Removes cached IMAP folder.
     *
     * @param fullName The IMAP folder full name
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     */
    public static void removeCachedFolder(final String fullName, final Session session, final int accountId) {
        final FolderCacheEntry entry = new FolderCacheEntry();
        SessionMailCache.getInstance(session, accountId).get(entry);
        final FolderMap folderMap = entry.getValue();
        if (null != folderMap) {
            final MailFolder mailFolder = folderMap.remove(fullName);
            if (null != mailFolder) {
                final String parentFullname = mailFolder.getParentFullname();
                if (null != parentFullname) {
                    folderMap.remove(parentFullname);
                    if (parentFullname.equals(MailFolder.DEFAULT_FOLDER_ID)) {
                        folderMap.remove("");
                    }
                }
            }
        }
    }

    /**
     * Decrements unread message counter from cached IMAP folder.
     *
     * @param fullName The IMAP folder full name
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     */
    public static void decrementUnreadMessageCount(final String fullName, final Session session, final int accountId) {
        final FolderCacheEntry entry = new FolderCacheEntry();
        SessionMailCache.getInstance(session, accountId).get(entry);
        final FolderMap folderMap = entry.getValue();
        if (null != folderMap) {
            synchronized (folderMap) {
                final MailFolder mailFolder = folderMap.get(fullName);
                if (null != mailFolder) {
                    final int cur = mailFolder.getUnreadMessageCount();
                    mailFolder.setUnreadMessageCount(cur > 0 ? cur - 1 : 0);
                }
            }
        }
    }

    private static final class FolderCacheEntry implements SessionMailCacheEntry<FolderMap> {

        private volatile FolderMap folderMap;

        private final CacheKey key;

        public FolderCacheEntry() {
            this(null);
        }

        public FolderCacheEntry(final FolderMap folderMap) {
            super();
            this.folderMap = folderMap;
            final int code = MailCacheCode.FOLDERS.getCode();
            key = Services.getService(CacheService.class).newCacheKey(code, code);
        }

        @Override
        public CacheKey getKey() {
            return key;
        }

        @Override
        public FolderMap getValue() {
            return folderMap;
        }

        @Override
        public void setValue(final FolderMap folderMap) {
            this.folderMap = folderMap;
        }

        @Override
        public Class<FolderMap> getEntryClass() {
            return FolderMap.class;
        }
    }

    private static Map<String, Object> mapFor(final String... pairs) {
        if (null == pairs) {
            return null;
        }
        final int length = pairs.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }
        final Map<String, Object> map = new HashMap<String, Object>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(pairs[i], pairs[i+1]);
        }
        return map;
    }

}
