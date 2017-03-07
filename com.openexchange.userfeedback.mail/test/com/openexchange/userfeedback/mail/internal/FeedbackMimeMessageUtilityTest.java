
package com.openexchange.userfeedback.mail.internal;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.userfeedback.mail.config.MailProperties;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;
import com.openexchange.userfeedback.mail.osgi.Services;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, MailProperties.class })
public class FeedbackMimeMessageUtilityTest {

    private final String TESTFILES_PATH = "./test/testfiles/";

    @Before
    public void setUp() throws Exception {
        
        PowerMockito.mockStatic(Services.class);
        
        PowerMockito.spy(MailProperties.class);
        PowerMockito.doReturn("sender@ox.de").when(MailProperties.class, "getSenderAddress");
        PowerMockito.doReturn("Sender").when(MailProperties.class, "getSenderName");
    }

    @Test
    public void createMailMessageTest_NoSecurity() throws OXException, IOException, MessagingException {
        FeedbackMimeMessageUtility messageUtility = new FeedbackMimeMessageUtility(false, false);
        MimeMessage mimeMessage = messageUtility.createMailMessage(new File(TESTFILES_PATH + "feedback.csv"), getDefaultFilter(), null);
        // Check if message has a multipart body with a plain text part and a file attachment
        Multipart content = (Multipart) mimeMessage.getContent();
        assertTrue(content.getCount() == 2);
        MimeBodyPart messageBodyPart = (MimeBodyPart) content.getBodyPart(0);
        assertTrue(messageBodyPart.getContentType().equals("text/plain"));
        MimeBodyPart fileBodyPart = (MimeBodyPart) content.getBodyPart(1);
        InputStream fileStream = (InputStream) fileBodyPart.getContent();
        assertTrue(fileStream != null);
        fileStream.close();
    }

    private FeedbackMailFilter getDefaultFilter() {
        return new FeedbackMailFilter("1", new HashMap<String, String>(), "subject", "Mail body", 0l, 0l, "");
    }

    @Test
    public void extractRecipientsTest_NoneNotNullResult() throws UnsupportedEncodingException {
        FeedbackMimeMessageUtility messageUtility = new FeedbackMimeMessageUtility(false, false);
        FeedbackMailFilter filter = getDefaultFilter();
        Address[] extractRecipients = messageUtility.extractRecipients(filter);
        assertTrue(extractRecipients != null);
    }

    @Test
    public void extractRecipientsTest_AdressNoPrivate() throws UnsupportedEncodingException {
        FeedbackMimeMessageUtility messageUtility = new FeedbackMimeMessageUtility(false, false);
        HashMap<String, String> recipients = new HashMap<>();
        final String recipient = "recipient1@ox.de";
        recipients.put(recipient, "");
        FeedbackMailFilter filter = new FeedbackMailFilter("1", recipients, "subject", "Mail body", 0l, 0l, "");
        InternetAddress[] extractRecipients = (InternetAddress[]) messageUtility.extractRecipients(filter);
        // created adress with empty personal information
        assertTrue(extractRecipients.length == 1);
        assertTrue(!extractRecipients[0].getAddress().isEmpty());
        assertTrue(extractRecipients[0].getPersonal().isEmpty());
    }

    @Test
    public void extractRecipientsTest_MultipleAdresses() throws UnsupportedEncodingException {
        FeedbackMimeMessageUtility messageUtility = new FeedbackMimeMessageUtility(false, false);
        HashMap<String, String> recipients = new HashMap<>();
        final String recipient = "recipient1@ox.de";
        recipients.put(recipient, "");
        final String recipient2 = "recipient2@ox.de";
        recipients.put(recipient2, "recipient2");
        FeedbackMailFilter filter = new FeedbackMailFilter("1", recipients, "subject", "Mail body", 0l, 0l, "");
        InternetAddress[] extractRecipients = (InternetAddress[]) messageUtility.extractRecipients(filter);
        // created adress with empty personal information
        assertTrue(extractRecipients.length == 2);
        assertTrue(!extractRecipients[0].getAddress().isEmpty());
        assertTrue(!extractRecipients[0].getPersonal().isEmpty());
        assertTrue(!extractRecipients[1].getAddress().isEmpty());
        assertTrue(extractRecipients[1].getPersonal().isEmpty());
    }
}
