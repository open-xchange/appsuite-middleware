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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.update.Tools.createIndex;
import static com.openexchange.tools.update.Tools.createPrimaryKey;
import static com.openexchange.tools.update.Tools.dropIndex;
import static com.openexchange.tools.update.Tools.dropPrimaryKey;
import static com.openexchange.tools.update.Tools.existsIndex;
import static com.openexchange.tools.update.Tools.existsPrimaryKey;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.databaseold.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.tools.update.Tools;

/**
 * Update task for improving indexes with version 6.10.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public class CorrectIndexes6_10 implements UpdateTask {

    private static final Log LOG = LogFactory.getLog(CorrectIndexes6_10.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(CorrectIndexes6_10.class);

    public CorrectIndexes6_10() {
        super();
    }

    public int addedWithVersion() {
        return 48;
    }

    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR },
        desc = { "" },
        exceptionId = { 1 },
        msg = { "An SQL error occurred: %1$s." }
    )
    public void perform(Schema schema, int contextId) throws AbstractOXException {
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            con.setAutoCommit(false);
            correctAppointmentParticipantIndexes(con);
            correctInfoStorePrimaryKey(con);
            correctInfoStoreLastModified(con);
            dropInfoStoreDocumentLastModified(con);
            createInfoStoreFolderIndex(con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw EXCEPTION.create(1, e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    private void correctAppointmentParticipantIndexes(Connection con) {
        String[] columns = { "cid", "member_uid", "object_id" };
        for (String table : new String[] { "prg_dates_members", "del_dates_members" }) {
            try {
                String indexName = existsIndex(con, table, columns);
                if (null == indexName) {
                    LOG.info("Creating new index named member with columns (cid,member_uid,object_id) on table " + table + ".");
                    createIndex(con, table, "member", columns, true);
                } else {
                    LOG.info("New unique index named " + indexName + " with columns (cid,member_uid,object_id) already exists on table " + table + ".");
                }
            } catch (SQLException e) {
                LOG.error("Problem correcting indexes on table " + table + ".", e);
            }
        }
    }

    private void correctInfoStorePrimaryKey(Connection con) {
        for (String table : new String[] { "infostore", "del_infostore" }) {
            String[] columns = { "cid", "id" };
            String documentTable = table + "_document";
            String[] documentForeignKeyColumns = { "cid", "infostore_id" };
            try {
                if (!existsPrimaryKey(con, table, columns)) {
                    String foreignKey = Tools.existsForeignKey(con, table, columns, documentTable, documentForeignKeyColumns);
                    if (null != foreignKey) {
                        LOG.info("Removing foreign key on " + documentTable + " referencing " + table + " temporarily.");
                        Tools.dropForeignKey(con, documentTable, foreignKey);
                    }
                    LOG.info("Removing old primary key (cid,id,folder_id) from table " + table + ".");
                    dropPrimaryKey(con, table);
                    LOG.info("Creating new primary key (cid,id) on table " + table + ".");
                    createPrimaryKey(con, table, columns);
                    if (null != foreignKey) {
                        foreignKey = Tools.existsForeignKey(con, table, columns, documentTable, documentForeignKeyColumns);
                        if (null == foreignKey) {
                            LOG.info("Recreating foreign key on " + documentTable + " referencing " + table + ".");
                            Tools.createForeignKey(con, documentTable, documentForeignKeyColumns, table, columns);
                        }
                    }
                } else {
                    LOG.info("New primary key (ci,id) already exists on table " + table + ".");
                }
            } catch (SQLException e) {
                LOG.error("Problem correcting primary key on table " + table + ".", e);
            }
        }
    }

    private void correctInfoStoreLastModified(Connection con) {
        String[] columns = { "cid", "last_modified" };
        for (String table : new String[] { "infostore", "del_infostore" }) {
            try {
                String indexName = existsIndex(con, table, new String[] { "last_modified" });
                if (null != indexName) {
                    LOG.info("Removing old index with columns (last_modified) on table " + table + ".");
                    dropIndex(con, table, indexName);
                } else {
                    LOG.info("Old index with columns (last_modified) on table " + table  + " not found.");
                }
                indexName = existsIndex(con, table, columns);
                if (null == indexName) {
                    LOG.info("Creating new index named lastModified with columns (cid,last_modified) on table " + table + ".");
                    createIndex(con, table, "lastModified", columns, false);
                } else {
                    LOG.info("New index named " + indexName + " with columns (cid,last_modified) already exists on table " + table + ".");
                }
            } catch (SQLException e) {
                LOG.error("Problem correcting indexes on table " + table + ".", e);
            }
        }
    }

    private void dropInfoStoreDocumentLastModified(Connection con) {
        String[] columns = { "last_modified" };
        for (String table : new String[] { "infostore_document", "del_infostore_document" }) {
            try {
                String indexName = existsIndex(con, table, columns);
                if (null != indexName) {
                    LOG.info("Removing old index with columns (last_modified) on table " + table + ".");
                    dropIndex(con, table, indexName);
                } else {
                    LOG.info("Old index with columns (last_modified) on table " + table + " not found.");
                }
            } catch (SQLException e) {
                LOG.error("Problem correcting indexes on table " + table + ".", e);
            }
        }
    }

    private void createInfoStoreFolderIndex(Connection con) {
        String[] columns = { "cid", "folder_id" };
        for (String table : new String[] { "infostore", "del_infostore" }) {
            try {
                String indexName = existsIndex(con, table, columns);
                if (null == indexName) {
                    LOG.info("Creating new index named folder with columns (cid,folder_id) on table " + table + ".");
                    createIndex(con, table, "folder", columns, false);
                } else {
                    LOG.info("New index named " + indexName + " with columns (cid,folder_id) already exists on table " + table + ".");
                }
            } catch (SQLException e) {
                LOG.error("Problem correcting indexes on table " + table + ".", e);
            }
        }
    }
}
