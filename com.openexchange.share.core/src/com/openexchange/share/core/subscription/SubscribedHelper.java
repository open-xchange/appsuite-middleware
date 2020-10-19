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
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.generic.DefaultFileStorageAccount;
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
    
    /** 
     * The identifiers of the parent folders where adjusting the subscribed flag is supported, which mark the entry points for federated 
     * sharing ("15" / "Public Files" / FolderObject#SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, and "10" / "Shared Files" / 
     * FolderObject#SYSTEM_USER_INFOSTORE_FOLDER_ID).
     */
    //    static final Set<String> SUPPORTED_PARENT_IDS = Collections.unmodifiableSet("10", "15");
    
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
     * The "subscibed subfolders" flag is implicitly set, too, so that the whole subfolder tree will effectively not appear in clients.
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
        FileStorageService fileStorageService = account.getFileStorageService();
        DefaultFileStorageAccount accountUpdate = new DefaultFileStorageAccount();
        accountUpdate.setServiceId(fileStorageService.getId());
        accountUpdate.setFileStorageService(fileStorageService);
        accountUpdate.setId(account.getId());
        accountUpdate.setDisplayName(account.getDisplayName());
        accountUpdate.setConfiguration(account.getConfiguration());
        accountUpdate.setMetaData(accountMetadata);
        fileStorageService.getAccountManager().updateAccount(accountUpdate, session);
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

    private JSONObject getAccountMetadata() {
        JSONObject metadata = account.getMetadata();
        return null == metadata ? new JSONObject() : metadata;
    }
    
    private static Set<String> getUnsubscribedIds(JSONObject accountMetadata) {
        JSONObject subscribedObject = accountMetadata.optJSONObject("subscribed");
        return null != subscribedObject ? new HashSet<String>(subscribedObject.keySet()) : java.util.Collections.emptySet();
    }

    private static Boolean getSubscribed(JSONObject accountMetadata, String folderId) {
        if (null == accountMetadata) {
            return null;
        }
        JSONObject subscribedObject = accountMetadata.optJSONObject("subscribed");
        if (null == subscribedObject || false == subscribedObject.hasAndNotNull(folderId)) {
            return null;
        }
        return B(subscribedObject.optBoolean(folderId, true));
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
