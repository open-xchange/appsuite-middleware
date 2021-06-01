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

package com.openexchange.push.dovecot;

import static com.openexchange.java.Autoboxing.I;
import javax.mail.FolderClosedException;
import javax.mail.MessagingException;
import javax.mail.StoreClosedException;
import org.slf4j.Logger;
import com.openexchange.dovecot.doveadm.client.DefaultDoveAdmCommand;
import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.dovecot.doveadm.client.DoveAdmCommand;
import com.openexchange.dovecot.doveadm.client.DoveAdmDataResponse;
import com.openexchange.dovecot.doveadm.client.DoveAdmResponse;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPAccess;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.dovecot.commands.CheckExistenceCommand;
import com.openexchange.push.dovecot.commands.RegistrationCommand;
import com.openexchange.push.dovecot.commands.UnregistrationCommand;
import com.openexchange.push.dovecot.registration.RegistrationContext;
import com.openexchange.push.dovecot.registration.RegistrationPerformer;
import com.openexchange.push.dovecot.registration.RegistrationResult;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
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

    private String getLoginFor(RegistrationContext registrationContext, DoveAdmClient doveAdmClient) throws OXException {
        int userId = registrationContext.getUserId();
        int contextId = registrationContext.getContextId();

        MailAccountStorageService mailAccountService = services.getOptionalService(MailAccountStorageService.class);
        if (null == mailAccountService) {
            throw ServiceExceptionCode.absentService(MailAccountStorageService.class);
        }
        MailAccount defaultMailAccount = mailAccountService.getDefaultMailAccount(userId, contextId);

        User user;
        if (registrationContext.getSession() instanceof ServerSession) {
            user = ((ServerSession) registrationContext.getSession()).getUser();
        } else {
            UserService userService = services.getOptionalService(UserService.class);
            if (null == userService) {
                throw ServiceExceptionCode.absentService(UserService.class);
            }
            user = userService.getUser(userId, contextId);
        }

        String login = MailConfig.getMailLogin(defaultMailAccount, user.getLoginInfo(), userId, contextId);
        return doveAdmClient.checkUser(login, userId, contextId);
    }

    private String generateIdFor(RegistrationContext registrationContext) {
        return new StringBuilder(16).append(registrationContext.getUserId()).append('@').append(registrationContext.getContextId()).toString();
    }

    @Override
    public RegistrationResult initateRegistration(RegistrationContext registrationContext) throws OXException {
        DoveAdmClient doveAdmClient = registrationContext.isDoveAdmBased() ? registrationContext.getDoveAdmClient() : null;
        if (null == doveAdmClient) {
            return initateRegistration(registrationContext.getSession());
        }

        String user = getLoginFor(registrationContext, doveAdmClient);
        String userInfo = generateIdFor(registrationContext);
        String valueToSet = "user=" + userInfo;

        // Check if there is already such a registration
        DoveAdmCommand command = craftMailboxMetadataGetCommandUsing("get-" + userInfo, user);
        DoveAdmResponse response = doveAdmClient.executeCommand(command);
        if (response.isError()) {
            OXException oexc = PushExceptionCodes.UNEXPECTED_ERROR.create("Failed to check for existent Dovecot Push for user " + registrationContext.getUserId() + " in context " + registrationContext.getContextId() + ": " + response.toString());
            return RegistrationResult.failedRegistrationResult(oexc, true, null);
        }
        DoveAdmDataResponse dataResponse = response.asDataResponse();
        String value = dataResponse.getResults().get(0).getValue("value");
        if (valueToSet.equals(value)) {
            LOGGER.debug("Successfully detected an existent Dovecot Push for user {} in context {}", I(registrationContext.getUserId()), I(registrationContext.getContextId()));
            return RegistrationResult.successRegistrationResult();
        }

        // No such registration available. Set it...
        command = craftMailboxMetadataSetCommandUsing(valueToSet, "reg-" + userInfo, user);
        response = doveAdmClient.executeCommand(command);
        if (response.isError()) {
            OXException oexc = PushExceptionCodes.UNEXPECTED_ERROR.create("Failed to register Dovecot Push for user " + registrationContext.getUserId() + " in context " + registrationContext.getContextId() + ": " + response.toString());
            return RegistrationResult.failedRegistrationResult(oexc, true, null);
        }

        LOGGER.debug("Successfully registered Dovecot Push for user {} in context {}", I(registrationContext.getUserId()), I(registrationContext.getContextId()));
        return RegistrationResult.successRegistrationResult();
    }

    private RegistrationResult initateRegistration(Session session) {
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
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess1 = mailAccess;

            // Get IMAP store
            final IMAPStore imapStore = IMAPAccess.getIMAPFolderStorageFrom(mailAccess1).getImapStore();
            logInfo = imapStore.toString();

            // Check capability
            if (!imapStore.hasCapability("METADATA")) {
                // No METADATA support
                LOGGER.info("No \"METADATA\" capability advertised for {}. Skipping listener registration.", imapStore);
                return RegistrationResult.deniedRegistrationResult("No \"METADATA\" capability supported");
            }

            // Proceed & grab INBOX folder
            final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder("INBOX");

            Boolean alreadyExists = (Boolean) imapFolder.doCommand(new CheckExistenceCommand(imapFolder, session));
            if (false == alreadyExists.booleanValue()) {
                // Advertise registration at Dovecot IMAP server through SETMETADATA command
                Boolean result = (Boolean) imapFolder.doCommand(new RegistrationCommand(imapFolder, session));
                if (false == result.booleanValue()) {
                    return RegistrationResult.deniedRegistrationResult("SETMETADATA command failed");
                }
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
    public void unregister(RegistrationContext registrationContext) throws OXException {
        DoveAdmClient doveAdmClient = registrationContext.isDoveAdmBased() ? registrationContext.getDoveAdmClient() : null;
        if (null == doveAdmClient) {
            unregister(registrationContext.getSession());
            return;
        }

        String user = getLoginFor(registrationContext, doveAdmClient);
        DoveAdmCommand command = craftMailboxMetadataUnsetCommandUsing("unreg-" + generateIdFor(registrationContext), user);
        DoveAdmResponse response = doveAdmClient.executeCommand(command);
        if (response.isError()) {
            LOGGER.warn("Failed to unregister Dovecot Push for user {} in context {}: {}", I(registrationContext.getUserId()), I(registrationContext.getContextId()), response.toString());
            return;
        }

        LOGGER.debug("Successfully unregistered Dovecot Push for user {} in context {}", I(registrationContext.getUserId()), I(registrationContext.getContextId()));
    }

    private void unregister(Session session) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            MailService mailService = services.getOptionalService(MailService.class);
            if (null != mailService) {
                // Connect it
                mailAccess = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
                mailAccess.connect(false);
                final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess1 = mailAccess;

                // Get IMAP store & execute SETMETADATA for unregistration
                IMAPStore imapStore = IMAPAccess.getIMAPFolderStorageFrom(mailAccess1).getImapStore();
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
            } catch (Exception x) {
                // Ignore
            }
        }
    }

    private DoveAdmCommand craftMailboxMetadataSetCommandUsing(String value, String commandId, String user) {
        return DefaultDoveAdmCommand.builder()
            .command("mailboxMetadataSet")
            .optionalIdentifier(commandId)
            .setParameter("user", user)
            .setParameter("mailbox", "")
            .setParameter("allowEmptyMailboxName", true)
            .setParameter("key", "/private/vendor/vendor.dovecot/http-notify")
            .setParameter("value", value)
            .build();
    }

    private DoveAdmCommand craftMailboxMetadataUnsetCommandUsing(String commandId, String user) {
        return DefaultDoveAdmCommand.builder()
            .command("mailboxMetadataUnset")
            .optionalIdentifier(commandId)
            .setParameter("user", user)
            .setParameter("mailbox", "")
            .setParameter("allowEmptyMailboxName", true)
            .setParameter("key", "/private/vendor/vendor.dovecot/http-notify")
            .build();
    }

    private DoveAdmCommand craftMailboxMetadataGetCommandUsing(String commandId, String user) {
        return DefaultDoveAdmCommand.builder()
            .command("mailboxMetadataGet")
            .optionalIdentifier(commandId)
            .setParameter("user", user)
            .setParameter("mailbox", "")
            .setParameter("allowEmptyMailboxName", true)
            .setParameter("key", "/private/vendor/vendor.dovecot/http-notify")
            .build();
    }

}
