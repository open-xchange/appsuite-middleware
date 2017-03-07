
package com.openexchange.userfeedback.mail.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.io.IOUtils;
import com.openexchange.exception.OXException;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.userfeedback.ExportResult;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.ExportType;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.mail.FeedbackMailService;
import com.openexchange.userfeedback.mail.config.MailProperties;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;
import com.openexchange.userfeedback.mail.osgi.Services;

public class FeedbackMailServiceSMTP implements FeedbackMailService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FeedbackMailServiceSMTP.class);
    public static final String FILENAME = "feedback";
    public static final String FILE_TYPE = ".csv";
    private List<InternetAddress> invalidAddresses;

    @Override
    public String sendFeedbackMail(FeedbackMailFilter filter) {
        invalidAddresses = new ArrayList<>();
        String result = "Sending email(s) failed for unkown reason, please contact the administrator or see the server logs";
        try {
            File feedbackfile = getFeedbackfile(filter);
            if (feedbackfile != null) {
                result = sendMail(feedbackfile, filter);
            }
        } catch (OXException e) {
            e.printStackTrace();
            result = "Failure";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private File getFeedbackfile(FeedbackMailFilter filter) throws OXException, IOException {
        FeedbackService feedbackService = Services.getService(FeedbackService.class);
        ExportResultConverter feedbackProvider = feedbackService.export(filter.getCtxGroup(), filter);
        ExportResult feedbackResult = feedbackProvider.get(ExportType.CSV);
        // get the csv file
        File result = null;
        try (InputStream stream = (InputStream) feedbackResult.getResult()) {
            result = getFileFromStream(stream);
        }
        return result;
    }

    private String sendMail(File feedbackFile, FeedbackMailFilter filter) {
        Properties smtpProperties = getSMTPProperties();
        Session smtpSession = Session.getInstance(smtpProperties);
        Transport transport = null;
        
        String result = "";
        
        try {
            Address[] recipients = extractValidRecipients(filter);
            MimeMessage mail = createMailMessage(feedbackFile, filter, smtpProperties, smtpSession);
            transport = smtpSession.getTransport("smtp");
            transport.connect(MailProperties.getSmtpHostname(), MailProperties.getSmtpPort(), MailProperties.getSmtpUsername(), MailProperties.getSmtpPassword());
            transport.sendMessage(mail, recipients);
            result = getPositiveSendingResult(recipients);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            result = result.concat(e.getMessage());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }

    private MimeMessage createMailMessage(File feedbackFile, FeedbackMailFilter filter, Properties smtpProperties, Session session) throws MessagingException, IOException {
        MimeMessage email = new MimeMessage(session);
        
        email.setSubject(filter.getSubject());
        email.setFrom(new InternetAddress(MailProperties.getSenderAddress(), MailProperties.getSenderName()));
        
        BodyPart messageBody = new MimeBodyPart();
        messageBody.setText(filter.getBody());
        Multipart completeMailContent = new MimeMultipart(messageBody);
        MimeBodyPart attachment = new MimeBodyPart();
        attachment.attachFile(feedbackFile);
        completeMailContent.addBodyPart(attachment);
        email.setContent(completeMailContent);
        
        return email;
    }

    private Address[] extractValidRecipients(FeedbackMailFilter filter) throws UnsupportedEncodingException {
        Map<String, String> recipients = filter.getRecipients();
        InternetAddress[] result = new InternetAddress[recipients.size()];
        int index = 0;
        for (Entry<String, String> recipient : recipients.entrySet()) {
            InternetAddress address = new InternetAddress(recipient.getKey(), recipient.getValue());
            try {
                address.validate();
                result[index] = address;
                index++;
            } catch (AddressException e) {
                this.invalidAddresses.add(address);
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    private File getFileFromStream (InputStream stream) throws IOException {
        final File tempFile = File.createTempFile(FILENAME, FILE_TYPE);
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(stream, out);
        }
        return tempFile;
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
