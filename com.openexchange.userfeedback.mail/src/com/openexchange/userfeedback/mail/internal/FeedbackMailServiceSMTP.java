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

package com.openexchange.userfeedback.mail.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.pgp.mail.PGPMimeService;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
import com.openexchange.userfeedback.mail.FeedbackMailService;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;
import com.openexchange.userfeedback.mail.osgi.Services;

/**
 * {@link FeedbackMailServiceSMTP}
 *
 * Send user feedback in form of a csv-file to a set of given recipients. Ensure a valid smtp server
 * configuration beforehand, see {@link MailProperties} for more information.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since 7.8.4
 */
public class FeedbackMailServiceSMTP implements FeedbackMailService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FeedbackMailServiceSMTP.class);
    public static final String FILENAME = "feedback";
    public static final String FILE_TYPE = ".csv";
    private List<InternetAddress> invalidAddresses;
    private FeedbackMimeMessageUtility messageUtility;

    /**
     * Initialises a new {@link FeedbackMailServiceSMTP}.
     */
    public FeedbackMailServiceSMTP() {
        super();
    }

    @Override
    public String sendFeedbackMail(FeedbackMailFilter filter) throws OXException {
        invalidAddresses = new ArrayList<>();
        messageUtility = new FeedbackMimeMessageUtility();
        String result = "Sending email(s) failed for unkown reason, please contact the administrator or see the server logs";
        File feedbackfile = messageUtility.getFeedbackfile(filter);
        if (feedbackfile != null) {
            try {
                result = sendMail(feedbackfile, filter);
            } catch (OXException e) {
                feedbackfile.delete();
                throw e;
            }
            feedbackfile.delete();
        }

        return result;
    }

    private String sendMail(File feedbackFile, FeedbackMailFilter filter) throws OXException {
        LeanConfigurationService leanConfig = Services.getService(LeanConfigurationService.class);
        boolean sign = false;
        boolean encrypt = false;
        String secretKeyFile = null;
        String secretKeyPassword = null;
        PGPSecretKey signingKey = null;
        PGPMimeService pgpMimeService = Services.getService(PGPMimeService.class);
        if (null != pgpMimeService && null != filter.getPgpKeys() && !filter.getPgpKeys().isEmpty()) {
            secretKeyFile = leanConfig.getProperty(UserFeedbackMailProperty.signKeyFile);
            secretKeyPassword = leanConfig.getProperty(UserFeedbackMailProperty.signKeyPassword);
            if (Strings.isNotEmpty(secretKeyFile) && Strings.isNotEmpty(secretKeyPassword)) {
                signingKey = messageUtility.parsePrivateKey(secretKeyFile);
                sign = true;
            }
            encrypt = true;
        }
        Properties smtpProperties = getSMTPProperties(leanConfig);
        Session smtpSession = Session.getInstance(smtpProperties);
        Transport transport = null;

        try {
            MimeMessage mail = messageUtility.createMailMessage(feedbackFile, filter, smtpSession);
            Address[] recipients = null;
            transport = smtpSession.getTransport("smtp");
            transport.connect(leanConfig.getProperty(UserFeedbackMailProperty.hostname), leanConfig.getIntProperty(UserFeedbackMailProperty.port), leanConfig.getProperty(UserFeedbackMailProperty.username), leanConfig.getProperty(UserFeedbackMailProperty.password));
            if (sign || encrypt) {
                Map<Address, PGPPublicKey> pgpRecipients = messageUtility.extractRecipientsForPgp(filter, invalidAddresses);
                MimeMessage pgpMail = null;
                if (encrypt) {
                    pgpMail = pgpMimeService.encryptSigned(mail, signingKey, secretKeyPassword.toCharArray(), new ArrayList<>(pgpRecipients.values()));
                } else {
                    pgpMail = pgpMimeService.encrypt(mail, new ArrayList<>(pgpRecipients.values()));
                }
                transport.sendMessage(pgpMail, pgpRecipients.keySet().toArray(new Address[pgpRecipients.size()]));
            }
            recipients = this.messageUtility.extractValidRecipients(filter, this.invalidAddresses);
            if (recipients.length == 0) {
                throw FeedbackExceptionCodes.INVALID_EMAIL_ADDRESSES.create();
            }
            if (sign) {
                MimeMessage signedMail = pgpMimeService.sign(mail, signingKey, secretKeyPassword.toCharArray());
                transport.sendMessage(signedMail, recipients);
            } else {
                transport.sendMessage(mail, recipients);
            }

            StringBuilder result = new StringBuilder();
            appendPositiveSendingResult(recipients, result);
            appendWarnings(result);
            return result.toString();
        } catch (MessagingException e) {
            LOG.error(e.getMessage(), e);
            throw FeedbackExceptionCodes.INVALID_SMTP_CONFIGURATION.create(e.getMessage());
        } finally {
            closeTransport(transport);
        }
    }

    /**
     * Appends the sending result to the specified {@link StringBuilder}
     *
     * @param recipients The recipients
     * @param builder The {@link StringBuilder}
     */
    private void appendPositiveSendingResult(Address[] recipients, StringBuilder builder) {
        builder.append("An email with user feedback was send to \n");
        for (int i = 0; i < recipients.length; i++) {
            InternetAddress address = (InternetAddress) recipients[i];
            String personalPart = address.getPersonal();
            builder.append(address.getAddress()).append(" ").append(personalPart != null && !personalPart.isEmpty() ? personalPart : "").append("\n");
        }
    }

    private void closeTransport(Transport transport) {
        if (transport != null) {
            try {
                transport.close();
            } catch (MessagingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private Properties getSMTPProperties(LeanConfigurationService leanConfig) {
        Properties properties = new Properties();
        SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
        String socketFactoryClass = factoryProvider.getDefault().getClass().getName();
        properties.put("mail.smtp.ssl.socketFactory.class", socketFactoryClass);
        properties.put("mail.smtp.ssl.socketFactory.port", leanConfig.getIntProperty(UserFeedbackMailProperty.port));
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.ssl.trust", "*");

        properties.put("mail.smtp.host", leanConfig.getProperty(UserFeedbackMailProperty.hostname));
        properties.put("mail.smtp.port", leanConfig.getIntProperty(UserFeedbackMailProperty.port));
        ;
        properties.put("mail.smtp.connectiontimeout", leanConfig.getIntProperty(UserFeedbackMailProperty.connectionTimeout));
        properties.put("mail.smtp.timeout", leanConfig.getIntProperty(UserFeedbackMailProperty.timeout));

        return properties;
    }

    /**
     * Appends any warnings to the specified {@link StringBuilder}
     *
     * @param builder The {@link StringBuilder} to append the warnings to
     */
    private void appendWarnings(StringBuilder builder) {
        if (invalidAddresses.size() > 0) {
            builder.append("\nThe following addresses are invalid and therefore ignored\n=======================\n");
            for (InternetAddress internetAddress : invalidAddresses) {
                builder.append(internetAddress.getAddress()).append("\n");
            }
        }
    }

}
