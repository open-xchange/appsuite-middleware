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

package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.SimpleUpdateTask;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * {@link CheckForPublicInfostoreFolderTask} - Checks for missing folder 'public_infostore' (15) in any available context.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CheckForPublicInfostoreFolderTask extends SimpleUpdateTask {

    public CheckForPublicInfostoreFolderTask() {
        super();
    }

    @Override
    protected void perform(final Connection con) throws SQLException {
        final TIntList contextIds;
        {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
                stmt = con.prepareStatement("SELECT t1.cid FROM oxfolder_tree AS t1 WHERE "+FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID+" NOT IN (SELECT fuid FROM oxfolder_tree AS t2 WHERE t2.cid = t1.cid) GROUP BY cid");
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return;
                }
                contextIds = new TIntArrayList(32);
                do {
                    contextIds.add(rs.getInt(1));
                } while (rs.next());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        }
        if (contextIds.isEmpty()) {
            return;
        }
        for (final int contextId : contextIds.toArray()) {
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement("INSERT INTO oxfolder_tree " +
                		"(fuid,cid,parent,fname,module,type,creating_date,created_from,changing_date,changed_from,permission_flag,subfolder_flag,default_flag) " +
                		"VALUES (15,?,9,'public_infostore',8,5,1220981203760,2,1220981203760,2,2,1,0)");
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);

                stmt = con.prepareStatement("INSERT INTO oxfolder_permissions (cid,fuid,permission_id,fp,orp,owp,odp,admin_flag,group_flag,system) " +
                		"VALUES (?,15,0,8,0,0,0,0,1,0)");
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);

                stmt = con.prepareStatement("INSERT INTO oxfolder_permissions (cid,fuid,permission_id,fp,orp,owp,odp,admin_flag,group_flag,system) " +
                    "VALUES (?,15,2,8,0,0,0,1,0,0)");
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
    }

}
