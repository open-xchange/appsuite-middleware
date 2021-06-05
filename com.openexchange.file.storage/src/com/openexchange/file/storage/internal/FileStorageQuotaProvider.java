/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.file.storage.internal;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.annotation.Nullable;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.osgi.OSGIFileStorageServiceRegistry;
import com.openexchange.java.Strings;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.AccountQuotas;
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
    public AccountQuota getFor(Session session, String compositeAccountID) throws OXException {
        return getFor(session, compositeAccountID, null);
    }

    @Override
    public AccountQuota getFor(Session session, String compositeAccountID, String folderId) throws OXException {
        String accountID = compositeAccountID;
        String serviceID = null;
        {
            List<String> unmangled = IDMangler.unmangle(compositeAccountID);
            if (unmangled.size() == 2) {
                serviceID = unmangled.get(0);
                accountID = unmangled.get(1);
            }
        }

        if (serviceID == null) {
            List<FileStorageService> storageServices = storageRegistry.getAllServices();
            for (FileStorageService storageService : storageServices) {
                AccountQuota accountQuota = getForService(session, accountID, folderId, storageService);
                if (accountQuota != null) {
                    return accountQuota;
                }
            }
        } else {
            AccountQuota accountQuota = getForService(session, accountID, folderId, storageRegistry.getFileStorageService(serviceID));
            if (accountQuota != null) {
                return accountQuota;
            }
        }

        throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, getModuleID());
    }

    private AccountQuota getForService(Session session, String accountID, String folderId, FileStorageService storageService) throws OXException {
        FileStorageAccountManager accountManager = storageService.getAccountManager();
        try {
            FileStorageAccount account = accountManager.getAccount(accountID, session);
            return getAccountQuota(account, folderId, session);
        } catch (OXException e) {
            // Could not be retrieved
        }

        return null;
    }

    @Override
    public AccountQuotas getFor(Session session) throws OXException {
        List<AccountQuota> accountQuotas = new LinkedList<AccountQuota>();
        List<OXException> warnings = null;
        for (FileStorageService storageService : storageRegistry.getAllServices()) {
            FileStorageAccountManager accountManager = storageService.getAccountManager();
            List<FileStorageAccount> accounts = accountManager.getAccounts(session);
            for (FileStorageAccount account : accounts) {
                try {
                    accountQuotas.add(getAccountQuota(account, null, session));
                } catch (OXException e) {
                    if (warnings == null) {
                        warnings = new LinkedList<OXException>();
                    }
                    warnings.add(e);
                }
            }
        }

        return new AccountQuotas(accountQuotas, warnings);
    }

    /**
     * Gets the {@link AccountQuota} for the given account
     *
     * @param account The account to get the quota for
     * @param folder The, optional, folder to get the quota for or null in order to get the quota for the root folder.
     * @param session The session
     * @return The {@link AccountQuota}
     * @throws OXException
     */
    private static AccountQuota getAccountQuota(FileStorageAccount account, @Nullable String folder, Session session) throws OXException {
        String accountID = account.getId();
        FileStorageAccountAccess accountAccess = account.getFileStorageService().getAccountAccess(accountID, session);
        accountAccess.connect();
        try {
            FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
            String folderId = Strings.isNotEmpty(folder) ? folder : folderAccess.getRootFolder().getId();
            Quota[] quotas = folderAccess.getQuotas(folderId, new Type[] { Type.FILE, Type.STORAGE });

            DefaultAccountQuota accountQuota = new DefaultAccountQuota(accountID, account.getDisplayName());
            if (quotas != null) {
                for (Quota quota : quotas) {
                    long limit = quota.getLimit();
                    switch (quota.getType()) {
                        case FILE:
                            if (limit == Quota.UNLIMITED) {
                                accountQuota.addQuota(com.openexchange.quota.Quota.UNLIMITED_AMOUNT);
                            } else {
                                accountQuota.addQuota(QuotaType.AMOUNT, limit, quota.getUsage());
                            }
                            break;

                        case STORAGE:
                            if (limit == Quota.UNLIMITED) {
                                accountQuota.addQuota(com.openexchange.quota.Quota.UNLIMITED_SIZE);
                            } else {
                                accountQuota.addQuota(QuotaType.SIZE, limit, quota.getUsage());
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
