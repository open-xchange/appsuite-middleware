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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.eav.storage.db.exception.EAVStorageException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.sql.SQLTestCase;


/**
 * {@link PathIndexStrategyTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PathIndexStrategyTest extends SQLTestCase {
    
    private String indexTable = "test_pathIndex";
    private String createStatement;
    private PathIndexStrategy strategy;

    private Context ctx = new SimContext(1);
    private int module = 23;
    private int objectId = 12;
    
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        strategy = new PathIndexStrategy(indexTable, getDBProvider());
        strategy.setColumnName("pathTable");
        strategy.setCreateTable("CREATE TABLE IF NOT EXISTS %%tablename%% (somefield TEXT)");
        strategy.setTablePrefix("test_payload_table");
        
        discoverCreateStatement("eav_pathIndex");
        changeTableName("eav_pathIndex", "test_pathIndex");
        dropIndexTable();
        createIndexTable();

    }

    /*
     * Registering means a table is assigned to a certain object, if it did not have a table already assigned
     */
    public void testRegister() throws EAVStorageException, TransactionException, SQLException {
        assertNoResult("SELECT 1 FROM test_pathIndex WHERE cid = 1 AND module = 23 AND objectId = 12");
        
        String table = strategy.register(ctx, module, objectId, "test_path12");
        
        assertEquals("test_path12", table);
        
        assertResult("SELECT 1 FROM test_pathIndex WHERE cid = 1 AND module = 23 AND objectId = 12 AND pathTable = 'test_path12'");
    }
    
    public void testRegisterShouldUpdateFromNull() throws TransactionException, SQLException, EAVStorageException {
        exec("INSERT INTO test_pathIndex (cid, module, objectId, pathTable) VALUES (1,23,12,'test_path12')");
    
        strategy.setColumnName("textTable");
        String table = strategy.register(ctx, module, objectId, "test_text12");
        
        assertEquals("test_text12", table);
        
        assertResult("SELECT 1 FROM test_pathIndex WHERE cid = 1 AND module = 23 AND objectId = 12 AND textTable = 'test_text12'");
    
    }
    
    /*
     * If a table was assigned before register is called, substitute the given table with the registered one
     */
    public void testRegisterWithExistingAssignment() throws TransactionException, SQLException, EAVStorageException {
        exec("INSERT INTO test_pathIndex (cid, module, objectId, pathTable, textTable) VALUES (1,23,12,'test_path12', 'test_text23')");
        assertResult("SELECT 1 FROM test_pathIndex WHERE cid = 1 AND module = 23 AND objectId = 12 AND textTable = 'test_text23'");

        strategy.setColumnName("textTable");
        String table = strategy.register(ctx, module, objectId, "test_text12");
        
        assertEquals("test_text23", table);
        
        assertResult("SELECT 1 FROM test_pathIndex WHERE cid = 1 AND module = 23 AND objectId = 12 AND textTable = 'test_text23'");
    }
    
    
    public void testCreateTableIfTableDoesNotExist() throws TransactionException, SQLException, EAVStorageException {
        exec("DROP TABLE IF EXISTS test_payload_table1");
        
        strategy.createNewTable(ctx);
        
        assertResult("SHOW CREATE TABLE test_payload_table1");
    
    }
    
    public void testGetTableMetadataForAllTables() throws TransactionException, SQLException, EAVStorageException {
        exec("CREATE TABLE test_payload_table1 (someField TEXT)");
        exec("CREATE TABLE test_payload_table2 (someField TEXT)");
        exec("CREATE TABLE test_payload_table3 (someField TEXT)");
        
        exec("INSERT INTO test_pathIndex (cid, module, objectId, pathTable) VALUES (1,23,12,'test_payload_table1')");
        exec("INSERT INTO test_pathIndex (cid, module, objectId, pathTable) VALUES (1,23,13,'test_payload_table1')");
        exec("INSERT INTO test_pathIndex (cid, module, objectId, pathTable) VALUES (1,23,14,'test_payload_table1')");
        exec("INSERT INTO test_pathIndex (cid, module, objectId, pathTable) VALUES (1,23,15,'test_payload_table1')");
        
        exec("INSERT INTO test_pathIndex (cid, module, objectId, pathTable) VALUES (1,23,21,'test_payload_table2')");
        exec("INSERT INTO test_pathIndex (cid, module, objectId, pathTable) VALUES (1,23,22,'test_payload_table2')");
        
        List<TableMetadata> tables = strategy.getTableMetadataForAllTables(ctx);
        
        assertEquals(3, tables.size());
        
        Map<String, Integer> expected = new HashMap<String, Integer>();
        expected.put("test_payload_table1", 4);
        expected.put("test_payload_table2", 2);
        expected.put("test_payload_table3", 0);
        
        Set<String> tableNames= new HashSet<String>(expected.keySet());
        
        for(TableMetadata metadata : tables) {
            assertTrue(metadata.getName()+" was unexpected", tableNames.remove(metadata.getName()));
            assertEquals((int)expected.get(metadata.getName()), (int)metadata.getObjectCount());
        }
        
        
    }
    
    @Override
    protected void tearDown() throws Exception {
        dropIndexTable();
        exec("DROP TABLE IF EXISTS test_payload_table1");
        exec("DROP TABLE IF EXISTS test_payload_table2");
        exec("DROP TABLE IF EXISTS test_payload_table3");
        exec("DROP TABLE IF EXISTS test_payload_table4");
        exec("DROP TABLE IF EXISTS test_payload_table5");
        exec("DROP TABLE IF EXISTS test_payload_table6");
        exec("DROP TABLE IF EXISTS test_payload_table7");
        super.tearDown();
    }
    
    // Setup Helpers
    
    private void createIndexTable() throws TransactionException, SQLException {
        exec(createStatement);
    }

    private void dropIndexTable() throws TransactionException, SQLException {
        exec("DROP TABLE IF EXISTS "+indexTable);
    }

    private void changeTableName(String origName, String newName) {
        createStatement = createStatement.replaceAll(origName, newName);
    }

    private void discoverCreateStatement(String tableName) throws TransactionException, SQLException {
        Map<String, Object> createTableRow = query("SHOW CREATE TABLE "+tableName).get(0);
        createStatement = (String) createTableRow.get("Create Table");
    }
    
    
    
}
