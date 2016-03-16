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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.SimpleUpdateTask;

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
                closeSQLStuff(rs, stmt);
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
                closeSQLStuff(stmt);

                stmt = con.prepareStatement("INSERT INTO oxfolder_permissions (cid,fuid,permission_id,fp,orp,owp,odp,admin_flag,group_flag,system) " +
                		"VALUES (?,15,0,8,0,0,0,0,1,0)");
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
                closeSQLStuff(stmt);

                stmt = con.prepareStatement("INSERT INTO oxfolder_permissions (cid,fuid,permission_id,fp,orp,owp,odp,admin_flag,group_flag,system) " +
                    "VALUES (?,15,2,8,0,0,0,1,0,0)");
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
            } finally {
                closeSQLStuff(stmt);
            }
        }
    }

}
