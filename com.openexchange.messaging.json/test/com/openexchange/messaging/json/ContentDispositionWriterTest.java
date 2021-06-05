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
import com.openexchange.messaging.ContentDisposition;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.StringMessageHeader;
import com.openexchange.messaging.generic.internet.MimeContentDisposition;

/**
 * {@link ContentDispositionWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContentDispositionWriterTest {

         @Test
     public void testWriteContentType() throws OXException, JSONException {
        final ContentDisposition contentDisp = new MimeContentDisposition();
        contentDisp.setDisposition(ContentDisposition.ATTACHMENT);
        contentDisp.setFilenameParameter("foo.dat");

        final ContentDispositionWriter writer = new ContentDispositionWriter();

        final SimEntry<String, Collection<MessagingHeader>> entry = entry( contentDisp );

        assertTrue(writer.handles(entry));
        assertEquals("Content-Disposition", writer.writeKey(entry));

        final Object value = writer.writeValue(entry, null);
        assertNotNull(value);

        final JSONObject jsonCDisp = (JSONObject) value;

        final JSONAssertion assertion = new JSONAssertion()
            .isObject()
                .hasKey("type").withValue(ContentDisposition.ATTACHMENT)
                .hasKey("params").withValueObject()
                    .hasKey("filename").withValue("foo.dat")
                .objectEnds()
            .objectEnds();

        assertValidates(assertion, jsonCDisp);

    }

         @Test
     public void testWriteBasicHeader() throws OXException, JSONException {
        final MessagingHeader contentDisp = new StringMessageHeader("Content-Disposition", ContentDisposition.ATTACHMENT+";filename=foo.dat");

        final ContentDispositionWriter writer = new ContentDispositionWriter();

        final SimEntry<String, Collection<MessagingHeader>> entry = entry( contentDisp );

        assertTrue(writer.handles(entry));
        assertEquals("Content-Disposition", writer.writeKey(entry));

        final Object value = writer.writeValue(entry, null);
        assertNotNull(value);

        final JSONObject jsonCDisp = (JSONObject) value;

        final JSONAssertion assertion = new JSONAssertion()
        .isObject()
            .hasKey("type").withValue(ContentDisposition.ATTACHMENT)
            .hasKey("params").withValueObject()
                .hasKey("filename").withValue("foo.dat")
            .objectEnds()
        .objectEnds();

        assertValidates(assertion, jsonCDisp);

    }

    private SimEntry<String, Collection<MessagingHeader>> entry(final MessagingHeader header) {
        return new SimEntry<String, Collection<MessagingHeader>>(header.getName(), Arrays.asList(header));
    }
}
