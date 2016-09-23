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

package com.openexchange.tools.oxfolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RemoveInconsistentLocksUpdateTasks} removes all file locks which may be hold by any user which doesn't have any permissions to do so anymore.
 *
 * See bug #47929 and #48907
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class RemoveInconsistentLocksUpdateTasks extends UpdateTaskAdapter {

    private final String SQL = "DELETE l FROM infostore_lock AS l INNER JOIN infostore AS i ON l.cid=i.cid and l.entity=i.id WHERE "
        + "i.folder_id NOT IN (SELECT fuid FROM oxfolder_permissions AS fp WHERE fp.cid=l.cid AND fp.permission_id=l.userid AND owp!=0) AND "
        + "l.entity NOT IN (SELECT object_id FROM object_permission AS op WHERE op.cid=l.cid AND op.permission_id=l.userid AND op.folder_id=i.folder_id AND op.bits=2);";

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.UpdateTaskV2#perform(com.openexchange.groupware.update.PerformParameters)
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);
        Connection con = null;
        PreparedStatement stmt = null;
        int rows = 0;
        try {
            con = dbService.getForUpdateTask(params.getContextId());
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL);
            rows = stmt.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            DBUtils.rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (rows > 0) {
                dbService.backForUpdateTask(params.getContextId(), con);
            } else {
                dbService.backForUpdateTaskAfterReading(params.getContextId(), con);
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.UpdateTaskV2#getDependencies()
     */
    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.FolderPermissionReadAllForUserInfostore.class.getName() };
    }

}
