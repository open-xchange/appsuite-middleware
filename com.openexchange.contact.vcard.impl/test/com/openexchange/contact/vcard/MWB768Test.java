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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;

/**
 * {@link MWB768Test}
 *
 * Imported vcard shows mail address twice in contact
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class MWB768Test extends VCardTest {

    /**
     * Initializes a new {@link MWB768Test}.
     */
    public MWB768Test() {
        super();
    }

    @Test
    public void testImportVCard1() {
        /*
         * import vCard
         */
        String vCard = // @formatter:off
            "BEGIN:VCARD\r\n" +
            "VERSION:4.0\r\n" +
            "EMAIL;PREF=1:marie.linan19876@example.com\r\n" +
            "FN:Marie LINAN\r\n" +
            "N:LINAN;Marie;;;\r\n" +
            "TEL;TYPE=work;VALUE=TEXT:0254786523\r\n" +
            "TEL;TYPE=cell;VALUE=TEXT:0656379123\r\n" +
            "UID:ef1c4d70-0c2f-434a-b3aa-ba782926236b\r\n" +
            "END:VCARD\r\n"
        ; // @formatter:on
        Contact contact = getMapper().importVCard(parse(vCard), null, getService().createParameters(), null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals("marie.linan19876@example.com", contact.getEmail1());
        assertNull(contact.getEmail2());
        assertNull(contact.getEmail3());
    }

    @Test
    public void testImportVCard2() {
        /*
         * import vCard
         */
        String vCard = // @formatter:off
            "BEGIN:VCARD\r\n" +
            "VERSION:4.0\r\n" +
            "EMAIL;PREF=1:paul.bosin1987@example.com\r\n" +
            "FN:Paul BOSIN\r\n" +
            "N:BOSIN;Paul;;;\r\n" +
            "TEL;TYPE=work;VALUE=TEXT:0356967534\r\n" +
            "TEL;TYPE=cell;VALUE=TEXT:0693693572\r\n" +
            "UID:e27f4994-2109-4b20-ba38-6212dcf20e61\r\n" +
            "END:VCARD\r\n"
        ; // @formatter:on
        Contact contact = getMapper().importVCard(parse(vCard), null, getService().createParameters(), null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals("paul.bosin1987@example.com", contact.getEmail1());
        assertNull(contact.getEmail2());
        assertNull(contact.getEmail3());
    }

}
