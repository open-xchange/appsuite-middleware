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
import com.openexchange.subscribe.Subscription;

/**
 * @author Martin Braun <martin.braun@open-xchange.com>
 */
public class SubscriptionFixtureFactory implements FixtureFactory<Subscription> {

    private final FixtureLoader fixtureLoader;

    public SubscriptionFixtureFactory(FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Fixtures<Subscription> createFixture(final Map<String, Map<String, String>> entries) {
        return new SubscriptionFixtures(entries, fixtureLoader);
    }

    private class SubscriptionFixtures extends DefaultFixtures<Subscription> implements Fixtures<Subscription> {

        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<Subscription>> subscriptions = new HashMap<String, Fixture<Subscription>>();

        public SubscriptionFixtures(final Map<String, Map<String, String>> entries, FixtureLoader fixtureLoader) {
            super(Subscription.class, entries, fixtureLoader);
            this.entries = entries;
        }

        @Override
        public Fixture<Subscription> getEntry(final String entryName) throws OXException {
            if (subscriptions.containsKey(entryName)) {
                return subscriptions.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }

            defaults(values);

            final Subscription subscription = new Subscription();

            Map<String, Object> config = new HashMap<String, Object>();

            if (values.containsKey("source")) {
                config.put("source", values.get("source"));
                values.remove("source");
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

            subscription.setConfiguration(config);

            apply(subscription, values);

            final Fixture<Subscription> fixture = new Fixture<Subscription>(subscription, values.keySet().toArray(new String[values.size()]), values);

            subscriptions.put(entryName, fixture);
            return fixture;
        }

        private void defaults(final Map<String, String> values) {
            if (false == values.containsKey("displayName")) {
                values.put("displayName", values.get("login"));
            }

            if (false == values.containsKey("secret")) {
                values.put("secret", values.get("false"));
            }
        }
    }
}
