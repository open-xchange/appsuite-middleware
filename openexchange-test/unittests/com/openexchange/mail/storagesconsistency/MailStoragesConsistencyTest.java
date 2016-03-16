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

package com.openexchange.mail.storagesconsistency;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailStoragesConsistencyTest} - This test class checks if changes made by folder storage are notified by corresponding message
 * storage, so that both storages reflects the same view on mailing system.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailStoragesConsistencyTest extends AbstractMailTest {

    private static final String TEMPORARY_FOLDER = "TemporaryFolder";

    private static final String INBOX = "INBOX";

    private static final MailField[] FIELDS_ID = { MailField.ID };

    /**
	 *
	 */
    public MailStoragesConsistencyTest() {
        super();
    }

    /**
     * @param name
     */
    public MailStoragesConsistencyTest(final String name) {
        super(name);
    }

    public void testMailStoragesConsistency1() {
        try {
            final SessionObject session = getSession();

            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();

            String fullname = null;
            try {
                String parentFullname = null;
                {
                    final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
                    if (inbox.isHoldsFolders()) {
                        fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(TEMPORARY_FOLDER).toString();
                        parentFullname = INBOX;
                    } else {
                        fullname = TEMPORARY_FOLDER;
                        parentFullname = MailFolder.DEFAULT_FOLDER_ID;
                    }

                    final MailFolderDescription mfd = new MailFolderDescription();
                    mfd.setExists(false);
                    mfd.setParentFullname(parentFullname);
                    mfd.setSeparator(inbox.getSeparator());
                    mfd.setSubscribed(false);
                    mfd.setName(TEMPORARY_FOLDER);

                    final MailPermission p = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID).createNewMailPermission(session, MailAccount.DEFAULT_ID);
                    p.setEntity(getUser());
                    p.setAllPermission(
                        OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION);
                    p.setFolderAdmin(true);
                    p.setGroupPermission(false);
                    mfd.addPermission(p);
                    mailAccess.getFolderStorage().createFolder(mfd);
                }
                /*
                 * Touch folder by message storage
                 */
                final String[] uids = mailAccess.getMessageStorage().appendMessages(fullname, getMessages(getTestMailDir(), -1));
                /*
                 * This copy operation on the same folder causes inconsistencies in java mail
                 */
                mailAccess.getMessageStorage().copyMessages(fullname, fullname, uids, true);
                /*
                 * Delete folder by folder storage
                 */
                mailAccess.getFolderStorage().deleteFolder(fullname, true);
                /*
                 * Check if folder storage's modification has been reported to message storage
                 */
                try {
                    mailAccess.getMessageStorage().getAllMessages(fullname, IndexRange.NULL, null, null, FIELDS_ID);
                } catch (final OXException e) {
                    if (e.getCause() != null) {
                        e.printStackTrace();
                        fail("Folder/message storage inconsistency detected: " + e.getCause().getMessage());
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    fail("Folder/message storage inconsistency detected: " + e.getMessage());
                } finally {
                    fullname = null;
                }

            } finally {
                if (fullname != null && mailAccess.getFolderStorage().exists(fullname)) {
                    mailAccess.getFolderStorage().deleteFolder(fullname, true);
                }

                mailAccess.close(false);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testMailStoragesConsistency2() {
        try {
            final SessionObject session = getSession();

            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();

            String fullname = null;
            String[] trashedIds = null;
            try {
                String parentFullname = null;
                {
                    final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
                    if (inbox.isHoldsFolders()) {
                        fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(TEMPORARY_FOLDER).toString();
                        parentFullname = INBOX;
                    } else {
                        fullname = TEMPORARY_FOLDER;
                        parentFullname = MailFolder.DEFAULT_FOLDER_ID;
                    }

                    final MailFolderDescription mfd = new MailFolderDescription();
                    mfd.setExists(false);
                    mfd.setParentFullname(parentFullname);
                    mfd.setSeparator(inbox.getSeparator());
                    mfd.setSubscribed(false);
                    mfd.setName(TEMPORARY_FOLDER);

                    final MailPermission p = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID).createNewMailPermission(session, MailAccount.DEFAULT_ID);
                    p.setEntity(getUser());
                    p.setAllPermission(
                        OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION);
                    p.setFolderAdmin(true);
                    p.setGroupPermission(false);
                    mfd.addPermission(p);
                    mailAccess.getFolderStorage().createFolder(mfd);
                }
                final String[] uids = mailAccess.getMessageStorage().appendMessages(fullname, getMessages(getTestMailDir(), -1));

                /*
                 * Touch trash folder by message storage
                 */
                final String trashFullname = mailAccess.getFolderStorage().getTrashFolder();

                final int numTrashedMails = getMessageCount(mailAccess, trashFullname);
                if (numTrashedMails >= 0) {
                    MailMessage[] trashed =
                        mailAccess.getMessageStorage().getAllMessages(
                            trashFullname,
                            IndexRange.NULL,
                            MailSortField.RECEIVED_DATE,
                            OrderDirection.ASC,
                            FIELDS_ID);
                    assertTrue("Size mismatch: " + trashed.length + " but should be " + numTrashedMails, trashed.length == numTrashedMails);
                    final Set<String> oldIds = new HashSet<String>(numTrashedMails);
                    for (int i = 0; i < trashed.length; i++) {
                        oldIds.add(trashed[i].getMailId());
                    }
                    /*
                     * Alter trash's content through clearing temporary folder by folder storage
                     */
                    mailAccess.getFolderStorage().clearFolder(fullname);
                    /*
                     * Check if folder storage's modification has been reported to message storage
                     */
                    if (!getUserSettingMail().isHardDeleteMsgs()) {
                        final int expectedMsgCount = numTrashedMails + uids.length;
                        assertEquals("Mails not completely moved to trash", expectedMsgCount, getMessageCount(mailAccess, trashFullname));
                        trashed =
                            mailAccess.getMessageStorage().getAllMessages(
                                trashFullname,
                                IndexRange.NULL,
                                MailSortField.RECEIVED_DATE,
                                OrderDirection.ASC,
                                FIELDS_ID);
                        assertTrue(
                            "Size mismatch: " + trashed.length + " but should be " + expectedMsgCount,
                            trashed.length == expectedMsgCount);

                        final Set<String> newIds = new HashSet<String>(numTrashedMails);
                        for (int i = 0; i < trashed.length; i++) {
                            newIds.add(trashed[i].getMailId());
                        }
                        newIds.removeAll(oldIds);

                        trashedIds = new String[newIds.size()];
                        int i = 0;
                        for (final String id : newIds) {
                            trashedIds[i++] = id;
                        }
                        assertTrue("Number of new trash mails does not match trashed mails", trashedIds.length == uids.length);
                    }
                }

            } finally {
                if (fullname != null && mailAccess.getFolderStorage().exists(fullname)) {
                    mailAccess.getFolderStorage().deleteFolder(fullname, true);
                }

                if (trashedIds != null) {
                    mailAccess.getMessageStorage().deleteMessages(mailAccess.getFolderStorage().getTrashFolder(), trashedIds, true);
                }

                mailAccess.close(false);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testMailStoragesConsistency3() {
        try {
            final SessionObject session = getSession();

            String fullname = null;

            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();
            try {
                // Append a message to a newly created folder
                String parentFullname = null;
                {
                    final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
                    if (inbox.isHoldsFolders()) {
                        fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(TEMPORARY_FOLDER).toString();
                        parentFullname = INBOX;
                    } else {
                        fullname = TEMPORARY_FOLDER;
                        parentFullname = MailFolder.DEFAULT_FOLDER_ID;
                    }

                    final MailFolderDescription mfd = new MailFolderDescription();
                    mfd.setExists(false);
                    mfd.setParentFullname(parentFullname);
                    mfd.setSeparator(inbox.getSeparator());
                    mfd.setSubscribed(false);
                    mfd.setName(TEMPORARY_FOLDER);

                    final MailPermission p = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID).createNewMailPermission(session, MailAccount.DEFAULT_ID);
                    p.setEntity(getUser());
                    p.setAllPermission(
                        OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION);
                    p.setFolderAdmin(true);
                    p.setGroupPermission(false);
                    mfd.addPermission(p);
                    mailAccess.getFolderStorage().createFolder(mfd);
                }
                final MailMessage appendMe = getMessages(getTestMailDir(), 1)[0];
                final String uid = mailAccess.getMessageStorage().appendMessages(fullname, new MailMessage[] { appendMe })[0];

                // Check that UID is valid
                assertNotSame(
                    "ID returned by MailMessageStorage.appendMessages() is invalid: " + Long.valueOf(uid),
                    Long.valueOf(-1),
                    Long.valueOf(uid));

                // Get that message by UID from folder
                final MailMessage mm = mailAccess.getMessageStorage().getMessage(fullname, uid, true);
                assertNotNull("Returned MailMessage object from MailMessageStorage.getMessage() is null but shouldn't.", mm);
            } finally {
                if (fullname != null && mailAccess.getFolderStorage().exists(fullname)) {
                    mailAccess.getFolderStorage().deleteFolder(fullname, true);
                }

                mailAccess.close(false);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
