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

package com.openexchange.mail.mime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.exception.OXException;

/**
 * {@link ContentTypeTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentTypeTest {

    /**
     * Initializes a new {@link ContentTypeTest}.
     */
    public ContentTypeTest() {
        super();
    }

    @Test
    public void testTruncatedNameParameter() {
        try {
            String hdr = "application/pdf; name=The New York Times - Breaking News, World News & Multimedia.loc.pdf";
            com.openexchange.mail.mime.ContentType contentType = new com.openexchange.mail.mime.ContentType(hdr);
            String name = contentType.getNameParameter();

            assertEquals("Unexpected \"name\" parameter.", "The New York Times - Breaking News, World News & Multimedia.loc.pdf", name);

            assertEquals("Unexpected toString() result.", "application/pdf; name=\"The New York Times - Breaking News, World News & Multimedia.loc.pdf\"", contentType.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testWithCurlyBraces() {
        try {
            String hdr = "{\"application/octet-stream\"}; name=\"6N1911.pdf\"";
            com.openexchange.mail.mime.ContentType contentType = new com.openexchange.mail.mime.ContentType(hdr);

            assertEquals("Unexpected primary type", "application", contentType.getPrimaryType());
            assertEquals("Unexpected subtype", "octet-stream", contentType.getSubType());
            assertEquals("Unexpected name parameter", "6N1911.pdf", contentType.getNameParameter());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testMalformedHeaderValue() {
        try {
            String hdr = "=?windows-1252?q?application/pdf; name=\"blatt8.pdf\"";
            com.openexchange.mail.mime.ContentType contentType = new com.openexchange.mail.mime.ContentType(hdr);

            assertEquals("Unexpected primary type", "application", contentType.getPrimaryType());
            assertEquals("Unexpected subtype", "pdf", contentType.getSubType());
            assertEquals("Unexpected name parameter", "blatt8.pdf", contentType.getNameParameter());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testBug56740() {
        try {
            String hdr = "t,text/html";
            com.openexchange.mail.mime.ContentType contentType = new com.openexchange.mail.mime.ContentType(hdr, true);
            fail("Content-Type string \"t,text/html\" should no pass strict parsing");
            contentType.setBaseType("foo/bar"); // To keep IDE happy
        } catch (Exception e) {
            assertTrue(e instanceof OXException);
            OXException oxe = (OXException) e;
            assertTrue(oxe.equalsCode(20, "MSG"));
        }

        try {
            String hdr = "t/@,image/svg+xml";
            com.openexchange.mail.mime.ContentType contentType = new com.openexchange.mail.mime.ContentType(hdr, true);
            fail("Content-Type string \"t/@,image/svg+xml\" should no pass strict parsing");
            contentType.setBaseType("foo/bar"); // To keep IDE happy
        } catch (Exception e) {
            assertTrue(e instanceof OXException);
            OXException oxe = (OXException) e;
            assertTrue(oxe.equalsCode(20, "MSG"));
        }

        try {
            String hdr = "text/plain;,text/html";
            com.openexchange.mail.mime.ContentType contentType = new com.openexchange.mail.mime.ContentType(hdr, true);
            fail("Content-Type string \"text/plain;,text/html\" should no pass strict parsing");
            contentType.setBaseType("foo/bar"); // To keep IDE happy
        } catch (Exception e) {
            assertTrue(e instanceof OXException);
            OXException oxe = (OXException) e;
            assertTrue(oxe.equalsCode(20, "MSG"));
        }
    }

}
