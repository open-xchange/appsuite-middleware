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

import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.test.fixtures.FixtureLoader;
import com.openexchange.test.fixtures.SimpleCredentials;

/**
 * Transforms strings of the kind "users:user_a",contacts:my_contact,custom:john doe
 * <john.doe@example.invalid>" into a distributionlist contact. Note that referenced
 * contacts should have an e-mail address defined.
 *
 * @author tfriedrich
 */
public class DistributionListTransformator implements Transformator {

    public DistributionListTransformator(final FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    private final FixtureLoader fixtureLoader;

    @Override
    public Object transform(final String value) throws OXException {
        if (null == value || 1 > value.length()) {
            return null;
        }
        String fixtureName = "users";
        String fixtureEntry = "";
        final String[] splitted = value.split(",");
        final List<DistributionListEntryObject> distributionListEntries = new ArrayList<DistributionListEntryObject>(splitted.length);
        for (int i = 0; i < splitted.length; i++) {
            final int idx = splitted[i].indexOf(':');
            if (0 < idx && splitted[i].length() > idx) {
                fixtureName = splitted[i].substring(0, idx);
                fixtureEntry = splitted[i].substring(idx + 1);
            } else {
                fixtureEntry = splitted[i];
            }
            distributionListEntries.add(getDistributionListEntry(fixtureName, fixtureEntry));
        }
        return distributionListEntries.toArray(new DistributionListEntryObject[distributionListEntries.size()]);
    }

    private DistributionListEntryObject getDistributionListEntry(final String fixtureName, final String fixtureEntry) throws OXException {
        if ("users".equals(fixtureName)) {
            return getUserDistributionListEntry(fixtureName, fixtureEntry);
        } else if ("contacts".equals(fixtureName)) {
            return getContactDistributionListEntry(fixtureName, fixtureEntry);
        } else if ("custom".equals(fixtureName)) {
            return getCustomDistributionListEntry(fixtureEntry);
        } else {
            throw OXException.general("Unable to convert " + fixtureName + ":" + fixtureEntry + " into a distribution list entry.");
        }
    }

    private DistributionListEntryObject getContactDistributionListEntry(final String fixtureName, final String fixtureEntry) throws OXException {
        final Contact contact = fixtureLoader.getFixtures(fixtureName, Contact.class).getEntry(fixtureEntry).getEntry();
        String email = null;
        if (contact.containsEmail1()) {
            email = contact.getEmail1();
        } else if (contact.containsEmail2()) {
            email = contact.getEmail2();
        } else if (contact.containsEmail3()) {
            email = contact.getEmail3();
        }
        if (null == email) {
            throw OXException.general("Contacts must contain an email address");
        }
        return new DistributionListEntryObject(contact.getDisplayName(), email, DistributionListEntryObject.INDEPENDENT);
    }

    private DistributionListEntryObject getUserDistributionListEntry(final String fixtureName, final String fixtureEntry) throws OXException {
        final Contact user = fixtureLoader.getFixtures(fixtureName, SimpleCredentials.class).getEntry(fixtureEntry).getEntry().asContact();
        final DistributionListEntryObject entry = new DistributionListEntryObject();
        entry.setDisplayname(user.getDisplayName());
        entry.setEmailaddress(user.getEmail1());
        entry.setEntryID(DistributionListEntryObject.INDEPENDENT);
        return entry;
        //		return new DistributionListEntryObject(user.getDisplayName(), user.getEmail1(), DistributionListEntryObject.INDEPENDENT);
    }

    private DistributionListEntryObject getCustomDistributionListEntry(final String fixtureEntry) throws OXException {
        InternetAddress address = null;
        try {
            final InternetAddress[] addresses = InternetAddress.parse(fixtureEntry);
            if (null == addresses || 1 > addresses.length || null == addresses[0]) {
                throw OXException.general("unable to parse custom distributionlist entry from " + fixtureEntry);
            }
            address = addresses[0];
        } catch (AddressException e) {
            throw OXException.general("unable to parse custom distributionlist entry from " + fixtureEntry + ": " + e.getMessage());
        }
        return new DistributionListEntryObject(address.getPersonal(), IDNA.toIDN(address.getAddress()), DistributionListEntryObject.INDEPENDENT);

    }
}
