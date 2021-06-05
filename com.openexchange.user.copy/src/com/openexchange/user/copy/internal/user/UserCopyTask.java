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

package com.openexchange.user.copy.internal.user;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.unified.UnifiedQuotaService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.RdbUserPermissionBitsStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.user.User;
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
        final int srcUsrId = tools.getSourceUserId().intValue();
        final Connection srcCon = tools.getSourceConnection();
        final Connection dstCon = tools.getDestinationConnection();

        boolean error = true;
        boolean filestoreUsageEntryCreated = false;
        int dstUsrId = 0;
        try {
            UserMapping mapping = new UserMapping();
            User srcUser = userService.getUser(srcCon, srcUsrId, srcCtx);
            if (userExistsInDestinationCtx(dstCtx, srcUser, dstCon)) {
                throw UserCopyExceptionCodes.USER_ALREADY_EXISTS.create(srcUser.getLoginInfo(), Integer.valueOf(dstCtx.getContextId()));
            }

            int fileStorageOwner = srcUser.getFileStorageOwner();
            if (fileStorageOwner > 0) {
                // Cannot copy a user whose files belong to another user in source context
                throw UserCopyExceptionCodes.FILE_STORAGE_CONFLICT.create(I(fileStorageOwner), I(srcCtx.getContextId()));
            }
            String qfsMode = FileStorages.getQuotaFileStorageService().getQuotaFileStorage(srcUsrId, srcCtx.getContextId(), Info.drive()).getMode();
            if (UnifiedQuotaService.MODE.equals(qfsMode)) {
                // Cannot copy a user using unified quota
                throw UserCopyExceptionCodes.UNIFIED_QUOTA_CONFLICT.create(I(srcUsrId), I(srcCtx.getContextId()));
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
            } catch (OXException e) {
                throw UserCopyExceptionCodes.DB_POOLING_PROBLEM.create(e);
            } catch (SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            }

            mapping.addMapping(I(srcUser.getId()), srcUser, I(dstUsrId), dstUser);
            error = false;
            return mapping;
        } catch (OXException e) {
            if (UserCopyExceptionCodes.prefix().equals(e.getPrefix())) {
                throw e;
            }
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
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
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
