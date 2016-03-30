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

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.ajax.mail.actions.SendResponse;
import com.openexchange.ajax.mail.contenttypes.MailContentType;
import com.openexchange.exception.OXException;

/**
 * {@link SendTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - tests with manager
 */
public final class SendTest extends AbstractMailTest {

    private MailTestManager manager;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public SendTest(final String name) {
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

        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
        /*
         * Create JSON mail object
         */
        final String mailObject_25kb = createSelfAddressed25KBMailObject().toString();
        /*
         * Perform send request
         */
        final SendResponse response = Executor.execute(getSession(), new SendRequest(mailObject_25kb));
        assertTrue("Send request failed", response.getFolderAndID() != null && response.getFolderAndID().length > 0);
        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
    }

    public void testSendWithManager() throws OXException, IOException, SAXException, JSONException {
        UserValues values = client.getValues();

        TestMail mail = new TestMail();
        mail.setSubject("Test sending with manager");
        mail.setFrom(values.getSendAddress());
        mail.setTo(Arrays.asList(new String[] { values.getSendAddress() }));
        mail.setContentType(MailContentType.PLAIN.toString());
        mail.setBody("This is the message body.");
        mail.sanitize();

        TestMail inSentBox = manager.send(mail);
        assertFalse("Sending resulted in error", manager.getLastResponse().hasError());
        assertEquals("Mail went into inbox", values.getSentFolder(), inSentBox.getFolder());
    }

    /**
     * Tests the <code>action=new</code> request on INBOX folder
     *
     * @throws Throwable
     */
    public void testSendUnicode() throws Throwable {

        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
        /*
         * Create JSON mail object
         */
        final String mailObject_25kb = createSelfAddressed25KBMailObject().toString();


        JSONObject jMail = new JSONObject(mailObject_25kb);
        JSONObject jAttach = jMail.getJSONArray("attachments").getJSONObject(0);

        char a = '\uD83D';
        char b = '\uDCA9';
        String s = "Pile of poo ";

        jAttach.put("content", s + a + b);

        /*
         * Perform send request
         */
        final SendResponse response = Executor.execute(getSession(), new SendRequest(jMail.toString(true)));
        final String[] folderAndID = response.getFolderAndID();
        assertTrue("Send request failed", folderAndID != null && folderAndID.length > 0);

        if (null != folderAndID) {
            final GetResponse getResponse = Executor.execute(getSession(), new GetRequest(folderAndID[0], folderAndID[1]));

            final String content = getResponse.getAttachments().getJSONObject(0).getString("content").replaceAll(Pattern.quote("&nbsp;"), " ");
            assertTrue("Content is empty", null != content && content.length() > 0);

            int pos = content.indexOf("Pile of poo ");
            assertTrue("Content not found: \"Pile of poo \" -- Content:\n" + content, pos >= 0);

            pos += s.length();
            assertEquals("Missing \\uD83D unicode", (int) '\uD83D', (int) content.charAt(pos++));
            assertEquals("Missing \\uDCA9 unicode", (int) '\uDCA9', (int) content.charAt(pos++));
        }
        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
    }

}
