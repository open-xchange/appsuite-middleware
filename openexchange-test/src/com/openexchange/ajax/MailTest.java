
package com.openexchange.ajax;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractClientSession;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.ajax.mail.TestMail;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.test.common.test.TestClassConfig;

public class MailTest extends AbstractClientSession {

    private static final String INBOX = "default0/INBOX";

    private static final StringBuilder FILE_CONTENT_BUILDER;

    static {
        FILE_CONTENT_BUILDER = new StringBuilder();
        FILE_CONTENT_BUILDER.append("First line of text file").append("\r\n");
        FILE_CONTENT_BUILDER.append("Second line of text file").append("\r\n");
        FILE_CONTENT_BUILDER.append("Third line of text file").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
        FILE_CONTENT_BUILDER.append("Blah blah blah blah blah blah blah blah bah blah foobar").append("\r\n");
    }

    public static File createTempFile() {
        try {
            final File tmpFile = File.createTempFile("file_", ".txt");
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)));
            final BufferedReader reader = new BufferedReader(new StringReader(FILE_CONTENT_BUILDER.toString()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                writer.write(new StringBuilder(line).append("\r\n").toString());
            }
            reader.close();
            writer.flush();
            writer.close();
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (IOException e) {
            return null;
        }
    }

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss", Locale.GERMAN);

    private static final String MAILTEXT = "This is mail text!<br>Next line<br/><br/>best regards,<br>Max Mustermann";

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().withUserPerContext(2).createAjaxClient().build();
    }

    protected MailTestManager mtm;
    protected MailTestManager mtm2;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mtm = new MailTestManager(getClient());
        mtm2 = new MailTestManager(testUser2.getAjaxClient());
    }

    @Test
    public void testFail() {
        try {
            final JSONObject mailObj = new JSONObject();
            final JSONArray attachments = new JSONArray();
            /*
             * Mail text
             */
            final JSONObject attach = new JSONObject();
            attach.put("content", MAILTEXT);
            attach.put("content_type", "text/plain");
            attachments.put(attach);

            mailObj.put("attachments", attachments);

            mtm.send(new TestMail(mailObj));
            AbstractAJAXResponse jResp = mtm.getLastResponse();
            assertTrue(jResp == null || jResp.hasError());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private String getMailOfUser2() {
        return testUser2.getLogin();
    }

    @Test
    public void testSendSimpleMail() throws IOException, Exception {
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", getMailOfUser2());
        setToAddress(mailObj);
        mailObj.put("subject", "JUnit Test Mail: " + SDF.format(new Date()));
        final JSONArray attachments = new JSONArray();
        /*
         * Mail text
         */
        final JSONObject attach = new JSONObject();
        attach.put("content", MAILTEXT);
        attach.put("content_type", "text/plain");
        attachments.put(attach);

        mailObj.put("attachments", attachments);

        TestMail send = mtm2.send(new TestMail(mailObj));
        assertFalse(mtm2.getLastResponse().getErrorMessage(), mtm2.getLastResponse().hasError());
        assertNotNull(send);
    }

    @Test
    public void testSendMailWithMultipleAttachment() throws IOException, JSONException, Exception {
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", getMailOfUser2());
        setToAddress(mailObj);
        mailObj.put("subject", "JUnit Test Mail with an attachment: " + SDF.format(new Date()));
        final JSONArray attachments = new JSONArray();
        /*
         * Mail text
         */
        final JSONObject attach = new JSONObject();
        attach.put("content", "Mail text");
        attach.put("content_type", "text/plain");
        attachments.put(attach);
        mailObj.put("attachments", attachments);

        TestMail send = mtm2.send(new TestMail(mailObj), new FileInputStream(createTempFile()));
        assertFalse(mtm2.getLastResponse().getErrorMessage(), mtm2.getLastResponse().hasError());
        assertNotNull(send);
    }

    @Test
    public void testForwardMail() throws IOException, JSONException, Exception {
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", getMailOfUser2());
        setToAddress(mailObj);
        mailObj.put("subject", "JUnit Test Mail with an attachment: " + SDF.format(new Date()));
        final JSONArray attachments = new JSONArray();
        /*
         * Mail text
         */
        final JSONObject attach = new JSONObject();
        attach.put("content", "Mail text");
        attach.put("content_type", "text/plain");
        attachments.put(attach);
        mailObj.put("attachments", attachments);
        /*
         * Upload files
         */
        TestMail forwardMe = new TestMail(mailObj);
        assertNotNull(forwardMe);
        mtm2.forwardAndSendBefore(forwardMe);
        assertFalse(mtm2.getLastResponse().getErrorMessage(), mtm2.getLastResponse().hasError());
        /*
         * Get forward mail for display
         */
        int[] columns = new int[] { MailListField.ID.getField() };

        MailMessage[] mails = mtm.listMails(INBOX, columns, MailListField.ID.getField(), Order.DESCENDING, false, "general");
        assertNotNull(mails);
        assertTrue(mails.length > 0);
    }

    @Test
    public void testSendForwardMailWithAttachments() throws IOException, JSONException, Exception {
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", getMailOfUser2());
        setToAddress(mailObj);
        mailObj.put("subject", "JUnit ForwardMe Mail with an attachment: " + SDF.format(new Date()));
        final JSONArray attachments = new JSONArray();
        /*
         * Mail text
         */
        final JSONObject attach = new JSONObject();
        attach.put("content", "Mail text");
        attach.put("content_type", "text/plain");
        attachments.put(attach);
        mailObj.put("attachments", attachments);
        /*
         * Upload files
         */
        TestMail send = mtm2.send(new TestMail(mailObj), new FileInputStream(createTempFile()));
        assertFalse(mtm2.getLastResponse().getErrorMessage(), mtm2.getLastResponse().hasError());
        assertNotNull(send);
        /*
         * Get forward mail for display
         */
        send.setSubject("Fwd: JUnit ForwardMe Mail with an attachment: " + SDF.format(new Date()));

        TestMail forwarded = mtm2.forwardButDoNotSend(send);
        assertFalse(mtm2.getLastResponse().hasError());
        assertNotNull(forwarded);
    }

    @Test
    public void testGetMails() throws IOException, SAXException, JSONException, Exception {
        AbstractAJAXResponse jResp = null;
        JSONObject mailObj = new JSONObject();
        mailObj.put("from", getMailOfUser2());
        setToAddress(mailObj);
        String subject = "JUnit testGetMails Test Mail: " + UUID.randomUUID().toString();
        mailObj.put("subject", subject);
        JSONArray attachments = new JSONArray();
        /*
         * Mail text
         */
        JSONObject attach = new JSONObject();
        attach.put("content", MAILTEXT);
        attach.put("content_type", "text/plain");
        attachments.put(attach);

        mailObj.put("attachments", attachments);
        

        /*
         * Send mail 10 times
         */
        TestMail mail = new TestMail(mailObj);
        mtm2.send(mail);

        jResp = mtm2.getLastResponse();
        assertFalse(jResp.getErrorMessage(), jResp.hasError());
        for (int i = 2; i <= 10; i++) {
            mtm2.send(mail);
            jResp = mtm2.getLastResponse();
            assertFalse(jResp.getErrorMessage(), jResp.hasError());
        }

        /*
         * Request mails
         */
        int[] columns = new int[] { MailListField.ID.getField(), MailListField.SUBJECT.getField() };

        MailMessage[] mails = mtm.listMails(INBOX, columns, MailListField.ID.getField(), Order.DESCENDING, false, "general");
        assertTrue(mails != null);
        int i = 0;
        for (MailMessage mailMessage : mails) {
            if (subject.equalsIgnoreCase(mailMessage.getSubject())) {
                i++;
            }
        }
        assertTrue("Should have found 10 mails but were " + mails.length, 10 == i);
    }

    private void setToAddress(JSONObject mailObj) throws JSONException {
        JSONArray to = new JSONArray();
        to.add(0, testUser.getLogin());
        mailObj.put("to", to);
    }
}
