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
 * mail:                    info@open-xchange.com
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

import java.io.IOException;
import java.net.URL;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;

/**
 * This test case tests the AJAX interface of the config jump URL generator.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ConfigJumpTest extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigJumpTest.class);

    /**
     * URL of the AJAX config interface.
     */
    private static final String CONTROL_URL = "/ajax/control";

    /**
     * Default constructor.
     * @param name Name of this test.
     */
    public ConfigJumpTest(final String name) {
        super(name);
    }

    /**
     * Tests if the url can be read from the server.
     * @throws Throwable if an error occurs.
     */
    public void testReadSettings() throws Throwable {
        final URL control = readURL(getWebConversation(), getHostName(),
            getSessionId());
        assertTrue("Got no value from server.", control.toString().length()
            > 0);
    }

    /**
     * Reads a configuration setting. A tree of configuration settings can also
     * be read. This tree will be returned as a string in JSON.
     * @param conversation web conversation.
     * @param hostName host name of the server.
     * @param sessionId session identifier of the user.
     * @return the value of the setting or a string with the tree of settings in
     * JSON.
     * @throws SAXException if parsing of the response fails.
     * @throws IOException if getting the response fails.
     * @throws JSONException if parsing the response fails.
     */
    public static URL readURL(final WebConversation conversation,
        final String hostName, final String sessionId)
        throws IOException, SAXException, JSONException {
        LOG.trace("Reading control center URL.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName
            + CONTROL_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: \"" + body + "\"");
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return new URL((String) response.getData());
    }
}
