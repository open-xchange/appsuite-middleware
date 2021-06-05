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

package com.openexchange.oauth.yahoo.access;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLHandshakeException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.api.YahooApi2;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.oauth.yahoo.internal.YahooRequestTuner;
import com.openexchange.session.Session;

/**
 * {@link YahooClient}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class YahooClient {

    private static final String EMPTY_STRING = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(YahooClient.class);

    private static final String ALL_CONTACT_IDS_URL = "https://social.yahooapis.com/v1/user/me/contacts?format=json";
    private static final String SINGLE_CONTACT_URL = "https://social.yahooapis.com/v1/user/me/contact/CONTACT_ID?format=json";

    private final OAuthService scribeService;
    private final OAuthAccount oauthAccount;

    /**
     * Initializes a new {@link YahooClient}.
     *
     * @throws OXException
     */
    public YahooClient(OAuthAccount oauthAccount, Session session) throws OXException {
        super();
        this.oauthAccount = oauthAccount;
        scribeService = new ServiceBuilder().provider(YahooApi2.class).apiKey(oauthAccount.getMetaData().getAPIKey(session)).apiSecret(oauthAccount.getMetaData().getAPISecret(session)).build();
    }

    /**
     * Executes specified request and returns its response.
     *
     * @param request The request
     * @return The response
     * @throws OXException If executing request fails
     */
    private Response execute(OAuthRequest request) throws OXException {
        try {
            return request.send(YahooRequestTuner.getInstance());
        } catch (org.scribe.exceptions.OAuthException e) {
            // Handle Scribe's org.scribe.exceptions.OAuthException (inherits from RuntimeException)
            if (ExceptionUtils.isEitherOf(e, SSLHandshakeException.class)) {
                List<Object> displayArgs = new ArrayList<>(2);
                displayArgs.add(SSLExceptionCode.extractArgument(e, "fingerprint"));
                displayArgs.add("social.yahooapis.com");
                throw SSLExceptionCode.UNTRUSTED_CERTIFICATE.create(e, displayArgs.toArray(new Object[] {}));
            }

            Throwable cause = e.getCause();
            if (cause instanceof java.net.SocketTimeoutException) {
                // A socket timeout
                throw OAuthExceptionCodes.CONNECT_ERROR.create(cause, new Object[0]);
            }

            throw OAuthExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Pings the account
     *
     * @return <code>true</code> if the account was successfully pinged; <code>false</code> otherwise
     * @throws OXException if a connection error is occurred
     */
    public boolean ping() throws OXException {
        return Strings.isNotEmpty(getGUID());
    }

    /**
     * Retrieves the GUID for the account
     *
     * @return The GUID of the account or an empty string if no GUID was returned from the provider
     * @throws OXException if a connection error is occurred
     */
    public String getGUID() throws OXException {
        OAuthRequest guidRequest = new OAuthRequest(Verb.GET, "https://social.yahooapis.com/v1/me/guid?format=json");
        scribeService.signRequest(new Token(oauthAccount.getToken(), oauthAccount.getSecret()), guidRequest);
        Response guidResponse = execute(guidRequest);
        String contentType = guidResponse.getHeader("Content-Type");
        if (null == contentType || false == contentType.toLowerCase().contains("application/json")) {
            throw OAuthExceptionCodes.NOT_A_VALID_RESPONSE.create();
        }

        try {
            JSONObject body = new JSONObject(guidResponse.getBody());
            if (false == body.hasAndNotNull("guid")) {
                return EMPTY_STRING;
            }
            JSONObject guidBody = body.optJSONObject("guid");
            if (guidBody == null) {
                return EMPTY_STRING;
            }
            return guidBody.optString("value");
        } catch (JSONException e) {
            LOGGER.debug("Unable to extract GUID from response {}", guidResponse.getBody());
        }
        return EMPTY_STRING;
    }

    /**
     * Gets all contacts in a single request
     *
     * @return
     * @throws OXException
     */
    public JSONObject getContacts() throws OXException {
        Token accessToken = new Token(oauthAccount.getToken(), oauthAccount.getSecret());

        // Now get the ids of all the users contacts
        OAuthRequest request = new OAuthRequest(Verb.GET, ALL_CONTACT_IDS_URL);
        scribeService.signRequest(accessToken, request);
        final Response response = execute(request);
        if (response.getCode() == 403) {
            throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(KnownApi.YAHOO.getDisplayName(), OXScope.contacts_ro.getDisplayName());
        }
        final String contentType = response.getHeader("Content-Type");
        if (null == contentType || false == contentType.toLowerCase().contains("application/json")) {
            throw OAuthExceptionCodes.NOT_A_VALID_RESPONSE.create();
        }
        return extractJson(response);
    }

    /**
     * Gets the contact with the specified identifier
     *
     * @param contactId The contact identifier
     * @return
     * @throws OXException
     */
    public JSONObject getContact(String contactId) throws OXException {
        Token accessToken = new Token(oauthAccount.getToken(), oauthAccount.getSecret());
        final String singleContactUrl = SINGLE_CONTACT_URL.replace("CONTACT_ID", contactId);
        // Request
        final OAuthRequest singleContactRequest = new OAuthRequest(Verb.GET, singleContactUrl);
        scribeService.signRequest(accessToken, singleContactRequest);
        final Response singleContactResponse = execute(singleContactRequest);
        return extractJson(singleContactResponse);
    }

    /**
     * Get the account's display name
     * 
     * @return the account's display name
     */
    public String getDisplayName() {
        return oauthAccount.getDisplayName();
    }

    /**
     * Extracts JSON out of given response
     *
     * @param response
     * @return
     * @throws OXException
     */
    private JSONObject extractJson(final Response response) throws OXException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(response.getStream(), Charsets.UTF_8);
            final JSONValue value = JSONObject.parse(reader);
            if (value.isObject()) {
                return value.toObject();
            }
            throw OAuthExceptionCodes.JSON_ERROR.create("Not a JSON object, but " + value.getClass().getName());
        } catch (JSONException e) {
            throw OAuthExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
        }
    }
}
