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

package com.openexchange.oauth.yahoo.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.YahooApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.yahoo.YahooService;
import com.openexchange.oauth.yahoo.osgi.YahooOAuthActivator;
import com.openexchange.session.Session;

/**
 * {@link YahooServiceImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class YahooServiceImpl implements YahooService {

    private final YahooOAuthActivator activator;

    private static final String ALL_CONTACT_IDS_URL = "http://social.yahooapis.com/v1/user/GUID/contacts?format=json";

    private static final String SINGLE_CONTACT_URL = "http://social.yahooapis.com/v1/user/GUID/contact/CONTACT_ID?format=json";

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(YahooServiceImpl.class));

    public YahooServiceImpl(final YahooOAuthActivator activator) {
        this.activator = activator;
    }

    /* (non-Javadoc)
     * @see com.openexchange.oauth.yahoo.YahooService#getContacts(java.lang.String, int, int, int)
     */
    @Override
    public List<Contact> getContacts(final Session session, final int user, final int contextId, final int accountId) {
        List<Contact> contacts = new ArrayList<Contact>();
        OAuthAccount account = null;


            final com.openexchange.oauth.OAuthService oAuthService = activator.getOauthService();
            try {
                account = oAuthService.getAccount(accountId, session, user, contextId);
            } catch (final OXException e) {
                LOG.error(e);
            }
            final Token accessToken = new Token(account.getToken(), account.getSecret());
            contacts = useAccessTokenToAccessData(accessToken);

        return contacts;
    }

    private List<Contact> useAccessTokenToAccessData(final Token accessToken) {
        final List<Contact> contactList = new ArrayList<Contact>();
        final OAuthService service = new ServiceBuilder().provider(YahooApi.class).apiKey(activator.getOAuthMetaData().getAPIKey()).apiSecret(
            activator.getOAuthMetaData().getAPISecret()).build();
        // Get the GUID of the current user from yahoo. This is needed for later requests
        final OAuthRequest request1 = new OAuthRequest(Verb.GET, "http://social.yahooapis.com/v1/me/guid?format=xml");
        service.signRequest(accessToken, request1);
        final Response response1 = request1.send();


        // Extract the Users ID from a response looking like this: <value>ANZAPAEE55TMMWPLYXQCJO7BAM<
        final Pattern pattern = Pattern.compile("<value>([^<]*)<");
        final Matcher matcher = pattern.matcher(response1.getBody());
        String guid = "";
        if (matcher.find()) {
            guid = matcher.group(1);
        }

        // Now get the ids of all the users contacts
        final String resource = ALL_CONTACT_IDS_URL.replace("GUID", guid);
        final OAuthRequest request = new OAuthRequest(Verb.GET, resource);
        service.signRequest(accessToken, request);
        final Response response = request.send();

        try {
            final JSONObject allContactsWholeResponse = new JSONObject(response.getBody());
            if (allContactsWholeResponse.has("contacts")) {
                final JSONObject contacts = (JSONObject) allContactsWholeResponse.get("contacts");
                if (contacts.has("contact")) {
                    final JSONArray allContactsArray = (JSONArray) contacts.get("contact");

                    // get each contact with its own request
                    for (int i = 0; i < allContactsArray.length(); i++) {
                        final JSONObject entry = allContactsArray.getJSONObject(i);
                        if (entry.has("id")) {
                            final String contactId = entry.getString("id");
                            final String singleContactUrl = SINGLE_CONTACT_URL.replace("GUID", guid).replace("CONTACT_ID", contactId);
                            final OAuthRequest singleContactRequest = new OAuthRequest(Verb.GET, singleContactUrl);
                            service.signRequest(accessToken, singleContactRequest);
                            final Response singleContactResponse = singleContactRequest.send();
                            contactList.add(parseSingleContact(singleContactResponse.getBody()));

                        }
                    }
                }
            }
        } catch (final JSONException e) {
            LOG.error(e);
        }

        return contactList;
    }

    private Contact parseSingleContact(final String singleContact) {
        final Contact oxContact = new Contact();
        try {
            final JSONObject all = new JSONObject(singleContact);
            if (all.has("contact")) {
                final JSONObject contact = all.getJSONObject("contact");
                if (contact.has("fields")) {
                    final JSONArray fields = contact.getJSONArray("fields");
                    for (int i = 0; i < fields.length(); i++) {
                        final JSONObject field = fields.getJSONObject(i);
                        if (field.has("type")) {
                            final String type = field.getString("type");

                            if (type.equals("name")) {
                                if (field.has("value")) {
                                    final JSONObject value = field.getJSONObject("value");
                                    if (value.has("givenName")) {
                                        oxContact.setGivenName(value.getString("givenName"));
                                    }

                                    if (value.has("familyName")) {
                                        oxContact.setSurName(value.getString("familyName"));
                                    }
                                    if (value.has("prefix") && !value.get("prefix").equals("")) {
                                        oxContact.setTitle(value.getString("prefix"));
                                    }
                                    if (value.has("suffix") && !value.get("suffix").equals("")) {
                                        oxContact.setSuffix(value.getString("suffix"));
                                    }
                                    if (value.has("middleName") && !value.get("middleName").equals("")) {
                                        oxContact.setMiddleName(value.getString("middleName"));
                                    }
                                }
                            }

                            else if (type.equals("email")) {
                                if (field.has("value")) {
                                    oxContact.setEmail1(field.getString("value"));
                                }
                            }

                            else if (type.equals("phone")) {
                                if (field.has("flags") && field.has("value")) {
                                    final String kind = field.getString("flags");
                                    if (kind.equals("[\"WORK\"]")) {
                                        oxContact.setTelephoneBusiness1(field.getString("value"));
                                    } else if (kind.equals("[\"HOME\"]")) {
                                        oxContact.setTelephoneHome1(field.getString("value"));
                                    } else if (kind.equals("[\"MOBILE\"]")) {
                                        oxContact.setCellularTelephone1((field.getString("value")));
                                    }
                                }
                            }

                            else if (type.equals("company")) {
                                if (field.has("value")) {
                                    oxContact.setCompany(field.getString("value"));
                                }
                            }

                            else if (type.equals("jobTitle")) {
                                if (field.has("value")) {
                                    oxContact.setPosition(field.getString("value"));
                                }
                            }

                            else if (type.equals("notes")) {
                                if (field.has("value")) {
                                    oxContact.setNote(field.getString("value"));
                                }
                            }

                            else if (type.equals("birthday")) {
                                int year = 0;
                                int month = 0;
                                int date = 0;
                                if (field.has("value")) {
                                    final JSONObject value = field.getJSONObject("value");
                                    if (value.has("day")) {
                                        date = Integer.parseInt(value.getString("day"));
                                    }
                                    if (value.has("day")) {
                                        date = Integer.parseInt(value.getString("day"));
                                    }
                                    if (value.has("month")) {
                                        month = Integer.parseInt(value.getString("month")) -1;
                                    }
                                    if (value.has("year")) {
                                        year = Integer.parseInt(value.getString("year")) - 1900;
                                    }
                                    if (date != 0 && month != 0) {
                                        oxContact.setBirthday(new Date(year, month, date));
                                    }
                                }
                            }

                            else if (type.equals("otherid")){
                                if (field.has("value") && field.has("flags")){
                                    final String kind = field.getString("flags");
                                    final Pattern pattern = Pattern.compile("\\[\"([^\"]*)\"\\]");
                                    final Matcher matcher = pattern.matcher(kind);
                                    if (matcher.find()){
                                        final String service = matcher.group(1);
                                        oxContact.setInstantMessenger1(field.getString("value") + " ("+service+")");
                                    }
                                }
                            }

                            else if (type.equals("address")) {
                                if (field.has("flags")) {
                                    final String kind = field.getString("flags");
                                    final JSONObject address = field.getJSONObject("value");
                                    if (kind.equals("[\"WORK\"]")) {
                                        if (address.has("street")) {
                                            oxContact.setStreetBusiness(address.getString("street"));
                                        }
                                        if (address.has("postalCode")) {
                                            oxContact.setPostalCodeBusiness(address.getString("postalCode"));
                                        }
                                        if (address.has("stateOrProvince")) {
                                            oxContact.setStateBusiness(address.getString("stateOrProvince"));
                                        }
                                        if (address.has("country")) {
                                            oxContact.setCountryBusiness(address.getString("country"));
                                        }
                                    } else if (kind.equals("[\"HOME\"]")) {
                                        if (address.has("street")) {
                                            oxContact.setStreetHome(address.getString("street"));
                                        }
                                        if (address.has("postalCode")) {
                                            oxContact.setPostalCodeHome(address.getString("postalCode"));
                                        }
                                        if (address.has("stateOrProvince")) {
                                            oxContact.setStateHome(address.getString("stateOrProvince"));
                                        }
                                        if (address.has("country")) {
                                            oxContact.setCountryHome(address.getString("country"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (final JSONException e) {
            LOG.error(e);
        }
        return oxContact;
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
