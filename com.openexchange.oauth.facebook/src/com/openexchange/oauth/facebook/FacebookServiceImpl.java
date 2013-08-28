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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
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
import com.openexchange.log.LogFactory;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.session.Session;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * {@link FacebookServiceImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class FacebookServiceImpl implements FacebookService {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(FacebookServiceImpl.class));

    private final com.openexchange.oauth.OAuthService oAuthService;
    private final OAuthServiceMetaDataFacebookImpl facebookMetaData;

    public FacebookServiceImpl(final com.openexchange.oauth.OAuthService oAuthService, final OAuthServiceMetaDataFacebookImpl facebookMetaData) {
        this.oAuthService = oAuthService;
        this.facebookMetaData = facebookMetaData;
    }

    @Override
    public List<Contact> getContacts(final Session session, final int user, final int contextId, final int accountId) throws OXException {

        List<Contact> contacts = new ArrayList<Contact>();
        final OAuthService service = new ServiceBuilder().provider(FacebookApi.class).apiKey(facebookMetaData.getAPIKey(session)).apiSecret(
            facebookMetaData.getAPISecret(session)).build();

        OAuthAccount account = null;

        try {
            account = oAuthService.getAccount(accountId, session, user, contextId);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
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
                LOG.error(e.getMessage(), e);
            }

            // get the users connections
            final OAuthRequest connectionsRequest = new OAuthRequest(
                Verb.GET,
                "https://api.facebook.com/method/fql.query?query=SELECT%20name,first_name,last_name,email,birthday_date,pic_big,hometown_location%20from%20user%20where%20uid%20in%20%28SELECT%20uid2%20from%20friend%20where%20uid1=" + myuid + "%29&format=JSON");
            service.signRequest(accessToken, connectionsRequest);
            final Response connectionsResponse = connectionsRequest.send();

            // parse the returned JSON into neat little contacts
            contacts = parseIntoContacts(connectionsResponse.getBody());
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

    public List<Contact> parseIntoContacts(final String jsonString) throws OXException {
        final List<Contact> contacts = new ArrayList<Contact>();

        try {
            final JSONArray allConnections = new JSONArray(jsonString);
            for (int i = 0; i < allConnections.length(); i++) {
                final JSONObject connection = allConnections.getJSONObject(i);
                final Contact contact = new Contact();
                if (JSONObject.NULL != connection.get("first_name") && !"".equals(connection.get("first_name")) && !"nil".equals(connection.get("first_name"))) {
                    contact.setGivenName((String) connection.get("first_name"));
                }
                if (JSONObject.NULL != connection.get("last_name") && !"".equals(connection.get("last_name")) && !"nil".equals(connection.get("last_name"))) {
                    contact.setSurName((String) connection.get("last_name"));
                }

                // TODO: this should be parallelized. It is these request that take longest
                if (JSONObject.NULL != connection.get("pic_big") && !"".equals(connection.get("pic_big")) && !"nil".equals(connection.get("pic_big"))) {
                    try {
                        OXContainerConverter.loadImageFromURL(contact, (String) connection.get("pic_big"));
                    } catch (final ConverterException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

                // No email-addresses available yet via API, sorry
                // System.out.println(connection.get("email"));

                if (JSONObject.NULL != connection.get("birthday_date") && !"".equals(connection.get("birthday_date")) && !"nil".equals(connection.get("birthday_date"))) {
                    final String dateString = (String) connection.get("birthday_date");
                    final Integer month = Integer.parseInt(dateString.substring(0, 2)) - 1;
                    final Integer day = Integer.parseInt(dateString.substring(3, 5));
                    Integer year = 0;
                    // year is available
                    if (dateString.length() == 10) {
                        year = Integer.parseInt(dateString.substring(6, 10)) - 1900;
                    }
                    // only set the birthday if there is a year given as well (Bug 23009 - Facebook birthday stored as "0000-00-00" in the database)
                    if (year != 0){
                    	contact.setBirthday(new Date(year, month, day));
                    }
                }
                // System.out.println();

                if (JSONObject.NULL != connection.get("hometown_location")) {
                    final JSONObject hometownLocation = (JSONObject) connection.get("hometown_location");
                    if (JSONObject.NULL != hometownLocation.get("city") && !"".equals(hometownLocation.get("city")) && !"nil".equals(hometownLocation.get("city"))) {
                        contact.setCityHome((String) hometownLocation.get("city"));
                    }
                    if (JSONObject.NULL != hometownLocation.get("country") && !"".equals(hometownLocation.get("country")) && !"nil".equals(hometownLocation.get("country"))) {
                        contact.setCountryHome((String) hometownLocation.get("country"));
                    }
                    if (JSONObject.NULL != hometownLocation.get("zip") && !"".equals(hometownLocation.get("zip")) && !"nil".equals(hometownLocation.get("zip"))) {
                        contact.setPostalCodeHome((String) hometownLocation.get("zip"));
                    }
                }
                contacts.add(contact);
            }
        } catch (final JSONException e) {
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
            LOG.error(e.getMessage(), e);
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
            LOG.error(e.getMessage(), e);
        }
        return displayName;
    }

}
