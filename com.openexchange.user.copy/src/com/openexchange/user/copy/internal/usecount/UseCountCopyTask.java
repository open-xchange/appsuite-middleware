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

package com.openexchange.user.copy.internal.usecount;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.user.User;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.chronos.ChronosCopyTask;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.contact.ContactCopyTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.tasks.TaskCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link UseCountCopyTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class UseCountCopyTask implements CopyUserTaskService {

    /**
     * Initializes a new {@link UseCountCopyTask}.
     */
    public UseCountCopyTask() {
        super();
    }

    @Override
    public String[] getAlreadyCopied() {
        return new String[] { UserCopyTask.class.getName(), ContextLoadTask.class.getName(), ConnectionFetcherTask.class.getName(), FolderCopyTask.class.getName(),
            ChronosCopyTask.class.getName(), ContactCopyTask.class.getName(), TaskCopyTask.class.getName() };
    }

    @Override
    public String getObjectName() {
        return "useCount";
    }

    @Override
    public ObjectMapping<?> copyUser(Map<String, ObjectMapping<?>> copied) throws OXException {
        CopyTools copyTools = new CopyTools(copied);
        Integer srcCtxId = copyTools.getSourceContextId();
        Integer dstCtxId = copyTools.getDestinationContextId();
        Integer dstUsrId = copyTools.getDestinationUserId();
        User srcUsr = copyTools.getSourceUser();
        Connection srcCon = copyTools.getSourceConnection();
        Connection dstCon = copyTools.getDestinationConnection();

        ObjectMapping<Integer> contactMapping = copyTools.checkAndExtractGenericMapping(Contact.class.getName());
        ObjectMapping<FolderObject> folderMapping = copyTools.checkAndExtractGenericMapping(FolderObject.class.getName());
        correctObjectUseCountTable(srcCon, i(srcCtxId), srcUsr.getId(), dstCon, i(dstCtxId), i(dstUsrId), contactMapping, folderMapping);
        return null;
    }

    @Override
    public void done(Map<String, ObjectMapping<?>> copied, boolean failed) {
        // nothing
    }

    private void correctObjectUseCountTable(Connection srcCon, int srcCtxId, int srcUserId, Connection dstCon, int dstCtxId, int dstUserId, ObjectMapping<Integer> contactMapping, ObjectMapping<FolderObject> folderMapping) throws OXException {
        String sql = "SELECT folder, object, value FROM object_use_count WHERE cid = ? AND user = ?";
        String dstSql = "INSERT INTO object_use_count (cid, user, folder, object, value) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement srcStmt = null;
        PreparedStatement dstStmt = null;
        ResultSet rs = null;
        try {
            dstStmt = dstCon.prepareStatement(dstSql);
            srcStmt = srcCon.prepareStatement(sql);
            srcStmt.setInt(1, srcCtxId);
            srcStmt.setInt(2, srcUserId);
            rs = srcStmt.executeQuery();
            while (rs.next()) {
                dstStmt.setInt(1, dstCtxId);
                dstStmt.setInt(2, dstUserId);
                FolderObject folderObj = new FolderObject(rs.getInt(1));
                FolderObject destFolderObj = folderMapping.getDestination(folderObj);
                if (destFolderObj == null) {
                    // skip use counts without folder mapping (probably internal users)
                    continue;
                }
                Integer destObjectId = contactMapping.getDestination(I(rs.getInt(2)));
                if (null == destObjectId) {
                    // skip use counts for contacts that weren't copied
                    continue;
                }
                dstStmt.setInt(3, destFolderObj.getObjectID());
                dstStmt.setInt(4, destObjectId.intValue());
                dstStmt.setInt(5, rs.getInt(3));
                dstStmt.addBatch();
            }
            dstStmt.executeBatch();
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            Databases.closeSQLStuff(rs, srcStmt);
            Databases.closeSQLStuff(dstStmt);
        }
    }

}
