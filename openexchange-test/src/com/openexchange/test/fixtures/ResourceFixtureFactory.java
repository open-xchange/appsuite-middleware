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
import com.openexchange.resource.Resource;

/**
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class ResourceFixtureFactory implements FixtureFactory<Resource> {

    private final FixtureLoader fixtureLoader;

    public ResourceFixtureFactory(FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Fixtures<Resource> createFixture(final Map<String, Map<String, String>> entries) {
        return new ResourceFixtures(entries, fixtureLoader);
    }

    private class ResourceFixtures extends DefaultFixtures<Resource> implements Fixtures<Resource> {

        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<Resource>> resourceMap = new HashMap<String, Fixture<Resource>>();

        public ResourceFixtures(final Map<String, Map<String, String>> values, FixtureLoader fixtureLoader) {
            super(Resource.class, values, fixtureLoader);
            this.entries = values;
        }

        @Override
        public Fixture<Resource> getEntry(final String entryName) throws OXException {
            if (resourceMap.containsKey(entryName)) {
                return resourceMap.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }
            final Resource resource = new Resource();
            apply(resource, values);
            final Fixture<Resource> fixture = new Fixture<Resource>(resource, values.keySet().toArray(new String[values.size()]), values);
            resourceMap.put(entryName, fixture);
            return fixture;
        }
    }
}
