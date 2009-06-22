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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.mail.MailException;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link IMAPSessionUtility} - IMAP utility class for session.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPSessionUtility {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IMAPSessionUtility.class);

    /**
     * Initializes a new {@link IMAPSessionUtility}.
     */
    private IMAPSessionUtility() {
        super();
    }

    /**
     * Fills session storage with data fetched from specified IMAP folder.
     * 
     * @param accountId The account ID
     * @param imapFolder The IMAP folder
     * @param key The session key
     * @param session The session providing user data
     * @throws MailException If a mail error occurs
     */
    public static void fillSessionStorage(final int accountId, final IMAPFolder imapFolder, final String key, final Session session) throws MailException {
        synchronized (session) {
            final Set<IMAPUpdateableData> sessionData = new HashSet<IMAPUpdateableData>();
            session.setParameter(key, sessionData);
            try {
                sessionData.addAll(Arrays.asList(IMAPCommandsCollection.fetchUIDAndFlags(imapFolder)));
            } catch (final MessagingException e) {
                throw MIMEMailException.handleMessagingException(e);
            }
        }
    }

    /**
     * Gets IMAP messages newly created, formerly deleted or of which flags have been changed since specified time stamp.
     * 
     * @param accountId The account ID
     * @param imapFolder The IMAP folder of which messages are examined
     * @param session The session providing user data
     * @param mode The mode; either <code>1</code> for new-and-modified only, <code>2</code> for deleted only, or <code>3</code> for
     *            new-and-modified and deleted
     * @return The IMAP messages of which flags have been changed since specified time stamp
     * @throws MailException If a mail error occurs
     */
    public static long[][] getChanges(final int accountId, final IMAPFolder imapFolder, final Session session, final int mode) throws MailException {
        synchronized (session) {
            try {
                final String fullName = imapFolder.getFullName();
                // Load IMAP storage data
                final IMAPUpdateableData[] actualData = IMAPCommandsCollection.fetchUIDAndFlags(imapFolder);
                // Load DB storage data
                final String key = getSessionKey(accountId, fullName);
                final Set<IMAPUpdateableData> sessionData;
                {
                    final Object parameter = session.getParameter(key);
                    if (null == parameter) {
                        sessionData = new HashSet<IMAPUpdateableData>();
                        session.setParameter(key, sessionData);
                    } else {
                        sessionData = (Set<IMAPUpdateableData>) parameter;
                    }
                }
                // Generate UID sets
                final Set<Long> actualUIDs = data2UIDSet(actualData);
                final Set<Long> dbUIDs = data2UIDSet(sessionData);
                final Set<Long> deleted;
                if (((mode & 2) > 0)) {
                    deleted = new HashSet<Long>(dbUIDs.size());
                    /*
                     * Detect deleted UIDs
                     */
                    deleted.addAll(dbUIDs);
                    deleted.removeAll(actualUIDs);
                    /*
                     * Retain all which occur in session storage
                     */
                    final Set<IMAPUpdateableData> deletedData = new HashSet<IMAPUpdateableData>(Arrays.asList(filterByUIDs(
                        deleted,
                        sessionData)));
                    deleted.retainAll(data2UIDSet(deletedData));
                    /*
                     * Remove deleted ones from session??? If yes, this routine's result are only yielded per call and thus are not
                     * reproduceable
                     */
                    for (final Iterator<IMAPUpdateableData> iter = sessionData.iterator(); iter.hasNext();) {
                        final IMAPUpdateableData tmp = iter.next();
                        if (deleted.contains(Long.valueOf(tmp.getUid()))) {
                            iter.remove();
                        }
                    }
                } else {
                    deleted = Collections.emptySet();
                }
                final Set<Long> newAndModified;
                if (((mode & 1) > 0)) {
                    newAndModified = new HashSet<Long>(actualUIDs.size());
                    /*
                     * Detect new UIDs
                     */
                    final Set<Long> newUIDs = new HashSet<Long>(actualUIDs);
                    newUIDs.removeAll(dbUIDs);
                    /*
                     * Insert new ones to session storage??? If yes, this routine's result are only yielded per call and thus are not
                     * reproduceable
                     */
                    sessionData.addAll(Arrays.asList(filterByUIDs(newUIDs, actualData)));
                    /*
                     * ... and add their UIDs to appropriate set
                     */
                    newAndModified.addAll(newUIDs);

                    /*
                     * Detect UIDs of changed messages
                     */
                    final Set<Long> existing = new HashSet<Long>(dbUIDs);
                    existing.retainAll(actualUIDs);
                    if (!existing.isEmpty()) {
                        final Set<IMAPUpdateableData> changedSessionData = new HashSet<IMAPUpdateableData>(Arrays.asList(filterByUIDs(
                            existing,
                            actualData)));
                        changedSessionData.removeAll(sessionData);
                        if (!changedSessionData.isEmpty()) {
                            /*
                             * Add UIDs of changed messages to appropriate set
                             */
                            final Set<Long> changedUIDs = data2UIDSet(changedSessionData);
                            newAndModified.addAll(changedUIDs);
                            /*
                             * Write changes to DB storage??? If yes, this routine's result are only yielded per call and thus are not
                             * reproduceable
                             */
                            for (final Iterator<IMAPUpdateableData> iter = sessionData.iterator(); iter.hasNext();) {
                                final IMAPUpdateableData tmp = iter.next();
                                if (changedUIDs.contains(Long.valueOf(tmp.getUid()))) {
                                    iter.remove();
                                }
                            }
                            sessionData.addAll(changedSessionData);
                        }
                    }
                } else {
                    newAndModified = Collections.emptySet();
                }
                /*
                 * Update caches if a change has been detected
                 */
                if (!newAndModified.isEmpty() || !deleted.isEmpty()) {
                    try {
                        MailMessageCache.getInstance().removeFolderMessages(
                            accountId,
                            fullName,
                            session.getUserId(),
                            session.getContextId());
                    } catch (final Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                /*
                 * Return collected UIDs
                 */
                return new long[][] { collection2array(newAndModified), collection2array(deleted) };
            } catch (final MessagingException e) {
                throw MIMEMailException.handleMessagingException(e);
            }
        }
    }

    /**
     * Removes specified deleted UIDs from session storage.
     * 
     * @param deletedUIDs The set of deleted UIDs
     * @param accountId The account ID
     * @param session The session
     * @param fullName The IMAP folder's full name
     * @throws MailException If an error occurs while deleting UIDs
     */
    public static void removeDeletedSessionData(final long[] deletedUIDs, final int accountId, final Session session, final String fullName) {
        final Set<Long> s = new HashSet<Long>(deletedUIDs.length);
        for (int i = 0; i < deletedUIDs.length; i++) {
            s.add(Long.valueOf(deletedUIDs[i]));
        }
        removeDeletedSessionData(s, accountId, session, fullName);
    }

    /**
     * Removes specified deleted UIDs from session storage.
     * 
     * @param deletedUIDs The set of deleted UIDs
     * @param accountId The account ID
     * @param session The session
     * @param fullName The IMAP folder's full name
     * @throws MailException If an error occurs while deleting UIDs
     */
    public static void removeDeletedSessionData(final Set<Long> deletedUIDs, final int accountId, final Session session, final String fullName) {
        synchronized (session) {
            final String key = getSessionKey(accountId, fullName);
            final Set<IMAPUpdateableData> sessionData;
            {
                final Object parameter = session.getParameter(key);
                if (null == parameter) {
                    sessionData = new HashSet<IMAPUpdateableData>();
                    session.setParameter(key, sessionData);
                    return;
                }
                sessionData = (Set<IMAPUpdateableData>) parameter;
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
     * @param session The session
     * @param fullName The IMAP folder's full name
     * @throws MailException If an error occurs while deleting UIDs
     */
    public static void removeDeletedFolder(final int accountId, final Session session, final String fullName) {
        synchronized (session) {
            final String key = getSessionKey(accountId, fullName);
            final Object parameter = session.getParameter(key);
            if (null == parameter) {
                return;
            }
            session.setParameter(key, null);
        }
    }

    private static Set<Long> data2UIDSet(final IMAPUpdateableData[] updateableDatas) {
        final Set<Long> uids = new HashSet<Long>(updateableDatas.length);
        for (int i = 0; i < updateableDatas.length; i++) {
            uids.add(Long.valueOf(updateableDatas[i].getUid()));
        }
        return uids;
    }

    private static Set<Long> data2UIDSet(final Collection<IMAPUpdateableData> updateableDatas) {
        final Set<Long> uids = new HashSet<Long>(updateableDatas.size());
        for (final IMAPUpdateableData updateableData : updateableDatas) {
            uids.add(Long.valueOf(updateableData.getUid()));
        }
        return uids;
    }

    private static IMAPUpdateableData[] filterByUIDs(final Set<Long> uids, final IMAPUpdateableData[] updateableDatas) {
        final List<IMAPUpdateableData> tmp = new ArrayList<IMAPUpdateableData>(uids.size());
        for (int i = 0; i < updateableDatas.length; i++) {
            final IMAPUpdateableData updateableData = updateableDatas[i];
            if (uids.contains(Long.valueOf(updateableData.getUid()))) {
                tmp.add(updateableData);
            }
        }
        return tmp.toArray(new IMAPUpdateableData[tmp.size()]);
    }

    private static IMAPUpdateableData[] filterByUIDs(final Set<Long> uids, final Collection<IMAPUpdateableData> updateableDatas) {
        final List<IMAPUpdateableData> tmp = new ArrayList<IMAPUpdateableData>(uids.size());
        for (final IMAPUpdateableData updateableData : updateableDatas) {
            if (uids.contains(Long.valueOf(updateableData.getUid()))) {
                tmp.add(updateableData);
            }
        }
        return tmp.toArray(new IMAPUpdateableData[tmp.size()]);
    }

    private static long[] collection2array(final Collection<Long> collection) {
        final long[] longs = new long[collection.size()];
        int i = 0;
        for (final Long lg : collection) {
            longs[i++] = lg.longValue();
        }
        return longs;
    }

    /**
     * Generates the session key for given arguments.
     * 
     * @param accountId The account ID
     * @param fullName The full name
     * @return The session key for given arguments
     */
    public static String getSessionKey(final int accountId, final String fullName) {
        return new StringBuilder(32).append("imap.data@").append(accountId).append('@').append(fullName).toString();
    }

}
