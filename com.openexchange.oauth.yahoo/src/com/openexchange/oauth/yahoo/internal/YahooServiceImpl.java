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

package com.openexchange.oauth.yahoo.internal;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
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
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.oauth.yahoo.YahooService;
import com.openexchange.oauth.yahoo.osgi.Services;
import com.openexchange.oauth.yahoo.osgi.YahooOAuthActivator;
import com.openexchange.session.Session;
import com.openexchange.threadpool.BoundedCompletionService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link YahooServiceImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class YahooServiceImpl implements YahooService {

    private static final String ALL_CONTACT_IDS_URL = "https://social.yahooapis.com/v1/user/GUID/contacts?format=json";

    private static final String SINGLE_CONTACT_URL = "https://social.yahooapis.com/v1/user/GUID/contact/CONTACT_ID?format=json";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(YahooServiceImpl.class);

    private final Pattern patternGuid;
    private final YahooOAuthActivator activator;

    public YahooServiceImpl(final YahooOAuthActivator activator) {
        super();
        this.activator = activator;
        patternGuid = Pattern.compile("<value>([^<]*)<");
    }

    @Override
    public List<Contact> getContacts(final Session session, final int user, final int contextId, final int accountId) throws OXException {
        OAuthAccess yahooAccess = getOAuthAccess(session, accountId);
        OAuthAccount account = yahooAccess.getOAuthAccount();

        final Token accessToken = new Token(account.getToken(), account.getSecret());
        return useAccessTokenToAccessData(accessToken, session);
    }

    private OAuthAccess getOAuthAccess(Session session, int accountId) throws OXException {
        OAuthAccessRegistryService service = Services.getService(OAuthAccessRegistryService.class);
        OAuthAccessRegistry oAuthAccessRegistry = service.get(API.YAHOO.getFullName());
        OAuthAccess oAuthAccess = oAuthAccessRegistry.get(session.getContextId(), session.getUserId());
        if (oAuthAccess == null) {
            OAuthAccess access = new YahooOAuthAccess(session, accountId);

            oAuthAccess = oAuthAccessRegistry.addIfAbsent(session.getContextId(), session.getUserId(), access);
            if (oAuthAccess == null) {
                access.initialize();
                oAuthAccess = access;
            }
        }
        return oAuthAccess;
    }

    private List<Contact> useAccessTokenToAccessData(final Token accessToken, Session session) throws OXException {
        final OAuthService service = new ServiceBuilder().provider(YahooApi2.class).apiKey(activator.getOAuthMetaData().getAPIKey(session)).apiSecret(activator.getOAuthMetaData().getAPISecret(session)).build();
        // Get the GUID of the current user from yahoo. This is needed for later requests
        final String guid;
        {
            OAuthRequest guidRequest = new OAuthRequest(Verb.GET, "https://social.yahooapis.com/v1/me/guid?format=xml");
            service.signRequest(accessToken, guidRequest);
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
            final Matcher matcher = patternGuid.matcher(guidResponse.getBody());
            guid = matcher.find() ? matcher.group(1) : "";
        }

        // Now get the ids of all the users contacts
        OAuthRequest request = new OAuthRequest(Verb.GET, ALL_CONTACT_IDS_URL.replace("GUID", guid));
        service.signRequest(accessToken, request);
        final Response response = request.send(YahooRequestTuner.getInstance());
        final String contentType = response.getHeader("Content-Type");
        if (null == contentType || false == contentType.toLowerCase().contains("application/json")) {
            throw OAuthExceptionCodes.NOT_A_VALID_RESPONSE.create();
        }
        request = null;
        try {
            final JSONObject allContactsWholeResponse = extractJson(response);
            if (!allContactsWholeResponse.hasAndNotNull("contacts")) {
                return Collections.emptyList();
            }
            final JSONObject contacts = allContactsWholeResponse.getJSONObject("contacts");
            if (!contacts.hasAndNotNull("contact")) {
                return Collections.emptyList();
            }
            final JSONArray allContactsArray = contacts.getJSONArray("contact");
            final CompletionService<Void> completionService = new BoundedCompletionService<Void>(ThreadPools.getThreadPool(), 10).setTrackable(true);
            int numTasks = 0;
            final int length = allContactsArray.length();
            final ConcurrentMap<Integer, Contact> contactMap = new ConcurrentHashMap<Integer, Contact>(length, 0.9f, 1);
            // get each contact with its own request
            for (int i = 0; i < length; i++) {
                final JSONObject entry = allContactsArray.getJSONObject(i);
                if (entry.hasAndNotNull("id")) {
                    final int index = i;
                    final Callable<Void> callable = new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            final String contactId = entry.getString("id");
                            final String singleContactUrl = SINGLE_CONTACT_URL.replace("GUID", guid).replace("CONTACT_ID", contactId);
                            // Request
                            final OAuthRequest singleContactRequest = new OAuthRequest(Verb.GET, singleContactUrl);
                            service.signRequest(accessToken, singleContactRequest);
                            final Response singleContactResponse = singleContactRequest.send(YahooRequestTuner.getInstance());
                            contactMap.put(Integer.valueOf(index), parseSingleContact(extractJson(singleContactResponse)));
                            return null;
                        }
                    };
                    completionService.submit(callable);
                    numTasks++;
                }
            }
            for (int i = 0; i < numTasks; i++) {
                completionService.take();
            }
            final List<Contact> contactList = new ArrayList<Contact>(length);
            for (int i = 0; i < length; i++) {
                final Contact contact = contactMap.get(Integer.valueOf(i));
                if (null != contact) {
                    contactList.add(contact);
                }
            }
            return contactList;
        } catch (final JSONException e) {
            LOG.error("", e);
        } catch (final OXException e) {
            LOG.error("", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("", e);
        } catch (final RuntimeException e) {
            LOG.error("", e);
        }
        return Collections.emptyList();
    }

    /**
     * Parses given contact.
     *
     * @param singleContact
     * @return
     */
    protected Contact parseSingleContact(final JSONObject all) {
        final Contact oxContact = new Contact();
        try {
            if (all.has("contact")) {
                final JSONObject contact = all.getJSONObject("contact");
                if (contact.has("fields")) {
                    final JSONArray fields = contact.getJSONArray("fields");
                    final int length = fields.length();
                    for (int i = 0; i < length; i++) {
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
                                        month = Integer.parseInt(value.getString("month")) - 1;
                                    }
                                    if (value.has("year")) {
                                        year = Integer.parseInt(value.getString("year")) - 1900;
                                    }
                                    if (date != 0 && month != 0) {
                                        oxContact.setBirthday(new Date(year, month, date));
                                    }
                                }
                            }

                            else if (type.equals("otherid")) {
                                if (field.has("value") && field.has("flags")) {
                                    final String kind = field.getString("flags");
                                    final Pattern pattern = Pattern.compile("\\[\"([^\"]*)\"\\]");
                                    final Matcher matcher = pattern.matcher(kind);
                                    if (matcher.find()) {
                                        final String service = matcher.group(1);
                                        oxContact.setInstantMessenger1(field.getString("value") + " (" + service + ")");
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
            LOG.error("", e);
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
            LOG.error("", e);
        }
        return displayName;
    }

    /** Extracts JSON out of given response */
    protected JSONObject extractJson(final Response response) throws OXException {
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
