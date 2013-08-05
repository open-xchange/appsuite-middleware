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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.log.LogFactory;

public class InfostoreRenamePersonalInfostoreFolders implements UpdateTask {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(InfostoreRenamePersonalInfostoreFolders.class));

    @Override
    public int addedWithVersion() {
        return 8;
    }

    @Override
    public int getPriority() {
        return UpdateTask.UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(final Schema schema, final int contextId)
            throws OXException {
        try {
            final List<NameCollision> collisions = NameCollision.getCollisions(contextId, getParentFolder());

            for(final NameCollision collision : collisions) {
                collision.resolve();
            }

        } catch (final SQLException e) {
            LOG.error("Error resolving name collisions: ",e);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
    }

    protected int getParentFolder() {
        return FolderObject.SYSTEM_INFOSTORE_FOLDER_ID;
    }


    private static final class NameCollision {
        private final String name;
        private final int contextId;

        private int nameCount = 1;

        public NameCollision(final String name, final int contextId) {
            this.name = name;
            this.contextId = contextId;
        }

        public void resolve() throws SQLException, OXException {
            LOG.info(String.format("Resolving name collisions for folders named %s in context %d", name, contextId));

            Connection writeCon = null;
            PreparedStatement stmt = null;
            PreparedStatement checkAvailable = null;
            ResultSet rs = null;

            try {
                writeCon = Database.get(contextId, true);
                writeCon.setAutoCommit(false);
                stmt = writeCon.prepareStatement("UPDATE oxfolder_tree SET fname = ? WHERE cid = ? and fuid = ?");
                stmt.setInt(2, contextId);

                checkAvailable = writeCon.prepareStatement("SELECT 1 FROM oxfolder_tree WHERE cid = ? AND fname = ?");
                checkAvailable.setInt(1, contextId);


                final List<Integer> rename = discoverIds(writeCon);

                for(final int id : rename) {
                    String newName = String.format("%s (%d)", name, nameCount++);
                    boolean free = false;
                    while(!free) {
                        checkAvailable.setString(2,newName);
                        rs = checkAvailable.executeQuery();
                        free = !rs.next();
                        rs.close();
                        rs = null;
                        if(!free) {
                            newName = String.format("%s (%d)", name, nameCount++);
                        }
                    }
                    stmt.setString(1, newName);
                    stmt.setInt(3, id);
                    stmt.executeUpdate();
                }

                writeCon.commit();
            } catch (final SQLException x) {
                try {
                    writeCon.rollback();
                } catch (final SQLException x2) {
                    LOG.error("Can't execute rollback.", x2);
                }
                throw x;
            } finally {
                if(stmt != null) {
                    try {
                        stmt.close();
                    } catch (final SQLException x) {
                        LOG.warn("Couldn't close statement", x);
                    }
                }
                if(checkAvailable != null) {
                    try {
                        checkAvailable.close();
                    } catch (final SQLException x) {
                        LOG.warn("Couldn't close statement", x);
                    }
                }

                if(null != rs) {
                    try {
                        rs.close();
                    } catch (final SQLException x) {
                        LOG.warn("Couldn't close result set", x);
                    }
                }

                if(writeCon != null) {
                    try {
                        writeCon.setAutoCommit(true);
                    } catch (final SQLException x){
                        LOG.warn("Can't reset auto commit", x);
                    }

                    if(writeCon != null) {
                        Database.back(contextId, true, writeCon);
                    }
                }
            }
        }


        private List<Integer> discoverIds(final Connection writeCon) throws SQLException {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            final List<Integer> ids = new ArrayList<Integer>();
            try {
                stmt = writeCon.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid = ? and fname = ?");
                stmt.setInt(1, contextId);
                stmt.setString(2,name);

                rs = stmt.executeQuery();
                while(rs.next()) { ids.add(rs.getInt(1)); }


            } finally {
                if(null != stmt) {
                    try {
                        stmt.close();
                    } catch (final SQLException x) {
                        LOG.warn("Couldn't close statement:",x);
                    }
                }
                if(rs != null){
                    try {
                        rs.close();
                    } catch (final SQLException x) {
                        LOG.warn("Couldn't close result set:",x);
                    }
                }
            }

            Collections.sort(ids);
            ids.remove(ids.size()-1);
            return ids;
        }

        public String getName(){
            return name;
        }

        public int getContextId(){
            return contextId;
        }



        public static List<NameCollision> getCollisions(final int contextId, final int parentFolder) throws SQLException, OXException {
            final List<NameCollision> c = new ArrayList<NameCollision>();
            Connection writeCon = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                writeCon = Database.get(contextId, true);
                if(parentFolder == -1) {
                    stmt = writeCon.prepareStatement("SELECT fname, cid  FROM oxfolder_tree WHERE module = ? GROUP BY fname,cid,parent HAVING count(*) > 1");
                    stmt.setInt(1, FolderObject.INFOSTORE);
                } else {
                    stmt = writeCon.prepareStatement("SELECT fname, cid  FROM oxfolder_tree WHERE module = ? and parent = ? GROUP BY fname,cid,parent HAVING count(*) > 1");
                    stmt.setInt(1, FolderObject.INFOSTORE);
                    stmt.setInt(2, parentFolder);
                }

                rs = stmt.executeQuery();

                while(rs.next()) {
                    final NameCollision nc = new NameCollision(rs.getString(1), rs.getInt(2));
                    c.add(nc);
                }

                rs.close();
                rs = null;

                return c;
            } finally {

                if(null != stmt) {
                    try {
                        stmt.close();
                    } catch (final SQLException x) {
                        LOG.warn("Couldn't close statement", x);
                    }
                }
                if(null != rs) {
                    try {
                        rs.close();
                    } catch (final SQLException x) {
                        LOG.warn("Couldn't close result set", x);
                    }
                }

                if(null != writeCon) {
                    Database.backAfterReading(contextId, writeCon);
                }
            }
        }
    }

}
