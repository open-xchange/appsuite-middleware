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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.ReadOnlyMailFolder;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.UnifiedINBOXException;
import com.openexchange.unifiedinbox.services.UnifiedINBOXServiceRegistry;
import com.openexchange.unifiedinbox.utility.UnifiedINBOXUtility;

/**
 * {@link UnifiedINBOXFolderConverter} - Converts a Unified INBOX folder to an instance of {@link MailFolder}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXFolderConverter {

    private static final MailFolder ROOT_UNIFIED_INBOX_FOLDER;

    static {
        final MailFolder tmp = new MailFolder();
        tmp.setSubscribed(true);
        tmp.setSupportsUserFlags(false);
        tmp.setRootFolder(true);
        tmp.setExists(true);
        tmp.setSeparator('/');
        // Only the default folder contains subfolders, to be more precise it only contains the INBOX folder.
        tmp.setSubfolders(true);
        tmp.setSubscribedSubfolders(true);
        tmp.setFullname(MailFolder.DEFAULT_FOLDER_ID);
        tmp.setParentFullname(null);
        tmp.setName(MailFolder.DEFAULT_FOLDER_NAME);
        tmp.setHoldsFolders(true);
        tmp.setHoldsMessages(false);
        {
            final MailPermission ownPermission = new DefaultMailPermission();
            ownPermission.setFolderPermission(OCLPermission.READ_FOLDER);
            ownPermission.setAllObjectPermission(OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
            ownPermission.setFolderAdmin(false);
            tmp.setOwnPermission(ownPermission);
            tmp.addPermission(ownPermission);
        }
        tmp.setDefaultFolder(false);
        tmp.setMessageCount(-1);
        tmp.setNewMessageCount(-1);
        tmp.setUnreadMessageCount(-1);
        tmp.setDeletedMessageCount(-1);
        ROOT_UNIFIED_INBOX_FOLDER = new ReadOnlyMailFolder(tmp);
    }

    /**
     * Prevent instantiation
     */
    private UnifiedINBOXFolderConverter() {
        super();
    }

    /**
     * Gets the instance of {@link MailFolder} reflcting root folder.
     * 
     * @return The instance of {@link MailFolder} reflcting root folder.
     */
    public static MailFolder getRootFolder() {
        return ROOT_UNIFIED_INBOX_FOLDER;
    }

    /**
     * Gets the appropriately filled instance of {@link MailFolder}.
     * 
     * @param accountId The account ID of the Unified INBOX account
     * @param session The session
     * @param fullname The folder's fullname
     * @param localizedName The localized name of the folder
     * @return The appropriately filled instance of {@link MailFolder}
     * @throws MailException If converting mail folder fails
     */
    public static MailFolder getUnifiedINBOXFolder(final int accountId, final Session session, final String fullname, final String localizedName) throws MailException {
        final MailFolder tmp = new MailFolder();
        // Subscription not supported by Unified INBOX, so every folder is "subscribed"
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
            ownPermission.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
            tmp.setOwnPermission(ownPermission);
            tmp.addPermission(ownPermission);
        }
        // What else?!
        tmp.setDefaultFolder(true);
        // Set message counts
        final boolean hasAtLeastOneSuchFolder = setMessageCounts(fullname, accountId, session, tmp);
        if (hasAtLeastOneSuchFolder) {
            tmp.setSubfolders(true);
            tmp.setSubscribedSubfolders(true);
        }
        return tmp;
    }

    private static boolean setMessageCounts(final String fullname, final int accountId, final Session session, final MailFolder tmp) throws UnifiedINBOXException, MailException {
        final MailAccount[] accounts;
        try {
            final MailAccountStorageService storageService = UnifiedINBOXServiceRegistry.getServiceRegistry().getService(
                MailAccountStorageService.class,
                true);
            accounts = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
        } catch (final ServiceException e) {
            throw new UnifiedINBOXException(e);
        } catch (final MailAccountException e) {
            throw new UnifiedINBOXException(e);
        }
        boolean retval = false;
        int totaCount = 0;
        int unreadCount = 0;
        int deletedCount = 0;
        int newCount = 0;
        for (final MailAccount mailAccount : accounts) {
            if (accountId != mailAccount.getId()) {
                final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, mailAccount.getId());
                boolean close = false;
                try {
                    mailAccess.connect();
                    close = true;
                    final String accountFullname = UnifiedINBOXUtility.determineAccountFullname(mailAccess, fullname);
                    // Check if account fullname is not null
                    if (null != accountFullname) {
                        // Get counts
                        final MailFolder mailFolder = mailAccess.getFolderStorage().getFolder(accountFullname);
                        totaCount += mailFolder.getMessageCount();
                        unreadCount += mailFolder.getUnreadMessageCount();
                        deletedCount += mailFolder.getDeletedMessageCount();
                        newCount += mailFolder.getNewMessageCount();
                        // At least one such default folder present
                        retval = true;
                    }
                } finally {
                    if (close) {
                        mailAccess.close(true);
                    }
                }
            }
        }
        // Apply counts
        tmp.setMessageCount(totaCount);
        tmp.setNewMessageCount(newCount);
        tmp.setUnreadMessageCount(unreadCount);
        tmp.setDeletedMessageCount(deletedCount);
        return retval;
    }

}
