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
 * {@link Bug16174StructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug16174StructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link Bug16174StructureTest}.
     */
    public Bug16174StructureTest() {
        super();
    }

    /**
     * Initializes a new {@link Bug16174StructureTest}.
     *
     * @param name The test name
     */
    public Bug16174StructureTest(final String name) {
        super(name);
    }



    private static final byte[] SIMPLE = ("From: postmaster@integralis.com\n" +
    		"To: martin.kauss@open-xchange.com\n" +
    		"Date: May 21, 2010 12:11:08 PM CEST\n" +
    		"MIME-Version: 1.0\n" +
    		"Subject: Test\n" +
    		"Content-Type: text/plain; charset=UTF-8\n" +
    		"\n" +
    		"This is a text message.\n" +
    		"\n").getBytes();

    public void testMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SIMPLE);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            final JSONObject jsonBodyObject;
            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                // {"data":"This is a text message.\n\n","id":"1"}
                assertTrue("Body object is not a JSON object.", (bodyObject instanceof JSONObject));
                jsonBodyObject = (JSONObject) bodyObject;
            }

            assertTrue("Missing \"data\" key.", jsonBodyObject.hasAndNotNull("data"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
