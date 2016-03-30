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
    public static boolean supportsUserFlags(final IMAPFolder f, final boolean load, final Session session, final int accountId) throws MessagingException {
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
                        final String[] userFlags = f.getAvailableFlags().getUserFlags();
                        if (null != userFlags && userFlags.length > 0) {
                            entry.setValue(Boolean.TRUE);
                        } else {
                            entry.setValue(Boolean.valueOf(supportsUserDefinedFlags(f)));
                        }
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
    public static void removeUserFlags(final IMAPFolder f, final Session session, final int accountId) {
        SessionMailCache.getInstance(session, accountId).remove(new UserFlagCacheEntry(f.getFullName()));
    }

    /**
     * Removes cached information if given IMAP folder supports user flags
     *
     * @param fullName The IMAP folder full name
     * @param session The session providing the session-bound cache
     * @param accountId The account ID
     */
    public static void removeUserFlags(final String fullName, final Session session, final int accountId) {
        SessionMailCache.getInstance(session, accountId).remove(new UserFlagCacheEntry(fullName));
    }

    private static final class UserFlagCacheEntry implements SessionMailCacheEntry<Boolean> {

        private final String fullname;

        private volatile Boolean value;

        private volatile CacheKey key;

        public UserFlagCacheEntry(final String fullname) {
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
        public void setValue(final Boolean value) {
            this.value = value;
        }

        @Override
        public Class<Boolean> getEntryClass() {
            return Boolean.class;
        }

    }
}
