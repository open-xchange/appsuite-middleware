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

package com.openexchange.ajax.simple;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.subscribe.helpers.TrustAdapter;

/**
 * {@link SimpleOXClient}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimpleOXClient {

    private static final String BASE = "/ajax";

    private boolean debug = "true".equals(System.getProperty("ebug"));

    private final HttpClient client = new HttpClient();
    private String sessionID;

    public SimpleOXClient(String host, boolean secure) {
        if (secure) {
            final Protocol https = new Protocol("https", new TrustAdapter(), 443);
            client.getHostConfiguration().setHost(host, 443, https);
        } else {
            client.getHostConfiguration().setHost(host);
        }
    }

    public SimpleOXClient(String host) {
        this(host, false);
    }

    public String login(String login, String password) throws JSONException, IOException {
        JSONObject obj = raw("login", "login", "name", login, "password", password);
        if (obj.has("error")) {
            throw new RuntimeException("Unexpected Repsonse: " + obj.toString());
        }
        return sessionID = obj.getString("session");
    }

    public boolean isLoggedIn() {
        return sessionID != null;
    }

    public SimpleOXModule getModule(String moduleName) {
        return new SimpleOXModule(this, moduleName);
    }

    public SimpleResponse call(String module, String action, Object... parameters) throws JSONException, IOException {
        return new SimpleResponse(raw(module, action, parameters));
    }

    public JSONObject raw(String module, String action, Object... parameters) throws JSONException, IOException {
        HttpMethod method = rawMethod(module, action, parameters);
        int statusCode = method.getStatusCode();
        if (statusCode != 200) {
            throw new IllegalStateException("Expected a return code of 200 but was " + statusCode);
        }
        String response = method.getResponseBodyAsString();
        if (debug) {
            System.out.println("Response: " + response);
        }
        return new JSONObject(response);
    }

    public HttpMethod rawMethod(String module, String action, Object... parameters) throws JSONException, IOException {
        Map<String, Object> params = M(parameters);
        params.put("action", action);
        if (!params.containsKey("session") && isLoggedIn()) {
            params.put("session", sessionID);
        }
        HttpMethod method;

        String url = BASE + "/" + module;

        if (params.containsKey("body")) {
            EntityEnclosingMethod entityMethod;
            String body = JSONCoercion.coerceToJSON(params.remove("body")).toString();
            if ("getWithBody".equals(action)) {
                entityMethod = new GetWithBody(url);
            } else {
                entityMethod = new PutMethod(url);
            }
            try {
                entityMethod.setRequestEntity(new StringRequestEntity(body, "text/javascript", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // Doesn't happen
                e.printStackTrace();
            }
            method = entityMethod;

            NameValuePair[] pairs = new NameValuePair[params.size()];
            int i = 0;
            for (Entry<String, Object> entry : params.entrySet()) {
                pairs[i++] = new NameValuePair(entry.getKey(), entry.getValue().toString());
            }

            method.setQueryString(pairs);
        } else {
            PostMethod post = new PostMethod(url);
            for (final Entry<String, Object> entry : params.entrySet()) {
                post.addParameter(new NameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            post.setQueryString(url);
            method = post;
        }

        client.executeMethod(method);

        return method;
    }

    private Map<String, Object> M(Object... parameters) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        for (int i = 0; i < parameters.length; i++) {
            map.put(parameters[i++].toString(), parameters[i]);
        }

        return map;
    }

    public HttpClient getClient() {
        return client;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
