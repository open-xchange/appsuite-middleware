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

package com.openexchange.server.impl;

import java.util.Arrays;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderProperties;

/**
 * EffectivePermission
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class EffectivePermission extends OCLPermission {

    private static final char CHAR_AT = '@';

    private static final char CHAR_BREAK = '\n';

    private static final char CHAR_PIPE = '|';

    private static final String STR_EFFECTIVE_PERMISSION = "EffectivePermission_";

    private static final String STR_EMPTY = "";

    private static final String STR_ADMIN = "Admin";

    private static final String STR_USER = "User";

    private static final String STR_GROUP = "Group";

    private static final int GAB = FolderObject.SYSTEM_LDAP_FOLDER_ID;

    private static final long serialVersionUID = -1303754404748836561L;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EffectivePermission.class);

    /**
     * The configuration profile of the current logged in user
     */
    private final UserPermissionBits permissionBits;

    /**
     * A reference to the associated folder object. Only needed when a <code>UserConfiguration</code> instance is present.
     */
    private int folderType = -1;

    /**
     * A reference to the associated folder object. Only needed when a <code>UserConfiguration</code> instance is present.
     */
    private int folderModule = -1;

    private final int createdBy;

    private boolean userConfigIsValid;

    private boolean userConfigAlreadyValidated;

    private OCLPermission underlyingPerm;

    public EffectivePermission(final int entity, final int fuid, final int folderType, final int folderModule, final int createdBy, final UserConfiguration userConfig) {
        super(entity, fuid);
        this.folderType = folderType;
        this.folderModule = folderModule;
        this.createdBy = createdBy;
        this.permissionBits = userConfig.getUserPermissionBits();
        validateUserConfig();
    }

    public EffectivePermission(final int entity, final int fuid, final int folderType, final int folderModule, final int createdBy, final UserPermissionBits permissionBits) {
        super(entity, fuid);
        this.folderType = folderType;
        this.folderModule = folderModule;
        this.createdBy = createdBy;
        this.permissionBits = permissionBits;
        validateUserConfig();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if (other == null || !(other instanceof EffectivePermission)) {
            return false;
        }
        final EffectivePermission ep = (EffectivePermission) other;
        if (!super.equals(ep) && (folderType != ep.folderType) && (folderModule != ep.folderModule)) {
            return false;
        }
        if (createdBy != ep.createdBy) {
            return false;
        }
        if (null != permissionBits) {
            return permissionBits.equals(ep.permissionBits);
        }
        return (null == ep.permissionBits);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + super.hashCode();
        hash = 31 * hash + folderType;
        hash = 31 * hash + folderModule;
        hash = 31 * hash + createdBy;
        if (null != permissionBits) {
            hash = 31 * hash + permissionBits.hashCode();
        }
        return hash;
    }

    public boolean hasModuleAccess(final int folderModule) {
        if (validateUserConfig()) {
            return permissionBits.hasModuleAccess(folderModule);
        }
        return true;
    }

    @Override
    public boolean isFolderAdmin() {
        if (FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID == getFuid()) {
            return super.isFolderAdmin();
        }
        if (validateUserConfig()) {
            if (!hasModuleAccess(folderModule)) {
                return false;
            } else if (folderType == FolderObject.PUBLIC && folderModule != FolderObject.INFOSTORE && !permissionBits.hasFullPublicFolderAccess()) {
                return false;
            }
            /*
             * else if (folderType == FolderObject.SHARED && !userConfig.hasFullSharedFolderAccess()) { return false; }
             */
        }
        return ((folderType == FolderObject.PUBLIC) && (permissionBits.getUserId() == createdBy)) || super.isFolderAdmin();
    }

    @Override
    public void setFolderAdmin(final boolean folderAdmin) {
        super.setFolderAdmin(folderAdmin);
        underlyingPerm = null;
    }

    @Override
    public void setGroupPermission(final boolean groupPermission) {
        super.setGroupPermission(groupPermission);
        underlyingPerm = null;
    }

    @Override
    public int getFolderPermission() {
        if (FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID == getFuid()) {
            return super.getFolderPermission();
        }
        if (validateUserConfig()) {
            if (!hasModuleAccess(folderModule)) {
                return NO_PERMISSIONS;
            } else if (isPublicFolder()) {
                if (folderModule != FolderObject.INFOSTORE && !permissionBits.hasFullPublicFolderAccess()) {
                    final int superFolderPermission = super.getFolderPermission();
                    return superFolderPermission > READ_FOLDER ? READ_FOLDER : superFolderPermission;
                }
            } else if (!permissionBits.hasFullSharedFolderAccess() && folderType == FolderObject.SHARED) {
                return NO_PERMISSIONS;
            }
        }
        return super.getFolderPermission();
    }

    @Override
    public boolean setFolderPermission(final int fp) {
        underlyingPerm = null;
        return super.setFolderPermission(fp);
    }

    @Override
    public int getReadPermission() {
        if (FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID == getFuid()) {
            return super.getReadPermission();
        }
        if (validateUserConfig()) {
            if (!hasModuleAccess(folderModule)) {
                return NO_PERMISSIONS;
            } else if (isPublicFolder()) {
                if (folderModule != FolderObject.INFOSTORE && !permissionBits.hasFullPublicFolderAccess()) {
                    final int superReadPermission = super.getReadPermission();
                    return superReadPermission > READ_ALL_OBJECTS ? READ_ALL_OBJECTS : superReadPermission;
                }
            } else if (!permissionBits.hasFullSharedFolderAccess() && folderType == FolderObject.SHARED) {
                return NO_PERMISSIONS;
            }
        }
        return super.getReadPermission();
    }

    @Override
    public boolean setReadObjectPermission(final int p) {
        underlyingPerm = null;
        return super.setReadObjectPermission(p);
    }

    @Override
    public int getWritePermission() {
        if (GAB == getFuid()) {
            int writePermission = super.getWritePermission();
            return OXFolderProperties.isEnableInternalUsersEdit() ? (writePermission <= NO_PERMISSIONS ? WRITE_OWN_OBJECTS : writePermission) : NO_PERMISSIONS;
        } else if (FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID == getFuid()) {
            return super.getWritePermission();
        }
        if (validateUserConfig()) {
            if (!hasModuleAccess(folderModule)) {
                return NO_PERMISSIONS;
            } else if (isPublicFolder()) {
                if (folderModule != FolderObject.INFOSTORE && !permissionBits.hasFullPublicFolderAccess()) {
                    return NO_PERMISSIONS;
                }
            } else if (!permissionBits.hasFullSharedFolderAccess() && folderType == FolderObject.SHARED) {
                return NO_PERMISSIONS;
            }
        }
        return super.getWritePermission();
    }

    @Override
    public boolean setWriteObjectPermission(final int p) {
        underlyingPerm = null;
        return super.setWriteObjectPermission(p);
    }

    @Override
    public int getDeletePermission() {
        if (FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID == getFuid()) {
            return super.getDeletePermission();
        }
        if (validateUserConfig()) {
            if (!hasModuleAccess(folderModule)) {
                return NO_PERMISSIONS;
            } else if (isPublicFolder()) {
                if (folderModule != FolderObject.INFOSTORE && !permissionBits.hasFullPublicFolderAccess()) {
                    return NO_PERMISSIONS;
                    /*
                     * return super.getDeletePermission() > DELETE_ALL_OBJECTS ? DELETE_ALL_OBJECTS : super .getDeletePermission();
                     */
                }
            } else if (!permissionBits.hasFullSharedFolderAccess() && folderType == FolderObject.SHARED) {
                return NO_PERMISSIONS;
            }
        }
        return super.getDeletePermission();
    }

    @Override
    public boolean setDeleteObjectPermission(final int p) {
        underlyingPerm = null;
        return super.setDeleteObjectPermission(p);
    }

    @Override
    public void setEntity(final int entity) {
        underlyingPerm = null;
        super.setEntity(entity);
    }

    @Override
    public void setFuid(final int fuid) {
        underlyingPerm = null;
        super.setFuid(fuid);
    }

    private static final int[] SYSTEM_PUBLIC_FOLDERS = {
        FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_GLOBAL_FOLDER_ID, FolderObject.SYSTEM_LDAP_FOLDER_ID };

    private boolean isPublicFolder() {
        return ((folderType == FolderObject.PUBLIC) || (Arrays.binarySearch(SYSTEM_PUBLIC_FOLDERS, getFuid())) >= 0);
    }

    private final boolean validateUserConfig() {
        if (userConfigAlreadyValidated) {
            return userConfigIsValid;
        }
        /*
         * Permission's entity should be equal to config's user or should be contained in config's groups. Otherwise given configuration
         * does not refer to permission's entity and has no effect on OCL permissions.
         */
        userConfigAlreadyValidated = true;
        userConfigIsValid = false;
        if (permissionBits == null) {
            return userConfigIsValid;
        }
        try {
            final int fuid = getFuid();
            if (fuid <= 0) {
                return userConfigIsValid;
            }
            OXFolderAccess folderAccess = null;
            if (folderType <= 0) {
                folderType = (folderAccess = new OXFolderAccess(permissionBits.getContext())).getFolderType(fuid, permissionBits.getUserId());
            }
            if (folderModule <= 0) {
                if (folderAccess == null) {
                    folderAccess = new OXFolderAccess(permissionBits.getContext());
                }
                folderModule = folderAccess.getFolderModule(fuid);
            }
        } catch (final OXException e) {
            LOG.error("", e);
            return userConfigIsValid;
        }
        if (permissionBits.getUserId() == getEntity()) {
            userConfigIsValid = true;
        } else {
            final int[] groups = permissionBits.getGroups();
            for (int i = 0; i < groups.length && !userConfigIsValid; i++) {
                userConfigIsValid = (groups[i] == getEntity());
            }
        }
        return userConfigIsValid;
    }

    @Override
    public boolean isFolderVisible() {
        return (isFolderAdmin() || (getFolderPermission() >= READ_FOLDER));
    }

    @Override
    public boolean canCreateObjects() {
        return (getFolderPermission() >= CREATE_OBJECTS_IN_FOLDER);
    }

    @Override
    public boolean canCreateSubfolders() {
        return (getFolderPermission() >= CREATE_SUB_FOLDERS);
    }

    @Override
    public boolean canReadOwnObjects() {
        return (getReadPermission() >= READ_OWN_OBJECTS);
    }

    @Override
    public boolean canReadAllObjects() {
        return (getReadPermission() >= READ_ALL_OBJECTS);
    }

    @Override
    public boolean canWriteOwnObjects() {
        return (getWritePermission() >= WRITE_OWN_OBJECTS);
    }

    @Override
    public boolean canWriteAllObjects() {
        return (getWritePermission() >= WRITE_ALL_OBJECTS);
    }

    @Override
    public boolean canDeleteOwnObjects() {
        return (getDeletePermission() >= DELETE_OWN_OBJECTS);
    }

    @Override
    public boolean canDeleteAllObjects() {
        return (getDeletePermission() >= DELETE_ALL_OBJECTS);
    }

    /**
     * Gets a value indicating whether the user's permissions allow public folder access, i.e. the bit
     * {@link UserPermissionBits#EDIT_PUBLIC_FOLDERS} is set or not.
     *
     * @return <code>true</code> if public folder access is available, <code>false</code>, otherwise
     */
    public boolean hasFullPublicFolderAccess() {
        return permissionBits.hasFullPublicFolderAccess();
    }

    /**
     * Gets a value indicating whether the user's permissions allow shared folder access, i.e. the bit
     * {@link UserPermissionBits#READ_CREATE_SHARED_FOLDERS} is set or not.
     *
     * @return <code>true</code> if shared folder access is available, <code>false</code>, otherwise
     */
    public boolean hasFullSharedFolderAccess() {
        return permissionBits.hasFullSharedFolderAccess();
    }

    /**
     * Gets the underlying folder's type.
     *
     * @return The folder type
     */
    public int getFolderType() {
        if (0 >= folderType) {
            try {
                folderType = new OXFolderAccess(permissionBits.getContext()).getFolderType(getFuid(), permissionBits.getUserId());
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
        return folderType;
    }

    /**
     * Gets the underlying folder's module.
     *
     * @return The folder module
     */
    public int getFolderModule() {
        if (0 >= folderModule) {
            try {
                folderModule = new OXFolderAccess(permissionBits.getContext()).getFolderModule(getFuid());
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
        return folderModule;
    }

    public OCLPermission getUnderlyingPermission() {
        if (underlyingPerm == null) {
            underlyingPerm = new OCLPermission();
            underlyingPerm.setEntity(super.getEntity());
            underlyingPerm.setFuid(super.getFuid());
            underlyingPerm.setFolderPermission(super.getFolderPermission());
            underlyingPerm.setReadObjectPermission(super.getReadPermission());
            underlyingPerm.setWriteObjectPermission(super.getWritePermission());
            underlyingPerm.setDeleteObjectPermission(super.getDeletePermission());
            underlyingPerm.setFolderAdmin(super.isFolderAdmin());
            underlyingPerm.setGroupPermission(super.isGroupPermission());
        }
        return underlyingPerm;
    }

    @Override
    public String toString() {
        return new StringBuilder(150).append(STR_EFFECTIVE_PERMISSION).append((isFolderAdmin() ? STR_ADMIN : STR_EMPTY)).append(
            (isGroupPermission() ? STR_GROUP : STR_USER)).append(CHAR_AT).append(getFolderPermission()).append(CHAR_PIPE).append(
            getReadPermission()).append(CHAR_PIPE).append(getWritePermission()).append(CHAR_PIPE).append(getDeletePermission()).append(
            CHAR_BREAK).append(super.toString()).toString();
    }

}
