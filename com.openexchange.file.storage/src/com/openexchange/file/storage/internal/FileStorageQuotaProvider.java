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

package com.openexchange.file.storage.internal;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.osgi.OSGIFileStorageServiceRegistry;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.session.Session;
import com.openexchange.tools.id.IDMangler;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class FileStorageQuotaProvider implements QuotaProvider {

    private final OSGIFileStorageServiceRegistry storageRegistry;

    public FileStorageQuotaProvider(OSGIFileStorageServiceRegistry storageRegistry) {
        super();
        this.storageRegistry = storageRegistry;
    }

    @Override
    public String getModuleID() {
        return "filestorage";
    }

    @Override
    public String getDisplayName() {
        return "Drive";
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        List<String> unmangled = IDMangler.unmangle(accountID);
        String serviceID = null;
        if (unmangled.size() == 2) {
            serviceID = unmangled.get(0);
            accountID = unmangled.get(1);
        }

        if (serviceID == null) {
            List<FileStorageService> storageServices = storageRegistry.getAllServices();
            for (FileStorageService storageService : storageServices) {
                AccountQuota accountQuota = getForService(session, accountID, storageService);
                if (accountQuota != null) {
                    return accountQuota;
                }
            }
        } else {
            AccountQuota accountQuota = getForService(session, accountID, storageRegistry.getFileStorageService(serviceID));
            if (accountQuota != null) {
                return accountQuota;
            }
        }

        throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, getModuleID());
    }

    @Override
    public List<AccountQuota> getFor(Session session) throws OXException {
        List<AccountQuota> accountQuotas = new LinkedList<AccountQuota>();
        for (FileStorageService storageService : storageRegistry.getAllServices()) {
            FileStorageAccountManager accountManager = storageService.getAccountManager();
            List<FileStorageAccount> accounts = accountManager.getAccounts(session);
            for (FileStorageAccount account : accounts) {
                accountQuotas.add(getAccountQuota(account, session));
            }
        }

        return accountQuotas;
    }

    private AccountQuota getForService(Session session, String accountID, FileStorageService storageService) throws OXException {
        FileStorageAccountManager accountManager = storageService.getAccountManager();
        for (FileStorageAccount account : accountManager.getAccounts(session)) {
            if (account.getId().equals(accountID)) {
                return getAccountQuota(account, session);
            }
        }

        return null;
    }

    private static AccountQuota getAccountQuota(FileStorageAccount account, Session session) throws OXException {
        String accountID = account.getId();
        FileStorageAccountAccess accountAccess = account.getFileStorageService().getAccountAccess(accountID, session);
        accountAccess.connect();
        try {
            FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
            FileStorageFolder rootFolder = folderAccess.getRootFolder();
            Quota[] quotas = folderAccess.getQuotas(rootFolder.getId(), new Type[] { Type.FILE, Type.STORAGE });

            DefaultAccountQuota accountQuota = new DefaultAccountQuota(accountID, account.getDisplayName());
            if (quotas != null) {
                for (Quota quota : quotas) {
                    long limit = quota.getLimit();
                    switch (quota.getType()) {
                        case FILE:
                            if (limit == Quota.UNLIMITED) {
                                accountQuota.addQuota(com.openexchange.quota.Quota.UNLIMITED_AMOUNT);
                            } else {
                                accountQuota.addQuota(
                                    QuotaType.AMOUNT,
                                    limit,
                                    quota.getUsage());
                            }
                            break;

                        case STORAGE:
                            if (limit == Quota.UNLIMITED) {
                                accountQuota.addQuota(com.openexchange.quota.Quota.UNLIMITED_SIZE);
                            } else {
                                accountQuota.addQuota(
                                    QuotaType.SIZE,
                                    limit,
                                    quota.getUsage());
                            }
                            break;
                    }
                }
            }

            return accountQuota;
        } finally {
            accountAccess.close();
        }
    }

}
