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

import static com.openexchange.imap.IMAPCommandsCollection.supportsUserDefinedFlags;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.cache.SessionMailCacheEntry;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link UserFlagsCache}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserFlagsCache {

    private static final boolean CHECK_ONCE = true;

    private static final String INBOX = "INBOX";

    /**
     * No instance
     */
    private UserFlagsCache() {
        super();
    }

    /**
     * Determines if specified IMAP folder supports user flags
     *
     * @param f The IMAP folder
     * @param load Whether the <code>SELECT</code> command should be invoked on IMAP folder or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return <code>true</code> if user flags are supported; otherwise <code>false</code>
     * @throws MessagingException If <code>SELECT</code> command fails
     */
    public static boolean supportsUserFlags(IMAPFolder f, boolean load, Session session, int accountId) throws MessagingException {
        final UserFlagCacheEntry entry = new UserFlagCacheEntry(CHECK_ONCE ? INBOX : f.getFullName());
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accountId);
        mailCache.get(entry);
        if (load && (null == entry.getValue())) {
            /*
             * Obtain folder lock here to avoid multiple acquire/release when invoking folder's methods
             */
            synchronized (f) {
                if (f.isOpen()) {
                    if (Folder.READ_WRITE == f.getMode()) {
                        entry.setValue(Boolean.valueOf(f.getPermanentFlags().contains(Flags.Flag.USER)));
                    } else {
                        // Close & reopen in read-write mode
                        f.close(false);
                        f.open(Folder.READ_WRITE);
                        entry.setValue(Boolean.valueOf(f.getPermanentFlags().contains(Flags.Flag.USER)));
                    }
                } else {
                    entry.setValue(Boolean.valueOf(supportsUserDefinedFlags(f)));
                }
            }
            mailCache.put(entry);
        }
        final Boolean b = entry.getValue();
        return b == null ? false : b.booleanValue();
    }

    /**
     * Removes cached information if given IMAP folder supports user flags
     *
     * @param f The IMAP folder
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     */
    public static void removeUserFlags(IMAPFolder f, Session session, int accountId) {
        SessionMailCache.getInstance(session, accountId).remove(new UserFlagCacheEntry(f.getFullName()));
    }

    /**
     * Removes cached information if given IMAP folder supports user flags
     *
     * @param fullName The IMAP folder full name
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     */
    public static void removeUserFlags(String fullName, Session session, int accountId) {
        SessionMailCache.getInstance(session, accountId).remove(new UserFlagCacheEntry(fullName));
    }

    private static final class UserFlagCacheEntry implements SessionMailCacheEntry<Boolean> {

        private final String fullname;

        private volatile Boolean value;

        private volatile CacheKey key;

        public UserFlagCacheEntry(String fullname) {
            super();
            this.fullname = fullname;
        }

        private CacheKey getKeyInternal() {
            CacheKey tmp = key;
            if (null == tmp) {
                key = tmp = Services.getService(CacheService.class).newCacheKey(MailCacheCode.USER_FLAGS.getCode(), fullname);
            }
            return tmp;
        }

        @Override
        public CacheKey getKey() {
            return getKeyInternal();
        }

        @Override
        public Boolean getValue() {
            return value;
        }

        @Override
        public void setValue(Boolean value) {
            this.value = value;
        }

        @Override
        public Class<Boolean> getEntryClass() {
            return Boolean.class;
        }

    }
}
