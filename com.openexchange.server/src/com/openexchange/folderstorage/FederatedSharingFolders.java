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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.folderstorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang.mutable.MutableInt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.ErrorStateFolderAccess.FileStorageFolderStub;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountMetaDataUtil;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStoragePermission;
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
                //TODO: Caching?
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

                            ret.addAll(toSortableIds(sharedFolders, service.getId(), account.getId(), ordinal));

                            //persist the folders to the account so they can be returned as "dummies" the next time the account has errors and is not able to get the real folders
                            storeSubFolders(account, sharedFolders, parentFolder, session);
                        } catch (Exception e) {
                            try {
                                //Error: We do add all last known sub folders in case of an error.
                                //Those folders get decorated with an error when loaded later and thus can be displayed by the client
                                LOG.debug("Unable to load federate sharing folders for account " + account.getId() + ": " + e.getMessage());
                                FileStorageFolderStub[] storedSubFolders = getLastKnownFolders(account, String.valueOf(parentFolder), session);
                                if (storedSubFolders.length == 0) {
                                    LOG.debug("There are no last known federated sharing folders for account " + account.getId());
                                }
                                ret.addAll(toSortableIds(storedSubFolders, service.getId(), account.getId(), ordinal));
                            } catch (Exception e2) {
                                LOG.error("Unable to load last known federate sharing folders for account " + account.getId(), e2);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to list federate sharing folders", e);
        }
        return ret.toArray(new SortableId[ret.size()]);
    }

    /**
     * Gets a sub folder from the list of known subfolders
     *
     * @param account The account
     * @param folderId The ID of the folder to get
     * @param session The session
     * @return The {@link FileStorageFolder} if it is present in the list of last known sub folders, null if unknown.
     * @throws OXException
     */
    public static FileStorageFolderStub getLastKnownFolder(FileStorageAccount account, String folderId, Session session) throws OXException {
        Optional<FileStorageFolderStub> folder = Arrays.asList(getLastKnownFolders(account, session)).stream().filter(f -> folderId.equals(f.getId())).findFirst();
        return folder.orElse(null);
    }

    /**
     * Gets the last known folders for a given parent and account
     *
     * @param account The account
     * @param parentId The parent ID to get the folders for
     * @param session
     * @return A list of last known folders with the given parent ID for the given account
     * @throws OXException
     */
    public static FileStorageFolderStub[] getLastKnownFolders(FileStorageAccount account, String parentId, Session session) throws OXException {
        List<FileStorageFolderStub> allKnownFolders = Arrays.asList(getLastKnownFolders(account, session));
        return allKnownFolders.stream().filter(folder -> Objects.equals(folder.getParentId(), parentId)).toArray(FileStorageFolderStub[]::new);
    }

    /**
     * Gets the last known folders for the given account
     *
     * @param account The account
     * @return A list of last known folders for the given account
     * @throws OXException
     */
    public static FileStorageFolderStub[] getLastKnownFolders(FileStorageAccount account, Session session) throws OXException {
        try {
            JSONArray lastKnownFolders = FileStorageAccountMetaDataUtil.getLastKnownFolders(account);
            if (lastKnownFolders != null) {
                ArrayList<FileStorageFolderStub> ret = new ArrayList<FileStorageFolderStub>(lastKnownFolders.length());
                for (int i = 0; i < lastKnownFolders.length(); i++) {
                    JSONObject jsonFolder = lastKnownFolders.getJSONObject(i);

                    FileStorageFolderStub folder = new FileStorageFolderStub();
                    folder.setId(jsonFolder.getString(FileStorageAccountMetaDataUtil.JSON_FIELD_FOLDER_ID));
                    folder.setName(jsonFolder.getString(FileStorageAccountMetaDataUtil.JSON_FIELD_FOLDER_NAME));
                    folder.setParentId(jsonFolder.optString(FileStorageAccountMetaDataUtil.JSON_FIELD_FOLDER_PARENT_ID, "10"));

                    folder.setExists(true);
                    folder.setSubscribed(true);

                    folder.setProperties(new HashMap<String, Object>());

                    //default permissions
                    List<FileStoragePermission> permissions = new ArrayList<FileStoragePermission>(1);
                    final DefaultFileStoragePermission defaultPermission = DefaultFileStoragePermission.newInstance();
                    defaultPermission.setEntity(session.getUserId());
                    permissions.add(defaultPermission);
                    folder.setPermissions(permissions);

                    ret.add(folder);
                }
                return ret.toArray(new FileStorageFolderStub[ret.size()]);
            }
            return new FileStorageFolderStub[0];
        } catch (JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Internal method to store the last known sub folders for a {@link FileStorageAccount} and the given parent
     *
     * @param account The account to store the folders for
     * @param folderd The list of sub folders to store
     * @param parentId The parentID to store the folders for
     * @param session The session
     * @throws OXException
     */
    private static void storeSubFolders(FileStorageAccount account, FileStorageFolder[] folders, int parentId, Session session) throws OXException {
        if (folders != null && folders.length > 0) {

            try {
                List<JSONObject> lastKnownFolders = new ArrayList<JSONObject>();
                for (FileStorageFolder folder : folders) {
                    JSONObject jsonFolder = new JSONObject();
                    jsonFolder.put(FileStorageAccountMetaDataUtil.JSON_FIELD_FOLDER_ID, folder.getId());
                    jsonFolder.put(FileStorageAccountMetaDataUtil.JSON_FIELD_FOLDER_NAME, folder.getName());
                    jsonFolder.put(FileStorageAccountMetaDataUtil.JSON_FIELD_FOLDER_PARENT_ID, String.valueOf(parentId));
                    lastKnownFolders.add(jsonFolder);
                }

                //preserve folders with a different parent
                JSONArray currentKnownFolders = FileStorageAccountMetaDataUtil.getLastKnownFolders(account);
                if (currentKnownFolders != null) {
                    for (int i = 0; i < currentKnownFolders.length(); i++) {
                        JSONObject knownFolder = currentKnownFolders.getJSONObject(i);
                        if (false == Objects.equals(knownFolder.optString(FileStorageAccountMetaDataUtil.JSON_FIELD_FOLDER_PARENT_ID, null), String.valueOf(parentId))) {
                            lastKnownFolders.add(knownFolder);
                        }
                    }
                }

                //Save if needed
                if (FileStorageAccountMetaDataUtil.setLastKnownFolders(account, lastKnownFolders)) {
                    account.getFileStorageService().getAccountManager().updateAccount(account, session);
                }
            } catch (JSONException e) {
                throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
            }
        }
    }
}
