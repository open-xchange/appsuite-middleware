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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.eav.storage.db.exception.EAVStorageException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.sim.SimBuilder;
import junit.framework.TestCase;


/**
 * {@link BalancedTableManagerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class BalancedTableManagerTest extends TestCase{
    
    private Context ctx = new SimContext(1);
    private int module = 23;
    private int oid = 12;
    
    /*
     * If a table was already assigned to an object, the manager should choose that table.
     */
    public void testChoosePredefinedTable() throws EAVStorageException {
        SimBuilder builder = new SimBuilder();
        builder.expectCall("getPredefinedTable", ctx, module, oid).andReturn("table1");
    
        BalancedTableManager manager = new BalancedTableManager(builder.getSim(TableManagerStrategy.class));
        
        assertEquals("table1", manager.getTable(ctx, module, oid));
        
        builder.assertAllWereCalled();
    }
    
    /*
     * If no table was assigned to an object, the manager should choose the emptiest table, provided it is not filled above the current threshold.
     */
    public void testChooseMostEmptyTable() throws EAVStorageException {
        
        List<TableMetadata> tables = declareTables("table1", 1000, "table2", 1200, "tableWithLowestObjectCount", 3, "table4", 2900);
        
        
        SimBuilder builder = new SimBuilder();
        builder.expectCall("getPredefinedTable", ctx, module, oid).andReturn(null)
               .expectCall("getTableMetadataForAllTables", ctx).andReturn(tables)
               .expectCall("register", ctx, module, oid, "tableWithLowestObjectCout").andReturn("tableWithLowestObjectCount");
        
        
        BalancedTableManager manager = new BalancedTableManager(builder.getSim(TableManagerStrategy.class));
        
        assertEquals("tableWithLowestObjectCount", manager.getTable(ctx, module, oid));
        
        builder.assertAllWereCalled();
        
    }

    /*
     * The threshold for needing to create new tables is calculated as follows:
     * Take m, the maximum number of objects to keep in a table and t, the number of tables, then the
     * threshold is defined as (1 - 1 / (2 to the power of t)) * m
     * meaning for 1 table it is 1/2 * the threshold, for 2 it is 3/4 * the threshold, for 3 it is 7/8 * the threshold and so on and so forth.  
     */
    public void testCalculateThreshold() {
        int ONE_MILLION = 1000000;
        assertEquals(ONE_MILLION / 2, BalancedTableManager.calculateThreshold(ONE_MILLION, 1));
        assertEquals( 3 * ONE_MILLION / 4, BalancedTableManager.calculateThreshold(ONE_MILLION, 2));
        assertEquals( 7 * ONE_MILLION / 8, BalancedTableManager.calculateThreshold(ONE_MILLION, 3));
        assertEquals( 15 * ONE_MILLION / 16, BalancedTableManager.calculateThreshold(ONE_MILLION, 4));
    }
    
    /*
     * If no table was assigned to an object and all tables are at or above the growth threshold, the manager should create a new table and choose it.
     */
    public void testCreateNewTable() throws EAVStorageException {
        List<TableMetadata> tables = declareTables("table1", 750, "table2", 750);
        
        
        SimBuilder builder = new SimBuilder();
        builder.expectCall("getPredefinedTable", ctx, module, oid).andReturn(null)
               .expectCall("getTableMetadataForAllTables", ctx).andReturn(tables)
               .expectCall("createNewTable", ctx).andReturn("newTable")
               .expectCall("register", ctx, module, oid, "newTable").andReturn("newTable");
        
        
        BalancedTableManager manager = new BalancedTableManager(builder.getSim(TableManagerStrategy.class));
        manager.setCapacity(1000);
        
        assertEquals("newTable", manager.getTable(ctx, module, oid));
        
        builder.assertAllWereCalled();

    }
    
    /*
     * If no table was assigned to an object and no table exists, create the first one.
     */
    public void testCreateInitialTable() throws EAVStorageException {
        SimBuilder builder = new SimBuilder();
        builder.expectCall("getPredefinedTable", ctx, module, oid).andReturn(null)
               .expectCall("getTableMetadataForAllTables", ctx).andReturn(new ArrayList<TableMetadata>())
               .expectCall("createNewTable", ctx).andReturn("newTable")
               .expectCall("register", ctx, module, oid, "newTable").andReturn("newTable");
        
        
        BalancedTableManager manager = new BalancedTableManager(builder.getSim(TableManagerStrategy.class));
        manager.setCapacity(1000);
        
        assertEquals("newTable", manager.getTable(ctx, module, oid));
        
        builder.assertAllWereCalled();

    }
    
    /*
     * If after determining a valid table it is noticed during registration that a predefined table was defined concurrently, use it instead
     */
    
    public void testUseConcurrentlyPredefinedTable() throws EAVStorageException {
        List<TableMetadata> tables = declareTables("table1", 1000, "table2", 1200, "tableWithLowestObjectCount", 3, "table4", 2900);
        
        
        SimBuilder builder = new SimBuilder();
        builder.expectCall("getPredefinedTable", ctx, module, oid).andReturn(null)
               .expectCall("getTableMetadataForAllTables", ctx).andReturn(tables)
               .expectCall("register", ctx, module, oid, "tableWithLowestObjectCout").andReturn("otherTable");
        
        
        BalancedTableManager manager = new BalancedTableManager(builder.getSim(TableManagerStrategy.class));
        
        assertEquals("otherTable", manager.getTable(ctx, module, oid));
        
        builder.assertAllWereCalled();

    }
    
    private List<TableMetadata> declareTables(Object...tableDef) {
        List<TableMetadata> tables = new ArrayList<TableMetadata>(tableDef.length/2);
        TableMetadata current = null;
        for(Object attr : tableDef) {
            if(current == null) {
                current = new TableMetadata();
                current.setName((String) attr);
            } else {
                current.setObjectCount((Integer) attr);
                tables.add(current);
                current = null;
            }
        }
        return tables;
    }

    
}
