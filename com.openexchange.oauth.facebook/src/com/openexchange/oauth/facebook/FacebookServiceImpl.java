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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang.time.DateUtils;
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
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPools;
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
    public List<Contact> getContacts(final Session session, final int user, final int contextId, final int accountId) throws OXException {

        List<Contact> contacts = Collections.emptyList();
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
            final Response ownProfileResponse = ownProfileRequest.send();

            String myuid = "";
            try {
                final JSONObject object = new JSONObject(ownProfileResponse.getBody());
                myuid = object.getString("id");
            } catch (final JSONException e) {
                LOG.error("", e);
            }

            // get the users connections
            final OAuthRequest connectionsRequest = new OAuthRequest(
                Verb.GET,
                "https://api.facebook.com/method/fql.query?query=SELECT%20name,first_name,last_name,email,birthday_date,pic_big,current_location,profile_url%20from%20user%20where%20uid%20in%20%28SELECT%20uid2%20from%20friend%20where%20uid1=" + myuid + "%29&format=JSON");
            service.signRequest(accessToken, connectionsRequest);
            final Response connectionsResponse = connectionsRequest.send();

            // parse the returned JSON into neat little contacts
            String jsonString = connectionsResponse.getBody();
            try {
                final JSONArray allConnections = new JSONArray(jsonString);
                jsonString = null;
                contacts = parseIntoContacts(allConnections);
            } catch (JSONException e) {
                // Maybe this is a JSONObject with an error
                try {
                    final JSONObject obj = new JSONObject(jsonString);
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

        return contacts;

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

    public List<Contact> parseIntoContacts(final JSONArray allConnections) throws OXException {
        final int length = allConnections.length();
        if (length <= 0) {
            return Collections.emptyList();
        }

        final List<Contact> contacts = new ArrayList<Contact>(length);
        final CompletionService<Void> completionService = new ThreadPoolCompletionService<Void>(ThreadPools.getThreadPool());
        int taskCount = 0;

        for (int i = 0; i < length; i++) {
            final JSONObject connection = allConnections.optJSONObject(i);
            if (null != connection) {
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
                        completionService.submit(new Callable<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    OXContainerConverter.loadImageFromURL(contact, imageUrl);
                                } catch (final Exception e) {
                                    LOG.error("", e);
                                }
                                return null;
                            }
                        });
                        taskCount++;
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
                contacts.add(contact);
            }
        }

        if (taskCount > 0) {
            ThreadPools.awaitCompletionService(completionService, taskCount);
        }

        return contacts;
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
     * Sets the birthday for the contact based on the facebook information
     * 
     * @param contact - the {@link Contact} to set the birthday for
     * @param birthday - the string the birthday is included in
     */
    protected void setBirthday(Contact contact, String birthdayString) {
        if (isValid(birthdayString) && !"00/00/0000".equals(birthdayString)) {

            Date birthday = null;
            try {
                birthday = DateUtils.parseDate(birthdayString, new String[] { "MM/dd/yyyy" });
            } catch (ParseException parseException) {
                LOG.info(
                    "Unable to parse birthday string for facebook user '{} {}'! Tried to parse {}.",
                    contact.getGivenName(),
                    contact.getSurName(),
                    birthdayString,
                    parseException);
            }

            // only set the birthday if there is a year given as well (Bug 23009 - Facebook birthday stored as "0000-00-00" in
            // the database)
            // because of the defined format 'MM/dd/yyyy' dates without a year are not accepted and 'birthday' will be null
            if (birthday != null) {
                contact.setBirthday(birthday);
            }
        }
    }

    /**
     * Sets the email address for the contact based on the facebook information
     * 
     * @param contact - the {@link Contact} to set the birthday for
     * @param email - the string the email is included in
     */
    protected void setEmail(Contact contact, String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();

            contact.setEmail1(email);
        } catch (AddressException addressException) {
            LOG.info(
                "Email address for facebook user '{} {}' is not valid and cannot be imported! Tried to import {}.",
                contact.getGivenName(),
                contact.getSurName(),
                email,
                addressException);
        }
    }
}
