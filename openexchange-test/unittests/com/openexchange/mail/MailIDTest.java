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

package com.openexchange.mail;

import java.io.IOException;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.dataobjects.IMAPMailFolder;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailIDTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailIDTest extends AbstractMailTest {

    private static final String INBOX = "INBOX";

    private static final String TEMP_FOLDER = "TempFolder";

    public MailIDTest() {
        super();
    }

    public MailIDTest(final String name) {
        super(name);
    }

    public void testMailID() throws OXException, MessagingException, IOException {
        final SessionObject session = getSession();
        final MailMessage[] mails = getMessages(getTestMailDir(), -1);

        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
        mailAccess.connect();
        try {
            /*
             * Create a new folder
             */
            String fullname = null;
            String parentFullname = null;
            {
                final MailFolder inbox = mailAccess.getFolderStorage().getFolder(INBOX);
                if (inbox.isHoldsFolders()) {
                    fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(
                            TEMP_FOLDER).toString();
                    parentFullname = INBOX;
                } else {
                    fullname = TEMP_FOLDER;
                    parentFullname = MailFolder.DEFAULT_FOLDER_ID;
                }

                final MailFolderDescription mfd = new MailFolderDescription();
                mfd.setExists(false);
                mfd.setParentFullname(parentFullname);
                mfd.setSeparator(inbox.getSeparator());
                mfd.setSubscribed(false);
                mfd.setName(TEMP_FOLDER);

                final MailPermission p = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID)
                        .createNewMailPermission(session, MailAccount.DEFAULT_ID);
                p.setEntity(getUser());
                p.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
                        OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                p.setFolderAdmin(true);
                p.setGroupPermission(false);
                mfd.addPermission(p);
                mailAccess.getFolderStorage().createFolder(mfd);
            }
            try {
                /*
                 * Find new folder
                 */
                boolean found = false;
                final MailFolder[] folders = mailAccess.getFolderStorage().getSubfolders(parentFullname, true);
                for (int i = 0; i < folders.length; i++) {
                    final MailFolder mf = folders[i];
                    assertTrue("Missing default folder flag", mf.containsDefaultFolder());
                    assertTrue("Missing deleted count", mf.containsDeletedMessageCount());
                    assertTrue("Missing exists flag", mf.containsExists());
                    assertTrue("Missing fullname", mf.containsFullname());
                    assertTrue("Missing holds folders flag", mf.containsHoldsFolders());
                    assertTrue("Missing holds messages flag", mf.containsHoldsMessages());
                    assertTrue("Missing message count", mf.containsMessageCount());
                    assertTrue("Missing name", mf.containsName());
                    assertTrue("Missing new message count", mf.containsNewMessageCount());
                    if (mf instanceof IMAPMailFolder) {
                        assertTrue("Missing non-existent flag", ((IMAPMailFolder) mf).containsNonExistent());
                    }
                    assertTrue("Missing own permission", mf.containsOwnPermission());
                    assertTrue("Missing parent fullname", mf.containsParentFullname());
                    assertTrue("Missing permissions", mf.containsPermissions());
                    assertTrue("Missing root folder flag", mf.containsRootFolder());
                    assertTrue("Missing separator flag", mf.containsSeparator());
                    assertTrue("Missing subfolder flag", mf.containsSubfolders());
                    assertTrue("Missing subscribed flag", mf.containsSubscribed());
                    assertTrue("Missing subscribed subfolders flag", mf.containsSubscribedSubfolders());
                    assertTrue("Missing supports user flags flag", mf.containsSupportsUserFlags());
                    assertTrue("Missing unread message count", mf.containsUnreadMessageCount());
                    if (fullname.equals(mf.getFullname())) {
                        found = true;
                        assertFalse("Subscribed, but shouldn't be", MailProperties.getInstance().isSupportSubscription() ? mf
                                .isSubscribed() : false);
                    }
                }
                assertTrue("Newly created subfolder not found!", found);
                /*
                 * Append mails to new folder
                 */
                final String[] uids = mailAccess.getMessageStorage().appendMessages(fullname, mails);
                try {
                    /*
                     * All UIDs should differ
                     */
                    for (int i = 0; i < uids.length; i++) {
                        for (int j = 0; j < uids.length; j++) {
                            if (j != i) {
                                assertTrue("ID of appended mail #" + (i + 1) + " is equal to ID of appended mail #"
                                        + (j + 1), uids[i] != uids[j]);
                            }
                        }
                    }
                } finally {
                    /*
                     * Delete appended mails
                     */
                    mailAccess.getMessageStorage().deleteMessages(fullname, uids, true);
                }
                /*
                 * Append again
                 */
                final String[] uids2 = mailAccess.getMessageStorage().appendMessages(fullname, mails);
                try {
                    /*
                     * All UIDs should differ
                     */
                    for (int i = 0; i < uids2.length; i++) {
                        for (int j = 0; j < uids2.length; j++) {
                            if (j != i) {
                                assertTrue("ID of appended mail #" + (i + 1) + " is equal to ID of appended mail #"
                                        + (j + 1), uids2[i] != uids2[j]);
                            }
                        }
                    }
                    /*
                     * ... and should differ compared to previous IDs
                     */
                    for (int i = 0; i < uids.length; i++) {
                        for (int j = 0; j < uids2.length; j++) {
                            assertTrue("An ID occured twice in folder" + fullname, uids[i] != uids2[j]);
                        }
                    }
                } finally {
                    /*
                     * Delete appended mails
                     */
                    mailAccess.getMessageStorage().deleteMessages(fullname, uids2, true);
                }
            } finally {
                if (fullname != null) {
                    mailAccess.getFolderStorage().deleteFolder(fullname, true);
                }
            }
        } finally {
            /*
             * close
             */
            mailAccess.close(false);
        }
    }
}
