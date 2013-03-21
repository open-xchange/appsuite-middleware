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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.spamhandler.spamassassin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.spamassassin.spamc.Spamc;
import org.apache.spamassassin.spamc.Spamc.SpamdResponse;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.service.MailService;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.spamassassin.api.SpamdProvider;
import com.openexchange.spamhandler.spamassassin.api.SpamdService;
import com.openexchange.spamhandler.spamassassin.exceptions.SpamhandlerSpamassassinExceptionCode;
import com.openexchange.spamhandler.spamassassin.osgi.ServiceRegistry;
import com.openexchange.spamhandler.spamassassin.property.PropertyHandler;

/**
 * {@link SpamAssassinSpamHandler} - The spam-assassin spam handler which expects spam mails being wrapped inside a mail created by
 * spam-assassin. Therefore handling a formerly spam mail as ham requires to extract the original mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public final class SpamAssassinSpamHandler extends SpamHandler {

    private static class PlainAndNestedMessages {

        private final String[] nestedMessages;

        private final String[] plainMessages;

        public PlainAndNestedMessages(final String[] nestedMessages, final String[] plainMessages) {
            super();
            this.nestedMessages = nestedMessages;
            this.plainMessages = plainMessages;
        }


        public String[] getNestedMessages() {
            return nestedMessages;
        }


        public String[] getPlainMessages() {
            return plainMessages;
        }

    }

    private static class SpamdSettings {

        private final String hostname;

        private final int port;

        private final String username;

        public SpamdSettings(final String hostname, final int port, final String username) {
            this.hostname = hostname;
            this.port = port;
            this.username = username;
        }


        public String getHostname() {
            return hostname;
        }


        public int getPort() {
            return port;
        }


        public String getUsername() {
            return username;
        }

    }

    private static class UnwrapParameter {

        private final MailAccess<?, ?> m_mailAccess;

        private final boolean m_move;

        private final String m_spamFullname;

        public UnwrapParameter(final String spamFullname, final boolean move, final MailAccess<?, ?> mailAccess) {
            m_spamFullname = spamFullname;
            m_move = move;
            m_mailAccess = mailAccess;
        }

        public MailAccess<?, ?> getMailAccess() {
            return m_mailAccess;
        }

        public String getSpamFullname() {
            return m_spamFullname;
        }

        public boolean isMove() {
            return m_move;
        }
    }

    private static final MailField[] FIELDS_HEADER_CT = { MailField.HEADERS, MailField.CONTENT_TYPE };

    private static final SpamAssassinSpamHandler instance = new SpamAssassinSpamHandler();

//    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(PropertyHandler.class));

    private static final String NAME = "SpamAssassin";

    /**
     * Initializes a new {@link SpamAssassinSpamHandler}.
     */
    private SpamAssassinSpamHandler() {
        super();
    }

    /**
     * Gets the singleton instance of {@link SpamAssassinSpamHandler}.
     *
     * @return The singleton instance of {@link SpamAssassinSpamHandler}
     */
    public static SpamAssassinSpamHandler getInstance() {
        return instance;
    }

    @Override
    public String getSpamHandlerName() {
        return NAME;
    }

    @Override
    public boolean isUnsubscribeSpamFolders() {
        final ConfigurationService configurationService = ServiceRegistry.getInstance().getService(ConfigurationService.class);
        return null == configurationService ? true : configurationService.getBoolProperty("com.openexchange.spamhandler.spamassassin.unsubscribeSpamFolders", true);
    }

    @Override
    public void handleHam(final int accountId, final String spamFullname, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        final MailService mailService = ServiceRegistry.getInstance().getService(MailService.class);
        if (null == mailService) {
            throw SpamhandlerSpamassassinExceptionCode.MAILSERVICE_MISSING.create();
        }
        final MailAccess<?, ?> mailAccess = mailService.getMailAccess(session, accountId);
        mailAccess.connect();
        final PropertyHandler instance2 = PropertyHandler.getInstance();
        final SpamdSettings spamdSettings = getSpamdSettings(session, instance2);
        unwrap(new UnwrapParameter(spamFullname, move, mailAccess), mailIDs, spamdSettings);
    }

    @Override
    public void handleSpam(final int accountId, final String fullname, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        /*
         * Copy to confirmed spam folder
         */
        final MailService mailService = ServiceRegistry.getInstance().getService(MailService.class);
        if (null == mailService) {
            throw SpamhandlerSpamassassinExceptionCode.MAILSERVICE_MISSING.create();
        }
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, accountId);
            mailAccess.connect();
            if (isCreateConfirmedSpam()) {
                final String confirmedSpamFullname = mailAccess.getFolderStorage().getConfirmedSpamFolder();
                mailAccess.getMessageStorage().copyMessages(fullname, confirmedSpamFullname, mailIDs, true);
            }
            final SpamdSettings spamdSettings = getSpamdSettings(session, PropertyHandler.getInstance());
            if (null != spamdSettings) {
                final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(fullname, mailIDs, new MailField[]{MailField.FULL});
                spamdMessageProcessing(mails, spamdSettings, true);
            }
            if (move) {
                /*
                 * Move to spam folder
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

    private void copyMessagesToConfirmedHamAndInbox(final UnwrapParameter paramObject, final String[] plainIDsArr, final String confirmedHamFullname, final SpamdSettings spamdSettings) throws OXException {
        final MailAccess<?, ?> mailAccess = paramObject.getMailAccess();
        final String spamFullname = paramObject.getSpamFullname();
        if (isCreateConfirmedHam()) {
            mailAccess.getMessageStorage().copyMessages(spamFullname, confirmedHamFullname, plainIDsArr, false);
        }
        if (null != spamdSettings) {
            final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(spamFullname, plainIDsArr, new MailField[]{MailField.FULL});
            spamdMessageProcessing(mails, spamdSettings, false);
        }
        if (paramObject.isMove()) {
            mailAccess.getMessageStorage().moveMessages(spamFullname, SpamHandler.FULLNAME_INBOX, plainIDsArr, true);
        }
    }

    private MailMessage[] getNestedMailsAndHandleOthersAsPlain(final UnwrapParameter paramObject, final String confirmedHamFullname, final String[] nestedMessages, final SpamdSettings spamdSettings) throws OXException {
        final int nestedmessagelength = nestedMessages.length;
        final List<MailMessage> nestedMails = new ArrayList<MailMessage>(nestedmessagelength);
        final String[] exc = new String[1];
        for (int i = 0; i < nestedmessagelength; i++) {
            final MailPart wrapped = paramObject.getMailAccess().getMessageStorage().getAttachment(paramObject.getSpamFullname(), nestedMessages[i], "2");
            wrapped.loadContent();
            MailMessage tmp = null;
            if (wrapped instanceof MailMessage) {
                tmp = (MailMessage) wrapped;
            } else if (wrapped.getContentType().startsWith(MimeTypes.MIME_MESSAGE_RFC822)) {
                tmp = (MailMessage) (wrapped.getContent());
            }
            if (null == tmp) {
                /*
                 * Handle like a plain spam message
                 */
                exc[0] = nestedMessages[i];
                copyMessagesToConfirmedHamAndInbox(paramObject, exc, confirmedHamFullname, spamdSettings);
            } else {
                nestedMails.add(tmp);
            }
        }
        return nestedMails.toArray(new MailMessage[nestedMails.size()]);
    }

    private SpamdSettings getSpamdSettings(final Session session, final PropertyHandler propertyHandler) throws OXException {
        SpamdSettings spamdSettings = null;
        if (propertyHandler.isSpamd()) {
            final SpamdService spamdservice = ServiceRegistry.getInstance().getService(SpamdService.class);
            SpamdProvider provider = null;
            if (null != spamdservice) {
                // We have a special service providing login information, so we use that one...
                try {
                    provider = spamdservice.getProvider(session);
                    spamdSettings = new SpamdSettings(provider.getHostname(), provider.getPort(), provider.getUsername());
                } catch (final OXException e) {
                    throw SpamhandlerSpamassassinExceptionCode.ERROR_GETTING_SPAMD_PROVIDER.create(e, e.getMessage());
                }
            } else {
                spamdSettings = new SpamdSettings(propertyHandler.getHostname(), propertyHandler.getPort(), getUsername(session));
            }
        }
        return spamdSettings;
    }

    private String getUsername(final Session session) {
        return session.getLogin();
    }

    /**
     * @param source - A string containing the message
     * @param spam - Whether the message should be marked as spam or ham, if set to false it's marked as ham
     * @param spamdsettings the settings how spamd can be reached
     * @throws OXException
     */
    private void sendToSpamd(final String source, final boolean spam, final SpamdSettings spamdsettings) throws OXException {
        final Spamc spamc = new Spamc();
        spamc.setHost(spamdsettings.getHostname());
        spamc.setPort(spamdsettings.getPort());
        spamc.setUserName(spamdsettings.getUsername());
        final PropertyHandler instance2 = PropertyHandler.getInstance();
        spamc.setRetrySleep(instance2.getRetrysleep());
        spamc.setConnectRetries(instance2.getRetries());
        spamc.setTimeout(instance2.getTimeout());
        SpamdResponse resp;
        try {
            resp = spamc.tell(source, spam, true, true, false, false);
        } catch (final IllegalArgumentException e) {
            throw SpamhandlerSpamassassinExceptionCode.WRONG_TELL_CMD_ARGS.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SpamhandlerSpamassassinExceptionCode.COMMUNICATION_ERROR.create(e, e.getMessage());
        }
        final int responseCode = resp.getResponseCode();
        if (Spamc.ExitCodes.EX_OK != responseCode) {
            throw SpamhandlerSpamassassinExceptionCode.WRONG_SPAMD_EXIT.create(responseCode);
        }
    }

    private PlainAndNestedMessages separatePlainAndNestedMessages(final String[] mailIDs, final MailMessage[] mails) {
        /*
         * Separate the plain from the nested messages inside spam folder
         */
        final List<String> plainIDs = new ArrayList<String>(mailIDs.length);
        final List<String> extractIDs = new ArrayList<String>(mailIDs.length);
        for (int i = 0; i < mails.length; i++) {
            final String spamHdr = mails[i].getFirstHeader(MessageHeaders.HDR_X_SPAM_FLAG);
            final String spamChecker = mails[i].getFirstHeader("X-Spam-Checker-Version");
            final ContentType contentType = mails[i].getContentType();
            if (spamHdr != null && "yes".regionMatches(true, 0, spamHdr, 0, 3) && contentType.isMimeType(MimeTypes.MIME_MULTIPART_ALL) && (spamChecker == null ? true : spamChecker.toLowerCase(
                Locale.ENGLISH).indexOf("spamassassin") != -1)) {
                extractIDs.add(mailIDs[i]);
            } else {
                plainIDs.add(mailIDs[i]);
            }
        }
        return new PlainAndNestedMessages(extractIDs.toArray(new String[extractIDs.size()]), plainIDs.toArray(new String[plainIDs.size()]));
    }

    private void spamdMessageProcessing(final MailMessage[] mails, final SpamdSettings spamdSettings, final boolean spam) throws OXException {
        for (final MailMessage mail : mails) {
            // ...then get the plaintext of the mail as spamhandler is not able to cope with our mail objects ;-) ...
            final String source = mail.getSource();

            // ...last send the plaintext over to the spamassassin daemon
            sendToSpamd(source, spam, spamdSettings);
        }
    }

    private void unwrap(final UnwrapParameter parameterObject, final String[] mailIDs, final SpamdSettings spamdSettings) throws OXException {
        final MailAccess<?, ?> mailAccess = parameterObject.getMailAccess();
        try {
            /*
             * Mark as ham. In contrast to mark as spam this is a very time sucking operation. In order to deal with the original messages
             * that are wrapped inside a SpamAssassin-created message it must be extracted. Therefore we need to access message's content
             * and cannot deal only with UIDs
             */
            final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(parameterObject.getSpamFullname(), mailIDs, FIELDS_HEADER_CT);
            final PlainAndNestedMessages plainAndNestedMessages = separatePlainAndNestedMessages(mailIDs, mails);
            final String confirmedHamFullname = mailAccess.getFolderStorage().getConfirmedHamFolder();
            /*
             * Copy plain messages to confirmed ham and INBOX
             */
            copyMessagesToConfirmedHamAndInbox(parameterObject, plainAndNestedMessages.getPlainMessages(), confirmedHamFullname, spamdSettings);
            /*
             * Handle spamassassin messages
             */
            final String[] nestedMessages = plainAndNestedMessages.getNestedMessages();

            final MailMessage[] nestedMails = getNestedMailsAndHandleOthersAsPlain(parameterObject, confirmedHamFullname, nestedMessages, spamdSettings);
            if (null != spamdSettings) {
                spamdMessageProcessing(nestedMails, spamdSettings, false);
            }
            final String[] ids = mailAccess.getMessageStorage().appendMessages(confirmedHamFullname, nestedMails);
            if (parameterObject.isMove()) {
                mailAccess.getMessageStorage().copyMessages(confirmedHamFullname, FULLNAME_INBOX, ids, true);
                mailAccess.getMessageStorage().deleteMessages(parameterObject.getSpamFullname(), nestedMessages, true);
            }
        } finally {
            mailAccess.close(true);
        }
    }

}
