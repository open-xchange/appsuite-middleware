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

package com.openexchange.user.copy.internal.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.RdbUserPermissionBitsStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;


/**
 * {@link UserCopyTask} - Loads the user from it's origin context and creates it within the destination context.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UserCopyTask implements CopyUserTaskService {

    private final UserService userService;
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link UserCopyTask}.
     *
     * @param userService The user service
     * @param databaseService The database service
     */
    public UserCopyTask(final UserService userService, DatabaseService databaseService) {
        super();
        this.userService = userService;
        this.databaseService = databaseService;
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return User.class.getName();
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public UserMapping copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools tools = new CopyTools(copied);
        final Context srcCtx = tools.getSourceContext();
        final Context dstCtx = tools.getDestinationContext();
        final Integer srcUsrId = tools.getSourceUserId();
        final Connection srcCon = tools.getSourceConnection();
        final Connection dstCon = tools.getDestinationConnection();

        boolean error = true;
        boolean filestoreUsageEntryCreated = false;
        int dstUsrId = 0;
        try {
            UserMapping mapping = new UserMapping();
            User srcUser = userService.getUser(srcCon, srcUsrId.intValue(), srcCtx);
            if (userExistsInDestinationCtx(dstCtx, srcUser, dstCon)) {
                throw UserCopyExceptionCodes.USER_ALREADY_EXISTS.create(srcUser.getLoginInfo(), Integer.valueOf(dstCtx.getContextId()));
            }

            int fileStorageOwner = srcUser.getFileStorageOwner();
            if (fileStorageOwner > 0) {
                // Cannot copy a user whose files belong to another user in source context
                throw UserCopyExceptionCodes.FILE_STORAGE_CONFLICT.create(fileStorageOwner, srcCtx.getContextId());
            }

            dstUsrId = userService.createUser(dstCon, dstCtx, srcUser);
            User dstUser = userService.getUser(dstCon, dstUsrId, dstCtx);

            // Check for individual file storage
            if (srcUser.getFilestoreId() > 0 && srcUser.getFileStorageOwner() <= 0) {
                // Ensure appropriate entry in 'filestore_usage' table using a separate connection to avoid deadlock later on
                // caused by "SELECT ... FOR UPDATE" in 'DBQuotaFileStorage.incUsage()'
                ensureFilestoreUsageEntry(dstUsrId, dstCtx);
                filestoreUsageEntryCreated = true;
            }

            /*
             * user configuration
             */
            try {
                final UserPermissionBits[] srcConfiguration = RdbUserPermissionBitsStorage.loadUserPermissionBits(srcCtx, srcCon, new int[] {srcUser.getId()});
                RdbUserPermissionBitsStorage.saveUserPermissionBits(srcConfiguration[0].getPermissionBits(), dstUser.getId(), true, dstCtx.getContextId(), dstCon);
            } catch (final OXException e) {
                throw UserCopyExceptionCodes.DB_POOLING_PROBLEM.create(e);
            } catch (final SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            }

            mapping.addMapping(srcUser.getId(), srcUser, dstUsrId, dstUser);
            error = false;
            return mapping;
        } catch (final OXException e) {
            throw UserCopyExceptionCodes.USER_SERVICE_PROBLEM.create(e);
        } finally {
            if (error && filestoreUsageEntryCreated) {
                // Drop previously created entry
                dropFilestoreUsageEntry(dstUsrId, dstCtx);
            }
        }
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
        //
    }

    private boolean userExistsInDestinationCtx(final Context dstCtx, final User srcUser, final Connection dstCon) throws OXException {
        final int dstCtxId = dstCtx.getContextId();
        final String srcUserName = srcUser.getLoginInfo();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = dstCon.prepareStatement("SELECT 1 FROM login2user WHERE cid = ? AND uid = ?");
            stmt.setInt(1, dstCtxId);
            stmt.setString(2, srcUserName);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private void ensureFilestoreUsageEntry(int userId, Context context) throws OXException {
        if (userId <= 0) {
            return;
        }

        Connection con = databaseService.getWritable(context);
        boolean modified = false;
        try {
            modified = ensureFilestoreUsageEntry0(userId, context, con);
        } finally {
            if (modified) {
                databaseService.backWritable(context, con);
            } else {
                databaseService.backWritableAfterReading(context, con);
            }
        }
    }

    private boolean ensureFilestoreUsageEntry0(int userId, Context context, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO filestore_usage (cid,user,used) VALUES (?,?,0)");
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, userId);
            try {
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                if (!Databases.isPrimaryKeyConflictInMySQL(e)) {
                    throw e;
                }
                // All fine... Seems that it already exists
                return false;
            }
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private void dropFilestoreUsageEntry(int userId, Context context) throws OXException {
        if (userId <= 0) {
            return;
        }

        Connection con = databaseService.getWritable(context);
        boolean modified = false;
        try {
            modified = dropFilestoreUsageEntry0(userId, context, con);
        } finally {
            if (modified) {
                databaseService.backWritable(context, con);
            } else {
                databaseService.backWritableAfterReading(context, con);
            }
        }
    }

    private boolean dropFilestoreUsageEntry0(int userId, Context context, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM filestore_usage WHERE cid=? AND user=?");
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
