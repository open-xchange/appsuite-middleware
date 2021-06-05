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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.LinkEntityInfo;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link EntityMangler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class EntityMangler {

    protected static final int NOT_SET = -1;
    
    protected final String serviceId;
    protected final String accountId;

    /**
     * Initializes a new {@link EntityMangler}.
     * 
     * @param serviceId The service identifier to mangle into qualified identifiers
     * @param accountId The account identifier to mangle into qualified identifiers
     */
    public EntityMangler(String serviceId, String accountId) {
        super();
        this.serviceId = serviceId;
        this.accountId = accountId;
    }
    
    /**
     * <i>Mangles</i> the identifiers of the passed {@link EntityInfo} object from the <i>remote</i> context or server, so that it can
     * be used within the local session of the storage account's context.
     * <p/>
     * The mangled <code>identifier</code> will be constructed based on the remote entity id, while the <code>entity</code> itself will
     * no longer be set in the resulting entity info object.
     * 
     * @param entityInfo The entity info to mangle the identifiers in
     * @return A new entity info instance with qualified remote identifiers
     */
    public EntityInfo mangleRemoteEntity(EntityInfo entityInfo) {
        if (null == entityInfo) {
            return null;
        }
        String identifier = entityInfo.getIdentifier();
        if (null != identifier) {
            identifier = IDMangler.mangle(serviceId, accountId, identifier);
        }
        String imageUrl = entityInfo.getImageUrl();
        if (null != imageUrl) {
            // TODO: encode service/account?
            imageUrl = null;
        }
        EntityInfo mangledEntityInfo = new EntityInfo(identifier, entityInfo.getDisplayName(), entityInfo.getTitle(), entityInfo.getFirstName(), entityInfo.getLastName(), entityInfo.getEmail1(), NOT_SET, imageUrl, entityInfo.getType());
        if (LinkEntityInfo.class.isInstance(entityInfo)) {
            LinkEntityInfo linkEntityInfo = (LinkEntityInfo) entityInfo;
            mangledEntityInfo = new LinkEntityInfo(mangledEntityInfo, linkEntityInfo.getShareUrl(), linkEntityInfo.getPassword(), linkEntityInfo.getExpiryDate(), linkEntityInfo.isIncludeSubfolders());
        }
        return mangledEntityInfo;
    }

    /**
     * <i>Unmangles</i> the identifiers of the passed {@link EntityInfo} object from the <i>local</i> context, so that it can be used
     * within the guest session of the foreign context.
     * <p/>
     * The <code>identifier</code> found in the passed entity info reference will used to extract the original entity identifier again,
     * which will get applied in the resulting entity info object.
     * 
     * @param entityInfo The entity info to unmangle the identifiers in
     * @return A new entity info instance with relative identifiers
     * @throws IllegalArgumentException If passed {@link EntityInfo} refers to a foreign account
     */
    public EntityInfo unmangleLocalEntity(EntityInfo entityInfo) {
        if (null == entityInfo) {
            return null;
        }
        String identifier = entityInfo.getIdentifier();
        int entity = entityInfo.getEntity();
        if (null != identifier) {
            List<String> components = IDMangler.unmangle(identifier);
            if (false == matchesAccount(components)) {
                throw new IllegalArgumentException("Cannot unmangle entity info with id " + identifier + " from foreign account");
            }
            identifier = components.get(2);
            entity = Integer.parseInt(identifier);
        }
        String imageUrl = entityInfo.getImageUrl();
        if (null != imageUrl) {
            // TODO: decode service/account?
            imageUrl = null;
        }
        EntityInfo unmangledEntityInfo = new EntityInfo(identifier, entityInfo.getDisplayName(), entityInfo.getTitle(), entityInfo.getFirstName(), entityInfo.getLastName(), entityInfo.getEmail1(), entity, imageUrl, entityInfo.getType());
        if (LinkEntityInfo.class.isInstance(entityInfo)) {
            LinkEntityInfo linkEntityInfo = (LinkEntityInfo) entityInfo;
            unmangledEntityInfo = new LinkEntityInfo(unmangledEntityInfo, linkEntityInfo.getShareUrl(), linkEntityInfo.getPassword(), linkEntityInfo.getExpiryDate(), linkEntityInfo.isIncludeSubfolders());
        }
        return unmangledEntityInfo;
    }
    
    /**
     * <i>Mangles</i> the identifiers found in the supplied folder permissions from the <i>remote</i> context, so that they can be used
     * within the local session of the storage account's context.
     * <p/>
     * For each permission entry, the mangled permission's <code>identifier</code> will be constructed based on the remote entity id,
     * while the <code>entity</code> itself will no longer be set in the resulting permission. The same is done with a potentially set
     * {@link EntityInfo} in the supplied foreign permission.
     * 
     * @param permissions The permissions to mangle the identifiers in
     * @return A list with new permissions with qualified remote entity identifiers
     */
    public List<FileStoragePermission> mangleRemotePermissions(List<FileStoragePermission> permissions) {
        if (null == permissions) {
            return null;
        }
        List<FileStoragePermission> mangledPermissions = new ArrayList<FileStoragePermission>(permissions.size());
        for (FileStoragePermission permission : permissions) {
            mangledPermissions.add(mangleRemotePermission(permission));
        }
        return mangledPermissions;
    }

    /**
     * <i>Mangles</i> the identifiers found in the supplied folder permission from the <i>remote</i> context, so that it can be used
     * within the local session of the storage account's context.
     * <p/>
     * The mangled permission's <code>identifier</code> will be constructed based on the remote entity id, while the <code>entity</code>
     * itself will no longer be set in the resulting permission. The same is done with a potentially set {@link EntityInfo} in the
     * supplied foreign permission.
     * 
     * @param permission The permission to mangle the identifiers in
     * @return A new permission with qualified remote entity identifiers
     */
    public FileStoragePermission mangleRemotePermission(FileStoragePermission permission) {
        if (null == permission) {
            return null;
        }
        DefaultFileStoragePermission mangledPermission = DefaultFileStoragePermission.newInstance(permission);
        mangledPermission.setIdentifier(mangleRemoteEntity(permission.getEntity()));
        mangledPermission.setEntity(NOT_SET);
        mangledPermission.setEntityInfo(mangleRemoteEntity(permission.getEntityInfo()));
        return mangledPermission;
    }

    /**
     * <i>Mangles</i> the identifiers found in the supplied object permission from the <i>remote</i> context, so that it can be used
     * within the local session of the storage account's context.
     * <p/>
     * The mangled permission's <code>identifier</code> will be constructed based on the remote entity id, while the <code>entity</code>
     * itself will no longer be set in the resulting permission. The same is done with a potentially set {@link EntityInfo} in the
     * supplied foreign permission.
     * 
     * @param permission The permission to mangle the identifiers in
     * @return A new permission with qualified remote entity identifiers
     */
    public FileStorageObjectPermission mangleRemoteObjectPermission(FileStorageObjectPermission permission) {
        if (null == permission) {
            return null;
        }
        DefaultFileStorageObjectPermission mangledPermission = new DefaultFileStorageObjectPermission(
            mangleRemoteEntity(permission.getEntity()), NOT_SET, permission.isGroup(), permission.getPermissions());
        mangledPermission.setEntityInfo(mangleRemoteEntity(permission.getEntityInfo()));
        return mangledPermission;
    }

    /**
     * <i>Mangles</i> the identifiers found in the supplied object permissions from the <i>remote</i> context, so that they can be used
     * within the local session of the storage account's context.
     * <p/>
     * For each permission entry, the mangled permission's <code>identifier</code> will be constructed based on the remote entity id,
     * while the <code>entity</code> itself will no longer be set in the resulting permission. The same is done with a potentially set
     * {@link EntityInfo} in the supplied foreign permission.
     * 
     * @param permissions The permissions to mangle the identifiers in
     * @return A list with new permissions with qualified remote entity identifiers
     */
    public List<FileStorageObjectPermission> mangleRemoteObjectPermissions(List<FileStorageObjectPermission> permissions) {
        if (null == permissions) {
            return null;
        }
        List<FileStorageObjectPermission> mangledPermissions = new ArrayList<FileStorageObjectPermission>(permissions.size());
        for (FileStorageObjectPermission permission : permissions) {
            mangledPermissions.add(mangleRemoteObjectPermission(permission));
        }
        return mangledPermissions;
    }
    
    /**
     * <i>Unmangles</i> the identifiers found in the supplied folder permission from the <i>local</i> context, so that it can be used
     * within the guest session of the foreign context.
     * <p/>
     * The passed permission's <code>identifier</code> will be used to extract the original entity identifier again, which will get applied
     * in the resulting permission object. The same is done with the identifiers found in a potentially set {@link EntityInfo} in the
     * supplied local permission.
     * 
     * @param permission The permission to unmangle the identifiers in
     * @return A new permission with relative entity identifiers
     */
    public Permission unmangleLocalPermission(Permission permission) {
        if (null == permission) {
            return null;
        }
        int entity = unmangleLocalEntity(permission.getIdentifier());
        BasicPermission unmangledPermission = new BasicPermission(permission);
        unmangledPermission.setEntity(entity);
        unmangledPermission.setIdentifier(String.valueOf(entity));
        unmangledPermission.setEntityInfo(unmangleLocalEntity(permission.getEntityInfo()));
        return unmangledPermission;
    }

    /**
     * <i>Unmangles</i> the identifiers found in the supplied object permission from the <i>local</i> context, so that it can be used
     * within the guest session of the foreign context.
     * <p/>
     * The passed permission's <code>identifier</code> will be used to extract the original entity identifier again, which will get applied
     * in the resulting permission object. The same is done with the identifiers found in a potentially set {@link EntityInfo} in the
     * supplied local permission.
     * 
     * @param permission The permission to unmangle the identifiers in
     * @return A new permission with relative entity identifiers
     */
    public FileStorageObjectPermission unmangleLocalObjectPermission(FileStorageObjectPermission permission) {
        if (null == permission) {
            return null;
        }
        int entity = unmangleLocalEntity(permission.getIdentifier());
        DefaultFileStorageObjectPermission unmangledPermission = new DefaultFileStorageObjectPermission(
            entity, permission.isGroup(), permission.getPermissions());
        unmangledPermission.setEntityInfo(unmangleLocalEntity(permission.getEntityInfo()));
        return unmangledPermission;
    }

    /**
     * <i>Unmangles</i> the identifiers found in the supplied list of folder permission from the <i>local</i> context, so that it can be
     * used within the guest session of the foreign context.
     * <p/>
     * The passed permission's <code>identifier</code> will be used to extract the original entity identifier again, which will get applied
     * in the resulting permission object. The same is done with the identifiers found in a potentially set {@link EntityInfo} in the
     * supplied local permission.
     * 
     * @param permissions The permissions to unmangle the identifiers in
     * @return A list with new permissions with relative entity identifiers
     */
    public Permission[] unmangleLocalPermissions(Permission[] permissions) {
        if (null == permissions) {
            return null;
        }
        Permission[] unmangledPermissions = new Permission[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            unmangledPermissions[i] = unmangleLocalPermission(permissions[i]);
        }
        return unmangledPermissions;
    }

    /**
     * <i>Unmangles</i> the identifiers found in the supplied list of object permission from the <i>local</i> context, so that it can be
     * used within the guest session of the foreign context.
     * <p/>
     * The passed permission's <code>identifier</code> will be used to extract the original entity identifier again, which will get applied
     * in the resulting permission object. The same is done with the identifiers found in a potentially set {@link EntityInfo} in the
     * supplied local permission.
     * 
     * @param permissions The permissions to unmangle the identifiers in
     * @return A list with new permissions with relative entity identifiers
     */
    public List<FileStorageObjectPermission> unmangleLocalObjectPermissions(List<FileStorageObjectPermission> permissions) {
        if (null == permissions) {
            return null;
        }
        List<FileStorageObjectPermission> unmangledPermissions = new ArrayList<FileStorageObjectPermission>(permissions.size());
        for (FileStorageObjectPermission permission : permissions) {
            unmangledPermissions.add(unmangleLocalObjectPermission(permission));
        }
        return unmangledPermissions;
    }

    /**
     * <i>Mangles</i> a numerical entity identifier from the <i>remote</i> context, so that it can be used within the local session of
     * the storage account's context.
     * 
     * @param entity The numerical identifier to mangle
     * @return The mangled identifier
     */
    public String mangleRemoteEntity(int entity) {
        if (0 > entity) {
            return null;
        }
        return IDMangler.mangle(serviceId, accountId, String.valueOf(entity));
    }

    protected int unmangleLocalEntity(String identifier) {
        if (null != identifier) {
            List<String> components = IDMangler.unmangle(identifier);
            if (false == matchesAccount(components)) {
                throw new IllegalArgumentException("Cannot unmangle entity info with id " + identifier + " from foreign account");
            }
            return Integer.parseInt(components.get(2));
        }
        return NOT_SET;
    }

    protected boolean matchesAccount(List<String> unmangledComponents) {
        return null != unmangledComponents && 3 <= unmangledComponents.size() && 
            Objects.equals(unmangledComponents.get(0), serviceId) && Objects.equals(unmangledComponents.get(1), accountId);
    }

}
