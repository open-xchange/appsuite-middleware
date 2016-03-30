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

import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug15008Test}
 *
 * vcf import failed
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug15008Test extends VCardTest {

    /**
     * Initializes a new {@link Bug15008Test}.
     */
    public Bug15008Test() {
        super();
    }

    public void testImportVCard() throws Exception {
        /*
         * import vCard
         */
        byte[] vCard = {
            66, 69, 71, 73, 78, 58, 86, 67, 65, 82, 68, 13, 10, 88, 45, 76, 79, 84, 85, 83, 45, 67, 72, 65, 82, 83, 69, 84, 58, 119, 105,
            110, 100, 111, 119, 115, 45, 49, 50, 53, 50, 13, 10, 86, 69, 82, 83, 73, 79, 78, 58, 51, 46, 48, 13, 10, 79, 82, 71, 58, 86,
            80, 67, 13, 10, 69, 77, 65, 73, 76, 59, 84, 89, 80, 69, 61, 73, 78, 84, 69, 82, 78, 69, 84, 58, 83, 116, 101, 102, 97, 110,
            46, 65, 100, 97, 109, 115, 64, 118, 105, 112, 99, 111, 109, 97, 103, 46, 100, 101, 13, 10, 69, 77, 65, 73, 76, 59, 84, 89, 80,
            69, 61, 73, 78, 84, 69, 82, 78, 69, 84, 58, 83, 116, 101, 102, 97, 110, 95, 95, 65, 100, 97, 109, 115, 64, 104, 111, 116, 109,
            97, 105, 108, 46, 99, 111, 109, 13, 10, 84, 69, 76, 59, 84, 89, 80, 69, 61, 87, 79, 82, 75, 58, 43, 52, 57, 56, 57, 53, 52,
            55, 53, 48, 49, 48, 56, 13, 10, 84, 69, 76, 59, 84, 89, 80, 69, 61, 72, 79, 77, 69, 58, 43, 52, 57, 32, 40, 52, 57, 53, 50,
            41, 32, 54, 49, 48, 52, 51, 48, 13, 10, 84, 69, 76, 59, 84, 89, 80, 69, 61, 67, 69, 76, 76, 58, 43, 52, 57, 32, 40, 49, 53,
            49, 41, 32, 53, 48, 49, 48, 52, 52, 51, 54, 13, 10, 65, 68, 82, 59, 84, 89, 80, 69, 61, 87, 79, 82, 75, 59, 69, 78, 67, 79,
            68, 73, 78, 71, 61, 81, 85, 79, 84, 69, 68, 45, 80, 82, 73, 78, 84, 65, 66, 76, 69, 58, 59, 59, 65, 108, 101, 114, 105, 99,
            104, 45, 69, 98, 101, 108, 105, 110, 103, 115, 45, 87, 101, 103, 32, 51, 56, 97, 61, 48, 65, 59, 82, 104, 97, 117, 100, 101,
            114, 102, 101, 104, 110, 59, 59, 50, 54, 56, 49, 55, 59, 68, 101, 117, 116, 115, 99, 104, 108, 97, 110, 100, 13, 10, 78, 58,
            65, 100, 105, 59, 59, 59, 59, 13, 10, 70, 78, 58, 65, 100, 105, 13, 10, 69, 78, 68, 58, 86, 67, 65, 82, 68, 13, 10
        };
        Contact contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("VPC", contact.getCompany());
        assertEquals("Stefan.Adams@vipcomag.de", contact.getEmail1());
        assertEquals("+498954750108", contact.getTelephoneBusiness1());
        assertEquals("+49 (4952) 610430", contact.getTelephoneHome1());
        assertEquals("+49 (151) 50104436", contact.getCellularTelephone1());
        assertEquals("Alerich-Ebelings-Weg 38a", contact.getStreetBusiness());
        assertEquals("Rhauderfehn", contact.getCityBusiness());
        assertEquals("26817", contact.getPostalCodeBusiness());
        assertEquals("Deutschland", contact.getCountryBusiness());
        assertEquals("Adi", contact.getDisplayName());
        assertEquals("Adi", contact.getSurName());
    }

}
