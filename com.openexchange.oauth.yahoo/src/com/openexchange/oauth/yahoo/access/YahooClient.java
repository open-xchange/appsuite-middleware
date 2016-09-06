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

package com.openexchange.oauth.yahoo.access;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.scope.Module;
import com.openexchange.oauth.yahoo.internal.YahooApi2;
import com.openexchange.oauth.yahoo.internal.YahooRequestTuner;
import com.openexchange.session.Session;

/**
 * {@link YahooClient}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class YahooClient {

    private static final Pattern PATTERN_GUID = Pattern.compile("<value>([^<]*)<");

    private static final String ALL_CONTACT_IDS_URL = "https://social.yahooapis.com/v1/user/GUID/contacts?format=json";
    private static final String SINGLE_CONTACT_URL = "https://social.yahooapis.com/v1/user/GUID/contact/CONTACT_ID?format=json";

    private final OAuthService service;
    private final OAuthAccount oauthAccount;

    /**
     * Initialises a new {@link YahooClient}.
     * 
     * @throws OXException
     */
    public YahooClient(OAuthAccount oauthAccount, Session session) throws OXException {
        super();
        this.oauthAccount = oauthAccount;
        service = new ServiceBuilder().provider(YahooApi2.class).apiKey(oauthAccount.getMetaData().getAPIKey(session)).apiSecret(oauthAccount.getMetaData().getAPISecret(session)).build();
    }

    /**
     * Pings the account
     * 
     * @return <code>true</code> if the account was successfully pinged; <code>false</code> otherwise
     * @throws OXException if a connection error is occurred
     */
    public boolean ping() throws OXException {
        return !Strings.isEmpty(getGUID());
    }

    /**
     * Retrieves the GUID for the account
     * 
     * @return The GUID of the account or an empty string if no GUID was returned from the provider
     * @throws OXException if a connection error is occurred
     */
    public String getGUID() throws OXException {
        OAuthRequest guidRequest = new OAuthRequest(Verb.GET, "https://social.yahooapis.com/v1/me/guid?format=xml");
        service.signRequest(new Token(oauthAccount.getToken(), oauthAccount.getSecret()), guidRequest);
        Response guidResponse;
        try {
            guidResponse = guidRequest.send(YahooRequestTuner.getInstance());
        } catch (org.scribe.exceptions.OAuthException e) {
            // Handle Scribe's org.scribe.exceptions.OAuthException (inherits from RuntimeException)
            Throwable cause = e.getCause();
            if (cause instanceof java.net.SocketTimeoutException) {
                // A socket timeout
                throw OAuthExceptionCodes.CONNECT_ERROR.create(cause, new Object[0]);
            }

            throw OAuthExceptionCodes.OAUTH_ERROR.create(cause, e.getMessage());
        }
        String contentType = guidResponse.getHeader("Content-Type");
        if (null == contentType || false == contentType.toLowerCase().contains("application/xml")) {
            throw OAuthExceptionCodes.NOT_A_VALID_RESPONSE.create();
        }

        final Matcher matcher = PATTERN_GUID.matcher(guidResponse.getBody());
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * Gets all contacts in a single request
     * 
     * @return
     * @throws OXException
     */
    public JSONObject getContacts() throws OXException {
        String guid = getGUID();

        Token accessToken = new Token(oauthAccount.getToken(), oauthAccount.getSecret());

        // Now get the ids of all the users contacts
        OAuthRequest request = new OAuthRequest(Verb.GET, ALL_CONTACT_IDS_URL.replace("GUID", guid));
        service.signRequest(accessToken, request);
        final Response response = request.send(YahooRequestTuner.getInstance());
        if (response.getCode() == 403) {
            throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(API.YAHOO.getShortName(), Module.contacts_ro.getDisplayName());
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
        final String singleContactUrl = SINGLE_CONTACT_URL.replace("GUID", getGUID()).replace("CONTACT_ID", contactId);
        // Request
        final OAuthRequest singleContactRequest = new OAuthRequest(Verb.GET, singleContactUrl);
        service.signRequest(accessToken, singleContactRequest);
        final Response singleContactResponse = singleContactRequest.send(YahooRequestTuner.getInstance());
        return extractJson(singleContactResponse);
    }
    
    /**
     * Get the account's display name
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
        } catch (final JSONException e) {
            throw OAuthExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
        }
    }
}
