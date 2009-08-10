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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.internal;

import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;

/**
 * {@link EffectivePermission} - A read-only permission considering user access restrictions and folder boundaries.
 * <p>
 * Access to any of the setXXX() method will throw an {@link UnsupportedOperationException}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EffectivePermission implements Permission {

    /**
     * The configuration profile of the current logged in user.
     */
    private UserConfiguration userConfig;

    /**
     * The type of the referenced folder.
     */
    private final Type type;

    /**
     * The content type of the referenced folder.
     */
    private final ContentType contentType;

    /**
     * The referenced folder's identifier.
     */
    private final String folderId;

    /**
     * The underlying permission.
     */
    private Permission underlyingPerm;

    /**
     * Initializes a new {@link EffectivePermission}.
     * 
     * @param underlyingPerm The underlying permission
     * @param folderId The referenced folder's identifier
     * @param type The type of the referenced folder
     * @param contentType The type of the referenced folder
     * @param userConfig The configuration profile of the current logged in user
     */
    public EffectivePermission(final Permission underlyingPerm, final String folderId, final Type type, final ContentType contentType, final UserConfiguration userConfig) {
        super();
        this.underlyingPerm = underlyingPerm;
        this.folderId = folderId;
        this.contentType = contentType;
        this.type = type;
        this.userConfig = userConfig;
    }

    @Override
    public int hashCode() {
        return underlyingPerm.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Permission)) {
            return false;
        }
        final Permission other = (Permission) obj;
        if (isAdmin() != other.isAdmin()) {
            return false;
        }
        if (getDeletePermission() != other.getDeletePermission()) {
            return false;
        }
        if (getEntity() != other.getEntity()) {
            return false;
        }
        if (getFolderPermission() != other.getFolderPermission()) {
            return false;
        }
        if (isGroup() != other.isGroup()) {
            return false;
        }
        if (getReadPermission() != other.getReadPermission()) {
            return false;
        }
        if (getSystem() != other.getSystem()) {
            return false;
        }
        if (getWritePermission() != other.getWritePermission()) {
            return false;
        }
        return true;
    }

    private boolean hasModuleAccess() {
        return userConfig.hasModuleAccess(getModule());
    }

    private int getType() {
        return type.getType();
    }

    private int getModule() {
        return contentType.getModule();
    }

    public int getDeletePermission() {
        if (!hasModuleAccess()) {
            return NO_PERMISSIONS;
        } else if ((FolderObject.PUBLIC == getType()) || String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID).equals(folderId)) {
            if ((getModule() != FolderObject.INFOSTORE) && !userConfig.hasFullPublicFolderAccess()) {
                return NO_PERMISSIONS;
                /*
                 * return super.getDeletePermission() > DELETE_ALL_OBJECTS ? DELETE_ALL_OBJECTS : super .getDeletePermission();
                 */
            }
        } else if (!userConfig.hasFullSharedFolderAccess() && (FolderObject.SHARED == getType())) {
            return NO_PERMISSIONS;
        }
        return underlyingPerm.getDeletePermission();
    }

    public int getEntity() {
        return underlyingPerm.getEntity();
    }

    public int getFolderPermission() {
        if (!hasModuleAccess()) {
            return NO_PERMISSIONS;
        } else if ((FolderObject.PUBLIC == getType()) || String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID).equals(folderId)) {
            if ((getModule() != FolderObject.INFOSTORE) && !userConfig.hasFullPublicFolderAccess()) {
                return underlyingPerm.getFolderPermission() > READ_FOLDER ? READ_FOLDER : underlyingPerm.getFolderPermission();
            }
        } else if (!userConfig.hasFullSharedFolderAccess() && (FolderObject.SHARED == getType())) {
            return NO_PERMISSIONS;
        }
        return underlyingPerm.getFolderPermission();
    }

    public int getReadPermission() {
        if (!hasModuleAccess()) {
            return NO_PERMISSIONS;
        } else if ((FolderObject.PUBLIC == getType()) || String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID).equals(folderId)) {
            if ((getModule() != FolderObject.INFOSTORE) && !userConfig.hasFullPublicFolderAccess()) {
                return underlyingPerm.getReadPermission() > READ_ALL_OBJECTS ? READ_ALL_OBJECTS : underlyingPerm.getReadPermission();
            }
        } else if (!userConfig.hasFullSharedFolderAccess() && (FolderObject.SHARED == getType())) {
            return NO_PERMISSIONS;
        }
        return underlyingPerm.getReadPermission();
    }

    public int getSystem() {
        return underlyingPerm.getSystem();
    }

    public int getWritePermission() {
        if (!hasModuleAccess()) {
            return NO_PERMISSIONS;
        } else if ((FolderObject.PUBLIC == getType()) || String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID).equals(folderId)) {
            if ((getModule() != FolderObject.INFOSTORE) && !userConfig.hasFullPublicFolderAccess()) {
                return NO_PERMISSIONS;
            }
        } else if (!userConfig.hasFullSharedFolderAccess() && (FolderObject.SHARED == getType())) {
            return NO_PERMISSIONS;
        }
        return underlyingPerm.getWritePermission();
    }

    public boolean isAdmin() {
        if (!hasModuleAccess()) {
            return false;
        } else if ((FolderObject.PUBLIC == getType()) && (getModule() != FolderObject.INFOSTORE) && !userConfig.hasFullPublicFolderAccess()) {
            return false;
        }
        return underlyingPerm.isAdmin();
    }

    public boolean isGroup() {
        return underlyingPerm.isGroup();
    }

    public void setAdmin(final boolean admin) {
        throw new UnsupportedOperationException("EffectivePermission.setAdmin()");
    }

    public void setAllPermissions(final int folderPermission, final int readPermission, final int writePermission, final int deletePermission) {
        throw new UnsupportedOperationException("EffectivePermission.setAllPermissions()");
    }

    public void setDeletePermission(final int permission) {
        throw new UnsupportedOperationException("EffectivePermission.setDeletePermission()");
    }

    public void setEntity(final int entity) {
        throw new UnsupportedOperationException("EffectivePermission.setEntity()");
    }

    public void setFolderPermission(final int permission) {
        throw new UnsupportedOperationException("EffectivePermission.setFolderPermission()");
    }

    public void setGroup(final boolean group) {
        throw new UnsupportedOperationException("EffectivePermission.setGroup()");
    }

    public void setMaxPermissions() {
        throw new UnsupportedOperationException("EffectivePermission.setMaxPermissions()");
    }

    public void setNoPermissions() {
        throw new UnsupportedOperationException("EffectivePermission.setNoPermissions()");
    }

    public void setReadPermission(final int permission) {
        throw new UnsupportedOperationException("EffectivePermission.setReadPermission()");
    }

    public void setSystem(final int system) {
        throw new UnsupportedOperationException("EffectivePermission.setSystem()");
    }

    public void setWritePermission(final int permission) {
        throw new UnsupportedOperationException("EffectivePermission.setWritePermission()");
    }

    @Override
    public Object clone() {
        try {
            final EffectivePermission clone = (EffectivePermission) super.clone();
            clone.userConfig = (UserConfiguration) userConfig.clone();
            clone.underlyingPerm = (Permission) underlyingPerm.clone();
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

}
