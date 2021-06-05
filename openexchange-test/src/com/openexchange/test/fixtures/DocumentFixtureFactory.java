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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.test.fixtures.transformators.CredentialsTransformator;

/**
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class DocumentFixtureFactory implements FixtureFactory<Document> {

    private final File datapath;
    private String seleniumDataPath;
    private String seleniumSeparator;
    private final FixtureLoader fixtureLoader;

    public DocumentFixtureFactory(File datapath, FixtureLoader fixtureLoader) {
        super();
        this.datapath = datapath;
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Fixtures<Document> createFixture(final Map<String, Map<String, String>> entries) {
        DocumentFixtures documentFixtures = new DocumentFixtures(entries, datapath, fixtureLoader);
        if (seleniumDataPath != null) {
            documentFixtures.setSeleniumConfiguration(seleniumDataPath, seleniumSeparator);
        }
        return documentFixtures;
    }

    public void setSeleniumConfiguration(String seleniumDataPath, String seleniumSeparator) {
        this.seleniumDataPath = seleniumDataPath;
        this.seleniumSeparator = seleniumSeparator;
    }

    private class DocumentFixtures extends DefaultFixtures<Document> implements Fixtures<Document> {

        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<Document>> knownDocuments = new HashMap<String, Fixture<Document>>();
        @SuppressWarnings("hiding")
        private final File datapath;
        @SuppressWarnings("hiding")
        private String seleniumDataPath;
        @SuppressWarnings("hiding")
        private String seleniumSeparator;

        public DocumentFixtures(final Map<String, Map<String, String>> values, File datapath, FixtureLoader fixtureLoader) {
            super(Document.class, values, fixtureLoader);
            this.entries = values;
            this.datapath = datapath;

            super.addTransformator(new CredentialsTransformator(fixtureLoader), "created_by");
        }

        @Override
        public Fixture<Document> getEntry(final String entryName) throws OXException {
            if (knownDocuments.containsKey(entryName)) {
                return knownDocuments.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }
            final Document document = new Document(datapath);
            apply(document, values);

            if (seleniumDataPath != null) {
                document.setSeleniumConfiguration(seleniumDataPath, seleniumSeparator);
            }

            final Fixture<Document> fixture = new Fixture<Document>(document, values.keySet().toArray(new String[values.size()]), values);
            knownDocuments.put(entryName, fixture);
            return fixture;
        }

        public void setSeleniumConfiguration(String seleniumDataPath, String seleniumSeparator) {
            this.seleniumDataPath = seleniumDataPath;
            this.seleniumSeparator = seleniumSeparator;
        }
    }
}
