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

    public AJAXSession(final WebConversation conversation, String hostname, final String id) {
        this(conversation, newHttpClient(conversation, hostname), id);
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
        DefaultHttpClient retval = new DefaultHttpClient(new ThreadSafeClientConnManager());

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
