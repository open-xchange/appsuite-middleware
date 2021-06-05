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

package com.openexchange.test.fixtures.transformators;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.test.fixtures.FixtureLoader;
import com.openexchange.test.fixtures.SimpleCredentials;

/**
 * Transforms strings of the kind "users:user_a" or "contacts:my_contact" into an internet
 * address (to be used as email address). If the suffix "(plain)" is used at a fixture,
 * only the plain e-mail address is added to the internet address object.
 *
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 * @author Martin Braun <martin.braun@open-xchange.com>
 */
public class InternetAddressTransformator implements Transformator {

    private final FixtureLoader fixtureLoader;

    public InternetAddressTransformator(final FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Object transform(final String value) throws OXException {
        if (null == value || 1 > value.length()) {
            return null;
        }

        if (false == value.contains(":")) {
            return getAddress(value);		// try to parseICal whole string
        }
        String fixtureName = "users";
        String fixtureEntry = "";
        final String[] splitted = value.split(",");

        final Pattern patFlags = Pattern.compile("\\((\\w+)\\)");
        final String plainMarker = "(plain)";
        final boolean[] plainFLags = new boolean[splitted.length];

        for (int i = 0; i < splitted.length; i++) {
            final Matcher mFlag = patFlags.matcher(splitted[i]);
            if (mFlag.find() && mFlag.group().contains(plainMarker)) {
                plainFLags[i] = true;
                splitted[i] = splitted[i].replace(plainMarker, "").replace(" ", "");
            }
        }

        final List<InternetAddress> addresses = new ArrayList<InternetAddress>(splitted.length);
        for (int i = 0; i < splitted.length; i++) {
            final int idx = splitted[i].indexOf(':');
            if (0 < idx && splitted[i].length() > idx) {
                fixtureName = splitted[i].substring(0, idx);
                fixtureEntry = splitted[i].substring(idx + 1);
            } else {
                fixtureEntry = splitted[i];
            }
            addresses.addAll(getAddresses(fixtureName, fixtureEntry, plainFLags[i]));
        }
        return addresses.toArray(new InternetAddress[addresses.size()]);
    }

    private InternetAddress getAddress(final String address) throws OXException {
        try {
            return InternetAddress.parse(address)[0];
        } catch (Exception e) {
            throw OXException.general("Unable to parse e-mail address from " + address);
        }
    }

    private List<InternetAddress> getAddresses(final String fixtureName, final String fixtureEntry, final boolean plainFlags) throws OXException {
        final Contact contact = ("users".equals(fixtureName)) ? fixtureLoader.getFixtures(fixtureName, SimpleCredentials.class).getEntry(fixtureEntry).getEntry().asContact() //users
            : fixtureLoader.getFixtures(fixtureName, Contact.class).getEntry(fixtureEntry).getEntry(); //contacts
        if (null == contact) {
            throw OXException.general("Unable to convert " + fixtureName + ":" + fixtureEntry + " into a contact.");
        }
        return getAddresses(contact, plainFlags);
    }

    private List<InternetAddress> getAddresses(final Contact contact, final boolean plainFlags) throws OXException {
        final List<InternetAddress> addresses = new ArrayList<InternetAddress>();
        if (contact.containsDistributionLists()) {
            final DistributionListEntryObject[] entries = contact.getDistributionList();
            if (null != entries && 0 < entries.length) {
                for (final DistributionListEntryObject entry : entries) {
                    if (null != entry && entry.containsEmailaddress()) {
                        try {
                            if (entry.containsDisplayname() && null != entry.getDisplayname()) {
                                addresses.add(new InternetAddress(entry.getEmailaddress(), entry.getDisplayname()));
                            } else {
                                addresses.add(new InternetAddress(entry.getEmailaddress()));
                            }
                        } catch (AddressException e) {
                            throw new OXException(e);
                        } catch (UnsupportedEncodingException e) {
                            throw new OXException(e);
                        }
                    }
                }
            }
        } else {
            try {
                if (contact.containsEmail1()) {
                    if (false == plainFlags) {
                        addresses.add(new InternetAddress(contact.getEmail1(), contact.getDisplayName()));
                    } else {
                        addresses.add(new InternetAddress(contact.getEmail1()));
                    }

                } else if (contact.containsEmail2()) {
                    if (false == plainFlags) {
                        addresses.add(new InternetAddress(contact.getEmail2(), contact.getDisplayName()));
                    } else {
                        addresses.add(new InternetAddress(contact.getEmail2()));
                    }
                } else if (contact.containsEmail3()) {
                    if (false == plainFlags) {
                        addresses.add(new InternetAddress(contact.getEmail3(), contact.getDisplayName()));
                    } else {
                        addresses.add(new InternetAddress(contact.getEmail3()));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                throw new OXException(e);
            } catch (AddressException e) {
                e.printStackTrace();
            }
        }
        if (1 > addresses.size()) {
            throw OXException.general("no e-mail addresses found in contact");
        }
        return addresses;
    }
}
