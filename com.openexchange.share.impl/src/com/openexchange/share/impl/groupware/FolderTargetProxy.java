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

package com.openexchange.share.impl.groupware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.folderstorage.DefaultPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.DriveTargetProxyType;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetProxyType;


/**
 * {@link FolderTargetProxy}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FolderTargetProxy extends AbstractTargetProxy {

    private final UserizedFolder folder;
    private final ShareTarget target;
    private final ShareTargetPath targetPath;


    public FolderTargetProxy(int module, UserizedFolder folder) {
        super();
        this.folder = folder;
        target = new ShareTarget(module, folder.getID(), null);
        targetPath = new ShareTargetPath(module, folder.getID(), null);
    }

    @Override
    public String getID() {
        return folder.getID();
    }

    @Override
    public String getFolderID() {
        return folder.getParentID();
    }

    @Override
    public ShareTarget getTarget() {
        return target;
    }

    @Override
    public ShareTargetPath getTargetPath() {
        return targetPath;
    }

    @Override
    public String getTitle() {
        String name = folder.getLocalizedName(folder.getUser().getLocale(), true);
        if (name == null) {
            name = folder.getName();
            if (name == null) {
                name = folder.getID();
            }
        }

        return name;
    }

    @Override
    public List<TargetPermission> getPermissions() {
        Permission[] permissions = folder.getPermissions();
        if (null == permissions) {
            return Collections.emptyList();
        }
        List<TargetPermission> targetPermissions = new ArrayList<TargetPermission>(permissions.length);
        for (Permission permission : permissions) {
            targetPermissions.add(CONVERTER.convert(permission));
        }
        return targetPermissions;
    }

    @Override
    public void applyPermissions(List<TargetPermission> permissions) {
        Permission[] origPermissionArray = folder.getPermissions();
        if (origPermissionArray == null) {
            origPermissionArray = new Permission[0];
        }

        List<Permission> origPermissions = new ArrayList<Permission>(origPermissionArray.length);
        Collections.addAll(origPermissions, origPermissionArray);
        List<Permission> newPermissions = mergePermissions(origPermissions, permissions, CONVERTER);
        folder.setPermissions(newPermissions.toArray(new Permission[newPermissions.size()]));
        setModified();
    }

    @Override
    public void removePermissions(List<TargetPermission> permissions) {
        Permission[] origPermissionArray = folder.getPermissions();
        if (origPermissionArray == null || origPermissionArray.length == 0) {
            return;
        }

        List<Permission> origPermissions = new ArrayList<Permission>(origPermissionArray.length);
        Collections.addAll(origPermissions, origPermissionArray);
        List<Permission> newPermissions = removePermissions(origPermissions, permissions, CONVERTER);
        folder.setPermissions(newPermissions.toArray(new Permission[newPermissions.size()]));
        setModified();
    }

    public UserizedFolder getFolder() {
        return folder;
    }

    @Override
    public TargetProxyType getProxyType() {
        return DriveTargetProxyType.FOLDER;
    }

    @Override
    public boolean mayAdjust() {
        return folder.getOwnPermission().isAdmin();
    }

    @Override
    public Date getTimestamp() {
        return folder.getLastModifiedUTC();
    }

    private static PermissionConverter<Permission> CONVERTER = new PermissionConverter<Permission>() {
        @Override
        public int getEntity(Permission permission) {
            return permission.getEntity();
        }

        @Override
        public boolean isGroup(Permission permission) {
            return permission.isGroup();
        }

        @Override
        public boolean isSystem(Permission permission) {
            return permission.getSystem() > 0;
        }

        @Override
        public int getBits(Permission permission) {
            return Permissions.createPermissionBits(permission);
        }

        @Override
        public Permission convert(TargetPermission permission) {
            return new DefaultPermission(permission.getEntity(), permission.isGroup(), permission.getBits());
        }

        @Override
        public TargetPermission convert(Permission permission) {
            return new TargetPermission(permission.getEntity(), permission.isGroup(), getBits(permission));
        }
    };

}
