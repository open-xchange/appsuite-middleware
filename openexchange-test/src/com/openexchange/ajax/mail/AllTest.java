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

import javax.mail.internet.InternetAddress;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link AllTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 */
public final class AllTest extends AbstractMailTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AllTest.class);

    String mailObject_25kb;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public AllTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());

        /*
         * Create JSON mail object
         */
        mailObject_25kb = createSelfAddressed25KBMailObject().toString();
    }

    @Override
    public void tearDown() throws Exception {
        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
        super.tearDown();
    }

    /**
     * Tests the <code>action=all</code> request on INBOX folder
     *
     * @throws Throwable
     */
    public void testAll() throws Throwable {
        /*
         * Insert <numOfMails> mails through a send request
         */
        final int numOfMails = 25;
        LOG.info("Sending " + numOfMails + " mails to fill emptied INBOX");
        final String eml =
            "Message-Id: <4A002517.4650.0059.1@foobar.com>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: " + getSendAddress() + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Subject: Invitation for launch\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"US-ASCII\"\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n" +
            "Blah blah blah blah blah blah";
        for (int i = 0; i < numOfMails; i++) {
            getClient().execute(new NewMailRequest(client.getValues().getInboxFolder(), eml, -1, true));
            LOG.info("Appended " + (i + 1) + ". mail of " + numOfMails);
        }
        /*
         * Perform all request
         */
        final AllResponse allR = Executor.execute(getSession(), new AllRequest(getInboxFolder(), COLUMNS_DEFAULT_LIST, 0, null, true));
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        final Object[][] array = allR.getArray();
        assertNotNull("Array of all request is null.", array);
        assertEquals("All request shows different number of mails.", numOfMails, array.length);
        assertEquals("Number of columns differs from request ones.", COLUMNS_DEFAULT_LIST.length, array[0].length);
    }

    /**
     * Tests the <code>action=all</code> request on INBOX folder
     *
     * @throws Throwable
     */
    public void testAllLimit() throws Throwable {
        /*
         * Insert <numOfMails> mails through a send request
         */
        final int numOfMails = 25;
        LOG.info("Sending " + numOfMails + " mails to fill emptied INBOX");
        final String eml =
            "Message-Id: <4A002517.4650.0059.1@foobar.com>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: " + getSendAddress() + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Subject: Invitation for launch\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"US-ASCII\"\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n" +
            "Blah blah blah blah blah blah";
        for (int i = 0; i < numOfMails; i++) {
            getClient().execute(new NewMailRequest(client.getValues().getInboxFolder(), eml, -1, true));
            LOG.info("Appended " + (i + 1) + ". mail of " + numOfMails);
        }
        /*
         * Perform all request
         */
        final int left = 0;
        final int right = 10;
        final AllRequest allRequest = new AllRequest(getInboxFolder(), COLUMNS_DEFAULT_LIST, 0, null, true);
        allRequest.setLeftHandLimit(left);
        allRequest.setRightHandLimit(right);
        final AllResponse allR = Executor.execute(getSession(), allRequest);
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        final Object[][] array = allR.getArray();
        assertNotNull("Array of all request is null.", array);
        assertEquals("All request shows different number of mails.", (right - left), array.length);
        assertEquals("Number of columns differs from request ones.", COLUMNS_DEFAULT_LIST.length, array[0].length);
    }

    public void testAllResponseGetMailObjects() throws Exception {

        /*
         * Insert <numOfMails> mails through a send request
         */
        final int numOfMails = 5;
        LOG.info("Sending " + numOfMails + " mails to fill emptied INBOX");
        final String eml =
            "Message-Id: <4A002517.4650.0059.1@foobar.com>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: " + getSendAddress() + "\n" +
            "To: " + getSendAddress() + "\n" +
            "Subject: Invitation for launch\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"US-ASCII\"\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "\n" +
            "Blah blah blah blah blah blah";
        for (int i = 0; i < numOfMails; i++) {
            getClient().execute(new NewMailRequest(client.getValues().getInboxFolder(), eml, -1, true));
            LOG.info("Appended " + (i + 1) + ". mail of " + numOfMails);
        }

        final AllResponse allR = Executor.execute(getSession(), new AllRequest(getInboxFolder(), COLUMNS_DEFAULT_LIST, 0, null, true));
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        final MailMessage[] mailMessages = allR.getMailMessages(COLUMNS_DEFAULT_LIST);
        for (final MailMessage mailMessage : mailMessages) {
            assertEquals("From is not equal", new InternetAddress(getSendAddress()), mailMessage.getFrom()[0]);
            assertEquals("Subject is not equal", "Invitation for launch", mailMessage.getSubject());
            assertEquals("Folder is not equal", getInboxFolder(), mailMessage.getFolder());
            assertEquals("hasAttachment is not equal", false, mailMessage.hasAttachment());
            assertEquals("To is not equal", new InternetAddress(getSendAddress()), mailMessage.getTo()[0]);
        }
    }

}
