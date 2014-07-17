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

import java.io.UnsupportedEncodingException;
import javax.mail.internet.AddressException;
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

    public void testBug33552() throws AddressException, UnsupportedEncodingException {
        String s = "Foo \u00e0 Bar <foo@bar.info>, =?UTF-8?Q?Foo_=C3=A0_Bar_=3Cfoo=40bar=2Einfo=3E?=, \"Foo, Bar\" <foo@bar.info>";

        InternetAddress[] parsed = QuotedInternetAddress.parse(s);

        parsed = QuotedInternetAddress.parse(s);

        assertEquals("Display name does not equals \"Foo \u00e0 Bar\"", parsed[0].getPersonal(), "Foo \u00e0 Bar");
        assertEquals("Address does not equals \"foo@bar.info\"", parsed[0].getAddress(), "foo@bar.info");
        assertEquals("Display name does not equals \"Foo \u00e0 Bar\"", parsed[1].getPersonal(), "Foo \u00e0 Bar");
        assertEquals("Address does not equals \"foo@bar.info\"", parsed[1].getAddress(), "foo@bar.info");
        assertEquals("Display name does not equals \"Foo, Bar\"", parsed[2].getPersonal(), "Foo, Bar");
        assertEquals("Address does not equals \"foo@bar.info\"",parsed[2].getAddress(), "foo@bar.info");
    }
}
