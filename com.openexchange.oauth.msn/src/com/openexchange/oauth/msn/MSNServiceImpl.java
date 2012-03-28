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

package com.openexchange.oauth.msn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.msn.osgi.MSNOAuthActivator;
import com.openexchange.session.Session;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * {@link MSNServiceImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class MSNServiceImpl implements MSNService {

    private final MSNOAuthActivator activator;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MSNServiceImpl.class));

    public MSNServiceImpl(final MSNOAuthActivator activator){
        this.activator = activator;
    }

    @Override
    public List<Contact> getContacts(final Session session, final int user, final int contextId, final int accountId) {
        List<Contact> contacts = new ArrayList<Contact>();

        OAuthAccount account = null;
        try {
            final com.openexchange.oauth.OAuthService oAuthService = activator.getOauthService();
            account = oAuthService.getAccount(accountId, session, user, contextId);
            // the account contains the refresh-token in this case
            final String wrap_access_token = useRefreshTokenToGetAccessToken(account.getToken());
            final JSONObject response = useAccessTokenToAccessData(wrap_access_token);
            contacts = parseIntoContacts(wrap_access_token, response);

        } catch (final OXException e) {
            LOG.error(e);
        }

        return contacts;
    }

    /**
     * @param wrap_access_token
     * @return
     */
    private JSONObject useAccessTokenToAccessData(final String wrap_access_token) {
        JSONObject wholeResponse = new JSONObject();
        final String responseString = "";
        final String protectedUrl = "http://apis.live.net/V4.1/";
        final GetMethod getMethod = new GetMethod(protectedUrl);
        getMethod.setRequestHeader("Accept", "application/json");
        getMethod.setRequestHeader("Content-type", "application/json");
        getMethod.setRequestHeader("Authorization", "WRAP access_token=" + wrap_access_token);

        final HttpClient client = new HttpClient();
        client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        try {
            final int responseCode = client.executeMethod(getMethod);

            final GetMethod getMethod2 = new GetMethod(protectedUrl + "Contacts/");
            getMethod2.setRequestHeader("Accept", "application/json");
            getMethod2.setRequestHeader("Content-type", "application/json");
            getMethod2.setRequestHeader("Authorization", "WRAP access_token=" + wrap_access_token);
            final int responseCode2 = client.executeMethod(getMethod2);

            final JSONObject response = new JSONObject(getMethod2.getResponseBodyAsString());
            String baseURI = "";
            if (null != response && response.has("BaseUri")) {
                baseURI = response.getString("BaseUri");
            }
            if (!baseURI.equals("")) {
                final String finalURL = baseURI + "Contacts/AllContacts";
                final GetMethod getMethod3 = new GetMethod(finalURL);
                getMethod3.setRequestHeader("Accept", "application/json");
                getMethod3.setRequestHeader("Content-type", "application/json");
                getMethod3.setRequestHeader("Authorization", "WRAP access_token=" + wrap_access_token);
                final int responseCode3 = client.executeMethod(getMethod3);
                wholeResponse = new JSONObject(getMethod3.getResponseBodyAsString());

            }
        } catch (final HttpException e) {
            LOG.error(e);
        } catch (final IOException e) {
            LOG.error(e);
        } catch (final JSONException e) {
            LOG.error(e);
        }
        return wholeResponse;
    }

    private List<Contact> parseIntoContacts(final String wrap_access_token, final JSONObject wholeResponse) {
        final List<Contact> contacts = new ArrayList<Contact>();
        final HttpClient client = new HttpClient();
        client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        try {
            String baseURI = "";
            if (wholeResponse.has("BaseUri")) {
                baseURI = wholeResponse.getString("BaseUri");
            }

            if (wholeResponse.has("Entries")) {
                final JSONArray entries = (JSONArray) wholeResponse.get("Entries");
                for (int i = 0; i < entries.length(); i++) {
                    final Contact contact = new Contact();
                    final JSONObject entry = entries.getJSONObject(i);
                    final String contactURI = entry.getString("SelfLink");
                    if (null != contactURI) {
                        final GetMethod getMethod4 = new GetMethod(baseURI + contactURI);
                        getMethod4.setRequestHeader("Accept", "application/json");
                        getMethod4.setRequestHeader("Content-type", "application/json");
                        getMethod4.setRequestHeader("Authorization", "WRAP access_token=" + wrap_access_token);
                        final int responseCode4 = client.executeMethod(getMethod4);
                        final JSONObject wlcontact = new JSONObject(getMethod4.getResponseBodyAsString());

                        if (wlcontact.has("FirstName")) {
                            final String firstname = wlcontact.getString("FirstName");
                            contact.setGivenName(firstname);
                        }
                        if (wlcontact.has("LastName")) {
                            final String lastname = wlcontact.getString("LastName");
                            contact.setSurName(lastname);
                        }
                        if (wlcontact.has("Locations")) {
                            final JSONArray locations = wlcontact.getJSONArray("Locations");
                            final JSONObject location = locations.getJSONObject(0);
                            if (location.has("City")) {
                                final String city = location.getString("City");
                                contact.setCityBusiness(city);
                            }
                            if (location.has("CountryRegion")) {
                                final String country = location.getString("CountryRegion");
                                contact.setCountryBusiness(country);
                            }
                        }
                        if (wlcontact.has("ThumbnailImageLink")) {
                            final String imageUrl = wlcontact.getString("ThumbnailImageLink");
                            try {
                                OXContainerConverter.loadImageFromURL(contact, imageUrl);
                            } catch (final ConverterException e) {
                                LOG.error(e);
                            }
                        }
                        contacts.add(contact);
                    }
                }
            }
        } catch (final HttpException e) {
            LOG.error(e);
        } catch (final IOException e) {
            LOG.error(e);
        } catch (final JSONException e) {
            LOG.error(e);
        }
        return contacts;
    }

    /**
     * @param token
     */
    private String useRefreshTokenToGetAccessToken(final String wrap_refresh_token) {
        String accessToken = "";
        final HttpClient client = new HttpClient();
        final PostMethod postMethod = new PostMethod("https://consent.live.com/RefreshToken.aspx" + "?wrap_refresh_token=" + wrap_refresh_token);

        RequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(postMethod.getQueryString(), "application/x-www-form-urlencoded", "UTF-8");
            postMethod.setRequestEntity(requestEntity);
            final int responseCode = client.executeMethod(postMethod);
            final String response = URLDecoder.decode(postMethod.getResponseBodyAsString(), "UTF-8");
            final Pattern pattern = Pattern.compile("wrap_access_token=([^&]*).*");
            final Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                accessToken = matcher.group(1);
            } else {
                LOG.error("No AccessToken found in response : " + postMethod.getResponseBodyAsString());
            }
        } catch (final UnsupportedEncodingException e) {
            LOG.error(e);
        } catch (final HttpException e) {
            LOG.error(e);
        } catch (final IOException e) {
            LOG.error(e);
        }
        return accessToken;
    }

    @Override
    public String getAccountDisplayName(final Session session, final int user, final int contextId, final int accountId) {
        String displayName = "";
        try {
            final com.openexchange.oauth.OAuthService oAuthService = activator.getOauthService();
            final OAuthAccount account = oAuthService.getAccount(accountId, session, user, contextId);
            displayName = account.getDisplayName();
        } catch (final OXException e) {
            LOG.error(e);
        }
        return displayName;
    }

}
