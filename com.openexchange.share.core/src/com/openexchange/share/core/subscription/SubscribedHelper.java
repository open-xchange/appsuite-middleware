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

package com.openexchange.share.core.subscription;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.b;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.functions.ErrorAwareFunction;
import com.openexchange.tools.iterator.FilteringSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link SubscribedHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class SubscribedHelper {

    private final FileStorageAccount account;
    private final Set<String> possibleParentIds;

    /**
     * Initializes a new {@link SubscribedHelper}.
     *
     * @param account The underlying file storage account
     * @param possibleParentIds The identifiers of those parent folders where subscribe/unsubscribe operations of subfolders are possible
     */
    public SubscribedHelper(FileStorageAccount account, Set<String> possibleParentIds) {
        super();
        this.account = account;
        this.possibleParentIds = possibleParentIds;
    }

    /**
     * Looks up the account configuration whether folders are currently unsubscribed and sets the <i>subscribed</i> accordingly in the
     * passed folder references.
     * <p/>
     * The "subscribed subfolders" flag is implicitly set, too, so that the whole subfolder tree will effectively not appear in clients.
     *
     * @param folders The folders to set the subscribed information in
     * @param filterUnsubscribed <code>true</code> to exclude currently unsubscribed folders, <code>false</code>, otherwise
     * @return The (optionally filtered) folders, enriched by their subscribed information
     */
    public DefaultFileStorageFolder[] addSubscribed(DefaultFileStorageFolder[] folders, boolean filterUnsubscribed) {
        if (null == folders || 0 == folders.length) {
            return folders;
        }
        JSONObject accountMetadata = getAccountMetadata();
        List<DefaultFileStorageFolder> filteredFolders = new ArrayList<DefaultFileStorageFolder>(folders.length);
        for (DefaultFileStorageFolder folder : folders) {
            if (isSupported(folder)) {
                Boolean subscribed = getSubscribed(accountMetadata, folder.getId());
                folder.setSubscribed(null == subscribed || b(subscribed));
                folder.setSubscribedSubfolders(folder.isSubscribed());
                if (filterUnsubscribed && false == folder.isSubscribed()) {
                    continue;
                }
            }
            filteredFolders.add(folder);
        }
        return filteredFolders.toArray(new DefaultFileStorageFolder[filteredFolders.size()]);
    }

    /**
     * Looks up the account configuration whether a particular folder is currently unsubscribed and sets the <i>subscribed</i> accordingly
     * in the passed folder reference.
     * <p/>
     * The "subscribed subfolders" flag is implicitly set, too, so that the whole subfolder tree will effectively not appear in clients.
     *
     * @param folder The folder to set the subscribed information in
     * @return The folder, enriched by its subscribed information
     */
    public DefaultFileStorageFolder addSubscribed(DefaultFileStorageFolder folder) {
        if (isSupported(folder)) {
            Boolean subscribed = getSubscribed(getAccountMetadata(), folder.getId());
            folder.setSubscribed(null == subscribed || b(subscribed));
            folder.setSubscribedSubfolders(folder.isSubscribed());
        }
        return folder;
    }

    /**
     * Sets whether a particular folder is currently subscribed or not in the underlying account configuration and updates the account
     * in the storage.
     *
     * @param session The account owner's session
     * @param folder The folder to set the subscribed information for
     * @param subscribed <code>true</code> if subscribed, <code>false</code>, otherwise, or <code>null</code> to remove the stored value
     * @return <code>true</code> if something was actually changed, <code>false</code>, otherwise
     * @throws OXException If saving the account fails, or the folder's subscribed status cannot be changed
     */
    public boolean setSubscribed(Session session, FileStorageFolder folder, Boolean subscribed) throws OXException {
        if (false == isSupported(folder)) {
            throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(account.getFileStorageService().getDisplayName());
        }
        JSONObject accountMetadata = getAccountMetadata();
        if (false == setSubscribed(accountMetadata, folder.getId(), subscribed)) {
            return false;
        }
        new AccountMetadataHelper(account, session).saveAccountMetadata(accountMetadata);
        return true;
    }

    /**
     * Checks the parent folder of each file in the passed list, and removes those files that are located in an folder subtree that is
     * currently not subscribed.
     *
     * @param files The files to filter
     * @param getFolderFunction A function to retrieve a folder for the check by its identifier
     * @return The filtered files
     */
    public List<File> filterUnsubscribed(List<File> files, ErrorAwareFunction<String, FileStorageFolder> getFolderFunction) {
        if (null == files || files.isEmpty()) {
            return files;
        }
        Set<String> unsubscribedIds = getUnsubscribedIds(getAccountMetadata());
        if (null == unsubscribedIds || unsubscribedIds.isEmpty()) {
            return files;
        }
        try {
            Map<String, FileStorageFolder> knownFolders = new HashMap<String, FileStorageFolder>();
            List<File> filteredFiles = new ArrayList<File>(files.size());
            for (File file : files) {
                if (isSubscribed(file.getFolderId(), knownFolders, getFolderFunction)) {
                    filteredFiles.add(file);
                }
            }
            return filteredFiles;
        } catch (OXException e) {
            getLogger(SubscribedHelper.class).warn("Unexpetced error filtering files in unsubscribed folders", e);
            return files;
        }
    }

    /**
     * Checks the parent folder of each file in the passed search iterator, and skips those files that are located in an folder subtree
     * that is currently not subscribed from the resulting iterator.
     *
     * @param searchIterator The search iterator to filter
     * @param getFolderFunction A function to retrieve a folder for the check by its identifier
     * @return A search iterator that skips filtered files
     */
    public SearchIterator<File> filterUnsubscribed(SearchIterator<File> searchIterator, ErrorAwareFunction<String, FileStorageFolder> getFolderFunction) {
        if (null == searchIterator || 0 == searchIterator.size()) {
            return searchIterator;
        }
        Set<String> unsubscribedIds = getUnsubscribedIds(getAccountMetadata());
        if (null == unsubscribedIds || unsubscribedIds.isEmpty()) {
            return searchIterator;
        }
        try {
            Map<String, FileStorageFolder> knownFolders = new HashMap<String, FileStorageFolder>();
            return new FilteringSearchIterator<File>(searchIterator) {

                @Override
                public boolean accept(File file) throws OXException {
                    return isSubscribed(file.getFolderId(), knownFolders, getFolderFunction);
                }
            };
        } catch (OXException e) {
            getLogger(SubscribedHelper.class).warn("Unexpetced error filtering files in unsubscribed folders", e);
            return searchIterator;
        }
    }

    private boolean isSupported(FileStorageFolder folder) {
        return null != folder && null != folder.getId() && null != folder.getParentId() && possibleParentIds.contains(folder.getParentId());
    }

    boolean isSubscribed(String folderId, Map<String, FileStorageFolder> knownFolders, ErrorAwareFunction<String, FileStorageFolder> getFolderFunction) throws OXException {
        if (Strings.isEmpty(folderId) || possibleParentIds.contains(folderId)) {
            return true;
        }
        FileStorageFolder folder = knownFolders.get(folderId);
        if (null == folder) {
            folder = getFolderFunction.apply(folderId);
            knownFolders.put(folderId, folder);
        }
        if (false == folder.isSubscribed()) {
            return false;
        }
        return isSubscribed(folder.getParentId(), knownFolders, getFolderFunction);
    }

    /**
     * Gets whether or not the given folder is currently marked as subscribed
     *
     * @param accountMetadata The metaData to check
     * @param folderId The ID of the folder
     * @return <code>true</code> if the folder with the given ID is marked as subscribed in the given meta data, <code>false</code> otherwise
     */
    public static Boolean getSubscribed(JSONObject accountMetadata, String folderId) {
        if (null == accountMetadata) {
            return null;
        }
        JSONObject subscribedObject = accountMetadata.optJSONObject("subscribed");
        if (null == subscribedObject || false == subscribedObject.hasAndNotNull(folderId)) {
            return null;
        }
        return B(subscribedObject.optBoolean(folderId, true));
    }


    private JSONObject getAccountMetadata() {
        JSONObject metadata = account.getMetadata();
        return null == metadata ? new JSONObject() : metadata;
    }

    private static Set<String> getUnsubscribedIds(JSONObject accountMetadata) {
        JSONObject subscribedObject = accountMetadata.optJSONObject("subscribed");
        return null != subscribedObject ? new HashSet<String>(subscribedObject.keySet()) : java.util.Collections.emptySet();
    }

    private static boolean setSubscribed(JSONObject accountMetadata, String folderId, Boolean subscribed) {
        Boolean oldSubscribed = getSubscribed(accountMetadata, folderId);
        if (Objects.equals(subscribed, oldSubscribed)) {
            return false;
        }
        JSONObject subscribedObject = accountMetadata.optJSONObject("subscribed");
        if (null == subscribedObject) {
            subscribedObject = new JSONObject();
            accountMetadata.putSafe("subscribed", subscribedObject);
        }
        subscribedObject.putSafe(folderId, Boolean.TRUE.equals(subscribed) ? null : subscribed);
        return true;
    }

}
