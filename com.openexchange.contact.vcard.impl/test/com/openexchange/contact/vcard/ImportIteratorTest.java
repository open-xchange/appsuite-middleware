/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.contact.vcard;

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
                assertEquals(1, countSubstring("BEGIN:VCARD", originalVCard));
                assertEquals(1, countSubstring("END:VCARD", originalVCard));
            }
            assertEquals(5, count);
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
