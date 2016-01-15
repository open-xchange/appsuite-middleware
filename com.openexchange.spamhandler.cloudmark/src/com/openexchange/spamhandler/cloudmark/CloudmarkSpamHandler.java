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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.spamhandler.cloudmark;

import java.io.File;
import java.io.IOException;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.mail.util.SharedFileInputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.datasource.FileHolderDataSource;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.MailTransport.SendRawProperties;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandler;

/**
 * Cloudmark spam handler
 *
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CloudmarkSpamHandler extends SpamHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CloudmarkSpamHandler.class);

    private static final String NAME = "CloudmarkSpamHandler";

    // -------------------------------------------------------------------------------------------

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CloudmarkSpamHandler}.
     */
    public CloudmarkSpamHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getSpamHandlerName() {
        return NAME;
    }

    private ThresholdFileHolder writeMessage(MailMessage original) throws OXException {
        if (null == original) {
            return null;
        }
        ThresholdFileHolder sink = new ThresholdFileHolder();
        boolean closeSink = true;
        try {
            original.writeTo(sink.asOutputStream());
            closeSink = false;
            return sink;
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (closeSink) {
                Streams.close(sink);
            }
        }
    }

    private ThresholdFileHolder writeMessage(Message original) throws OXException {
        if (null == original) {
            return null;
        }
        ThresholdFileHolder sink = new ThresholdFileHolder();
        boolean closeSink = true;
        try {
            original.writeTo(sink.asOutputStream());
            closeSink = false;
            return sink;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (closeSink) {
                Streams.close(sink);
            }
        }
    }

    private void getAndTransport(String mailId, InternetAddress targetAddress, InternetAddress senderAddress, boolean wrap, final String fullName, final Session session, MailAccess<?, ?> mailAccess) throws OXException {
        ThresholdFileHolder sink = writeMessage(mailAccess.getMessageStorage().getMessage(fullName, mailId, false));
        if (null != sink) {
            try {
                // Initialize send properties
                SendRawProperties sendRawProperties = MailTransport.SendRawProperties.newInstance()
                    .addRecipient(targetAddress)
                    .setSender(senderAddress)
                    .setValidateAddressHeaders(false)
                    .setSanitizeHeaders(false);

                // Wrap if demanded
                if (wrap) {
                    MimeMessage toSend = new MimeMessage(MimeDefaultSession.getDefaultSession());
                    MimeMultipart multipart = new MimeMultipart();

                    MimeBodyPart bodyPart = new MimeBodyPart();
                    {
                        File tempFile = sink.getTempFile();
                        if (null == tempFile) {
                            bodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(sink.toByteArray(), "message/rfc822")));
                        } else {
                            bodyPart.setDataHandler(new DataHandler(new FileHolderDataSource(sink, "message/rfc822")));
                        }
                    }
                    bodyPart.setHeader("Content-Type", "message/rfc822");
                    bodyPart.setHeader("Content-Disposition", "inline");

                    multipart.addBodyPart(bodyPart);
                    toSend.setContent(multipart);
                    toSend.saveChanges();

                    ThresholdFileHolder tmp = writeMessage(toSend);
                    Streams.close(sink);
                    sink = tmp;
                }

                // Transport message (either as-is or wrapped)
                MailTransport transport = MailTransport.getInstance(session);
                try {
                    File tempFile = sink.getTempFile();
                    if (null == tempFile) {
                        transport.sendRawMessage(sink.getStream(), sendRawProperties);
                    } else {
                        transport.sendRawMessage(new SharedFileInputStream(tempFile), sendRawProperties);
                    }
                } finally {
                    transport.close();
                }

            } catch (MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            } catch (IOException e) {
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(sink);
            }
        }
    }

    @Override
    public void handleSpam(final int accountId, final String fullName, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        ConfigurationService configuration = services.getService(ConfigurationService.class);
        String sTargetSpamEmailAddress = configuration.getProperty("com.openexchange.spamhandler.cloudmark.targetSpamEmailAddress", "").trim();

        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();

            if (Strings.isEmpty(sTargetSpamEmailAddress)) {
                LOG.warn("There is no value configured for 'com.openexchange.spamhandler.cloudmark.targetSpamEmailAddress', cannot process spam reporting to server.");
            } else {
                InternetAddress targetSpamAddress = null;
                try {
                    targetSpamAddress = new InternetAddress(sTargetSpamEmailAddress, true);
                } catch (final AddressException e) {
                    LOG.error("The configured target eMail address is not valid", e);
                }

                // Check whether we are supposed to wrap the message
                boolean wrap = configuration.getBoolProperty("com.openexchange.spamhandler.cloudmark.wrapMessage", false);

                if (null != targetSpamAddress) {
                    InternetAddress senderAddress = getSenderAddress(session);
                    for (String mailId : mailIDs) {
                        getAndTransport(mailId, targetSpamAddress, senderAddress, wrap, fullName, session, mailAccess);
                    }
                }
            }

            if (move) {
                final String targetSpamFolder = configuration.getProperty("com.openexchange.spamhandler.cloudmark.targetSpamFolder", "1").trim();
                if (targetSpamFolder.equals("1")) {
                    mailAccess.getMessageStorage().moveMessages(fullName, mailAccess.getFolderStorage().getTrashFolder(), mailIDs, true);
                } else if (targetSpamFolder.equals("2")) {
                    mailAccess.getMessageStorage().moveMessages(fullName, mailAccess.getFolderStorage().getSpamFolder(), mailIDs, true);
                } else if (targetSpamFolder.equals("3")) {
                    mailAccess.getMessageStorage().moveMessages(fullName, mailAccess.getFolderStorage().getConfirmedSpamFolder(), mailIDs, true);
                } else if (targetSpamFolder.equals("0")) {
                	// no move at all
                } else {
                    mailAccess.getMessageStorage().moveMessages(fullName, mailAccess.getFolderStorage().getTrashFolder(), mailIDs, true);
                    LOG.error("There is no valid 'com.openexchange.spamhandler.cloudmark.targetSpamFolder' configured. Moving spam to trash.");
                }
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public void handleHam(final int accountId, final String fullName, final String[] mailIDs, final boolean move, final Session session) throws OXException {
        ConfigurationService configuration = services.getService(ConfigurationService.class);
        String sTargetHamEmailAddress = configuration.getProperty("com.openexchange.spamhandler.cloudmark.targetHamEmailAddress", "").trim();

        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();

            if (Strings.isEmpty(sTargetHamEmailAddress)) {
                LOG.warn("There is no value configured for 'com.openexchange.spamhandler.cloudmark.targetHamEmailAddress', cannot process ham reporting to server.");
            } else {
                InternetAddress targetHamAddress = null;
                try {
                    targetHamAddress = new InternetAddress(sTargetHamEmailAddress, true);
                } catch (final AddressException e) {
                    LOG.error("The configured target eMail address is not valid", e);
                }

                // Check whether we are supposed to wrap the message
                boolean wrap = configuration.getBoolProperty("com.openexchange.spamhandler.cloudmark.wrapMessage", false);

                if (null != targetHamAddress) {
                    InternetAddress senderAddress = getSenderAddress(session);
                    for (String mailId : mailIDs) {
                        getAndTransport(mailId, targetHamAddress, senderAddress, wrap, fullName, session, mailAccess);
                    }
                }
            }

            if (move) {
                String targetSpamFolder = configuration.getProperty("com.openexchange.spamhandler.cloudmark.targetSpamFolder", "1").trim();
                if (!targetSpamFolder.equals("0")) {
                    mailAccess.getMessageStorage().moveMessages(fullName, "INBOX", mailIDs, true);
                }
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public boolean isCreateConfirmedSpam() {
        final ConfigurationService configuration = services.getService(ConfigurationService.class);
        return configuration.getProperty("com.openexchange.spamhandler.cloudmark.targetSpamFolder", "1").trim().equals("3");
    }

    @Override
    public boolean isCreateConfirmedHam() {
        return false;
    }

    /**
     * Gets the session users sender address.
     *
     * @return The address or <code>null</code> if not configured
     */
    private static InternetAddress getSenderAddress(Session session) throws OXException {
        UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session);
        if (usm == null) {
            return null;
        }

        String sendAddr = usm.getSendAddr();
        if (sendAddr == null) {
            return null;
        }

        try {
            return new InternetAddress(sendAddr, true);
        } catch (AddressException e) {
            return null;
        }
    }

}
