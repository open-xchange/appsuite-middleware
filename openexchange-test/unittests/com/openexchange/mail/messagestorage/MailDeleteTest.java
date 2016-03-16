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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link MailDeleteTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public final class MailDeleteTest extends MessageStorageTest {

    /**
	 *
	 */
    public MailDeleteTest() {
        super();
    }

    private static final MailField[] FIELDS_ID = { MailField.ID };

    private static final MailField[] FIELDS_EVEN_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS, MailField.FROM,
        MailField.TO, MailField.DISPOSITION_NOTIFICATION_TO, MailField.COLOR_LABEL, MailField.HEADERS, MailField.SUBJECT,
        MailField.THREAD_LEVEL, MailField.SIZE, MailField.PRIORITY, MailField.SENT_DATE, MailField.RECEIVED_DATE, MailField.CC,
        MailField.BCC, MailField.FOLDER_ID };

    public void testMailDeleteNonExistingMails() throws OXException {
        /*
         * Delete non existing mail
         */
        try {
            final long currentTimeMillis = System.currentTimeMillis();
            mailAccess.getMessageStorage().deleteMessages("INBOX", new String[] { String.valueOf(currentTimeMillis), String.valueOf(currentTimeMillis + 1) }, true);
        } catch (final Exception e) {
            fail("No Exception should be thrown here. Exception was " + e.getMessage());
        }
    }

    public void testMailDeleteNonExistingMailsMixed() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        /*
         * Delete non existing mail
         */
        try {
            final long currentTimeMillis = System.currentTimeMillis();
            mailAccess.getMessageStorage().deleteMessages("INBOX", new String[] { String.valueOf(currentTimeMillis), uids[0] }, true);

            final MailMessage message = mailAccess.getMessageStorage().getMessage("INBOX", uids[0], true);
            assertTrue("The message which should be deleted in the mixed delete test, isn't deleted.", null == message);
        } catch (final Exception e) {
            fail("No Exception should be thrown here. Exception was " + e.getMessage());
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailDeleteNonExistingFolder() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        /*
         * Delete non existing mail
         */
        try {
            mailAccess.getMessageStorage().deleteMessages("NonExistingFolder1337", uids, true);
        } catch (final OXException e) {
            assertTrue("Wrong Exception is thrown.", e.getErrorCode().endsWith("-1002"));
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);
        }
    }

    public void testMailDelete() throws OXException {
        final String[] uids = mailAccess.getMessageStorage().appendMessages("INBOX", testmessages);
        String[] trashedIDs = null;
        try {
            final String trashFullname = mailAccess.getFolderStorage().getTrashFolder();
            // MailFolder trash = mailAccess.getFolderStorage().getFolder(trashFullname);
            final int prevMessageCount = getMessageCount(mailAccess, trashFullname);

            MailMessage[] trashed = mailAccess.getMessageStorage().getAllMessages(trashFullname, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.DESC, FIELDS_ID);
            final Set<String> prevIds = new HashSet<String>();
            for (final MailMessage mail : trashed) {
                prevIds.add(mail.getMailId());
            }

            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, false);

            // trash = mailAccess.getFolderStorage().getFolder(trashFullname);
            final boolean countCap = getMessageCount(mailAccess, trashFullname) >= 0;
            if (countCap) {
                assertTrue("Trash's number of message has not been increased appropriately", prevMessageCount + uids.length == getMessageCount(mailAccess, trashFullname));
    
                trashed = mailAccess.getMessageStorage().getAllMessages(trashFullname, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.DESC, FIELDS_ID);
                assertTrue("Size mismatch: " + trashed.length + " but should be " + getMessageCount(mailAccess, trashFullname), trashed.length == getMessageCount(mailAccess, trashFullname));
                final Set<String> ids = new HashSet<String>(getMessageCount(mailAccess, trashFullname));
                for (final MailMessage mail : trashed) {
                    ids.add(mail.getMailId());
                }
                ids.removeAll(prevIds);
                assertTrue("Size mismatch: " + ids.size() + " but should be " + uids.length, ids.size() == uids.length);
    
                trashedIDs = new String[uids.length];
                {
                    int k = 0;
                    for (final String id : ids) {
                        trashedIDs[k++] = id;
                    }
                }
    
                trashed = mailAccess.getMessageStorage().getMessages(trashFullname, trashedIDs, FIELDS_EVEN_MORE);
                assertTrue("No matching trashed messages found: "
                    + (null == trashed ? "null" : String.valueOf(trashed.length))
                    + " IDs: "
                    + Arrays.toString(trashedIDs), trashed != null && trashed.length == uids.length);
                for (int i = 0; i < trashed.length; i++) {
                    assertFalse("Missing mail ID", trashed[i].getMailId() == null);
                    assertTrue("Missing content type", trashed[i].containsContentType());
                    assertTrue("Missing flags", trashed[i].containsFlags());
                    assertTrue("Missing From", trashed[i].containsFrom());
                    assertTrue("Missing To", trashed[i].containsTo());
                    assertTrue("Missing Disposition-Notification-To", trashed[i].containsDispositionNotification());
                    assertTrue("Missing color label", trashed[i].containsColorLabel());
                    assertTrue("Missing headers", trashed[i].containsHeaders());
                    assertTrue("Missing subject", trashed[i].containsSubject());
                    assertTrue("Missing thread level", trashed[i].containsThreadLevel());
                    assertTrue("Missing size", trashed[i].containsSize());
                    assertTrue("Missing priority", trashed[i].containsPriority());
                    assertTrue("Missing sent date", trashed[i].containsSentDate());
                    assertTrue("Missing received date", trashed[i].containsReceivedDate());
                    assertTrue("Missing Cc", trashed[i].containsCc());
                    assertTrue("Missing Bcc", trashed[i].containsBcc());
                    assertTrue("Missing folder fullname", trashed[i].containsFolder());
                }
            }
        } finally {
            mailAccess.getMessageStorage().deleteMessages("INBOX", uids, true);

            if (trashedIDs != null) {
                mailAccess.getMessageStorage().deleteMessages(mailAccess.getFolderStorage().getTrashFolder(), trashedIDs, true);
            }
        }
    }

}
