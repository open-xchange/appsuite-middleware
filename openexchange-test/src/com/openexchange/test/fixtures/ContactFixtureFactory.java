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

package com.openexchange.test.fixtures;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.fixtures.transformators.BooleanTransformator;
import com.openexchange.test.fixtures.transformators.DistributionListTransformator;
import com.openexchange.test.fixtures.transformators.FileToByteArrayTransformator;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Markus Wagner <markus.wagner@open-xchange.com>
 * @author Martin Braun <martin.braun@open-xchange.com>
 */
public class ContactFixtureFactory implements FixtureFactory<Contact> {

    private final FixtureLoader fixtureLoader;

    public ContactFixtureFactory(FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Fixtures<Contact> createFixture(final Map<String, Map<String, String>> entries) {
        return new ContactFixtures(entries, fixtureLoader);
    }

    private class ContactFixtures extends DefaultFixtures<Contact> implements Fixtures<Contact> {

        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<Contact>> contacts = new HashMap<String, Fixture<Contact>>();

        public ContactFixtures(final Map<String, Map<String, String>> entries, FixtureLoader fixtureLoader) {
            super(Contact.class, entries, fixtureLoader);
            this.entries = entries;
            addTransformator(new DistributionListTransformator(fixtureLoader), "distribution_list");
            addTransformator(new BooleanTransformator(), "private_flag");
            addTransformator(new FileToByteArrayTransformator(fixtureLoader), "image1");
        }

        @Override
        public Fixture<Contact> getEntry(final String entryName) throws OXException {
            if (contacts.containsKey(entryName)) {
                return contacts.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }
            defaults(values);
            final Contact contact = new Contact();
            apply(contact, values);

            final Fixture<Contact> fixture = new Fixture<Contact>(contact, values.keySet().toArray(new String[values.size()]), values) {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public Comparator getComparator(final String field) {
                    if ("birthday".equals(field) || "anniversary".equals(field)) {
                        return new DayOnlyDateComparator();
                    }
                    return super.getComparator(field);
                }
            };

            contacts.put(entryName, fixture);
            return fixture;
        }

        private void defaults(final Map<String, String> values) {
            if (false == values.containsKey("display_name")) {
                final String surName = values.containsKey("sur_name") ? values.get("sur_name").toString() : null;
                final String givenName = values.containsKey("given_name") ? values.get("given_name").toString() : null;
                if (null != surName) {
                    values.put("display_name", null == givenName ? surName : String.format("%s, %s", surName, givenName));
                } else if (null != givenName) {
                    values.put("display_name", givenName);
                }
            }
        }
    }
}
