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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.vcard;

import java.io.IOException;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.converter.ConverterException;

/**
 * Testing bug 6962, in which different MIME types ruin parsing.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class VCardMimeTypeTest extends AbstractVCardUnitTest {

    public final String vcard1 = "BEGIN:VCARD\n" +
    		"VERSION:3.0\n" +
    		"PRODID:OPEN-XCHANGE\n" +
    		"FN:Prinz\\, Tobias\n" +
    		"N:Prinz;Tobias;;;\n" +
    		"NICKNAME:Tierlieb\n" +
    		"BDAY:19810501\n" +
    		"ADR;TYPE=work:;;;Somewhere;NRW;58641;DE\n" +
    		"TEL;TYPE=home,voice:+49 2538 7921\n" +
    		"EMAIL:tobias.prinz@open-xchange.com\n" +
    		"ORG:- deactivated -\n" +
    		"REV:20061204T160750.018Z\n" +
    		"URL:www.tobias-prinz.de\n" +
    		"UID:80@ox6.netline.de\n" +
    		"END:VCARD\n";

    public final String vcard2 = "BEGIN:VCARD\n" +
    		"VERSION:3.0\n" +
    		"N:;Svetlana;;;\n" +
    		"FN:Svetlana\n" +
    		"TEL;type=CELL;type=pref:6670373\n" +
    		"CATEGORIES:Nicht abgelegt\n" +
    		"X-ABUID:CBC739E8-694E-4589-8651-8C30E1A6E724\\:ABPerson\n" +
    		"END:VCARD";

    public void test6962variant1() throws IOException, ConverterException {
        performTest("vCard 1 as " + mime1, vcard1, mime1);
    }

    public void test6962variant2() throws IOException, ConverterException {
        try {
            performTest("vCard 2 as " + mime1, vcard2, mime1);
            fail("Should not be able to parse this as VCard 2.1");
        }  catch (VersitException e) {
            assertTrue(true);
        }
    }

    public void test6962variant3() throws IOException, ConverterException {
        performTest("vCard 1 as " + mime2, vcard1, mime2);
    }

    public void test6962variant4() throws IOException, ConverterException {
        performTest("vCard 2 as " + mime2, vcard2, mime2);
    }
}
