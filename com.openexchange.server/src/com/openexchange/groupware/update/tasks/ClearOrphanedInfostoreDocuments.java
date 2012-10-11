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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.ForeignKeyOld;
import com.openexchange.tools.update.Tools;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ClearOrphanedInfostoreDocuments implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ClearOrphanedInfostoreDocuments.class));

    @Override
    public int addedWithVersion() {
        return 26;
    }

    @Override
    public int getPriority() {
        return UpdateTask.UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(Schema schema, int contextId) throws OXException {
        PreparedStatement select = null;
        PreparedStatement delete = null;
        PreparedStatement addKey = null;
        Connection con = null;
        ResultSet rs = null;
        try {
            LOG.info("Clearing orphaned infostore document versions");
            con = Database.getNoTimeout(contextId, true);

            List<ForeignKeyOld> keys = ForeignKeyOld.getForeignKeys(con, "infostore_document");
            ForeignKeyOld fk = new ForeignKeyOld("infostore_document", "infostore_id", "infostore", "id");

            if( keys.contains(fk)) {
                LOG.info("Foreign Key "+fk+" exists. Skipping Update Task.");
                return;
            }

            con.setAutoCommit(false);
            select = con.prepareStatement("SELECT doc.cid, doc.infostore_id, doc.version_number, doc.file_store_location FROM infostore_document AS doc LEFT JOIN infostore AS info ON info.cid = doc.cid AND info.id = doc.infostore_id WHERE info.id IS NULL");
            delete  = con.prepareStatement("DELETE FROM infostore_document WHERE cid = ? AND infostore_id = ? AND version_number = ?");
            addKey = con.prepareStatement("ALTER TABLE infostore_document ADD FOREIGN KEY (cid, infostore_id) REFERENCES infostore (cid, id)");
            rs = select.executeQuery();

            int counter = 0;
            while(rs.next()) {
                int cid = rs.getInt(1);
                int id = rs.getInt(2);
                int version = rs.getInt(3);
                String fileStoreLocation = rs.getString(4);

                delete.setInt(1, cid);
                delete.setInt(2, id);
                delete.setInt(3, version);
                delete.executeUpdate();
                if (null != fileStoreLocation) {
                    // Version 0 has no file.
                    Tools.removeFile(cid, fileStoreLocation);
                } else if (0 != version) {
                    LOG.warn("Found file version without location in filestore. cid:" + cid + ",id:" + id + ",version:" + version + ".");
                }
                counter++;
            }
            LOG.info("Cleared "+counter+" orphaned documents");

            LOG.info("Adding foreign key: "+fk);

            // Need one with CID as well, so FK system doesn't work.
            addKey.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException e1) {
                // IGNORE
            }
            LOG.error(e.getMessage(),e);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, select);
            DBUtils.closeSQLStuff(null, delete);
            DBUtils.closeSQLStuff(null, addKey);
            DBUtils.autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }
}
