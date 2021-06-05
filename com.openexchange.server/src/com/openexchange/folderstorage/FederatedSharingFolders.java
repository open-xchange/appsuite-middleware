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

package com.openexchange.folderstorage;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.filestorage.FileStorageId;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link FederatedSharingFolders} - Handling for folders shared from other OX installations
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class FederatedSharingFolders {

    private static final Logger LOG = LoggerFactory.getLogger(FederatedSharingFolders.class);

    /**
     * Internal method to transform an array of {@link FileStorageFolder} to a list of {@link SortableId}.
     *
     * @param folders The array of folders to transform.
     * @param serviceId The ID of the related file storage service
     * @param accountId The ID of the related file storage account
     * @param ordinal The ordinal number to set and increment
     * @return A list of {@link SortableId}
     */
    private static List<SortableId> toSortableIds(final FileStorageFolder[] folders, String serviceId, String accountId, MutableInt ordinal) {
        ArrayList<SortableId> ret = new ArrayList<SortableId>(folders.length);
        for (FileStorageFolder folder : folders) {
            ret.add(toSortableId(folder, serviceId, accountId, ordinal));
        }

        return ret;
    }

    /**
     * Internal method to transform a {@link FileStorageFolder} to a {@link SortableId}
     *
     * @param folder The folder to transform
     * @param serviceId The ID of the related file storage service
     * @param accountId The ID of the related file storage account
     * @param ordinal The ordinal number to set and increment
     * @return The {@link SortabaleId}
     */
    private static SortableId toSortableId(final FileStorageFolder folder, String serviceId, String accountId, MutableInt ordinal) {
        String folderId = IDMangler.mangle(serviceId, accountId, folder.getId());
        FileStorageId fileStorageId = new FileStorageId(folderId, ordinal.intValue(), folder.getName());
        ordinal.increment();
        return fileStorageId;
    }

    /**
     * Returns if there is at least one Federal Sharing account for the given session
     *
     * @param session The session to check
     * @return <code>True</code>, if there is at least one federated sharing account for the given session <code>false</code> otherwise.
     * @throws OXException
     */
    public static boolean hasFederalSharingAccount(final Session session) throws OXException {
        FileStorageServiceRegistry registry = ServerServiceRegistry.getInstance().getService(FileStorageServiceRegistry.class);
        List<FileStorageService> allServices = registry.getAllServices();
        for (FileStorageService service : allServices) {
            if (service instanceof AccountAware && service instanceof SharingFileStorageService) {
                List<FileStorageAccount> accounts = ((AccountAware) service).getAccounts(session);
                if (accounts.size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets a list of federated shared folders for the given {@link Session}
     *
     * @param parentFolder the ID of the parent folder to get the subfolders for
     * @param session The {@link Session} to get the federated shared folders for
     * @param forceRetry Forces a retry of listing the folders, even if the account is "error-"flagged for some time and would throw the error without trying to access the remote share.
     * @return The list of federated shared folders for the given session
     */
    public static SortableId[] getFolders(final int parentFolder, final Session session, final boolean forceRetry) {
        List<SortableId> ret = new ArrayList<>();
        MutableInt ordinal = new MutableInt(0);
        try {
            FileStorageServiceRegistry registry = ServerServiceRegistry.getInstance().getService(FileStorageServiceRegistry.class);
            List<FileStorageService> allServices = registry.getAllServices();
            for (FileStorageService service : allServices) {
                if (service instanceof AccountAware && service instanceof SharingFileStorageService) {
                    List<FileStorageAccount> accounts = ((AccountAware) service).getAccounts(session);
                    for (FileStorageAccount account : accounts) {
                        try {
                            if (forceRetry) {
                                //Remove any known, recent, errors if forced
                                ((SharingFileStorageService) service).resetRecentError(account.getId(), session);
                            }
                            FileStorageAccountAccess access = account.getFileStorageService().getAccountAccess(account.getId(), session);
                            access.connect();
                            FileStorageFolderAccess folderAccess = access.getFolderAccess();
                            FileStorageFolder[] sharedFolders = folderAccess.getSubfolders(String.valueOf(parentFolder), true);
                            if (sharedFolders != null) {
                                ret.addAll(toSortableIds(sharedFolders, service.getId(), account.getId(), ordinal));
                            }
                        }
                        catch(Exception e) {
                            LOG.error("Unable to load last known federate sharing folders for account " + account.getId(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to list federate sharing folders", e);
        }
        return ret.toArray(new SortableId[ret.size()]);
    }
}
