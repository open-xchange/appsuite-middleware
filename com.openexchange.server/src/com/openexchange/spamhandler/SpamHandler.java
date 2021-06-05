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

package com.openexchange.spamhandler;

import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;

/**
 * {@link SpamHandler} - The abstract Spam handler class used by mail module to handle Spam-related actions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class SpamHandler {

    /**
     * The fallback spam handler
     */
    public static final String SPAM_HANDLER_FALLBACK = "NoSpamHandler";

    /**
     * The fullname of the INBOX folder
     */
    protected static final String FULLNAME_INBOX = "INBOX";

    private final int hashCode;

    /**
     * Initializes a new {@link SpamHandler}
     */
    protected SpamHandler() {
        super();
        hashCode = getSpamHandlerName().hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SpamHandler)) {
            return false;
        }

        SpamHandler other = (SpamHandler) obj;
        String myName = getSpamHandlerName();
        if (myName == null) {
            if (other.getSpamHandlerName() != null) {
                return false;
            }
        } else if (!myName.equals(other.getSpamHandlerName())) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    /**
     * Indicates whether to create the confirmed-spam folder during check for default mail folders during login if Spam is enabled for
     * logged-in user.<br>
     * Default is <code>true</code>.
     * <p>
     * Implementations may override this method to change the default behavior.
     *
     * @param session The associated session to check for
     * @return <code>true</code> to create the confirmed-spam folder during check for default mail folders; otherwise <code>false</code> to
     *         not create the folder
     * @throws OXException If check fails
     */
    public boolean isCreateConfirmedSpam(Session session) throws OXException {
        return true;
    }

    /**
     * Indicates whether to create the confirmed-ham folder during check for default mail folders during login if Spam is enabled for
     * logged-in user.<br>
     * Default is <code>true</code>.
     * <p>
     * Implementations may override this method to change the default behavior.
     *
     * @param session The associated session to check for
     * @return <code>true</code> to create the confirmed-ham folder during check for default mail folders; otherwise <code>false</code> to
     *         not create the folder
     * @throws OXException If check fails
     */
    public boolean isCreateConfirmedHam(Session session) throws OXException {
        return true;
    }

    /**
     * Indicates whether the confirmed-spam/confirmed-ham folders shall automatically be unsubscribed during login.<br>
     * Default is <code>true</code>.
     * <p>
     * Implementations may override this method to change the default behavior.
     *
     * @param session The associated session to check for
     * @return <code>true</code> to automatically unsubscribe the confirmed-spam/confirmed-ham folders; otherwise <code>false</code> to
     *         leave subscription status unchanged.
     * @throws OXException If check fails
     */
    public boolean isUnsubscribeSpamFolders(Session session) throws OXException {
        return true;
    }

    /**
     * Handles messages that should be treated as Spam messages. This means to copy the mails identified by specified mail IDs to the
     * defined confirmed Spam folder to properly teach the Spam system to handle these mails as Spam.
     * <p>
     * This method may be overridden if another Spam handling is desired.
     *
     * @param accountId The account ID
     * @param fullName The full name of the folder containing Spam messages
     * @param mailIDs The mail IDs
     * @param move If <code>true</code> the mails identified by specified mail IDs are supposed to be moved to Spam folder; otherwise the mails remain in
     *            the source folder.
     * @param session The session providing needed user data
     * @throws OXException If handling Spam fails
     */
    public void handleSpam(final int accountId, final String fullName, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        /*
         * Copy to confirmed spam folder
         */
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            if (isCreateConfirmedSpam(session)) {
                final String confirmedSpamFullname = mailAccess.getFolderStorage().getConfirmedSpamFolder();
                mailAccess.getMessageStorage().copyMessages(fullName, confirmedSpamFullname, mailIDs, true);
            }
            if (move) {
                /*
                 * Move to spam folder
                 */
                final String spamFullname = mailAccess.getFolderStorage().getSpamFolder();
                mailAccess.getMessageStorage().moveMessages(fullName, spamFullname, mailIDs, true);
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    /**
     * Gets the spam handler name which is used on registration
     *
     * @return The spam handler name
     */
    public abstract String getSpamHandlerName();

    /**
     * Handles messages that are located in Spam folder but should be treated as ham messages. This means to copy the mails identified by
     * specified mail IDs to the defined confirmed ham folder to properly teach the Spam system to handle these mails as ham.
     * <p>
     * Dependent on the used Spam system, the spam messages cannot be copied/moved as they are, but need to be parsed in the way the Spam
     * system wraps Spam messages. If Spam system does not wrap original messages, then the default Spam handler is supposed to be used.
     *
     * @param accountId The account ID
     * @param spamFullName The Spam folder's full name
     * @param mailIDs The mail IDs
     * @param move If <code>true</code> the mails identified by specified mail IDs are moved to INBOX folder; otherwise the mails remain in
     *            Spam folder
     * @param session The session providing needed user data
     * @throws OXException If handling ham fails
     */
    public abstract void handleHam(int accountId, String spamFullName, String[] mailIDs, boolean move, Session session) throws OXException;

}
