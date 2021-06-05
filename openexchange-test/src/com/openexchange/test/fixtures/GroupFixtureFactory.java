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
import com.openexchange.group.Group;

/**
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class GroupFixtureFactory implements FixtureFactory<Group> {

    private final FixtureLoader fixtureLoader;

    public GroupFixtureFactory(FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Fixtures<Group> createFixture(final Map<String, Map<String, String>> entries) {
        return new GroupFixtures(entries, fixtureLoader);
    }

    private class GroupFixtures extends DefaultFixtures<Group> implements Fixtures<Group> {

        private final Map<String, Map<String, String>> entries;

        private final Map<String, Fixture<Group>> groupMap = new HashMap<String, Fixture<Group>>();

        public GroupFixtures(final Map<String, Map<String, String>> values, FixtureLoader fixtureLoader) {
            super(Group.class, values, fixtureLoader);
            this.entries = values;
        }

        @Override
        public Fixture<Group> getEntry(final String entryName) throws OXException {
            if (groupMap.containsKey(entryName)) {
                return groupMap.get(entryName);
            }

            final Map<String, String> values = entries.get(entryName);

            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }

            final Group group = new Group();

            apply(group, values);

            final Fixture<Group> fixture = new Fixture<Group>(group, values.keySet().toArray(new String[values.size()]), values);

            groupMap.put(entryName, fixture);
            return fixture;
        }
    }
}
