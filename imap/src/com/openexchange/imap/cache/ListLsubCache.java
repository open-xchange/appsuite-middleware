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

package com.openexchange.imap.cache;

import java.util.List;
import java.util.concurrent.locks.Lock;
import javax.mail.MessagingException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.mail.MailException;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.cache.SessionMailCacheEntry;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link ListLsubCache} - A session-bound cache for LIST/LSUB entries.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ListLsubCache {

    private static final long TIMEOUT = 300000;

    private static final String INBOX = "INBOX";

    private static final boolean DO_STATUS = true;

    private static final boolean DO_GETACL = true;

    /**
     * No instance
     */
    private ListLsubCache() {
        super();
    }

    /**
     * Removes cached LIST/LSUB entry.
     * 
     * @param fullName The full name
     * @param accountId The account ID
     * @param session The session
     */
    public static void removeCachedEntry(final String fullName, final int accountId, final Session session) {
        final ListLsubCacheEntry entry = new ListLsubCacheEntry();
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accountId);
        mailCache.get(entry);
        final ListLsubCollection collection = entry.getValue();
        if (null != collection) {
            synchronized (collection) {
                collection.remove(fullName);
            }
        }
    }

    /**
     * Clears the cache.
     * 
     * @param accountId The account ID
     * @param session The session
     */
    public static void clearCache(final int accountId, final Session session) {
        final ListLsubCacheEntry entry = new ListLsubCacheEntry();
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accountId);
        mailCache.get(entry);
        final ListLsubCollection collection = entry.getValue();
        if (null != collection) {
            synchronized (collection) {
                collection.clear();
            }
        }
    }

    /**
     * Gets the separator character.
     * 
     * @param accountId The account ID
     * @param imapStore The connected IMAP store instance
     * @param session The session
     * @return The separator
     * @throws MailException If a mail error occurs
     */
    public static char getSeparator(final int accountId, final IMAPStore imapStore, final Session session) throws MailException {
        try {
            return getSeparator(accountId, (IMAPFolder) imapStore.getFolder(INBOX), session);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets the separator character.
     * 
     * @param accountId The account ID
     * @param imapFolder An IMAP folder
     * @param session The session
     * @return The separator
     * @throws MailException If a mail error occurs
     */
    public static char getSeparator(final int accountId, final IMAPFolder imapFolder, final Session session) throws MailException {
        return getCachedLISTEntry(INBOX, accountId, imapFolder, session).getSeparator();
    }

    /**
     * Gets cached LSUB entry for specified full name.
     * 
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapFolder The IMAP
     * @param session The session
     * @return The cached LSUB entry
     * @throws MailException If loading the entry fails
     */
    public static ListLsubEntry getCachedLSUBEntry(final String fullName, final int accountId, final IMAPFolder imapFolder, final Session session) throws MailException {
        final ListLsubCollection collection = getCollection(accountId, imapFolder, session);
        synchronized (collection) {
            /*
             * Check collection's stamp
             */
            if ((System.currentTimeMillis() - collection.getStamp()) > TIMEOUT) {
                collection.reinit(imapFolder, DO_STATUS, DO_GETACL);
            }
            /*
             * Return
             */
            final ListLsubEntry lsubEntry = collection.getLsub(fullName);
            return null == lsubEntry ? ListLsubCollection.emptyEntryFor(fullName) : lsubEntry;
        }
    }

    /**
     * Gets cached LIST entry for specified full name.
     * 
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapStore The IMAP store
     * @param session The session
     * @return The cached LIST entry
     * @throws MailException If loading the entry fails
     */
    public static ListLsubEntry getCachedLISTEntry(final String fullName, final int accountId, final IMAPStore imapStore, final Session session) throws MailException {
        try {
            final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(INBOX);
            final ListLsubCollection collection = getCollection(accountId, imapFolder, session);
            synchronized (collection) {
                /*
                 * Check collection's stamp
                 */
                if ((System.currentTimeMillis() - collection.getStamp()) > TIMEOUT) {
                    collection.reinit(imapFolder, DO_STATUS, DO_GETACL);
                }
                /*
                 * Return
                 */
                final ListLsubEntry listEntry = collection.getList(fullName);
                return null == listEntry ? ListLsubCollection.emptyEntryFor(fullName) : listEntry;
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    /**
     * Gets cached LIST entry for specified full name.
     * 
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapFolder The IMAP
     * @param session The session
     * @return The cached LIST entry
     * @throws MailException If loading the entry fails
     */
    public static ListLsubEntry getCachedLISTEntry(final String fullName, final int accountId, final IMAPFolder imapFolder, final Session session) throws MailException {
        final ListLsubCollection collection = getCollection(accountId, imapFolder, session);
        synchronized (collection) {
            /*
             * Check collection's stamp
             */
            if ((System.currentTimeMillis() - collection.getStamp()) > TIMEOUT) {
                collection.reinit(imapFolder, DO_STATUS, DO_GETACL);
            }
            /*
             * Return
             */
            final ListLsubEntry listEntry = collection.getList(fullName);
            return null == listEntry ? ListLsubCollection.emptyEntryFor(fullName) : listEntry;
        }
    }

    /**
     * Gets cached LIST/LSUB entry for specified full name.
     * 
     * @param fullName The full name
     * @param accountId The account ID
     * @param imapFolder The IMAP
     * @param session The session
     * @return The cached LIST/LSUB entry
     * @throws MailException If loading the entry fails
     */
    public static ListLsubEntry[] getCachedEntries(final String fullName, final int accountId, final IMAPFolder imapFolder, final Session session) throws MailException {
        final ListLsubCollection collection = getCollection(accountId, imapFolder, session);
        synchronized (collection) {
            /*
             * Check collection's stamp
             */
            if ((System.currentTimeMillis() - collection.getStamp()) > TIMEOUT) {
                collection.reinit(imapFolder, DO_STATUS, DO_GETACL);
            }
            /*
             * Return
             */
            final ListLsubEntry listEntry = collection.getList(fullName);
            final ListLsubEntry lsubEntry = collection.getLsub(fullName);
            final ListLsubEntry emptyEntryFor = ListLsubCollection.emptyEntryFor(fullName);
            return new ListLsubEntry[] { listEntry == null ? emptyEntryFor : listEntry, lsubEntry == null ? emptyEntryFor : lsubEntry };
        }
    }

    private static ListLsubCollection getCollection(final int accountId, final IMAPFolder imapFolder, final Session session) throws MailException {
        /*
         * Initialize appropriate cache entry
         */
        final ListLsubCacheEntry entry = new ListLsubCacheEntry();
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accountId);
        mailCache.get(entry);
        ListLsubCollection collection = entry.getValue();
        if (null == collection) {
            final Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
            if (null == lock) {
                synchronized (session) {
                    mailCache.get(entry);
                    collection = entry.getValue();
                    if (null == collection) {
                        final ListLsubCollection newCol = new ListLsubCollection(imapFolder, DO_STATUS, DO_GETACL);
                        entry.setValue(newCol);
                        mailCache.put(entry);
                        collection = newCol;
                    }
                }
            } else {
                lock.lock();
                try {
                    mailCache.get(entry);
                    collection = entry.getValue();
                    if (null == collection) {
                        final ListLsubCollection newCol = new ListLsubCollection(imapFolder, DO_STATUS, DO_GETACL);
                        entry.setValue(newCol);
                        mailCache.put(entry);
                        collection = newCol;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        return collection;
    }

    /**
     * Gets child LIST entries for specified full name.
     * 
     * @param fullName The full name
     * @return The child LIST entries
     * @throws MailException
     */
    public static List<ListLsubEntry> getListChildren(final String fullName, final int accountId, final IMAPFolder imapFolder, final Session session) throws MailException {
        final ListLsubCollection collection = getCollection(accountId, imapFolder, session);
        synchronized (collection) {
            /*
             * Check collection's stamp
             */
            if ((System.currentTimeMillis() - collection.getStamp()) > TIMEOUT) {
                collection.reinit(imapFolder, DO_STATUS, DO_GETACL);
            }
            return collection.getListChildren(fullName);
        }
    }

    /**
     * Gets child LSUB entries for specified full name.
     * 
     * @param fullName The full name
     * @return The child LSUB entries
     * @throws MailException If a mail error occurs
     */
    public static List<ListLsubEntry> getLsubChildren(final String fullName, final int accountId, final IMAPFolder imapFolder, final Session session) throws MailException {
        final ListLsubCollection collection = getCollection(accountId, imapFolder, session);
        synchronized (collection) {
            /*
             * Check collection's stamp
             */
            if ((System.currentTimeMillis() - collection.getStamp()) > TIMEOUT) {
                collection.reinit(imapFolder, DO_STATUS, DO_GETACL);
            }
            return collection.getLsubChildren(fullName);
        }
    }

    /**
     * Checks for any subscribed subfolder.
     * 
     * @param fullName The full name
     * @return <code>true</code> if a subscribed subfolder exists; otherwise <code>false</code>
     * @throws MailException If a mail error occurs
     */
    public static boolean hasAnySubscribedSubfolder(final String fullName, final int accountId, final IMAPFolder imapFolder, final Session session) throws MailException {
        final ListLsubCollection collection = getCollection(accountId, imapFolder, session);
        synchronized (collection) {
            /*
             * Check collection's stamp
             */
            if ((System.currentTimeMillis() - collection.getStamp()) > TIMEOUT) {
                collection.reinit(imapFolder, DO_STATUS, DO_GETACL);
            }
            return collection.hasAnySubscribedSubfolder(fullName);
        }
    }

    private static final class ListLsubCacheEntry implements SessionMailCacheEntry<ListLsubCollection> {

        private volatile ListLsubCollection collection;

        private final CacheKey key;

        public ListLsubCacheEntry() {
            this(null);
        }

        public ListLsubCacheEntry(final ListLsubCollection collection) {
            super();
            this.collection = collection;
            final int code = MailCacheCode.LIST_LSUB.getCode();
            key = IMAPServiceRegistry.getService(CacheService.class).newCacheKey(code, code);
        }

        public CacheKey getKey() {
            return key;
        }

        public ListLsubCollection getValue() {
            return collection;
        }

        public void setValue(final ListLsubCollection collection) {
            this.collection = collection;
        }

        public Class<ListLsubCollection> getEntryClass() {
            return ListLsubCollection.class;
        }
    }

}
