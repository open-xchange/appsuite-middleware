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
import java.util.Properties;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
import com.openexchange.userfeedback.mail.FeedbackMailService;
import com.openexchange.userfeedback.mail.config.MailProperties;
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

    @Override
    public String sendFeedbackMail(FeedbackMailFilter filter) throws OXException {
        invalidAddresses = new ArrayList<>();
        if (this.messageUtility == null) {
            messageUtility = new FeedbackMimeMessageUtility(false, false);
        }
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
        Properties smtpProperties = getSMTPProperties();
        Session smtpSession = Session.getInstance(smtpProperties);
        Transport transport = null;

        String result = "";

        try {
            Address[] recipients = this.messageUtility.extractValidRecipients(filter, this.invalidAddresses);
            if (recipients.length == 0 || (recipients.length == 1 && recipients[0] == null)) {
                throw FeedbackExceptionCodes.INVALID_EMAIL_ADDRESSES.create();
            }
            MimeMessage mail = messageUtility.createMailMessage(feedbackFile, filter, smtpSession);
            transport = smtpSession.getTransport("smtp");
            transport.connect(MailProperties.getSmtpHostname(), MailProperties.getSmtpPort(), MailProperties.getSmtpUsername(), MailProperties.getSmtpPassword());
            transport.sendMessage(mail, recipients);
            result = getPositiveSendingResult(recipients);
        } catch (MessagingException e) {
            LOG.error(e.getMessage(), e);
            throw FeedbackExceptionCodes.INVALID_SMTP_CONFIGURATION.create(e.getMessage());
        } finally {
            closeTransport(transport);
        }

        result = result.concat(appendWarnings());

        return result;
    }

    private String getPositiveSendingResult(Address[] recipients) {
        String result = "An email with userfeedback was send to \n";
        for (int i = 0; i < recipients.length; i++) {
            InternetAddress address = (InternetAddress) recipients[i];
            String personalPart = address.getPersonal();
            result += address.getAddress().concat(" ").concat(personalPart != null && !personalPart.isEmpty() ? personalPart : "").concat("\n");
        }
        return result;
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

    private Properties getSMTPProperties() {
        Properties properties = new Properties();
        SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
        String socketFactoryClass = factoryProvider.getDefault().getClass().getName();
        properties.put("mail.smtp.ssl.socketFactory.class", socketFactoryClass);
        properties.put("mail.smtp.ssl.socketFactory.port", MailProperties.getSmtpPort());
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.ssl.trust", "*");

        properties.put("mail.smtp.host", MailProperties.getSmtpHostname());
        properties.put("mail.smtp.port", MailProperties.getSmtpPort());
        properties.put("mail.smtp.connectiontimeout", MailProperties.getSmtpConnectionTimeout());
        properties.put("mail.smtp.timeout", MailProperties.getSmtpTimeout());
        properties.put("mail.smtp.ssl.protocols", MailProperties.getSmtpProtocol());

        return properties;
    }

    private String appendWarnings() {
        String result = "";
        if (invalidAddresses.size() > 0) {
            result = "\nThe following addresses are invalid and therefore igonred\n=======================\n";
            for (InternetAddress internetAddress : invalidAddresses) {
                result = result.concat(internetAddress.getAddress().concat("\n"));
            }
        }
        return result;
    }
}
