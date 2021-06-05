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

package com.openexchange.spamhandler.spamexperts;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.MailAccounts;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.spamexperts.exceptions.SpamExpertsExceptionCode;
import com.openexchange.spamhandler.spamexperts.management.SpamExpertsConfig;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;


/**
 * {@link SpamExpertsSpamHandler}
 *
 */
public class SpamExpertsSpamHandler extends SpamHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SpamExpertsSpamHandler.class);

    private final ServiceLookup services;
    private final SpamExpertsConfig config;

    /**
     * Initializes a new {@link SpamExpertsSpamHandler}.
     */
    public SpamExpertsSpamHandler(SpamExpertsConfig config, ServiceLookup services) {
        super();
        this.config = config;
        this.services = services;
    }

    @Override
    public String getSpamHandlerName() {
        return "SpamExperts";
    }

    private void copyToSpamexpertsFolder(String folder, MailMessage[] messages, Session session) throws OXException {
        if (null == messages) {
            throw SpamExpertsExceptionCode.UNABLE_TO_GET_MAILS.create();
        }

        SSLSocketFactoryProvider factoryProvider = services.getService(SSLSocketFactoryProvider.class);
        String socketFactoryClass = factoryProvider.getDefault().getClass().getName();

        URI imapUrl = config.getImapURL(session);

        String imapUser = config.requireProperty(session, "com.openexchange.custom.spamexperts.imapuser");
        String imapPassword = config.requireProperty(session, "com.openexchange.custom.spamexperts.imappassword");

        final Properties props = MimeDefaultSession.getDefaultMailProperties();
        if ("imaps".equals(imapUrl.getScheme())) {
            props.put("mail.imap.socketFactory.class", socketFactoryClass);
        } else {
            props.put("mail.imap.ssl.socketFactory.class", socketFactoryClass);
            props.put("mail.imap.starttls.enable", "true");
        }

        props.put("mail.imap.socketFactory.port", Integer.toString(imapUrl.getPort()));
        props.put("mail.imap.socketFactory.fallback", "false");

        if (isPrimaryImapAccount(imapUrl.getHost(), imapUrl.getPort(), session.getUserId(), session.getContextId())) {
            props.put("mail.imap.primary", "true");
        }

        javax.mail.Session imapSession = javax.mail.Session.getInstance(props, null);

        IMAPStore imapStore = null;
        try {
            imapStore = (IMAPStore) imapSession.getStore("imap");
            imapStore.connect(imapUrl.getHost(), imapUrl.getPort(), imapUser, imapPassword);
            IMAPFolder sf = (IMAPFolder) imapStore.getFolder(folder);
            if (!sf.exists()) {
                throw SpamExpertsExceptionCode.FOLDER_DOES_NOT_EXIST.create(folder);
            }

            MimeMessage[] sfmesgs = new MimeMessage[messages.length];
            for (int i = 0; i < messages.length; i++) {
                MailMessage mail = messages[i];
                if (null != mail) {
                    sfmesgs[i] = MimeMessageUtility.newMimeMessage(MimeMessageUtility.getStreamFromMailPart(mail), null);
                }
            }

            sf.appendMessages(sfmesgs);
        } catch (MessagingException e) {
            LOG.error("", e);
            throw new OXException(e);
        } catch (OXException e) {
            LOG.error("", e);
            throw e;
        } finally {
            if ((null != imapStore) && imapStore.isConnected()) {
                try {
                    imapStore.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Checks if given host/port denote the primary IMAP account of specified user.
     *
     * @param host The host
     * @param port The port
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if given host/port denote the primary IMAP account; otherwise <code>false</code>
     */
    private boolean isPrimaryImapAccount(String host, int port, int userId, int contextId) {
        try {
            MailAccountStorageService storageService = services.getOptionalService(MailAccountStorageService.class);
            if (storageService == null) {
                return false;
            }

            MailAccount defaultMailAccount = storageService.getDefaultMailAccount(userId, contextId);
            return MailAccounts.isEqualImapAccount(defaultMailAccount, host, port);
        } catch (Exception e) {
            LOG.warn("Failed to check for primary IMAP account of user {} in context {}", I(userId), I(contextId), e);
           return false;
        }
    }

    @Override
    public void handleHam(int accountId, String spamFullName, String[] mailIDs, boolean move, Session session) throws OXException {
        LOG.debug("handleHam");
        LOG.debug("accid: {}, spamfullname: {}, move: {}, session: {}", I(accountId), spamFullName, B(move), session.toString());

        // get access to internal mailstore
        MailService mailService = services.getOptionalService(MailService.class);
        if (null == mailService) {
            throw SpamExpertsExceptionCode.MAILSERVICE_MISSING.create();
        }

        String trainHamFolder = config.requireProperty(session, "com.openexchange.custom.spamexperts.trainhamfolder");

        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();

            MailMessage[] mails = mailAccess.getMessageStorage().getMessages(spamFullName, mailIDs, new MailField[]{MailField.FULL});
            copyToSpamexpertsFolder(trainHamFolder, mails, session);

            if (move) {
                /*
                 * Move to inbox
                 */
                mailAccess.getMessageStorage().moveMessages(spamFullName, FULLNAME_INBOX, mailIDs, true);
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public void handleSpam(final int accountId, final String fullName, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        LOG.debug("handleSpam");
        LOG.debug("accid: {}, fullname: {}, move: {}, session: {}", I(accountId), fullName, B(move), session.toString());

        // get access to internal mailstore
        MailService mailService = services.getOptionalService(MailService.class);
        if (null == mailService) {
            throw SpamExpertsExceptionCode.MAILSERVICE_MISSING.create();
        }

        String trainHamFolder = config.getPropertyFor(session, "com.openexchange.custom.spamexperts.trainspamfolder", "Spam", String.class).trim();

        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();

            MailMessage[] mails = mailAccess.getMessageStorage().getMessages(fullName, mailIDs, new MailField[]{MailField.FULL});
            copyToSpamexpertsFolder(trainHamFolder, mails, session);

            if (move) {
                /*
                 * Move to spam folder (copied from spamassassin spamhandler)
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

}
