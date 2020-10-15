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

package com.openexchange.file.storage.xox;

import static com.openexchange.java.Autoboxing.I;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.api.client.ApiClient;
import com.openexchange.api.client.common.calls.GetJsonObjectDataCall;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.EntityInfo.Type;
import com.openexchange.java.Strings;
import com.openexchange.share.core.subscription.EntityMangler;

/**
 * {@link EntityHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class EntityHelper extends EntityMangler {
    
    /** static cache to store resolved entity infos for a short while */
    private static final Cache<String, Optional<EntityInfo>> ENTITY_CACHE = 
        CacheBuilder.newBuilder().maximumSize(60000).expireAfterWrite(5, TimeUnit.MINUTES).build();

    private final ApiClient apiClient;
    
    /**
     * Initializes a new {@link EntityHelper}.
     * 
     * @param account The underlying file storage account
     * @param apiClient The (logged in) API client to use for accessing the remote share
     */
    public EntityHelper(FileStorageAccount account, ApiClient apiClient) {
        super(account.getFileStorageService().getId(), account.getId());
        this.apiClient = apiClient;
    }

    /**
     * Resolves and builds additional entity info for the users and groups referenced by the supplied permissions under perspective of
     * the API client's user, and returns an array of new permissions enriched by these entity info.
     * 
     * @param permissions The permissions to enhance
     * @return An array with new permissions, enhanced with additional details of the underlying entity
     */
    public Permission[] addEntityInfos(Permission[] permissions) {
        if (null == permissions || 0 == permissions.length) {
            return permissions;
        }
        Permission[] enhancedPermissions = new Permission[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            enhancedPermissions[i] = addEntityInfo(permissions[i]);
        }
        return enhancedPermissions;
    }

    /**
     * Resolves and builds additional entity info for a certain user or group under perspective of the API client's user, and returns
     * a new permission enriched by these entity info.
     * 
     * @param permission The permission to enhance
     * @return A new permission, enhanced with additional details of the underlying entity, or the passed permission as is if no
     *         info could be resolved
     */
    public Permission addEntityInfo(Permission permission) {
        if (null == permission || null != permission.getEntityInfo() || 0 > permission.getEntity() || 0 == permission.getEntity() && false == permission.isGroup()) {
            return permission;
        }
        EntityInfo entityInfo = optEntityInfo(permission.getEntity(), permission.isGroup());
        if (null == entityInfo) {
            return permission;
        }
        BasicPermission enhancedPermission = new BasicPermission(permission);
        enhancedPermission.setEntityInfo(entityInfo);
        return enhancedPermission;
    }

    /**
     * Resolves and builds additional entity info for the users and groups referenced by the supplied permissions under perspective of
     * the API client's user, and returns new object permissions enriched by these entity info.
     * 
     * @param permissions The permissions to enhance
     * @return A list with new object permissions, enhanced with additional details of the underlying entity
     */
    public List<FileStorageObjectPermission> addObjectPermissionEntityInfos(List<FileStorageObjectPermission> permissions) {
        if (null == permissions) {
            return null;
        }
        List<FileStorageObjectPermission> enhancedPermissions = new ArrayList<FileStorageObjectPermission>(permissions.size());
        for (FileStorageObjectPermission permission : permissions) {
            enhancedPermissions.add(addObjectPermissionEntityInfo(permission));
        }
        return enhancedPermissions;
    }

    /**
     * Resolves and builds additional entity info for a certain user or group under perspective of the API client's user, and returns
     * a new object permission enriched by these entity info.
     * 
     * @param permission The permission to enhance
     * @return A new object permission, enhanced with additional details of the underlying entity, or the passed permission as is if no
     *         info could be resolved
     */
    public FileStorageObjectPermission addObjectPermissionEntityInfo(FileStorageObjectPermission permission) {
        if (null == permission || null != permission.getEntityInfo() || 0 > permission.getEntity() || 0 == permission.getEntity() && false == permission.isGroup()) {
            return permission;
        }
        EntityInfo entityInfo = optEntityInfo(permission.getEntity(), permission.isGroup());
        if (null == entityInfo) {
            return permission;
        }
        DefaultFileStorageObjectPermission enhancedPermission = new DefaultFileStorageObjectPermission(
            permission.getIdentifier(), permission.getEntity(), permission.isGroup(), permission.getPermissions());
        enhancedPermission.setEntityInfo(entityInfo);
        return enhancedPermission;
    }

    /**
     * Resolves and builds additional entity info for a certain user or group under perspective of the passed session's user.
     * 
     * @param entity The identifier of the entity to resolve
     * @param isGroup <code>true</code> if the entity refers to a group, <code>false</code>, otherwise
     * @return The entity info, or <code>null</code> if the referenced entity could not be resolved
     */
    public EntityInfo optEntityInfo(int entity, boolean isGroup) {
        if (0 > entity || 0 == entity && false == isGroup) {
            getLogger(EntityHelper.class).warn("Unable to lookup entity info for {}", I(entity));
            return null;
        }
        try {
            return ENTITY_CACHE.get(getCacheKey(apiClient, entity), new Callable<Optional<EntityInfo>>() {

                @Override
                public Optional<EntityInfo> call() throws Exception {
                    return Optional.ofNullable(isGroup ? optGroupInfo(entity) : optUserInfo(entity));
                }
            }).orElse(null);
        } catch (ExecutionException e) {
            getLogger(EntityHelper.class).warn("Error looking entity info {} in account {}", I(entity), apiClient.getLoginLink(), e);
            return null;
        }
    }

    private static String getCacheKey(ApiClient apiClient, int entity) {
        return new StringBuilder()
            .append(apiClient.getUserId()).append('@')
            .append(apiClient.getContextId()).append('@').append(apiClient.getLoginLink()).append(':')
            .append(entity)
        .toString();
    }

    EntityInfo optGroupInfo(int groupId) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("id", String.valueOf(groupId));
        JSONObject groupData;
        try {
            groupData = apiClient.execute(new GetJsonObjectDataCall("group", "get", parameters));
        } catch (OXException e) {
            getLogger(EntityHelper.class).debug("Error looking up group {} in account {}", I(groupId), apiClient.getLoginLink(), e);
            return null;
        }
        int entity = groupData.optInt("id", -1);
        String displayName = groupData.optString("display_name");
        return new EntityInfo(String.valueOf(entity), displayName, null, null, null, null, entity, null, Type.GROUP);
    }

    EntityInfo optUserInfo(int userId) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("id", String.valueOf(userId));
        parameters.put("columns", "2,500,501,502,505,524,555,606");
        JSONObject userData;
        try {
            userData = apiClient.execute(new GetJsonObjectDataCall("user", "get", parameters));
        } catch (OXException e) {
            getLogger(EntityHelper.class).debug("Error looking up user {} in account {}", I(userId), apiClient.getLoginLink(), e);
            return null;
        }
        int entity = userData.optInt("id", -1);
        int guestCreatedBy = userData.optInt("guest_created_by", -1);
        String displayName = userData.optString("display_name");
        String title = userData.optString("title");
        String firstName = userData.optString("first_name");
        String lastName = userData.optString("last_name");
        String email1 = userData.optString("email1");
        String imageUrl = userData.optString("image1_url");
        EntityInfo.Type type = 0 < guestCreatedBy ? (Strings.isEmpty(email1) ? Type.ANONYMOUS : Type.GUEST) : Type.USER;
        return new EntityInfo(String.valueOf(entity), displayName, title, firstName, lastName, email1, entity, imageUrl, type);
    }
    
}
