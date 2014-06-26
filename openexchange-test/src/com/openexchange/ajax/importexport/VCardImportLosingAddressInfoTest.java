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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.webdav.xml.ContactTest;


/**
 * Bug 14350 and related to 6962
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class VCardImportLosingAddressInfoTest extends AbstractVCardImportTest {

    /**
     * Initializes a new {@link VCardImportLosingAddressInfoTest}.
     * @param name
     * @throws Exception
     */
    public VCardImportLosingAddressInfoTest(String name) throws Exception {
        super(name);
    }

    public void test14350() throws Exception {
        String vcard = "BEGIN:VCARD\n"
            + "VERSION:3.0\n"
            + "PRODID:OPEN-XCHANGE\n"
            + "FN:Prinz\\, Tobias\n"
            + "N:Prinz;Tobias;;;\n"
            + "NICKNAME:Tierlieb\n"
            + "BDAY:19810501\n"
            + "ADR;TYPE=work:;;Broadway 3131 / 5th Ave;T\u221a\u00babingen;Baden-W\u221a\u00barttemberg;57621;Germany\n"
            + "ADR;TYPE=home:;;Testroad 4711;Port de la V\u221a\u00a9rde;Skol-upon-sea;37542;France\n"
            + "ORG:- deactivated -\n" + "REV:20061204T160750.018Z\n"
            + "UID:80@ox6.netline.de\n"
            + "END:VCARD\n";
        final ImportResult[] importResult = importVCard(
            getWebConversation(),
            new ByteArrayInputStream(vcard.getBytes(com.openexchange.java.Charsets.UTF_8)),
            testFolder.getObjectID(),
            timeZone,
            emailaddress,
            getHostName(),
            getSessionId());

        assertFalse("Worked?", importResult[0].hasError());

        final int contactId = Integer.parseInt(importResult[0].getObjectId());
        final Contact actual = ContactTest.loadContact(
            getWebConversation(),
            contactId,
            testFolder.getObjectID(),
            getHostName(),
            getLogin(),
            getPassword(), "");

        assertEquals("Checking name:", "Prinz", actual.getSurName());

        assertEquals("Street, business", "Broadway 3131 / 5th Ave", actual.getStreetBusiness());
        assertEquals("City, business", "T\u221a\u00babingen", actual.getCityBusiness());
        assertEquals("State, business", "Baden-W\u221a\u00barttemberg", actual.getStateBusiness());
        assertEquals("ZIP, business", "57621", actual.getPostalCodeBusiness());
        assertEquals("Country, business", "Germany", actual.getCountryBusiness());

        assertEquals("Street, home", "Testroad 4711", actual.getStreetHome());
        assertEquals("City, home", "Port de la V\u221a\u00a9rde", actual.getCityHome());
        assertEquals("State, home", "Skol-upon-sea", actual.getStateHome());
        assertEquals("ZIP, home", "37542", actual.getPostalCodeHome());
        assertEquals("Country, home", "France", actual.getCountryHome());
    }

}
