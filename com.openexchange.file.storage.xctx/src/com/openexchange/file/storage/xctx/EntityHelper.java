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

package com.openexchange.file.storage.xctx;

import static com.openexchange.java.Autoboxing.I;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.share.core.subscription.XctxEntityHelper;
import com.openexchange.user.UserService;

/**
 * {@link EntityHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class EntityHelper extends XctxEntityHelper {

    private final XctxAccountAccess accountAccess;

    /**
     * Initializes a new {@link EntityHelper}.
     * 
     * @param accountAccess The parent account access
     */
    public EntityHelper(XctxAccountAccess accountAccess) {
        super(accountAccess.getService().getId(), accountAccess.getAccountId(), (String) accountAccess.getAccount().getConfiguration().get("url"));
        this.accountAccess = accountAccess;
    }

    /**
     * Resolves and builds additional entity info for a certain user or group under perspective of the passed session's user.
     * 
     * @param session The session to use to resolve the entities in
     * @param entity The identifier of the entity to resolve
     * @param isGroup <code>true</code> if the entity refers to a group, <code>false</code>, otherwise
     * @return The entity info, or <code>null</code> if the referenced entity could not be resolved
     */
    public EntityInfo optEntityInfo(Session session, int entity, boolean isGroup) {
        if (0 > entity || 0 == entity && false == isGroup) {
            getLogger(EntityHelper.class).warn("Unable to lookup entity info for {}", I(entity));
            return null;
        }
        return lookupEntity(session, entity, isGroup);
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
     * @return A list with new permissions, enhanced with additional details of the underlying entity
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

    @Override
    protected UserService getUserService() throws OXException {
        return accountAccess.getServiceSafe(UserService.class);
    }

    @Override
    protected GroupService getGroupService() throws OXException {
        return accountAccess.getServiceSafe(GroupService.class);
    }

    @Override
    protected ShareService getShareService() throws OXException {
        return accountAccess.getServiceSafe(ShareService.class);
    }

    @Override
    protected DispatcherPrefixService getDispatcherPrefixService() throws OXException {
        return accountAccess.getServiceSafe(DispatcherPrefixService.class);
    }
    
}
