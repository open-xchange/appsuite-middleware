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

package com.openexchange.eav.storage.memory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.eav.EAVException;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVNodeProcessor;
import com.openexchange.eav.EAVNodeVisitor;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVPathFilter;
import com.openexchange.eav.EAVSelectiveFilter;
import com.openexchange.eav.EAVSetTransformation;
import com.openexchange.eav.EAVSetTransformationVisitor;
import com.openexchange.eav.EAVStorage;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeFilter;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.TreeTools;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link InMemoryStorage}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InMemoryStorage implements EAVStorage {

    private Map<Integer, EAVNode> roots = new HashMap<Integer, EAVNode>();
    
    private EAVNode getRoot(Context ctx) {
        EAVNode root = roots.get(ctx.getContextId());
        if(root != null) {
            return root;
        }
        root = new EAVNode();
        roots.put(ctx.getContextId(), root);
        return root;
    }
    
    public void insert(Context ctx, EAVPath parentPath, EAVNode tree) throws EAVException {
        assertNotSet(getRoot(ctx), parentPath.append(tree.getName()));
        EAVNode node = getContainerAndAppendMissing(getRoot(ctx), parentPath);
        node.addChildren(copyIncoming(tree));
    }

    private void assertNotSet(EAVNode node, EAVPath path) throws EAVException {
        if(null != node.resolve(path)) {
            throw EAVErrorMessage.PATH_TAKEN.create(path.toString());
        }
    }

    public void update(Context ctx, EAVPath parentPath, EAVNode tree) throws EAVException{
        EAVNode node = resolveStrict(getRoot(ctx), parentPath);
        EAVNode child = node.getChildByName(tree.getName());
        if(child == null) {
            insert(ctx, parentPath, tree);
        } else {
            merge(ctx, node, tree);
        }
    }

    private void merge(final Context ctx, final EAVNode child, final EAVNode update) throws EAVException {
        final List<EAVNode> leafsToAdd = new ArrayList<EAVNode>();
        final List<NodeTuple> updates = new ArrayList<NodeTuple>();
        final List<EAVNode> leafsToRemove = new ArrayList<EAVNode>();
        
        update.visit(new EAVNodeVisitor() {
            public void visit(int index, EAVNode node) {
                if(!node.isLeaf()) {
                    return;
                }
                EAVPath relativePath = node.getPath();
                EAVNode toUpdate = child.resolve(relativePath);
                if(toUpdate != null) {
                    if(node.getType() == EAVType.NULL) {
                        leafsToRemove.add(toUpdate);
                    } else {
                        updates.add(new NodeTuple(toUpdate, node));
                    }
                } else {
                    leafsToAdd.add(node);
                }
            }
        });
        
        for (NodeTuple nodeTuple : updates) {
            assertTypesMatch(nodeTuple.left, nodeTuple.right);
        }
        
        for (NodeTuple nodeTuple : updates) {
            nodeTuple.left.copyPayloadFromOther(nodeTuple.right);
            processIncoming(nodeTuple.left);
        }
        
        for(EAVNode leaf : leafsToRemove) {
            leaf.getParent().removeChild(leaf.getName());
        }
        
        for(EAVNode leaf : leafsToAdd) {
            getContainerAndAppendMissing(child, leaf.getPath().parent()).addChildren(copyIncoming(leaf));
        }
        
        
    }

    private void assertTypesMatch(EAVNode left, EAVNode right) throws EAVException {
        if(left.getType() != right.getType() || left.getContainerType() != right.getContainerType()) {
            throw EAVErrorMessage.TYPE_MISMATCH.create(left.toString(), left.getType().name(), left.getContainerType().name(), right.getType().name(), right.getContainerType().name());
        }
    }
    
    private void assertTypesMatch(EAVNode node, EAVSetTransformation transformation) throws EAVException {
        if(node.getType() != transformation.getType()) {
            throw EAVErrorMessage.TYPE_MISMATCH.create(node.toString(), node.getType().name(), node.getContainerType().name(), transformation.getType().name(), "MULTIPLE");
        }
    }

    public void updateSets(Context ctx, EAVPath path, EAVSetTransformation update) throws EAVException{
        final EAVNode parent = resolveStrict(getRoot(ctx), path);
        
        final List<SetUpdate> setUpdates = new ArrayList<SetUpdate>();
        update.visit(new EAVSetTransformationVisitor() {
            public void visit(int index, EAVSetTransformation node) {
                if(!node.isLeaf()) {
                    return;
                }
                
                EAVPath relativePath = node.getPath();
                EAVNode toUpdate = parent.resolve(relativePath);
                setUpdates.add(new SetUpdate(toUpdate, node));
            }

        });
        
        for (SetUpdate setUpdate : setUpdates) {
            if(setUpdate.node == null) {
                throw EAVErrorMessage.UNKNOWN_PATH.create(setUpdate.transformation.getPath());
            }
            assertTypesMatch(setUpdate.node, setUpdate.transformation);
            assertIsMultiple(setUpdate.node);
        
        }

        for (SetUpdate setUpdate : setUpdates) {
            applySetUpdate(setUpdate.node, setUpdate.transformation);
        }
    }
    
    private EAVNode resolveStrict(EAVNode node, EAVPath path) throws EAVException {
        EAVNode subNode = node.resolve(path);
        if(subNode == null) {
            throw EAVErrorMessage.UNKNOWN_PATH.create(path);
        }
        return subNode;
       
    }

    private void assertIsMultiple(EAVNode node) throws EAVException {
        if(!node.isMultiple()) {
            throw EAVErrorMessage.CAN_ONLY_ADD_AND_REMOVE_FROM_SET_OR_MULTISET.create(node.getPath().toString(), node.getContainerType().name());
        }
    }

    private static final EAVSetUpdater SET_UPDATER = new EAVSetUpdater();
    
    private void applySetUpdate(EAVNode toUpdate, EAVSetTransformation node) {
        toUpdate.getContainerType().doSwitch(SET_UPDATER, toUpdate, node);
    }

    public void replace(Context ctx, EAVPath path, EAVNode tree) throws EAVException{
        final EAVNode parent = getContainerAndAppendMissing(getRoot(ctx), path);
        parent.replaceChild(copyIncoming(tree));
    }

    public EAVNode get(Context ctx, EAVPath path) throws EAVException{
        EAVNode found = resolveStrict(getRoot(ctx), path);
        if(found.isLeaf()) {
            return copyOutgoing(found);
        }
        return copyNoBinaries(found);
    }


    public EAVNode get(Context ctx, EAVPath path, boolean allBinaries) throws EAVException{
        return copyOutgoing(resolveStrict(getRoot(ctx), path));
    }

    public EAVNode get(Context ctx, EAVPath path, Set<EAVPath> loadBinaries) throws EAVException{
        return copyOutgoingWithNamedBinaries(resolveStrict(getRoot(ctx), path), loadBinaries);
    }


    public void delete(Context ctx, EAVPath path) throws EAVException{
        EAVNode node = getContainerAndAppendMissing(getRoot(ctx), path);
        node.getParent().removeChild(node.getName());
    }
    
    
    private EAVNode getContainerAndAppendMissing(EAVNode parent, EAVPath path) {
        if(path.isEmpty()) {
            return parent;
        }
        EAVNode childNode = parent.getChildByName(path.first());
        if(childNode == null) {
            childNode = new EAVNode(parent, path.first());
        }
        return getContainerAndAppendMissing(childNode, path.shiftLeft());
    }
    
    public EAVTypeMetadataNode getTypes(Context ctx, EAVPath parent, EAVNode node) throws EAVException {
        EAVNode child = resolveStrict(getRoot(ctx), parent.append(node.getName()));
        EAVTypeMetadataNode retval = new EAVTypeMetadataNode(node.getName());
        copyTypeMetadata(child, retval);
        return retval;
    }

    
    private void copyTypeMetadata(EAVNode child, EAVTypeMetadataNode retval) {
        if(child.isLeaf()) {
            retval.setType(child.getType());
            retval.setContainerType(child.getContainerType());
        } else {
            for(EAVNode subnode : child.getChildren()) {
                EAVTypeMetadataNode metadata = new EAVTypeMetadataNode(subnode.getName());
                copyTypeMetadata(subnode, metadata);
                retval.addChild(metadata);
            }
        }
    }

    private EAVNode copyNoBinaries(EAVNode original) {
        return TreeTools.copy(original, new EAVTypeFilter(EnumSet.complementOf(EnumSet.of(EAVType.BINARY))));
    }

    private EAVNode copyOutgoing(EAVNode original) {
        return TreeTools.copy(original, new ByteArrayToInputStreamProcessor());
    }

    private EAVNode copyOutgoingWithNamedBinaries(EAVNode original, Set<EAVPath> loadBinaries) {
        return TreeTools.copy(original, new EAVSelectiveFilter(EnumSet.of(EAVType.BINARY), new EAVPathFilter(loadBinaries,  original.getPath())), new ByteArrayToInputStreamProcessor());
    }


    private EAVNode copyIncoming(EAVNode original) {
        return TreeTools.copy(original, new InputStreamToByteArrayProcessor());
    }
    
    private static final InputStreamToByteArrayProcessor incomingProcessor = new InputStreamToByteArrayProcessor();
    
    private void processIncoming(EAVNode node) {
        incomingProcessor.process(node);
    }
    
    private static final class NodeTuple {
        public EAVNode left;
        public EAVNode right;
        
        private NodeTuple(EAVNode left, EAVNode right) {
            this.left = left;
            this.right = right;
        }
        
    }
    
    private static final class SetUpdate {
        public EAVNode node;
        public EAVSetTransformation transformation;
        
        private SetUpdate(EAVNode node, EAVSetTransformation transformation) {
            this.node = node;
            this.transformation = transformation;
        }
        
    }
    
    private static final class InputStreamToByteArrayProcessor implements EAVNodeProcessor {

        public EAVNode process(EAVNode node) {
            if(node.getType() == EAVType.BINARY && !node.isMultiple()) {
                InputStream data = (InputStream) node.getPayload();
                List<Byte> bytes = new ArrayList<Byte>();
                
                try {
                    int b = -1;
                    while((b = data.read()) != -1) {
                        bytes.add((byte)b);
                    }
                    
                    byte[] byteArr = new byte[bytes.size()];
                    int index = 0;
                    for(Byte d : bytes) {
                        byteArr[index++] = d;
                    }
                    
                    node.setPayload(node.getType(), node.getContainerType(), byteArr);

                } catch (IOException x) {
                    x.printStackTrace();
                }
            }
            return node;
        }
        
    }
    
    private static final class ByteArrayToInputStreamProcessor implements EAVNodeProcessor {

        public EAVNode process(EAVNode node) {
            if(node.getType() == EAVType.BINARY && !node.isMultiple()) {
                byte[] bytes = (byte[]) node.getPayload();
                node.setPayload(node.getType(), node.getContainerType(), new ByteArrayInputStream(bytes));
            }
            return node;
        }
        
    }

    

}
