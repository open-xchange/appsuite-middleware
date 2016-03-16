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
 * {@link MailUUEncodedStructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailUUEncodedStructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link MailUUEncodedStructureTest}.
     */
    public MailUUEncodedStructureTest() {
        super();
    }

    /**
     * Initializes a new {@link MailUUEncodedStructureTest}.
     *
     * @param name The test name
     */
    public MailUUEncodedStructureTest(final String name) {
        super(name);
    }

    private static final byte[] SIMPLE = ("Date: Mon, 2 Nov 2009 06:50:42 +0100 (CET)\n" +
    		"Message-Id: <200911020550.nA25oglg029683@datacom.sender.com>\n" +
    		"Subject: \n" +
    		"To: d.user@receiver.de\n" +
    		"From: datacom@sender.com\n" +
    		"\n" +
    		"Daten\n" +
    		"\n" +
    		"begin 644 uuencode-Test.txt\n" +
    		"M1V5S8VAI8VAT90T*#0I$87,@554@<W1E:'0@9OQR(&1I92!7=7)Z96QN(&EN\n" +
    		"M(%5.25@N($1A<R!552!I;B!5565N8V]D92!U;F0@+61E8V]D92!S=&5H=\"`-\n" +
    		"M\"F5B96YS;R!W:64@9&%S(%55(&)E:2!556-P(&;\\<B!53DE8('1O(%5.25@@\n" +
    		"M8V]P>2!P<F]T;V-O;\"X@06QS;R!D:64@W&)E<G1R86=U;F<@#0IV;VX@96EN\n" +
    		"M96T@54Y)6\"U#;VUP=71E<B!Z=2!E:6YE;2!A;F1E<F5N(%5.25@M0V]M<'5T\n" +
    		"%97(N#0H`\n" +
    		"`\n" +
    		"end").getBytes();

    public void testMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SIMPLE);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                assertTrue("Body object is not a JSON object.", (bodyObject instanceof JSONObject));
            }

            final JSONObject headers = jsonMailObject.getJSONObject("headers");
            final JSONObject ct = headers.getJSONObject("content-type");
            assertEquals("Unexpected content type", "text/plain", ct.get("type"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testMIMEStructureWithParsedUUEncodedParts() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SIMPLE);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().setParseUUEncodedParts(true).parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            final JSONArray jsonBodyObject;
            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                assertTrue("Body object is not a JSON array.", (bodyObject instanceof JSONArray));
                jsonBodyObject = (JSONArray) bodyObject;
            }

            final JSONObject headers = jsonMailObject.getJSONObject("headers");
            final JSONObject ct = headers.getJSONObject("content-type");
            assertEquals("Unexpected content type", "multipart/mixed", ct.get("type"));

            assertEquals("Unexpected number of parts", 2, jsonBodyObject.length());
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
