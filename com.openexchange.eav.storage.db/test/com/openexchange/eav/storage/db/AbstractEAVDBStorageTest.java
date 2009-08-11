package com.openexchange.eav.storage.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.eav.AbstractNode;
import com.openexchange.eav.EAVDSL;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.storage.db.sql.SQLType;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.sql.SQLTestCase;

import static com.openexchange.eav.EAVDSL.*;

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

/**
 * {@link AbstractEAVDBStorageTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class AbstractEAVDBStorageTest extends SQLTestCase {


    protected void clearTables() throws TransactionException, SQLException {
        for(SQLType type : SQLType.values()) {
            
            List<String> tables = getTables(type);
            
            for(String tableName : tables) {
                if(tableName.equals(type.getTablePrefix()+"1")) {
                    exec("DELETE FROM "+tableName);
                } else {
                    dropTable(tableName);
                }
            }
            
        }
    }
    
    protected List<String> getTables(SQLType type) throws TransactionException, SQLException {
        List<String> tables = new ArrayList<String>();
        List<Map<String,Object>> result = query("SHOW TABLES LIKE '"+type.getTablePrefix()+"%'");
        
        for(Map<String, Object> row : result) {
            tables.add((String) row.values().iterator().next());
        }
        
        return tables;
    }
    
    protected void createPayloadTables(SQLType type, int upTo) throws TransactionException, SQLException {
        List<String> tables = getTables(type);
        Set<Integer> existing = new HashSet<Integer>();
        for(String table : tables) {
            existing.add(getIndex(table, type));
        }
        
        String tablePrefix = type.getTablePrefix();
        String origName = tablePrefix+"1";
        
        for(int i = 1; i < upTo; i++) {
            if(!existing.contains(i)) {
                copyTableStructure(origName, tablePrefix+i);
            }
        }
        
    }
    
    protected int getIndex(String tableName, SQLType type) {
        return Integer.valueOf(tableName.substring(type.getTablePrefix().length()));
    }
    
    
    protected void createPathIndexEntry(int cid, int module, int objectId, String intTable, String textTable, String varcharTable, String blobTable, String boolTable, String referenceTable, String pathTable) throws TransactionException, SQLException {
        insert("eav_pathIndex", "cid", cid, "module", module, "objectId", objectId, "intTable", intTable, "textTable", textTable, "varcharTable", varcharTable, "blobTable", blobTable, "boolTable", boolTable, "referenceTable", referenceTable, "pathTable", pathTable);
    }
    
    protected void createPathIndexEntry(int cid, int module, int objectId) throws TransactionException, SQLException {
        createPathIndexEntry(cid, module, objectId, "eav_int1", "eav_text1", "eav_varchar1", "eav_blob1", "eav_bool1", "eav_reference1", "eav_paths1");
    }
    
    protected void removeAllPathIndexEntries() throws TransactionException, SQLException {
        exec("DELETE FROM eav_pathIndex");
    }
    
    public static <T extends AbstractNode<T>>  void assertEquals(AbstractNode<T> expected, AbstractNode<T> actual) {
        EAVDSL.assertEquals(expected, actual);
    }

    public static <T extends AbstractNode<T>> void assertEquals(String message, AbstractNode<T> expected, AbstractNode<T> actual) {
        EAVDSL.assertEquals(message, expected, actual);
    }
    
    protected EAVPath assemblePathOfNode(String node, List<Map<String, Object>> result) {
        Map<Long, String> nodeId2Name = new HashMap<Long, String>();
        Map<String, Long> name2NodeId = new HashMap<String, Long>();
        Map<Long, Long> child2parent= new HashMap<Long, Long>();
        
        for(Map<String, Object> row : result) {
            Long nodeId = (Long) row.get("nodeId");
            Long parent = (Long) row.get("parent");
            String name = (String) row.get("name");
            
            nodeId2Name.put(nodeId, name);
            name2NodeId.put(name, nodeId);
            child2parent.put(nodeId, parent);
        }
        
        List<String> inversePath = new ArrayList<String>();
        
        Long currentChild = name2NodeId.get(node);
        while(currentChild != null) {
            inversePath.add(nodeId2Name.get(currentChild));
            currentChild = child2parent.get(currentChild); 
        }
    
        Collections.reverse(inversePath);
        return new EAVPath(inversePath);
    }
    
    protected long getNodeId(String name, List<Map<String, Object>> result) {
        for(Map<String, Object> row : result) {
            Long nodeId = (Long) row.get("nodeId");
            String currentName = (String) row.get("name");
            if(name.equals(currentName)) {
                return nodeId;
            }
        }
        return -1;
    }

       
}
