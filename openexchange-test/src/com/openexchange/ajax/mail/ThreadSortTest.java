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

import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.groupware.search.Order;

/**
 * {@link ThreadSortTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 */
public final class ThreadSortTest extends AbstractMailTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ThreadSortTest.class);

    String mailObject_25kb;

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public ThreadSortTest(final String name) {
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
     * Tests the <code>action=all</code> request with thread-sort enabled on INBOX folder
     *
     * @throws Throwable
     */
    public void testAllThreadSort() throws Throwable {
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
            "Content-Type: text/plain; charset=\"UTF-8\"\n" +
            "Content-Transfer-Encoding: 8bit\n" +
            "\n" +
            "This is a MIME message. If you are reading this text, you may want to \n" +
            "consider changing to a mail reader or gateway that understands how to \n" +
            "properly handle MIME multipart messages.";
        for (int i = 0; i < numOfMails; i++) {
            getClient().execute(new NewMailRequest(client.getValues().getInboxFolder(), eml, -1, true));
            LOG.info("Appended " + (i + 1) + ". mail of " + numOfMails);
        }
        /*
         * Perform all request with thread-sort enabled
         */
        final AllResponse allR = Executor.execute(getSession(), new AllRequest(
            getInboxFolder(),
            COLUMNS_DEFAULT_LIST,
            0,
            Order.DESCENDING,
            true).setThreadSort(true));
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        final Object[][] array = allR.getArray();
        assertNotNull("Array of all request is null.", array);
        assertEquals("All request shows different number of mails.", numOfMails, array.length);
        assertEquals("Number of columns differs from request ones.", COLUMNS_DEFAULT_LIST.length, array[0].length);
        /*
         * Check thread-level
         */
        for (int i = 0; i < array.length; i++) {
            final Object[] inner = array[i];
            for (int j = 0; j < inner.length; j++) {
                final int threadLevel = Integer.parseInt(inner[2].toString());
                assertTrue("Illegal thread level value: " + threadLevel, threadLevel >= 0);
            }
        }
    }

}
