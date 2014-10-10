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

package com.openexchange.mail.mime;

import javax.mail.internet.InternetAddress;
import junit.framework.TestCase;


/**
 * {@link QuotedInternetAddressTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
public class QuotedInternetAddressTest extends TestCase {
    /**
     * Initializes a new {@link QuotedInternetAddressTest}.
     */
    public QuotedInternetAddressTest() {
        super();
    }

    public void testBug33552() throws Exception {
        String s = "Foo \u00e0 Bar <foo@bar.info>, =?UTF-8?Q?Foo_=C3=A0_Bar_=3Cfoo=40bar=2Einfo=3E?=, \"Foo, Bar\" <foo@bar.info>";
        InternetAddress[] parsed = QuotedInternetAddress.parse(s);

        assertEquals("Display name does not equals \"Foo \u00e0 Bar\"", "Foo \u00e0 Bar", parsed[0].getPersonal());
        assertEquals("Address does not equals \"foo@bar.info\"", "foo@bar.info", parsed[0].getAddress());

        assertEquals("Display name does not equals \"Foo \u00e0 Bar\"", "Foo \u00e0 Bar", parsed[1].getPersonal());
        assertEquals("Address does not equals \"foo@bar.info\"", "foo@bar.info", parsed[1].getAddress());

        assertEquals("Display name does not equals \"Foo, Bar\"", "Foo, Bar", parsed[2].getPersonal());
        assertEquals("Address does not equals \"foo@bar.info\"", "foo@bar.info", parsed[2].getAddress());
    }

    public void testBug33305() throws Exception {
        QuotedInternetAddress a = new QuotedInternetAddress("\u00d6tt\u00f6 <stark@wie-die-wutz.de>");
        assertEquals("Unexpected personal", "\u00d6tt\u00f6", a.getPersonal());
        assertEquals("Unexpected mail-safe form", "=?UTF-8?B?w5Z0dMO2?= <stark@wie-die-wutz.de>", a.toString());
        assertEquals("Unexpected unicode form", "\u00d6tt\u00f6 <stark@wie-die-wutz.de>", a.toUnicodeString());

        InternetAddress[] parsed = QuotedInternetAddress.parse("\u00d6tt\u00f6 <stark@wie-die-wutz.de>, Foo \u00e0 Bar <foo@bar.info>");
        assertEquals("Unexpected personal", "\u00d6tt\u00f6", parsed[0].getPersonal());
        assertEquals("Unexpected mail-safe form", "=?UTF-8?B?w5Z0dMO2?= <stark@wie-die-wutz.de>", parsed[0].toString());
        assertEquals("Unexpected unicode form", "\u00d6tt\u00f6 <stark@wie-die-wutz.de>", parsed[0].toUnicodeString());

        assertEquals("Unexpected personal", "Foo \u00e0 Bar", parsed[1].getPersonal());
        assertEquals("Unexpected mail-safe form", "=?UTF-8?Q?Foo_=C3=A0_Bar?= <foo@bar.info>", parsed[1].toString());
        assertEquals("Unexpected unicode form", "Foo \u00e0 Bar <foo@bar.info>", parsed[1].toUnicodeString());
    }

    public void testBug34070() throws Exception {
        String s = "=?windows-1252?Q?Betz=2C_C=E4cilia?= <caecilia.betz@invalid.org>";
        QuotedInternetAddress addr = new QuotedInternetAddress(s);

        assertEquals("Display name does not match \"Betz, C\u00e4cilia\"", "Betz, C\u00e4cilia", addr.getPersonal());
        assertEquals("Address does not match \"caecilia.betz@open-xchange.com\"", "caecilia.betz@invalid.org", addr.getAddress());

    }

    public void testBug34755() throws Exception {
        String s = "=?windows-1252?Q?Kr=F6ning=2C_User?= <user4@ox.microdoc.de>";
        QuotedInternetAddress addr = new QuotedInternetAddress(s);

        assertEquals("Display name does not match \"Kr\u00f6ning, User\"", "Kr\u00f6ning, User", addr.getPersonal());
        assertEquals("Address does not match \"user4@ox.microdoc.de\"", "user4@ox.microdoc.de", addr.getAddress());

    }

}
