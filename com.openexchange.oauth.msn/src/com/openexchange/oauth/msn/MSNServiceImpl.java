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

package com.openexchange.oauth.msn;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.msn.osgi.MSNOAuthActivator;
import com.openexchange.session.Session;

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
            final String wrap_access_token = useRefreshTokenToGetAccessToken(account, session);
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
        try {
            final String responseString = "";
            final String protectedUrl = "https://apis.live.net/v5.0/me/contacts?access_token=" + URLEncoder.encode(wrap_access_token, "UTF-8");
            final GetMethod getMethod = new GetMethod(protectedUrl);

            final HttpClient client = new HttpClient();
            client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
            final int responseCode = client.executeMethod(getMethod);
            String response = getMethod.getResponseBodyAsString();
            wholeResponse = new JSONObject(response);

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
        try {
            JSONArray arr = wholeResponse.getJSONArray("data");
            for(int i = 0, size = arr.length(); i < size; i++) {
            	JSONObject cObj = arr.getJSONObject(i);
            	Contact c = new Contact();

            	if (cObj.hasAndNotNull("first_name")) {
            		c.setGivenName(cObj.optString("first_name"));
            	}

            	if (cObj.hasAndNotNull("last_name")) {
            		c.setSurName(cObj.optString("last_name"));
            	}

            	if (cObj.hasAndNotNull("name")) {
            		c.setDisplayName(cObj.optString("name"));
            	} else {
            		c.setDisplayName(c.getGivenName() + " " + c.getSurName());
            	}

            	if (cObj.has("emails")) {
            		List<String> mailAddresses = new ArrayList<String>();
            		JSONObject emails = cObj.getJSONObject("emails");
            		for(String key: new String[]{"preferred", "account", "other", "personal","business"}) {
            			if (emails.hasAndNotNull(key)) {
            				String address = emails.optString(key);
            				if (!mailAddresses.contains(address)) {
            					mailAddresses.add(address);
            				}
            			}
            		}

            		int counter = 0;
            		for (String mailAddress : mailAddresses) {
						switch(counter) {
						case 0:
							c.setEmail1(mailAddress);
							counter++;
							break;
						case 1:
							c.setEmail2(mailAddress);
							counter++;
							break;
						case 2:
							c.setEmail3(mailAddress);
							counter++;
							break;
						}
					}
            	}

            	if (cObj.has("addresses")) {
            		JSONObject obj = cObj.getJSONObject("addresses");
            		if (obj.has("personal")) {
            			JSONObject personalAddress = obj.getJSONObject("personal");
            			if(personalAddress.hasAndNotNull("postal_code")) {
            				c.setPostalCodeHome(personalAddress.getString("postal_code"));
            			}
            			if(personalAddress.hasAndNotNull("street")) {
            				c.setStreetHome(personalAddress.getString("street"));
            			}
            			if(personalAddress.hasAndNotNull("city")) {
            				c.setCityHome(personalAddress.getString("city"));
            			}
            			if(personalAddress.hasAndNotNull("state")) {
            				c.setStateHome(personalAddress.getString("state"));
            			}

            		}

            		if (obj.has("business")) {
            			JSONObject businessAddress = obj.getJSONObject("business");
            			if(businessAddress.hasAndNotNull("postal_code")) {
            				c.setPostalCodeBusiness(businessAddress.getString("postal_code"));
            			}
            			if(businessAddress.hasAndNotNull("street")) {
            				c.setStreetBusiness(businessAddress.getString("street"));
            			}
            			if(businessAddress.hasAndNotNull("city")) {
            				c.setCityBusiness(businessAddress.getString("city"));
            			}
            			if(businessAddress.hasAndNotNull("state")) {
            				c.setStateBusiness(businessAddress.getString("state"));
            			}
            		}
            	}
            	// TODO: Picture?

            	contacts.add(c);
            }
        } catch (JSONException x) {
        	LOG.error(x);
        }

        return contacts;
    }

    /**
     * @param session 
     * @param secret
     * @param token
     * @throws OXException
     */
    private String useRefreshTokenToGetAccessToken(OAuthAccount account, Session session) throws OXException {
    	String callback = null;
    	try {
    		JSONObject metadata = new JSONObject(account.getSecret());
    		callback = metadata.getString("callback");
    	} catch (JSONException x) {
    		throw OAuthExceptionCodes.INVALID_ACCOUNT.create();
    	}
    	String accessToken = "";

    	try {
    		final HttpClient client = new HttpClient();
    		final PostMethod postMethod = new PostMethod("https://login.live.com/oauth20_token.srf?client_id=" + account.getMetaData().getAPIKey(session) + "&redirect_uri=" + URLEncoder.encode(callback, "UTF-8") + "&client_secret=" + URLEncoder.encode(account.getMetaData().getAPISecret(session), "UTF-8")+"&refresh_token=" + account.getToken() + "&grant_type=refresh_token");

    		RequestEntity requestEntity;
            requestEntity = new StringRequestEntity(postMethod.getQueryString(), "application/x-www-form-urlencoded", "UTF-8");
            postMethod.setRequestEntity(requestEntity);
            final int responseCode = client.executeMethod(postMethod);
            final String response = URLDecoder.decode(postMethod.getResponseBodyAsString(), "UTF-8");
            return new JSONObject(response).getString("access_token");
        } catch (final UnsupportedEncodingException e) {
            LOG.error(e);
        } catch (final HttpException e) {
            LOG.error(e);
        } catch (final IOException e) {
            LOG.error(e);
        } catch (JSONException e) {
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
