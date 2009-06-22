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

package com.openexchange.datatypes.genericonf.storage.impl;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.datatypes.genericonf.storage.GenericConfigStorageErrorMessage;
import com.openexchange.datatypes.genericonf.storage.GenericConfigStorageException;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;

/**
 * {@link MySQLGenericConfigurationStorage}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MySQLGenericConfigurationStorage implements GenericConfigurationStorageService {

    private static final Log LOG = LogFactory.getLog(MySQLGenericConfigurationStorage.class);
    
    private DBProvider provider;

    public void setDBProvider(DBProvider provider) {
        this.provider = provider;
    }

    public int save(Context ctx, Map<String, Object> content) throws GenericConfigStorageException {
        return save(null, ctx, content);
    }
    
    public int save(Connection con, final Context ctx, final Map<String, Object> content) throws GenericConfigStorageException {
        return ((Integer) write(con, ctx, new TX() {

            public Object perform() throws SQLException {
                Connection con = getConnection();

                InsertIterator insertIterator = new InsertIterator();
                insertIterator.prepareStatements(this);
                
                int id = IDGenerator.getId(ctx, Types.GENERIC_CONFIGURATION, con);
                int cid = ctx.getContextId();
                insertIterator.setIds(cid, id);

                Tools.iterate(content, insertIterator);
                insertIterator.close();
                insertIterator.throwException();
                return I(id);
            }

        })).intValue();
    }

    private Object write(Connection con, Context ctx, TX tx) throws GenericConfigStorageException {
        Connection writeCon = con;
        boolean connectionHandling = con == null;
        try {
            if(connectionHandling) {
                writeCon = provider.getWriteConnection(ctx);
                writeCon.setAutoCommit(false);
            }
            tx.setConnection(writeCon);
            Object retval = tx.perform();
            if(connectionHandling) {
                writeCon.commit();
            }
            return retval;
        } catch (SQLException x) {
            try {
                if(connectionHandling) {
                    writeCon.rollback();
                }
            } catch (SQLException e) {
            }
            LOG.error(x.getMessage(), x);
            GenericConfigStorageErrorMessage.SQLException.throwException(x, x.getMessage());
            return null;
        } catch (TransactionException e) {
            throw new GenericConfigStorageException(e);
        } finally {
            tx.close();
            if(connectionHandling) {
                try {
                    writeCon.setAutoCommit(true);
                } catch (SQLException e) {
                }
                provider.releaseWriteConnection(ctx, writeCon);
            }
        }
    }
    
    public void fill(Context ctx, int id, Map<String, Object> content) throws GenericConfigStorageException {
        fill(null, ctx, id, content);
    }

    public void fill(Connection con, Context ctx, int id, Map<String, Object> content) throws GenericConfigStorageException {
        Connection readCon = con;
        boolean connectionHandling = con == null;
        try {
            if(connectionHandling) {
                readCon = provider.getReadConnection(ctx);
            }
            loadValues(readCon, ctx, id, content, "genconf_attributes_strings");
            loadValues(readCon, ctx, id, content, "genconf_attributes_bools");
        } catch (TransactionException e) {
            throw new GenericConfigStorageException(e);
        } finally {
            if(connectionHandling) {
                provider.releaseReadConnection(ctx, readCon);
            }
        }
    }

    private void loadValues(Connection readCon, Context ctx, int id, Map<String, Object> content, String tablename) throws GenericConfigStorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            stmt = readCon.prepareStatement("SELECT name, value FROM "+tablename+" WHERE cid = ? AND id = ?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                Object value = rs.getObject("value");
                
                content.put(name, value);
            }
        } catch (SQLException x) {
            GenericConfigStorageErrorMessage.SQLException.throwException(x, stmt.toString());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException x) {
                }

            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException x) {
                }
            }
        }
    }
    
   
    public void update(final Context ctx, final int id, final Map<String, Object> content) throws GenericConfigStorageException {
        update(null, ctx, id, content);
    }
    
    public void update(Connection con, final Context ctx, final int id, final Map<String, Object> content) throws GenericConfigStorageException {
        final Map<String, Object> original = new HashMap<String, Object>();
        fill(con, ctx, id, original);

        write(con, ctx, new TX() {

            public Object perform() throws SQLException {
                UpdateIterator updateIterator = new UpdateIterator();
                try {
                    updateIterator.prepareStatements(this);
                    updateIterator.setIds(ctx.getContextId(), id);
                    updateIterator.setOriginal(original);
                    
                    Tools.iterate(content, updateIterator);
                } finally {
                    updateIterator.close();
                }
                updateIterator.throwException();
                return null;
            }

        });

    }

    public void delete(final Context ctx, final int id) throws GenericConfigStorageException {
        delete(null, ctx, id);
    }
    
    public void delete(Connection con, final Context ctx, final int id) throws GenericConfigStorageException {

        write(con, ctx, new TX() {

            @Override
            public Object perform() throws SQLException {
                clearTable("genconf_attributes_strings");
                clearTable("genconf_attributes_bools");
                return null;
            }
            
            private void clearTable(String tablename) throws SQLException {
                PreparedStatement delete = null;
                delete = prepare("DELETE FROM "+tablename+" WHERE cid = ? AND id = ?");
                delete.setInt(1, ctx.getContextId());
                delete.setInt(2, id);
                delete.executeUpdate();

            }

        });

    }

    public void delete(Connection con, final Context ctx) throws GenericConfigStorageException {

        write(con, ctx, new TX() {

            @Override
            public Object perform() throws SQLException {
                clearTable("genconf_attributes_strings");
                clearTable("genconf_attributes_bools");
                return null;
            }
            
            private void clearTable(String tablename) throws SQLException {
                PreparedStatement delete = null;
                delete = prepare("DELETE FROM "+tablename+" WHERE cid = ?");
                delete.setInt(1, ctx.getContextId());
                delete.executeUpdate();
            }

        });

    }

    public List<Integer> search(Context ctx, Map<String, Object> query) throws GenericConfigStorageException {
        return search(null, ctx, query);
    }
    
    public List<Integer> search(Connection con, Context ctx, Map<String, Object> query) throws GenericConfigStorageException {
        boolean handleOwnConnections = con == null;
            
        LinkedList<Integer> list = new LinkedList<Integer>();
        StringBuilder builder = new StringBuilder("SELECT DISTINCT p.id FROM ");
        SearchIterator whereIterator = new SearchIterator();
        Tools.iterate(query, whereIterator);
        
        
        builder.append(whereIterator.getFrom());
        builder.append(" WHERE ");
        builder.append("(");
        builder.append(whereIterator.getWhere());
        builder.append(") AND p.cid = ?");
        whereIterator.addReplacement(I(ctx.getContextId()));
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if(handleOwnConnections) {
                con = provider.getReadConnection(ctx);
            }
            stmt = con.prepareStatement(builder.toString());
            whereIterator.setReplacements(stmt);
            rs = stmt.executeQuery();
            
            while(rs.next()) {
                list.add(I(rs.getInt(1)));
            }
            
        } catch (TransactionException e) {
            throw new GenericConfigStorageException(e);
        } catch (SQLException e) {
            GenericConfigStorageErrorMessage.SQLException.throwException(e, stmt.toString());
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException x) {
                    // Ignore
                }
            }
            if(rs != null) {
                try {
                    rs.close(); 
                } catch (SQLException x) {
                    // Ignore
                }
            }
            if(handleOwnConnections) {
                provider.releaseReadConnection(ctx, con);
            }
        }
        
        return list;
    }

}
