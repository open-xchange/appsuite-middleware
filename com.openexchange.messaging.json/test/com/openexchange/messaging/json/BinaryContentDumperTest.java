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

package com.openexchange.messaging.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.BinaryContent;

/**
 * {@link BinaryContentDumperTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class BinaryContentDumperTest {
         @Test
     public void testHandles() {
        assertTrue(new BinaryContentDumper().handles(new BinaryContent() {

            @Override
            public InputStream getData() throws OXException {
                // Nothing to do
                return null;
            }

        }));
    }

         @Test
     public void testDump() throws OXException, IOException {
        final InputStream is = new ByteArrayInputStream("Hello World".getBytes(com.openexchange.java.Charsets.UTF_8));
        final BinaryContent content = getBinaryContent(is);

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();

        new BinaryContentDumper().dump(content, bout);

        assertEquals("Hello World", bout.toString("UTF-8"));

    }

    private BinaryContent getBinaryContent(final InputStream is) {
        return new BinaryContent() {

            @Override
            public InputStream getData() throws OXException {
                return is;
            }

        };
    }

}
