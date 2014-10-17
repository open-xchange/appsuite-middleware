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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.impl.groupware;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.DefaultPermission;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleHandler;
import com.openexchange.share.recipient.InternalRecipient;
import com.openexchange.user.UserService;


/**
 * {@link AbstractModuleHandler}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class AbstractModuleHandler implements ModuleHandler {

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractModuleHandler}.
     */
    protected AbstractModuleHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getTargetTitle(ShareTarget target, Session session) throws OXException {
        UserService userService = getService(UserService.class);
        if (target.isFolder()) {
            UserizedFolder folder = getFolderService().getFolder(FolderStorage.REAL_TREE_ID, target.getFolder(), session, null);
            User user = userService.getUser(session.getUserId(), session.getContextId());
            String name = folder.getLocalizedName(user.getLocale(), true);
            if (name == null) {
                name = folder.getName();
                if (name == null) {
                    name = folder.getID();
                }
            }

            return name;
        }

        return getItemTitle(target.getFolder(), target.getItem(), session);
    }

    /**
     * Gets the human-readable title for the given item.
     *
     * @param folder The items parent folder
     * @param item The items id
     * @param session The session
     * @return The title; never <code>null</code>
     * @throws OXException
     */
    protected abstract String getItemTitle(String folder, String item, Session session) throws OXException;

    @Override
    public void updateFolders(List<ShareTarget> folders, List<InternalRecipient> finalRecipients, Session session, Connection writeCon) throws OXException {
        FolderService folderService = getFolderService();
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        decorator.put(Connection.class.getName(), writeCon);
        for (ShareTarget target : folders) {
            /*
             * 1. Get folders
             * 2. Merge permissions
             * 3. Write back
             */
            UserizedFolder folder = folderService.getFolder(FolderStorage.REAL_TREE_ID, target.getFolder(), session, decorator);
            mergePermissions(folder, finalRecipients);
            folderService.updateFolder(folder, folder.getLastModified(), session, decorator);
        }
    }

    private void mergePermissions(UserizedFolder folder, List<InternalRecipient> finalRecipients) {
        Permission[] origPermissions = folder.getPermissions();
        List<Permission> newPermissions = new ArrayList<Permission>(origPermissions == null ? 0 : origPermissions.length + finalRecipients.size());

        if (origPermissions == null || origPermissions.length == 0) {
            for (InternalRecipient recipient : finalRecipients) {
                newPermissions.add(new DefaultPermission(recipient.getEntity(), recipient.isGroup(), recipient.getBits()));
            }
        } else {
            Map<Integer, Permission> permissionsByEntity = new HashMap<Integer, Permission>();
            for (Permission permission : origPermissions) {
                permissionsByEntity.put(permission.getEntity(), permission);
            }

            for (InternalRecipient recipient : finalRecipients) {
                permissionsByEntity.remove(recipient.getEntity());
                newPermissions.add(new DefaultPermission(recipient.getEntity(), recipient.isGroup(), recipient.getBits()));
            }

            for (Permission permission : permissionsByEntity.values()) {
                newPermissions.add(permission);
            }
        }

        folder.setPermissions(newPermissions.toArray(new Permission[newPermissions.size()]));
    }

    /**
     * Takes a folder permission bit mask and deduces the according object permissions.
     *
     * @param folderPermissionBits The folder permission bit mask
     * @return The object permission bits
     */
    protected int getObjectPermissionBits(int folderPermissionBits) {
        int objectBits = ObjectPermission.NONE;
        int[] permissionBits = Permissions.parsePermissionBits(folderPermissionBits);
        int rp = permissionBits[1];
        int wp = permissionBits[2];
        int dp = permissionBits[3];
        if (dp >= Permission.DELETE_ALL_OBJECTS) {
            objectBits = ObjectPermission.DELETE;
        } else if (wp >= Permission.WRITE_ALL_OBJECTS) {
            objectBits = ObjectPermission.WRITE;
        } else if (rp >= Permission.READ_ALL_OBJECTS) {
            objectBits = ObjectPermission.READ;
        }

        return objectBits;
    }

    protected FolderService getFolderService() throws OXException {
        return getService(FolderService.class);
    }

    protected <T> T getService(Class<T> clazz) throws OXException {
        T service = services.getService(clazz);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }

        return service;
    }

}
