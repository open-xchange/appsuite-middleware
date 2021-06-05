/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.oauth.yahoo.internal;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccountDeleteListener;
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
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class YahooServiceImpl implements YahooService, OAuthAccountDeleteListener {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(YahooServiceImpl.class);

    private final ServiceLookup services;

    /**
     * Initialises a new {@link YahooServiceImpl}.
     */
    public YahooServiceImpl(ServiceLookup serviceLookup) {
        super();
        this.services = serviceLookup;
    }

    @Override
    public void onBeforeOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        // Nothing
    }

    @Override
    public void onAfterOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        OAuthAccessRegistryService registryService = services.getService(OAuthAccessRegistryService.class);
        OAuthAccessRegistry registry = registryService.get(KnownApi.YAHOO.getServiceId());
        OAuthAccess oAuthAccess = registry.get(cid, user, id);
        if (oAuthAccess == null || oAuthAccess.getAccountId() != id) {
            return;
        }

        boolean purged = registry.purgeUserAccess(cid, user, id);
        if (purged) {
            LOGGER.info("Removed Yahoo! OAuth access from registry for the deleted OAuth account with id '{}' for user '{}' in context '{}'", I(id), I(user), I(cid));
        }
    }

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

    @Override
    public String getAccountDisplayName(Session session, int user, int contextId, int accountId) {
        String displayName = "";
        try {
            OAuthAccess yahooAccess = getOAuthAccess(session, accountId);
            YahooClient yc = (YahooClient) yahooAccess.getClient().client;
            displayName = yc.getDisplayName();
        } catch (OXException e) {
            LOGGER.error("{}", e.getMessage(), e);
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
        OAuthAccessRegistry oAuthAccessRegistry = service.get(KnownApi.YAHOO.getServiceId());
        OAuthAccess oAuthAccess = oAuthAccessRegistry.get(session.getContextId(), session.getUserId(), accountId);
        if (oAuthAccess == null) {
            OAuthAccess access = new YahooOAuthAccess(session, accountId);

            oAuthAccess = oAuthAccessRegistry.addIfAbsent(session.getContextId(), session.getUserId(), accountId, access);
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
                                    Calendar c = Calendar.getInstance();
                                    c.set(year, month, date);
                                    oxContact.setBirthday(new Date(c.getTimeInMillis()));
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
        } catch (JSONException e) {
            LOGGER.error("", e);
        }
        return oxContact;
    }

}
