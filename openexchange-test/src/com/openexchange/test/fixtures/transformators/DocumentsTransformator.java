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
import com.openexchange.exception.OXException;
import com.openexchange.test.fixtures.Document;
import com.openexchange.test.fixtures.FixtureLoader;

/**
 * Transforms strings of the kind document:big_image,small_image into a
 * Document[] array.
 *
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class DocumentsTransformator implements Transformator {

    private final FixtureLoader fixtureLoader;

    public DocumentsTransformator(FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public Document[] transform(final String value) throws OXException {
        if (null == value || 1 > value.length()) {
            return null;
        }
        String fixtureName = "documents";
        String fixtureEntry = "";
        final String[] splitted = value.split(",");
        final List<Document> documents = new ArrayList<Document>(splitted.length);
        for (int i = 0; i < splitted.length; i++) {
            final int idx = splitted[i].indexOf(':');
            if (0 < idx && splitted[i].length() > idx) {
                fixtureName = splitted[i].substring(0, idx);
                fixtureEntry = splitted[i].substring(idx + 1);
            } else {
                fixtureEntry = splitted[i];
            }
            documents.add(getDocument(fixtureName, fixtureEntry));
        }
        return documents.toArray(new Document[documents.size()]);
    }

    private final Document getDocument(final String fixtureName, final String fixtureEntry) throws OXException {
        return fixtureLoader.getFixtures(fixtureName, Document.class).getEntry(fixtureEntry).getEntry();
    }
}
