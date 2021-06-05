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
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;

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
            final Protocol https = new Protocol("https", new DefaultProtocolSocketFactory(), 443);
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
                entityMethod.setRequestEntity(new StringRequestEntity(body, "application/json", "UTF-8"));
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
