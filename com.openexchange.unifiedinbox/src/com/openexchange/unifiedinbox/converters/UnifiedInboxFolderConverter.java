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

package com.openexchange.unifiedinbox.converters;

import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
import com.openexchange.mail.dataobjects.ReadOnlyMailFolder;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.UnifiedInboxAccess;
import com.openexchange.unifiedinbox.UnifiedInboxException;
import com.openexchange.unifiedinbox.services.Services;
import com.openexchange.unifiedinbox.utility.UnifiedInboxUtility;

/**
 * {@link UnifiedInboxFolderConverter} - Converts a Unified Mail folder to an instance of {@link MailFolder}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxFolderConverter {

    private static final String PROTOCOL = UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX;

    static final int[] EMPTY_COUNTS = new int[] { 0, 0, 0, 0 };

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(UnifiedInboxFolderConverter.class);

    private static final MailFolder ROOT_UNIFIED_INBOX_FOLDER;

    static {
        final MailFolder tmp = new MailFolder();
        tmp.setSubscribed(true);
        tmp.setSupportsUserFlags(false);
        tmp.setRootFolder(true);
        tmp.setExists(true);
        tmp.setSeparator('/');
        // Only the default folder contains subfolders
        tmp.setSubfolders(true);
        tmp.setSubscribedSubfolders(true);
        tmp.setFullname(MailFolder.DEFAULT_FOLDER_ID);
        tmp.setParentFullname(null);
        tmp.setName(UnifiedInboxManagement.NAME_UNIFIED_INBOX);
        tmp.setHoldsFolders(true);
        tmp.setHoldsMessages(false);
        {
            final MailPermission ownPermission = new DefaultMailPermission();
            ownPermission.setFolderPermission(OCLPermission.READ_FOLDER);
            ownPermission.setAllObjectPermission(OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
            ownPermission.setFolderAdmin(false);
            tmp.setOwnPermission(ownPermission);
        }
        {
            final MailPermission permission = new DefaultMailPermission();
            permission.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
            permission.setGroupPermission(true);
            permission.setFolderPermission(OCLPermission.READ_FOLDER);
            permission.setAllObjectPermission(OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
            permission.setFolderAdmin(false);
            tmp.addPermission(permission);
        }
        tmp.setDefaultFolder(false);
        tmp.setDefaultFolderType(DefaultFolderType.NONE);
        tmp.setMessageCount(-1);
        tmp.setNewMessageCount(-1);
        tmp.setUnreadMessageCount(-1);
        tmp.setDeletedMessageCount(-1);
        tmp.setProperty("protocol", PROTOCOL);
        ROOT_UNIFIED_INBOX_FOLDER = new ReadOnlyMailFolder(tmp);
    }

    /**
     * Prevent instantiation
     */
    private UnifiedInboxFolderConverter() {
        super();
    }

    /**
     * Gets the instance of {@link MailFolder} for root folder.
     *
     * @return The instance of {@link MailFolder} for root folder.
     */
    public static MailFolder getRootFolder() {
        return ROOT_UNIFIED_INBOX_FOLDER;
    }

    /**
     * Gets the appropriately filled instance of {@link MailFolder}.
     *
     * @param unifiedInboxAccountId The account ID of the Unified Mail account
     * @param session The session
     * @param fullname The folder's full name
     * @param localizedName The localized name of the folder
     * @return The appropriately filled instance of {@link MailFolder}
     * @throws OXException If converting mail folder fails
     */
    public static MailFolder getUnifiedINBOXFolder(final int unifiedInboxAccountId, final Session session, final String fullname, final String localizedName) throws OXException {
        final MailFolder tmp = new MailFolder();
        // Subscription not supported by Unified Mail, so every folder is "subscribed"
        tmp.setSubscribed(true);
        tmp.setSupportsUserFlags(true);
        tmp.setRootFolder(false);
        tmp.setExists(true);
        tmp.setSeparator('/');
        tmp.setFullname(fullname);
        tmp.setParentFullname(MailFolder.DEFAULT_FOLDER_ID);
        tmp.setName(localizedName);
        tmp.setHoldsFolders(true);
        tmp.setHoldsMessages(true);
        {
            final MailPermission ownPermission = new DefaultMailPermission();
            ownPermission.setFolderPermission(OCLPermission.READ_FOLDER);
            ownPermission.setAllObjectPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_ALL_OBJECTS);
            ownPermission.setFolderAdmin(false);
            tmp.setOwnPermission(ownPermission);
        }
        {
            final MailPermission permission = new DefaultMailPermission();
            permission.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
            permission.setGroupPermission(true);
            permission.setFolderPermission(OCLPermission.READ_FOLDER);
            permission.setAllObjectPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_ALL_OBJECTS);
            permission.setFolderAdmin(false);
            tmp.addPermission(permission);
        }
        // What else?!
        tmp.setDefaultFolder(true);
        if (UnifiedInboxAccess.INBOX.equals(fullname)) {
            tmp.setDefaultFolderType(DefaultFolderType.INBOX);
        } else if (UnifiedInboxAccess.TRASH.equals(fullname)) {
            tmp.setDefaultFolderType(DefaultFolderType.TRASH);
        } else if (UnifiedInboxAccess.SENT.equals(fullname)) {
            tmp.setDefaultFolderType(DefaultFolderType.SENT);
        } else if (UnifiedInboxAccess.SPAM.equals(fullname)) {
            tmp.setDefaultFolderType(DefaultFolderType.SPAM);
        } else if (UnifiedInboxAccess.DRAFTS.equals(fullname)) {
            tmp.setDefaultFolderType(DefaultFolderType.DRAFTS);
        } else {
            tmp.setDefaultFolderType(DefaultFolderType.NONE);
        }
        // Set message counts
        final boolean hasAtLeastOneSuchFolder = setMessageCounts(unifiedInboxAccountId, session, tmp);
        if (hasAtLeastOneSuchFolder) {
            tmp.setSubfolders(true);
            tmp.setSubscribedSubfolders(true);
        }
        tmp.setProperty("protocol", PROTOCOL);
        return tmp;
    }

    public static void setPermissions(final MailFolder mailFolder, final int userId) {
        MailPermission realPermission = mailFolder.getOwnPermission();
        final int fp = realPermission.getFolderPermission();
        {
            final MailPermission ownPermission = new DefaultMailPermission();
            ownPermission.setEntity(userId);
            ownPermission.setGroupPermission(false);
            // Grant not more than OCLPermission.CREATE_OBJECTS_IN_FOLDER
            ownPermission.setFolderPermission(fp > OCLPermission.CREATE_OBJECTS_IN_FOLDER ? OCLPermission.CREATE_OBJECTS_IN_FOLDER : fp);
            ownPermission.setAllObjectPermission(OCLPermission.READ_ALL_OBJECTS, realPermission.getWritePermission(), OCLPermission.DELETE_ALL_OBJECTS);
            ownPermission.setFolderAdmin(false);
            mailFolder.setOwnPermission(ownPermission);
        }
        {
            final MailPermission permission = new DefaultMailPermission();
            permission.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
            permission.setGroupPermission(true);
            // Grant not more than OCLPermission.CREATE_OBJECTS_IN_FOLDER
            permission.setFolderPermission(fp > OCLPermission.CREATE_OBJECTS_IN_FOLDER ? OCLPermission.CREATE_OBJECTS_IN_FOLDER : fp);
            permission.setAllObjectPermission(OCLPermission.READ_ALL_OBJECTS, realPermission.getWritePermission(), OCLPermission.DELETE_ALL_OBJECTS);
            permission.setFolderAdmin(false);
            mailFolder.removePermissions();
            mailFolder.addPermission(permission);
        }
    }

    private static boolean setMessageCounts(final int unifiedInboxAccountId, final Session session, final MailFolder tmp) throws UnifiedInboxException, OXException {
        boolean retval = false;
        {
            final MailAccountFacade mailAccountFacade = Services.getService(MailAccountFacade.class);
            final MailAccount[] arr = mailAccountFacade.getUserMailAccounts(session.getUserId(), session.getContextId());
            for (int i = 0; !retval && i < arr.length; i++) {
                final MailAccount mailAccount = arr[i];
                if (unifiedInboxAccountId != mailAccount.getId() && mailAccount.isUnifiedINBOXEnabled()) {
                    retval = true;
                }
            }
        }
        // Apply counts
        tmp.setMessageCount(-1);
        tmp.setNewMessageCount(-1);
        tmp.setUnreadMessageCount(-1);
        tmp.setDeletedMessageCount(-1);
        return retval;
    }

    /**
     * Gets the default folder's message counts of denoted account.
     *
     * @param accountId The account ID
     * @param session The session providing needed user data
     * @param fullnames The fullnames
     * @return The default folder's message counts of denoted account
     * @throws OXException If a mail error occurs
     */
    public static int[][] getAccountDefaultFolders(final int accountId, final Session session, final String[] fullnames) throws OXException {
        final int[][] retval;
        retval = getAccountDefaultFolders0(accountId, session, fullnames);
        return retval;
    }

    private static int[][] getAccountDefaultFolders0(final int accountId, final Session session, final String[] fullnames) throws OXException {
        final int[][] retval = new int[fullnames.length][];
        // Get & connect appropriate mail access
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            for (int i = 0; i < retval.length; i++) {
                final String accountFullname = UnifiedInboxUtility.determineAccountFullName(mailAccess, fullnames[i]);
                if (null != accountFullname && mailAccess.getFolderStorage().exists(accountFullname)) {
                    final MailFolder mf = mailAccess.getFolderStorage().getFolder(accountFullname);
                    retval[i] =
                        new int[] { mf.getMessageCount(), mf.getUnreadMessageCount(), mf.getDeletedMessageCount(), mf.getNewMessageCount() };
                } else {
                    LOG.debug("Missing folder \"{}\" in account {}", fullnames[i], accountId);
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            return new int[0][];
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
        return retval;
    }

    /**
     * Merges specified default folders.
     *
     * @param accountFolders The default folders
     * @param fullNames The full names
     * @param localizedNames The localized names
     * @return The merged default folders
     */
    public static MailFolder[] mergeAccountDefaultFolders(final List<int[][]> accountFolders, final String[] fullNames, final String[] localizedNames) {
        if (accountFolders.isEmpty()) {
            return new MailFolder[0];
        }
        final MailFolder[] retval = new MailFolder[accountFolders.get(0).length];
        final int size = accountFolders.size();
        for (int i = 0; i < retval.length; i++) {
            final MailFolder tmp = retval[i] = new MailFolder();
            // Subscription not supported by Unified Mail, so every folder is "subscribed"
            final String fullname = fullNames[i];
            tmp.setSubscribed(true);
            tmp.setSupportsUserFlags(true);
            tmp.setRootFolder(false);
            tmp.setExists(true);
            tmp.setSeparator('/');
            tmp.setFullname(fullname);
            tmp.setParentFullname(MailFolder.DEFAULT_FOLDER_ID);
            tmp.setName(localizedNames[i]);
            tmp.setHoldsFolders(true);
            tmp.setHoldsMessages(true);
            {
                final MailPermission ownPermission = new DefaultMailPermission();
                ownPermission.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
                tmp.setOwnPermission(ownPermission);
                tmp.addPermission(ownPermission);
            }
            // What else?!
            tmp.setDefaultFolder(true);
            if (UnifiedInboxAccess.INBOX.equals(fullname)) {
                tmp.setDefaultFolderType(DefaultFolderType.INBOX);
            } else if (UnifiedInboxAccess.TRASH.equals(fullname)) {
                tmp.setDefaultFolderType(DefaultFolderType.TRASH);
            } else if (UnifiedInboxAccess.SENT.equals(fullname)) {
                tmp.setDefaultFolderType(DefaultFolderType.SENT);
            } else if (UnifiedInboxAccess.SPAM.equals(fullname)) {
                tmp.setDefaultFolderType(DefaultFolderType.SPAM);
            } else if (UnifiedInboxAccess.DRAFTS.equals(fullname)) {
                tmp.setDefaultFolderType(DefaultFolderType.DRAFTS);
            } else {
                tmp.setDefaultFolderType(DefaultFolderType.NONE);
            }
            // Gather counts
            int totaCount = 0;
            int unreadCount = 0;
            int deletedCount = 0;
            int newCount = 0;
            final Iterator<int[][]> it = accountFolders.iterator();
            boolean hasSubfolders = false;
            for (int j = 0; j < size; j++) {
                final int[] accountDefaultFolder = it.next()[i];
                if (null != accountDefaultFolder) {
                    hasSubfolders = true;
                    // Add counts
                    totaCount += accountDefaultFolder[0];
                    unreadCount += accountDefaultFolder[1];
                    deletedCount += accountDefaultFolder[2];
                    newCount += accountDefaultFolder[3];
                }
            }
            // Apply results
            tmp.setSubfolders(hasSubfolders);
            tmp.setSubscribedSubfolders(hasSubfolders);
            tmp.setMessageCount(totaCount);
            tmp.setNewMessageCount(newCount);
            tmp.setUnreadMessageCount(unreadCount);
            tmp.setDeletedMessageCount(deletedCount);
            tmp.setProperty("protocol", PROTOCOL);
        }
        return retval;
    }

}
