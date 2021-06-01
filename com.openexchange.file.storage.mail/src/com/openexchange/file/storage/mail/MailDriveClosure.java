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

package com.openexchange.file.storage.mail;

import java.io.IOException;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.mail.accesscontrol.AccessControl;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link MailDriveClosure}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class MailDriveClosure<R> {

    /**
     * Initializes a new {@link MailDriveClosure}.
     */
    public MailDriveClosure() {
        super();
    }

    /**
     * Performs the actual operation
     *
     * @param imapStore The connected IMAP store to use
     * @param mailAccess The backing mail access
     * @return The return value
     * @throws OXException If an Open-Xchange error occurred
     * @throws MessagingException If a messaging error occurs
     * @throws IOException If an I/O error occurred
     */
    protected abstract R doPerform(IMAPStore imapStore, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, MessagingException, IOException;

    /**
     * Performs this closure's operation.
     *
     * @param session The associated session
     * @param httpClient The HTTP client to use
     * @return The return value
     * @throws OXException If operation fails
     */
    public R perform(Session session) throws OXException {
        AccessControl accessControl = AccessControl.getAccessControl(session);
        try {
            accessControl.acquireGrant();
            return innerPerform(session);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, new Object[0]);
        } finally {
            accessControl.close();
        }
    }

    private R innerPerform(Session session) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();
            return doPerform(com.openexchange.imap.IMAPAccess.getIMAPStoreFrom(mailAccess), mailAccess);
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw com.openexchange.imap.IMAPAccess.getImapMessageStorageFrom(mailAccess).handleMessagingException(e);
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            MailAccess.closeInstance(mailAccess);
        }
    }

}
