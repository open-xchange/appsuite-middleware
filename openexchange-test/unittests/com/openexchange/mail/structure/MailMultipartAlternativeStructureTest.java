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
 * {@link MailMultipartAlternativeStructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailMultipartAlternativeStructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link MailMultipartAlternativeStructureTest}.
     */
    public MailMultipartAlternativeStructureTest() {
        super();
    }

    /**
     * Initializes a new {@link MailMultipartAlternativeStructureTest}.
     *
     * @param name The test name
     */
    public MailMultipartAlternativeStructureTest(final String name) {
        super(name);
    }



    private static final byte[] MP_ALTERNATIVE = ("Date: Sat, 14 Nov 2009 17:03:09 +0100 (CET)\n" +
    		"From: alice@foobar.com\n" +
    		"To: bob@foobar.com\n" +
    		"Message-ID: <1837640730.5.1258214590077.JavaMail.foobar@foobar>\n" +
    		"Subject: The mail subject\n" +
    		"MIME-Version: 1.0\n" +
    		"Content-Type: multipart/alternative; \n" +
    		"    boundary=\"----=_Part_4_2138910064.1258214589840\"\n" +
    		"X-Priority: 3\n" +
    		"\n" +
    		"------=_Part_4_2138910064.1258214589840\n" +
    		"MIME-Version: 1.0\n" +
    		"Content-Type: text/plain; charset=UTF-8\n" +
    		"Content-Transfer-Encoding: 7bit\n" +
    		"\n" +
    		"Mail text.\n" +
    		"\n" +
    		"People have been asking for support for the IMAP IDLE command for quite\n" +
    		"a few years and I think I've finally figured out how to provide such\n" +
    		"support safely. The difficulty isn't in executing the command, which\n" +
    		"is quite straightforward, the difficulty is in deciding how to expose\n" +
    		"it to applications, and inhandling the multithreading issues that\n" +
    		"arise.\n" +
    		"\n" +
    		"------=_Part_4_2138910064.1258214589840\n" +
    		"MIME-Version: 1.0\n" +
    		"Content-Type: text/html; charset=UTF-8\n" +
    		"Content-Transfer-Encoding: 7bit\n" +
    		"\n" +
    		"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
    		"\n" +
    		"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
    		"  <head>\n" +
    		"    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\" />\n" +
    		"    <title></title>\n" +
    		"  </head>\n" +
    		"\n" +
    		"  <body>\n" +
    		"    Mail text.<br />\n" +
    		"    <br />\n" +
    		"    People have been asking for support for the IMAP IDLE command for quite<br />\n" +
    		"    a few years and I think I&#39;ve finally figured out how to provide such<br />\n" +
    		"    support safely. The difficulty isn&#39;t in executing the command, which<br />\n" +
    		"    is quite straightforward, the difficulty is in deciding how to expose<br />\n" +
    		"    it to applications, and inhandling the multithreading issues that<br />\n" +
    		"    arise.<br />\n" +
    		"    <br />\n" +
    		"  </body>\n" +
    		"</html>\n" +
    		"\n" +
    		"------=_Part_4_2138910064.1258214589840--").getBytes();

    public void testMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(MP_ALTERNATIVE);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            final JSONArray bodyArray;
            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                assertTrue("Body object is not a JSON array.", (bodyObject instanceof JSONArray));
                bodyArray = (JSONArray) bodyObject;
            }

            final int length = bodyArray.length();
            assertEquals("Expected two body parts.", 2, length);

            // System.out.println(jsonMailObject.toString(2));

            for (int i = 0; i < length; i++) {
                final JSONObject bodyPartObject = bodyArray.getJSONObject(i);
                final JSONObject contentType = bodyPartObject.getJSONObject("headers").getJSONObject("content-type");
                if (0 == i) {
                    assertTrue("First body part is not plain text.", contentType.getString("type").startsWith("text/plain"));
                } else {
                    assertTrue("Second body part is not HTML.", contentType.getString("type").startsWith("text/htm"));
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
