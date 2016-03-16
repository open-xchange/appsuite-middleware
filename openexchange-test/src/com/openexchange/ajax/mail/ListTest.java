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

import org.slf4j.Logger;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.ListRequest;
import com.openexchange.ajax.mail.actions.NewMailRequest;
import com.openexchange.mail.MailListField;

/**
 * {@link ListTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ListTest extends AbstractMailTest {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ListTest.class);

    /**
     * Default constructor.
     *
     * @param name Name of this test.
     */
    public ListTest(final String name) {
        super(name);
    }

    /**
     * Tests the <code>action=list</code> request on INBOX folder
     *
     * @throws Throwable
     */
    public void testList() throws Throwable {
        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
        /*
         * Create mail
         */
        final String eml =
            ("Message-Id: <4A002517.4650.0059.1@foobar.com>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: #ADDR#\n" +
            "To: #ADDR#\n" +
            "Subject: Invitation for launch\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"UTF-8\"\n" +
            "Content-Transfer-Encoding: 8bit\n" +
            "\n" +
            "This is a MIME message. If you are reading this text, you may want to \n" +
            "consider changing to a mail reader or gateway that understands how to \n" +
            "properly handle MIME multipart messages.").replaceAll("#ADDR#", getSendAddress());
        /*
         * Insert mails
         */
        final int numOfMails = 25;
        LOG.info("Sending " + numOfMails + " mails to fill emptied INBOX");
        for (int i = 0; i < numOfMails; i++) {
            getClient().execute(new NewMailRequest(client.getValues().getInboxFolder(), eml, -1, true));
            LOG.info("Appended " + (i + 1) + ". mail of " + numOfMails);
        }
        /*
         * Get their folder and IDs
         */
        final String[][] folderAndIDs = getFolderAndIDs(getInboxFolder());
        /*
         * Perform list request
         */
        final int[] columns = new int[COLUMNS_DEFAULT_LIST.length + 1];
        System.arraycopy(COLUMNS_DEFAULT_LIST, 0, columns, 0, COLUMNS_DEFAULT_LIST.length);
        columns[columns.length - 1] = MailListField.ACCOUNT_NAME.getField();
        final CommonListResponse response = Executor.execute(getSession(), new ListRequest(folderAndIDs, columns));
        if (response.hasError()) {
            fail(response.getException().toString());
        }
        final Object[][] array = response.getArray();
        assertNotNull("Array of list request is null.", array);
        assertEquals("List request shows different number of mails.", numOfMails, array.length);
        for (int i = 0; i < array.length; i++) {
            final Object[] fields = array[i];
            final Object accountName = fields[fields.length - 1];
            assertNotNull("Account name is null.", accountName);
        }
        /*
         * Clean everything
         */
        Executor.execute(getSession(), new DeleteRequest(folderAndIDs, true));
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
    }

    /**
     * Tests the <code>action=list</code> request on INBOX folder
     *
     * @throws Throwable
     */
    public void testListWithMimeType() throws Throwable {
        /*
         * Clean everything
         */
        clearFolder(getInboxFolder());
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
        /*
         * Create mail
         */
        final String eml =
            ("Message-Id: <4A002517.4650.0059.1@foobar.com>\n" +
            "Date: Tue, 05 May 2009 11:37:58 -0500\n" +
            "From: #ADDR#\n" +
            "To: #ADDR#\n" +
            "Subject: Invitation for launch\n" +
            "Mime-Version: 1.0\n" +
            "Content-Type: text/plain; charset=\"UTF-8\"\n" +
            "Content-Transfer-Encoding: 8bit\n" +
            "\n" +
            "This is a MIME message. If you are reading this text, you may want to \n" +
            "consider changing to a mail reader or gateway that understands how to \n" +
            "properly handle MIME multipart messages.").replaceAll("#ADDR#", getSendAddress());
        /*
         * Insert mails
         */
        final int numOfMails = 25;
        LOG.info("Sending " + numOfMails + " mails to fill emptied INBOX");
        for (int i = 0; i < numOfMails; i++) {
            getClient().execute(new NewMailRequest(client.getValues().getInboxFolder(), eml, -1, true));
            LOG.info("Appended " + (i + 1) + ". mail of " + numOfMails);
        }
        /*
         * Get their folder and IDs
         */
        final String[][] folderAndIDs = getFolderAndIDs(getInboxFolder());
        /*
         * Perform list request
         */
        final int[] columns = new int[COLUMNS_DEFAULT_LIST.length + 2];
        System.arraycopy(COLUMNS_DEFAULT_LIST, 0, columns, 0, COLUMNS_DEFAULT_LIST.length);
        columns[columns.length - 2] = MailListField.ACCOUNT_NAME.getField();
        columns[columns.length - 1] = MailListField.MIME_TYPE.getField();
        final CommonListResponse response = Executor.execute(getSession(), new ListRequest(folderAndIDs, columns));
        if (response.hasError()) {
            fail(response.getException().toString());
        }
        final Object[][] array = response.getArray();
        assertNotNull("Array of list request is null.", array);
        assertEquals("List request shows different number of mails.", numOfMails, array.length);
        for (int i = 0; i < array.length; i++) {
            final Object[] fields = array[i];
            final Object accountName = fields[fields.length - 2];
            assertNotNull("Account name is null.", accountName);
            final Object mimeType = fields[fields.length - 1];
            assertNotNull("MIME type is null.", accountName);
        }
        /*
         * Clean everything
         */
        Executor.execute(getSession(), new DeleteRequest(folderAndIDs, true));
        clearFolder(getSentFolder());
        clearFolder(getTrashFolder());
    }

}
