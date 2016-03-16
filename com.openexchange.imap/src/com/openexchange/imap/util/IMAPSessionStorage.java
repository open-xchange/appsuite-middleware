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

package com.openexchange.imap.util;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import javax.mail.MessagingException;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.mime.MimeMailException;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link IMAPSessionStorage} - An IMAP storage held by a session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class IMAPSessionStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPSessionStorage.class);

    private final Object lock;

    private final ConcurrentMap<AccAndFN, Set<IMAPUpdateableData>> dataMap;

    /**
     * Initializes a new {@link IMAPSessionStorage}.
     *
     * @param lock The lock object
     */
    IMAPSessionStorage(final Object lock) {
        super();
        dataMap = new NonBlockingHashMap<AccAndFN, Set<IMAPUpdateableData>>();
        this.lock = lock;
    }

    /**
     * Checks if session storage contains entries for given folder.
     *
     * @param accountId The account ID
     * @param imapFolder The IMAP folder
     * @return <code>true</code> if session storage contains entries for given folder; otherwise <code>false</code>
     */
    public boolean hasSessionStorage(final int accountId, final IMAPFolder imapFolder) {
        return (dataMap.containsKey(new AccAndFN(accountId, imapFolder.getFullName())));
    }

    /**
     * Fills storage with data fetched from specified IMAP folder.
     *
     * @param accountId The account ID
     * @param imapFolder The IMAP folder
     * @throws OXException If a mail error occurs
     */
    public void fillSessionStorage(final int accountId, final IMAPFolder imapFolder) throws OXException {
        final Set<IMAPUpdateableData> currentData;
        try {
            currentData = new HashSet<IMAPUpdateableData>(Arrays.asList(IMAPCommandsCollection.fetchUIDAndFlags(imapFolder)));
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
        final AccAndFN key = new AccAndFN(accountId, imapFolder.getFullName());
        Set<IMAPUpdateableData> data = dataMap.get(key);
        if (null == data) {
            data = dataMap.putIfAbsent(key, currentData);
            if (null != data) {
                data.addAll(currentData);
            }
        } else {
            data.addAll(currentData);
        }
    }

    /**
     * Gets IMAP messages newly created, formerly deleted or of which flags have been changed since specified time stamp.
     *
     * @param accountId The account ID
     * @param imapFolder The IMAP folder of which messages are examined
     * @param mode The mode; either <code>1</code> for new-and-modified only, <code>2</code> for deleted only, or <code>3</code> for
     *            new-and-modified and deleted
     * @param userId The user ID
     * @param contextId The context ID
     * @return The IMAP messages of which flags have been changed since specified time stamp
     * @throws OXException If a mail error occurs
     */
    public long[][] getChanges(final int accountId, final IMAPFolder imapFolder, final int mode, final int userId, final int contextId) throws OXException {
        synchronized (lock) {
            try {
                final String fullName = imapFolder.getFullName();
                // Load IMAP storage data
                final IMAPUpdateableData[] actualData = IMAPCommandsCollection.fetchUIDAndFlags(imapFolder);
                // Load DB storage data
                final AccAndFN key = new AccAndFN(accountId, fullName);
                Set<IMAPUpdateableData> sessionData = dataMap.get(key);
                if (null == sessionData) {
                    sessionData = new HashSet<IMAPUpdateableData>();
                    dataMap.put(key, sessionData);
                }
                // Generate UID sets
                final TLongSet actualUIDs = data2UIDSet(actualData);
                final TLongSet dbUIDs = data2UIDSet(sessionData);
                final TLongSet deleted;
                if (((mode & 2) > 0)) {
                    deleted = new TLongHashSet(dbUIDs.size());
                    /*
                     * Detect deleted UIDs
                     */
                    deleted.addAll(dbUIDs.toArray());
                    deleted.removeAll(actualUIDs.toArray());
                    /*
                     * Retain all which occur in session storage
                     */
                    final Set<IMAPUpdateableData> deletedData = new HashSet<IMAPUpdateableData>(Arrays.asList(filterByUIDs(
                        deleted,
                        sessionData)));
                    deleted.retainAll(data2UIDSet(deletedData).toArray());
                    /*
                     * Remove deleted ones from session??? If yes, this routine's result are only yielded per call and thus are not
                     * reproduceable
                     */
                    for (final Iterator<IMAPUpdateableData> iter = sessionData.iterator(); iter.hasNext();) {
                        final IMAPUpdateableData tmp = iter.next();
                        if (deleted.contains(tmp.getUid())) {
                            iter.remove();
                        }
                    }
                } else {
                    deleted = new TLongHashSet(0);
                }
                final TLongSet newAndModified;
                if (((mode & 1) > 0)) {
                    newAndModified = new TLongHashSet(actualUIDs.size());
                    /*
                     * Detect new UIDs
                     */
                    final TLongHashSet newUIDs = new TLongHashSet(actualUIDs);
                    newUIDs.removeAll(dbUIDs.toArray());
                    /*
                     * Insert new ones to session storage??? If yes, this routine's result are only yielded per call and thus are not
                     * reproduceable
                     */
                    sessionData.addAll(Arrays.asList(filterByUIDs(newUIDs, actualData)));
                    /*
                     * ... and add their UIDs to appropriate set
                     */
                    newAndModified.addAll(newUIDs.toArray());

                    /*
                     * Detect UIDs of changed messages
                     */
                    final TLongSet existing = new TLongHashSet(dbUIDs);
                    existing.retainAll(actualUIDs.toArray());
                    if (!existing.isEmpty()) {
                        final Set<IMAPUpdateableData> changedSessionData = new HashSet<IMAPUpdateableData>(Arrays.asList(filterByUIDs(
                            existing,
                            actualData)));
                        changedSessionData.removeAll(sessionData);
                        if (!changedSessionData.isEmpty()) {
                            /*
                             * Add UIDs of changed messages to appropriate set
                             */
                            final TLongSet changedUIDs = data2UIDSet(changedSessionData);
                            newAndModified.addAll(changedUIDs.toArray());
                            /*
                             * Write changes to DB storage??? If yes, this routine's result are only yielded per call and thus are not
                             * reproduceable
                             */
                            for (final Iterator<IMAPUpdateableData> iter = sessionData.iterator(); iter.hasNext();) {
                                final IMAPUpdateableData tmp = iter.next();
                                if (changedUIDs.contains(tmp.getUid())) {
                                    iter.remove();
                                }
                            }
                            sessionData.addAll(changedSessionData);
                        }
                    }
                } else {
                    newAndModified = new TLongHashSet(0);
                }
                /*
                 * Update caches if a change has been detected
                 */
                if (!newAndModified.isEmpty() || !deleted.isEmpty()) {
                    try {
                        MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, userId, contextId);
                    } catch (final Exception e) {
                        LOG.error("", e);
                    }
                }
                /*
                 * Return collected UIDs
                 */
                return new long[][] { newAndModified.toArray(), deleted.toArray() };
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            }
        }
    }

    /**
     * Removes specified deleted UIDs from session storage.
     *
     * @param deletedUIDs The set of deleted UIDs
     * @param accountId The account ID
     * @param fullName The IMAP folder's full name
     * @throws OXException If an error occurs while deleting UIDs
     */
    public void removeDeletedSessionData(final long[] deletedUIDs, final int accountId, final String fullName) {
        final Set<Long> s = new HashSet<Long>(deletedUIDs.length);
        for (int i = 0; i < deletedUIDs.length; i++) {
            s.add(Long.valueOf(deletedUIDs[i]));
        }
        removeDeletedSessionData(s, accountId, fullName);
    }

    /**
     * Removes specified deleted UIDs from session storage.
     *
     * @param deletedUIDs The set of deleted UIDs
     * @param accountId The account ID
     * @param fullName The IMAP folder's full name
     * @throws OXException If an error occurs while deleting UIDs
     */
    public void removeDeletedSessionData(final Set<Long> deletedUIDs, final int accountId, final String fullName) {
        synchronized (lock) {
            final AccAndFN key = new AccAndFN(accountId, fullName);
            Set<IMAPUpdateableData> sessionData = dataMap.get(key);
            if (null == sessionData) {
                sessionData = new HashSet<IMAPUpdateableData>();
                dataMap.put(key, sessionData);
            }
            for (final Iterator<IMAPUpdateableData> iter = sessionData.iterator(); iter.hasNext();) {
                final IMAPUpdateableData tmp = iter.next();
                if (deletedUIDs.contains(Long.valueOf(tmp.getUid()))) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Removes specified deleted UIDs from session storage.
     *
     * @param accountId The account ID
     * @param fullName The IMAP folder's full name
     * @throws OXException If an error occurs while deleting UIDs
     */
    public void removeDeletedFolder(final int accountId, final String fullName) {
        synchronized (lock) {
            final AccAndFN key = new AccAndFN(accountId, fullName);
            final Set<IMAPUpdateableData> sessionData = dataMap.get(key);
            if (null == sessionData) {
                return;
            }
            dataMap.remove(key);
        }
    }

    /*-
     * ############################ HELPER METHODS ############################
     */

    private static TLongHashSet data2UIDSet(final IMAPUpdateableData[] updateableDatas) {
        final TLongHashSet uids = new TLongHashSet(updateableDatas.length);
        for (int i = 0; i < updateableDatas.length; i++) {
            uids.add(updateableDatas[i].getUid());
        }
        return uids;
    }

    private static TLongHashSet data2UIDSet(final Collection<IMAPUpdateableData> updateableDatas) {
        final TLongHashSet uids = new TLongHashSet(updateableDatas.size());
        for (final IMAPUpdateableData updateableData : updateableDatas) {
            uids.add(updateableData.getUid());
        }
        return uids;
    }

    private static IMAPUpdateableData[] filterByUIDs(final TLongSet uids, final IMAPUpdateableData[] updateableDatas) {
        final List<IMAPUpdateableData> tmp = new ArrayList<IMAPUpdateableData>(uids.size());
        for (int i = 0; i < updateableDatas.length; i++) {
            final IMAPUpdateableData updateableData = updateableDatas[i];
            if (uids.contains(updateableData.getUid())) {
                tmp.add(updateableData);
            }
        }
        return tmp.toArray(new IMAPUpdateableData[tmp.size()]);
    }

    private static IMAPUpdateableData[] filterByUIDs(final TLongSet uids, final Collection<IMAPUpdateableData> updateableDatas) {
        final List<IMAPUpdateableData> tmp = new ArrayList<IMAPUpdateableData>(uids.size());
        for (final IMAPUpdateableData updateableData : updateableDatas) {
            if (uids.contains(updateableData.getUid())) {
                tmp.add(updateableData);
            }
        }
        return tmp.toArray(new IMAPUpdateableData[tmp.size()]);
    }

    /*-
     * ############################ HELPER CLASSES ############################
     */

    private static final class AccAndFN {

        private final int acc;

        private final String fn;

        public AccAndFN(final int acc, final String fn) {
            super();
            this.acc = acc;
            this.fn = fn;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + acc;
            result = prime * result + ((fn == null) ? 0 : fn.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof AccAndFN)) {
                return false;
            }
            final AccAndFN other = (AccAndFN) obj;
            if (acc != other.acc) {
                return false;
            }
            if (fn == null) {
                if (other.fn != null) {
                    return false;
                }
            } else if (!fn.equals(other.fn)) {
                return false;
            }
            return true;
        }
    }
}
