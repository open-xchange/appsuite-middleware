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

package com.openexchange.spamhandler.spamexperts;

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
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.service.MailService;
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

        final Properties props = new Properties();
        if ("imaps".equals(imapUrl.getScheme())) {
            props.put("mail.imap.socketFactory.class", socketFactoryClass);
        } else {
            props.put("mail.imap.ssl.socketFactory.class", socketFactoryClass);
            props.put("mail.imap.starttls.enable", "true");
        }

        props.put("mail.imap.socketFactory.port", Integer.toString(imapUrl.getPort()));
        props.put("mail.imap.socketFactory.fallback", "false");

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
                    sfmesgs[i++] = MimeMessageUtility.newMimeMessage(MimeMessageUtility.getStreamFromMailPart(mail), null);
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

    /* (non-Javadoc)
     * @see com.openexchange.spamhandler.SpamHandler#handleHam(int, java.lang.String, java.lang.String[], boolean, com.openexchange.session.Session)
     */
    @Override
    public void handleHam(int accountId, String spamFullName, String[] mailIDs, boolean move, Session session) throws OXException {
        LOG.debug("handleHam");
        LOG.debug("accid: {}, spamfullname: {}, move: {}, session: {}", accountId, spamFullName, move, session.toString());

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
        LOG.debug("accid: {}, fullname: {}, move: {}, session: {}", accountId, fullName, move, session.toString());

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
