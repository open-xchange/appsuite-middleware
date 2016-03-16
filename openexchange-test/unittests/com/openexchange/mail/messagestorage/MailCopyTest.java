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

import com.openexchange.exception.OXException;
import java.io.IOException;
import javax.mail.MessagingException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link MailCopyTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailCopyTest extends MessageStorageTest {

    /**
	 *
	 */
    public MailCopyTest() {
        super();
    }

    private static final MailField[] FIELDS_ID = { MailField.ID };

    private static final MailField[] FIELDS_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS, MailField.BODY };

    private static final MailField[] FIELDS_EVEN_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS, MailField.FROM,
        MailField.TO, MailField.DISPOSITION_NOTIFICATION_TO, MailField.COLOR_LABEL, MailField.HEADERS, MailField.SUBJECT,
        MailField.THREAD_LEVEL, MailField.SIZE, MailField.PRIORITY, MailField.SENT_DATE, MailField.RECEIVED_DATE, MailField.CC,
        MailField.BCC, MailField.FOLDER_ID };

    private static final MailField[] FIELDS_FULL = { MailField.FULL };

    public void testMailCopyNotExistingMails() throws OXException, MessagingException, IOException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            final String fullname = createTemporaryFolder(getSession(), mailAccess);

            try {
                /*
                 * Copy not existing message to valid folder
                 */
                String[] tmpCopy = null;
                try {
                    final long currentTimeMillis = System.currentTimeMillis();
                    tmpCopy = mailAccess.getMessageStorage().copyMessages("INBOX", fullname, new String[] {
                        String.valueOf(currentTimeMillis), String.valueOf(currentTimeMillis + 1) }, false);
                } catch (final Exception e) {
                    fail("No exception should be thrown here");
                }
                assertNotNull("Move returned no IDs", tmpCopy);
                assertTrue("Method moveMessages returned wrong id at pos 0. Must be null, but was " + tmpCopy[0], tmpCopy[0] == null);
                assertTrue("Method moveMessages returned wrong id at pos 1. Must be null, but was " + tmpCopy[1], tmpCopy[1] == null);
            } finally {
                mailAccess.getFolderStorage().deleteFolder(fullname, true);
            }

        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailCopyNotExistingMailsMixed() throws OXException, MessagingException, IOException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            final String fullname = createTemporaryFolder(getSession(), mailAccess);

            try {
                /*
                 * Copy not existing message to valid folder
                 */
                String[] tmpCopy = null;
                try {
                    tmpCopy = mailAccess.getMessageStorage().copyMessages("INBOX", fullname, new String[] {
                        String.valueOf(System.currentTimeMillis()), uids[0] }, false);
                } catch (final Exception e) {
                    fail("No exception should be thrown here");
                }
                assertNotNull("Move returned no IDs", tmpCopy);
                assertTrue("Method moveMessages returned wrong id at pos 0. Must be null, but was " + tmpCopy[0], tmpCopy[0] == null);
                assertTrue("Method moveMessages returned wrong id at pos 1. Must be != null, but was " + tmpCopy[1], tmpCopy[1] != null);
            } finally {
                mailAccess.getFolderStorage().deleteFolder(fullname, true);
            }

        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailCopyToNotExistingFolder() throws OXException, MessagingException, IOException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            /*
             * Copy messages to not existing folder
             */
            final MailFolder inbox = mailAccess.getFolderStorage().getFolder("INBOX");
            final String tmpFolderName = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append("MichGibtEsNicht").toString();
            try {
                assertNull("No ids should be returned", mailAccess.getMessageStorage().copyMessages("INBOX", tmpFolderName, uids, false));
            } catch (final OXException e) {
                assertTrue("Wrong Exception is thrown.", e.getErrorCode().endsWith("-1002"));
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailCopyFromNotExistingFolder() throws OXException, MessagingException, IOException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            final String fullname = createTemporaryFolder(getSession(), mailAccess);

            try {
                /*
                 * Copy messages from not existing folder
                 */
                try {
                    assertNull("No ids should be returned", mailAccess.getMessageStorage().copyMessages("MichGibtEsHoffentlichNicht", fullname, uids, false));
                } catch (final OXException e) {
                    assertTrue("Wrong Exception is thrown.", e.getErrorCode().endsWith("-1002"));
                }
            } finally {
                mailAccess.getFolderStorage().deleteFolder(fullname, true);
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailCopy() throws OXException, MessagingException, IOException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        try {
            final String fullname = createTemporaryFolder(getSession(), mailAccess);

            try {
                final String[] copied = mailAccess.getMessageStorage().copyMessages("INBOX", fullname, uids, false);
                assertTrue("Missing copied mail IDs", copied != null);
                assertTrue("Number of copied messages does not match", copied.length == uids.length);
                for (int i = 0; i < copied.length; i++) {
                    assertTrue("Invalid mail ID", copied[i] != null);
                }

                MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages(fullname, copied, FIELDS_ID);
                for (int i = 0; i < fetchedMails.length; i++) {
                    assertFalse("Mail ID is null", fetchedMails[i].getMailId() == null);
                }

                fetchedMails = mailAccess.getMessageStorage().getMessages(fullname, copied, FIELDS_MORE);
                for (int i = 0; i < fetchedMails.length; i++) {
                    assertFalse("Missing mail ID", fetchedMails[i].getMailId() == null);
                    assertTrue("Missing content type", fetchedMails[i].containsContentType());
                    assertTrue("Missing flags", fetchedMails[i].containsFlags());
                    if (fetchedMails[i].getContentType().isMimeType("multipart/*")) {
                        assertFalse("Enclosed count returned -1", fetchedMails[i].getEnclosedCount() == -1);
                    } else {
                        assertFalse("Content is null", fetchedMails[i].getContent() == null);
                    }
                }

                fetchedMails = mailAccess.getMessageStorage().getMessages(fullname, copied, FIELDS_EVEN_MORE);
                for (int i = 0; i < fetchedMails.length; i++) {
                    assertFalse("Missing mail ID", fetchedMails[i].getMailId() == null);
                    assertTrue("Missing content type", fetchedMails[i].containsContentType());
                    assertTrue("Missing flags", fetchedMails[i].containsFlags());
                    assertTrue("Missing From", fetchedMails[i].containsFrom());
                    assertTrue("Missing To", fetchedMails[i].containsTo());
                    assertTrue("Missing Disposition-Notification-To", fetchedMails[i].containsDispositionNotification());
                    assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
                    assertTrue("Missing headers", fetchedMails[i].containsHeaders());
                    assertTrue("Missing subject", fetchedMails[i].containsSubject());
                    assertTrue("Missing thread level", fetchedMails[i].containsThreadLevel());
                    assertTrue("Missing size", fetchedMails[i].containsSize());
                    assertTrue("Missing priority", fetchedMails[i].containsPriority());
                    assertTrue("Missing sent date", fetchedMails[i].containsSentDate());
                    assertTrue("Missing received date", fetchedMails[i].containsReceivedDate());
                    assertTrue("Missing Cc", fetchedMails[i].containsCc());
                    assertTrue("Missing Bcc", fetchedMails[i].containsBcc());
                    assertTrue("Missing folder fullname", fetchedMails[i].containsFolder());
                }

                fetchedMails = mailAccess.getMessageStorage().getMessages(fullname, copied, FIELDS_FULL);
                for (int i = 0; i < fetchedMails.length; i++) {
                    assertFalse("Missing mail ID", fetchedMails[i].getMailId() == null);
                    assertTrue("Missing content type", fetchedMails[i].containsContentType());
                    assertTrue("Missing flags", fetchedMails[i].containsFlags());
                    assertTrue("Missing From", fetchedMails[i].containsFrom());
                    assertTrue("Missing To", fetchedMails[i].containsTo());
                    assertTrue("Missing Disposition-Notification-To", fetchedMails[i].containsDispositionNotification());
                    assertTrue("Missing color label", fetchedMails[i].containsColorLabel());
                    assertTrue("Missing headers", fetchedMails[i].containsHeaders());
                    assertTrue("Missing subject", fetchedMails[i].containsSubject());
                    assertTrue("Missing thread level", fetchedMails[i].containsThreadLevel());
                    assertTrue("Missing size", fetchedMails[i].containsSize());
                    assertTrue("Missing priority", fetchedMails[i].containsPriority());
                    assertTrue("Missing sent date", fetchedMails[i].containsSentDate());
                    assertTrue("Missing received date", fetchedMails[i].containsReceivedDate());
                    assertTrue("Missing Cc", fetchedMails[i].containsCc());
                    assertTrue("Missing Bcc", fetchedMails[i].containsBcc());
                    assertTrue("Missing folder fullname", fetchedMails[i].containsFolder());
                    if (fetchedMails[i].getContentType().isMimeType("multipart/*")) {
                        assertFalse("Enclosed count returned -1", fetchedMails[i].getEnclosedCount() == -1);
                    } else {
                        assertFalse("Content is null", fetchedMails[i].getContent() == null);
                    }
                }

            } finally {
                mailAccess.getFolderStorage().deleteFolder(fullname, true);
            }

        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

}
