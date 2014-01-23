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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.ByteArrayInputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.spamexperts.exceptions.SpamhandlerSpamExpertsExceptionCode;
import com.openexchange.spamhandler.spamexperts.osgi.SpamExpertsServiceRegistry;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;


/**
 * {@link SpamExpertsSpamHandler}
 *
 */
public class SpamExpertsSpamHandler extends SpamHandler {


    private static final SpamExpertsSpamHandler instance = new SpamExpertsSpamHandler();
    
    private static final Logger LOG = LoggerFactory.getLogger(SpamExpertsSpamHandler.class);
    
    private static final String CONFIG_SP_IMAPURL = "com.openexchange.custom.spamexperts.imapurl";
    private static final String CONFIG_SP_IMAPUSER = "com.openexchange.custom.spamexperts.imapuser";
    private static final String CONFIG_SP_IMAPPASSWORD = "com.openexchange.custom.spamexperts.imappassword";
    private static final String CONFIG_SP_TRAIN_SPAM_FOLDER = "com.openexchange.custom.spamexperts.trainspamfolder";
    private static final String CONFIG_SP_TRAIN_HAM_FOLDER = "com.openexchange.custom.spamexperts.trainhamfolder";
    
    /**
     * Initializes a new {@link SpamExpertsSpamHandler}.
     */
    private SpamExpertsSpamHandler() {
        super();
    }

    public static SpamExpertsSpamHandler getInstance() {
        return instance;
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.spamhandler.SpamHandler#getSpamHandlerName()
     */
    @Override
    public String getSpamHandlerName() {
        return "SpamExperts";
    }

    private void copyToSpamexpertsFolder(final String folder, final MailMessage []messages) throws OXException {
        ConfigurationService cfgsrv = SpamExpertsServiceRegistry.getInstance().getService(ConfigurationService.class);
        if( null == cfgsrv ) {
            throw SpamhandlerSpamExpertsExceptionCode.CONFIGSERVICE_MISSING.create();
        }
        final String spImapUrl = cfgsrv.getProperty(CONFIG_SP_IMAPURL);
        if( null == spImapUrl ) {
            throw SpamhandlerSpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_IMAPURL);
        }
        final String spImapUser = cfgsrv.getProperty(CONFIG_SP_IMAPUSER);
        if( null == spImapUser ) {
            throw SpamhandlerSpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_IMAPUSER);
        }
        final String spImapPw = cfgsrv.getProperty(CONFIG_SP_IMAPPASSWORD);
        if( null == spImapPw ) {
            throw SpamhandlerSpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_IMAPPASSWORD);
        }
        
        final URLName imapURL = new URLName(spImapUrl);
        final Properties props = new Properties();
        final javax.mail.Session imapSession = javax.mail.Session.getInstance(props);
        final IMAPStore imapStore = new IMAPStore(imapSession, imapURL);
        
        try {
            if( null == messages ) {
                throw SpamhandlerSpamExpertsExceptionCode.UNABLE_TO_GET_MAILS.create();
            }
            imapStore.connect(spImapUser, spImapPw);
            IMAPFolder sf = (IMAPFolder)imapStore.getFolder(folder);
            if( ! sf.exists() ) {
                throw SpamhandlerSpamExpertsExceptionCode.FOLDER_DOES_NOT_EXIST.create(folder);
            }
            
            MimeMessage []sfmesgs = new MimeMessage[messages.length];
            
            int i = 0;
            for(final MailMessage mail : messages) {
                sfmesgs[i++] = new MimeMessage(imapSession, new ByteArrayInputStream(mail.getSourceBytes()));
            }
            sf.appendMessages(sfmesgs);
        } catch (MessagingException e) {
            LOG.error(e.getMessage(),e);
            throw new OXException(e);
        } catch (OXException e) {
            LOG.error(e.getMessage(),e);
            throw e;
        } finally {
            if( null != imapStore && imapStore.isConnected() ) {
                try {
                    imapStore.close();
                } catch (MessagingException e) {}
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.spamhandler.SpamHandler#handleHam(int, java.lang.String, java.lang.String[], boolean, com.openexchange.session.Session)
     */
    @Override
    public void handleHam(int accountId, String spamFullname, String[] mailIDs, boolean move, Session session) throws OXException {
        LOG.debug("handleHam");
        LOG.debug("accid: " + accountId + ", spamfullname: " + spamFullname + ", move: " + move + ", session: " + session.toString());

        ConfigurationService cfgsrv = SpamExpertsServiceRegistry.getInstance().getService(ConfigurationService.class);
        if( null == cfgsrv ) {
            throw SpamhandlerSpamExpertsExceptionCode.CONFIGSERVICE_MISSING.create();
        }
        final String folder = cfgsrv.getProperty(CONFIG_SP_TRAIN_HAM_FOLDER);
        if( null == folder ) {
            throw SpamhandlerSpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_TRAIN_HAM_FOLDER);
        }

        // get access to internal mailstore
        final MailService mailService = SpamExpertsServiceRegistry.getInstance().getService(MailService.class);
        if (null == mailService) {
            throw SpamhandlerSpamExpertsExceptionCode.MAILSERVICE_MISSING.create();
        }
        MailAccess<?, ?> mailAccess = null;
        
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();
            final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(spamFullname, mailIDs, new MailField[]{MailField.FULL});

            copyToSpamexpertsFolder(folder, mails);
            
            if (move) {
                /*
                 * Move to inbox
                 */
                mailAccess.getMessageStorage().moveMessages(spamFullname, FULLNAME_INBOX, mailIDs, true);
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    public void handleSpam(final int accountId, final String fullname, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        LOG.debug("handleSpam");
        LOG.debug("accid: " + accountId + ", fullname: " + fullname + ", move: " + move + ", session: " + session.toString());

        ConfigurationService cfgsrv = SpamExpertsServiceRegistry.getInstance().getService(ConfigurationService.class);
        if( null == cfgsrv ) {
            throw SpamhandlerSpamExpertsExceptionCode.CONFIGSERVICE_MISSING.create();
        }
        final String folder = cfgsrv.getProperty(CONFIG_SP_TRAIN_SPAM_FOLDER);
        if( null == folder ) {
            throw SpamhandlerSpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(CONFIG_SP_TRAIN_SPAM_FOLDER);
        }

        // get access to internal mailstore
        final MailService mailService = SpamExpertsServiceRegistry.getInstance().getService(MailService.class);
        if (null == mailService) {
            throw SpamhandlerSpamExpertsExceptionCode.MAILSERVICE_MISSING.create();
        }
        MailAccess<?, ?> mailAccess = null;
        
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();
            final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(fullname, mailIDs, new MailField[]{MailField.FULL});

            copyToSpamexpertsFolder(folder, mails);
            
            if (move) {
                /*
                 * Move to spam folder (copied from spamassassin spamhandler)
                 */
                final String spamFullname = mailAccess.getFolderStorage().getSpamFolder();
                mailAccess.getMessageStorage().moveMessages(fullname, spamFullname, mailIDs, true);
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }
}
