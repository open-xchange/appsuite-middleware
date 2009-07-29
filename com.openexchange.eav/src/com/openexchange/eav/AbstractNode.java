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

package com.openexchange.eav;

import java.util.ArrayList;
import java.util.List;


/**
 * {@link AbstractNode}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class AbstractNode<T extends AbstractNode<T>> {
    
    protected T parent;
    protected List<T> children = new ArrayList<T>();
    protected String name;
    
    public AbstractNode(T parent) {
        this(parent, null);
    }
    
    public AbstractNode() {
        this((String)null);
    }
    
    public AbstractNode(T parent, String name) {
        this.parent = parent;
        parent.getChildren().add((T)this);
        this.name = name;
    }
    
    public AbstractNode(String name) {
        this.name = name;
    }
    
    public boolean isRoot() {
        return parent == null;
    }


    public List<T> getChildren() {
        return children;
    }


    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public EAVPath getPath() {
        if(parent == null) {
            return new EAVPath(name);
        }
        return parent.getPath().append(name);
    }
    
    public EAVPath getRelativePath(EAVPath relativePath) {
        if (getPath().equals(relativePath)) {
            return new EAVPath();
        }
        return parent.getRelativePath(relativePath).append(name);
    }

    public EAVPath getRelativePath(T relativeNode) {
        return getRelativePath(relativeNode.getPath());
    }


    
    public T getParent() {
        return parent;
    }

    public void removeChild(String childName) {
        List<T> newNodes = new ArrayList<T>(children.size());
        for(T child : children) {
            if(!child.getName().equals(childName)) {
                newNodes.add(child);
            }
        }
        this.children = newNodes;
    }
    
    public void addChild(T child) {
        addChildren(child);
    }
    
    public void replaceChild(T tree) {
        removeChild(tree.getName());
        addChildren(tree);
    }


    public void visitUpward(AbstractNodeVisitor<T> visitor) {
        visitUpward(0, visitor);
    }

    private void visitUpward(int index, AbstractNodeVisitor<T> visitor) {
        visitor.visit(index, (T) this);
        if(parent != null) {
            parent.visitUpward(index-1, visitor);
        }        
    }
    
    public void visit(AbstractNodeVisitor<T> visitor) {
        try {
            visit(0, visitor);
        } catch (AbstractNodeVisitor.RecursionBreak br) {
            return;
        }
    }

    private void visit(int index, AbstractNodeVisitor<T> visitor) {
            boolean recurseFurther = true;
            try {
                visitor.visit(index, (T) this);
            } catch (AbstractNodeVisitor.SkipSubtree skip) {
                recurseFurther = false;
            }
            if(recurseFurther) {
                for(T child : children) {
                    child.visit(index+1, visitor);
                }
            }
    }

    public T resolve(EAVPath path) {
        if(path.isEmpty()) {
            return (T)this;
        }
        T child = getChildByName(path.first());
        if(child == null) {
            return null;
        }
        return child.resolve(path.shiftLeft());
    }
    
    public String toString() {
        return getPath().toString();
    }

    public T getChildByName(String name) {
        for(T node : children) {
            if(name.equals(node.getName())) {
                return node;
            }
        }
        return null;
    }

    public void addChildren(T...children) {
        for(T child : children) {
            this.children.add(child);
            child.parent = (T)this;
        }
    }
    
    
    public abstract void copyPayloadFromOther(T other);
    public abstract T newInstance();
   
    
}
