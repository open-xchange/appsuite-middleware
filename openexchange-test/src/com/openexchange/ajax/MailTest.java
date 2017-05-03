
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mail.TestMail;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;

public class MailTest extends AbstractAJAXSession {

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
        } catch (final IOException e) {
            return null;
        }
    }

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss", Locale.GERMAN);

    private static final String MAILTEXT = "This is mail text!<br>Next line<br/><br/>best regards,<br>Max Mustermann";

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
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testSendSimpleMail() throws IOException, SAXException, Exception {
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", testUser2.getLogin());
        mailObj.put("to", testUser.getLogin());
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

        TestMail send = mtm.send(new TestMail(mailObj));
        assertNotNull(send);
        assertFalse(mtm.getLastResponse().hasError());
    }

    @Test
    public void testSendMailWithMultipleAttachment() throws IOException, SAXException, JSONException, Exception {
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", testUser2.getLogin());
        mailObj.put("to", testUser.getLogin());
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

        TestMail send = mtm.send(new TestMail(mailObj), new FileInputStream(createTempFile()));
        assertNotNull(send);
        assertFalse(mtm.getLastResponse().hasError());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testForwardMail() throws IOException, SAXException, JSONException, Exception {
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", testUser2.getLogin());
        mailObj.put("to", testUser.getLogin());
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
        mtm.forwardAndSendBefore(forwardMe);
        AbstractAJAXResponse jResp = mtm.getLastResponse();
        /*
         * Get forward mail for display
         */
        TestMail forwarded = mtm.get(forwardMe.getFolderAndId());
        assertTrue(forwarded != null);
    }

    @Test
    public void testSendForwardMailWithAttachments() throws IOException, SAXException, JSONException, Exception {
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", testUser2.getLogin());
        mailObj.put("to", testUser.getLogin());
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
        TestMail send = mtm.send(new TestMail(mailObj), new FileInputStream(createTempFile()));
        /*
         * Get forward mail for display
         */
        send.setSubject("Fwd: JUnit ForwardMe Mail with an attachment: " + SDF.format(new Date()));

        TestMail forwarded = mtm.forwardButDoNotSend(send);
        assertNotNull(forwarded);
        assertFalse(mtm.getLastResponse().hasError());
    }

    @Test
    public void testGetMails() throws IOException, SAXException, JSONException, Exception {
        AbstractAJAXResponse jResp = null;
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", testUser2.getLogin());
        mailObj.put("to", testUser.getLogin());
        mailObj.put("subject", "JUnit testGetMails Test Mail: " + SDF.format(new Date()));
        final JSONArray attachments = new JSONArray();
        /*
         * Mail text
         */
        final JSONObject attach = new JSONObject();
        attach.put("content", MAILTEXT);
        attach.put("content_type", "text/plain");
        attachments.put(attach);

        mailObj.put("attachments", attachments);

        /*
         * Send mail 10 times
         */
        TestMail mail = new TestMail(mailObj);
        mtm.send(mail);

        jResp = mtm.getLastResponse();
        assertFalse(jResp.hasError());
        for (int i = 2; i <= 10; i++) {
            mtm.send(mail);
            jResp = mtm.getLastResponse();
            assertFalse(jResp.hasError());
        }

        /*
         * Request mails
         */
        int[] columns = new int[] { MailListField.ID.getField() };

        MailMessage[] mails = mtm.listMails("INBOX", columns, MailListField.ID.getField(), Order.DESCENDING, false, "general");

        assertTrue(mails != null);
        assertTrue(mails.length == 10);
    }

    @Test
    public void testGetMsgSrc() throws IOException, SAXException, JSONException, Exception {
        AbstractAJAXResponse jResp = null;
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", testUser2.getLogin());
        mailObj.put("to", testUser.getLogin());
        mailObj.put("subject", "JUnit Source Test Mail: " + SDF.format(new Date()));
        final JSONArray attachments = new JSONArray();
        /*
         * Mail text
         */
        final JSONObject attach = new JSONObject();
        attach.put("content", MAILTEXT);
        attach.put("content_type", "text/plain");
        attachments.put(attach);

        mailObj.put("attachments", attachments);

        TestMail mail = mtm.send(new TestMail(mailObj));
        jResp = mtm.getLastResponse();
        assertFalse(jResp.hasError());

        final String mailidentifier = mail.getId();
        List<Parameter> additional = new ArrayList<>();
        additional.add(new Parameter(Mail.PARAMETER_SHOW_SRC, "true"));
        int[] columns = new int[] { MailListField.ID.getField() };
        MailMessage[] mails = mtm.listMails("INBOX", columns, MailListField.ID.getField(), Order.DESCENDING, false, additional);
        assertFalse(mtm.getLastResponse().hasError());
        assertTrue(mails != null);
        assertNotNull(mails[0].getSource());
    }
}
