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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.io.IOUtils;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.userfeedback.ExportResult;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.ExportType;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;
import com.openexchange.userfeedback.mail.osgi.Services;

/**
 * {@link FeedbackMimeMessageUtility}
 * 
 * Utility class for creation of {@link MimeMessage}s and related operations for user feedback purposes.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since 7.8.4
 */
public class FeedbackMimeMessageUtility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FeedbackMimeMessageUtility.class);
    private static final String FILENAME = "feedback";
    private static final String FILE_TYPE = ".csv";
    private boolean isPGPEncrypted;
    private boolean isSigned;

    /**
     * Initializes a new {@link FeedbackMimeMessageUtility}.
     * 
     * @param isPGPEncrypted
     * @param isSigned
     */
    public FeedbackMimeMessageUtility(boolean isPGPEncrypted, boolean isSigned) {
        super();
        this.isPGPEncrypted = isPGPEncrypted;
        this.isSigned = isSigned;
    }

    /**
     * Create a {@link MimeMessage} which can be send via email for the given {@link File} and {@link FeedbackMailFilter}.
     *
     * @param feedbackFile, the file that should be attached to the email
     * @param filter, the filter to use
     * @param session, the session for MimeMessage creation purposes
     * @return a MimeMessage with the gathered user feedback, which can be send
     * @throws OXException
     */
    public MimeMessage createMailMessage(File feedbackFile, FeedbackMailFilter filter, Session session) throws OXException {
        MimeMessage mimeMessage = new MimeMessage(session);
        if (isPGPEncrypted) {
            //Not ready yet
        } else if (isSigned) {
            //Not ready yet
        } else {
            mimeMessage = getNotEncryptedUnsignedMail(feedbackFile, filter, session);
        }
        return mimeMessage;
    }

    private MimeMessage getNotEncryptedUnsignedMail(File feedbackFile, FeedbackMailFilter filter, Session session) throws OXException {
        MimeMessage email = new MimeMessage(session);

        try {
            email.setSubject(filter.getSubject());
            LeanConfigurationService leanConfigService = Services.getService(LeanConfigurationService.class);
            email.setFrom(new InternetAddress(leanConfigService.getProperty(UserFeedbackMailProperty.senderAddress), leanConfigService.getProperty(UserFeedbackMailProperty.senderName)));

            BodyPart messageBody = new MimeBodyPart();
            messageBody.setText(filter.getBody());
            Multipart completeMailContent = new MimeMultipart(messageBody);
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(feedbackFile);
            completeMailContent.addBodyPart(attachment);
            email.setContent(completeMailContent);
        } catch (MessagingException e) {
            LOG.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return email;
    }

    /**
     * Loads the file with all user feedback from the {@link FeedbackService} and translates
     * the result into a file, that is returned.
     * 
     * @param filter, all necessary filter informations
     * @return a file with all user feedback for the given filter
     * @throws OXException, when something during the export goes wrong
     */
    public File getFeedbackfile(FeedbackMailFilter filter) throws OXException {
        FeedbackService feedbackService = Services.getService(FeedbackService.class);
        ExportResultConverter feedbackProvider = feedbackService.export(filter.getCtxGroup(), filter);
        ExportResult feedbackResult = feedbackProvider.get(ExportType.CSV);
        // get the csv file
        File result = null;
        try (InputStream stream = (InputStream) feedbackResult.getResult()) {
            result = getFileFromStream(stream);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    private File getFileFromStream(InputStream stream) throws OXException, IOException {
        File tempFile = File.createTempFile(FILENAME, FILE_TYPE);
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(stream, out);
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return tempFile;
    }

    /**
     * Extract all valid email addresses from the given filter and also put all invalid addresses into
     * the given list of "invalidAddresses".
     * 
     * @param filter, the filter object with all needed information
     * @param invalidAddresses, the list where all invalid email addresses should be stored
     * @return an Array with {@link InternetAddress}s
     */
    public Address[] extractValidRecipients(FeedbackMailFilter filter, List<InternetAddress> invalidAddresses) {
        Map<String, String> recipients = filter.getRecipients();
        List<InternetAddress> validRecipients = new ArrayList<>();
        for (Entry<String, String> recipient : recipients.entrySet()) {
            InternetAddress address = null;
            try {
                address = new InternetAddress(recipient.getKey(), recipient.getValue());
                address.validate();
                validRecipients.add(address);
            } catch (UnsupportedEncodingException e) {
                LOG.error(e.getMessage(), e);
            } catch (@SuppressWarnings("unused") AddressException e) {
                invalidAddresses.add(address);
                // validation exception does not trigger any logging
            }
        }
        return validRecipients.toArray(new InternetAddress[validRecipients.size()]);
    }
}
