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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.openexchange.exception.OXException;
import com.openexchange.test.fixtures.Document;
import com.openexchange.test.fixtures.FixtureLoader;

/**
 * Transforms strings of the kind document:big_image into a byte[] array.
 *
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class FileToByteArrayTransformator implements Transformator {

    private final FixtureLoader fixtureLoader;

    public FileToByteArrayTransformator(FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
    }

    @Override
    public byte[] transform(final String value) throws OXException {
        if (null == value || 1 > value.length()) {
            return null;
        }
        String fixtureName = "documents";
        String fixtureEntry = "";
        final int idx = value.indexOf(':');
        if (0 < idx && value.length() > idx) {
            fixtureName = value.substring(0, idx);
            fixtureEntry = value.substring(idx + 1);
        } else {
            fixtureEntry = value;
        }
        try {
            return getByteArray(getDocument(fixtureName, fixtureEntry).getFile());
        } catch (IOException e) {
            throw new OXException(e);
        }
    }

    private final Document getDocument(final String fixtureName, final String fixtureEntry) throws OXException {
        return fixtureLoader.getFixtures(fixtureName, Document.class).getEntry(fixtureEntry).getEntry();
    }

    private byte[] getByteArray(final File file) throws IOException {
        final FileInputStream fileInputStream = new FileInputStream(file);
        final byte[] data = new byte[(int) file.length()];
        fileInputStream.read(data);
        fileInputStream.close();
        return data;
    }
}
