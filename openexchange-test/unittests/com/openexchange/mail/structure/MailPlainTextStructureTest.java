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

import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link MailPlainTextStructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailPlainTextStructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link MailPlainTextStructureTest}.
     */
    public MailPlainTextStructureTest() {
        super();
    }

    /**
     * Initializes a new {@link MailPlainTextStructureTest}.
     *
     * @param name The test name
     */
    public MailPlainTextStructureTest(final String name) {
        super(name);
    }



    private static final byte[] SIMPLE = ("Return-Path: <user4@ox.microdoc.de>\n" +
    		"Received: from ox.microdoc.de ([unix socket])\n" +
    		"                 by ox-p5 (Cyrus v2.2.13-Debian-2.2.13-10+etch2) with LMTPA;\n" +
    		"                 Sat, 14 Nov 2009 17:03:09 +0100\n" +
    		"Received: from ox-p5 (localhost [127.0.0.1])\n" +
    		"                 by ox.microdoc.de (Postfix) with ESMTP id CD65A64F41\n" +
    		"                 for <user3@ox.microdoc.de>; Sat, 14 Nov 2009 17:03:09 +0100 (CET)\n" +
    		"Date: Sat, 14 Nov 2009 17:03:09 +0100 (CET)\n" +
    		"From: user5@ox.microdoc.de\n" +
    		"To: user5@ox.microdoc.de\n" +
    		"Message-ID: <1837640730.5.1258214590077.JavaMail.foobar@foobar>\n" +
    		"Subject: The mail subject\n" +
    		"MIME-Version: 1.0\n" +
    		"Content-Type: text/plain; charset=UTF-8\n" +
    		"Content-Transfer-Encoding: 7bit\n" +
    		"X-Sieve: CMU Sieve 2.2\n" +
    		"X-Priority: 3\n" +
    		"X-Mailer: Open-Xchange Mailer v6.12.0-Rev3\n" +
    		"X-OX-Marker: 4936b5b2-d634-4c5a-b4e8-23f516a6f95e\n" +
    		"\n" +
    		"SOME-BASE64-DATA").getBytes();

    public void testMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SIMPLE);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            System.out.println(jsonMailObject.toString(2));

            final JSONObject jsonBodyObject;
            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                assertTrue("Body object is not a JSON object.", (bodyObject instanceof JSONObject));
                jsonBodyObject = (JSONObject) bodyObject;
            }

            final String id = jsonBodyObject.getString("id");
            assertEquals("Wring part ID.", "1", id);

            assertNull("Content-Tranfer-Encoding header is present, but shouldn't", jsonMailObject.getJSONObject("headers").opt(
                "content-transfer-encoding"));

            final JSONObject contentTypeJsonObject = jsonMailObject.getJSONObject("headers").getJSONObject("content-type");
            assertNotNull("Missing Content-Type header.", contentTypeJsonObject);

            assertEquals("Unexpected Content-Type.", "text/plain", contentTypeJsonObject.getString("type").toLowerCase());

            final JSONObject parameterJsonObject = contentTypeJsonObject.getJSONObject("params");
            assertEquals("Unexpected charset.", "utf-8", parameterJsonObject.getString("charset").toLowerCase());

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
