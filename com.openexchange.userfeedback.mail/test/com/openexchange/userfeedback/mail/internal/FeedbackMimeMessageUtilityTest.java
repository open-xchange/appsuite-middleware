
package com.openexchange.userfeedback.mail.internal;

import static org.junit.Assert.assertTrue;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.pgp.keys.parsing.KeyRingParserResult;
import com.openexchange.pgp.keys.parsing.PGPKeyRingParser;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;
import com.openexchange.userfeedback.mail.osgi.Services;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Services.class, LeanConfigurationService.class })
public class FeedbackMimeMessageUtilityTest {

    private final String TESTFILES_PATH = "./test/testfiles/";

    @Mock
    LeanConfigurationService leanConfigurationService;
    @Mock
    PGPKeyRingParser pgpKeyRingParser;
    @Mock
    KeyRingParserResult keyRingParserResult;

    @Before
    public void setUp() throws Exception {

        PowerMockito.mockStatic(Services.class);
        PowerMockito.when(Services.getService(LeanConfigurationService.class)).thenReturn(leanConfigurationService);
        PowerMockito.when(Services.getService(PGPKeyRingParser.class)).thenReturn(pgpKeyRingParser);

        PowerMockito.when(leanConfigurationService.getProperty(UserFeedbackMailProperty.senderAddress)).thenReturn("sender@ox.de");
        PowerMockito.when(leanConfigurationService.getProperty(UserFeedbackMailProperty.senderName)).thenReturn("Sender");
    }

    @Test
    public void createMailMessageTest_NoSecurity() throws OXException, IOException, MessagingException {
        MimeMessage mimeMessage = FeedbackMimeMessageUtility.createMailMessage(new FileInputStream(TESTFILES_PATH + "feedback.csv"), getDefaultFilter(), null);
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
        return new FeedbackMailFilter("1", new HashMap<String, String>(), "subject", "Mail body", 0l, 0l, "", false);
    }

    private FeedbackMailFilter getCompressedFilter() {
        return new FeedbackMailFilter("1", new HashMap<String, String>(), "subject", "Mail body", 0l, 0l, "", true);
    }

    @Test
    public void createMailMessageTest_Compressed() throws OXException, IOException, MessagingException {
        MimeMessage mimeMessage = FeedbackMimeMessageUtility.createMailMessage(new FileInputStream(TESTFILES_PATH + "feedback.csv"), getCompressedFilter(), null);
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

    @Test
    public void extractRecipientsTest_NoneNotNullResult() throws UnsupportedEncodingException, OXException {
        FeedbackMailFilter filter = getDefaultFilter();
        Address[] extractRecipients = FeedbackMimeMessageUtility.extractValidRecipients(filter, new ArrayList<InternetAddress>());
        assertTrue(extractRecipients != null);
    }

    @Test
    public void extractRecipientsTest_AdressNoPrivate() throws UnsupportedEncodingException, OXException {
        HashMap<String, String> recipients = new HashMap<>();
        final String recipient = "recipient1@ox.de";
        recipients.put(recipient, "");
        FeedbackMailFilter filter = new FeedbackMailFilter("1", recipients, "subject", "Mail body", 0l, 0l, "", false);
        InternetAddress[] extractRecipients = (InternetAddress[]) FeedbackMimeMessageUtility.extractValidRecipients(filter, new ArrayList<InternetAddress>());
        // created adress with empty personal information
        assertTrue(extractRecipients.length == 1);
        assertTrue(!extractRecipients[0].getAddress().isEmpty());
        assertTrue(extractRecipients[0].getPersonal().isEmpty());
    }

    @Test
    public void extractRecipientsTest_MultipleAdresses() throws UnsupportedEncodingException, OXException {
        HashMap<String, String> recipients = new HashMap<>();
        final String recipient = "recipient1@ox.de";
        recipients.put(recipient, "");
        final String recipient2 = "recipient2@ox.de";
        recipients.put(recipient2, "recipient2");
        FeedbackMailFilter filter = new FeedbackMailFilter("1", recipients, "subject", "Mail body", 0l, 0l, "", false);
        InternetAddress[] extractRecipients = (InternetAddress[]) FeedbackMimeMessageUtility.extractValidRecipients(filter, new ArrayList<InternetAddress>());
        // created adress with empty personal information
        assertTrue(extractRecipients.length == 2);
        assertTrue(!extractRecipients[0].getAddress().isEmpty());
        assertTrue(!extractRecipients[0].getPersonal().isEmpty());
        assertTrue(!extractRecipients[1].getAddress().isEmpty());
        assertTrue(extractRecipients[1].getPersonal().isEmpty());
    }
    
    @Test
    public void extractRecipientsForPgpTest_invalidAddress() throws OXException {
        HashMap<String, String> recipients = new HashMap<>();
        final String recipient = "recipient1ox.de";
        recipients.put(recipient, "");
        HashMap<String, String> pgpKeys = new HashMap<String, String>();
        pgpKeys.put("recipient", "key");
        FeedbackMailFilter filter = new FeedbackMailFilter("1", recipients, pgpKeys,"subject", "Mail body", 0l, 0l, "", false);
        List<InternetAddress> invalidAddresses = new ArrayList<>();
        List<InternetAddress> pgpFailedAddresses = new ArrayList<>();
        Map<Address, PGPPublicKey> extractRecipientsForPgp = FeedbackMimeMessageUtility.extractRecipientsForPgp(filter, invalidAddresses, pgpFailedAddresses);
        assertTrue("Invalid address passed", extractRecipientsForPgp.size() == 0);
        assertTrue("Invalid address not recognized", invalidAddresses.size() == 1);
    }
    
    @Test
    public void extractRecipientsForPgpTest_failedPGPAddress() throws OXException, IOException {
        
        PowerMockito.when(pgpKeyRingParser.parse(org.mockito.Matchers.any(InputStream.class))).thenReturn(keyRingParserResult);
        PowerMockito.when(keyRingParserResult.toEncryptionKey()).thenReturn(null);
        
        HashMap<String, String> recipients = new HashMap<>();
        final String recipient = "recipient1@ox.de";
        recipients.put(recipient, "");
        HashMap<String, String> pgpKeys = new HashMap<String, String>();
        pgpKeys.put("recipient1@ox.de", "key");
        FeedbackMailFilter filter = new FeedbackMailFilter("1", recipients, pgpKeys,"subject", "Mail body", 0l, 0l, "", false);
        List<InternetAddress> invalidAddresses = new ArrayList<>();
        List<InternetAddress> pgpFailedAddresses = new ArrayList<>();
        Map<Address, PGPPublicKey> extractRecipientsForPgp = FeedbackMimeMessageUtility.extractRecipientsForPgp(filter, invalidAddresses, pgpFailedAddresses);
        assertTrue("Invalid address passed", extractRecipientsForPgp.size() == 0);
        assertTrue("Invalid address not recognized", pgpFailedAddresses.size() == 1);
    }
    
    @Test
    public void extractRecipientsForPgpTest_validPGPAddress() throws OXException, IOException, PGPException {
        PGPPublicKey pgpPublicKey = PowerMockito.mock(PGPPublicKey.class);
        
        PowerMockito.when(pgpKeyRingParser.parse(org.mockito.Matchers.any(InputStream.class))).thenReturn(keyRingParserResult);
        PowerMockito.when(keyRingParserResult.toEncryptionKey()).thenReturn(pgpPublicKey);
        
        HashMap<String, String> recipients = new HashMap<>();
        final String recipient = "recipient1@ox.de";
        recipients.put(recipient, "");
        HashMap<String, String> pgpKeys = new HashMap<String, String>();
        pgpKeys.put("recipient1@ox.de", "key");
        FeedbackMailFilter filter = new FeedbackMailFilter("1", recipients, pgpKeys,"subject", "Mail body", 0l, 0l, "", false);
        List<InternetAddress> invalidAddresses = new ArrayList<>();
        List<InternetAddress> pgpFailedAddresses = new ArrayList<>();
        Map<Address, PGPPublicKey> extractRecipientsForPgp = FeedbackMimeMessageUtility.extractRecipientsForPgp(filter, invalidAddresses, pgpFailedAddresses);
        assertTrue("Failed to add valid address with PGP key", extractRecipientsForPgp.size() == 1);
    }
}
