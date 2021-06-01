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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.common.groupware.importexport.ContactTestData;

/**
 * This bug is concerned with data sent as UTF8 stream with a Byte Order Mark (BOM).
 * UTF8 does not need one (it would be pointless actually, since the main improvements
 * of UTF8 are dynamic length and backwards-compatibility to US-ASCII). It is actually
 * recommended not to use one, but of course, some Windows programs insist on using
 * one anyway. This test tries out several BOMs to prove that the import works even
 * in those weird situations.
 *
 * @author tobiasp
 *
 */
public class Bug18482Test_ByteOrderMarkOnUtf8 extends AbstractManagedContactTest {

    String csv = ContactTestData.IMPORT_MULTIPLE;

    @Test
    public void testNone() throws Exception {
        testWithBOM();
    }

    @Test
    public void testUTF8() throws Exception {
        testWithBOM(0xEF, 0xBB, 0xBF);
    }

    @Test
    public void testUTF16LE() throws Exception {
        testWithBOM(0xFF, 0xFE);
    }

    @Test
    public void testUTF16BE() throws Exception {
        testWithBOM(0xFE, 0xFF);
    }

    @Test
    public void testUTF32LE() throws Exception {
        testWithBOM(0xFF, 0xFE, 0x00, 0x00);
    }

    @Test
    public void testUTF32BE() throws Exception {
        testWithBOM(0x00, 0x00, 0xFE, 0xFF);
    }

    private void testWithBOM(int... bom) throws Exception {
        byte[] bytes = csv.getBytes(com.openexchange.java.Charsets.UTF_8);
        byte[] streambase = new byte[bom.length + bytes.length];
        for (int i = 0; i < bom.length; i++) {
            streambase[i] = (byte) bom[i];
        }
        for (int i = bom.length; i < streambase.length; i++) {
            streambase[i] = bytes[i - bom.length];
        }

        InputStream stream = new ByteArrayInputStream(streambase);
        CSVImportRequest importRequest = new CSVImportRequest(folderID, stream, false);
        AbstractAJAXResponse response = cotm.getClient().execute(importRequest);

        assertFalse(response.hasError());
        assertFalse(response.hasConflicts());

        JSONArray data = (JSONArray) response.getData();
        assertEquals(2, data.length());

        Contact c1 = cotm.getAction(folderID, data.getJSONObject(0).getInt("id"));
        Contact c2 = cotm.getAction(folderID, data.getJSONObject(1).getInt("id"));
        assertTrue(c1.getGivenName().equals(ContactTestData.NAME1));
        assertTrue(c2.getGivenName().equals(ContactTestData.NAME2));
    }

}
