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

package com.openexchange.push.dovecot;

import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageDelegator;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.dovecot.commands.RegistrationCommand;
import com.openexchange.push.dovecot.commands.UnregistrationCommand;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.push.dovecot.registration.RegistrationResult;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;


/**
 * {@link DefaultRegistrationPerformer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DefaultRegistrationPerformer implements RegistrationPerformer {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DefaultRegistrationPerformer.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DefaultRegistrationPerformer}.
     */
    public DefaultRegistrationPerformer(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public RegistrationResult initateRegistration(Session session) throws OXException {
        String logInfo = null;
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            MailService mailService = services.getOptionalService(MailService.class);
            if (null == mailService) {
                // Currently no MailService available
                return RegistrationResult.deniedRegistrationResult("Currently no MailService available");
            }

            // Connect it
            mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            mailAccess.connect(false);

            // Get IMAP store
            final IMAPStore imapStore = getImapFolderStorageFrom(mailAccess).getImapStore();
            logInfo = imapStore.toString();

            // Check capability
            if (!imapStore.hasCapability("METADATA")) {
                // No METADATA support
                LOGGER.info("No \"METADATA\" capability advertised for {}. Skipping listener registration.", imapStore);
                return RegistrationResult.deniedRegistrationResult("No \"METADATA\" capability supported");
            }

            // Proceed & grab INBOX folder
            final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder("INBOX");

            // Advertise registration at Dovecot IMAP server through SETMETADATA command
            Boolean result = (Boolean) imapFolder.doCommand(new RegistrationCommand(imapFolder, session));
            if (false == result.booleanValue()) {
                return RegistrationResult.deniedRegistrationResult("SETMETADATA command failed");
            }

            // Success
            return RegistrationResult.successRegistrationResult();
        } catch (FolderClosedException e) {
            return RegistrationResult.failedRegistrationResult(MimeMailException.handleMessagingException(e), true, logInfo);
        } catch (StoreClosedException e) {
            return RegistrationResult.failedRegistrationResult(MimeMailException.handleMessagingException(e), true, logInfo);
        } catch (MessagingException e) {
            return RegistrationResult.failedRegistrationResult(MimeMailException.handleMessagingException(e), true, logInfo);
        } catch (OXException e) {
            if (MimeMailExceptionCode.LOGIN_FAILED.equals(e)) {
                Throwable cause = null == e.getCause() ? e : e.getCause();
                return RegistrationResult.failedRegistrationResult(PushExceptionCodes.AUTHENTICATION_ERROR.create(cause, new Object[0]), false, logInfo);
            }
            return RegistrationResult.failedRegistrationResult(e, false, logInfo);
        } finally {
            closeMailAccess(mailAccess);
            mailAccess = null;
        }
    }

    @Override
    public void unregister(Session session) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            MailService mailService = services.getOptionalService(MailService.class);
            if (null != mailService) {
                // Connect it
                mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
                mailAccess.connect(false);

                // Get IMAP store & execute SETMETADATA for unregistration
                IMAPStore imapStore = getImapFolderStorageFrom(mailAccess).getImapStore();
                IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder("INBOX");
                imapFolder.doCommand(new UnregistrationCommand(imapFolder));
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (OXException e) {
            if (MimeMailExceptionCode.LOGIN_FAILED.equals(e)) {
                Throwable cause = null == e.getCause() ? e : e.getCause();
                throw PushExceptionCodes.AUTHENTICATION_ERROR.create(cause, new Object[0]);
            }
            throw e;
        } finally {
            closeMailAccess(mailAccess);
            mailAccess = null;
        }
    }

    private void closeMailAccess(final MailAccess<?, ?> mailAccess) {
        if (null != mailAccess) {
            try {
                mailAccess.close(false);
            } catch (final Exception x) {
                // Ignore
            }
        }
    }

    private IMAPFolderStorage getImapFolderStorageFrom(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        IMailFolderStorage fstore = mailAccess.getFolderStorage();
        if (!(fstore instanceof IMAPFolderStorage)) {
            if (!(fstore instanceof IMailFolderStorageDelegator)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation: " + fstore.getClass().getName());
            }
            fstore = ((IMailFolderStorageDelegator) fstore).getDelegateFolderStorage();
            if (!(fstore instanceof IMAPFolderStorage)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation: " + fstore.getClass().getName());
            }
        }
        return (IMAPFolderStorage) fstore;
    }

}
