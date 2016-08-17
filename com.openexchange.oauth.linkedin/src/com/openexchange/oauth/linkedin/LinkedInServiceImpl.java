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
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.UnsynchronizedPushbackReader;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link LinkedInServiceImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class LinkedInServiceImpl implements LinkedInService {

    private static final String PERSONAL_FIELDS = "id,first-name,last-name,email-address,phone-numbers,headline,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions,industry,public-profile-url";

    private static final String RELATION_TO_VIEWER = "relation-to-viewer:(connections:(person:(id,first-name,last-name,picture-url,headline)))";

    private static final String PERSONAL_FIELD_QUERY = ":(" + PERSONAL_FIELDS + ")";

    private static final String CONNECTIONS_URL = "http://api.linkedin.com/v1/people/~/connections:(id,first-name,last-name,email-address,phone-numbers,im-accounts,twitter-accounts,date-of-birth,main-address,picture-url,positions)";

    private static final String IN_JSON = "?format=json";

    private final ServiceLookup services;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LinkedInServiceImpl.class);

    /**
     * Initializes a new {@link LinkedInServiceImpl}.
     *
     * @param services The service look-up
     */
    public LinkedInServiceImpl(final ServiceLookup services) {
        super();
        this.services = services;
    }

    public Response performRequest(final Session session, final int user, final int contextId, final int accountId, final Verb method, final String url) throws OXException {
        try {
            final OAuthServiceMetaData linkedInMetaData = new OAuthServiceMetaDataLinkedInImpl(services);

            final OAuthService service = new ServiceBuilder().provider(LinkedInApi.class).apiKey(linkedInMetaData.getAPIKey(session)).apiSecret(
                linkedInMetaData.getAPISecret(session)).build();

            OAuthAccount account = null;
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

            final Token accessToken = new Token(account.getToken(), account.getSecret());
            final OAuthRequest request = new OAuthRequest(method, url);
            request.setConnectTimeout(5, TimeUnit.SECONDS);
            request.setReadTimeout(30, TimeUnit.SECONDS);
            service.signRequest(accessToken, request);
            return request.send();
        } catch (org.scribe.exceptions.OAuthException e) {
            // Handle Scribe's org.scribe.exceptions.OAuthException (inherits from RuntimeException)
            Throwable cause = e.getCause();
            if (cause instanceof java.net.SocketTimeoutException) {
                // A socket timeout
                throw OAuthExceptionCodes.CONNECT_ERROR.create(cause, new Object[0]);
            }

            throw OAuthExceptionCodes.OAUTH_ERROR.create(cause, e.getMessage());
        }
    }

    private JSONObject extractJson(final Response response) throws OXException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(response.getStream(), Charsets.UTF_8);
            final JSONValue value = JSONObject.parse(reader);
            if (value.isObject()) {
                return value.toObject();
            }
            throw OAuthExceptionCodes.JSON_ERROR.create("Not a JSON object, but " + value.getClass().getName());
        } catch (final JSONException e) {
            throw OAuthExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
        }
    }

    private JSONValue extractJsonValue(final Response response, final Collection<Object> fallback) throws OXException {
        UnsynchronizedPushbackReader reader = null;
        try {
            reader = new UnsynchronizedPushbackReader(new InputStreamReader(response.getStream(), Charsets.UTF_8));
            // Read first character...
            final int read = reader.read();
            if (read < 0) {
                return null;
            }
            final char c = (char) read;
            // ... and push it back to reader
            reader.unread(c);
            if ('[' == c || '{' == c) {
                // Expect JSON content
                return JSONObject.parse(reader);
            }
            LOG.warn("No JSON format in LinkedIn response. Assume XML format.");
            final String body = Streams.reader2string(reader);
            fallback.add(body);
            return null;
        } catch (final JSONException e) {
            throw OAuthExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw OAuthExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
        }
    }

    protected List<String> extractIds(final Response response) throws OXException {
        List<String> result = new LinkedList<String>();
        try {
            final JSONObject json = new JSONObject(response.getBody());
            final JSONArray ids = json.getJSONArray("values");
            result = extractIds(ids);
        } catch (final JSONException e) {
            LOG.error("", e);
        }
        return result;
    }

    protected List<String> extractIds(final JSONArray connections) throws OXException {
        final List<String> result = new LinkedList<String>();
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
        final Response response = performRequest(session, user, contextId, accountId, Verb.GET, CONNECTIONS_URL + IN_JSON);
        if (response == null) {
            return Collections.emptyList();
        }
        // FIXME: Error handling via exception is completely missing... :-(
        // at least log anything here
        if (response.getCode() != 200) {
            LOG.error(response.getBody());
        }
        final List<Object> fallback = new ArrayList<Object>(1);
        final JSONValue jsonValue = extractJsonValue(response, fallback);
        if (null != jsonValue) {
            final LinkedInXMLParser parser = new LinkedInXMLParser();
            final List<Contact> contacts = parser.parseConnections(jsonValue);
            return contacts;
        }
        // No JSON format
        if (fallback.isEmpty()) {
            return Collections.emptyList();
        }
        final LinkedInXMLParser parser = new LinkedInXMLParser();
        final List<Contact> contacts = parser.parseConnections(fallback.get(0).toString());
        return contacts;
    }

    @Override
    public JSONObject getProfileForId(final String id, final Session session, final int user, final int contextId, final int accountId) throws OXException {
        final String uri = "http://api.linkedin.com/v1/people/id=" + id + PERSONAL_FIELD_QUERY;
        final Response response = performRequest(session, user, contextId, accountId, Verb.GET, uri + IN_JSON);
        if (response == null) {
            return new JSONObject(0);
        }
        return extractJson(response);
    }

    @Override
    public JSONObject getRelationToViewer(final String id, final Session session, final int user, final int contextId, final int accountId) throws OXException {
        final String uri = "http://api.linkedin.com/v1/people/id=" + id + ":(relation-to-viewer)";
        final Response response = performRequest(session, user, contextId, accountId, Verb.GET, uri + IN_JSON);
        if (response == null) {
            return new JSONObject(0);
        }
        return extractJson(response);
    }

    @Override
    public JSONObject getConnections(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        final String uri = "http://api.linkedin.com/v1/people/~/connections" + PERSONAL_FIELD_QUERY;
        final Response response = performRequest(session, user, contextId, accountId, Verb.GET, uri + IN_JSON);
        if (response == null) {
            return new JSONObject(0);
        }
        return extractJson(response);
    }

    @Override
    public List<String> getUsersConnectionsIds(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        final String uri = "http://api.linkedin.com/v1/people/~/connections:(id)";
        final Response response = performRequest(session, user, contextId, accountId, Verb.GET, uri + IN_JSON);
        if (response == null) {
            return Collections.emptyList();
        }
        return extractIds(response);
    }

    public JSONObject getFullProfileById(final String id, final Session session, final int user, final int contextId, final int accountId) throws OXException {
        final String uri = "http://api.linkedin.com/v1/people/id=" + id + ":(" + RELATION_TO_VIEWER + "," + PERSONAL_FIELDS + ")";
        final Response response = performRequest(session, user, contextId, accountId, Verb.GET, uri + IN_JSON);
        if (response == null) {
            return new JSONObject(0);
        }
        return extractJson(response);
    }

    @Override
    public JSONObject getFullProfileByEMail(final List<String> email, final Session session, final int user, final int contextId, final int accountId) throws OXException {

        String uri = null;
        if (email.size() == 1) {
            uri = "http://api.linkedin.com/v1/people/email=" + email.get(0) + ":(" + RELATION_TO_VIEWER + "," + PERSONAL_FIELDS + ")";
        } else {
            final StringBuilder b = new StringBuilder("http://api.linkedin.com/v1/people::(");
            for (final String s : email) {
                b.append("email=").append(s).append(',');
            }
            b.setLength(b.length() - 1);
            b.append("):(").append(RELATION_TO_VIEWER).append(',').append(PERSONAL_FIELDS).append(')');
            uri = b.toString();
        }
        final Response response = performRequest(session, user, contextId, accountId, Verb.GET, uri + IN_JSON);
        if (response == null) {
            return new JSONObject(0);
        }
        return extractJson(response);
    }

    @Override
    public JSONObject getFullProfileByFirstAndLastName(String firstName, String lastName, ServerSession session, final int user, final int contextId, final int accountId) throws OXException {
        try {
            String uri = "http://api.linkedin.com/v1/people-search?first-name=" + URLEncoder.encode(firstName, "UTF-8") + "&last-name=" + URLEncoder.encode(
                lastName,
                "UTF-8") + "&sort=distance&format=json";
            Response response = performRequest(session, user, contextId, accountId, Verb.GET, uri);
            if (response == null) {
                return new JSONObject();
            }
            JSONObject data = extractJson(response);
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
            } else {
                uri = "http://api.linkedin.com/v1/people-search?first-name=" + URLEncoder.encode(lastName, "UTF-8") + "&last-name=" + URLEncoder.encode(
                    firstName,
                    "UTF-8") + "&sort=distance&format=json";
                response = performRequest(session, user, contextId, accountId, Verb.GET, uri);
                if (response == null) {
                    return new JSONObject();
                }
                data = extractJson(response);
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
        } catch (UnsupportedEncodingException e) {
            // Ignore
        }
        return null;

    }

    @Override
    public JSONObject getNetworkUpdates(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        final String uri = "http://api.linkedin.com/v1/people/~/network/updates" + IN_JSON + "&type=CONN";
        final Response response = performRequest(session, user, contextId, accountId, Verb.GET, uri);
        if (response == null) {
            return new JSONObject();
        }
        return extractJson(response);
    }

    @Override
    public JSONObject getMessageInbox(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        final String uri = "http://api.linkedin.com/v1/people/~/mailbox:(id,folder,from:(person:(id,first-name,last-name,picture-url,headline)),recipients:(person:(id,first-name,last-name,picture-url,headline)),subject,short-body,last-modified,timestamp,mailbox-item-actions,body)?message-type=message-connections,invitation-request,invitation-reply,inmail-direct-connection&format=json";
        final Response response = performRequest(session, user, contextId, accountId, Verb.GET, uri);
        if (response == null) {
            return new JSONObject();
        }
        return extractJson(response);
    }

}
