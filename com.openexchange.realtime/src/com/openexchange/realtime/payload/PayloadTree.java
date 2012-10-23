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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link PayloadTree} - Stanzas carry a payload that resembles an n-ary tree. This class handles the representation of this payload.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PayloadTree {

    private PayloadTreeNode root;

    /**
     * Initializes a new {@link PayloadTree} with an empty root node.
     */
    public PayloadTree() {
    }

    /**
     * Initializes a new {@link PayloadTree} with an initial root node.
     * 
     * @param root The root node of the new PayloadTree
     */
    public PayloadTree(PayloadTreeNode root) {
        this.root = root;
    }

    /**
     * Gets the root node of this PayloadTree.
     * 
     * @return The root
     */
    public PayloadTreeNode getRoot() {
        return root;
    }

    /**
     * Sets the root node of this PayloadTree.
     * 
     * @param root The root to set
     */
    public void setRoot(PayloadTreeNode root) {
        this.root = root;
    }

    /**
     * Check if this PayloadTreeNode is empty.
     * 
     * @return true if the root node is null, false otherwise.
     */
    public boolean isEmpty() {
        return (root == null);
    }

    /**
     * Get the number of nodes forming this PayloadTree.
     * 
     * @return The number of nodes forming this PayloadTree.
     */
    public int getNumberOfNodes() {
        int numberOfNodes = 0;

        if (root != null) {
            numberOfNodes += 1;
            numberOfNodes += recursiveGetNumberOfNodes(root);
        }

        return numberOfNodes;
    }

    /**
     * Recursively count the number of nodes below a given PayloadTreeNode.
     * 
     * @param node The node where we start counting
     * @return The number of nodes below a given PayloadTreeNode
     */
    public int recursiveGetNumberOfNodes(PayloadTreeNode node) {
        int numberOfNodes = node.getNumberOfChildren();

        for (PayloadTreeNode child : node.getChildren()) {
            numberOfNodes += recursiveGetNumberOfNodes(child);
        }

        return numberOfNodes;
    }

    /**
     * Get the namespaces of all PayloadElements found in this PayloadTree.
     * 
     * @return An empty Collection if the PayloadElements don't contain namespaces or the namespaces of of the PayloadElements associated
     *         with this tree
     */
    public Collection<String> getNamespaces() {
        if (root != null) {
            return recursivelyGetNamespaces(root);
        }

        return Collections.emptySet();
    }

    /**
     * Recursively get the namespaces of PayloadElements contained in PaloadTreeNodes below a given node.
     * 
     * @param node The PayloadTreeNode where the search should start, must not be null.
     * @return An empty Collection if the PayloadElements don't contain namespaces or the namespaces of the PayloadElements below the given
     *         node
     * @throws IllegalArgumentException If obligatory parameter is missing.
     */
    public Collection<String> recursivelyGetNamespaces(PayloadTreeNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Obligatory parameter node missing.");
        }
        
        Set<String> namespaces = new HashSet<String>();

        namespaces.add(node.getNamespace());
        for (PayloadTreeNode child : node.getChildren()) {
            namespaces.addAll(recursivelyGetNamespaces(child));
        }

        return namespaces;
    }

    /**
     * Search PayloadTreeNodes with PayloadElements matching a given ElementPath identifying the PayloadElements in this PayloadTree.
     * 
     * @param elementPath ElementPath identifying the PayloadElements
     * @return An empty Collection if no node with matching payloads can be found, a Collection containing the PayloadTreeNodes otherwise.
     * @throws IllegalArgumentException If obligatory parameter is missing.
     */
    public Collection<PayloadTreeNode> search(ElementPath elementPath) {
        if (elementPath == null) {
            throw new IllegalArgumentException("Obligatory parameter elementPath missing.");
        }
        if (root != null) {
            return recursiveSearch(root, elementPath);
        }

        return Collections.emptyList();

    }

    /**
     * Recursively find PayloadTreeNodes with PayloadElements matching a given ElementPath identifying the PayloadElements.
     * 
     * @param node The PayloadTreeNode where the recursive find starts. The node is inclued in the search, must not be null
     * @param elementPath ElementPath identifying the PayloadElements, must not be null
     * @return An empty Collection if no node with matching payloads can be found, a Collection containing the PayloadTreeNodes otherwise.
     * @throws IllegalArgumentException If obligatory parameter is missing.
     */
    private Collection<PayloadTreeNode> recursiveSearch(PayloadTreeNode node, ElementPath elementPath) {
        if (node == null || elementPath == null) {
            throw new IllegalArgumentException("Obligatory parameter missing.");
        }
        
        List<PayloadTreeNode> matches = new ArrayList<PayloadTreeNode>();

        if (elementPath.equals(node.getElementPath())) {
            matches.add(node);
        }
        for (PayloadTreeNode child : node.getChildren()) {
            matches.addAll(recursiveSearch(child, elementPath));
        }

        return matches;
    }
    
    @Override
    public String toString() {
        String stringRepresentation = PayloadTree.class.getSimpleName() + "@" + hashCode();

        if(root != null) {
            String treeRep = root.recursiveToString(0);
            stringRepresentation += "\n" + "|" + "\n";
            stringRepresentation += treeRep;
        }

        return stringRepresentation;
    }

}
