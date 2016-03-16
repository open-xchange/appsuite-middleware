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

package com.openexchange.ajax;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import javax.activation.MimetypesFileTypeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieJar;
import com.openexchange.tools.URLParameter;

/**
 * AJAXFileUploadTest
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class AJAXFileUploadTest extends AbstractAJAXTest {

	private final static String URL = "/ajax/file";

	private final static String FILE_CONTENT = "A hash table supporting full concurrency of retrievals and adjustable expected concurrency for updates.\n"
			+ "This class obeys the same functional specification as Hashtable, and includes versions of methods corresponding to each method of Hashtable.\n"
			+ "However, even though all operations are thread-safe, retrieval operations do not entail locking, and there is not any support for locking the entire table in a way that prevents all access.\n"
			+ "This class is fully interoperable with Hashtable in programs that rely on its thread safety but not on its synchronization details.\n\n"
			+ "Retrieval operations (including get) generally do not block, so may overlap with update operations (including put and remove).\n"
			+ "Retrievals reflect the results of the most recently completed update operations holding upon their onset.\n"
			+ "For aggregate operations such as putAll and clear, concurrent retrievals may reflect insertion or removal of only some entries.\n"
			+ "Similarly, Iterators and Enumerations return elements reflecting the state of the hash table at some point at or since the creation of the iterator/enumeration.\n"
			+ "They do not throw ConcurrentModificationException. However, iterators are designed to be used by only one thread at a time.";

	private String sessionId;

	/**
	 * @param name
	 *            The name
	 */
	public AJAXFileUploadTest(final String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		sessionId = getSessionId();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		logout();
	}

	private static final String getUploadedFile(final WebConversation conversation, final String hostname,
			final String sessionId, final String id) throws IOException, SAXException {

		final GetMethodWebRequest getRequest = new GetMethodWebRequest(hostname + URL);
		getRequest.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		getRequest.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET);
		getRequest.setParameter(AJAXServlet.PARAMETER_ID, id);

		final WebResponse resp = conversation.getResponse(getRequest);
		return resp.getText();
	}

	private static final JSONObject uploadFiles(final WebConversation conversation, final String hostname,
			final String sessionId, final File[] files, final String module, final String fileFilter,
			final boolean setCookie) throws IOException, JSONException {
		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);
		parameter.setParameter(AJAXServlet.PARAMETER_MODULE, module);
		parameter.setParameter(AJAXServlet.PARAMETER_TYPE, fileFilter);

		WebRequest req = null;
		WebResponse resp = null;

		if (setCookie) {
			/*
			 * Add cookie
			 */
			final CookieJar cookieJar = new CookieJar();
			cookieJar.putCookie(LoginServlet.SESSION_PREFIX + sessionId, sessionId);
		}

		final PostMethodWebRequest postReq = new PostMethodWebRequest(hostname + URL + parameter.getURLParameters(), true);

		for (int i = 0; i < files.length; i++) {
			final File f = files[i];
			postReq.selectFile(new StringBuilder("file_").append(i).toString(), f, getFileContentType(f));
		}

		req = postReq;
		resp = conversation.getResource(req);
		if (resp.getResponseCode() >= 300) {
			throw new Error("Error Status Code " + resp.getResponseCode() + ": " + resp.getResponseMessage());
		}
		final JSONObject jResponse = extractFromCallback(resp.getText());
		return jResponse;
	}

	private static final String getFileContentType(final File f) {
		return new MimetypesFileTypeMap().getContentType(f);
	}

	private static final File createTempFile() {
		try {
			final File tmpFile = File.createTempFile("file_", ".txt");
			final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)));
			final BufferedReader reader = new BufferedReader(new StringReader(FILE_CONTENT));
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

	public void testUploadFile() {
		try {
			final File[] fa = { createTempFile(), createTempFile(), createTempFile() };
			final JSONObject jResp = uploadFiles(getWebConversation(), PROTOCOL + getHostName(), sessionId, fa,
					AJAXServlet.MODULE_MAIL, "file", false);

			assertTrue("JSON response is either null or has key \"error\"!", jResp != null && !jResp.has("error"));
			assertTrue("JSON response has no key \"data\"", jResp.has("data"));
			final JSONArray jArray = jResp.getJSONArray("data");
			assertTrue("Number of received IDs is " + jArray.length() + " but should be 3", jArray.length() == 3);
		} catch (final IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (final JSONException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testGetUploadedFile() {
		try {
			final File[] fa = { createTempFile() };
			final JSONObject jResp = uploadFiles(getWebConversation(), PROTOCOL + getHostName(), sessionId, fa,
					AJAXServlet.MODULE_MAIL, "file", false);

			assertTrue("JSON response is either null or has key \"error\"!", jResp != null && !jResp.has("error"));
			assertTrue("JSON response has no key \"data\"", jResp.has("data"));
			final JSONArray jArray = jResp.getJSONArray("data");
			assertTrue("Number of received IDs is " + jArray.length() + " but should be 1", jArray.length() == 1);

			final String id = jArray.getString(0);
			final String content = getUploadedFile(getWebConversation(), PROTOCOL + getHostName(), sessionId, id);

			assertTrue("File content was not present!", content != null && content.length() > 0);
			assertTrue("File content is not equal to expected one", FILE_CONTENT.replaceAll("\r?\n", "").equalsIgnoreCase(content.replaceAll("\r?\n", "")));

		} catch (final IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (final JSONException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (final SAXException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
