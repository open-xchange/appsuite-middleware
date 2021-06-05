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

package javax.mail.internet.idn;

import javax.mail.internet.AddressException;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests for class {@link IDNA}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class IDNATest {

     @Test
     public void testToACE_ParamNull_ReturnNull() throws AddressException {
        String aceText = IDNA.toACE(null);

        Assert.assertNull(aceText);
    }

     @Test
     public void testToACE_ParamIsEmptyString_ReturnEmptyString() throws AddressException {
        String aceText = IDNA.toACE("");

        Assert.assertNotNull(aceText);
        Assert.assertEquals(0, aceText.length());
    }

     @Test
     public void testToACE_OnlyMailAdressWithUmlaute_ConvertToAscii() throws AddressException {
        String testString = "someone@m\u00fcller.de";

        String aceText = IDNA.toACE(testString);
        Assert.assertEquals("someone@xn--mller-kva.de", aceText);
    }

     @Test
     public void testToACE_MailAdressWithUmlauteAndContent_ReturnParamString() throws AddressException {
        String testString = "\"Sch\u00f6nmackers Umweltdienste - Abrechnung \" <zentrale.abrechnung@schoenmackers.de>";

        String aceText = IDNA.toACE(testString);
        Assert.assertEquals(testString, aceText);
    }

     @Test
     public void testToACE_MailAdressWithUmlauteAndUtfPrefix_ReturnParamString() throws AddressException {
        String testString = "=?UTF-8?B?w5xiZXJ3ZWlzdW5n? =\" <\u00fcberweisung@aftrapp.com>";

        String aceText = IDNA.toACE(testString);
        Assert.assertEquals(testString, aceText);
    }

     @Test
     public void testToACE_WithoutUmlaute_ReturnParamString() throws AddressException {
        String testString = "martin.schneider@open-xchange.com";

        String aceText = IDNA.toACE(testString);
        Assert.assertEquals(testString, aceText);
    }

     @Test
     public void testToACE_WithoutUmlauteButLessAndGreaterSigns_ReturnParamString() throws AddressException {
        String testString = "<martin.schneider@open-xchange.com>";

        String aceText = IDNA.toACE(testString);
        Assert.assertEquals(testString, aceText);
    }

     @Test
     public void testToACE_WithUmlauteAndLessAndGreaterSigns_ConvertToAscii() throws AddressException {
        String testString = "<martin.schneider@\u00f6pen-xch\u00e4nge.com>";

        String aceText = IDNA.toACE(testString);
        Assert.assertEquals("<martin.schneider@xn--pen-xchnge-w5a2s.com>", aceText);
    }

     @Test
     public void testToACE_WithUmlauteWithoutLessAndGreaterSigns_ConvertToAscii() throws AddressException {
        String testString = "martin.schneider@\u00f6pen-xch\u00e4nge.com";

        String aceText = IDNA.toACE(testString);
        Assert.assertEquals("martin.schneider@xn--pen-xchnge-w5a2s.com", aceText);
    }

     @Test
     public void testToACE_NotValidEmail_ReturnParamString() throws AddressException {
        String testString = "<m\u00e4rtin.schneider";

        String aceText = IDNA.toACE(testString);
        Assert.assertEquals(testString, aceText);
    }
}
