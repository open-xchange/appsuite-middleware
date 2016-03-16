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
