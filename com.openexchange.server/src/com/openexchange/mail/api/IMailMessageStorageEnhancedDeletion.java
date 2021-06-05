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

package com.openexchange.mail.api;

import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;

/**
 * {@link IMailMessageStorageEnhancedDeletion} - Extends basic folder storage by requesting a mailbox' conversation threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailMessageStorageEnhancedDeletion extends IMailMessageStorage {

    /**
     * Indicates if enhanced deletion is supported.
     *
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isEnhancedDeletionSupported() throws OXException;

    /**
     * Deletes the messages located in given folder identified through given mail IDs.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     *
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @param hardDelete <code>true</code> to hard delete the messages, meaning not to create a backup copy of each message in default trash folder; otherwise <code>false</code>
     * @return The identifiers of those mails that were put into trash (if any)
     * @throws OXException If messages cannot be deleted.
     */
    MailPath[] deleteMessagesEnhanced(String folder, String[] mailIds, boolean hardDelete) throws OXException;

    /**
     * (Hard) Deletes the messages located in given folder identified through given mail IDs.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     *
     * @param folder The folder full name
     * @param mailIds The mail IDs
     * @return The identifiers of those mails that were removed (if any)
     * @throws OXException If messages cannot be deleted.
     */
    MailPath[] hardDeleteMessages(String folder, String[] mailIds) throws OXException;

}
