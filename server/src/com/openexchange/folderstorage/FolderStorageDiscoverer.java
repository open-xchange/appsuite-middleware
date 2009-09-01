
package com.openexchange.folderstorage;

/**
 * {@link FolderStorageDiscoverer} - The folder storage discovery.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FolderStorageDiscoverer {

    /**
     * Gets the folder storage for specified tree-folder-pair.
     * 
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @return The folder storage for specified tree-folder-pair or <code>null</code>
     */
    FolderStorage getFolderStorage(String treeId, String folderId);

    /**
     * Gets the folder storages for specified tree-parent-pair.
     * 
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @return The folder storages for specified tree-parent-pair or an empty array if none available
     */
    FolderStorage[] getFolderStoragesForParent(String treeId, String parentId);

    /**
     * Gets the folder storages for specified tree identifier.
     * 
     * @param treeId The tree identifier
     * @return The folder storages for specified tree identifier or an empty array if none available
     */
    FolderStorage[] getFolderStoragesForTreeID(String treeId);

    /**
     * Gets the tree folder storages. No cache folder storage is returned.
     * 
     * @param treeId The tree identifier
     * @return The tree folder storages or an empty array if none available
     */
    FolderStorage[] getTreeFolderStorages(String treeId);

    /**
     * Gets the folder storage capable to handle given content type in specified tree.
     * 
     * @param treeId The tree identifier
     * @param contentType The content type
     * @return The folder storage capable to handle given content type in specified tree
     */
    FolderStorage getFolderStorageByContentType(String treeId, ContentType contentType);

}
