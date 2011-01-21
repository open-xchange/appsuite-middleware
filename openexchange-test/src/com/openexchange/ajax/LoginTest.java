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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieJar;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.OxHttpClient;
import com.openexchange.ajax.framework.Params;

/**
 * This class contains the login test. It also contains static methods to made
 * logins from other places.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginTest extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(LoginTest.class);

    /**
     * URL of the login AJAX servlet.
     */
    private static final String LOGIN_URL = "/ajax/login";

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public LoginTest(final String name) {
        super(name);
    }

    /**
     * This method mades a login and returns the complete login object.
     * @param conversation WebConversation.
     * @param hostname hostname of the server running the server.
     * @param login Login of the user.
     * @param password Password of the user.
     * @return the session identifier if the login is successful.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     */
    private static JSONObject login(final WebConversation conversation,
        final String hostname, final String login, final String password)
        throws IOException, SAXException, JSONException {
        
        checkNotLoggedIn(conversation.getCookieNames());
        
        LOG.trace("Logging in.");
        	//do request
        	DefaultHttpClient newClient = new OxHttpClient();
        	newClient.setCookieStore(new BasicCookieStore());
         	
         	Params params = new Params(
         			AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGIN,
         			AJAXServlet.PARAMETER_USERNAME, login,
         			AJAXServlet.PARAMETER_PASSWORD, password
         			);
         	HttpGet loginRequest = new HttpGet(PROTOCOL + hostname + LOGIN_URL + params.toString());
        	HttpResponse response = newClient.execute(loginRequest);
            assertEquals("Login: Response code is not okay.", HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

            String responseBody = EntityUtils.toString(response.getEntity());
            
            List<Cookie> cookies = newClient.getCookieStore().getCookies();
            for(Cookie cookie: cookies){
            	conversation.putCookie(cookie.getName(), cookie.getValue());
            }
            
            
            JSONObject json;
			try {
            	json =  new JSONObject(responseBody);
	        } catch (final JSONException e) {
	            LOG.error("Can't parse this body to JSON: \"" + responseBody + '\"');
	            throw e;
	        }
	        assertFalse(json.optString("error"), json.has("error"));
	        assertTrue("Session ID is missing: " + responseBody, json.has(
	            Login.PARAMETER_SESSION));
	        assertTrue("Random is missing: " + responseBody, json.has(LoginFields.PARAM_RANDOM));
	        return json;
    }

    private static void checkNotLoggedIn(String[] cookieNames) {
        for (String string : cookieNames) {
            if(string.startsWith("open-xchange")) {
                throw new IllegalStateException("This webconversation was used to log in. You must use a different web conversation, when you want to open another session.");
            }
        }
    }

    /**
     * This method mades a logout.
     * @param conversation WebConversation.
     * @param hostname hostname of the server running the server.
     * @param sessionId Session identifier of the user.
     * @throws IOException if the communication with the server fails.
     * @throws SAXException if a SAX error occurs. 
     */
    public static void logout(final WebConversation conversation,
        final String hostname, final String sessionId)
        throws IOException, SAXException {
    	
        LOG.trace("Logging out.");
    	//do request
    	DefaultHttpClient newClient = new OxHttpClient();
    	
    	CookieStore cookieStore = new BasicCookieStore();
    	CookieJar cookieJar = conversation.getCookieJar();
    	com.openexchange.ajax.framework.Executor.syncCookies(cookieStore, cookieJar, hostname);

		newClient.setCookieStore(cookieStore );

     	Params params = new Params(
     			AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LOGOUT,
     			AJAXServlet.PARAMETER_SESSION, sessionId
     			);
     	HttpGet logoutRequest = new HttpGet(PROTOCOL + hostname + LOGIN_URL + params.toString());
    	HttpResponse response = newClient.execute(logoutRequest);
    	
        assertEquals("Logout: Response code is not okay.", HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    /**
     * This method mades a login and returns the sessionId if the login is
     * successful.
     * @param conversation WebConversation.
     * @param hostname hostname of the server running the server.
     * @param login Login of the user.
     * @param password Password of the user.
     * @return the session identifier if the login is successful.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     */
    public static String getSessionId(final WebConversation conversation,
        final String hostname, final String login, final String password)
        throws IOException, SAXException, JSONException {
        final JSONObject jslogin = login(conversation, hostname, login,
            password);
        final String sessionId = jslogin.getString("session");
        assertNotNull("Can't get sessionId", sessionId);
        assertTrue("Can't get sessionId", sessionId.length() > 0);
        return sessionId;
    }
}
