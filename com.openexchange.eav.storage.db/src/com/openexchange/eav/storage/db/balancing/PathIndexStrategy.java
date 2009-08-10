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

package com.openexchange.eav.storage.db.balancing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.eav.storage.db.exception.EAVStorageException;
import com.openexchange.eav.storage.db.exception.EAVStorageExceptionMessage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.ISNULL;
import com.openexchange.sql.grammar.ModifyCommand;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.sql.grammar.UPDATE;
import com.openexchange.tools.sql.DBUtils;

import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;


/**
 * {@link PathIndexStrategy}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PathIndexStrategy implements TableManagerStrategy {

    private static final Log LOG = LogFactory.getLog(PathIndexStrategy.class);
    
    private String indexTable;
    private String columnName;
    private DBProvider provider;

    private String createTable;


    private String tablePrefix;

    public PathIndexStrategy(String indexTable, DBProvider provider) {
        super();
        this.indexTable = indexTable;
        this.provider = provider;
    }

    /* (non-Javadoc)
     * @see com.openexchange.eav.storage.db.balancing.TableManagerStrategy#createNewTable()
     */
    public String createNewTable(Context ctx) throws EAVStorageException {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        try {
            con = provider.getWriteConnection(ctx);
            stmt = con.prepareStatement("SHOW TABLES like '"+tablePrefix+"%'");
            int count = 1;
            rs = stmt.executeQuery();
            while(rs.next()) {
                count++;
            }
            
            String newTableName = tablePrefix+count;
            
            stmt.close();
            
            stmt = con.prepareStatement(createTable.replaceAll("%%tablename%%", newTableName));
            stmt.execute();
            
            stmt.close();
            rs.close();
            
            stmt = con.prepareStatement("SHOW TABLES LIKE '"+newTableName+"'");
            rs = stmt.executeQuery();
            
            if(!rs.next()) {
                throw EAVStorageExceptionMessage.CouldNotCreateTable.create(newTableName);
            }
            
        } catch (TransactionException e) {
            throw new EAVStorageException(e);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw EAVStorageExceptionMessage.SQLException.create();
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(ctx, con);
        }
   
        return null;
    }

    public String getPredefinedTable(Context ctx, int module, int oid) throws EAVStorageException {
        SELECT select = new SELECT(columnName).FROM(indexTable).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("module", PLACEHOLDER)).AND(new EQUALS("objectId", PLACEHOLDER))); 
        
        Connection con = null;
        ResultSet rs = null;
        StatementBuilder builder = new StatementBuilder();
        
        try {
            con = provider.getReadConnection(ctx);
            rs = builder.executeQuery(con, select, Arrays.asList(ctx.getContextId(), module, oid));
            if(rs.next()) {
                return rs.getString(1);
            }
        } catch (TransactionException e) {
            throw new EAVStorageException(e);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw EAVStorageExceptionMessage.SQLException.create();
        } finally {
            provider.releaseReadConnection(ctx, con);
            try {
                builder.closePreparedStatement(null, rs);
            } catch (SQLException e) {
                // Ignore
            }
        }
   
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.eav.storage.db.balancing.TableManagerStrategy#getTableMetadataForAllTables()
     */
    public List<TableMetadata> getTableMetadataForAllTables(Context ctx) throws EAVStorageException {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        
        List<TableMetadata> tables = new ArrayList<TableMetadata>();
        
        try {
            con = provider.getWriteConnection(ctx);
            
            Map<String, Long> objectCounts = new HashMap<String, Long>();
            stmt = con.prepareStatement("SELECT "+columnName+" AS tableName, COUNT(*) as objectCount FROM "+indexTable+" GROUP BY (tableName)");
            rs = stmt.executeQuery();
            while(rs.next()) {
                objectCounts.put(rs.getString(1), (Long) rs.getObject(2));
            }
            
            
            
            stmt = con.prepareStatement("SHOW TABLES like '"+tablePrefix+"%'");
            rs = stmt.executeQuery();
            while(rs.next()) {
                TableMetadata metadata = new TableMetadata();
                metadata.setName(rs.getString(1));
                if(objectCounts.containsKey(metadata.getName())) {
                    metadata.setObjectCount(objectCounts.get(metadata.getName()));
                }
                tables.add(metadata);
            }
            
            stmt.close();
            rs.close();
            
            
            
            
        } catch (TransactionException e) {
            throw new EAVStorageException(e);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw EAVStorageExceptionMessage.SQLException.create();
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(ctx, con);
        }
   
        return tables;
    }


    public String register(Context ctx, int module, int oid, String table) throws EAVStorageException {
        return registerInternal(1, ctx, module, oid, table);
    }
    
    public String registerInternal(int countdown, Context ctx, int module, int oid, String table) throws EAVStorageException {
        UPDATE update = new UPDATE(indexTable).SET(columnName, PLACEHOLDER).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new EQUALS("module", PLACEHOLDER)).AND(new EQUALS("objectId", PLACEHOLDER)).AND(new ISNULL(columnName))); 
        
        boolean updated = execute(ctx, update, table, ctx.getContextId(), module, oid ) > 0;
        
        if(updated) {
            return table;
        }
        
        // Exists already, or was created in the mean time?
        
        String predefinedTable = getPredefinedTable(ctx, module, oid);
        if(predefinedTable != null) {
            return predefinedTable;
        }
        
        
        INSERT insert = new INSERT().INTO(indexTable).SET("cid", PLACEHOLDER).SET("module", PLACEHOLDER).SET("objectId", PLACEHOLDER).SET(columnName, PLACEHOLDER); 
        try {
            execute(ctx, insert, ctx.getContextId(), module, oid, table );
        } catch (EAVStorageException x) {
            if(countdown == 0) {
                throw x;
            }
            return registerInternal(countdown-1, ctx, module, oid, table);
        }
        
        return table;
    }

    private int execute(Context ctx, ModifyCommand modification, Object...args) throws EAVStorageException {
        Connection con = null;
        try {
            con = provider.getWriteConnection(ctx);
            return new StatementBuilder().executeStatement(con, modification, Arrays.asList(args));
        
        } catch (TransactionException x) {
            throw new EAVStorageException(x);
        } catch (SQLException x) {
            LOG.error(x.getMessage(), x);
            throw EAVStorageExceptionMessage.SQLException.create();
        } finally {
            if(con != null){
                provider.releaseWriteConnection(ctx, con);
            }
        }
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setCreateTable(String string) {
        this.createTable = string;
    }

    public void setTablePrefix(String string) {
        this.tablePrefix = string;
    }

}
