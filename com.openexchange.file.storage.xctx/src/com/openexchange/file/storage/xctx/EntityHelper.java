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

package com.openexchange.file.storage.xctx;

import static com.openexchange.java.Autoboxing.I;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.EntityInfo.Type;
import com.openexchange.groupware.LinkEntityInfo;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.tools.id.IDMangler;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link EntityHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class EntityHelper {

    private static int NOT_SET = -1;

    private final XctxAccountAccess accountAccess;

    /**
     * Initializes a new {@link EntityHelper}.
     * 
     * @param accountAccess The parent account access
     */
    public EntityHelper(XctxAccountAccess accountAccess) {
        super();
        this.accountAccess = accountAccess;
    }

    /**
     * <i>Mangles</i> the identifiers of the passed {@link EntityInfo} object from the <i>remote</i> context, so that it can be used
     * within the local session of the storage account's context.
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
            identifier = IDMangler.mangle(accountAccess.getService().getId(), accountAccess.getAccountId(), identifier);
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
     */
    public EntityInfo unmangleLocalEntity(EntityInfo entityInfo) {
        if (null == entityInfo) {
            return null;
        }
        String identifier = entityInfo.getIdentifier();
        if (null != identifier) {
            List<String> components = IDMangler.unmangle(identifier);
            if (matchesAccount(components)) {
                identifier = components.get(2);
            }
        }
        int entity = Integer.parseInt(identifier);
        String imageUrl = entityInfo.getImageUrl();
        if (null != imageUrl) {
            // TODO: decode service/account?
            imageUrl = null;
        }
        return new EntityInfo(identifier, entityInfo.getDisplayName(), entityInfo.getTitle(), entityInfo.getFirstName(), entityInfo.getLastName(), entityInfo.getEmail1(), entity, imageUrl, entityInfo.getType());
    }

    /**
     * Resolves and builds additional entity info for the users and groups referenced by the supplied permissions under perspective of
     * the passed session's user, and returns a new object permission enriched by these entity info.
     * 
     * @param session The session to use to resolve the entities in
     * @param permissions The permissions to enhance
     * @return A list with new object permissions, enhanced with additional details of the underlying entity
     */
    public List<FileStorageObjectPermission> addObjectPermissionEntityInfos(Session session, List<FileStorageObjectPermission> permissions) {
        if (null == permissions) {
            return null;
        }
        List<FileStorageObjectPermission> enhancedPermissions = new ArrayList<FileStorageObjectPermission>(permissions.size());
        for (FileStorageObjectPermission permission : permissions) {
            enhancedPermissions.add(addObjectPermissionEntityInfo(session, permission));
        }
        return enhancedPermissions;
    }

    /**
     * Resolves and builds additional entity info for a certain user or group under perspective of the passed session's user, and returns
     * a new object permission enriched by these entity info.
     * 
     * @param session The session to use to resolve the entity in
     * @param permission The permission to enhance
     * @return A new object permission, enhanced with additional details of the underlying entity, or the passed permission as is if no
     *         info could be resolved
     */
    public FileStorageObjectPermission addObjectPermissionEntityInfo(Session session, FileStorageObjectPermission permission) {
        if (null == permission) {
            return null;
        }
        EntityInfo entityInfo = lookupEntity(session, permission.getEntity(), permission.isGroup());
        if (null == entityInfo) {
            return permission;
        }
        DefaultFileStorageObjectPermission enhancedPermission = new DefaultFileStorageObjectPermission(
            permission.getIdentifier(), permission.getEntity(), permission.isGroup(), permission.getPermissions());
        enhancedPermission.setEntityInfo(entityInfo);
        return enhancedPermission;
    }

    /**
     * Resolves and builds additional entity info for the users and groups referenced by the supplied permissions under perspective of
     * the passed session's user, and returns a list of new permissions enriched by these entity info.
     * 
     * @param session The session to use to resolve the entities in
     * @param permissions The permissions to enhance
     * @return A list with new object permissions, enhanced with additional details of the underlying entity
     */
    public List<FileStoragePermission> addPermissionEntityInfos(Session session, List<FileStoragePermission> permissions) {
        if (null == permissions) {
            return null;
        }
        List<FileStoragePermission> enhancedPermissions = new ArrayList<FileStoragePermission>(permissions.size());
        for (FileStoragePermission permission : permissions) {
            enhancedPermissions.add(addPermissionEntityInfo(session, permission));
        }
        return enhancedPermissions;
    }

    /**
     * Resolves and builds additional entity info for a certain user or group under perspective of the passed session's user, and returns
     * a new permission enriched by these entity info.
     * 
     * @param session The session to use to resolve the entity in
     * @param permission The permission to enhance
     * @return A new permission, enhanced with additional details of the underlying entity, or the passed permission as is if no
     *         info could be resolved
     */
    public FileStoragePermission addPermissionEntityInfo(Session session, FileStoragePermission permission) {
        if (null == permission) {
            return null;
        }
        EntityInfo entityInfo = lookupEntity(session, permission.getEntity(), permission.isGroup());
        if (null == entityInfo) {
            return permission;
        }
        DefaultFileStoragePermission enhancedPermission = DefaultFileStoragePermission.newInstance(permission);
        enhancedPermission.setEntityInfo(entityInfo);
        return enhancedPermission;
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

    private String generateShareLink(GuestInfo guest) {
        try {
            if (null != guest.getLinkTarget()) {
                ShareTargetPath targetPath = new ShareTargetPath(guest.getLinkTarget().getModule(), guest.getLinkTarget().getFolder(), guest.getLinkTarget().getItem());
                return guest.generateLink(accountAccess.getGuestHostData(), targetPath);
            }
            return guest.generateLink(accountAccess.getGuestHostData(), null);
        } catch (OXException e) {
            getLogger(EntityHelper.class).warn("Error generating share link for {}", guest, e);
            return null;
        }
    }

    private String mangleRemoteEntity(int entity) {
        if (0 > entity) {
            return null;
        }
        return IDMangler.mangle(accountAccess.getService().getId(), accountAccess.getAccountId(), String.valueOf(entity));
    }

    /**
     * Resolves and builds additional entity info for a certain user or group under perspective of the passed session's user.
     * <p/>
     * If no entity could be found in the session's context, a placeholder entity is returned.
     * 
     * @param session The session to use to resolve the entity
     * @param entity The identifier of the entity to resolve
     * @param isGroup <code>true</code> if the entity refers to a group, <code>false</code>, otherwise
     * @return The entity info, or <code>null</code> if the referenced entity could not be resolved
     */
    private EntityInfo lookupEntity(Session session, int entity, boolean isGroup) {
        if (0 > entity || 0 == entity && false == isGroup) {
            getLogger(EntityHelper.class).warn("Unable to lookup entity info for {}", I(entity));
            return null;
        }
        if (isGroup) {
            /*
             * lookup group and build entity info
             */
            Group group = lookupGroup(session, entity);
            return null != group ? getEntityInfo(group) : null;
        }
        /*
         * lookup user and build entity info
         */
        User user = lookupUser(session, entity);
        if (null == user) {
            return null;
        }
        EntityInfo entityInfo = getEntityInfo(user);
        if (ShareTool.isAnonymousGuest(user)) {
            /*
             * derive additional link information for anonymous guests
             */
            GuestInfo guest = lookupGuest(session, entity);
            if (null != guest) {
                String shareUrl = generateShareLink(guest);
                return new LinkEntityInfo(entityInfo, shareUrl, guest.getPassword(), guest.getExpiryDate(), false);
            }
        }
        return entityInfo;
    }

    private User lookupUser(Session session, int userId) {
        try {
            return accountAccess.getServiceSafe(UserService.class).getUser(userId, session.getContextId());
        } catch (OXException e) {
            getLogger(EntityHelper.class).warn("Error looking up user {} in context {}", I(userId), I(session.getContextId()), e);
            return null;
        }
    }

    private Group lookupGroup(Session session, int groupId) {
        try {
            return accountAccess.getServiceSafe(GroupService.class).getGroup(ServerSessionAdapter.valueOf(session).getContext(), groupId);
        } catch (OXException e) {
            getLogger(EntityHelper.class).warn("Error looking up group {} in context {}", I(groupId), I(session.getContextId()), e);
            return null;
        }
    }

    private GuestInfo lookupGuest(Session session, int guestId) {
        try {
            return accountAccess.getServiceSafe(ShareService.class).getGuestInfo(session, guestId);
        } catch (OXException e) {
            getLogger(EntityHelper.class).warn("Error looking up guest {} in context {}", I(guestId), I(session.getContextId()), e);
            return null;
        }
    }
    
    private EntityInfo getEntityInfo(User user) {
        EntityInfo.Type type = user.isGuest() ? Type.GUEST : Type.USER;
        return new EntityInfo(String.valueOf(user.getId()), user.getDisplayName(), null, user.getGivenName(), user.getSurname(), user.getMail(), user.getId(), null, type);
    }
    
    private EntityInfo getEntityInfo(Group group) {
        return new EntityInfo(String.valueOf(group.getIdentifier()), group.getDisplayName(), null, null, null, null, group.getIdentifier(), null, Type.GROUP);
    }

    private int unmangleLocalEntity(String identifier) {
        if (null != identifier) {
            List<String> components = IDMangler.unmangle(identifier);
            if (matchesAccount(components)) {
                try {
                    return Integer.parseInt(components.get(2));
                } catch (NumberFormatException e) {
                    getLogger(EntityHelper.class).warn("Unexpected error extracting entity identifier from {}", identifier, e);
                }
            }
        }
        return -1;
    }

    private boolean matchesAccount(List<String> unmangledComponents) {
        return null != unmangledComponents && 3 == unmangledComponents.size() && 
            Objects.equals(unmangledComponents.get(0), accountAccess.getService().getId()) && 
            Objects.equals(unmangledComponents.get(1), accountAccess.getAccountId());
    }

}
