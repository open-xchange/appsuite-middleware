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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailMultipartMixedStructureTest} - Test for output of structured JSON mail object.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailMultipartMixedStructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link MailMultipartMixedStructureTest}.
     */
    public MailMultipartMixedStructureTest() {
        super();
    }

    /**
     * Initializes a new {@link MailMultipartMixedStructureTest}.
     * 
     * @param name The test name
     */
    public MailMultipartMixedStructureTest(final String name) {
        super(name);
    }

    

    private static final byte[] MP_MIXED = ("Date: Sat, 14 Nov 2009 17:34:32 +0100 (CET)\n" + 
    		"From: alice@foobar.com\n" + 
    		"To: bob@foobar.com\n" + 
    		"Message-ID: <1043855276.4621.1258216472739.JavaMail.foobar@foobar.com>\n" + 
    		"Subject: Mail subject\n" + 
    		"MIME-Version: 1.0\n" + 
    		"Content-Type: multipart/mixed; boundary=\"----=_Part_4619_202988661.1258216472662\"\n" + 
    		"X-Priority: 3\n" + 
    		"\n" + 
    		"------=_Part_4619_202988661.1258216472662\n" + 
    		"Content-Type: multipart/alternative;  boundary=\"----=_Part_4620_1426393991.1258216472662\"\n" + 
    		"\n" + 
    		"------=_Part_4620_1426393991.1258216472662\n" + 
    		"MIME-Version: 1.0\n" + 
    		"Content-Type: text/plain; charset=UTF-8\n" + 
    		"Content-Transfer-Encoding: 7bit\n" + 
    		"\n" + 
    		"Some text here.\n" + 
    		"\n" + 
    		"------=_Part_4620_1426393991.1258216472662\n" + 
    		"MIME-Version: 1.0\n" + 
    		"Content-Type: text/html; charset=UTF-8\n" + 
    		"Content-Transfer-Encoding: 7bit\n" + 
    		"\n" + 
    		"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + 
    		"\n" + 
    		"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + 
    		"  <head>\n" + 
    		"    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\" />\n" + 
    		"    <title></title>\n" + 
    		"  </head>\n" + 
    		"\n" + 
    		"  <body>\n" + 
    		"    <p style=\"margin: 0px;\">Some text here.<span></span></p>\n" + 
    		"\n" + 
    		"    <p style=\"margin: 0px;\">&#160;</p>\n" + 
    		"  </body>\n" + 
    		"</html>\n" + 
    		"\n" + 
    		"------=_Part_4620_1426393991.1258216472662--\n" + 
    		"\n" + 
    		"------=_Part_4619_202988661.1258216472662\n" + 
    		"Content-Type: image/png; name=ox-logo.png\n" + 
    		"Content-Transfer-Encoding: base64\n" + 
    		"Content-Disposition: ATTACHMENT; filename=ox-logo.png\n" + 
    		"\n" + 
    		"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\n" + 
    		"+c5snl7EVltttUaMuOuuu6oXXnhBrrvuOh544AFeffXVJo0wECbKwwO77bYbw4YNQ2tNrp0GQm9/\n" + 
    		"+9v5z//8T6y17LDDDiv1hWbMmMH73/9+tNaccMIJHHroof3e1L333ls9/fTTct5557F06VJEhIsu\n" + 
    		"uogLL7yQ2267TX3/+9+XO++8k0WLFhWar5waWL7YIsKmm27K7rvvztFHH13EgXfddVe6u7txzrHT\n" + 
    		"TjsNeM3bbbcde+yxB8YYttpqKx5++GG23HJLtXjx4q9ec801U+6//36WLVs2oBS3MjPutNNO3Hnn\n" + 
    		"ney222684Q1vKFLoBkpvfetb2WOPPQDYfffd+fOf/8xuu+2m5s6dK9deey2PPPJIsZflJIgcLc2R\n" + 
    		"YmstW221FXvuuSdHHnnkZZttttmZAB/84AfV3Llz5Yc//CFPPPFEEfpalQ9eTrncZJNNaFOb2tSm\n" + 
    		"NrWpTW1qU5va1KY2talNbWpTm9rUpja1qU1talOb2tSmNrWpTW1qU5va1KY2talNbWpTm9rUpja1\n" + 
    		"qU1tatOa0v8HOZSaeq+Kbh4AAAAASUVORK5CYII=\n" + 
    		"------=_Part_4619_202988661.1258216472662--\n").getBytes();

    public void testMIMEStructure() {
        try {
            final SessionObject session = getSession();

            final MailMessage mail = MIMEMessageConverter.convertMessage(MP_MIXED);

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
                    assertTrue("First body part is not multipart/alternative.", contentType.getString("type").startsWith("multipart/alternative"));
                    checkMultipartAlternative(bodyPartObject);
                } else {
                    assertTrue("Second body part is not an image.", contentType.getString("type").startsWith("image/"));
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static void checkMultipartAlternative(final JSONObject multipartAlternativeObject) throws JSONException {
        final JSONArray bodyArray;
        {
            final Object bodyObject = multipartAlternativeObject.opt("body");
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
    }
    
}
