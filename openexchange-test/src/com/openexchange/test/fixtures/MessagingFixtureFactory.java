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
import java.util.UUID;
import com.openexchange.exception.OXException;

/**
 * @author Martin Braun <martin.braun@open-xchange.com>
 */
public class MessagingFixtureFactory implements FixtureFactory<Messaging> {

    private final FixtureLoader fixtureLoader;

    public MessagingFixtureFactory(FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Fixtures<Messaging> createFixture(final Map<String, Map<String, String>> entries) {
        return new MessagingFixtures(entries, fixtureLoader);
    }

    private class MessagingFixtures extends DefaultFixtures<Messaging> implements Fixtures<Messaging> {

        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<Messaging>> messagings = new HashMap<String, Fixture<Messaging>>();

        public MessagingFixtures(final Map<String, Map<String, String>> entries, FixtureLoader fixtureLoader) {
            super(Messaging.class, entries, fixtureLoader);
            this.entries = entries;
        }

        @Override
        public Fixture<Messaging> getEntry(final String entryName) throws OXException {
            if (messagings.containsKey(entryName)) {
                return messagings.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }

            defaults(values);

            final Messaging messaging = new Messaging();

            Map<String, Object> config = new HashMap<String, Object>();

            if (values.containsKey("type")) {
                config.put("type", values.get("type"));
                values.remove("type");
            }
            if (values.containsKey("name")) {
                config.put("name", values.get("name"));
                values.remove("name");
            }
            if (values.containsKey("url")) {
                config.put("url", values.get("url"));
                values.remove("url");
            }
            if (values.containsKey("login")) {
                config.put("login", values.get("login"));
                values.remove("login");
            }
            if (values.containsKey("password")) {
                config.put("password", values.get("password"));
                values.remove("password");
            }
            if (values.containsKey("message")) {
                config.put("message", values.get("message") + " - " + UUID.randomUUID().toString());
                values.remove("message");
            }

            messaging.setConfiguration(config);

            apply(messaging, values);

            final Fixture<Messaging> fixture = new Fixture<Messaging>(messaging, values.keySet().toArray(new String[values.size()]), values);

            messagings.put(entryName, fixture);
            return fixture;
        }

        private void defaults(final Map<String, String> values) {
            if (false == values.containsKey("displayName")) {
                values.put("displayName", values.get("login"));
            }
        }
    }
}
