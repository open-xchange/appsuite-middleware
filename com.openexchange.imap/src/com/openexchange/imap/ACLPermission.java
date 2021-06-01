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

package com.openexchange.imap;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.acl.ACLExtension;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.entity2acl.Entity2ACL;
import com.openexchange.imap.entity2acl.Entity2ACLArgs;
import com.openexchange.imap.entity2acl.Entity2ACLExceptionCode;
import com.openexchange.imap.entity2acl.UserGroupID;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.server.impl.OCLPermission;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;

/**
 * {@link ACLPermission} - Maps existing folder permissions to corresponding IMAP ACL.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ACLPermission extends MailPermission {

    private static final long serialVersionUID = -3140342221453395764L;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ACLPermission.class);

    private transient ACL acl;

    private int canRename;
    private int canStoreSeenFlag;

    /**
     * Initializes a new {@link ACLPermission}.
     */
    public ACLPermission() {
        super();
        canRename = -1;
        canStoreSeenFlag = -1;
    }

    @Override
    public void setEntity(int entity) {
        super.setEntity(entity);
        acl = null;
    }

    @Override
    public void setFolderAdmin(boolean folderAdmin) {
        super.setFolderAdmin(folderAdmin);
        acl = null;
    }

    @Override
    public void setGroupPermission(boolean groupPermission) {
        super.setGroupPermission(groupPermission);
        acl = null;
    }

    @Override
    public boolean setFolderPermission(int p) {
        acl = null;
        return super.setFolderPermission(p);
    }

    @Override
    public boolean setReadObjectPermission(int p) {
        acl = null;
        return super.setReadObjectPermission(p);
    }

    @Override
    public boolean setWriteObjectPermission(int p) {
        acl = null;
        return super.setWriteObjectPermission(p);
    }

    @Override
    public boolean setDeleteObjectPermission(int p) {
        acl = null;
        return super.setDeleteObjectPermission(p);
    }

    @Override
    public boolean setAllObjectPermission(int pr, int pw, int pd) {
        acl = null;
        return super.setAllObjectPermission(pr, pw, pd);
    }

    @Override
    public boolean setAllPermission(int fp, int opr, int opw, int opd) {
        acl = null;
        return super.setAllPermission(fp, opr, opw, opd);
    }

    private static final String ERR = "This method is not applicable to an ACL permission";

    @Override
    public void setFuid(int pid) {
        LOG.warn(ERR);
    }

    @Override
    public int getFuid() {
        LOG.warn(ERR);
        return -1;
    }

    @Override
    public void reset() {
        super.reset();
        acl = null;
    }

    @Override
    public int canRename() {
        return canRename;
    }

    /**
     * Sets the rename flag.
     *
     * @param canRename The rename flag
     */
    public void setCanRename(int canRename) {
        this.canRename = canRename;
    }

    @Override
    public int canStoreSeenFlag() {
        return canStoreSeenFlag;
    }

    /**
     * Sets the canStoreSeenFlag
     *
     * @param canStoreSeenFlag The store <code>"seen"</code> flag permission to set
     */
    public void setCanStoreSeenFlag(int canStoreSeenFlag) {
        this.canStoreSeenFlag = canStoreSeenFlag;
    }

    /*-
     * Full rights: "acdilprsw"
     */

    /**
     * Maps this permission to ACL rights and fills them into an instance of {@link ACL}.
     *
     * @param args The IMAP-server-specific arguments used for mapping
     * @param imapConfig The user's IMAP configuration
     * @param imapStore The IMAP store
     * @param ctx The context
     * @return An instance of {@link ACL} representing this permission's rights
     * @throws OXException If this permission cannot be mapped to an instance of {@link ACL}
     */
    public ACL getPermissionACL(Entity2ACLArgs args, IMAPConfig imapConfig, IMAPStore imapStore, Context ctx) throws OXException {
        if (acl != null) {
            /*
             * Return caches ACL
             */
            return acl;
        }
        Rights rights = permission2Rights(this, imapConfig);
        if (isGroupPermission() && OCLPermission.ALL_GROUPS_AND_USERS != getEntity()) {
            // Group not supported
            GroupService groups = Services.getService(GroupService.class);
            Group group = groups.getGroup(ctx, getEntity());
            throw Entity2ACLExceptionCode.UNKNOWN_GROUP.create(I(getEntity()), I(ctx.getContextId()), imapConfig.getServer(), group.getDisplayName());
        }
        return (acl = new ACL(Entity2ACL.getInstance(imapStore, imapConfig).getACLName(getEntity(), ctx, args), rights));
    }

    /**
     * Parses the rights given through specified instance of {@link ACL} into this permission object.
     *
     * @param acl The source instance of {@link ACL}
     * @param args The IMAP-server-specific arguments used for mapping
     * @param imapStore The IMAP store
     * @param imapConfig The user's IMAP configuration
     * @param ctx The context
     * @throws OXException If given ACL cannot be applied to this permission
     */
    public void parseACL(ACL acl, Entity2ACLArgs args, IMAPStore imapStore, IMAPConfig imapConfig, Context ctx) throws OXException {
        final UserGroupID res = Entity2ACL.getInstance(imapStore, imapConfig).getEntityID(acl.getName(), ctx, args);
        setEntity(res.getId());
        setGroupPermission(res.isGroup());
        parseRights(acl.getRights(), imapConfig);
        this.acl = acl;
    }

    /**
     * Parses given rights into this permission object
     *
     * @param rights -The rights to parse
     * @param imapConfig The IMAP configuration
     */
    public void parseRights(Rights rights, IMAPConfig imapConfig) {
        rights2Permission(rights, this, imapConfig);
        canRename = imapConfig.getACLExtension().canCreate(rights) ? 1 : 0;
        canStoreSeenFlag = imapConfig.getACLExtension().canKeepSeen(rights) ? 1 : 0;
    }

    /**
     * Maps given permission to rights
     *
     * @param permission The permission
     * @param imapConfig The IMAP configuration
     * @return Mapped rights
     */
    public static Rights permission2Rights(OCLPermission permission, IMAPConfig imapConfig) {
        final Rights rights = new Rights();
        final ACLExtension aclExtension = imapConfig.getACLExtension();
        boolean hasAnyRights = false;
        if (permission.isFolderAdmin()) {
            aclExtension.addFolderAdminRights(rights);
            hasAnyRights = true;
        }
        if (permission.canCreateSubfolders()) {
            aclExtension.addCreateSubfolders(rights);
            hasAnyRights = true;
        } else if (permission.canCreateObjects()) {
            aclExtension.addCreateObjects(rights);
            hasAnyRights = true;
        } else if (permission.isFolderVisible()) {
            aclExtension.addFolderVisibility(rights);
            hasAnyRights = true;
        }
        if (permission.getReadPermission() >= OCLPermission.READ_ALL_OBJECTS) {
            aclExtension.addReadAllKeepSeen(rights);
            hasAnyRights = true;
        }
        if (permission.getWritePermission() >= OCLPermission.WRITE_ALL_OBJECTS) {
            aclExtension.addWriteAll(rights);
            hasAnyRights = true;
        }
        if (permission.getDeletePermission() >= OCLPermission.DELETE_ALL_OBJECTS) {
            aclExtension.addDeleteAll(rights);
            hasAnyRights = true;
        }
        if (hasAnyRights) {
            aclExtension.addNonMappable(rights);
        }
        return rights;
    }

    /**
     * Parses specified rights into given permission object. If the latter parameter is left to <code>null</code>, a new instance of
     * {@link OCLPermission} is going to be created, filled, and returned. Otherwise the given instance of {@link OCLPermission} is filled
     * and returned.
     *
     * @param rights The rights to parse
     * @param permission The permission object which may be <code>null</code>
     * @param imapConfig The IMAP configuration
     * @return The corresponding permission
     */
    public static OCLPermission rights2Permission(Rights rights, OCLPermission permission, IMAPConfig imapConfig) {
        final OCLPermission oclPermission = permission == null ? new OCLPermission() : permission;
        final ACLExtension aclExtension = imapConfig.getACLExtension();
        /*
         * Folder admin
         */
        oclPermission.setFolderAdmin(aclExtension.containsFolderAdminRights(rights));
        /*
         * Folder permission
         */
        if (aclExtension.containsCreateSubfolders(rights)) {
            oclPermission.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
        } else if (aclExtension.containsCreateObjects(rights)) {
            oclPermission.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
        } else if (aclExtension.containsFolderVisibility(rights)) {
            oclPermission.setFolderPermission(OCLPermission.READ_FOLDER);
        } else {
            oclPermission.setFolderPermission(OCLPermission.NO_PERMISSIONS);
        }
        /*
         * Read permission
         */
        if (aclExtension.containsReadAll(rights)) {
            oclPermission.setReadObjectPermission(OCLPermission.READ_ALL_OBJECTS);
        } else {
            oclPermission.setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
        }
        /*
         * Write permission
         */
        if (aclExtension.containsWriteAll(rights)) {
            oclPermission.setWriteObjectPermission(OCLPermission.WRITE_ALL_OBJECTS);
        } else {
            oclPermission.setWriteObjectPermission(OCLPermission.NO_PERMISSIONS);
        }
        /*
         * Delete permission
         */
        if (aclExtension.containsDeleteAll(rights)) {
            oclPermission.setDeleteObjectPermission(OCLPermission.DELETE_ALL_OBJECTS);
        } else {
            oclPermission.setDeleteObjectPermission(OCLPermission.NO_PERMISSIONS);
        }
        return oclPermission;
    }

    @Override
    public Object clone() {
        try {
            final ACLPermission clone = (ACLPermission) super.clone();
            // if (null == acl) {
            // clone.acl = null;
            // } else {
            // clone.acl = new ACL(acl.getName(), (Rights) acl.getRights().clone());
            // }
            clone.acl = null;
            return clone;
        } catch (CloneNotSupportedException e) {
            LOG.error("", e);
            throw new RuntimeException("CloneNotSupportedException even though it's cloenable", e);
        }
    }
}
