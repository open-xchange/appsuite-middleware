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

import javax.mail.MessagingException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.cache.SessionMailCacheEntry;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.Rights;

/**
 * {@link RightsCache}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RightsCache {

    /**
     * No instance
     */
    private RightsCache() {
        super();
    }

    /**
     * Gets cached <code>MYRIGHTS</code> command invoked on given IMAP folder.
     *
     * @param f The IMAP folder
     * @param load Whether <code>MYRIGHTS</code> command should be invoked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     * @return The cached rights or <code>null</code>
     * @throws MessagingException If <code>MYRIGHTS</code> command fails
     */
    public static Rights getCachedRights(IMAPFolder f, boolean load, Session session, int accountId) throws MessagingException {
        RightsCacheEntry entry = new RightsCacheEntry(f.getFullName());
        SessionMailCache mailCache = SessionMailCache.getInstance(session, accountId);
        mailCache.get(entry);
        if (load && (null == entry.getValue())) {
            try {
                entry.setValue(f.myRights());
            } catch (MessagingException e) {
                // Hmm...
                throw e;
            }
            mailCache.put(entry);
        }
        return entry.getValue();
    }

    /**
     * Removes cached <code>MYRIGHTS</code> command invoked on given IMAP folder.
     *
     * @param f The IMAP folder
     * @param session The session providing the session-bound cache
     * @param accontId The account ID
     */
    public static void removeCachedRights(IMAPFolder f, Session session, int accontId) {
        SessionMailCache sessionMailCache = SessionMailCache.optInstance(session, accontId);
        if (null != sessionMailCache) {
            sessionMailCache.remove(new RightsCacheEntry(f.getFullName()));
        }
    }

    /**
     * Removes cached <code>MYRIGHTS</code> command invoked on given IMAP folder.
     *
     * @param fullName The IMAP folder full name
     * @param session The session providing the session-bound cache
     * @param accontId The account ID
     */
    public static void removeCachedRights(String fullName, Session session, int accontId) {
        SessionMailCache sessionMailCache = SessionMailCache.optInstance(session, accontId);
        if (null != sessionMailCache) {
            sessionMailCache.remove(new RightsCacheEntry(fullName));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private static final class RightsCacheEntry implements SessionMailCacheEntry<Rights> {

        private final String fullname;
        private volatile Rights rights;
        private volatile CacheKey key;

        RightsCacheEntry(String fullname) {
            this(fullname, null);
        }

        RightsCacheEntry(String fullname, Rights rights) {
            super();
            this.fullname = fullname;
            this.rights = rights;
        }

        private CacheKey getKeyInternal() {
            CacheKey tmp = key;
            if (null == tmp) {
                key = tmp = Services.getService(CacheService.class).newCacheKey(MailCacheCode.RIGHTS.getCode(), fullname);
            }
            return tmp;
        }

        @Override
        public CacheKey getKey() {
            return getKeyInternal();
        }

        @Override
        public Rights getValue() {
            return rights;
        }

        @Override
        public void setValue(Rights value) {
            rights = value;
        }

        @Override
        public Class<Rights> getEntryClass() {
            return Rights.class;
        }
    } // End of class RightsCacheEntry

}
