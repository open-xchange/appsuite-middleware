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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.test.osgi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginTest;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.URLParameter;

/**
 * {@link BundleTestEventAdmin} - Test absence of event admin bundle
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class BundleTestEventAdmin extends AbstractBundleTest {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(BundleTestEventAdmin.class);

	private static final String BUNDLE_ID = "org.eclipse.equinox.event";

	private static final String APPOINTMENT_URL = "/ajax/calendar";

	private static final String FOLDER_URL = "/ajax/folders";

	/**
	 * Initializes a new {@link BundleTestEventAdmin}
	 */
	public BundleTestEventAdmin(final String name) {
		super(name);
	}

	public void testEventAdminAbsence() {
		try {
			final LoginTest loginTest = new LoginTest("LoginTest");
			final JSONObject loginObject = login(getWebConversation(), loginTest.getHostName(), loginTest.getLogin(),
					loginTest.getPassword());

			/*
			 * Login should work without problems
			 */
			assertTrue("Error contained in returned JSON object", !loginObject.has("error")
					|| loginObject.isNull("error"));

			/*
			 * Check for session ID
			 */
			assertTrue("Missing session ID", loginObject.has("session") && !loginObject.isNull("session"));
			final String sessionId = loginObject.getString("session");

			/*
			 * Check behavior if inserting a new appointment
			 */
			final AppointmentObject newApp = createAppointmentObject("TestAppointment", System.currentTimeMillis(),
					System.currentTimeMillis() + 3600l, getStandardCalendarFolder(getWebConversation(), loginTest
							.getHostName(), sessionId));
			final JSONObject appointmentObject = insertAppointment(getWebConversation(), newApp, TimeZone
					.getTimeZone("UTC"), loginTest.getHostName(), sessionId);

			/*
			 * Check for error
			 */
			assertTrue("No error contained in returned JSON object", appointmentObject.has("error")
					&& !appointmentObject.isNull("error"));

		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private static JSONObject insertAppointment(final WebConversation webCon, final AppointmentObject appointmentObj,
			final TimeZone userTimeZone, final String host, final String session) throws JSONException,
			MalformedURLException, IOException, SAXException {
		final StringWriter stringWriter = new StringWriter();

		final JSONObject jsonObj = new JSONObject();
		final AppointmentWriter appointmentwriter = new AppointmentWriter(userTimeZone);
		appointmentwriter.writeAppointment(appointmentObj, jsonObj);

		stringWriter.write(jsonObj.toString());
		stringWriter.flush();

		final URLParameter parameter = new URLParameter();
		parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
		parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);

		final ByteArrayInputStream bais = new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8"));
		final WebRequest req = new PutMethodWebRequest(
				PROTOCOL + host + APPOINTMENT_URL + parameter.getURLParameters(), bais, "text/javascript");
		final WebResponse resp = webCon.getResponse(req);
		assertEquals("Response code is not okay.", HttpServletResponse.SC_OK, resp.getResponseCode());
		final String body = resp.getText();
		final JSONObject json;
		try {
			json = new JSONObject(body);
		} catch (final JSONException e) {
			LOG.error("Can't parse this body to JSON: \"" + body + '\"');
			throw e;
		}
		return json;
	}

	private static AppointmentObject createAppointmentObject(String title, long startTime, long endTime,
			int appointmentFolderId) {
		AppointmentObject appointmentobject = new AppointmentObject();
		appointmentobject.setTitle(title);
		appointmentobject.setStartDate(new Date(startTime));
		appointmentobject.setEndDate(new Date(endTime));
		appointmentobject.setLocation("Location");
		appointmentobject.setShownAs(AppointmentObject.ABSENT);
		appointmentobject.setParentFolderID(appointmentFolderId);

		return appointmentobject;
	}

	private static int getStandardCalendarFolder(final WebConversation conversation, final String hostname,
			final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		List<FolderObject> subfolders = getSubfolders(conversation, hostname, sessionId, ""
				+ FolderObject.SYSTEM_PRIVATE_FOLDER_ID, false, true);
		for (Iterator<FolderObject> iter = subfolders.iterator(); iter.hasNext();) {
			FolderObject subfolder = iter.next();
			if (subfolder.getModule() == FolderObject.CALENDAR && subfolder.isDefaultFolder()) {
				return subfolder.getObjectID();
			}
		}
		return -1;
	}

	private static List<FolderObject> getSubfolders(final WebConversation conversation, final String hostname,
			final String sessionId, final String parentIdentifier, final boolean printOutput, boolean ignoreMailfolder)
			throws MalformedURLException, IOException, SAXException, JSONException, OXException {
		final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname + FOLDER_URL);
		req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
		req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
		req.setParameter("parent", parentIdentifier);
		String columns = FolderObject.OBJECT_ID + "," + FolderObject.MODULE + "," + FolderObject.FOLDER_NAME + ","
				+ FolderObject.SUBFOLDERS + "," + FolderObject.STANDARD_FOLDER + "," + FolderObject.CREATED_BY;
		req.setParameter(AJAXServlet.PARAMETER_COLUMNS, columns);

		if (ignoreMailfolder) {
			req.setParameter(AJAXServlet.PARAMETER_IGNORE, "mailfolder");
		}

		final WebResponse resp = conversation.getResponse(req);
		JSONObject respObj = new JSONObject(resp.getText());
		if (printOutput)
			System.out.println(respObj.toString());
		if (respObj.has("error") && !respObj.isNull("error")) {
			throw new OXException("Error occured: " + respObj.getString("error"));
		}
		if (!respObj.has("data") || respObj.isNull("data")) {
			throw new OXException("Error occured: Missing key \"data\"");
		}
		JSONArray data = respObj.getJSONArray("data");
		final List<FolderObject> folders = new ArrayList<FolderObject>();
		for (int i = 0; i < data.length(); i++) {
			JSONArray arr = data.getJSONArray(i);
			FolderObject subfolder = new FolderObject();
			try {
				subfolder.setObjectID(arr.getInt(0));
			} catch (JSONException exc) {
				subfolder.removeObjectID();
				subfolder.setFullName(arr.getString(0));
			}
			subfolder.setModule(FolderParser.getModuleFromString(arr.getString(1),
					subfolder.containsObjectID() ? subfolder.getObjectID() : -1));
			subfolder.setFolderName(arr.getString(2));
			subfolder.setSubfolderFlag(arr.getBoolean(3));
			subfolder.setDefaultFolder(arr.getBoolean(4));
			if (!arr.isNull(5))
				subfolder.setCreatedBy(arr.getInt(5));
			folders.add(subfolder);
		}
		return folders;
	}

	@Override
	protected String getBundleName() {
		return BUNDLE_ID;
	}

}
