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

package com.openexchange.realtime.client.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.realtime.client.RTConnection;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.test.performance.realtime.config.ConfigurationProperties;
import com.openexchange.test.performance.realtime.config.ConfigurationProvider;
import com.openexchange.test.performance.realtime.transfer.MessageHandler;
import com.openexchange.test.performance.realtime.transfer.RealtimeTransferManager;
import com.openexchange.test.performance.realtime.user.RealtimeUser;

/**
 * {@link DefaultRTConnection}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DefaultRTConnection implements RTConnection {

    private final RTConnectionProperties properties;

    private RealtimeTransferManager rtm;

    private RealtimeUser user;

    private DefaultHttpClient client;

    public DefaultRTConnection(final RTConnectionProperties properties) {
        super();
        this.properties = properties;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.realtime.client.RTConnection#connect(com.openexchange.realtime.client.RTMessageHandler)
     */
    @Override
    public String connect(final RTMessageHandler messageHandler) throws RTException {
        user = createOXSession();
        try {
            ConfigurationProperties props = new ConfigurationProperties();
            props.put(ConfigurationProperties.KEY_CONSOLE_LOG, "true");
            props.put(ConfigurationProperties.KEY_SERVER_PROTOCOL, properties.getProtocol());
            props.put(ConfigurationProperties.KEY_SERVER_HOST, properties.getHost());
            props.put(ConfigurationProperties.KEY_SERVER_PORT, String.valueOf(properties.getPort()));
            props.put(ConfigurationProperties.KEY_SERVER_PATH, "/realtime/atmosphere/rt");
            props.put(ConfigurationProperties.KEY_STANZA_TRACING, "true");
            ConfigurationProvider.setProperties(props);

            if (messageHandler != null) {
                rtm = new RealtimeTransferManager(user, new MessageHandler() {
                    @Override
                    public void onReceive(String message) {
                        try {
                            JSONValue jsonValue = JSONObject.parse(new StringReader(message));
                            messageHandler.onMessage(jsonValue);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                rtm = new RealtimeTransferManager(user);
            }

            return rtm.getResource();
        } catch (IOException e) {
            throw new RTException("Connection could not be established.", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.realtime.client.RTConnection#post(com.openexchange.realtime.packet.Stanza)
     */
    @Override
    public void post(JSONValue message) throws RTException {
        checkState();
        try {
            rtm.fire(message);
        } catch (IOException e) {
            throw new RTException("Error while sending stanza.", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.realtime.client.RTConnection#postReliable(com.openexchange.realtime.packet.Stanza)
     */
    @Override
    public void postReliable(JSONValue message) throws RTException {
        checkState();
        try {
            if (message.isObject()) {
                rtm.trackAndSend((JSONObject) message);
            } else {
                JSONArray array = (JSONArray) message;
                int length = array.length();
                for (int i = 0; i < length; i++) {
                    rtm.trackAndSend(array.getJSONObject(i));
                }
            }
        } catch (IOException e) {
            throw new RTException("Error while sending stanza.", e);
        } catch (JSONException e) {
            throw new RTException("Error while transforming stanza.", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.realtime.client.RTConnection#close()
     */
    @Override
    public void close() throws RTException {
        checkState();
        rtm.close();
        closeOXSession(user);
    }

    private void checkState() throws RTException {
        if (rtm == null || user == null || client == null) {
            throw new RTException("Invalid connection state.");
        }
    }

    private RealtimeUser createOXSession() throws RTException {
        client = new DefaultHttpClient();
        String target = buildDestination() + "/appsuite/api/login?action=login&name=";
        target += properties.getUser();
        target += "&password=";
        target += properties.getPassword();

        HttpPost request = new HttpPost(target);
        try {
            HttpResponse response = client.execute(request);
            CookieStore cookieStore = client.getCookieStore();
            String[] cookies = parseCookies(cookieStore);

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new RTException("Error during login. Response did not contain an entity.");
            }

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RTException(
                    "Error during login. " + "Server responded with status code " + response.getStatusLine().getStatusCode() + ": " + response.getStatusLine().getReasonPhrase());
            }

            String[] sessionInfo = parseLoginResponse(entity);
            RealtimeUser user = new RealtimeUser(properties.getUser(), sessionInfo[0], cookies[1],  cookies[2], cookies[0]);
            return user;
        } catch (ClientProtocolException e) {
            throw new RTException("Error during login.", e);
        } catch (IOException e) {
            throw new RTException("Error during login.", e);
        } catch (JSONException e) {
            throw new RTException("Could not parse login response.", e);
        }
    }

    private String[] parseLoginResponse(HttpEntity entity) throws JSONException, RTException, IOException {
        Reader reader = new InputStreamReader(entity.getContent());
        JSONObject json = (JSONObject) JSONObject.parse(reader);
        if (!json.has("session")) {
            throw new RTException("Error during login. Response did not contain session id.");
        }

        if (!json.has("random")) {
            throw new RTException("Error during login. Response did not contain random token.");
        }

        String session = json.getString("session");
        String random = json.getString("random");
        reader.close();

        return new String[] { session, random };
    }

    private String[] parseCookies(CookieStore cookieStore) throws RTException {
        List<Cookie> cookies = cookieStore.getCookies();
        String secretKey = null;
        String secretValue = null;
        String jsessionId = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith("open-xchange-secret-")) {
                secretKey = cookie.getName();
                secretValue = cookie.getValue();
            } else if (cookie.getName().equals("JSESSIONID")) {
                jsessionId = cookie.getValue();
            }
        }

        if (secretKey == null) {
            throw new RTException("Error during login. Secret cookie is missing in response.");
        }

        if (jsessionId == null) {
            throw new RTException("Error during login. JSESSIONID cookie is missing in response.");
        }

        return new String[] { jsessionId, secretKey, secretValue };
    }

    private void closeOXSession(RealtimeUser user) throws RTException {
        String target = buildDestination() + "/appsuite/api/login?action=logout&session=";
        target += user.getSession();
        try {
            HttpGet request = new HttpGet(target);
            HttpResponse response = client.execute(request);
            EntityUtils.consumeQuietly(response.getEntity());
        } catch (ClientProtocolException e) {
            throw new RTException("Error during logout.", e);
        } catch (IOException e) {
            throw new RTException("Error during logout.", e);
        } finally {
            client.getConnectionManager().shutdown();
            client = null;
        }
    }

    private String buildDestination() {
        return properties.getProtocol() + "://" + properties.getHost() + ':' + properties.getPort();
    }


}
