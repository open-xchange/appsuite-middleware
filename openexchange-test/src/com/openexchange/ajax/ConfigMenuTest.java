/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.tools.RandomString;
import com.openexchange.tools.URLParameter;

/**
 * This test case tests the AJAX interface of the config system for the AJAX
 * GUI.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigMenuTest extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ConfigMenuTest.class);

    private static final String CONFIG_URL = "/ajax/config/";

    private static final int NUMBER_OF_SETTINGS = 10;

    private static final int PATH_DEPTH = 10;

    private static final int MAX_PATH_NAME_LENGTH = 10;

    private static final int MAX_VALUE_LENGTH = 10;

    /**
     * Default constructor.
     * @param name Name of this test.
     */
    public ConfigMenuTest(final String name) {
        super(name);
    }

    /**
     * This method tests storing of a number of random values under path with
     * random length and depth.
     * @throws Throwable if an error occurs.
     */
    public void notestStoreExampleSetting() throws Throwable {
        final Random rand = new Random(System.currentTimeMillis());
        String[] path = new String[NUMBER_OF_SETTINGS];
        String[] value = new String[path.length];
        for (int i = 0; i < path.length; i++) {
            path[i] = "";
            final StringBuilder pathBuilder = new StringBuilder();
            final int pathLength = rand.nextInt(PATH_DEPTH) + 1;
            for (int j = 0; j < pathLength; j++) {
                pathBuilder.append(RandomString.generateLetter(
                    rand.nextInt(MAX_PATH_NAME_LENGTH) + 1) + "/");
                path[i] = pathBuilder.toString();
            }
            path[i] = path[i].substring(0, path[i].length() - 1);
            value[i] = RandomString.generateLetter(
                rand.nextInt(MAX_VALUE_LENGTH) + 1);
        }
        for (int i = 0; i < path.length; i++) {
            storeSetting(getWebConversation(), getHostName(), getSessionId(),
                path[i], value[i]);
        }
        for (int i = 0; i < path.length; i++) {
            assertEquals("Value of the setting differs.", value[i], readSetting(
                getWebConversation(), getHostName(), getSessionId(), path[i]));
        }
    }

    /**
     * Tests if the settings can be read from the server.
     * @throws Throwable if an error occurs.
     */
    public void testReadSettings() throws Throwable {
        final String value = readSetting(getWebConversation(), getHostName(),
            getSessionId(), "");
        assertTrue("Got no value from server.", value.length() > 0);
    }

    public void testTimeZone() throws Throwable {
        final String timeZone = readSetting(getWebConversation(), getHostName(),
            getSessionId(), "timezone");
        final String testTimeZone = "Australia/Hobart";
        storeSetting(getWebConversation(), getHostName(), getSessionId(),
            "timezone", testTimeZone);
        assertEquals(testTimeZone, readSetting(getWebConversation(),
            getHostName(), getSessionId(), "timezone"));
        storeSetting(getWebConversation(), getHostName(), getSessionId(),
            "timezone", timeZone);
    }

    public static String readSetting(final WebConversation conversation,
        final String hostName, final String sessionId, final String path)
        throws Throwable {
        LOG.trace("Reading setting.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName
            + CONFIG_URL + path);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        req.setHeaderField("Content-Type", "");
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: \"" + body + "\"");
        final Response response = Response.parse(body);
        return response.getData().toString();
    }

    public static void storeSetting(final WebConversation conversation,
        final String hostName, final String sessionId, final String path,
        final String value) throws Throwable {
        LOG.trace("Storing setting.");
        URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + hostName
            + CONFIG_URL + path + parameter.getURLParameters(),
            new ByteArrayInputStream(value.getBytes("UTF-8")),
            "application/octet-stream");
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        LOG.trace("Setting stored.");
    }
}
