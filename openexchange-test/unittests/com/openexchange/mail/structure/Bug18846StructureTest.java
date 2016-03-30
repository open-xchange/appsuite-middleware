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

package com.openexchange.mail.structure;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link Bug18846StructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug18846StructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link Bug18846StructureTest}.
     */
    public Bug18846StructureTest() {
        super();
    }

    /**
     * Initializes a new {@link Bug18846StructureTest}.
     *
     * @param name The test name
     */
    public Bug18846StructureTest(final String name) {
        super(name);
    }

    private static final byte[] SOURCE = ("Date: Wed, 18 May 2011 12:05:12 +0200 (CEST)\n" +
        "From: me@somewhere.com\n" +
        "To: you@another.org\n" +
        "Message-ID: <1003957407.805.1305713112926>\n" +
        "Subject: Bug #18846 Test\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: multipart/mixed; \n" +
        "    boundary=\"----=_Part_803_1839345999.1305713112873\"\n" +
        "X-Priority: 3\n" +
        "Importance: Medium\n" +
        "\n" +
        "------=_Part_803_1839345999.1305713112873\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: text/plain; charset=UTF-8\n" +
        "Content-Transfer-Encoding: quoted-printable\n" +
        "\n" +
        "Some mail text.\n" +
        "\n" +
        "------=_Part_803_1839345999.1305713112873\n" +
        "Content-Type: message/rfc822; name=part.eml\n" +
        "Content-Disposition: INLINE; filename=part.eml\n" +
        "\n" +
        "Content-Type: message/rfc822\n" +
        "\n" +
        "Date: Tue, 26 Apr 2011 09:29:24 +0200 (CEST)\n" +
        "From: jane@bar.com\n" +
        "To: christine@domain.com\n" +
        "Message-ID: <675680366.104.1303802964881>\n" +
        "Subject: blabla\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: text/plain; charset=UTF-8\n" +
        "X-Priority: 3\n" +
        "Importance: Medium\n" +
        "\n" +
        "lalalalalalala\n" +
        "\n" +
        "------=_Part_803_1839345999.1305713112873--\n").getBytes();

    public void testMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SOURCE);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            final JSONArray jsonBodyArray;
            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                assertTrue("Body object is not a JSON array.", (bodyObject instanceof JSONArray));
                jsonBodyArray = (JSONArray) bodyObject;
            }
            assertTrue("Unexpected array length.", jsonBodyArray.length() == 2);

            final JSONObject bodyPart = jsonBodyArray.getJSONObject(1);
            {
                final String contentType = bodyPart.getJSONObject("headers").getJSONObject("content-type").getString("type");
                assertEquals("Unexpected Content-Type: " + contentType, "message/rfc822", contentType);
            }
            final JSONObject nestedMail = bodyPart.getJSONObject("body");
            {
                final String contentType = nestedMail.getJSONObject("headers").getJSONObject("content-type").getString("type");
                assertEquals("Unexpected Content-Type: " + contentType, "message/rfc822", contentType);
            }
            final JSONObject nestedNestedMail = nestedMail.getJSONObject("body");
            {
                final String contentType = nestedNestedMail.getJSONObject("headers").getJSONObject("content-type").getString("type");
                assertEquals("Unexpected Content-Type: " + contentType, "text/plain", contentType);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
