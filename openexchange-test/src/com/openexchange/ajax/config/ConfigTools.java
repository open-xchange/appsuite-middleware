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

package com.openexchange.ajax.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.TimeZone;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.SetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.tools.URLParameter;

/**
 * Utility class that contains all methods for making config requests to the
 * server.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ConfigTools extends Assert {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigTools.class);

    /**
     * URL of the AJAX config interface.
     */
    private static final String CONFIG_URL = "/ajax/config/";

    /**
     * Prevent instantiation
     */
    private ConfigTools() {
        super();
    }

    /**
     * Reads a configuration setting. A tree of configuration settings can also
     * be read. This tree will be returned as a string in JSON.
     * @param conversation web conversation.
     * @param hostName host name of the server.
     * @param sessionId session identifier of the user.
     * @param path path to the setting.
     * @return the value of the setting or a string with the tree of settings in
     * JSON.
     * @throws SAXException if parsing of the response fails.
     * @throws IOException if getting the response fails.
     * @throws JSONException if parsing the response fails.
     */
    public static String readSetting(final WebConversation conversation,
        final String hostName, final String sessionId, final String path)
        throws IOException, SAXException, JSONException {
        LOG.trace("Reading setting.");
        final WebRequest req = new GetMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + CONFIG_URL + '/' + path);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: \"" + body + "\"");
        final Response response = Response.parse(body);
        assertFalse(response.getErrorMessage(), response.hasError());
        return response.getData().toString();
    }

    /**
     * Stores a configuration setting.
     * @param conversation web conversation.
     * @param hostName host name of the server.
     * @param sessionId session identifier of the user.
     * @param path path to the setting.
     * @param value the value to write.
     * @throws Throwable if an error occurs.
     */
    public static void storeSetting(final WebConversation conversation,
        final String hostName, final String sessionId, final String path,
        final String value) throws Throwable {
        LOG.trace("Storing setting.");
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        final WebRequest req = new PutMethodWebRequest(AbstractAJAXTest.PROTOCOL
            + hostName + CONFIG_URL + '/' + path + parameter.getURLParameters(),
            new ByteArrayInputStream(value.getBytes(com.openexchange.java.Charsets.UTF_8)),
            "application/octet-stream");
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK,
            resp.getResponseCode());
        LOG.trace("Setting stored.");
    }

    /**
     * @deprecated use {@link #getUserId(AJAXClient)}.
     */
    @Deprecated
    public static int getUserId(final WebConversation conversation,
        final String hostName, final String sessionId) throws IOException,
        SAXException, JSONException, OXException, OXException {
        AJAXConfig.init();
        final AJAXSession session = new AJAXSession(conversation, hostName, sessionId);
        return Executor.execute(session, new GetRequest(Tree.Identifier), hostName).getInteger();
    }

    public static int getUserId(final AJAXClient client) throws OXException,
        IOException, SAXException, JSONException {
        return client.getValues().getUserId();
    }

    /**
     * Reads the time zone of the user.
     * @param conversation web conversation.
     * @param hostName host name of the server.
     * @param sessionId session identifier of the user.
     * @return the time zone configured by the user.
     * @throws SAXException if parsing of the response fails.
     * @throws IOException if getting the response fails.
     * @throws JSONException if parsing the response fails.
     * @throws OXException
     */
    public static TimeZone getTimeZone(final WebConversation conversation,
        final String hostName, final String sessionId) throws IOException,
        SAXException, JSONException, OXException, OXException {
        AJAXConfig.init();
        final AJAXSession session = new AJAXSession(conversation, hostName, sessionId);
        final String value = ConfigTools.get(session, new GetRequest(Tree
            .TimeZone)).getString();
        return TimeZone.getTimeZone(value);
    }

    /**
     * Reads the mail filter value.
     * @param conversation web conversation.
     * @param hostName host name of the server.
     * @param sessionId session identifier of the user.
     * @return the time zone configured by the user.
     * @throws SAXException if parsing of the response fails.
     * @throws IOException if getting the response fails.
     * @throws JSONException if parsing the response fails.
     * @throws OXException
     */
    public static boolean getMailFilterValue(final WebConversation conversation,
        final String hostName, final String sessionId) throws IOException,
        SAXException, JSONException, OXException, OXException {
        AJAXConfig.init();
        final AJAXSession session = new AJAXSession(conversation, hostName, sessionId);
        final Boolean value = ConfigTools.get(session, new GetRequest(Tree
            .MailFilter)).getBoolean();
        return value;
    }

    /**
     * @deprecated use {@link UserValues} from {@link AJAXClient} or make directly the request with {@link GetRequest}.
     */
    @Deprecated
    public static GetResponse get(AJAXClient client, GetRequest request) throws OXException, IOException, SAXException, JSONException {
        return client.execute(request);
    }

    public static GetResponse get(final AJAXSession session, final GetRequest request) throws OXException, IOException, SAXException, JSONException {
        return Executor.execute(session, request);
    }

    /**
     * @deprecated use {@link AJAXClient#execute(com.openexchange.ajax.framework.AJAXRequest)}.
     */
    @Deprecated
    public static SetResponse set(final AJAXClient client, final SetRequest request) throws OXException, IOException,
        SAXException, JSONException {
        return client.execute(request);
    }

    public static GetResponse get(final AJAXSession session,
        final GetRequest request, String protocol, String hostname) throws OXException, IOException,
        SAXException, JSONException {
        if(protocol != null && hostname != null) {
            return Executor.execute(session, request, protocol, hostname);
        }
        return Executor.execute(session, request);
    }

}
