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

package com.openexchange.userfeedback.mail.internal;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.InputStream;
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
import javax.mail.internet.MimeMessage.RecipientType;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.pgp.core.exceptions.PGPCoreExceptionCodes;
import com.openexchange.pgp.mail.PGPMimeService;
import com.openexchange.server.ServiceExceptionCode;
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

    /**
     * Initialises a new {@link FeedbackMailServiceSMTP}.
     */
    public FeedbackMailServiceSMTP() {
        super();
    }

    @Override
    public String sendFeedbackMail(FeedbackMailFilter filter) throws OXException {
        String result = "Sending email(s) failed for unkown reason, please contact the administrator or see the server logs";
        try (InputStream data = FeedbackMimeMessageUtility.getFeedbackFile(filter)) {
            result = sendMail(data, filter);
        } catch (IOException e) {
            throw FeedbackExceptionCodes.UNEXPECTED_ERROR.create(e);
        }
        return result;
    }

    private String sendMail(InputStream data, FeedbackMailFilter filter) throws OXException {
        LeanConfigurationService leanConfig = Services.getService(LeanConfigurationService.class);
        if (leanConfig == null) {
            throw ServiceExceptionCode.absentService(LeanConfigurationService.class);
        }

        boolean sign = false;
        boolean encrypt = false;
        String secretKeyFile = leanConfig.getProperty(UserFeedbackMailProperty.signKeyFile);
        String secretKeyPassword = leanConfig.getProperty(UserFeedbackMailProperty.signKeyPassword);
        PGPSecretKey signingKey = null;
        List<InternetAddress> invalidAddresses = new ArrayList<>();
        List<InternetAddress> pgpFailedAddresses = new ArrayList<>();
        Map<Address, PGPPublicKey> pgpRecipients = null;

        if (Strings.isNotEmpty(secretKeyFile)) {
            signingKey = FeedbackMimeMessageUtility.parsePrivateKey(secretKeyFile);
            sign = true;
        }
        if (null != filter.getPgpKeys() && !filter.getPgpKeys().isEmpty()) {
            pgpRecipients = FeedbackMimeMessageUtility.extractRecipientsForPgp(filter, invalidAddresses, pgpFailedAddresses);
            encrypt = true;
        }
        Properties smtpProperties = getSMTPProperties(leanConfig);
        String smtpUser = leanConfig.getProperty(UserFeedbackMailProperty.username);
        String smtpPass = leanConfig.getProperty(UserFeedbackMailProperty.password);
        if (Strings.isEmpty(smtpUser) || Strings.isEmpty(smtpPass)) {
            smtpProperties.put("mail.smtp.auth", "false");
            smtpUser = null;
            smtpPass = null;
        } else {
            // setting mail.smtp.auth to true enables attempts to use AUTH
            smtpProperties.put("mail.smtp.auth", "true");
        }
        Session smtpSession = Session.getInstance(smtpProperties);
        Transport transport = null;
        JSONObject result = new JSONObject();

        try {
            MimeMessage mail = FeedbackMimeMessageUtility.createMailMessage(data, filter, smtpSession);

            Address[] recipients = null;

            recipients = FeedbackMimeMessageUtility.extractValidRecipients(filter, invalidAddresses);
            if (recipients.length == 0 && (null == pgpRecipients || pgpRecipients.size() == 0)) {
                if (pgpFailedAddresses.size() > 0) {
                    throw FeedbackExceptionCodes.INVALID_EMAIL_ADDRESSES_PGP.create();
                }
                throw FeedbackExceptionCodes.INVALID_EMAIL_ADDRESSES.create();
            }
            mail.addRecipients(RecipientType.TO, recipients);

            transport = smtpSession.getTransport("smtp");
            String smtpHost = leanConfig.getProperty(UserFeedbackMailProperty.hostname);
            int smtpPort = leanConfig.getIntProperty(UserFeedbackMailProperty.port);
            transport.connect(smtpHost, smtpPort, smtpUser, smtpPass);

            PGPMimeService pgpMimeService = Services.getService(PGPMimeService.class);
            if (pgpMimeService == null) {
                throw ServiceExceptionCode.absentService(PGPMimeService.class);
            }

            if (encrypt && null != pgpRecipients && pgpRecipients.size() > 0) {
                MimeMessage pgpMail = null;
                Address[] pgpAddresses = pgpRecipients.keySet().toArray(new Address[pgpRecipients.size()]);
                mail.addRecipients(RecipientType.TO, pgpAddresses);
                if (sign) {
                    pgpMail = pgpMimeService.encryptSigned(mail, signingKey, secretKeyPassword.toCharArray(), new ArrayList<>(pgpRecipients.values()));
                } else {
                    pgpMail = pgpMimeService.encrypt(mail, new ArrayList<>(pgpRecipients.values()));
                }
                transport.sendMessage(pgpMail, pgpAddresses);
                appendPositiveSendingResult(pgpAddresses, result, sign, true);
            }

            if (recipients.length > 0) {
                if (sign) {
                    MimeMessage signedMail = pgpMimeService.sign(mail, signingKey, secretKeyPassword.toCharArray());
                    transport.sendMessage(signedMail, recipients);
                } else {
                    transport.sendMessage(mail, recipients);
                }
                appendPositiveSendingResult(recipients, result, sign, false);
            }
            appendWarnings(result, invalidAddresses, pgpFailedAddresses);
            return result.toString();
        } catch (JSONException e) {
            // will not happen
            return null;
        } catch (OXException e) {
            if (PGPCoreExceptionCodes.BAD_PASSWORD.equals(e)) {
                throw FeedbackExceptionCodes.INVALID_PGP_CONFIGURATION.create(e, e.getMessage());
            }
            throw e;
        } catch (MessagingException e) {
            LOG.error(e.getMessage(), e);
            throw FeedbackExceptionCodes.INVALID_SMTP_CONFIGURATION.create(e.getMessage());
        } finally {
            closeTransport(transport);
        }
    }

    /**
     * Appends the sending result to the specified {@link JSONObject}
     *
     * @param recipients The recipients
     * @param result The {@link JSONObject}
     * @param pgpSign If PGP was used to sign the feedback mail
     * @param pgpEncrypt If PGP was used to encrypt the feedback mail
     */
    private void appendPositiveSendingResult(Address[] recipients, JSONObject result, boolean pgpSign, boolean pgpEncrypt) throws JSONException {
        if (null != recipients && recipients.length > 0) {
            JSONArray array = new JSONArray(recipients.length);
            for (int i = 0; i < recipients.length; i++) {
                InternetAddress address = (InternetAddress) recipients[i];
                array.add(i, address.getAddress());
            }
            if (pgpSign && pgpEncrypt) {
                result.put("pgp", array);
            } else if (pgpSign && !pgpEncrypt) {
                result.put("sign", array);
            } else if (!pgpSign && pgpEncrypt) {
                result.put("encrypt", array);
            } else {
                result.put("normal", array);
            }
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
        Properties properties = MimeDefaultSession.getDefaultMailProperties();
        SSLSocketFactoryProvider factoryProvider = Services.getService(SSLSocketFactoryProvider.class);
        if (factoryProvider != null) {
            String socketFactoryClass = factoryProvider.getDefault().getClass().getName();
            properties.put("mail.smtp.ssl.socketFactory.class", socketFactoryClass);
        }
        properties.put("mail.smtp.ssl.socketFactory.port", I(leanConfig.getIntProperty(UserFeedbackMailProperty.port)));
        properties.put("mail.smtp.starttls.enable", Boolean.TRUE);
        properties.put("mail.smtp.ssl.trust", "*");

        properties.put("mail.smtp.host", leanConfig.getProperty(UserFeedbackMailProperty.hostname));
        properties.put("mail.smtp.port", I(leanConfig.getIntProperty(UserFeedbackMailProperty.port)));

        properties.put("mail.smtp.connectiontimeout", I(leanConfig.getIntProperty(UserFeedbackMailProperty.connectionTimeout)));
        properties.put("mail.smtp.timeout", I(leanConfig.getIntProperty(UserFeedbackMailProperty.timeout)));

        return properties;
    }

    /**
     * Appends any warnings to the specified {@link JSONObject}
     *
     * @param result The {@link JSONObject} to append the warnings to
     */
    private void appendWarnings(JSONObject result, List<InternetAddress> invalidAddresses, List<InternetAddress> pgpFailedAddresses) throws JSONException {
        if (invalidAddresses.size() > 0) {
            JSONArray array = new JSONArray(invalidAddresses.size());
            for (InternetAddress internetAddress : invalidAddresses) {
                array.add(0, internetAddress.getAddress());
            }
            result.put("fail", array);
        }
        if (pgpFailedAddresses.size() > 0) {
            JSONArray array = new JSONArray(pgpFailedAddresses.size());
            for (InternetAddress internetAddress : pgpFailedAddresses) {
                array.add(0, internetAddress.getAddress());
            }
            result.put("pgpFail", array);
        }
    }

}
