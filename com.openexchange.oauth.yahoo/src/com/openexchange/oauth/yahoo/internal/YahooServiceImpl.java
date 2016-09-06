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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.oauth.yahoo.YahooService;
import com.openexchange.oauth.yahoo.access.YahooClient;
import com.openexchange.oauth.yahoo.access.YahooOAuthAccess;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.BoundedCompletionService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link YahooServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class YahooServiceImpl implements YahooService, OAuthAccountDeleteListener {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(YahooServiceImpl.class);

    private ServiceLookup services;

    /**
     * Initialises a new {@link YahooServiceImpl}.
     */
    public YahooServiceImpl(ServiceLookup serviceLookup) {
        super();
        this.services = serviceLookup;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthAccountDeleteListener#onBeforeOAuthAccountDeletion(int, java.util.Map, int, int, java.sql.Connection)
     */
    @Override
    public void onBeforeOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthAccountDeleteListener#onAfterOAuthAccountDeletion(int, java.util.Map, int, int, java.sql.Connection)
     */
    @Override
    public void onAfterOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.yahoo.YahooService#getContacts(com.openexchange.session.Session, int, int, int)
     */
    @Override
    public List<Contact> getContacts(Session session, int user, int contextId, int accountId) throws OXException {
        OAuthAccess yahooAccess = getOAuthAccess(session, accountId);

        YahooClient yc = (YahooClient) yahooAccess.getClient().client;
        JSONObject contactsJson = yc.getContacts();

        try {
            if (!contactsJson.hasAndNotNull("contacts")) {
                return Collections.emptyList();
            }
            final JSONObject contacts = contactsJson.getJSONObject("contacts");
            if (!contacts.hasAndNotNull("contact")) {
                return Collections.emptyList();
            }
            final JSONArray allContactsArray = contacts.getJSONArray("contact");

            final CompletionService<Void> completionService = new BoundedCompletionService<Void>(ThreadPools.getThreadPool(), 10).setTrackable(true);
            int numTasks = 0;
            final int length = allContactsArray.length();
            final ConcurrentMap<Integer, Contact> contactMap = new ConcurrentHashMap<Integer, Contact>(length, 0.9f, 1);
            for (int i = 0; i < length; i++) {
                final JSONObject entry = allContactsArray.getJSONObject(i);
                if (entry.hasAndNotNull("id")) {
                    final int index = i;
                    final Callable<Void> callable = new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            contactMap.put(Integer.valueOf(index), parseSingleContact(entry));
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
        } catch (JSONException e) {
            LOGGER.error("{}", e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("{}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.yahoo.YahooService#getAccountDisplayName(com.openexchange.session.Session, int, int, int)
     */
    @Override
    public String getAccountDisplayName(Session session, int user, int contextId, int accountId) {
        String displayName = "";
        try {
            final OAuthService oAuthService = services.getService(OAuthService.class);
            final OAuthAccount account = oAuthService.getAccount(accountId, session, user, contextId);
            displayName = account.getDisplayName();
        } catch (final OXException e) {
            LOGGER.error("", e);
        }
        return displayName;
    }

    /**
     * Gets the {@link OAuthAccess} for the specified account identifier
     * 
     * @param session The {@link Session}
     * @param accountId The account identifier
     * @return The {@link OAuthAccess}
     * @throws OXException
     */
    private OAuthAccess getOAuthAccess(Session session, int accountId) throws OXException {
        OAuthAccessRegistryService service = services.getService(OAuthAccessRegistryService.class);
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

    /**
     * Parses given contact.
     *
     * @param singleContact
     * @return
     */
    protected Contact parseSingleContact(final JSONObject contact) {
        final Contact oxContact = new Contact();
        try {
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
        } catch (final JSONException e) {
            LOGGER.error("", e);
        }
        return oxContact;
    }

}
