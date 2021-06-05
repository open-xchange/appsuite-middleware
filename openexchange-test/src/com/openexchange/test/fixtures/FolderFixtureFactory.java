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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.fixtures.transformators.FolderModuleTransformator;
import com.openexchange.test.fixtures.transformators.FolderTypeTransformator;

/**
 * {@link FolderFixtureFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FolderFixtureFactory implements FixtureFactory<FolderObject> {

    private final FixtureLoader fixtureLoader;

    public FolderFixtureFactory(FixtureLoader fixtureLoader) {
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Fixtures<FolderObject> createFixture(Map<String, Map<String, String>> entries) {
        return new FolderFixtures(entries, fixtureLoader);
    }

    private class FolderFixtures extends DefaultFixtures<FolderObject> {

        private final Map<String, Map<String, String>> entries;

        private final Map<String, Fixture<FolderObject>> folders = new HashMap<String, Fixture<FolderObject>>();

        public FolderFixtures(Map<String, Map<String, String>> values, FixtureLoader fixtureLoader) {
            super(FolderObject.class, values, fixtureLoader);
            addTransformator(new FolderModuleTransformator(), "module");
            addTransformator(new FolderTypeTransformator(), "type");
            this.entries = values;
        }

        @Override
        public Fixture<FolderObject> getEntry(String entryName) throws OXException {
            if (folders.containsKey(entryName)) {
                return folders.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }
            final FolderObject folder = new FolderObject();
            apply(folder, values);
            final Fixture<FolderObject> fixture = new Fixture<FolderObject>(folder, values.keySet().toArray(new String[values.size()]), values);
            folders.put(entryName, fixture);
            return fixture;
        }

    }

}
