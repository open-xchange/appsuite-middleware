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

package com.openexchange.contact.vcard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link ImportIteratorTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ImportIteratorTest extends VCardTest {

    /**
     * Initializes a new {@link ImportIteratorTest}.
     */
    public ImportIteratorTest() {
        super();
    }

         @Test
     public void testImportAndKeepMultipleVCards() throws Exception {
        String vCardString =
            "BEGIN:VCARD\r\n"+
            "VERSION:2.1\r\n"+
            "X-PRODID:SuperHorst\r\n"+
            "FN:Horst Example\r\n"+
            "END:VCARD\r\n" +

            "BEGIN:VCARD\r\n"+
            "VERSION:2.1\r\n"+
            "FN:Otto Example\r\n"+
            "END:VCARD\r\n" +

            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "PRODID:OPEN-XCHANGE\n" +
            "FN:Prinz\\, Tobias\n" +
            "N:Prinz;Tobias;;;\n" +
            "NICKNAME:Tierlieb\n" +
            "BDAY:19810501\n" +
            "REV:20061204T160750.018Z\n" +
            "UID:80@ox6.netline.de\n" +
            "END:VCARD\n" +

            "BEGIN:VCARD\r\n"+
            "VERSION:2.1\r\n"+
            "FN:Herbert Example\r\n"+
            "END:VCARD\r\n" +

            "BEGIN:VCARD\r\n"+
            "VERSION:2.1\r\n"+
            "FN:Klaus Example\r\n"+
            "END:VCARD\r\n"
        ;
        VCardParameters parameters = getService().createParameters().setKeepOriginalVCard(true);
        SearchIterator<VCardImport> searchIterator = null;
        try {
            searchIterator = getService().importVCards(Streams.newByteArrayInputStream(vCardString.getBytes(Charsets.UTF_8)), parameters);
            assertTrue(searchIterator.hasNext());
            int count = 0;
            while (searchIterator.hasNext()) {
                count++;
                VCardImport vCardImport = searchIterator.next();
                assertNotNull(vCardImport.getContact());
                String originalVCard = Streams.stream2string(vCardImport.getVCard().getStream(), Charsets.UTF_8_NAME);
                assertNotNull(originalVCard);
                assertTrue(originalVCard.startsWith("BEGIN:VCARD"));
                assertTrue(originalVCard.trim().endsWith("END:VCARD"));
                Assert.assertEquals(1, countSubstring("BEGIN:VCARD", originalVCard));
                Assert.assertEquals(1, countSubstring("END:VCARD", originalVCard));
            }
            Assert.assertEquals(5, count);
        } finally {
            SearchIterators.close(searchIterator);
        }

    }

    private static int countSubstring(String subStr, String str){
        int count = 0;
        for (int loc = str.indexOf(subStr); loc != -1; loc = str.indexOf(subStr, loc + subStr.length())) {
            count++;
        }
        return count;
    }

         @Test
     public void testEmptyHasNext() throws Exception {
        VCardParameters parameters = getService().createParameters().setKeepOriginalVCard(true);
        SearchIterator<VCardImport> searchIterator = null;
        try {
            searchIterator = getService().importVCards(Streams.EMPTY_INPUT_STREAM, parameters);
            assertFalse(searchIterator.hasNext());
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

         @Test
     public void testEmptyNext() throws Exception {
        VCardParameters parameters = getService().createParameters().setKeepOriginalVCard(true);
        SearchIterator<VCardImport> searchIterator = null;
        try {
            searchIterator = getService().importVCards(Streams.EMPTY_INPUT_STREAM, parameters);
            assertNull(searchIterator.next());
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

}
