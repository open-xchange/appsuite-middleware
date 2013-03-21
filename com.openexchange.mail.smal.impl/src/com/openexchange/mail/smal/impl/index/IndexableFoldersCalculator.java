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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.index;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.smal.impl.SmalMailAccess;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.session.Session;


/**
 * {@link IndexableFoldersCalculator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexableFoldersCalculator {

    public static Set<MailFolder> calculatePrivateMailFolders(Session session, MailAccountStorageService storageService, int accountId) throws OXException {
        int userId = session.getUserId();
        int contextId = session.getContextId();
        MailAccount mailAccount = storageService.getMailAccount(accountId, userId, contextId);
        String mailServer = mailAccount.getMailServer();
        if (!mailAccount.isDefaultAccount() && AccountBlacklist.isServerBlacklisted(mailServer)) {
            return Collections.emptySet();
        }

        Set<MailFolder> allFolders = new HashSet<MailFolder>();
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = SmalMailAccess.getUnwrappedInstance(
            session,
            accountId);
        try {
            mailAccess.connect();
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            MailFolder rootFolder = folderStorage.getRootFolder();
            MailFolder[] subfolders = folderStorage.getSubfolders(rootFolder.getFullname(), true);
            addFoldersRecursive(subfolders, allFolders, folderStorage);
        } finally {
            SmalMailAccess.closeUnwrappedInstance(mailAccess);
        }

        return allFolders;
    }

    public static Map<Integer, Set<MailFolder>> calculatePrivateMailFolders(Session session, MailAccountStorageService storageService) throws OXException {
        int userId = session.getUserId();
        int contextId = session.getContextId();
        MailAccount[] mailAccounts = storageService.getUserMailAccounts(userId, contextId);
        Map<Integer, Set<MailFolder>> folderMap = new HashMap<Integer, Set<MailFolder>>();
        for (MailAccount account : mailAccounts) {
            String mailServer = account.getMailServer();
            if (!account.isDefaultAccount() && AccountBlacklist.isServerBlacklisted(mailServer)) {
                continue;
            }

            Set<MailFolder> allFolders = new HashSet<MailFolder>();
            int accountId = account.getId();
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = SmalMailAccess.getUnwrappedInstance(
                session,
                accountId);
            try {
                mailAccess.connect();
                IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                MailFolder rootFolder = folderStorage.getRootFolder();
                MailFolder[] subfolders = folderStorage.getSubfolders(rootFolder.getFullname(), true);
                addFoldersRecursive(subfolders, allFolders, folderStorage);
                folderMap.put(new Integer(accountId), allFolders);
            } finally {
                SmalMailAccess.closeUnwrappedInstance(mailAccess);
            }
        }

        return folderMap;
    }

    private static void addFoldersRecursive(MailFolder[] subfolders, Set<MailFolder> allFolders, IMailFolderStorage folderStorage) throws OXException {
        for (MailFolder folder : subfolders) {
            if (!folder.exists() || folder.isSpam() || folder.isConfirmedSpam()) {
                continue;
            }

            boolean index = true;
            if ((folder.containsShared() && folder.isShared()) || (folder.containsPublic() && folder.isPublic())) {
                index = false;
            }

            MailPermission ownPermission = folder.getOwnPermission();
            if (index && ownPermission.isFolderVisible() && ownPermission.canReadAllObjects() && folder.isHoldsMessages()) {
                allFolders.add(folder);
            }

            if (folder.isHoldsFolders()) {
                MailFolder[] subsubfolders = folderStorage.getSubfolders(folder.getFullname(), true);
                if (subsubfolders != null && subsubfolders.length > 0 && subsubfolders != IMailFolderStorage.EMPTY_PATH) {
                    addFoldersRecursive(subsubfolders, allFolders, folderStorage);
                }
            }
        }
    }

}
