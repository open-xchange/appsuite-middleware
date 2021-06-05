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
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.exception.OXException;

/**
 * {@link ContentDispositionTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentDispositionTest {

    /**
     * Initializes a new {@link ContentDispositionTest}.
     */
    public ContentDispositionTest() {
        super();
    }

    @Test
    public void testFileNameParameterFromBogusHeaders() {
        try {
            String hdr = "attachment; filename=Jana's application 4 cell phone.rtf";
            String fileName = new com.openexchange.mail.mime.ContentDisposition(hdr).getFilenameParameter();
            assertEquals("Unpexpected \"filename\" parameter.", "Jana's application 4 cell phone.rtf", fileName);

            hdr = "attachment; filename*0=Test - Test.pdf";
            fileName = new com.openexchange.mail.mime.ContentDisposition(hdr).getFilenameParameter();
            assertEquals("Unpexpected \"filename\" parameter.", "Test - Test.pdf", fileName);

            hdr = "filename=\"Scan1852.pdf\"; filename=\"Scan1852.pdf\"";
            fileName = new com.openexchange.mail.mime.ContentDisposition(hdr).getFilenameParameter();
            assertEquals("Unpexpected \"filename\" parameter.", "Scan1852.pdf", fileName);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testFileNameParameterFromRFC2231EncodedParameters() {
        try {
            ContentDisposition cd = new ContentDisposition("attachment; filename*0*=iso-2022-jp''%1B%24%42%23%38%37%6E%24%4E%4D%3C%34%29%25%33%25; filename*1*=%69%25%60%3C%42%40%53%1B%28%42%2E%64%6F%63%78");
            assertEquals("Unpexpected \"filename\" parameter.", "\uff18\u6708\u306e\u5915\u520a\u30b3\u30e9\u30e0\u5b9f\u7e3e.docx", cd.getFilenameParameter());
        } catch (OXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
