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

import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.eav.EAVContainerType;
import com.openexchange.eav.EAVException;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVSetTransformation;
import com.openexchange.eav.EAVStorage;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.TreeTools;
import com.openexchange.eav.storage.db.exception.EAVStorageException;
import com.openexchange.eav.storage.db.exception.EAVStorageExceptionMessage;
import com.openexchange.eav.storage.db.sql.Paths;
import com.openexchange.eav.storage.db.sql.SQLType;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.Command;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.ISNULL;
import com.openexchange.sql.grammar.Predicate;
import com.openexchange.sql.grammar.SELECT;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Storage implements EAVStorage {
    private static final Log LOG = LogFactory.getLog(Storage.class);
    private DBProvider provider;

    public Storage(DBProvider provider) {
        this.provider = provider;
    }

    public void delete(Context ctx, EAVPath path) throws EAVException {
        // TODO Auto-generated method stub
    }

    public EAVNode get(Context ctx, EAVPath path) throws EAVException {
        return get(ctx, path, true);
    }

    public EAVNode get(Context ctx, EAVPath path, boolean allBinaries) throws EAVException {
        Connection con = null;
        try {
            con = provider.getReadConnection(ctx);
        } catch (TransactionException e) {
            throw EAVStorageExceptionMessage.SQLException.create(e);
        }
        SQLStorage storage = new SQLStorage(ctx, getModule(path), getObjectId(path));
        storage.init(con, allBinaries);
        return storage.getEAVNode(chopPath(path));
    }

    public EAVNode get(Context ctx, EAVPath path, Set<EAVPath> loadBinaries) throws EAVException {
        // TODO Auto-generated method stub
        return null;
    }

    public EAVTypeMetadataNode getTypes(Context ctx, EAVPath parent, EAVNode node) throws EAVException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.eav.EAVStorage#insert(com.openexchange.groupware.contexts.Context, com.openexchange.eav.EAVPath, com.openexchange.eav.EAVNode)
     */
    public void insert(Context ctx, EAVPath parentPath, EAVNode tree) throws EAVException {
        EAVNode absoluteTree = absolute(parentPath, tree);
        
        ObjectAddress address = new ObjectAddress(ctx, absoluteTree);
        
        Map<SQLType, String> tables = getTables(address, absoluteTree);
        
        EAVNode root = ObjectAddress.getEAVRoot(absoluteTree);
        
        
        dumpNode(tables, address, root, -1);
    }

    // Could be done smarter
    private int resolvePath(String table, ObjectAddress address, EAVPath path, int parentId) throws EAVStorageException {
        if(path.isEmpty()) {
            return parentId;
        }
        Context ctx = address.ctx;
        Predicate predicate = getPredicate(address).AND(new EQUALS("name", PLACEHOLDER));
        if(parentId == -1) {
            predicate = predicate.AND(new ISNULL("parent"));
        } else {
            predicate = predicate.AND(new EQUALS("parent", parentId));
        }
        
        SELECT select = new SELECT("nodeId").FROM(table).WHERE(predicate);
        
        Connection con = null;
        ResultSet rs = null;
        StatementBuilder builder = new StatementBuilder();
        boolean found = false;
        int id = -1;
        try {
            con = provider.getReadConnection(ctx);
            
            rs = builder.executeQuery(con, select, Arrays.asList(path.first()));
            
            if(rs.next()) {
                found = true;
                id = rs.getInt(1);
            }
        } catch (TransactionException e) {
            throw new EAVStorageException(e);
        } catch (SQLException e) {
            LOG.error(e.getMessage(),e);
            throw EAVStorageExceptionMessage.SQLException.create();
        } finally {
            provider.releaseReadConnection(ctx, con);
            try {
                builder.closePreparedStatement(null, rs);
            } catch (SQLException e) {
            }
        }
        
        if(found) {
            return resolvePath(table, address, path.shiftLeft(), id);
        }
        
        return 0;
    }

    private Predicate getPredicate(ObjectAddress address) {
        return new EQUALS("cid", address.ctx.getContextId()).AND(new EQUALS("module", address.module)).AND(new EQUALS("objectId", address.id));
    }

    private void dumpNode(Map<SQLType, String> tables, ObjectAddress address, EAVNode node, int parentId) throws EAVStorageException {
        if(!node.isLeaf()) {
            int nodeId = resolvePath(tables.get(SQLType.PATH), address, node.getPath(), parentId);
            if(nodeId == 0) {
                dumpNodeToDB(tables, address, node, parentId);
            } else {
                for(EAVNode child : node.getChildren()) {
                    dumpNode(tables, address, child, nodeId);
                }
            }
        } else {
            dumpNodeToDB(tables, address, node, parentId);
        }
    }
    
    private void dumpNodeToDB(Map<SQLType, String> tables, ObjectAddress address, EAVNode node, int parentId) throws EAVStorageException {
        
        int id = writePathElement(tables.get(SQLType.PATH), address, node.getName(), node.getType(), parentId);
        
        if(node.isLeaf()) {
            SQLType sqlType = SQLType.chooseType(node.getType(), node.getPayload());
            writePayload(tables.get(sqlType), address.ctx, id, sqlType, node.getType(), node.getContainerType(), node.getPayload());
        } else {
            for(EAVNode child : node.getChildren()) {
                dumpNodeToDB(tables, address, child, id);
            }
        }
        
    }

    private void writePayload(String payloadTable, Context ctx, int id, SQLType sqlType, EAVType type, EAVContainerType cType, Object payload) throws EAVStorageException {
        INSERT insert = new INSERT().INTO(payloadTable).SET("cid", PLACEHOLDER).SET("containerType", PLACEHOLDER).SET("nodeId", PLACEHOLDER).SET("payload", PLACEHOLDER);
        List<Object> values = new ArrayList<Object>(Arrays.asList(ctx.getContextId(), cType.name(), id));
        values.add(convert(sqlType, type, payload));
    
        write(ctx, insert, values);
    }

    private Object convert(SQLType sqlType, EAVType type, Object payload) {
        return payload;
    }

    private int writePathElement(String pathTable, ObjectAddress address, String name, EAVType type, int parentId) throws EAVStorageException {
        int nodeId = getId(address.ctx);
        INSERT insert = new INSERT().INTO(pathTable).SET(Paths.cid, PLACEHOLDER).SET(Paths.nodeId, PLACEHOLDER).SET(Paths.module, PLACEHOLDER).SET(Paths.objectId, PLACEHOLDER).SET(Paths.name, PLACEHOLDER).SET(Paths.eavType, PLACEHOLDER);
        List<Object> values = new ArrayList();
        values.addAll(Arrays.asList(address.ctx.getContextId(), nodeId, address.module, address.id, name, type.name()));
        
        if(parentId != -1) {
            insert = insert.SET(Paths.parent, PLACEHOLDER);
            values.add(parentId);
        }
        
        write(address.ctx, insert, values);
        
        return nodeId;
    }

    private int getId(Context ctx) throws EAVStorageException {
        Connection con = null;
        try {
            con = provider.getWriteConnection(ctx);
            con.setAutoCommit(false);
            return IDGenerator.getId(ctx, Types.EAV_NODE, con);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw EAVStorageExceptionMessage.SQLException.create();
        } catch (TransactionException e) {
            throw new EAVStorageException(e);
        } finally {
            if(con != null) {
                try {
                    con.setAutoCommit(true);
                    con.rollback();
                } catch (SQLException e) {
                }
                provider.releaseWriteConnection(ctx, con);
            }
            
        }
    }

    private void write(Context ctx, Command element, List<Object> values) throws EAVStorageException {
        Connection con = null;
        try {
            con = provider.getWriteConnection(ctx);
            StatementBuilder statementBuilder = new StatementBuilder();
            
            String command = statementBuilder.buildCommand(element); // For debugging
            
            statementBuilder.executeStatement(con, element, values);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw EAVStorageExceptionMessage.SQLException.create();
        } catch (TransactionException e) {
            throw new EAVStorageException(e);
        } finally {
            if(con != null) {
                provider.releaseWriteConnection(ctx, con);
            }
        }
    }

    private Map<SQLType, String> getTables(ObjectAddress address, EAVNode absoluteTree) throws EAVStorageException {
        // This is optimisable.
        Map<SQLType, String> tables = new EnumMap<SQLType, String>(SQLType.class);
        for(SQLType type : SQLType.values()) {
            String table = type.getTableManager(provider).getTable(address.ctx, address.module, address.id);
            tables.put(type, table);
        }
        return tables;
    }

    private EAVNode absolute(EAVPath parentPath, EAVNode tree) {
        
        EAVNode parent = null;
        EAVNode firstNode = null;
        
        while(!parentPath.isEmpty()) {
            EAVNode current = new EAVNode(parentPath.first());
            if(parent != null) {
                parent.addChild(current);
            }
            if(firstNode == null) {
                firstNode = current;
            }
            parent = current;
            parentPath = parentPath.shiftLeft();
        }
        
        parent.addChild(tree);
        return firstNode;
    }

    public void replace(Context ctx, EAVPath path, EAVNode tree) throws EAVException {
        // TODO Auto-generated method stub

    }

    public void update(Context ctx, EAVPath parentPath, EAVNode tree) throws EAVException {
        // TODO Auto-generated method stub

    }

    public void updateSets(Context ctx, EAVPath path, EAVSetTransformation update) throws EAVException {
        // TODO Auto-generated method stub

    }
    
    private int getModule(EAVPath path) {
        String module = path.first();
        if (module.equalsIgnoreCase("calendar")) {
            return Types.APPOINTMENT;
        } else if (module.equalsIgnoreCase("contact")) {
            return Types.CONTACT;
        } else if (module.equalsIgnoreCase("task")) {
            return Types.TASK;
        } else if (module.equalsIgnoreCase("folder")) {
            return Types.FOLDER;
        } else {
            return 0;
        }
    }
    
    private int getFolderId(EAVPath path) {
        if (getModule(path) == 0) {
            return 0;
        } else {
            return Integer.parseInt(path.shiftLeft().first());
        }
    }
    
    private int getObjectId(EAVPath path) {
        int module = getModule(path);
        if (module == Types.FOLDER) {
            return Integer.parseInt(path.shiftLeft().first());
        } else if (module == 0) {
            return 0;
        } else {
            return Integer.parseInt(path.shiftLeft().shiftLeft().first());
        }
    }
    
    private EAVPath chopPath(EAVPath path) {
        int module = getModule(path);
        if (module == Types.FOLDER) {
            return path.shiftLeft().shiftLeft();
        } else if (module == 0) {
            return path.shiftLeft();
        } else {
            return path.shiftLeft().shiftLeft().shiftLeft();
        }
    }
    
    private static final class ObjectAddress {
        public Context ctx;
        public int module;
        public int id;
        
        public ObjectAddress(Context ctx, EAVNode absoluteTree) {
            //TODO
            module = Types.CONTACT;
            id = Integer.parseInt(absoluteTree.getChildren().get(0).getChildren().get(0).getName());
            this.ctx = ctx;
        }

        public static EAVNode getEAVRoot(EAVNode absoluteTree) {
            EAVNode node = TreeTools.copy(absoluteTree.getChildren().get(0).getChildren().get(0).getChildren().get(0));
            return node;
        }
    }

}
