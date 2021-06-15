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

import java.io.InputStream;
import java.util.List;
import org.junit.Test;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link MWB1133Test}
 *
 * incomplete import from VCF file
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.6
 */
public class MWB1133Test extends VCardTest {

    /**
     * Initializes a new {@link MWB1133Test}.
     */
    public MWB1133Test() {
        super();
    }

    @Test
    public void testImportVCard() throws Exception {
        String vCard = // @formatter:off
            "begin:vcard" + "\n" +
            "fn:Hans Wurst" + "\n" +
            "n:Wurst;Hans" + "\n" +
            "org:Orga;IT" + "\n" +
            "email;internet:hans@example.com" + "\n" +
            "title:Boss" + "\n" +
            "tel;work:+49 111 1111" + "\n" +
            "tel;home:+49 222 2222" + "\n" +
            "version:2.1" + "\n" +
            "end:vcard" + "\n" +
            "" + "\n" +
            "begin:vcard" + "\n" +
            "fn:Some Guiy" + "\n" +
            "n:;Some Guy" + "\n" +
            "version:2.1" + "\n" +
            "end:vcard" + "\n" +
            "" + "\n" +
            "begin:vcard" + "\n" +
            "fn:foo@example.com" + "\n" +
            "email;internet:foo@example.com" + "\n" +
            "version:2.1" + "\n" +
            "end:vcard" + "\n" +
            "" + "\n" +
            "begin:vcard" + "\n" +
            "fn:bar@example.com" + "\n" +
            "email;internet:bar@example.com" + "\n" +
            "version:2.1" + "\n" +
            "end:vcard" + "\n" +
            "" + "\n" 
        ; // @formatter:on
        
        List<VCardImport> vCardImports;
        try (InputStream inputStream = Streams.newByteArrayInputStream(vCard.getBytes(Charsets.UTF_8))) {
            vCardImports = SearchIterators.asList(getService().importVCards(inputStream, getParameters()));
        }
        org.junit.Assert.assertEquals("Not all vcards imported", 4, vCardImports.size());
    }

}
