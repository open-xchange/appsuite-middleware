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
 * {@link Bug21656Test}
 *
 *  vCard containing option QUOTED-PRINTABLE can not be imported
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug21656Test extends VCardTest {

    /**
     * Initializes a new {@link Bug21656Test}.
     */
    public Bug21656Test() {
        super();
    }

    public void testImportVCard() throws Exception {
        /*
         * import vCard
         */
        byte[] vCard = {
            66, 69, 71, 73, 78, 58, 86, 67, 65, 82, 68, 13, 10, 88, 45, 76, 79, 84, 85, 83, 45, 67, 72, 65, 82, 83, 69, 84, 58, 119, 105,
            110, 100, 111, 119, 115, 45, 49, 50, 53, 50, 13, 10, 86, 69, 82, 83, 73, 79, 78, 58, 51, 46, 48, 13, 10, 79, 82, 71, 58, 80,
            101, 115, 99, 104, 108, 97, 32, 38, 32, 82, 111, 99, 104, 109, 101, 115, 32, 71, 109, 98, 72, 13, 10, 69, 77, 65, 73, 76, 59,
            84, 89, 80, 69, 61, 73, 78, 84, 69, 82, 78, 69, 84, 58, 109, 110, 105, 99, 107, 101, 108, 64, 103, 112, 114, 46, 100, 101, 13,
            10, 84, 69, 76, 59, 84, 89, 80, 69, 61, 70, 65, 88, 58, 48, 54, 57, 32, 51, 54, 32, 54, 48, 32, 56, 55, 32, 48, 57, 13, 10,
            84, 69, 76, 59, 84, 89, 80, 69, 61, 87, 79, 82, 75, 58, 48, 54, 57, 32, 51, 54, 32, 54, 48, 32, 56, 55, 32, 48, 56, 13, 10,
            84, 69, 76, 59, 84, 89, 80, 69, 61, 67, 69, 76, 76, 58, 48, 49, 55, 50, 32, 54, 55, 48, 32, 49, 49, 32, 53, 55, 13, 10, 84,
            73, 84, 76, 69, 58, 66, 101, 114, 101, 105, 99, 104, 115, 108, 101, 105, 116, 101, 114, 32, 69, 110, 101, 114, 103, 105, 101,
            32, 117, 110, 100, 32, 65, 98, 102, 97, 108, 108, 13, 10, 85, 82, 76, 58, 119, 119, 119, 46, 103, 112, 114, 46, 100, 101, 13,
            10, 78, 79, 84, 69, 58, 32, 13, 10, 65, 68, 82, 59, 84, 89, 80, 69, 61, 87, 79, 82, 75, 59, 69, 78, 67, 79, 68, 73, 78, 71,
            61, 81, 85, 79, 84, 69, 68, 45, 80, 82, 73, 78, 84, 65, 66, 76, 69, 58, 59, 59, 72, 105, 110, 116, 101, 114, 103, 97, 115,
            115, 101, 32, 49, 56, 61, 48, 65, 59, 72, 111, 99, 104, 104, 101, 105, 109, 32, 97, 109, 32, 77, 97, 105, 110, 59, 59, 54, 53,
            50, 51, 57, 59, 68, 101, 117, 116, 115, 99, 104, 108, 97, 110, 100, 13, 10, 78, 58, 78, 105, 99, 107, 101, 108, 59, 77, 97,
            114, 99, 117, 115, 59, 59, 59, 13, 10, 70, 78, 58, 77, 97, 114, 99, 117, 115, 32, 78, 105, 99, 107, 101, 108, 13, 10, 69, 78,
            68, 58, 86, 67, 65, 82, 68, 13, 10
        };
        Contact contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Peschla & Rochmes GmbH", contact.getCompany());
        assertEquals("mnickel@gpr.de", contact.getEmail1());
//        assertEquals("069 36 60 87 09", contact.getFaxOther());
        assertEquals("069 36 60 87 08", contact.getTelephoneBusiness1());
        assertEquals("0172 670 11 57", contact.getCellularTelephone1());
        assertEquals("Bereichsleiter Energie und Abfall", contact.getPosition());
        assertEquals("Hintergasse 18", contact.getStreetBusiness());
        assertEquals("Hochheim am Main", contact.getCityBusiness());
        assertEquals("65239", contact.getPostalCodeBusiness());
        assertEquals("Deutschland", contact.getCountryBusiness());
        assertEquals("Marcus Nickel", contact.getDisplayName());
        assertEquals("Nickel", contact.getSurName());
        assertEquals("Marcus", contact.getGivenName());
    }

}
