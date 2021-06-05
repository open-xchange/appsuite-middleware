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

import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.exception.OXException;

/**
 * {@link WarningsTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class WarningsTest extends VCardTest {

    /**
     * Initializes a new {@link WarningsTest}.
     */
    public WarningsTest() {
        super();
    }

         @Test
     public void testImportWithoutN() throws OXException {
        String vCardString =
            "BEGIN:VCARD\r\n"+
            "VERSION:2.1\r\n"+
            "FN:Erika Mustermann\r\n"+
            "END:VCARD\r\n"
        ;
        VCardImport vCardImport = importVCard(vCardString);
        assertNotNull(vCardImport.getContact());
        List<OXException> warnings = vCardImport.getWarnings();
        assertNotNull(warnings);
        Assert.assertEquals(1, warnings.size());
    }

         @Test
     public void testImportWithInvalidEMail() throws OXException {
        String vCardString =
            "BEGIN:VCARD\r\n"+
            "VERSION:2.1\r\n"+
            "N:Mustermann;Erika\r\n"+
            "FN:Erika Mustermann\r\n"+
            "EMAIL;PREF;INTERNET:erika@mustermann@de\r\n"+
            "END:VCARD\r\n"
        ;
        VCardImport vCardImport = importVCard(vCardString);
        assertNotNull(vCardImport.getContact());
        List<OXException> warnings = vCardImport.getWarnings();
        assertNotNull(warnings);
        Assert.assertFalse(warnings.isEmpty());
    }

         @Test
     public void testImportInvalidPhoto() throws OXException {
        String vCardString =
            "BEGIN:VCARD\r\n"+
            "VERSION:2.1\r\n"+
            "N:Mustermann;Erika\r\n"+
            "FN:Erika Mustermann\r\n"+
            "PHOTO;JPEG:http://www.open-xchange.com\r\n"+
            "END:VCARD\r\n"
        ;
        VCardImport vCardImport = importVCard(vCardString);
        assertNotNull(vCardImport.getContact());
        List<OXException> warnings = vCardImport.getWarnings();
        assertNotNull(warnings);
        Assert.assertEquals(1, warnings.size());
    }

         @Test
     public void testImportInvalidBirthday() throws OXException {
        String vCardString =
            "BEGIN:VCARD\r\n"+
            "VERSION:2.1\r\n"+
            "N:Mustermann;Erika\r\n"+
            "FN:Erika Mustermann\r\n"+
            "BDAY:gestern\r\n"+
            "END:VCARD\r\n"
        ;
        VCardImport vCardImport = importVCard(vCardString);
        assertNotNull(vCardImport.getContact());
        List<OXException> warnings = vCardImport.getWarnings();
        assertNotNull(warnings);
        Assert.assertEquals(1, warnings.size());
    }

}
