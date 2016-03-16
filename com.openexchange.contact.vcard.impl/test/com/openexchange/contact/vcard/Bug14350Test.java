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
 * {@link Bug14350Test}
 *
 * vcard: addresses are not imported
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug14350Test extends VCardTest {

    /**
     * Initializes a new {@link Bug14350Test}.
     */
    public Bug14350Test() {
        super();
    }

    public void testImportVCard() throws Exception {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "PRODID:OPEN-XCHANGE\n" +
            "FN:Prinz\\, Tobias\n" +
            "N:Prinz;Tobias;;;\n" +
            "NICKNAME:Tierlieb\n" +
            "BDAY:19810501\n" +
            "ADR;TYPE=work:;;Broadway 3131 / 5th Ave;T\u00fcbingen;Baden-W\u00fcrttemberg;57621;Germany\n" +
            "ADR;TYPE=home:;;Testroad 4711;Port de la V\u00e9rde;Skol-upon-sea;37542;France\n" +
            "ORG:- deactivated -\n" +
            "REV:20061204T160750.018Z\n" +
            "UID:80@ox6.netline.de\n" +
            "END:VCARD\n";
        ;
        Contact contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Broadway 3131 / 5th Ave", contact.getStreetBusiness());
        assertEquals("T\u00fcbingen", contact.getCityBusiness());
        assertEquals("Baden-W\u00fcrttemberg", contact.getStateBusiness());
        assertEquals("57621", contact.getPostalCodeBusiness());
        assertEquals("Germany", contact.getCountryBusiness());
        assertEquals("Testroad 4711", contact.getStreetHome());
        assertEquals("Port de la V\u00e9rde", contact.getCityHome());
        assertEquals("Skol-upon-sea", contact.getStateHome());
        assertEquals("37542", contact.getPostalCodeHome());
        assertEquals("France", contact.getCountryHome());

    }

}
