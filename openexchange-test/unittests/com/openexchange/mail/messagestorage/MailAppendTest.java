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

package com.openexchange.mail.messagestorage;

import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MessageHeaders;

/**
 * This test class tests the append and the getMessage(s) Operations as they belong together.
 *
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Ignore ("Causes OOM on client")
public final class MailAppendTest extends MessageStorageTest {

    private static final String INBOX = "INBOX";

    private String[] uids = null;

    private static MailField[][] variations = null;

    private final MailField[] fieldWithoutUidFolderAndFlags = {
        MailField.CONTENT_TYPE, MailField.FROM, MailField.TO, MailField.CC, MailField.BCC, MailField.SUBJECT,
        MailField.SIZE, MailField.SENT_DATE, MailField.THREAD_LEVEL, MailField.DISPOSITION_NOTIFICATION_TO,
        MailField.PRIORITY, MailField.COLOR_LABEL, MailField.HEADERS, MailField.BODY };

    private final MailField[] fieldsfull = { MailField.FULL };

    static {
        variations = generateVariations();
    }
    /**
	 *
	 */
    public MailAppendTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    // First the basic tests so that a mail can be appended and deleted
    public void testMailAppendAndDeleteMails() throws OXException {
        this.uids = this.mailAccess.getMessageStorage().appendMessages(INBOX, testmessages);
        mailAccess.getMessageStorage().deleteMessages(INBOX, uids, true);
    }

    // Then we test if the get Methods are running correctly
    public void testMailAppendAndGetOneMessage() throws OXException {
        // At first we should test the append - get - delete operation with one mail only so that we see, that the basic functions
        // are working
        this.uids = this.mailAccess.getMessageStorage().appendMessages(INBOX, new MailMessage[]{testmessages[0]});

        try {
            final MailMessage m = mailAccess.getMessageStorage().getMessage(INBOX, uids[0], true);
            m.removeHeader(MessageHeaders.HDR_X_OX_MARKER);
            compareMailMessages(testmessages[0], m, fieldWithoutUidFolderAndFlags, fieldsfull, "test message", "fetched message", true);
        } finally {
            mailAccess.getMessageStorage().deleteMessages(INBOX, uids, true);
        }
    }

    public void testMailAppendAndGetMessage() throws OXException {
        // At first we should test the append - get - delete operation with one mail only so that we see, that the basic functions
        // are working
        this.uids = this.mailAccess.getMessageStorage().appendMessages(INBOX, testmessages);

        try {
            for (int i = 0; i < uids.length; i++) {
                final MailMessage m = mailAccess.getMessageStorage().getMessage(INBOX, uids[i], true);
                m.removeHeader(MessageHeaders.HDR_X_OX_MARKER);
                compareMailMessages(testmessages[i], m, fieldWithoutUidFolderAndFlags, fieldsfull, "test message", "fetched message", true);
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages(INBOX, uids, true);
        }
    }

    public void testMailAppendAndGetMessages() throws OXException {
        this.uids = this.mailAccess.getMessageStorage().appendMessages(INBOX, testmessages);
        try {
            // Check only for the one values here (that is the size of the enum out of all combinations):
            for (int k = 0; k < MailField.values().length; k++) {
                final MailField[] fields = variations[k];
                final MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages(INBOX, uids, fields);
                assertTrue("The size of the uids is not equal with the size of the fetched mails which is returned from the getMessages method", fetchedMails.length == uids.length);
                for (int i = 0; i < fetchedMails.length; i++) {
                    fetchedMails[i].removeHeader(MessageHeaders.HDR_X_OX_MARKER);
                    compareMailMessages(testmessages[i], fetchedMails[i], fieldWithoutUidFolderAndFlags, fields, "test messages", "fetched messages " + i, true);
                }
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages(INBOX, uids, true);
        }
    }

    public void testMailAppendAndGetMessagesRandomFields() throws OXException {
        this.uids = this.mailAccess.getMessageStorage().appendMessages(INBOX, testmessages);
        try {
            // Check only for the one values here (that is the size of the enum out of all combinations):
            final Random random = new Random();
            final int length = MailField.values().length;
            final int nextInt = random.nextInt(variations.length - length) + length;
            final MailField[] fields = variations[nextInt];
            final MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages(INBOX, uids, fields);
            assertTrue("The size of the uids is not equal with the size of the fetched mails which is returned from the getMessages method", fetchedMails.length == uids.length);
            for (int i = 0; i < fetchedMails.length; i++) {
                fetchedMails[i].removeHeader(MessageHeaders.HDR_X_OX_MARKER);
                compareMailMessages(testmessages[i], fetchedMails[i], fieldWithoutUidFolderAndFlags, fields, "test messages", "fetched messages " + i, true);
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages(INBOX, uids, true);
        }
    }

    @After
    public void tearDown() throws Exception {
        mailAccess.close(false);
        super.tearDown();
    }
}
