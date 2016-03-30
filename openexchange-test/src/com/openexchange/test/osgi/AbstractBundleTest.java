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

package com.openexchange.test.osgi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.control.console.StartBundle;
import com.openexchange.control.console.StopBundle;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.JMXInit;

/**
 * {@link AbstractBundleTest} - Abstract super class for a test class that stops/starts a specific bundle to check behavior on absence.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractBundleTest extends TestCase {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractBundleTest.class);

    protected static final String PROTOCOL = "http://";

    private WebConversation webConversation;

    protected StartBundle startBundle;

    protected StopBundle stopBundle;

    /**
     * Initializes a new {@link AbstractBundleTest}
     *
     * @param name The test case name
     */
    protected AbstractBundleTest(final String name) {
        super(name);
    }

    protected String getJMXHost() {
        return JMXInit.getJMXProperty(JMXInit.Property.JMX_HOST);
    }

    protected int getJMXPort() {
        return Integer.parseInt(JMXInit.getJMXProperty(JMXInit.Property.JMX_PORT));
    }

    protected String getJMXLogin() {
        return JMXInit.getJMXProperty(JMXInit.Property.JMX_LOGIN);
    }

    protected String getJMXPassword() {
        return JMXInit.getJMXProperty(JMXInit.Property.JMX_PASSWORD);
    }

    protected WebConversation getWebConversation() {
        if (webConversation == null) {
            webConversation = newWebConversation();
        }
        return webConversation;
    }

    /**
     * Setup the web conversation here so tests are able to create additional if several users are needed for tests.
     *
     * @return a new web conversation.
     */
    protected WebConversation newWebConversation() {
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        HttpUnitOptions.setScriptingEnabled(false);
        return new WebConversation();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        stopBundle = new StopBundle(getJMXHost(), getJMXPort(), getJMXLogin(), getJMXPassword());
        startBundle = new StartBundle(getJMXHost(), getJMXPort(), getJMXLogin(), getJMXPassword());
        stopBundle.stop(getBundleName());
        LOG.info("Bundle stopped: " + getBundleName());
    }

    @Override
    public void tearDown() throws Exception {
        startBundle.start(getBundleName());
        LOG.info("Bundle started: " + getBundleName());
        stopBundle = null;
        startBundle = null;
        super.tearDown();
    }

    protected abstract String getBundleName();

    private static final String LOGIN_URL = "/ajax/login";

    protected static JSONObject login(final WebConversation conversation, final String hostname, final String login, final String password) throws IOException, SAXException, JSONException {
        final WebRequest req = new PostMethodWebRequest(PROTOCOL + hostname + LOGIN_URL);
        req.setParameter("action", "login");
        req.setParameter("name", login);
        req.setParameter("password", password);
        final WebResponse resp = conversation.getResponse(req);
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

    private static final String FOLDER_URL = "/ajax/folders";

    protected static JSONObject getRootFolders(final WebConversation conversation, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException {
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname + FOLDER_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ROOT);
        final String columns = DataObject.OBJECT_ID + "," + FolderObject.MODULE + "," + FolderObject.FOLDER_NAME + "," + FolderObject.SUBFOLDERS;
        req.setParameter(AJAXServlet.PARAMETER_COLUMNS, columns);
        final WebResponse resp = conversation.getResponse(req);
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

    protected static int getStandardCalendarFolder(final WebConversation conversation, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
        final List<FolderObject> subfolders = getSubfolders(
            conversation,
            hostname,
            sessionId,
            "" + FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
            true);
        for (final Iterator<FolderObject> iter = subfolders.iterator(); iter.hasNext();) {
            final FolderObject subfolder = iter.next();
            if (subfolder.getModule() == FolderObject.CALENDAR && subfolder.isDefaultFolder()) {
                return subfolder.getObjectID();
            }
        }
        return -1;
    }

    protected static int getStandardInfostoreFolder(final WebConversation conversation, final String hostname, final String sessionId) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
        final List<FolderObject> subfolders = getSubfolders(
            conversation,
            hostname,
            sessionId,
            "" + FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
            true);
        for (final Iterator<FolderObject> iter = subfolders.iterator(); iter.hasNext();) {
            final FolderObject subfolder = iter.next();
            if (subfolder.getModule() == FolderObject.INFOSTORE && subfolder.isDefaultFolder()) {
                return subfolder.getObjectID();
            }
        }
        return -1;
    }

    protected static List<FolderObject> getSubfolders(final WebConversation conversation, final String hostname, final String sessionId, final String parentIdentifier, final boolean ignoreMailfolder) throws MalformedURLException, IOException, SAXException, JSONException, OXException {
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostname + FOLDER_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST);
        req.setParameter("parent", parentIdentifier);
        final String columns = DataObject.OBJECT_ID + "," + FolderObject.MODULE + "," + FolderObject.FOLDER_NAME + "," + FolderObject.SUBFOLDERS + "," + FolderObject.STANDARD_FOLDER + "," + DataObject.CREATED_BY;
        req.setParameter(AJAXServlet.PARAMETER_COLUMNS, columns);

        if (ignoreMailfolder) {
            req.setParameter(AJAXServlet.PARAMETER_IGNORE, "mailfolder");
        }

        final WebResponse resp = conversation.getResponse(req);
        final JSONObject respObj = new JSONObject(resp.getText());
        if (respObj.has("error") && !respObj.isNull("error")) {
            throw OXException.general("Error occured: " + respObj.getString("error"));
        }
        if (!respObj.has("data") || respObj.isNull("data")) {
            throw OXException.general("Error occured: Missing key \"data\"");
        }
        final JSONArray data = respObj.getJSONArray("data");
        final List<FolderObject> folders = new ArrayList<FolderObject>();
        for (int i = 0; i < data.length(); i++) {
            final JSONArray arr = data.getJSONArray(i);
            final FolderObject subfolder = new FolderObject();
            try {
                subfolder.setObjectID(arr.getInt(0));
            } catch (final JSONException exc) {
                subfolder.removeObjectID();
                subfolder.setFullName(arr.getString(0));
            }
            subfolder.setModule(FolderParser.getModuleFromString(
                arr.getString(1),
                subfolder.containsObjectID() ? subfolder.getObjectID() : -1));
            subfolder.setFolderName(arr.getString(2));
            subfolder.setSubfolderFlag(arr.getBoolean(3));
            subfolder.setDefaultFolder(arr.getBoolean(4));
            if (!arr.isNull(5)) {
                subfolder.setCreatedBy(arr.getInt(5));
            }
            folders.add(subfolder);
        }
        return folders;
    }
}
