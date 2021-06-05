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

import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CredentialFixtureFactory implements FixtureFactory<SimpleCredentials> {

    private final TestUserConfigFactory userConfigFactory;
    private final ContactFinder contactFinder;
    private final FixtureLoader fixtureLoader;

    public CredentialFixtureFactory(TestUserConfigFactory userConfigFactory, ContactFinder contactFinder, FixtureLoader fixtureLoader) {
        super();
        this.userConfigFactory = userConfigFactory;
        this.contactFinder = contactFinder;
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Fixtures<SimpleCredentials> createFixture(final Map<String, Map<String, String>> entries) {
        return new CredentialFixtures(entries, userConfigFactory, contactFinder, fixtureLoader);
    }

    private class CredentialFixtures extends DefaultFixtures<SimpleCredentials> implements Fixtures<SimpleCredentials> {

        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<SimpleCredentials>> credentialMap = new HashMap<String, Fixture<SimpleCredentials>>();
        @SuppressWarnings("hiding")
        private final TestUserConfigFactory userConfigFactory;
        @SuppressWarnings("hiding")
        private final ContactFinder contactFinder;

        public CredentialFixtures(final Map<String, Map<String, String>> values, TestUserConfigFactory userConfigFactory, ContactFinder contactFinder, FixtureLoader fixtureLoader) {
            super(SimpleCredentials.class, values, fixtureLoader);
            this.entries = values;
            this.userConfigFactory = userConfigFactory;
            this.contactFinder = contactFinder;
        }

        @Override
        public Fixture<SimpleCredentials> getEntry(final String entryName) throws OXException {
            if (credentialMap.containsKey(entryName)) {
                return credentialMap.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }
            final SimpleCredentials credentials = new SimpleCredentials(userConfigFactory, contactFinder);
            apply(credentials, values);
            final Fixture<SimpleCredentials> fixture = new Fixture<SimpleCredentials>(credentials, values.keySet().toArray(new String[values.size()]), values);
            credentialMap.put(entryName, fixture);
            return fixture;
        }
    }
}
