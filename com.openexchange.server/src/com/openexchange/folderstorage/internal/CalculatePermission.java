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

package com.openexchange.folderstorage.internal;

import static com.openexchange.osgi.util.ServiceCallWrapper.doServiceCall;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.internal.performers.AbstractPerformer;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceUser;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link CalculatePermission} - Utility class to obtain an effective permission.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CalculatePermission {

    /**
     * Initializes a new {@link CalculatePermission}.
     */
    private CalculatePermission() {
        super();
    }

    /**
     * Calculates the effective user permissions for given folder.
     *
     * @param folder The folder whose effective user permissions shall be calculated
     * @param context The context
     */
    public static void calculateUserPermissions(Folder folder, Context context) {
        Permission[] staticPermissions = folder.getPermissions();
        if (null == staticPermissions || 0 == staticPermissions.length) {
            return;
        }
        UserPermissionBitsStorage userConfStorage = UserPermissionBitsStorage.getInstance();
        String id = folder.getID();
        Type type = folder.getType();
        ContentType contentType = folder.getContentType();

        Permission[] userizedPermissions = new Permission[staticPermissions.length];
        TIntIntHashMap toLoad = new TIntIntHashMap(staticPermissions.length);
        for (int index = 0; index < staticPermissions.length; index++) {
            Permission staticPermission = staticPermissions[index];
            if (0 == staticPermission.getSystem()) {
                // A non-system permission
                if (staticPermission.isGroup()) {
                    userizedPermissions[index] = staticPermission;
                } else {
                    // Load appropriate user configuration
                    toLoad.put(staticPermission.getEntity(), index);
                }
            }
        }
        /*
         * Batch-load user configurations
         */
        if (!toLoad.isEmpty()) {
            int[] userIds = toLoad.keys();
            try {
                UserPermissionBits[] configurations = userConfStorage.getUserPermissionBits(context, userIds);
                for (int i = 0; i < configurations.length; i++) {
                    int userId = userIds[i];
                    if (toLoad.containsKey(userId)) {
                        int index = toLoad.get(userId);
                        UserPermissionBits userPermissionBits = configurations[i];
                        userizedPermissions[index] = new EffectivePermission(
                            staticPermissions[index],
                            id,
                            type,
                            contentType,
                            userPermissionBits,
                            Collections.<ContentType> emptyList()).setEntityInfo(userId, context);
                    }
                }
            } catch (OXException e) {
                final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CalculatePermission.class);
                logger.warn("User configuration could not be loaded. Ignoring user permissions.", e);
            }
        }
        /*
         * Remove possible null values & apply to folder
         */
        java.util.List<Permission> tmp = new ArrayList<Permission>(userizedPermissions.length);
        for (int i = 0; i < userizedPermissions.length; i++) {
            Permission p = userizedPermissions[i];
            if (null != p) {
                tmp.add(p);
            }
        }
        folder.setPermissions(tmp.toArray(new Permission[tmp.size()]));
    }

    /**
     * Calculates the effective permission for given user in given folder.
     *
     * @param folder The folder
     * @param performer The performer to calculate for
     * @param allowedContentTypes The allowed content types; an empty list indicates all are allowed
     * @return The effective permission for given user in given folder
     * @throws OXException If calculating the effective permission fails
     */
    public static Permission calculate(final Folder folder, final AbstractPerformer performer, final java.util.List<ContentType> allowedContentTypes) throws OXException {
        final ServerSession session = performer.getSession();
        if (null == session) {
            return calculate(folder, performer.getUser(), performer.getContext(), allowedContentTypes);
        }
        return calculate(folder, session, allowedContentTypes);
    }

    /**
     * Calculates the effective permission for given user in given folder.
     *
     * @param folder The folder
     * @param user The user
     * @param context The context
     * @param allowedContentTypes The allowed content types; an empty list indicates all are allowed
     * @return The effective permission for given user in given folder
     * @throws OXException If calculating the effective permission fails
     */
    public static Permission calculate(final Folder folder, final User user, final Context context, final java.util.List<ContentType> allowedContentTypes) throws OXException {
        try {
            UserPermissionBits userPermissionBits = getUserPermissionBits(user.getId(), context);
            return new EffectivePermission(getMaxPermission(folder.getPermissions(), userPermissionBits), folder.getID(), folder.getType(), folder.getContentType(), userPermissionBits, allowedContentTypes).setEntityInfo(user.getId(), context);
        } catch (OXException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }
    }

    public static boolean isVisible(final Folder folder, final User user, final Context context, final java.util.List<ContentType> allowedContentTypes) throws OXException {
        final UserPermissionBits userPermissionBits;
        try {
            userPermissionBits = getUserPermissionBits(user.getId(), context);
        } catch (OXException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }


        /*
         * Check visibility by folder
         */
        Type type = folder.getType();
        ContentType contentType = folder.getContentType();
        if (PrivateType.getInstance().equals(type)) {
            int createdBy = folder.getCreatedBy();
            if (createdBy <= 0 || createdBy == user.getId()) {
                return hasAccess(contentType, userPermissionBits, allowedContentTypes);
            }
        }

        /*
         * Check visibility by effective permission
         */
        return new EffectivePermission(getMaxPermission(folder.getPermissions(), userPermissionBits), folder.getID(), type, contentType, userPermissionBits, allowedContentTypes).isVisible();
    }

    private static boolean hasAccess(final ContentType contentType, final UserPermissionBits permissionBits, final java.util.List<ContentType> allowedContentTypes) {
        final int module = contentType.getModule();
        if (!permissionBits.hasModuleAccess(module)) {
            return false;
        }
        if (null == allowedContentTypes || allowedContentTypes.isEmpty()) {
            return true;
        }
        final TIntSet set = new TIntHashSet(allowedContentTypes.size() + 2);
        for (final ContentType allowedContentType : allowedContentTypes) {
            set.add(allowedContentType.getModule());
        }
        // Module SYSTEM is allowed in any case
        set.add(FolderObject.SYSTEM_MODULE);
        set.add(FolderObject.UNBOUND);
        return set.isEmpty() ? true : set.contains(module);
    }

    private static final int[] mapping = { 0, 2, 4, -1, 8 };

    /**
     * The actual max permission that can be transfered in field 'bits' or JSON's permission object
     */
    private static final int MAX_PERMISSION = 64;

    private static final int[] parsePermissionBits(final int bitsArg) {
        int bits = bitsArg;
        final int[] retval = new int[5];
        for (int i = retval.length - 1; i >= 0; i--) {
            final int shiftVal = (i * 7); // Number of bits to be shifted
            retval[i] = bits >> shiftVal;
            bits -= (retval[i] << shiftVal);
            if (retval[i] == MAX_PERMISSION) {
                retval[i] = Permission.MAX_PERMISSION;
            } else if (i < (retval.length - 1)) {
                retval[i] = mapping[retval[i]];
            } else {
                retval[i] = retval[i];
            }
        }
        return retval;
    }

    /**
     * Calculates the effective permission for given session's user in given folder.
     *
     * @param folder The folder
     * @param session The session
     * @param allowedContentTypes The allowed content types; an empty list indicates all are allowed
     * @return The effective permission for given session's user in given folder
     */
    public static Permission calculate(final Folder folder, final ServerSession session, final java.util.List<ContentType> allowedContentTypes) {
        final UserPermissionBits userPermissionBits = session.getUserPermissionBits();
        return new EffectivePermission(
            getMaxPermission(folder.getPermissions(), userPermissionBits),
            folder.getID(),
            folder.getType(),
            folder.getContentType(),
            userPermissionBits,
            allowedContentTypes);
    }

    private static User[] getUsers(final int[] userIds, final Context context) throws OXException {
        try {
            return doServiceCall(CalculatePermission.class, UserService.class,
                new ServiceUser<UserService, User[]>() {
                    @Override
                    public User[] call(UserService service) throws Exception {
                        return service.getUser(context, userIds);
                    }
                });
        } catch (ServiceException e) {
            throw e.toOXException();
        }
    }

    private static UserPermissionBits getUserPermissionBits(final int userId, final Context context) throws OXException {
        try {
            return doServiceCall(CalculatePermission.class, UserPermissionService.class,
                new ServiceUser<UserPermissionService, UserPermissionBits>() {
                    @Override
                    public UserPermissionBits call(UserPermissionService service) throws Exception {
                        return service.getUserPermissionBits(userId, context);
                    }
                });
        } catch (ServiceException e) {
            throw e.toOXException();
        }
    }

    private static UserPermissionBits[] getUserPermissionBits(final Context context, final User[] users) throws OXException {
        try {
            return doServiceCall(CalculatePermission.class, UserPermissionService.class,
                new ServiceUser<UserPermissionService, UserPermissionBits[]>() {
                    @Override
                    public UserPermissionBits[] call(UserPermissionService service) throws Exception {
                        return service.getUserPermissionBits(context, users);
                    }
                });
        } catch (ServiceException e) {
            throw e.toOXException();
        }
    }

    private static Permission getMaxPermission(final Permission[] permissions, final UserPermissionBits userPermissionBits) {
        DummyPermission p = new DummyPermission();
        p.setNoPermissions();
        p.setEntity(userPermissionBits.getUserId());

        if (null == permissions || 0 == permissions.length) {
            return p;
        }

        TIntSet ids = getEntityIdsFor(userPermissionBits);
        int fp = 0;
        int rp = 0;
        int wp = 0;
        int dp = 0;
        boolean admin = false;
        for (Permission cur : permissions) {
            if (ids.contains(cur.getEntity())) {
                // Folder permission
                int tmp = cur.getFolderPermission();
                if (tmp > fp) {
                    fp = tmp;
                }
                // Read permission
                tmp = cur.getReadPermission();
                if (tmp > rp) {
                    rp = tmp;
                }
                // Write permission
                tmp = cur.getWritePermission();
                if (tmp > wp) {
                    wp = tmp;
                }
                // Delete permission
                tmp = cur.getDeletePermission();
                if (tmp > dp) {
                    dp = tmp;
                }
                // Admin flag
                if (!admin) {
                    admin = cur.isAdmin();
                }

            }
        }
        if (admin) {
            p.setAdmin(admin);
        }
        p.setAllPermissions(fp, rp, wp, dp);

        return p;
    }

    private static TIntSet getEntityIdsFor(final UserPermissionBits userPermissionBits) {
        int[] groups = userPermissionBits.getGroups();
        TIntSet ids = new TIntHashSet(groups.length + 1);
        ids.add(userPermissionBits.getUserId());
        ids.addAll(groups);
        return ids;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /**
     * A {@link Permission} implementation.
     */
    public static final class DummyPermission implements Permission {

        private static final long serialVersionUID = 5488824197214654462L;

        private int system;
        private int deletePermission;
        private int folderPermission;
        private int readPermission;
        private int writePermission;
        private boolean admin;
        private int entity;
        private boolean group;

        /**
         * Initializes an empty {@link DummyPermission}.
         */
        public DummyPermission() {
            super();
        }

        @Override
        public boolean isVisible() {
            return isAdmin() || getFolderPermission() > NO_PERMISSIONS;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (admin ? 1231 : 1237);
            result = prime * result + deletePermission;
            result = prime * result + entity;
            result = prime * result + folderPermission;
            result = prime * result + (group ? 1231 : 1237);
            result = prime * result + readPermission;
            result = prime * result + system;
            result = prime * result + writePermission;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Permission)) {
                return false;
            }
            final Permission other = (Permission) obj;
            if (admin != other.isAdmin()) {
                return false;
            }
            if (deletePermission != other.getDeletePermission()) {
                return false;
            }
            if (entity != other.getEntity()) {
                return false;
            }
            if (folderPermission != other.getFolderPermission()) {
                return false;
            }
            if (group != other.isGroup()) {
                return false;
            }
            if (readPermission != other.getReadPermission()) {
                return false;
            }
            if (system != other.getSystem()) {
                return false;
            }
            if (writePermission != other.getWritePermission()) {
                return false;
            }
            return true;
        }

        @Override
        public int getDeletePermission() {
            return deletePermission;
        }

        @Override
        public int getEntity() {
            return entity;
        }

        @Override
        public int getFolderPermission() {
            return folderPermission;
        }

        @Override
        public int getReadPermission() {
            return readPermission;
        }

        @Override
        public int getSystem() {
            return system;
        }

        @Override
        public int getWritePermission() {
            return writePermission;
        }

        @Override
        public boolean isAdmin() {
            return admin;
        }

        @Override
        public boolean isGroup() {
            return group;
        }

        @Override
        public void setAdmin(final boolean admin) {
            this.admin = admin;
        }

        @Override
        public void setAllPermissions(final int folderPermission, final int readPermission, final int writePermission, final int deletePermission) {
            this.folderPermission = folderPermission;
            this.readPermission = readPermission;
            this.deletePermission = deletePermission;
            this.writePermission = writePermission;
        }

        @Override
        public void setDeletePermission(final int permission) {
            deletePermission = permission;
        }

        @Override
        public void setEntity(final int entity) {
            this.entity = entity;
        }

        @Override
        public void setFolderPermission(final int permission) {
            folderPermission = permission;
        }

        @Override
        public void setGroup(final boolean group) {
            this.group = group;
        }

        @Override
        public void setMaxPermissions() {
            folderPermission = Permission.MAX_PERMISSION;
            readPermission = Permission.MAX_PERMISSION;
            deletePermission = Permission.MAX_PERMISSION;
            writePermission = Permission.MAX_PERMISSION;
            admin = true;
        }

        @Override
        public void setNoPermissions() {
            folderPermission = Permission.NO_PERMISSIONS;
            readPermission = Permission.NO_PERMISSIONS;
            deletePermission = Permission.NO_PERMISSIONS;
            writePermission = Permission.NO_PERMISSIONS;
            admin = false;
        }

        @Override
        public void setReadPermission(final int permission) {
            readPermission = permission;
        }

        @Override
        public void setSystem(final int system) {
            this.system = system;
        }

        @Override
        public void setWritePermission(final int permission) {
            writePermission = permission;
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (final CloneNotSupportedException e) {
                throw new InternalError(e.getMessage());
            }
        }

    } // End of DummyPermission

}
