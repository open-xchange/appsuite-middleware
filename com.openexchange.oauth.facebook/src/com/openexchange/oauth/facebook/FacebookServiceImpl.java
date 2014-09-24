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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.oauth.facebook;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * {@link FacebookServiceImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FacebookServiceImpl implements FacebookService {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FacebookServiceImpl.class);

    private final com.openexchange.oauth.OAuthService oAuthService;
    private final OAuthServiceMetaDataFacebookImpl facebookMetaData;

    public FacebookServiceImpl(final com.openexchange.oauth.OAuthService oAuthService, final OAuthServiceMetaDataFacebookImpl facebookMetaData) {
        super();
        this.oAuthService = oAuthService;
        this.facebookMetaData = facebookMetaData;
    }

    @Override
    public SearchIterator<Contact> getContacts(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        final OAuthService service = new ServiceBuilder().provider(FacebookApi.class).apiKey(facebookMetaData.getAPIKey(session)).apiSecret(
            facebookMetaData.getAPISecret(session)).build();
        OAuthAccount account = null;
        try {
            account = oAuthService.getAccount(accountId, session, user, contextId);
        } catch (final OXException e) {
            LOG.error("", e);
        }
        if (null != account) {
            // get the users own profile (for his id) with the given access token
            final Token accessToken = new Token(checkToken(account.getToken()), account.getSecret());
            final OAuthRequest ownProfileRequest = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");
            service.signRequest(accessToken, ownProfileRequest);
            Response ownProfileResponse;
            try {
                ownProfileResponse = ownProfileRequest.send(FacebookRequestTuner.getInstance());
            } catch (org.scribe.exceptions.OAuthException e) {
                // Handle Scribe's org.scribe.exceptions.OAuthException (inherits from RuntimeException)
                Throwable cause = e.getCause();
                if (cause instanceof java.net.SocketTimeoutException) {
                    // A socket timeout
                    throw OAuthExceptionCodes.CONNECT_ERROR.create(cause, new Object[0]);
                }

                throw OAuthExceptionCodes.OAUTH_ERROR.create(cause, e.getMessage());
            }

            String myuid = "";
            try {
                final JSONObject object = new JSONObject(ownProfileResponse.getBody());
                myuid = object.getString("id");
            } catch (final JSONException e) {
                LOG.error("", e);
            }

            // get the users connections
            JSONObject jResponse;
            {
                OAuthRequest connectionsRequest = new OAuthRequest(Verb.GET, "https://graph.facebook.com/v2.0/fql?q=SELECT%20name,first_name,last_name,email,birthday_date,pic_big,current_location,profile_url%20from%20user%20where%20uid%20in%20%28SELECT%20uid2%20from%20friend%20where%20uid1=" + myuid + "%29&format=JSON");
                service.signRequest(accessToken, connectionsRequest);
                final Response connectionsResponse = connectionsRequest.send(FacebookRequestTuner.getInstance());

                // parse the returned JSON into neat little contacts
                jResponse = toJsonObject(connectionsResponse);
            }
            try {
                return parse(jResponse.getJSONArray("data"));
            } catch (JSONException e) {
                // Maybe this is a JSONObject with an error
                try {
                    final JSONObject obj = jResponse;
                    if (obj.has("error")) {
                        LOG.error(obj.get("error").toString());
                        throw OXException.general(obj.getJSONObject("error").getString("message"));
                    }
                } catch (final JSONException x) {
                    // Give up
                }
                LOG.error("", e);
            }
        }
        return SearchIteratorAdapter.emptyIterator();
    }

    private JSONObject toJsonObject(Response connectionsResponse) throws OXException {
        try {
            return new JSONObject(connectionsResponse.getBody());
        } catch (JSONException fatal) {
            LOG.error("Facebook response is no valid JSON.", fatal);
            throw OXException.general("Facebook response is no valid JSON.", fatal);
        }
    }

    private static final Pattern P_EXPIRES = Pattern.compile("&expires(=[0-9]+)?$");

    private static String checkToken(final String accessToken) {
        if (accessToken.indexOf("&expires") < 0) {
            return accessToken;
        }
        final Matcher m = P_EXPIRES.matcher(accessToken);
        final StringBuffer sb = new StringBuffer(accessToken.length());
        if (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Parses facebook contacts from the supplied JSON array and provides them sequentially via a search iterator.
     *
     * @param allConnections The JSON array containing the facebook contacts
     * @return A search iterator for the parsed contacts
     */
    private SearchIterator<Contact> parse(final JSONArray allConnections) {
        if (null == allConnections || 0 == allConnections.length()) {
            return SearchIteratorAdapter.emptyIterator();
        }
        return new SearchIterator<Contact>() {

            int index = 0;

            @Override
            public int size() {
                return allConnections.length();
            }

            @Override
            public Contact next() throws OXException {
                JSONObject connection = allConnections.optJSONObject(index++);
                return null != connection ? parse(connection) : null;
            }

            @Override
            public boolean hasWarnings() {
                return false;
            }

            @Override
            public boolean hasNext() throws OXException {

                return index < allConnections.length();
            }

            @Override
            public OXException[] getWarnings() {
                return null;
            }

            @Override
            public void close() throws OXException {
                //
            }

            @Override
            public void addWarning(OXException warning) {
            }
        };
    }

    /**
     * Parses a facebook contact from the supplied JSON object.
     *
     * @param connection The JSON object containing the facebook contact
     * @return The parsed contact
     */
    private Contact parse(JSONObject connection) {
        final Contact contact = new Contact();
        {
            final String profileUrl = connection.optString("profile_url", null);
            if (isValid(profileUrl)) {
                contact.setURL(profileUrl);
            }
        }
        {
            final String givenName = connection.optString("first_name", null);
            if (isValid(givenName)) {
                contact.setGivenName(givenName);
            }
        }
        {
            final String surName = connection.optString("last_name", null);
            if (isValid(surName)) {
                contact.setSurName(surName);
            }
        }
        {
            final String email = connection.optString("email", null);
            setEmail(contact, email);
        }
        {
            final String imageUrl = connection.optString("pic_big", null);
            if (isValid(imageUrl)) {
                try {
                    OXContainerConverter.loadImageFromURL(contact, imageUrl);
                } catch (final Exception e) {
                    LOG.error("", e);
                }
            }
        }
        {
            final String birthdayString = connection.optString("birthday_date", null);
            setBirthday(contact, birthdayString);
        }
        final JSONObject currentLocation = connection.optJSONObject("current_location");
        if (null != currentLocation) {
            String tmp = connection.optString("city", null);
            if (isValid(tmp)) {
                contact.setCityHome(tmp);
            }
            tmp = connection.optString("country", null);
            if (isValid(tmp)) {
                contact.setCountryHome(tmp);
            }
            tmp = connection.optString("zip", null);
            if (isValid(tmp)) {
                contact.setPostalCodeHome(tmp);
            }
        }
        return contact;
    }

    @Override
    public String getAccountDisplayName(final Session session, final int user, final int contextId, final int accountId) {
        String displayName = "";
        try {
            final OAuthAccount account = oAuthService.getAccount(accountId, session, user, contextId);
            displayName = account.getDisplayName();
        } catch (final OXException e) {
            LOG.error("", e);
        }
        return displayName;
    }

    private static boolean isValid(final String toCheck) {
        return !Strings.isEmpty(toCheck) && !"nil".equals(toCheck);
    }

    /**
     * Sets the birthday for the contact based on the Facebook information
     *
     * @param contact - the {@link Contact} to set the birthday for
     * @param birthday - the string the birthday is included in
     */
    protected void setBirthday(Contact contact, String birthdayString) {
        if (isValid(birthdayString) && !"00/00/0000".equals(birthdayString)) {

            final String regex = "([0-9]{2})/([0-9]{2})/([0-9]{4})";
            if (birthdayString.matches(regex)) {
                final Pattern pattern = Pattern.compile(regex);
                final Matcher matcher = pattern.matcher(birthdayString);
                if (matcher.matches() && matcher.groupCount() == 3) {
                    final int month = Integer.parseInt(matcher.group(1));
                    final int day = Integer.parseInt(matcher.group(2));
                    final int year = Integer.parseInt(matcher.group(3));
                    final Calendar cal = Calendar.getInstance(TimeZones.UTC);
                    cal.clear();
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    cal.set(Calendar.MONTH, month - 1);
                    cal.set(Calendar.YEAR, year);
                    contact.setBirthday(cal.getTime());
                }
            } else {
                LOG.debug(
                    "Unable to parse birthday string for facebook user '{} {}' because pattern did not match! Tried to parse {}.",
                    contact.getGivenName(),
                    contact.getSurName(),
                    birthdayString);
            }
        }
    }

    /**
     * Sets the E-Mail address for the contact based on the Facebook information
     *
     * @param contact - the {@link Contact} to set the E-Mail address for
     * @param email - the string the email is included in
     */
    protected void setEmail(Contact contact, String email) {
        if (isValid(email)) {
            try {
                new InternetAddress(email).validate();
                contact.setEmail1(email);
            } catch (AddressException addressException) {
                LOG.debug(
                    "Email address for facebook user '{} {}' is not valid and cannot be imported! Tried to import {}.",
                    contact.getGivenName(),
                    contact.getSurName(),
                    email,
                    addressException);
            }
        }
    }

}
