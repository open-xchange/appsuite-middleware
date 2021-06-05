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

    private static IMAPSessionStorage ensureExistence(Session session) {
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
    public static boolean hasSessionStorage(int accountId, IMAPFolder imapFolder, Session session) {
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
    public static void fillSessionStorage(int accountId, IMAPFolder imapFolder, Session session) throws OXException {
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
    public static long[][] getChanges(int accountId, IMAPFolder imapFolder, Session session, int mode) throws OXException {
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
    public static void removeDeletedSessionData(long[] deletedUIDs, int accountId, Session session, String fullName) {
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
    public static void removeDeletedSessionData(Set<Long> deletedUIDs, int accountId, Session session, String fullName) {
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
    public static void removeDeletedFolder(int accountId, Session session, String fullName) {
        if (!ENABLED) {
            return;
        }
        ensureExistence(session).removeDeletedFolder(accountId, fullName);
    }

}
