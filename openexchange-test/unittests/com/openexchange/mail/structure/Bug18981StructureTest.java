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
 * {@link Bug18981StructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug18981StructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link Bug18981StructureTest}.
     */
    public Bug18981StructureTest() {
        super();
    }

    /**
     * Initializes a new {@link Bug18981StructureTest}.
     *
     * @param name The test name
     */
    public Bug18981StructureTest(final String name) {
        super(name);
    }

    private static final byte[] SOURCE = ("Reply-To: <orderstatus_notification@barfoo.com>\n" +
    		"From: <orderstatus_notification@barfoo.com>\n" +
    		"To: <foobar@dotcom.de>, \n" +
    		"    <>, \n" +
    		"    <>, \n" +
    		"Cc: <>, \n" +
    		"    <blubber@barfoo.com>, \n" +
    		"Subject: Ihre Bestellung bei Foobar - Voraussichtliches Lieferdatum \n" +
    		"Date: Sat, 16 Apr 2011 06:16:43 +0100\n" +
    		"Message-ID: 129474046034864895@E3BUSOPSDB\n" +
    		"MIME-Version: 1.0\n" +
    		"Content-Type: multipart/related;\n" +
    		"    boundary=\"----=_NextPart_000_0001_0ce908fe.cc7a5096\"\n" +
    		"X-Priority: 3 (Normal)\n" +
    		"X-MSMail-Priority: Normal\n" +
    		"X-Mailer: SuperMailer V1.0\n" +
    		"Importance: Normal\n" +
    		"\n" +
    		"This is a multi-partmessage in MIME  format.\n" +
    		"\n" +
    		"------=_NextPart_000_0001_0ce908fe.cc7a5096\n" +
    		"Content-Type: multipart/alternative;\n" +
    		"    boundary=\"----=_NextPart_001_0002_0ce908fe.cc7a5096\"\n" +
    		"\n" +
    		"\n" +
    		"------=_NextPart_001_0002_0ce908fe.cc7a5096\n" +
    		"Content-Type: text/plain;\n" +
    		"    charset=\"ISO-8859-1\"\n" +
    		"Content-Transfer-Encoding: 7bit\n" +
    		"\n" +
    		"Some text\n" +
    		"\n" +
    		"\n" +
    		"------=_NextPart_001_0002_0ce908fe.cc7a5096\n" +
    		"Content-Type: text/html;\n" +
    		"    charset=\"ISO-8859-1\"\n" +
    		"Content-Transfer-Encoding: 7bit\n" +
    		"\n" +
    		"<html><body>Some text<br><br></body></html>\n" +
    		"\n" +
    		"------=_NextPart_001_0002_0ce908fe.cc7a5096--\n" +
    		"\n" +
    		"------=_NextPart_000_0001_0ce908fe.cc7a5096--").getBytes();

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
            assertTrue("Unexpected array length: " + jsonBodyArray.length(), jsonBodyArray.length() == 1);

            final JSONObject headers = jsonMailObject.getJSONObject("headers");

            assertTrue("Found more than one \"To\" header: " + headers.get("to"), 1 == headers.getJSONArray("to").length());
            assertTrue("Found more than one \"Cc\" header: " + headers.get("cc"), 1 == headers.getJSONArray("cc").length());

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
