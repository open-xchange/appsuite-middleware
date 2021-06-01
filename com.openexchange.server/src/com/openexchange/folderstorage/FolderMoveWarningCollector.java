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

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.internal.performers.PathPerformer;
import com.openexchange.folderstorage.osgi.FolderStorageServices;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FolderMoveWarningCollector}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.5
 */
public class FolderMoveWarningCollector {

    /**
     * {@link RootFolderType} defines the type of the root folder
     *
     * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
     * @since v7.10.5
     */
    private enum RootFolderType {
        NOTAVAILABLE,
        PRIVATE,
        PUBLIC,
        SHARED;
    }

    /**
     * <code>"15"</code>
     */
    private static final String INFOSTORE_PUBLIC = Integer.toString(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);

    /**
     * <code>"10"</code>
     */
    private static final String INFOSTORE_USER = Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);

    private static final String CAPABILITY_PERMISSIONS = "permissions";
    private static final String DEFAULT_TREE_ID = "1";
    private static final int SYSTEM_SYSTEM = 1;

    private FolderStorage folderStorage;
    private Folder folderToMove;
    private String newParentFolderId;
    private FolderStorage newParentStorage;
    private final ServerSession session;
    private final StorageParameters storageParameters;

    /**
     *
     * Initializes a new {@link FolderMoveWarningCollector}.
     *
     * @param session The server session.
     * @param storageParameters The storage parameters.
     * @throws OXException in case the session is missing
     */
    public FolderMoveWarningCollector(ServerSession session, StorageParameters storageParameters) throws OXException {
        if (session == null) {
            throw FolderExceptionErrorMessage.MISSING_SESSION.create();
        }
        this.session = session;
        this.storageParameters = require(storageParameters, "storageParameters");
    }

    /**
     *
     * Initializes a new {@link FolderMoveWarningCollector}.
     *
     * @param session The server session.
     * @param storageParameters The storage parameters.
     * @param storageFolder The folder to move.
     * @param realStorage The folder storage of the folder to move.
     * @param newParentId The id of the new parent folder.
     * @param newRealParentStorage The folder storage of the new parent folder.
     * @throws OXException in case the session is missing
     */
    public FolderMoveWarningCollector(ServerSession session, StorageParameters storageParameters, Folder storageFolder, FolderStorage realStorage, String newParentId, FolderStorage newRealParentStorage) throws OXException {
        this(session, storageParameters);
        this.folderToMove = require(storageFolder, "storageFolder");
        CalculatePermission.calculateUserPermissions(folderToMove, storageParameters.getContext());
        this.folderStorage = require(realStorage, "realStorage");
        this.newParentFolderId = require(newParentId, "newParentId");
        this.newParentStorage = require(newRealParentStorage, "newRealParentStorage");
    }

    /**
     *
     * Collects the warnings for moving a folder to a new parent folder.
     *
     * @return A list with the collected warnings.
     * @throws OXException
     */
    public Optional<OXException> collectWarnings() throws OXException {
        if (isModuleInfostore(folderToMove)) {
            Folder newParentFolder = getFolder(newParentFolderId, newParentStorage);
            MoveFolderPermissionMode mode = getMoveFolderPermissionMode(newParentFolder.getID());
            OXException warning = checkForWarnings(folderToMove, newParentFolder, mode);
            return Optional.ofNullable(warning);
        }
        return Optional.empty();
    }

    /**
     *
     * Gets the {@link MoveFolderPermissionMode} depending on the new parent folder and the configuration.
     *
     * @param folderId The id of the new parent folder.
     * @return The {@link MoveFolderPermissionMode}
     * @throws OXException
     */
    public MoveFolderPermissionMode getMoveFolderPermissionMode(String folderId) throws OXException {
        LeanConfigurationService configService = FolderStorageServices.requireService(LeanConfigurationService.class);
        String mode = null;
        RootFolderType folderType = checkFolderType(folderId);
        int contextId = storageParameters.getContextId();
        int userId = storageParameters.getUserId();
        switch (folderType) {
            case PUBLIC:
                mode = configService.getProperty(userId, contextId, MovePermissionProperty.MOVE_TO_PUBLIC);
                break;
            case SHARED:
                mode = configService.getProperty(userId, contextId, MovePermissionProperty.MOVE_TO_SHARED);
                break;
            case PRIVATE:
                mode = configService.getProperty(userId, contextId, MovePermissionProperty.MOVE_TO_PRIVATE);
                break;
            case NOTAVAILABLE:
            default:
                break;
        }
        return MoveFolderPermissionMode.getByName(mode);
    }

    /**
     * Adds the parent permissions to the given map
     *
     * @param permsMappingPerEntity The map to add the parent permissions to
     * @param parentMappingPerEntity The map containing the parent permissions
     */
    private void addParentPermissionsToFolder(Map<Integer, Permission> permsMappingPerEntity, Map<Integer, Permission> parentMappingPerEntity) {
        for (Map.Entry<Integer, Permission> entityEntry : parentMappingPerEntity.entrySet()) {
            Integer entity = entityEntry.getKey();
            if (OCLPermission.ALL_GUESTS == entity.intValue()) {
                continue;
            }
            Permission parentPerm = entityEntry.getValue();
            if (permsMappingPerEntity.containsKey(entity)) {
                BasicPermission folderPerm = new BasicPermission(permsMappingPerEntity.remove(entity));
                // @formatter:off
                folderPerm.setAllPermissions(Math.max(folderPerm.getFolderPermission(), parentPerm.getFolderPermission()),
                                             Math.max(folderPerm.getReadPermission(), parentPerm.getReadPermission()),
                                             Math.max(folderPerm.getWritePermission(), parentPerm.getWritePermission()),
                                             Math.max(folderPerm.getDeletePermission(), parentPerm.getDeletePermission()));
                // @formatter:on
                folderPerm.setAdmin(folderPerm.isAdmin() || parentPerm.isAdmin());
                folderPerm.setEntity(entity.intValue());
                permsMappingPerEntity.put(entity, folderPerm);
            } else {
                permsMappingPerEntity.put(entity, parentPerm);
            }
        }
    }

    /**
     * Checks the root folder type of the given folder
     *
     * @param folderId The folder id.
     * @return The {@link RootFolderType}
     * @throws OXException
     */
    private RootFolderType checkFolderType(String folderId) throws OXException {
        if (null == folderId) {
            return RootFolderType.NOTAVAILABLE;
        }
        int creatorOfTheLastFolder = -1;
        for (UserizedFolder f : getPathFolders(DEFAULT_TREE_ID, folderId, false)) {
            if (f != null && f.getID() != null) {
                if (f.getID().equals(INFOSTORE_USER)) {
                    if (creatorOfTheLastFolder == storageParameters.getUserId()) {
                        return RootFolderType.PRIVATE;
                    }
                    return RootFolderType.SHARED;
                } else if (f.getID().equals(INFOSTORE_PUBLIC)) {
                    return RootFolderType.PUBLIC;
                }
                creatorOfTheLastFolder = f.getCreatedBy();
            }
        }
        return RootFolderType.NOTAVAILABLE;
    }

    /**
     * Checks the folder for warnings
     *
     * @param folderToMove The folder to move.
     * @param newParentFolder The new parent folder.
     * @param mode The {@link MoveFolderPermissionMode}.
     * @return The warning or null
     * @throws OXException
     */
    private OXException checkForWarnings(Folder folderToMove, Folder newParentFolder, MoveFolderPermissionMode mode) throws OXException {
        List<Permission> oldPermissions = getPermissions(folderToMove);
        List<Permission> newPermissions = previewPermissionsForMove(folderToMove, newParentFolder, mode);
        Folder originalParentFolder = getFolder(folderToMove.getParentID(), folderStorage);
        List<Integer> oldPermissionEntities = getPermissionEntities(oldPermissions);
        List<Integer> newPermissionEntities = getPermissionEntities(newPermissions);

        OXException subfolderWarning = checkSubfoldersForWarnings(folderToMove, mode, oldPermissionEntities, newPermissionEntities, originalParentFolder, newParentFolder);
        if (subfolderWarning != null) {
            return subfolderWarning;
        }

        if (containEqualEntities(oldPermissionEntities, newPermissionEntities) == false) {
            boolean hasFPermissionsBefore = containsForeignPermissionEntities(session.getUserId(), oldPermissionEntities);
            boolean hasFPermissionsAfter = containsForeignPermissionEntities(session.getUserId(), newPermissionEntities);
            /*
             * Disabled warnings for getting new permissions with moving a folder
             */
            //            if (hasFPermissionsBefore == false && hasFPermissionsAfter == true) {
            //                return FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING.create(getWarningParameters(folderToMove, originalParentFolder, newParentFolder));
            //            } else
            if (hasFPermissionsBefore == true && hasFPermissionsAfter == false) {
                return FolderExceptionErrorMessage.MOVE_TO_NOT_SHARED_WARNING.create(getWarningParameters(folderToMove, originalParentFolder, newParentFolder));
            } else if (hasFPermissionsBefore == true && hasFPermissionsAfter == true) {
                if (mode.equals(MoveFolderPermissionMode.MERGE)) {
                    /*
                     * Disabled warnings for getting new permissions with moving a folder
                     */
                    // return FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING.create(getWarningParameters(session, folderToMove, originalParentFolder, newParentFolder, locale));
                    return null;
                }
                return FolderExceptionErrorMessage.MOVE_TO_ANOTHER_SHARED_WARNING.create(getWarningParameters(folderToMove, originalParentFolder, newParentFolder));
            }
        }
        return null;

    }

    /**
     * Checks subfolders for warnings.
     *
     * @param folderToMove The folder to move.
     * @param mode The {@link MoveFolderPermissionMode}.
     * @param oldPermissionEntities A list with the permission entities the folder had before the move.
     * @param newPermissionEntities A list with the permission entities the folder will have after the move.
     * @param originalParentFolder The original parent folder.
     * @param newParentFolder The new parent folder.
     * @return {@link OXException} in case a warning is found
     * @throws OXException
     */
    private OXException checkSubfoldersForWarnings(Folder folderToMove, MoveFolderPermissionMode mode, List<Integer> oldPermissionEntities, List<Integer> newPermissionEntities, Folder originalParentFolder, Folder newParentFolder) throws OXException {
        // check for warnings in subfolders is disabled for folders in external accounts, these folders are not permission aware
        // subfolders are relevant for warnings only if
        // 1. the folder supports the permissions capability
        // 2. the MoveFolderPermissionMode is INHERIT, otherwise the permissions of the subfolders are preserved with modes KEEP or MERGE
        // 3. the parent folder has no foreign permissions
        if (folderToMove.getSupportedCapabilities().contains(CAPABILITY_PERMISSIONS) && mode.equals(MoveFolderPermissionMode.INHERIT) && false == containsForeignPermissionEntities(session.getUserId(), oldPermissionEntities)) {
            SortableId[] subfolderIds = folderStorage.getSubfolders(folderToMove.getTreeID(), folderToMove.getID(), storageParameters);
            if (subfolderIds.length > 0) {
                List<Integer> subfolderPermissionEntities = new ArrayList<>();
                collectSubfolderShares(subfolderIds, subfolderPermissionEntities);
                if (containEqualEntities(subfolderPermissionEntities, newPermissionEntities) == false && containsForeignPermissionEntities(session.getUserId(), subfolderPermissionEntities)) {
                    if (containsForeignPermissionEntities(session.getUserId(), newPermissionEntities)) {
                        return FolderExceptionErrorMessage.MOVE_SHARED_SUBFOLDERS_TO_SHARED_WARNING.create(getWarningParameters(folderToMove, originalParentFolder, newParentFolder));
                    }
                    return FolderExceptionErrorMessage.MOVE_SHARED_SUBFOLDERS_TO_NOT_SHARED_WARNING.create(getWarningParameters(folderToMove, originalParentFolder, newParentFolder));
                }
            }
        }
        return null;
    }

    /**
     * Collects all foreign permission entities from the given subfolders and their children and its them to the given list
     *
     * @param subfolders The subfolder to check
     * @param subfolderPermissionEntities The list of entities
     * @throws OXException
     */
    private void collectSubfolderShares(SortableId[] subfolders, List<Integer> subfolderPermissionEntities) throws OXException {
        for (SortableId subfolderId : subfolders) {
            String id = subfolderId.getId();
            Folder subfolder = getFolder(id, folderStorage);
            List<Integer> entities = getPermissionEntities(getPermissions(subfolder));
            if (containsForeignPermissionEntities(session.getUserId(), entities)) {
                for (Integer pe : entities) {
                    if (subfolderPermissionEntities.contains(pe) == false) {
                        subfolderPermissionEntities.add(pe);
                    }
                }
            }
            SortableId[] subsubfolderIDs = folderStorage.getSubfolders(subfolder.getTreeID(), subfolder.getID(), storageParameters);
            collectSubfolderShares(subsubfolderIDs, subfolderPermissionEntities);
        }
    }

    /**
     *
     * Gets a value indicating whether two list with permission entities contain the same entities.
     *
     * @param entities1 One permission entity list.
     * @param entities2 The other permission entity list.
     * @return @return <code>true</code> if the lists have the same entities, <code>false</code>, otherwise
     */
    private boolean containEqualEntities(List<Integer> entities1, List<Integer> entities2) {
        if (entities1 == null || entities2 == null || entities1.size() != entities2.size()) {
            return false;
        }
        for (Integer entity : entities1) {
            if (entities2.contains(entity) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets a value indicating whether the supplied folder contains permissions for entities other than the supplied current user.
     *
     * @param userID The entity identifier of the user that should be considered as "not" foreign
     * @param entities The entities to check
     * @return <code>true</code> if foreign permissions were found, <code>false</code>, otherwise
     */
    private boolean containsForeignPermissionEntities(int userID, List<Integer> entities) {
        return entities != null && (entities.contains(I(userID)) ? 1 < entities.size() : 0 < entities.size());
    }

    /**
     * Gets the combined folder permissions
     *
     * @param folderToMove The folder to move.
     * @param newParentFolder The new parent folder.
     * @return The combined permissions
     */
    private List<Permission> getCombinedFolderPermissions(Folder folderToMove, Folder newParentFolder) {
        Map<Integer, Permission> permsMappingPerEntity = new HashMap<>();
        Map<Integer, Permission> systemPermsMappingPerEntity = new HashMap<>();
        Map<Integer, Permission> parentMappingPerEntity = new HashMap<>();
        Map<Integer, Permission> parentSystemMappingPerEntity = new HashMap<>();

        mapPermissions(folderToMove.getPermissions(), permsMappingPerEntity, systemPermsMappingPerEntity);
        mapPermissions(newParentFolder.getPermissions(), parentMappingPerEntity, parentSystemMappingPerEntity);

        addParentPermissionsToFolder(permsMappingPerEntity, parentMappingPerEntity);
        addParentPermissionsToFolder(systemPermsMappingPerEntity, parentSystemMappingPerEntity);

        return mergeAllPermissions(permsMappingPerEntity, systemPermsMappingPerEntity);
    }

    /**
     * Gets the folder with the given folder id from the given storage
     *
     * @param folderId The folder id.
     * @param folderStorage The folder storage.
     * @return The folder
     * @throws OXException
     */
    private Folder getFolder(String folderId, FolderStorage folderStorage) throws OXException {
        Folder folder = folderStorage.getFolder(DEFAULT_TREE_ID, folderId, storageParameters);
        CalculatePermission.calculateUserPermissions(folder, storageParameters.getContext());
        return folder;
    }

    /**
     * Gets the folder path for the given tree id and folder id
     *
     * @param treeId The tree id
     * @param folderId The folder id
     * @return The path as a string
     * @throws OXException
     */
    private String getFolderPath(String treeId, String folderId) throws OXException {
        List<UserizedFolder> response = getPathFolders(treeId, folderId, true);
        return getPathString(response, storageParameters.getUser().getLocale());
    }

    /**
     * Gets the paths folder for the given tree id and folder id
     *
     * @param treeId The tree id.
     * @param folderId The folder id.
     * @param altNames whether to use alt names or not
     * @return An array of folders
     * @throws OXException
     */
    private List<UserizedFolder> getPathFolders(String treeId, String folderId, boolean altNames) throws OXException {
        final PathPerformer pathPerformer = new PathPerformer(session, initDecorator(altNames));
        UserizedFolder[] response = pathPerformer.doPath(treeId, folderId, true);
        return Arrays.asList(response);
    }

    /**
     * Gets a readable path string containing all folder names separated by the path separator character <code>/</code>.
     *
     * @param path The file storage folders on the path in reverse order, i.e. the root folder is the last one
     * @param additionalFolders Additional folders to append at the end of the path
     * @return The path string
     */
    private String getPathString(List<UserizedFolder> path, Locale locale) {
        if ((null == path || 0 == path.size())) {
            return "/";
        }
        Collections.reverse(path);
        return "/" + path.stream().filter((f) -> f.getID().contentEquals(FolderStorage.PRIVATE_ID) == false).map(e -> e.getLocalizedName(locale, true)).collect(Collectors.joining("/"));
    }

    /**
     *
     * Gets the permission entities from a given list of permissions.
     *
     * @param permissions The permission list.
     * @return A list, that contains only the entities of the permissions.
     */
    private List<Integer> getPermissionEntities(List<Permission> permissions) {
        List<Integer> entityIds = new ArrayList<>();
        if (null != permissions && 0 < permissions.size()) {
            for (Permission permission : permissions) {
                Integer entity = I(permission.getEntity());
                if (entityIds.contains(entity) == false) {
                    entityIds.add(entity);
                }
            }
        }
        return entityIds;
    }

    /**
     * Get the permissions of a folder as a list.
     *
     * @param folder The folder.
     * @return A list of permissions or <code>null</code>, if no permissions available.
     */
    private List<Permission> getPermissions(Folder folder) {
        Permission[] permissions = folder.getPermissions();
        return permissions != null ? Arrays.asList(permissions) : null;
    }

    /**
     * Creates the parameters for a warning
     *
     * @param folderToMove The folder to move.
     * @param sourceFolder The original parent folder.
     * @param targetFolder The new parent folder.
     * @return The parameters
     * @throws OXException
     */
    private Object[] getWarningParameters(Folder folderToMove, Folder sourceFolder, Folder targetFolder) throws OXException {
        Object folderPath = getFolderPath(sourceFolder.getTreeID(), folderToMove.getID());
        Object sourceFolderPath = getFolderPath(sourceFolder.getTreeID(), sourceFolder.getID());
        Object folderId = folderToMove.getID();
        String targetFolderId = targetFolder.getID();
        Object targetFolderPath = getFolderPath(targetFolder.getTreeID(), targetFolderId);

        return new Object[] { folderPath, sourceFolderPath, targetFolderPath, folderId, targetFolderId };
    }

    /**
     * Creates and initializes a folder service decorator ready to use with calls to the underlying folder service.
     *
     * @param altNames Whether altNames should be used.
     * @return A new folder service decorator
     */
    private FolderServiceDecorator initDecorator(boolean altNames) {
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        Object connection = session.getParameter(Connection.class.getName());
        if (null != connection) {
            decorator.put(Connection.class.getName(), connection);
        }
        decorator.put("altNames", B(altNames).toString());
        decorator.setLocale(storageParameters.getUser().getLocale());
        return decorator;
    }

    /**
     * Checks whether the given folder is in the infostore module
     *
     * @param folder The folder to check
     * @return <code>true</code> if the folder is in the infostore module, <code>false</code> otherwise
     */
    private boolean isModuleInfostore(Folder folder) {
        return folder != null && folder.getContentType() != null && folder.getContentType().getModule() == Module.INFOSTORE.getFolderConstant();
    }

    /**
     * Maps the given permissions into the given maps
     *
     * @param permissions The permissions to map
     * @param permsMappingPerEntity The map for normal permissions
     * @param systemPermsMappingPerEntity The map for system permissions
     */
    private void mapPermissions(Permission[] permissions, Map<Integer, Permission> permsMappingPerEntity, Map<Integer, Permission> systemPermsMappingPerEntity) {
        for (Permission p : permissions) {
            if (p.getSystem() == SYSTEM_SYSTEM) {
                systemPermsMappingPerEntity.put(I(p.getEntity()), p);
            } else {
                permsMappingPerEntity.put(I(p.getEntity()), p);
            }
        }
    }

    /**
     * Merges all permissions into a single permissions list
     *
     * @param permsMappingPerEntity The normal permissions
     * @param systemPermsMappingPerEntity The system permissions
     * @return A list of permissions
     */
    private List<Permission> mergeAllPermissions(Map<Integer, Permission> permsMappingPerEntity, Map<Integer, Permission> systemPermsMappingPerEntity) {
        List<Permission> compiledPerms = new ArrayList<>();
        compiledPerms.addAll(permsMappingPerEntity.values());
        compiledPerms.addAll(systemPermsMappingPerEntity.values());
        return compiledPerms;
    }

    /**
     *
     * Shows a preview of the permissions that the folder will have after the move.
     *
     * @param folderToMove The folder to move
     * @param newParentFolder The target folder
     * @param mode The {@link MoveFolderPermissionMode} (see {@link FolderMoveWarningCollector#getMoveFolderPermissionMode})
     * @return An array with the new permissions the folder will have after the move.
     */
    private List<Permission> previewPermissionsForMove(Folder folderToMove, Folder newParentFolder, MoveFolderPermissionMode mode) {

        List<Permission> newPermissions = null;
        if (newParentFolder.getSupportedCapabilities().contains(CAPABILITY_PERMISSIONS)) {
            List<Permission> currentPermissions = getPermissions(folderToMove);
            switch (mode) {
                case INHERIT:
                    newPermissions = getPermissions(newParentFolder);
                    break;
                case MERGE:
                    newPermissions = getCombinedFolderPermissions(folderToMove, newParentFolder);
                    break;
                case KEEP:
                    newPermissions = currentPermissions;
                    break;
                default:
                    break;

            }
            newPermissions = processInheritedPermissions(currentPermissions, newPermissions, newParentFolder);
        }
        return newPermissions;
    }

    /**
     *
     * Processes inherited permissions for the new permissions.
     *
     * @param oldPermissions The original permissions of the folder.
     * @param newPermissions The calculated new permissions of the folder.
     * @param newParentFolder The folder id of the new parent folder.
     * @return A list with the adjusted new permissions.
     */
    private List<Permission> processInheritedPermissions(List<Permission> oldPermissions, List<Permission> newPermissions, Folder newParentFolder) {
        if (oldPermissions == null || newPermissions == null) {
            return newPermissions;
        }

        // When moving a folder with an "inherited" permission to another folder, this inherited permission should be removed implicitly.
        List<Permission> oldInheritedPermissions = oldPermissions.parallelStream().filter((p) -> p.getType().equals(FolderPermissionType.INHERITED)).collect(Collectors.toList());
        List<Permission> result = newPermissions.parallelStream().filter((p) -> oldInheritedPermissions.contains(p) == false).collect(Collectors.toList());

        // When the new parent folder already has an "inherited" or "legator" permission entry, this will get an "inherited" entry in the moved folder automatically.
        List<Permission> newParentPermissions = getPermissions(newParentFolder);
        if (newParentPermissions != null) {
            List<Permission> legatorPermissionsToInherit = newParentPermissions.stream().filter((p) -> FolderPermissionType.NORMAL.equals(p.getType()) == false).collect(Collectors.toList());
            for (Permission p : legatorPermissionsToInherit) {
                BasicPermission inheritedPermission = new BasicPermission(p);
                inheritedPermission.setType(FolderPermissionType.INHERITED);
                if (FolderPermissionType.LEGATOR.equals(p.getType())) {
                    inheritedPermission.setPermissionLegator(String.valueOf(newParentFolder.getID()));
                }
                result.add(inheritedPermission);
            }
        }
        return result;
    }

    /**
     * 
     * Checks if a required parameter is not NULL.
     *
     * @param <T> The type of the parameter.
     * @param parameter The parameter.
     * @param name The name of the parameter.
     * @return The value of the parameter unequal NULL.
     * @throws OXException Throws {@link FolderExceptionErrorMessage.MISSING_PARAMETER} exception if the parameter is NULL.
     */
    private <T> T require(T parameter, String name) throws OXException {
        if (parameter == null) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(name);
        }
        return parameter;
    }
}
