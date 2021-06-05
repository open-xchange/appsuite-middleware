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

package com.openexchange.imap.cache;

import static com.openexchange.java.Strings.isEmpty;
import java.util.Arrays;
import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.cache.SessionMailCacheEntry;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link NamespaceFoldersCache}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NamespaceFoldersCache {

    private static final int NS_PERSONAL = 1;
    private static final int NS_USER = 2;
    private static final int NS_SHARED = 3;

    private static final String[] EMPTY_ARR = new String[0];

    /**
     * No instance
     */
    private NamespaceFoldersCache() {
        super();
    }

    /**
     * Gets cached personal namespaces when invoking <code>NAMESPACE</code> command on given IMAP store
     *
     * @param imapStore The IMAP store on which <code>NAMESPACE</code> command is invoked
     * @param load Whether <code>NAMESPACE</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return The <b>binary-sorted</b> personal namespace folders
     * @throws MessagingException If <code>NAMESPACE</code> command fails
     */
    public static String[] getPersonalNamespaces(IMAPStore imapStore, boolean load, Session session, int accountId) throws MessagingException {
        final NamespaceFoldersCacheEntry entry = new NamespaceFoldersCacheEntry(NS_PERSONAL);
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accountId);
        mailCache.get(entry);
        if (load && (null == entry.getValue())) {
            Folder[] pns = imapStore.getPersonalNamespaces();
            if ((pns == null) || (pns.length == 0)) {
                entry.setValue(EMPTY_ARR);
            } else {
                final String[] fullnames = new String[pns.length];
                for (int i = 0; i < pns.length; i++) {
                    final Folder namespaceFolder = pns[i];
                    fullnames[i] = namespaceFolder.getFullName();
                }
                Arrays.sort(fullnames);
                entry.setValue(fullnames);
            }
            mailCache.put(entry);
        }
        return entry.getValue();
    }

    /**
     * Gets cached personal namespace when invoking <code>NAMESPACE</code> command on given IMAP store
     *
     * @param imapStore The IMAP store on which <code>NAMESPACE</code> command is invoked
     * @param load Whether <code>NAMESPACE</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return The personal namespace folder or <code>null</code>
     * @throws MessagingException If <code>NAMESPACE</code> command fails
     */
    public static String getPersonalNamespace(IMAPStore imapStore, boolean load, Session session, int accountId) throws MessagingException {
        String[] personalNamespaces = getPersonalNamespaces(imapStore, load, session, accountId);
        return null != personalNamespaces && personalNamespaces.length != 0 ? personalNamespaces[0] : null;
    }

    /**
     * Checks if personal namespaces contain the specified full name
     *
     * @param fullname The full name to check
     * @param imapStore The IMAP store
     * @param load Whether <code>NAMESPACE</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return <code>true</code> if personal namespaces contain the specified full name; otherwise <code>false</code>
     * @throws MessagingException If <code>NAMESPACE</code> command fails
     */
    public static boolean containedInPersonalNamespaces(String fullname, IMAPStore imapStore, boolean load, Session session, int accountId) throws MessagingException {
        return Arrays.binarySearch(getPersonalNamespaces(imapStore, load, session, accountId), fullname) >= 0;
    }

    /**
     * Gets cached user namespaces when invoking <code>NAMESPACE</code> command on given IMAP store
     *
     * @param imapStore The IMAP store on which <code>NAMESPACE</code> command is invoked
     * @param load Whether <code>NAMESPACE</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return The <b>binary-sorted</b> user namespace folders
     * @throws MessagingException If <code>NAMESPACE</code> command fails
     */
    public static String[] getUserNamespaces(IMAPStore imapStore, boolean load, Session session, int accountId) throws MessagingException {
        final NamespaceFoldersCacheEntry entry = new NamespaceFoldersCacheEntry(NS_USER);
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accountId);
        mailCache.get(entry);
        if (load && (null == entry.getValue())) {
            Folder[] uns = imapStore.getUserNamespaces(null);
            if ((uns == null) || (uns.length == 0)) {
                entry.setValue(EMPTY_ARR);
            } else {
                final String[] fullnames = new String[uns.length];
                for (int i = 0; i < uns.length; i++) {
                    fullnames[i] = uns[i].getFullName();
                }
                Arrays.sort(fullnames);
                entry.setValue(fullnames);
            }
            mailCache.put(entry);
        }
        return entry.getValue();
    }

    /**
     * Checks if user namespaces contain the specified full name
     *
     * @param fullname The full name to check
     * @param imapStore The IMAP store
     * @param load Whether <code>NAMESPACE</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return <code>true</code> if user namespaces contain the specified fullname; otherwise <code>false</code>
     * @throws MessagingException If <code>NAMESPACE</code> command fails
     */
    public static boolean containedInUserNamespaces(String fullname, IMAPStore imapStore, boolean load, Session session, int accountId) throws MessagingException {
        return Arrays.binarySearch(getUserNamespaces(imapStore, load, session, accountId), fullname) >= 0;
    }

    /**
     * Checks if provided full name starts with any of user namespaces.
     *
     * @param fullname The full name to check
     * @param imapStore The IMAP store
     * @param load Whether <code>NAMESPACE</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return <code>true</code> if provided full name starts with any of user namespaces; otherwise <code>false</code>
     * @throws MessagingException If <code>NAMESPACE</code> command fails
     */
    public static boolean startsWithAnyOfUserNamespaces(String fullname, IMAPStore imapStore, boolean load, Session session, int accountId) throws MessagingException {
        for (String userNamespace : getUserNamespaces(imapStore, load, session, accountId)) {
            if (!isEmpty(userNamespace) && fullname.startsWith(userNamespace)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets cached shared namespaces when invoking <code>NAMESPACE</code> command on given IMAP store
     *
     * @param imapStore The IMAP store on which <code>NAMESPACE</code> command is invoked
     * @param load Whether <code>NAMESPACE</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return The <b>binary-sorted</b> shared namespace folders
     * @throws MessagingException If <code>NAMESPACE</code> command fails
     */
    public static String[] getSharedNamespaces(IMAPStore imapStore, boolean load, Session session, int accountId) throws MessagingException {
        final NamespaceFoldersCacheEntry entry = new NamespaceFoldersCacheEntry(NS_SHARED);
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accountId);
        mailCache.get(entry);
        if (load && (null == entry.getValue())) {
            Folder[] sns = imapStore.getSharedNamespaces();
            if ((sns == null) || (sns.length == 0)) {
                entry.setValue(EMPTY_ARR);
            } else {
                final String[] fullnames = new String[sns.length];
                for (int i = 0; i < sns.length; i++) {
                    fullnames[i] = sns[i].getFullName();
                }
                Arrays.sort(fullnames);
                entry.setValue(fullnames);
            }
            mailCache.put(entry);
        }
        return entry.getValue();
    }

    /**
     * Checks if shared namespaces contain the specified full name.
     *
     * @param fullname The full name to check
     * @param imapStore The IMAP store
     * @param load Whether <code>NAMESPACE</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return <code>true</code> if shared namespaces contain the specified full name; otherwise <code>false</code>
     * @throws MessagingException If <code>NAMESPACE</code> command fails
     */
    public static boolean containedInSharedNamespaces(String fullname, IMAPStore imapStore, boolean load, Session session, int accountId) throws MessagingException {
        return Arrays.binarySearch(getSharedNamespaces(imapStore, load, session, accountId), fullname) >= 0;
    }

    /**
     * Checks if provided full name starts with any of shared namespaces.
     *
     * @param fullname The full name to check
     * @param imapStore The IMAP store
     * @param load Whether <code>NAMESPACE</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return <code>true</code> if provided full name starts with any of shared namespaces; otherwise <code>false</code>
     * @throws MessagingException If <code>NAMESPACE</code> command fails
     */
    public static boolean startsWithAnyOfSharedNamespaces(String fullname, IMAPStore imapStore, boolean load, Session session, int accountId) throws MessagingException {
        for (String sharedNamespace : getSharedNamespaces(imapStore, load, session, accountId)) {
            if (!isEmpty(sharedNamespace) && fullname.startsWith(sharedNamespace)) {
                return true;
            }
        }
        return false;
    }

    private static final class NamespaceFoldersCacheEntry implements SessionMailCacheEntry<String[]> {

        private final int namespaceKey;
        private volatile String[] fullnames;
        private volatile CacheKey key;

        NamespaceFoldersCacheEntry(int namespaceKey) {
            this(namespaceKey, null);
        }

        NamespaceFoldersCacheEntry(int namespaceKey, String[] fullnames) {
            super();
            this.namespaceKey = namespaceKey;
            this.fullnames = fullnames;
        }

        private CacheKey getKeyInternal() {
            CacheKey tmp = key;
            if (null == tmp) {
                key = tmp = Services.getService(CacheService.class).newCacheKey(MailCacheCode.NAMESPACE_FOLDERS.getCode(), namespaceKey);
            }
            return tmp;
        }

        @Override
        public CacheKey getKey() {
            return getKeyInternal();
        }

        @Override
        public String[] getValue() {
            return fullnames;
        }

        @Override
        public void setValue(String[] value) {
            fullnames = value;
        }

        @Override
        public Class<String[]> getEntryClass() {
            return String[].class;
        }

    }
}
