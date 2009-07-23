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

package com.openexchange.eav.storage.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVNodeVisitor;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVSetTransformation;
import com.openexchange.eav.EAVSetTransformationVisitor;
import com.openexchange.eav.EAVType;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link InMemoryStorage}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InMemoryStorage {

    private EAVNode root = new EAVNode("");
    
    public void insert(Context ctx, EAVPath parentPath, EAVNode tree) {
        EAVNode node = getContainerAndAppendMissing(root, parentPath);
        node.addChildren(tree);
    }

    public void update(Context ctx, EAVPath parentPath, EAVNode tree) {
        EAVNode node = getContainerAndAppendMissing(root, parentPath);
        EAVNode child = node.getChildByName(tree.getName());
        if(child == null) {
            insert(ctx, parentPath, tree);
        } else {
            merge(ctx, node, tree);
        }
    }

    private void merge(final Context ctx, final EAVNode child, final EAVNode update) {
        final List<EAVNode> leavesToAdd = new ArrayList<EAVNode>();
        update.visit(new EAVNodeVisitor() {
            public void visit(int index, EAVNode node) {
                if(!node.isLeaf()) {
                    return;
                }
                EAVPath relativePath = node.getPath();
                EAVNode toUpdate = child.resolve(relativePath);
                if(toUpdate != null) {
                    if(node.getType() == EAVType.NULL) {
                        toUpdate.getParent().removeChild(node.getName());
                    } else {
                        toUpdate.copyPayload(node);
                    }
                } else {
                    leavesToAdd.add(node);
                }
            }
        });
        for(EAVNode leaf : leavesToAdd) {
            getContainerAndAppendMissing(child, leaf.getPath().parent()).addChildren(leaf);
        }
    }

    public void updateArrays(Context ctx, EAVPath path, EAVSetTransformation update) {
        final EAVNode parent = getContainerAndAppendMissing(root, path);
        
        update.visit(new EAVSetTransformationVisitor() {
            public void visit(int index, EAVSetTransformation node) {
                if(!node.isLeaf()) {
                    return;
                }
                
                EAVPath relativePath = node.getPath();
                EAVNode toUpdate = parent.resolve(relativePath);
                if(toUpdate != null && toUpdate.isMultiple()) {
                    applyArrayUpdate(toUpdate, node);
                }
            }

        });
        
        
    }

    private void applyArrayUpdate(EAVNode toUpdate, EAVSetTransformation node) {
        
    }

    public void replace(Context ctx, EAVPath parentPath, EAVNode tree) {

    }

    public EAVNode get(Context ctx, EAVPath path) {
        return root.resolve(path);
    }

    public EAVNode get(Context ctx, EAVPath path, boolean allBinaries) {
        return null;
    }

    public EAVNode get(Context ctx, EAVPath path, List<EAVNode> loadBinaries) {
        return null;
    }

    public void delete(Context ctx, EAVPath path) {

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



}
