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

package com.openexchange.realtime.payload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link PayloadTreeNode} -
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PayloadTreeNode {

    private PayloadTreeNode parent;

    /** The payloadElement representing the data of this node. Used as Delegate. */
    private PayloadElement payloadElement;

    private List<PayloadTreeNode> children;

    /**
     * Initializes a new {@link PayloadTreeNode}.
     */
    public PayloadTreeNode() {
        children = new ArrayList<PayloadTreeNode>();
    }

    /**
     * Initializes a new {@link PayloadTreeNode} with the given PayloadElement as data.
     * 
     * @param payloadElement the PayloadElement to associate with this node
     */
    public PayloadTreeNode(PayloadElement payloadElement) {
        this();
        this.payloadElement = payloadElement;
    }

    /**
     * Gets the parent node
     * 
     * @return The parent
     */
    public PayloadTreeNode getParent() {
        return parent;
    }

    /**
     * Sets the parent node
     * 
     * @param parent The parent to set
     */
    public void setParent(PayloadTreeNode parent) {
        this.parent = parent;
    }

    /**
     * Gets the PayloadElement of associated with this node.
     * 
     * @return The the PayloadElement
     */
    public PayloadElement getPayloadElement() {
        return payloadElement;
    }

    /**
     * Sets the PayloadElement of associated with this node.
     * 
     * @param data The the PayloadElement to set.
     */
    public void setPayloadElement(PayloadElement data) {
        this.payloadElement = data;
    }

    /**
     * Gets the children of this node
     * 
     * @return The children
     */
    public List<PayloadTreeNode> getChildren() {
        return children;
    }

    /**
     * Get the number of children (1 level below this node)
     * 
     * @return the number of children (1 level below this node)
     */
    public int getNumberOfChildren() {
        return getChildren().size();
    }

    /**
     * Check for children below this node
     * 
     * @return true if there are children below this node, false otherwise
     */
    public boolean hasChildren() {
        return (getNumberOfChildren() > 0);
    }

    /**
     * Sets the children of this node
     * 
     * @param nodes The nodes to set as children of this node
     */
    public void setChildren(Collection<PayloadTreeNode> nodes) {
        ArrayList<PayloadTreeNode> children = new ArrayList<PayloadTreeNode>(nodes);
        for (PayloadTreeNode node : children) {
            node.setParent(this);
        }
        this.children = children;
    }

    /**
     * Add a node to the children of this node.
     * 
     * @param node the node to add
     * @return This PayloadTreeNode
     */
    public PayloadTreeNode addChild(PayloadTreeNode node) {
        node.parent = this;
        children.add(node);
        return this;
    }

    /**
     * Add a Collection of nodes to the children of this node.
     * 
     * @param nodes the nodes to add
     * @return This PayloadTreeNode
     */
    public PayloadTreeNode addChildren(Collection<PayloadTreeNode> nodes) {
        for (PayloadTreeNode node : nodes) {
            node.parent = this;
        }
        children.addAll(nodes);
        return this;
    }

    /**
     * Remove a node from the children of this node.
     * 
     * @param node The node to remove
     * @return Ths PayloadTreeNode
     */
    public PayloadTreeNode removeChild(PayloadTreeNode node) {
        children.remove(node);
        return this;
    }

    /**
     * Remove a Collection of nodes from the children of this node.
     * 
     * @param nodes The nodes to remove
     * @return
     */
    public PayloadTreeNode removeChildren(Collection<PayloadTreeNode> nodes) {
        children.removeAll(nodes);
        return this;
    }

    // -------------------------------------------------------------------------
    // Delegate methods

    /**
     * Get the data object from the PayloadElement associated with this node.
     * 
     * @return Null or the data object from the PayloadElement associated with this node.
     * @see com.openexchange.realtime.payload.PayloadElement#getData()
     */
    public Object getData() {
        if (payloadElement != null) {
            return payloadElement.getData();
        }
        return null;
    }

    /**
     * Set the data object of PayloadElement associated with this node.
     * 
     * @param data The data object from the PayloadElement associated with this node.
     * @param format The data object's format
     * @see com.openexchange.realtime.payload.PayloadElement#setData(java.lang.Object, java.lang.String)
     * @throws IllegalStateExcpetion If no PayloadElement is associated with this node
     */
    public void setData(Object data, String format) {
        if (payloadElement == null) {
            throw new IllegalStateException("PayloadElement delegate wasn't set, yet!");
        }
        payloadElement.setData(data, format);
    }

    /**
     * Get the element name of the PayloadElement associated with this node.
     * 
     * @return Null or the element name of the PayloadElement associated with this node
     * @see com.openexchange.realtime.payload.PayloadElement#getElementName()
     */
    public String getElementName() {
        if (payloadElement != null) {
            return payloadElement.getElementName();
        }
        return null;

    }

    /**
     * Get the format of the PayloadElement associated with this node.
     * 
     * @return Null or the format of the PayloadElement associated with this node.
     * @see com.openexchange.realtime.payload.PayloadElement#getFormat()
     */
    public String getFormat() {
        if (payloadElement != null) {
            return payloadElement.getFormat();
        }
        return null;
    }

    /**
     * Get the ElementPath of the PayloadElement associated with this node.
     * 
     * @return Null or the ElementPath of the PayloadElement associated with this node
     */
    public ElementPath getElementPath() {
        if(payloadElement != null) {
            return new ElementPath(getNamespace(), getElementName());
        }
        return null;
    }

    /**
     * Get the namespace of the PayloadElement associated with this node.
     * 
     * @return Null or the namespace of the PayloadElement associated with this node 
     * @see com.openexchange.realtime.payload.PayloadElement#getNamespace()
     */
    public String getNamespace() {
        if(payloadElement != null) {
            return payloadElement.getNamespace();
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((payloadElement == null) ? 0 : payloadElement.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PayloadTreeNode))
            return false;
        PayloadTreeNode other = (PayloadTreeNode) obj;
        if (children == null) {
            if (other.children != null)
                return false;
        } else if (!children.equals(other.children))
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (payloadElement == null) {
            if (other.payloadElement != null)
                return false;
        } else if (!payloadElement.equals(other.payloadElement))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PayloadTreeNode [ payloadElement=" + payloadElement + ", children=" + children + "]";
    }

}
