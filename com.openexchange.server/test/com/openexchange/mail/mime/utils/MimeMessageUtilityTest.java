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

package com.openexchange.mail.mime.utils;

import junit.framework.TestCase;


/**
 * {@link MimeMessageUtilityTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class MimeMessageUtilityTest extends TestCase{

    /**
     * Initializes a new {@link MimeMessageUtilityTest}.
     */
    public MimeMessageUtilityTest() {
        super();
    }

    public void testForBug33044_SubjectUnfolding() {
        String s = "=?UTF-8?B?UG90d2llcmR6ZW5pZSB6YW3Ds3dp?=\r\n =?UTF-8?B?ZW5pYQ==?=";
        s = MimeMessageUtility.decodeEnvelopeSubject(s);

        assertEquals("Subject nor properly unfolded/decoded.", "Potwierdzenie zam\u00f3wienia", s);
    }

    public void testForBug36072_AddressUnfolding() {
        String s = "=?UTF-8?Q?Wielkoszcz=C4=99ko=C5=9Bciskowiczkiewi?= =?UTF-8?Q?cz=C3=B3wnaOm=C3=B3jbo=C5=BCejestemno=C5=BCemwie?= "
            + "=?UTF-8?Q?leznacz=C4=85cychznak=C3=B3wsi=C4=99znaczyb?= =?UTF-8?Q?oprzecie=C5=BCniemo=C5=BCeby=C4=87zbyt=C5=82atwo!?= <foo@bar.tld>";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals("Address nor properly unfolded/decoded.", "Wielkoszcz\u0119ko\u015bciskowiczkiewicz\u00f3wnaOm\u00f3jbo\u017cejestemno\u017cemwieleznacz\u0105cychznak\u00f3wsi\u0119znaczyboprzecie\u017cniemo\u017ceby\u0107zbyt\u0142atwo! "
            + "<foo@bar.tld>", s);

        //expected (a b)
        s = "=?ISO-8859-1?Q?a?= b";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals("Not properly unfolded/decoded.", "a b", s);

        //expected (ab)
        s = "=?ISO-8859-1?Q?a?= =?ISO-8859-1?Q?b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals("Not properly unfolded/decoded.", "ab", s);

        //expected (ab)
        s = "=?ISO-8859-1?Q?a?=  =?ISO-8859-1?Q?b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals("Not properly unfolded/decoded.", "ab", s);

        //expected (ab)
        s = "=?ISO-8859-1?Q?a?=\r\n" +
            "\t=?ISO-8859-1?Q?b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals("Not properly unfolded/decoded.", "ab", s);

        //expected (a b)
        s = "=?ISO-8859-1?Q?a_b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals("Not properly unfolded/decoded.", "a b", s);

        //expected (a b)
        s = "=?ISO-8859-1?Q?a?= =?ISO-8859-2?Q?_b?=";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals("Not properly unfolded/decoded.", "a b", s);

        //expected (a b)
        s = "a b";
        s = MimeMessageUtility.decodeMultiEncodedHeader(s);
        assertEquals("Not properly unfolded/decoded.", "a b", s);
    }
}
