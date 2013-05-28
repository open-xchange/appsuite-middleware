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

import static com.openexchange.imap.IMAPCommandsCollection.canCreateSubfolder;
import javax.mail.MessagingException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.cache.SessionMailCacheEntry;
import com.openexchange.session.Session;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link RootSubfolderCache}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RootSubfolderCache {

    /**
     * No instance
     */
    private RootSubfolderCache() {
        super();
    }

    /**
     * Gets cached <code>boolean</code> value if root folder allows subfolder creation
     *
     * @param f The IMAP root folder
     * @param load Whether subfolder creation shall be checked if no cache entry present or not
     * @param session The session providing the session-bound cache
     * @param accontId The account ID
     * @return The cached subfolder creation or <code>null</code>
     * @throws MessagingException If checking subfolder creation fails
     */
    public static Boolean canCreateSubfolders(final DefaultFolder f, final boolean load, final Session session, final int accontId) throws MessagingException {
        final CreationCacheEntry entry = new CreationCacheEntry();
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accontId);
        mailCache.get(entry);
        if (load && (null == entry.getValue())) {
            entry.setValue(canCreateSubfolder(f));
            mailCache.put(entry);
        }
        return entry.getValue();
    }

    /**
     * Removes cached <code>boolean</code> value if root folder allows subfolder creation
     *
     * @param f The IMAP root folder
     * @param session The session providing the session-bound cache
     * @param accontId The account ID
     */
    public static void removeCachedRights(final IMAPFolder f, final Session session, final int accountId) {
        SessionMailCache.getInstance(session, accountId).remove(new CreationCacheEntry());
    }

    private static final class CreationCacheEntry implements SessionMailCacheEntry<Boolean> {

        private static final Integer DUMMY = Integer.valueOf(1);

        private volatile Boolean subfolderCreation;

        private volatile CacheKey key;

        public CreationCacheEntry() {
            this(Boolean.FALSE);
        }

        public CreationCacheEntry(final Boolean subfolderCreation) {
            super();
            this.subfolderCreation = subfolderCreation;
        }

        private CacheKey getKeyInternal() {
            CacheKey tmp = key;
            if (null == tmp) {
                key = tmp = Services.getService(CacheService.class).newCacheKey(MailCacheCode.ROOT_SUBFOLDER.getCode(), DUMMY);
            }
            return tmp;
        }

        @Override
        public CacheKey getKey() {
            return getKeyInternal();
        }

        @Override
        public Boolean getValue() {
            return subfolderCreation;
        }

        @Override
        public void setValue(final Boolean value) {
            subfolderCreation = value;
        }

        @Override
        public Class<Boolean> getEntryClass() {
            return Boolean.class;
        }
    }

}
