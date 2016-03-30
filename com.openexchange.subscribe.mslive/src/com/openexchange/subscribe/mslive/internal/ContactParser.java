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

package com.openexchange.subscribe.mslive.internal;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.groupware.container.Contact;

/**
 * {@link ContactParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ContactParser {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactParser.class);

    /**
     * Initializes a new {@link ContactParser}.
     */
    public ContactParser() {
        super();
    }

    /**
     * Parse to contact object
     * 
     * @param response
     * @return
     */
    public List<Contact> parse(final JSONObject response) {
        final List<Contact> contacts = new LinkedList<Contact>();
        try {
            JSONArray arr = response.getJSONArray("data");
            for (int i = 0, size = arr.length(); i < size; i++) {
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
                    List<String> mailAddresses = new LinkedList<String>();
                    JSONObject emails = cObj.getJSONObject("emails");
                    for (String key : new String[] { "preferred", "account", "other", "personal", "business" }) {
                        if (emails.hasAndNotNull(key)) {
                            String address = emails.optString(key);
                            if (!mailAddresses.contains(address)) {
                                mailAddresses.add(address);
                            }
                        }
                    }

                    int counter = 0;
                    for (String mailAddress : mailAddresses) {
                        switch (counter) {
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
                        if (personalAddress.hasAndNotNull("postal_code")) {
                            c.setPostalCodeHome(personalAddress.getString("postal_code"));
                        }
                        if (personalAddress.hasAndNotNull("street")) {
                            c.setStreetHome(personalAddress.getString("street"));
                        }
                        if (personalAddress.hasAndNotNull("city")) {
                            c.setCityHome(personalAddress.getString("city"));
                        }
                        if (personalAddress.hasAndNotNull("state")) {
                            c.setStateHome(personalAddress.getString("state"));
                        }

                    }

                    if (obj.has("business")) {
                        JSONObject businessAddress = obj.getJSONObject("business");
                        if (businessAddress.hasAndNotNull("postal_code")) {
                            c.setPostalCodeBusiness(businessAddress.getString("postal_code"));
                        }
                        if (businessAddress.hasAndNotNull("street")) {
                            c.setStreetBusiness(businessAddress.getString("street"));
                        }
                        if (businessAddress.hasAndNotNull("city")) {
                            c.setCityBusiness(businessAddress.getString("city"));
                        }
                        if (businessAddress.hasAndNotNull("state")) {
                            c.setStateBusiness(businessAddress.getString("state"));
                        }
                    }
                }
                // TODO: Picture?

                contacts.add(c);
            }
        } catch (JSONException x) {
            LOG.error("", x);
        }

        return contacts;
    }
}
