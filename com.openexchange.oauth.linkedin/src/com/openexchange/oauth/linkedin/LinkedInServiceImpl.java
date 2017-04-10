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

package com.openexchange.oauth.linkedin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.version.Version;

/**
 * {@link LinkedInServiceImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class LinkedInServiceImpl implements LinkedInService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LinkedInServiceImpl.class);

    public static enum Verb {
      GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE
    }

    // --------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    private final CloseableHttpClient httpClient;

    /**
     * Initializes a new {@link LinkedInServiceImpl}.
     *
     * @param services The service look-up
     */
    public LinkedInServiceImpl(final ServiceLookup services) {
        super();
        this.services = services;

        ClientConfig clientConfig = ClientConfig.newInstance()
            .setUserAgent("OX LinkedIn Http Client v" + Version.getInstance().getVersionString())
            .setMaxTotalConnections(100)
            .setMaxConnectionsPerRoute(100)
            .setConnectionTimeout(5000)
            .setSocketReadTimeout(30000);
        httpClient = HttpClients.getHttpClient(clientConfig);
    }

    /**
     * Shuts-down this LinkedIn service.
     */
    public void shutDown() {
        Streams.close(httpClient);
    }

    private String urlEncode(String toEncode) {
        try {
            return URLEncoder.encode(toEncode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should not occur
            return URLEncoder.encode(toEncode);
        }
    }

    private void setCommonHeaders(HttpRequestBase request, String token) {
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private JSONValue performRequest(Session session, int user, int contextId, int accountId, String url) throws OXException {
        // Get specified OAuth account for denoted user
        OAuthAccount account;
        try {
            final com.openexchange.oauth.OAuthService oAuthService = services.getService(com.openexchange.oauth.OAuthService.class);
            if (null == oAuthService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(com.openexchange.oauth.OAuthService.class.getName());
            }
            account = oAuthService.getAccount(accountId, session, user, contextId);
        } catch (final OXException e) {
            LOG.error("", e);
            return null;
        }

        HttpGet get = null;
        CloseableHttpResponse response = null;
        try {
            // Initialize request
            get = new HttpGet(url);
            setCommonHeaders(get, account.getToken());

            // Execute
            response = httpClient.execute(get);

            // Check status code
            {
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode < 200 || statusCode >= 300) {
                    String body = null;
                    {
                        try {
                            body = Streams.reader2string(new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8));
                        } catch (Exception x) {
                            // ignore
                        }
                    }

                    String reason = null == body ? statusLine.getReasonPhrase() : body;
                    throw new HttpResponseException(statusCode, reason);
                }
            }

            // Parse response body to JSON
            try {
                return JSONObject.parse(new InputStreamReader(response.getEntity().getContent(), Charsets.UTF_8));
            } catch (JSONException e) {
                throw OAuthExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        } catch (HttpResponseException e) {
            if (400 == e.getStatusCode() || 401 == e.getStatusCode()) {
                // Authentication failed
                throw OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(e, account.getDisplayName(), account.getId(), user, contextId);
            }
            throw OAuthExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw OAuthExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
        } finally {
            close(get, response);
            Streams.close(response);
        }
    }

    protected List<String> extractIds(final JSONObject jRresponse) {
        JSONArray ids = jRresponse.optJSONArray("values");

        int length = ids.length();
        List<String> result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            JSONObject jConnection = ids.optJSONObject(i);
            if (null != jConnection) {
                String id = jConnection.optString("id", null);
                if (null != id) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    protected List<String> extractIds(final JSONArray connections) {
        List<String> result = new LinkedList<String>();
        try {
            for (int i = 0, max = connections.length(); i < max; i++) {
                result.add(connections.getJSONObject(i).getString("id"));
            }
        } catch (final JSONException e) {
            LOG.error("", e);
        }
        return result;
    }

    @Override
    public String getAccountDisplayName(final Session session, final int user, final int contextId, final int accountId) {
        String displayName = "";
        try {
            final com.openexchange.oauth.OAuthService oAuthService = services.getService(com.openexchange.oauth.OAuthService.class);
            if (null == oAuthService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(com.openexchange.oauth.OAuthService.class.getName());
            }
            final OAuthAccount account = oAuthService.getAccount(accountId, session, user, contextId);
            displayName = account.getDisplayName();
        } catch (final OXException e) {
            LOG.error("", e);
        }
        return displayName;
    }

    @Override
    public List<Contact> getContacts(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        JSONValue jResponse = performRequest(session, user, contextId, accountId, "https://api.linkedin.com/v1/people/~/connections:(id,first-name,last-name,email-address,phone-numbers,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions)?format=json");
        if (jResponse == null) {
            return Collections.emptyList();
        }

        LinkedInXMLParser parser = new LinkedInXMLParser();
        final List<Contact> contacts = parser.parseConnections(jResponse);
        return contacts;
    }

    @Override
    public JSONObject getProfileForId(final String id, final Session session, final int user, final int contextId, final int accountId) throws OXException {
        String uri = "https://api.linkedin.com/v1/people/id=" + id + ":(" + "id,first-name,last-name,email-address,phone-numbers,headline,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions,industry,public-profile-url" + ")";

        JSONValue jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return new JSONObject(0);
        }

        return jResponse.toObject();
    }

    @Override
    public JSONObject getRelationToViewer(final String id, final Session session, final int user, final int contextId, final int accountId) throws OXException {
        String uri = "https://api.linkedin.com/v1/people/id=" + id + ":(relation-to-viewer)";

        JSONValue jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return new JSONObject(0);
        }

        return jResponse.toObject();
    }

    @Override
    public JSONObject getConnections(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        String uri = "https://api.linkedin.com/v1/people/~/connections" + ":(" + "id,first-name,last-name,email-address,phone-numbers,headline,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions,industry,public-profile-url" + ")";

        JSONValue jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return new JSONObject(0);
        }

        return jResponse.toObject();
    }

    @Override
    public List<String> getUsersConnectionsIds(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        String uri = "https://api.linkedin.com/v1/people/~/connections:(id)";

        JSONValue jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return Collections.emptyList();
        }

        return extractIds(jResponse.toObject());
    }

    public JSONObject getFullProfileById(final String id, final Session session, final int user, final int contextId, final int accountId) throws OXException {
        String uri = "https://api.linkedin.com/v1/people/id=" + id + ":(" + "relation-to-viewer:(connections:(person:(id,first-name,last-name,picture-url,headline)))" + "," + "id,first-name,last-name,email-address,phone-numbers,headline,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions,industry,public-profile-url" + ")";

        JSONValue jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return new JSONObject(0);
        }

        return jResponse.toObject();
    }

    @Override
    public JSONObject getFullProfileByEMail(final List<String> email, final Session session, final int user, final int contextId, final int accountId) throws OXException {
        String uri = null;
        if (email.size() == 1) {
            uri = "https://api.linkedin.com/v1/people/email=" + email.get(0) + ":(" + "relation-to-viewer:(connections:(person:(id,first-name,last-name,picture-url,headline)))" + "," + "id,first-name,last-name,email-address,phone-numbers,headline,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions,industry,public-profile-url" + ")";
        } else {
            final StringBuilder b = new StringBuilder("http://api.linkedin.com/v1/people::(");
            for (final String s : email) {
                b.append("email=").append(s).append(',');
            }
            b.setLength(b.length() - 1);
            b.append("):(").append("relation-to-viewer:(connections:(person:(id,first-name,last-name,picture-url,headline)))").append(',').append("id,first-name,last-name,email-address,phone-numbers,headline,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions,industry,public-profile-url").append(')');
            uri = b.toString();
        }

        JSONValue jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return new JSONObject(0);
        }

        return jResponse.toObject();
    }

    @Override
    public JSONObject getFullProfileByFirstAndLastName(String firstName, String lastName, ServerSession session, final int user, final int contextId, final int accountId) throws OXException {
        String uri = "https://api.linkedin.com/v1/people-search?first-name=" + urlEncode(firstName) + "&last-name=" + urlEncode(lastName) + "&sort=distance&format=json";

        JSONValue jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return new JSONObject(0);
        }

        JSONObject data = jResponse.toObject();
        if (data.optInt("numResults") > 0) {
            JSONObject people = data.optJSONObject("people");
            if (people != null) {
                JSONArray values = people.optJSONArray("values");
                if (null != values && values.length() > 0) {
                    JSONObject firstMatch = values.optJSONObject(0);
                    if (firstMatch != null) {
                        String id = firstMatch.optString("id");
                        if (id != null) {
                            return getFullProfileById(id, session, user, contextId, accountId);
                        }
                    }
                }
            }

            // Not found
            return null;
        }

        uri = "https://api.linkedin.com/v1/people-search?first-name=" + urlEncode(lastName) + "&last-name=" + urlEncode(firstName) + "&sort=distance&format=json";

        jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return new JSONObject(0);
        }

        data = jResponse.toObject();
        if (data.optInt("numResults") > 0) {
            JSONObject people = data.optJSONObject("people");
            if (people != null) {
                JSONArray values = people.optJSONArray("values");
                if (null != values && values.length() > 0) {
                    JSONObject firstMatch = values.optJSONObject(0);
                    if (firstMatch != null) {
                        String id = firstMatch.optString("id");
                        if (id != null) {
                            return getFullProfileById(id, session, user, contextId, accountId);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public JSONObject getNetworkUpdates(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        String uri = "https://api.linkedin.com/v1/people/~/network/updates" + "?format=json" + "&type=CONN";

        JSONValue jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return new JSONObject(0);
        }

        return jResponse.toObject();
    }

    @Override
    public JSONObject getMessageInbox(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        String uri = "https://api.linkedin.com/v1/people/~/mailbox:(id,folder,from:(person:(id,first-name,last-name,picture-url,headline)),recipients:(person:(id,first-name,last-name,picture-url,headline)),subject,short-body,last-modified,timestamp,mailbox-item-actions,body)?message-type=message-connections,invitation-request,invitation-reply,inmail-direct-connection&format=json";

        JSONValue jResponse = performRequest(session, user, contextId, accountId, uri);
        if (jResponse == null) {
            return new JSONObject(0);
        }

        return jResponse.toObject();
    }

    // -------------------------------------------------------------------------------------------------

    /**
     * Resets given HTTP request
     *
     * @param request The HTTP request
     */
    private static void reset(HttpRequestBase request) {
        if (null != request) {
            try {
                request.reset();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Ensures that the entity content is fully consumed and the content stream, if exists, is closed silently.
     *
     * @param response The HTTP response to consume and close
     */
    private static void consume(HttpResponse response) {
        if (null != response) {
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                try {
                    EntityUtils.consume(entity);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Closes the supplied HTTP request & response resources silently.
     *
     * @param request The HTTP request to reset
     * @param response The HTTP response to consume and close
     */
    private static void close(HttpRequestBase request, HttpResponse response) {
        consume(response);
        reset(request);
    }

}
