/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.framework;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;

/**
 * This class stores the HTTP client instance and the session identifier for an AJAX session. Additionally the fallback web conversation is
 * stored here.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AJAXSession {

    /**
     * User Agent displayed to server - needs to be consistent during a test run for security purposes
     */
    public static final String USER_AGENT = "HTTP API Testing Agent";

    private final WebConversation conversation;

    private final DefaultHttpClient httpClient;

    private String id;

    public AJAXSession() {
        this(newWebConversation(), newHttpClient(), null);
    }

    public AJAXSession(WebConversation conversation, DefaultHttpClient httpClient, String id) {
        super();
        this.conversation = conversation;
        this.httpClient = httpClient;
        this.id = id;
    }

    public WebConversation getConversation() {
        return conversation;
    }

    public DefaultHttpClient getHttpClient() {
        return httpClient;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Setup the web conversation here so tests are able to create additional if several users are needed for tests.
     *
     * @return a new web conversation.
     */
    public static WebConversation newWebConversation() {
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        HttpUnitOptions.setScriptingEnabled(false);
        WebConversation retval = new WebConversation();
        retval.getClientProperties().setAcceptGzip(false);
        retval.getClientProperties().setUserAgent(USER_AGENT);
        return retval;
    }

    public static DefaultHttpClient newHttpClient() {
        ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager();
        connManager.setDefaultMaxPerRoute(5000);
        connManager.setMaxTotal(10000);
        DefaultHttpClient retval = new DefaultHttpClient(connManager);

        HttpParams params = retval.getParams();
        int minute = 1 * 60 * 1000 * 1000;
        HttpConnectionParams.setConnectionTimeout(params, minute);
        HttpConnectionParams.setSoTimeout(params, minute);

        retval.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY); // OX cookies work with all
                                                                                                        // browsers, meaning they are a mix
                                                                                                        // of the Netscape draft and the
                                                                                                        // RFC
        retval.getParams().setParameter("User-Agent", USER_AGENT); // needs to be consistent
        retval.getParams().setParameter("http.useragent", USER_AGENT); // needs to be consistent
        return retval;
    }

    public static DefaultHttpClient newHttpClient(WebConversation conversation, String hostname) {
        DefaultHttpClient retval = newHttpClient();
        Executor.syncCookies(conversation, retval, hostname);
        return retval;
    }
}
