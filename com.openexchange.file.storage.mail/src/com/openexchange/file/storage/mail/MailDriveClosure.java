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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.file.storage.mail;

import static com.openexchange.file.storage.mail.AbstractMailDriveResourceAccess.getIMAPStore;
import static com.openexchange.file.storage.mail.AbstractMailDriveResourceAccess.getImapMessageStorageFrom;
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
            return doPerform(getIMAPStore(mailAccess), mailAccess);
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw getImapMessageStorageFrom(mailAccess).handleMessagingException(e);
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            MailAccess.closeInstance(mailAccess);
        }
    }

}
