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

import static com.openexchange.json.JSONAssertion.assertValidates;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.json.JSONAssertion;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeContentType;

/**
 * {@link ContentTypeWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContentTypeWriterTest {

         @Test
     public void testWriteContentType() throws OXException, JSONException {
        final ContentType contentType = new MimeContentType();
        contentType.setPrimaryType("text");
        contentType.setSubType("plain");
        contentType.setCharsetParameter("UTF-8");
        contentType.setNameParameter("something.txt");

        final ContentTypeWriter writer = new ContentTypeWriter();

        final SimEntry<String, Collection<MessagingHeader>> entry = entry( contentType );

        assertTrue(writer.handles(entry));
        assertEquals("Content-Type", writer.writeKey(entry));

        final Object value = writer.writeValue(entry, null);
        assertNotNull(value);

        final JSONObject jsonCType = (JSONObject) value;

        final JSONAssertion assertion = new JSONAssertion()
            .isObject()
                .hasKey("type").withValue("text/plain")
                .hasKey("params").withValueObject()
                    .hasKey("charset").withValue("UTF-8")
                    .hasKey("name").withValue("something.txt")
                .objectEnds()
            .objectEnds();

        assertValidates(assertion, jsonCType);

    }

         @Test
     public void testWriteBasicHeader() throws OXException, JSONException {
        final MessagingHeader contentType = new StringMessageHeader("Content-Type", "text/plain;charset=UTF-8;name=something.txt");

        final ContentTypeWriter writer = new ContentTypeWriter();

        final SimEntry<String, Collection<MessagingHeader>> entry = entry( contentType );

        assertTrue(writer.handles(entry));
        assertEquals("Content-Type", writer.writeKey(entry));

        final Object value = writer.writeValue(entry, null);
        assertNotNull(value);

        final JSONObject jsonCType = (JSONObject) value;

        final JSONAssertion assertion = new JSONAssertion()
            .isObject()
                .hasKey("type").withValue("text/plain")
                .hasKey("params").withValueObject()
                    .hasKey("charset").withValue("UTF-8")
                    .hasKey("name").withValue("something.txt")
                .objectEnds()
            .objectEnds();

        assertValidates(assertion, jsonCType);

    }

    private SimEntry<String, Collection<MessagingHeader>> entry(final MessagingHeader header) {
        return new SimEntry<String, Collection<MessagingHeader>>(header.getName(), Arrays.asList(header));
    }
}
