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
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.EntityInfo.Type;
import com.openexchange.groupware.LinkEntityInfo;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.subscription.EntityMangler;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link EntityHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class EntityHelper extends EntityMangler {

    private final XctxAccountAccess accountAccess;

    /**
     * Initializes a new {@link EntityHelper}.
     * 
     * @param accountAccess The parent account access
     */
    public EntityHelper(XctxAccountAccess accountAccess) {
        super(accountAccess.getService().getId(), accountAccess.getAccountId());
        this.accountAccess = accountAccess;
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

}
