package com.openexchange.ajax;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieJar;
import com.openexchange.ajax.container.Response;
import com.openexchange.tools.URLParameter;

public class MailTest extends AbstractAJAXTest {

	private String sessionId = null;

	public MailTest(final String name) {
		super(name);
	}

	private static final String MAIL_URL = "/ajax/mail";

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


	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception{
		sessionId = getSessionId();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception{
		logout();
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

	public static JSONObject sendMail(final WebConversation conversation, final String hostname, final String sessionId,
			final JSONObject mailObj, final File[] uploadFileAttachments, final boolean setCookie) throws Exception {
		return sendMail(conversation, hostname, sessionId, mailObj.toString(), uploadFileAttachments, setCookie);
	}

	public static JSONObject sendMail(final WebConversation conversation, final String hostname, final String sessionId,
			final String mailObjStr, final File[] uploadFileAttachments, final boolean setCookie) throws Exception {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);

		WebRequest req = null;
		WebResponse resp = null;

		if (setCookie) {
			/*
			 * Add cookie
			 */
			final CookieJar cookieJar = new CookieJar();
			cookieJar.putCookie(LoginServlet.SESSION_PREFIX + sessionId, sessionId);
		}

		final PostMethodWebRequest postReq = new PostMethodWebRequest(hostname + MAIL_URL + parameter.getURLParameters(), true);
		postReq.setParameter("json_0", mailObjStr);

		JSONObject jResponse = null;
		if (uploadFileAttachments != null) {
			for (int i = 0; i < uploadFileAttachments.length; i++) {
				final File f = uploadFileAttachments[i];
				postReq.selectFile(new StringBuilder("file_").append(i).toString(), f, getFileContentType(f));
			}
		}
		req = postReq;
		resp = conversation.getResource(req);
		jResponse = extractFromCallback(resp.getText());
		return jResponse;
	}

	public static JSONObject forwardMail(final WebConversation conversation, final String hostname, final String sessionId,
			final JSONObject mailObj, final String msgRef, final File[] uploadFileAttachments) throws Exception {
		if (!mailObj.has("msgRef") || mailObj.isNull("msgRef")) {
			mailObj.putOpt("msgRef", msgRef);
		}
		return sendMail(conversation, hostname, sessionId, mailObj.toString(), uploadFileAttachments, false);
	}

	public static JSONObject getForwardMailForDisplay(final WebConversation conversation, final String hostname, final String sessionId,
			final String mailIdentifier, final boolean addCookie) throws Exception {
		final GetMethodWebRequest getReq = new GetMethodWebRequest(hostname + MAIL_URL);
		getReq.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		getReq.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_FORWARD);
		getReq.setParameter(AJAXServlet.PARAMETER_ID, mailIdentifier);
		if (addCookie) {
			/*
			 * Add cookie
			 */
			final CookieJar cookieJar = new CookieJar();
			cookieJar.putCookie(LoginServlet.SESSION_PREFIX + sessionId, sessionId);
		}
		/*
		 * Submit response
		 */
		final WebResponse resp = conversation.getResponse(getReq);
		final JSONObject jResponse = new JSONObject(resp.getText());
		return jResponse;
	}

	public static JSONObject deleteMail(final WebConversation conversation, final String hostname, final String sessionId,
			final String mailIdentifier, final boolean addCookie) throws Exception {
		/*
		 * Parameters
		 */
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_DELETE);
		/*
		 * Create InputStream
		 */
		final byte[] bytes = new StringBuilder(50).append('[').append("{\"id\":\"").append(mailIdentifier).append("\"}").append(']').toString().getBytes(com.openexchange.java.Charsets.UTF_8);
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		/*
		 * Define the request
		 */
		final PutMethodWebRequest putReq = new PutMethodWebRequest(hostname + MAIL_URL + parameter.getURLParameters(), bais, "text/javascript; charset=UTF-8");
		if (addCookie) {
			final CookieJar cookieJar = new CookieJar();
			cookieJar.putCookie(LoginServlet.SESSION_PREFIX + sessionId, sessionId);
		}
		final WebResponse resp = conversation.getResponse(putReq);
		final JSONObject jResponse = new JSONObject(resp.getText());
		return jResponse;
	}

	public static JSONObject getAllMails(final WebConversation conversation, final String hostname,
			final String sessionId, final String folder, final int[] cols, final boolean setCookie) throws IOException, SAXException, JSONException {
		return getAllMails(conversation, null, hostname, sessionId, folder, cols, setCookie);
	}

	public static JSONObject getAllMails(final WebConversation conversation, final String protocol, final String hostname,
			final String sessionId, final String folder, final int[] cols, final boolean setCookie) throws IOException, SAXException, JSONException {
		final GetMethodWebRequest getReq = new GetMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://")) + hostname + MAIL_URL);
		if (setCookie) {
			/*
			 * Set cookie cause a request has already been fired before with the same session id.
			 */
			final CookieJar cookieJar = new CookieJar();
			cookieJar.putCookie(LoginServlet.SESSION_PREFIX + sessionId, sessionId);
		}
		getReq.setParameter(Mail.PARAMETER_SESSION, sessionId);
		getReq.setParameter(Mail.PARAMETER_ACTION, Mail.ACTION_ALL);
		getReq.setParameter(Mail.PARAMETER_MAILFOLDER, folder);
		final String colsStr;
		if (cols != null && cols.length > 0) {
			final StringBuilder sb = new StringBuilder();
			sb.append(cols[0]);
			for (int i = 1; i < cols.length; i++) {
				//sb.append("%2C").append(cols[i]);
				sb.append(',').append(cols[i]);
			}
			colsStr = sb.toString();
		} else {
			colsStr = "600,601,602,612,603,607,610,608,611,614,102";
		}
		getReq.setParameter(Mail.PARAMETER_COLUMNS, colsStr);
		getReq.setParameter(Mail.PARAMETER_SORT, "610");
		getReq.setParameter(Mail.PARAMETER_ORDER, "asc");
		final WebResponse resp = conversation.getResponse(getReq);
		final JSONObject jResponse = new JSONObject(resp.getText());
		return jResponse;
	}

    public static Response getMail(final WebConversation conversation,
            final String hostName, final String sessionId, final String mailId)
            throws IOException, SAXException, JSONException {
    	return getMail(conversation, null, hostName, sessionId, mailId);
    }

    public static Response getMail(final WebConversation conversation,
        final String protocol, final String hostName, final String sessionId, final String mailId)
        throws IOException, SAXException, JSONException {
        final GetMethodWebRequest getReq = new GetMethodWebRequest(((null == protocol) ? PROTOCOL : (protocol + "://"))
            + hostName + MAIL_URL);
        getReq.setParameter(Mail.PARAMETER_SESSION, sessionId);
        getReq.setParameter(Mail.PARAMETER_ACTION, Mail.ACTION_GET);
        getReq.setParameter(Mail.PARAMETER_ID, mailId);
        final WebResponse resp = conversation.getResponse(getReq);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response;
    }

	private static String getFileContentType(final File f) {
		return new MimetypesFileTypeMap().getContentType(f);
	}

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss", Locale.GERMAN);

	private static final String MAILTEXT = "This is mail text!<br>Next line<br/><br/>best regards,<br>Max Mustermann";

	public void testFail() {
		try {
			JSONObject jResp = null;

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

			jResp = sendMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailObj, null, false);
			assertTrue(jResp == null || jResp.has("error"));
		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testSendSimpleMail() throws IOException, SAXException, Exception {
		JSONObject jResp = null;
		try {
			final JSONObject mailObj = new JSONObject();
			mailObj.put("from", getSeconduser());
			mailObj.put("to", getLogin());
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

			jResp = sendMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailObj, null, false);
			assertTrue(jResp != null);
		} finally {
			if (jResp != null && jResp.has("data") && !jResp.isNull("data")) {
				final String mailIdentifer = jResp.getString("data");
				try {
					deleteMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailIdentifer, true);
				} catch (final Exception e) {
					System.err.println("Test-Mail \"" + mailIdentifer + "\" could NOT be deleted!");
					e.printStackTrace();
				}
			}
		}
	}

	public void testSendMailWithMultipleAttachment() throws IOException, SAXException, JSONException, Exception {
		JSONObject jResp = null;
		try {
			final JSONObject mailObj = new JSONObject();
			mailObj.put("from", getSeconduser());
			mailObj.put("to", getLogin());
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

			final File[] fa = { createTempFile(), createTempFile(), createTempFile() };

			jResp = sendMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailObj, fa, false);
			assertTrue(jResp != null);
		} finally {
			if (jResp != null && jResp.has("data") && !jResp.isNull("data")) {
				final String mailIdentifer = jResp.getString("data");
				deleteMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailIdentifer, true);
			}
		}
	}

	public void testForwardMail() throws IOException, SAXException, JSONException, Exception {
		JSONObject jResp = null;
		String mailIdentifer = null;
		try {
			final JSONObject mailObj = new JSONObject();
			mailObj.put("from", getSeconduser());
			mailObj.put("to", getLogin());
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
			final File[] fa = { createTempFile(), createTempFile(), createTempFile() };
			jResp = sendMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailObj, fa, false);
			mailIdentifer = jResp.getString("data");
			/*
			 * Get forward mail for display
			 */
			jResp = getForwardMailForDisplay(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailIdentifer, true);
			assertTrue(jResp != null);
			/*
			 *
			 */
		} finally {
			if (mailIdentifer != null) {
			    deleteMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailIdentifer, true);
			}
		}
	}

	public void testSendForwardMailWithAttachments() throws IOException, SAXException, JSONException, Exception {
		JSONObject jResp = null;
		String mailIdentifer = null;
		try {
			final JSONObject mailObj = new JSONObject();
			mailObj.put("from", getSeconduser());
			mailObj.put("to", getLogin());
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
			File[] fa = { createTempFile(), createTempFile(), createTempFile() };
			jResp = sendMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailObj, fa, false);
			mailIdentifer = jResp.getString("data");
			/*
			 * Get forward mail for display
			 */
			mailObj.put("subject", "Fwd: JUnit ForwardMe Mail with an attachment: " + SDF.format(new Date()));
			fa = new File[] { createTempFile() };
			jResp = forwardMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailObj, mailIdentifer, fa);
			assertTrue(jResp != null);
			/*
			 *
			 */
		} finally {
			if (mailIdentifer != null) {
			    deleteMail(getWebConversation(), MailTest.PROTOCOL + getHostName(), getSessionId(), mailIdentifer, true);
			}
		}
	}

	public void testNewMailsInInbox() {
		assertTrue(true);
	}

	public void testGetMails() throws IOException, SAXException, JSONException, Exception {
	    JSONObject jResp = null;
		final JSONObject mailObj = new JSONObject();
		mailObj.put("from", getSeconduser());
		mailObj.put("to", getLogin());
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

		final WebConversation conversation = getWebConversation();
		/*
		 * Send mail 10 times
		 */
		jResp = sendMail(conversation, MailTest.PROTOCOL + getHostName(), getSessionId(), mailObj, null, false);
		assertFalse(jResp.has("error"));
		for (int i = 2; i <= 10; i++) {
			jResp = sendMail(conversation, MailTest.PROTOCOL + getHostName(), getSessionId(), mailObj, null, true);
			assertFalse(jResp.has("error"));
		}

		/*
		 * Request mails
		 */
		jResp = getAllMails(conversation, getHostName(), getSessionId(), "INBOX", null, true);

		assertTrue(jResp != null);
	}

	public void testGetMsgSrc() throws IOException, SAXException, JSONException, Exception {
		JSONObject jResp = null;
		final JSONObject mailObj = new JSONObject();
		mailObj.put("from", getSeconduser());
		mailObj.put("to", getLogin());
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

		final WebConversation conversation = getWebConversation();
		/*
		 * Send mail 10 times
		 */
		jResp = sendMail(conversation, MailTest.PROTOCOL + getHostName(), getSessionId(), mailObj, null, false);
		assertFalse(jResp.has("error"));

		final String mailidentifier = jResp.getString("data");
		final GetMethodWebRequest getReq = new GetMethodWebRequest(MailTest.PROTOCOL + getHostName() + MAIL_URL);
		/*
		 * Set cookie cause a request has already been fired before with the same session id.
		 */
		final CookieJar cookieJar = new CookieJar();
		cookieJar.putCookie(LoginServlet.SESSION_PREFIX + getSessionId(), getSessionId());
		getReq.setParameter(Mail.PARAMETER_SESSION, getSessionId());
		getReq.setParameter(Mail.PARAMETER_ACTION, Mail.ACTION_GET);
		getReq.setParameter(Mail.PARAMETER_ID, mailidentifier);
		getReq.setParameter(Mail.PARAMETER_SHOW_SRC, "true");
		final WebResponse resp = conversation.getResponse(getReq);
		jResp = new JSONObject(resp.getText());
		assertFalse(jResp.has("error"));
	}

	public void testReplyMail() {
		assertTrue(true);
	}
}
