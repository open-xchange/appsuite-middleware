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
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.msn.osgi.MSNOAuthActivator;
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

    public MSNServiceImpl(MSNOAuthActivator activator){
        this.activator = activator;
    }

    @Override
    public List<Contact> getContacts(String password, int user, int contextId, int accountId) {
        List<Contact> contacts = new ArrayList<Contact>();

        OAuthAccount account = null;
        try {
            com.openexchange.oauth.OAuthService oAuthService = activator.getOauthService();
            account = oAuthService.getAccount(accountId, password, user, contextId);
            // the account contains the refresh-token in this case
            String wrap_access_token = useRefreshTokenToGetAccessToken(account.getToken());
            JSONObject response = useAccessTokenToAccessData(wrap_access_token);
            contacts = parseIntoContacts(wrap_access_token, response);

        } catch (OXException e) {
            LOG.error(e);
        }

        return contacts;
    }

    /**
     * @param wrap_access_token
     * @return
     */
    private JSONObject useAccessTokenToAccessData(String wrap_access_token) {
        JSONObject wholeResponse = new JSONObject();
        String responseString = "";
        String protectedUrl = "http://apis.live.net/V4.1/";
        GetMethod getMethod = new GetMethod(protectedUrl);
        getMethod.setRequestHeader("Accept", "application/json");
        getMethod.setRequestHeader("Content-type", "application/json");
        getMethod.setRequestHeader("Authorization", "WRAP access_token=" + wrap_access_token);

        HttpClient client = new HttpClient();
        client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        try {
            int responseCode = client.executeMethod(getMethod);

            GetMethod getMethod2 = new GetMethod(protectedUrl + "Contacts/");
            getMethod2.setRequestHeader("Accept", "application/json");
            getMethod2.setRequestHeader("Content-type", "application/json");
            getMethod2.setRequestHeader("Authorization", "WRAP access_token=" + wrap_access_token);
            int responseCode2 = client.executeMethod(getMethod2);

            JSONObject response = new JSONObject(getMethod2.getResponseBodyAsString());
            String baseURI = "";
            if (null != response && response.has("BaseUri")) {
                baseURI = response.getString("BaseUri");
            }
            if (!baseURI.equals("")) {
                String finalURL = baseURI + "Contacts/AllContacts";
                GetMethod getMethod3 = new GetMethod(finalURL);
                getMethod3.setRequestHeader("Accept", "application/json");
                getMethod3.setRequestHeader("Content-type", "application/json");
                getMethod3.setRequestHeader("Authorization", "WRAP access_token=" + wrap_access_token);
                int responseCode3 = client.executeMethod(getMethod3);
                wholeResponse = new JSONObject(getMethod3.getResponseBodyAsString());

            }
        } catch (HttpException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
        } catch (JSONException e) {
            LOG.error(e);
        }
        return wholeResponse;
    }

    private List<Contact> parseIntoContacts(String wrap_access_token, JSONObject wholeResponse) {
        List<Contact> contacts = new ArrayList<Contact>();
        HttpClient client = new HttpClient();
        client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        try {
            String baseURI = "";
            if (wholeResponse.has("BaseUri")) {
                baseURI = wholeResponse.getString("BaseUri");
            }

            if (wholeResponse.has("Entries")) {
                JSONArray entries = (JSONArray) wholeResponse.get("Entries");
                for (int i = 0; i < entries.length(); i++) {
                    Contact contact = new Contact();
                    JSONObject entry = entries.getJSONObject(i);
                    String contactURI = entry.getString("SelfLink");
                    if (null != contactURI) {
                        GetMethod getMethod4 = new GetMethod(baseURI + contactURI);
                        getMethod4.setRequestHeader("Accept", "application/json");
                        getMethod4.setRequestHeader("Content-type", "application/json");
                        getMethod4.setRequestHeader("Authorization", "WRAP access_token=" + wrap_access_token);
                        int responseCode4 = client.executeMethod(getMethod4);
                        JSONObject wlcontact = new JSONObject(getMethod4.getResponseBodyAsString());

                        if (wlcontact.has("FirstName")) {
                            String firstname = wlcontact.getString("FirstName");
                            contact.setGivenName(firstname);
                        }
                        if (wlcontact.has("LastName")) {
                            String lastname = wlcontact.getString("LastName");
                            contact.setSurName(lastname);
                        }
                        if (wlcontact.has("Locations")) {
                            JSONArray locations = wlcontact.getJSONArray("Locations");
                            JSONObject location = locations.getJSONObject(0);
                            if (location.has("City")) {
                                String city = location.getString("City");
                                contact.setCityBusiness(city);
                            }
                            if (location.has("CountryRegion")) {
                                String country = location.getString("CountryRegion");
                                contact.setCountryBusiness(country);
                            }
                        }
                        if (wlcontact.has("ThumbnailImageLink")) {
                            String imageUrl = wlcontact.getString("ThumbnailImageLink");
                            try {
                                OXContainerConverter.loadImageFromURL(contact, imageUrl);
                            } catch (ConverterException e) {
                                LOG.error(e);
                            }
                        }
                        contacts.add(contact);
                    }
                }
            }
        } catch (HttpException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
        } catch (JSONException e) {
            LOG.error(e);
        }
        return contacts;
    }

    /**
     * @param token
     */
    private String useRefreshTokenToGetAccessToken(String wrap_refresh_token) {
        String accessToken = "";
        HttpClient client = new HttpClient();
        PostMethod postMethod = new PostMethod("https://consent.live.com/RefreshToken.aspx" + "?wrap_refresh_token=" + wrap_refresh_token);

        RequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(postMethod.getQueryString(), "application/x-www-form-urlencoded", "UTF-8");
            postMethod.setRequestEntity(requestEntity);
            int responseCode = client.executeMethod(postMethod);
            String response = URLDecoder.decode(postMethod.getResponseBodyAsString(), "UTF-8");
            Pattern pattern = Pattern.compile("wrap_access_token=([^&]*).*");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                accessToken = matcher.group(1);
            } else {
                LOG.error("No AccessToken found in response : " + postMethod.getResponseBodyAsString());
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        } catch (HttpException e) {
            LOG.error(e);
        } catch (IOException e) {
            LOG.error(e);
        }
        return accessToken;
    }

    @Override
    public String getAccountDisplayName(String password, int user, int contextId, int accountId) {
        String displayName = "";
        try {
            com.openexchange.oauth.OAuthService oAuthService = activator.getOauthService();
            OAuthAccount account = oAuthService.getAccount(accountId, password, user, contextId);
            displayName = account.getDisplayName();
        } catch (OXException e) {
            LOG.error(e);
        }
        return displayName;
    }

}
