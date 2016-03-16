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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.payload.converter.impl.DefaultPayloadTreeConverter;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link PayloadTreeNode} - A Node of the complete PayloadTree found in a Stanza. Holds a PayloadElement as data and may hava 0 - n
 * children.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PayloadTreeNode implements VisitablePayload, Serializable {

    private static final long serialVersionUID = -4402820599792777503L;

    public static volatile DefaultPayloadTreeConverter CONVERTER = null;
    
    private PayloadTreeNode parent;
    
    // ElementPath for empty container nodes
    private ElementPath elementPath;

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
     * Initializes a new {@link PayloadTreeNode} without a PayloadElement For e.g. Collection nodes that contain data as cildren. 
     * 
     * @param elementPath The {@link ElementPath} to use for this node.
     */
    public PayloadTreeNode(ElementPath elementPath) {
        this();
        this.elementPath = elementPath;
    }

    /**
     * Initializes a new {@link PayloadTreeNode} based on another PayloadTreeNode
     *
     * @param otherTreeNode the other PayloadTreeNode, must not be null
     * @throws IllegalArgumentException if the other PayloadTreeNode is null
     */
    public PayloadTreeNode(PayloadTreeNode otherTreeNode) {
        this();
        if (otherTreeNode == null) {
            throw new IllegalArgumentException("Other PayloadTreeNode must not be null.");
        }
        PayloadElement otherPayloadElement = otherTreeNode.payloadElement;
        if (otherPayloadElement != null) {
            this.payloadElement = new PayloadElement(otherPayloadElement);
        }
        for (PayloadTreeNode otherChild : otherTreeNode.children) {
            children.add(new PayloadTreeNode(otherChild));
        }
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
     * Gets an unmodifiable view of the children of this node.
     *
     * @return Unmodifiable view of the children of this node.
     */
    public Collection<PayloadTreeNode> getChildren() {
        return Collections.unmodifiableCollection(children);
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

    public void setElementPath(ElementPath elementPath) {
        this.elementPath = elementPath;
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
        if (payloadElement != null) {
            return new ElementPath(getNamespace(), getElementName());
        }
        return elementPath;
    }

    /**
     * Get the namespace of the PayloadElement associated with this node.
     *
     * @return Null or the namespace of the PayloadElement associated with this node
     * @see com.openexchange.realtime.payload.PayloadElement#getNamespace()
     */
    public String getNamespace() {
        if (payloadElement != null) {
            return payloadElement.getNamespace();
        } else {
            return elementPath.getNamespace();
        }
    }

    /**
     * Get the namespaces of the PayloadElements associated with this node and all children.
     *
     * @return An empty Collection if the PayloadElements don't contain namespaces or a Collection of the namespaces of the PayloadElement
     *         associated with this node and all children
     */
    public Collection<String> getNamespaces() {
        ArrayList<String> namespaces = new ArrayList<String>();
        namespaces.add(getNamespace());
        for (PayloadTreeNode child : children) {
            namespaces.addAll(child.getNamespaces());
        }
        return namespaces;
    }

    @Override
    public void accept(PayloadVisitor visitor) {
        payloadElement.accept(visitor);
        for (PayloadTreeNode child : children) {
            child.accept(visitor);
        }
    }
    
    public PayloadTreeNode toInternal() throws OXException {
        if (CONVERTER != null) {
            return CONVERTER.incoming(this);
        }
        throw new IllegalStateException("No Converter is set!");
    }
    
    public PayloadTreeNode toExternal(String format) throws OXException {
        if (CONVERTER != null) {
            return CONVERTER.outgoing(this, format);
        }
        throw new IllegalStateException("No Converter is set!");
    }
    
    public PayloadTreeNode internalClone() throws OXException {
        return toExternal("native").toInternal();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((payloadElement == null) ? 0 : payloadElement.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PayloadTreeNode)) {
            return false;
        }
        PayloadTreeNode other = (PayloadTreeNode) obj;
        if (payloadElement == null) {
            if (other.payloadElement != null) {
                return false;
            }
        } else if (!payloadElement.equals(other.payloadElement)) {
            return false;
        }
        if (children == null) {
            if (other.children != null) {
                return false;
            }
        } else if (!children.equals(other.children)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PayloadTreeNode" + "@" + hashCode() + " [parent=" + (parent == null ? null : parent.getClass().getSimpleName() + "@" + parent.hashCode()) + ", payloadElement=" + payloadElement + ", children=" + children.size() + "]";
    }

    /**
     * Recursively format a PayloadTreeNode to a String
     *
     * @param node The PayloadTreeNode where transformation starts
     * @param numOfTabs Indentation level. Normally one would start with 0.
     * @return The PayloadTreeNode as String
     */
    public String recursiveToString(int numOfTabs) {
        return recursiveToString(this, numOfTabs);
    }

    private static String recursiveToString(PayloadTreeNode node, int numOfTabs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numOfTabs; i++) {
            sb.append('\t');
        }
        sb.append(node);
        int childIndent = numOfTabs + 1;
        for (PayloadTreeNode child : node.getChildren()) {
            sb.append('\n').append(recursiveToString(child, childIndent));
        }
        return sb.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Static {@link Builder} to create nested PayloadTreeNodes and Trees more fluently.
     *
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    public static class Builder {

        PayloadTreeNode node;

        /**
         * Initializes a new {@link Builder} with an empty PayloadTreeNode that can then be modified via the Builder.
         */
        public Builder() {
            this.node = new PayloadTreeNode();
        }

        public Builder withoutPayload(String namespace, String elementName) {
            node.setElementPath(new ElementPath(namespace, elementName));
            return this;
        }
        /**
         * Create and set the PayloadElemet of the PayloadTreeNode we are currently building.
         *
         * @param data data of the new PayloadElement
         * @param format format of the new PayloadElement
         * @param namespace namespace of the new PayloadElement
         * @param elementName elementname of the new PayloadElement
         * @return the builder for further modification or building of the current PayloadTreenode
         */
        public Builder withPayload(Object data, String format, String namespace, String elementName) {
            node.setPayloadElement(new PayloadElement(data, format, namespace, elementName));
            return this;
        }

        /**
         * Set the PayloadElemet of the PayloadTreeNode we are currently building.
         *
         * @param payloadElement the payloadElement to use
         * @return the builder for further modification or building of the current PayloadTreenode
         */
        public Builder withPayload(PayloadElement payloadElement) {
            node.setPayloadElement(payloadElement);
            return this;
        }
  
        /**
         * Create a new PayloadTreeNode and add it as child to the PayloadTreeNode we are currently building.
         *
         * @param data data of the PayloadElement of the new PayloadTreeNode
         * @param format format of the PayloadElement of the new PayloadTreeNode
         * @param namespace namespace of the PayloadElement of the new PayloadTreeNode
         * @param elementName elementname of the PayloadElement of the new PayloadTreeNode
         * @return the builder for further modification or building of the current PayloadTreenode
         */
        public Builder andChild(Object data, String format, String namespace, String elementName) {
            node.addChild(new PayloadTreeNode(new PayloadElement(data, format, namespace, elementName)));
            return this;
        }

        /**
         * Create a new PayloadTreeNode and add it as child to the PayloadTreeNode we are currently building.
         *
         * @param data Uniform list data of the PayloadElement of the new PayloadTreeNodes
         * @param format format of all the PayloadElements of the new PayloadTreeNodes
         * @param namespace namespace of all the PayloadElements of the new PayloadTreeNodes
         * @param elementName elementname of all the PayloadElements of the new PayloadTreeNodes
         * @return the builder for further modification or building of the current PayloadTreenode
         */
        public Builder andChildren(List<? extends Object> data, String format, String namespace, String elementName) {
            for (Object object : data) {
                node.addChild(new PayloadTreeNode(new PayloadElement(object, format, namespace, elementName)));
            }
            return this;
        }

        /**
         * Create a new PayloadTreeNode and add it as child to the PayloadTreeNode we are currently building.
         *
         * @param payloadElement the PayloadElement used to create a new child which is going to be added to the PayloadTreeNode we are
         *            currently building
         * @return the builder for further modification or building of the current PayloadTreenode
         */
        public Builder andChild(PayloadElement payloadElement) {
            PayloadTreeNode child = new PayloadTreeNode(payloadElement);
            node.addChild(child);
            return this;
        }

        /**
         * Add a PayloadTreeNode as child to the PayloadTreeNode we are currently building.
         *
         * @param payloadTreeNode the PayloadTreeNode to adds to the PayloadTreeNode we are currently building
         * @return the builder for further modification or building of the current PayloadTreenode
         */
        public Builder andChild(PayloadTreeNode payloadTreeNode) {
            node.addChild(payloadTreeNode);
            return this;
        }

        /**
         * Validate and return the constructed PayloadTreeNode.
         *
         * @return the constructed PayloadTreeNode
         */
        public PayloadTreeNode build() {
            return this.node;
        }

    }

}
