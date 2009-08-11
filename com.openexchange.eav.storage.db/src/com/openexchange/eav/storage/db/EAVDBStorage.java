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

import java.sql.Connection;
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
import com.openexchange.eav.storage.db.sql.PathIndex;
import com.openexchange.eav.storage.db.sql.Paths;
import com.openexchange.eav.storage.db.sql.SQLType;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.Command;
import com.openexchange.sql.grammar.INSERT;

import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;


/**
 * {@link EAVDBStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class EAVDBStorage implements EAVStorage {

    private static final Log LOG = LogFactory.getLog(EAVDBStorage.class);
    
    private DBProvider provider;

    public EAVDBStorage(DBProvider provider) {
        this.provider = provider;
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.eav.EAVStorage#delete(com.openexchange.groupware.contexts.Context, com.openexchange.eav.EAVPath)
     */
    public void delete(Context ctx, EAVPath path) throws EAVException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.eav.EAVStorage#get(com.openexchange.groupware.contexts.Context, com.openexchange.eav.EAVPath)
     */
    public EAVNode get(Context ctx, EAVPath path) throws EAVException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.eav.EAVStorage#get(com.openexchange.groupware.contexts.Context, com.openexchange.eav.EAVPath, boolean)
     */
    public EAVNode get(Context ctx, EAVPath path, boolean allBinaries) throws EAVException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.eav.EAVStorage#get(com.openexchange.groupware.contexts.Context, com.openexchange.eav.EAVPath, java.util.Set)
     */
    public EAVNode get(Context ctx, EAVPath path, Set<EAVPath> loadBinaries) throws EAVException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.eav.EAVStorage#getTypes(com.openexchange.groupware.contexts.Context, com.openexchange.eav.EAVPath, com.openexchange.eav.EAVNode)
     */
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
        
        EAVNode rootNode = ObjectAddress.getEAVRoot(absoluteTree);
        
        dumpNode(tables, address, rootNode, -1);
    }


    private void dumpNode(Map<SQLType, String> tables, ObjectAddress address, EAVNode node, int parentId) throws EAVStorageException {
        
        int id = writePathElement(tables.get(SQLType.PATH), address, node.getName(), node.getType(), parentId);
        
        if(node.isLeaf()) {
            SQLType sqlType = SQLType.chooseType(node.getType(), node.getPayload());
            writePayload(tables.get(sqlType), address.ctx, id, sqlType, node.getType(), node.getContainerType(), node.getPayload());
        } else {
            for(EAVNode child : node.getChildren()) {
                dumpNode(tables, address, child, id);
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

    /* (non-Javadoc)
     * @see com.openexchange.eav.EAVStorage#replace(com.openexchange.groupware.contexts.Context, com.openexchange.eav.EAVPath, com.openexchange.eav.EAVNode)
     */
    public void replace(Context ctx, EAVPath path, EAVNode tree) throws EAVException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.eav.EAVStorage#update(com.openexchange.groupware.contexts.Context, com.openexchange.eav.EAVPath, com.openexchange.eav.EAVNode)
     */
    public void update(Context ctx, EAVPath parentPath, EAVNode tree) throws EAVException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.eav.EAVStorage#updateSets(com.openexchange.groupware.contexts.Context, com.openexchange.eav.EAVPath, com.openexchange.eav.EAVSetTransformation)
     */
    public void updateSets(Context ctx, EAVPath path, EAVSetTransformation update) throws EAVException {
        // TODO Auto-generated method stub

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
            //TODO
            return absoluteTree.getChildren().get(0).getChildren().get(0).getChildren().get(0);
        }
    }
}
