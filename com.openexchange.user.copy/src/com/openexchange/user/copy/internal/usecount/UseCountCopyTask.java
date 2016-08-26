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

package com.openexchange.user.copy.internal.usecount;

import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.calendar.CalendarCopyTask;
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
            CalendarCopyTask.class.getName(), ContactCopyTask.class.getName(), TaskCopyTask.class.getName() };
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
                FolderObject folderObj = new FolderObject(I(rs.getInt(1)));
                FolderObject destFolderObj = folderMapping.getDestination(folderObj);
                if (destFolderObj == null) {
                    // skip use counts without folder mapping (probably internal users)
                    continue;
                }
                dstStmt.setInt(3, destFolderObj.getObjectID());
                dstStmt.setInt(4, contactMapping.getDestination(I(rs.getInt(2))));
                dstStmt.setInt(5, rs.getInt(3));
                dstStmt.addBatch();
            }
            dstStmt.executeBatch();
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, srcStmt);
            DBUtils.closeSQLStuff(dstStmt);
        }
    }

}
