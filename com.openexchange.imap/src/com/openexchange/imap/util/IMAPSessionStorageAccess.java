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

import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link IMAPSessionStorageAccess} - IMAP utility class for session storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPSessionStorageAccess {

    private static final boolean ENABLED = false;

    /**
     * Initializes a new {@link IMAPSessionStorageAccess}.
     */
    private IMAPSessionStorageAccess() {
        super();
    }

    /**
     * Checks if enabled.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    public static boolean isEnabled() {
        return ENABLED;
    }

    private static IMAPSessionStorage ensureExistence(final Session session) {
        return new IMAPSessionStorage(session);
    }

    /**
     * Checks if session storage contains entries for given folder.
     *
     * @param accountId The account ID
     * @param imapFolder The IMAP folder
     * @param session The session providing user data
     * @return <code>true</code> if session storage contains entries for given folder; otherwise <code>false</code>
     */
    public static boolean hasSessionStorage(final int accountId, final IMAPFolder imapFolder, final Session session) {
        if (!ENABLED) {
            return true;
        }
        return ensureExistence(session).hasSessionStorage(accountId, imapFolder);
    }

    /**
     * Fills session storage with data fetched from specified IMAP folder.
     *
     * @param accountId The account ID
     * @param imapFolder The IMAP folder
     * @param session The session providing user data
     * @throws OXException If a mail error occurs
     */
    public static void fillSessionStorage(final int accountId, final IMAPFolder imapFolder, final Session session) throws OXException {
        if (!ENABLED) {
            return;
        }
        ensureExistence(session).fillSessionStorage(accountId, imapFolder);
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
     * @throws OXException If a mail error occurs
     */
    public static long[][] getChanges(final int accountId, final IMAPFolder imapFolder, final Session session, final int mode) throws OXException {
        if (!ENABLED) {
            return new long[][] { new long[] {}, new long[] {} };
        }
        return ensureExistence(session).getChanges(accountId, imapFolder, mode, session.getUserId(), session.getContextId());
    }

    /**
     * Removes specified deleted UIDs from session storage.
     *
     * @param deletedUIDs The set of deleted UIDs
     * @param accountId The account ID
     * @param session The session
     * @param fullName The IMAP folder's full name
     * @throws OXException If an error occurs while deleting UIDs
     */
    public static void removeDeletedSessionData(final long[] deletedUIDs, final int accountId, final Session session, final String fullName) {
        if (!ENABLED) {
            return;
        }
        ensureExistence(session).removeDeletedSessionData(deletedUIDs, accountId, fullName);
    }

    /**
     * Removes specified deleted UIDs from session storage.
     *
     * @param deletedUIDs The set of deleted UIDs
     * @param accountId The account ID
     * @param session The session
     * @param fullName The IMAP folder's full name
     * @throws OXException If an error occurs while deleting UIDs
     */
    public static void removeDeletedSessionData(final Set<Long> deletedUIDs, final int accountId, final Session session, final String fullName) {
        if (!ENABLED) {
            return;
        }
        ensureExistence(session).removeDeletedSessionData(deletedUIDs, accountId, fullName);
    }

    /**
     * Removes specified deleted UIDs from session storage.
     *
     * @param accountId The account ID
     * @param session The session
     * @param fullName The IMAP folder's full name
     * @throws OXException If an error occurs while deleting UIDs
     */
    public static void removeDeletedFolder(final int accountId, final Session session, final String fullName) {
        if (!ENABLED) {
            return;
        }
        ensureExistence(session).removeDeletedFolder(accountId, fullName);
    }

}
