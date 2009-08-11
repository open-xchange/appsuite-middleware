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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.eav.EAVContainerType;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.storage.db.exception.EAVStorageException;
import com.openexchange.eav.storage.db.exception.EAVStorageExceptionMessage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.SELECT;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.eav.storage.db.sql.AbstractColumns.cid;
import static com.openexchange.eav.storage.db.sql.PathIndex.module;
import static com.openexchange.eav.storage.db.sql.PathIndex.objectId;
import static com.openexchange.eav.storage.db.sql.PathIndex.pathIndex;
import static com.openexchange.eav.storage.db.sql.PathIndex.pathTable;
import static com.openexchange.eav.storage.db.sql.Paths.*;
import static com.openexchange.sql.tools.SQLTools.createLIST;
import static com.openexchange.eav.storage.db.sql.AbstractDataTable.payload;
import static com.openexchange.eav.storage.db.sql.AbstractDataTable.containerType;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SQLStorage {

    private int id;

    private int mod;

    private Context ctx;
    
    private String pathsTable;
    
    private String ints, texts, varchars, blobs, bools, references;
    
    private Map<String, String> tables;
    
    private Connection con;

    private boolean allBinaries;

    public SQLStorage(Context ctx, int module, int objectId) {
        this.ctx = ctx;
        this.mod = module;
        this.id = objectId;
    }
    
    public void init(Connection con, boolean allBinaries) throws EAVStorageException {
        this.con = con;
        this.allBinaries = allBinaries;
        tables = new HashMap<String, String>();
        List<Object> values = new ArrayList<Object>();
        
        SELECT select = new SELECT(ASTERISK)
            .FROM(pathIndex)
            .WHERE(new EQUALS(cid, PLACEHOLDER)
                .AND(new EQUALS(module, PLACEHOLDER))
                .AND(new EQUALS(objectId, PLACEHOLDER)));
        
        values.add(ctx.getContextId());
        values.add(mod);
        values.add(id);

        StatementBuilder sb = new StatementBuilder();
        ResultSet rs = null;
        try {
            rs = sb.executeQuery(con, select, values);
            if (rs.next()) {
                pathsTable = rs.getString(pathTable.getName());
                ints = rs.getString(intTable.getName());
                texts = rs.getString(textTable.getName());
                varchars = rs.getString(varcharTable.getName());
                blobs = rs.getString(blobTable.getName());
                bools = rs.getString(boolTable.getName());
                references = rs.getString(referenceTable.getName());
                tables.put("intTable", ints);
                tables.put("textTable", texts);
                tables.put("varcharTable", varchars);
                tables.put("blobTable", blobs);
                tables.put("boolTable", bools);
                tables.put("referencesTable", references);
            }
        } catch (SQLException e) {
            throw EAVStorageExceptionMessage.SQLException.create(e);
        } finally {
            try {
                sb.closePreparedStatement(null, rs);
            } catch (SQLException e) {
                throw EAVStorageExceptionMessage.SQLException.create(e);
            }
        }
    }
    
    public EAVNode getEAVNode(EAVPath path) throws EAVStorageException {
        Node n = getNode(path);
        return getEAVNode(n);
    }
    
    private EAVNode getEAVNode(Node n) throws EAVStorageException {
        EAVNode retval = null;
        if (n.type == null) {
            writeInnerNode(retval, n);
        } else {
            retval = new EAVNode();
            writeLeaf(retval, n);
        }
        
        return retval;
    }
    
    private void writeInnerNode(EAVNode target, Node source) throws EAVStorageException {
        target.setName(source.getName());
        for (Node childNode : getChildNodes(source.getNodeId())) {
            target.addChild(getEAVNode(childNode));
        }
    }
    
    private List<Node> getChildNodes(int parentId) throws EAVStorageException {
        List<Node> retval = null;
        SELECT select = new SELECT(ASTERISK).FROM(pathsPrefix + pathsTable)
            .WHERE(new EQUALS(cid, PLACEHOLDER)
                .AND(new EQUALS(module, PLACEHOLDER))
                .AND(new EQUALS(parent, PLACEHOLDER)));
        
        List<Object> values = new ArrayList<Object>();
        values.add(ctx.getContextId());
        values.add(mod);
        values.add(parentId);
        
        StatementBuilder sb = new StatementBuilder();
        ResultSet rs = null;
        try {
            rs = sb.executeQuery(con, select, values);
            retval = getNodes(rs);
        } catch (SQLException e) {
            throw EAVStorageExceptionMessage.SQLException.create(e);
        } finally {
            try {
                sb.closePreparedStatement(null, rs);
            } catch (SQLException e) {
                throw EAVStorageExceptionMessage.SQLException.create(e);
            }
        }
        return retval;
    }
    
    private List<Node> getNodes(ResultSet rs) throws SQLException {
        List<Node> retval = new ArrayList<Node>();
        
        while (rs.next()) {
            Node n = new Node();
            n.setName(rs.getString(name.getName()));
            n.setNodeId(rs.getInt(nodeId.getName()));
            n.setParent(rs.getInt(parent.getName()));
            n.setType(rs.getString(eavType.getName()));
            retval.add(n);
        }
        
        return retval;
    }

    private void writeLeaf(EAVNode target, Node source) throws EAVStorageException {
        target.setName(source.getName());
        loadValue(target, source);
    }
    
    private void loadValue(EAVNode target, Node source) throws EAVStorageException {
        EAVType type = EAVType.getType(source.type);
        if (type == EAVType.BINARY && !allBinaries) {
            return;
        }
        String table = (String) type.doSwitch(EAVType.tableSwitcher);
        
        SELECT select = new SELECT(payload, containerType).FROM(tables.get(table))
            .WHERE(new EQUALS(cid, PLACEHOLDER)
                .AND(new EQUALS(nodeId, PLACEHOLDER)));
        
        List<Object> values = new ArrayList<Object>();
        values.add(ctx.getContextId());
        values.add(source.getNodeId());
        
        StatementBuilder sb = new StatementBuilder();
        ResultSet rs = null;
        Object object = null;
        EAVContainerType cType = null;
        try {
            rs = sb.executeQuery(con, select, values);
            
            if (rs.next()) {
                cType = EAVContainerType.getType(rs.getString(containerType.getName()));
                if (cType == EAVContainerType.MULTISET || cType == EAVContainerType.SET) {
                    List<Object> list = new ArrayList<Object>();
                    list.add(rs.getObject(payload.getName()));
                    while (rs.next()) {
                        list.add(rs.getObject(payload.getName()));
                    }
                    object = list.toArray();
                } else {
                    object = rs.getObject(payload.getName());
                }
            }
        } catch (SQLException e) {
            throw EAVStorageExceptionMessage.SQLException.create(e);
        } finally {
            try {
                sb.closePreparedStatement(null, rs);
            } catch (SQLException e) {
                throw EAVStorageExceptionMessage.SQLException.create(e);
            }
        }
        
        type.doSwitch(EAVType.valueSwitcher, target, cType, object);
    }
    
    private Node getNode(EAVPath path) throws EAVStorageException {
        SELECT select = new SELECT(ASTERISK).FROM(pathsPrefix + pathsTable)
            .WHERE(new EQUALS(cid, PLACEHOLDER)
                .AND(new EQUALS(module, PLACEHOLDER))
                .AND(new EQUALS(objectId, PLACEHOLDER))
                .AND(new IN(name, createLIST(path.length(), PLACEHOLDER))));
        
        List<Object> values = new ArrayList<Object>();
        values.add(ctx.getContextId());
        values.add(mod);
        values.add(id);
        EAVPath pointer = path;
        while (!pointer.isEmpty()) {
            values.add(pointer.last());
            pointer = pointer.parent();
        }
        
        StatementBuilder sb = new StatementBuilder();
        ResultSet rs = null;
        Node node = null;
        try {
            rs = sb.executeQuery(con, select, values);
            node = findNodeInResultSet(rs, path);
        } catch (SQLException e) {
            throw EAVStorageExceptionMessage.SQLException.create(e);
        } finally {
            try {
                sb.closePreparedStatement(null, rs);
            } catch (SQLException e) {
                throw EAVStorageExceptionMessage.SQLException.create(e);
            }
        }
        return node;
    }
    
    private Node findNodeInResultSet(ResultSet rs, EAVPath path) throws SQLException, EAVStorageException {
        Map<Integer, Node> nodes = new HashMap<Integer, Node>();
        List<Node> candidates = new ArrayList<Node>();
        
        while (rs.next()) {
            Node n = new Node();
            n.setName(rs.getString(name.getName()));
            n.setParent(rs.getInt(parent.getName()));
            n.setNodeId(rs.getInt(nodeId.getName()));
            n.setType(rs.getString(eavType.getName()));
            
            nodes.put(n.getNodeId(), n);
            if (path.last().equals(n.getName())) {
                candidates.add(n);
            }
        }
        
        if (candidates.size() == 1) {
            return candidates.get(0);
        } else if (candidates.size() == 0) {
            return null;
        }
        
        return processCandidates(candidates, nodes, path);
    }
    
    private Node processCandidates(List<Node> candidates, Map<Integer, Node> nodes, EAVPath path) throws EAVStorageException {
        Node retval = null;
        boolean found = false;
        for (Node candidate : candidates) {
            boolean isBad = false;
            Node pointer = candidate;
            EAVPath pathPointer = path;
            while (pointer.getParent() != null) {
                isBad = !pathPointer.last().equals(pointer.getName());
                if (isBad) {
                    break;
                }
                pointer = nodes.get(pointer.getParent());
                pathPointer = path.parent();
            }
            if (!isBad) {
                found = true;
                retval = candidate;
                break;
            }
        }
        if (!found) {
            throw EAVStorageExceptionMessage.NoSuchNodeException.create(path.toString());
        }
        return retval;
    }

    private class Node {
        private Integer nodeId;
        private String name;
        private Integer parent;
        private String type;
        
        public Integer getNodeId() {
            return nodeId;
        }
        
        public void setNodeId(Integer nodeId) {
            this.nodeId = nodeId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Integer getParent() {
            return parent;
        }
        
        public void setParent(Integer parent) {
            this.parent = parent;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
    }
    
}
