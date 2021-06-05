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
import com.openexchange.test.fixtures.transformators.DocumentsTransformator;
import com.openexchange.test.fixtures.transformators.UserIdTransformator;

/**
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class InfoItemFixtureFactory implements FixtureFactory<InfoItem> {

    private final FixtureLoader fixtureLoader;

    public InfoItemFixtureFactory(FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Fixtures<InfoItem> createFixture(final Map<String, Map<String, String>> entries) {
        return new InfoItemFixtures(entries, fixtureLoader);
    }

    private class InfoItemFixtures extends DefaultFixtures<InfoItem> implements Fixtures<InfoItem> {

        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<InfoItem>> knownInfoitems = new HashMap<String, Fixture<InfoItem>>();

        public InfoItemFixtures(final Map<String, Map<String, String>> values, FixtureLoader fixtureLoader) {
            super(InfoItem.class, values, fixtureLoader);
            this.entries = values;
            super.addTransformator(new DocumentsTransformator(fixtureLoader), "versions");
            super.addTransformator(new UserIdTransformator(fixtureLoader), "created_by");
        }

        @Override
        public Fixture<InfoItem> getEntry(final String entryName) throws OXException {
            if (knownInfoitems.containsKey(entryName)) {
                return knownInfoitems.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }
            final InfoItem item = new InfoItem();
            apply(item, values);

            if (item.containsVersions()) {
                for (final Document version : item.getVersions()) {
                    version.setParent(item);
                }
            }

            final Fixture<InfoItem> fixture = new Fixture<InfoItem>(item, values.keySet().toArray(new String[values.size()]), values);
            knownInfoitems.put(entryName, fixture);
            return fixture;
        }
    }
}
