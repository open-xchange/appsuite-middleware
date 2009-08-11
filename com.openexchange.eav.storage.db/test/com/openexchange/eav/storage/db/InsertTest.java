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

package com.openexchange.eav.storage.db;

import static com.openexchange.eav.EAVDSL.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import com.openexchange.eav.EAVException;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.storage.db.exception.EAVStorageExceptionMessage;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.tx.TransactionException;

/**
 * {@link InsertTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InsertTest extends AbstractEAVDBStorageTest {

    private Storage storage;
    private SimContext ctx = new SimContext(1);
    
    public void setUp() throws Exception {
        super.setUp();
        this.storage = new Storage(getDBProvider());
    }
    
    public void tearDown() throws Exception {
        clearTables();
        removeAllPathIndexEntries();
        super.tearDown();
    }
    
    public void testInsertIntegerLeafWithPriorTableData() throws Exception {
        createPathIndexEntry(1, Types.CONTACT, 23);
        
        EAVNode node = N("myInteger", 12);
        
        EAVPath path = new EAVPath("contacts","12","23", "com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = "+Types.CONTACT+" AND objectId = 23 ");
        
        assertEquals(2, result.size());
        
        EAVPath savedPath = assemblePathOfNode("myInteger", result);

        assertEquals(new EAVPath("com.openexchange.test", "myInteger"), savedPath);
        
        long nodeId = getNodeId("myInteger", result);
        
        assertResult("SELECT 1 FROM eav_int1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+nodeId+" AND payload = 12");
    }

 
    public void testInsertIntegerLeafWithoutPriorTableData() throws EAVException, TransactionException, SQLException {
        exec("DROP TABLE eav_int1"); // Force creation of table
        
        EAVNode node = N("myInteger", 12);
        
        EAVPath path = new EAVPath("contacts","12","23", "com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = "+Types.CONTACT+" AND objectId = 23 ");
        
        assertEquals(2, result.size());
        
        EAVPath savedPath = assemblePathOfNode("myInteger", result);

        assertEquals(new EAVPath("com.openexchange.test", "myInteger"), savedPath);
        
        long nodeId = getNodeId("myInteger", result);
        
        assertResult("SELECT 1 FROM eav_int1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+nodeId+" AND payload = 12");
   }
    
    public void testInsertIntegerLeafUnderPartiallyExistingPath() throws EAVException, TransactionException, SQLException {
        createPathIndexEntry(1, Types.CONTACT, 23);
        exec("INSERT INTO eav_paths1 ( cid, module, objectId, nodeId, name, parent, eavType ) VALUES (1, "+Types.CONTACT+", 23, 2, 'com.openexchange.test', NULL, 'OBJECT')");
        
        EAVNode node = N("myInteger", 12);
        
        EAVPath path = new EAVPath("contacts","12","23", "com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = "+Types.CONTACT+" AND objectId = 23 ");
        
        assertEquals(2, result.size());
        
        EAVPath savedPath = assemblePathOfNode("myInteger", result);

        assertEquals(new EAVPath("com.openexchange.test", "myInteger"), savedPath);
        
        long nodeId = getNodeId("myInteger", result);
        
        assertResult("SELECT 1 FROM eav_int1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+nodeId+" AND payload = 12");

    }
    
    public void testInsertForFolder() throws EAVException, TransactionException, SQLException {
        createPathIndexEntry(1, Types.FOLDER, 23);
        
        EAVNode node = N("myInteger", 12);
        
        EAVPath path = new EAVPath("folder", "23", "com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = "+Types.FOLDER+" AND objectId = 23 ");
        
        assertEquals(2, result.size());
        
        EAVPath savedPath = assemblePathOfNode("myInteger", result);

        assertEquals(new EAVPath("com.openexchange.test", "myInteger"), savedPath);
        
        long nodeId = getNodeId("myInteger", result);
        
        assertResult("SELECT 1 FROM eav_int1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+nodeId+" AND payload = 12");
        
    }
    
    public void testInsertForContext() throws EAVException, TransactionException, SQLException {
        createPathIndexEntry(1, 0, 1);
        
        EAVNode node = N("myInteger", 12);
        
        EAVPath path = new EAVPath("com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = 0 AND objectId = 1 ");
        
        assertEquals(2, result.size());
        
        EAVPath savedPath = assemblePathOfNode("myInteger", result);

        assertEquals(new EAVPath("com.openexchange.test", "myInteger"), savedPath);
        
        long nodeId = getNodeId("myInteger", result);
        
        assertResult("SELECT 1 FROM eav_int1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+nodeId+" AND payload = 12");
    }
    
    
    // Multiples
    
    public void testInsertMultiple() throws Exception {
        createPathIndexEntry(1, Types.CONTACT, 23);
        
        EAVNode node = N("myIntegers", 12, 13, 14, 15);
        
        EAVPath path = new EAVPath("contacts","12","23", "com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = "+Types.CONTACT+" AND objectId = 23 ");
        
        assertEquals(2, result.size());
        
        EAVPath savedPath = assemblePathOfNode("myIntegers", result);

        assertEquals(new EAVPath("com.openexchange.test", "myIntegers"), savedPath);
        
        long nodeId = getNodeId("myIntegers", result);
        
        assertResult("SELECT 1 FROM eav_int1 WHERE cid = 1 AND containerType = 'MULTISET' AND nodeId = "+nodeId+" AND payload = 12");
        assertResult("SELECT 1 FROM eav_int1 WHERE cid = 1 AND containerType = 'MULTISET' AND nodeId = "+nodeId+" AND payload = 13");
        assertResult("SELECT 1 FROM eav_int1 WHERE cid = 1 AND containerType = 'MULTISET' AND nodeId = "+nodeId+" AND payload = 14");
        assertResult("SELECT 1 FROM eav_int1 WHERE cid = 1 AND containerType = 'MULTISET' AND nodeId = "+nodeId+" AND payload = 15");
    }
    
    // BLOB
    
    public void testInsertBlob() throws Exception{
        createPathIndexEntry(1, Types.CONTACT, 23);
        
        EAVNode node = N("myBlob", "Hello World".getBytes("UTF-8"));
        
        EAVPath path = new EAVPath("contacts","12","23", "com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = "+Types.CONTACT+" AND objectId = 23 ");
        
        assertEquals(2, result.size());
        
        EAVPath savedPath = assemblePathOfNode("myBlob", result);

        assertEquals(new EAVPath("com.openexchange.test", "myBlob"), savedPath);
        
        long nodeId = getNodeId("myBlob", result);
        
        result = query("SELECT payload FROM eav_blob1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+nodeId);
    
        assertEquals(1, result.size());
        
        byte[] bytes = (byte[]) result.get(0).get("payload");
        
        assertEquals("Hello World", new String(bytes, "UTF-8"));
    }
    
    // BOOL
    
    public void testInsertBool() throws Exception {
        createPathIndexEntry(1, Types.CONTACT, 23);
        
        EAVNode node = N("myBool", true);
        
        EAVPath path = new EAVPath("contacts","12","23", "com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = "+Types.CONTACT+" AND objectId = 23 ");
        
        assertEquals(2, result.size());
        
        EAVPath savedPath = assemblePathOfNode("myBool", result);

        assertEquals(new EAVPath("com.openexchange.test", "myBool"), savedPath);
        
        long nodeId = getNodeId("myBool", result);
        
        assertResult("SELECT 1 FROM eav_bool1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+nodeId+" AND payload = 1");
    }
    
    // TEXT
    
    public void testInsertText() throws Exception {
        createPathIndexEntry(1, Types.CONTACT, 23);
        
        EAVNode node = N("myString", "Hello World");
        
        EAVPath path = new EAVPath("contacts","12","23", "com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = "+Types.CONTACT+" AND objectId = 23 ");
        
        assertEquals(2, result.size());
        
        EAVPath savedPath = assemblePathOfNode("myString", result);

        assertEquals(new EAVPath("com.openexchange.test", "myString"), savedPath);
        
        long nodeId = getNodeId("myString", result);
        
        assertResult("SELECT 1 FROM eav_text1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+nodeId+" AND payload = 'Hello World'");
    }
    
    public void testSubObject() throws Exception{
        createPathIndexEntry(1, Types.CONTACT, 23);
        
        EAVNode node = N("subObject", 
            N("myString", "Hello World"),
            N("myBool", true)
        );
        
        EAVPath path = new EAVPath("contacts","12","23", "com.openexchange.test");
        
        storage.insert(ctx, path, node);
        
        List<Map<String, Object>> result = query("SELECT * FROM eav_paths1 WHERE cid = 1 AND module = "+Types.CONTACT+" AND objectId = 23 ");
        
        assertEquals(4, result.size());
        
        assertEquals(new EAVPath("com.openexchange.test", "subObject", "myString"), assemblePathOfNode("myString", result));
        assertEquals(new EAVPath("com.openexchange.test", "subObject", "myBool"), assemblePathOfNode("myBool", result));
        
        
        
        long stringId = getNodeId("myString", result);
        long boolId = getNodeId("myBool", result);
        
        assertResult("SELECT 1 FROM eav_text1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+stringId+" AND payload = 'Hello World'");
        assertResult("SELECT 1 FROM eav_bool1 WHERE cid = 1 AND containerType = 'SINGLE' AND nodeId = "+boolId+" AND payload = 1");

    }
    
    
    /*
     * Error Conditions
     */
    
    public void testFailsOnInsertingFloat() throws Exception{
        createPathIndexEntry(1, Types.CONTACT, 23);
        
        EAVNode node = N("myInteger", 12.5);
        
        EAVPath path = new EAVPath("contacts","12","23", "com.openexchange.test");
        try {
            storage.insert(ctx, path, node);
            fail("Could insert float");
        } catch (EAVException x) {
            assertEquals(EAVStorageExceptionMessage.NO_FLOATS.getDetailNumber(), x.getDetailNumber());
        }
    }
    
    public void testInsertOnExistingPathShouldFail() throws EAVException{
        EAVNode tree = N("com.openexchange.test", 
            N("exampleString", "Hello")
        );
        
        EAVPath PARENT = new EAVPath("contacts","12","23");
        
        storage.insert(ctx, PARENT, tree);
        
        try {
            storage.insert(ctx, PARENT, tree);
            fail("Could insert on existing path");
        } catch (EAVException x) {
            assertEquals("Got: "+x.getMessage(), x.getDetailNumber(), EAVStorageExceptionMessage.PATH_TAKEN.getDetailNumber());
        }
    }
}
