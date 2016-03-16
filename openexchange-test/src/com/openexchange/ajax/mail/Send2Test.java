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

package com.openexchange.ajax.mail;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.mail.MailJSONField;

/**
 * {@link Send2Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - tests with manager
 */
public final class Send2Test extends AbstractMailTest {

    private MailTestManager manager;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public Send2Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = new MailTestManager(client, false);
    }

    @Override
    protected void tearDown() throws Exception {
        manager.cleanUp();
        super.tearDown();
    }

    /**
     * Tests the <code>action=new</code> request on INBOX folder
     *
     * @throws Throwable
     */
    public void testSend() throws Throwable {
        // Create JSON mail object
        final JSONObject jMail = new JSONObject(16);
        jMail.put(MailJSONField.FROM.getKey(), getSendAddress(client));
        jMail.put(MailJSONField.RECIPIENT_TO.getKey(), getSendAddress());
        jMail.put(MailJSONField.RECIPIENT_CC.getKey(), "");
        jMail.put(MailJSONField.RECIPIENT_BCC.getKey(), "");
        jMail.put(MailJSONField.SUBJECT.getKey(), "OX Guard Test");
        jMail.put(MailJSONField.PRIORITY.getKey(), "3");

        jMail.put("sendtype", "0");
        jMail.put("copy2Sent", "false");

        {
            final JSONObject jHeaders = new JSONObject(4);
            jHeaders.put("X-OxGuard",true).put("X-OxGuard-ID","45fe1029-edd1-4866-8a1a-7889b4a98bca");
            jMail.putOpt("headers", jHeaders);
        }

        {
            final JSONObject jAttachment = new JSONObject(4);
            jAttachment.put(MailJSONField.CONTENT_TYPE.getKey(), "text/html");
            jAttachment.put(MailJSONField.CONTENT.getKey(), "<table><tr>\n<td style=\"background-color:#FFFFFF; height:52px; width:100px;\">\n<span style = \"font-size:48px; font-family: Veranda; font-weight: bold; color: #6666FF;\">OX</span>\n</td><td align=\"center\" style=\"width:300px;\"><h1>Secure Email</h1></td>\n</tr>\n</table>");

            final JSONArray jAttachments = new JSONArray(1);
            jAttachments.put(jAttachment);
            jMail.put(MailJSONField.ATTACHMENTS.getKey(), jAttachments);
        }

        // Perform send request
        final SendResponse response = Executor.execute(getSession(), new SendRequest(jMail.toString()));
        final String[] folderAndID = response.getFolderAndID();

        assertNull("Send request failed", folderAndID);

    }

}
