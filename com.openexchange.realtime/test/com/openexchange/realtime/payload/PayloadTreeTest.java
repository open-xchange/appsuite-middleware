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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link PayloadTreeTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
/**
 * {@link PayloadTreeTest}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PayloadTreeTest {

    private PayloadTreeNode payloadTreeNode1;

    private PayloadTreeNode payloadTreeNode2;

    private PayloadTreeNode payloadTreeNode3;

    private PayloadElement payloadElement1;

    private PayloadElement payloadElement2;

    private PayloadElement payloadElement3;

    private List<PayloadTreeNode> emptyPayloadTreeNodeList;

    private List<PayloadTreeNode> filledPayloadTreeNodeList;

    private PayloadTree emptyTree;

    private PayloadTree treeWithRootNode;

    private PayloadElement filledTreePayloadElement1;

    private PayloadElement filledTreePayloadElement2;

    private PayloadElement filledTreePayloadElement3;

    private PayloadElement filledTreePayloadElement4;

    private PayloadElement filledTreePayloadElement5;

    private PayloadElement filledTreePayloadElement6;

    private PayloadTreeNode filledTreePayloadTreeNode1;

    private PayloadTreeNode filledTreePayloadTreeNode2;

    private PayloadTreeNode filledTreePayloadTreeNode3;

    private PayloadTreeNode filledTreePayloadTreeNode4;

    private PayloadTreeNode filledTreePayloadTreeNode5;

    private PayloadTreeNode filledTreePayloadTreeNode6;

    private PayloadTree filledTree;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        payloadElement1 = new PayloadElement(1, "Integer", "namespace1", "testElement1");
        payloadElement2 = new PayloadElement(2, "Integer", "namespace2", "testElement2");
        payloadElement3 = new PayloadElement(3, "Integer", "namespace3", "testElement3");

        payloadTreeNode1 = new PayloadTreeNode(payloadElement1);
        payloadTreeNode2 = new PayloadTreeNode(payloadElement2);
        payloadTreeNode3 = new PayloadTreeNode(payloadElement3);

        emptyPayloadTreeNodeList = new ArrayList<PayloadTreeNode>();
        filledPayloadTreeNodeList = new ArrayList<PayloadTreeNode>();
        filledPayloadTreeNodeList.add(payloadTreeNode1);
        filledPayloadTreeNodeList.add(payloadTreeNode2);
        filledPayloadTreeNodeList.add(payloadTreeNode3);

        filledTreePayloadElement1 = new PayloadElement(1, "Integer", "namespace1", "testElement1");
        filledTreePayloadElement2 = new PayloadElement(2, "Integer", "namespace2", "testElement2");
        filledTreePayloadElement3 = new PayloadElement(3, "Integer", "namespace3", "testElement3");
        filledTreePayloadElement4 = new PayloadElement(4, "Integer", "namespace4", "testElement4");
        filledTreePayloadElement5 = new PayloadElement(5, "Integer", "namespace5", "testElement5");
        filledTreePayloadElement6 = new PayloadElement(6, "Integer", "namespace6", "testElement6");

        filledTreePayloadTreeNode1 = new PayloadTreeNode(filledTreePayloadElement1);
        filledTreePayloadTreeNode2 = new PayloadTreeNode(filledTreePayloadElement2);
        filledTreePayloadTreeNode3 = new PayloadTreeNode(filledTreePayloadElement3);
        filledTreePayloadTreeNode4 = new PayloadTreeNode(filledTreePayloadElement4);
        filledTreePayloadTreeNode5 = new PayloadTreeNode(filledTreePayloadElement5);
        filledTreePayloadTreeNode6 = new PayloadTreeNode(filledTreePayloadElement6);

        emptyTree = new PayloadTree();
        treeWithRootNode = new PayloadTree(payloadTreeNode1);

        /*
         * tree
         * |
         * 1
         * /\
         * 2 4
         * / /\
         * 3 5 6
         */
        filledTree = new PayloadTree(filledTreePayloadTreeNode1);

        filledTreePayloadTreeNode2.addChild(filledTreePayloadTreeNode3);
        filledTree.getRoot().addChild(filledTreePayloadTreeNode2);

        filledTreePayloadTreeNode4.addChild(filledTreePayloadTreeNode5);
        filledTreePayloadTreeNode4.addChild(filledTreePayloadTreeNode6);
        filledTree.getRoot().addChild(filledTreePayloadTreeNode4);
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTree#PayloadTree()}.
     */
    @Test
    public void testPayloadTree() {
        assertEquals(0, emptyTree.getNumberOfNodes());
        assertNull(emptyTree.getRoot());
        assertTrue(emptyTree.isEmpty());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTree#PayloadTree(com.openexchange.realtime.payload.PayloadTreeNode)}.
     */
    @Test
    public void testPayloadTreePayloadTreeNode() {
        PayloadTree payloadTree = new PayloadTree(payloadTreeNode1);
        assertFalse(payloadTree.isEmpty());
        assertEquals(payloadTreeNode1, payloadTree.getRoot());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTree#getRoot()}.
     */
    @Test
    public void testGetRoot() {
        assertEquals(payloadTreeNode1, treeWithRootNode.getRoot());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTree#setRoot(com.openexchange.realtime.payload.PayloadTreeNode)}.
     */
    @Test
    public void testSetRoot() {
        emptyTree.setRoot(payloadTreeNode2);
        assertEquals(payloadTreeNode2, emptyTree.getRoot());
        treeWithRootNode.setRoot(payloadTreeNode3);
        assertEquals(payloadTreeNode3, treeWithRootNode.getRoot());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTree#isEmpty()}.
     */
    @Test
    public void testIsEmpty() {
        assertTrue(emptyTree.isEmpty());
        assertFalse(treeWithRootNode.isEmpty());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTree#getNumberOfNodes()}.
     */
    @Test
    public void testGetNumberOfNodes() {
        treeWithRootNode.getRoot().addChild(payloadTreeNode2);
        treeWithRootNode.getRoot().addChild(payloadTreeNode3);
        assertEquals(3, treeWithRootNode.getNumberOfNodes());
    }

    /**
     * Test method for
     * {@link com.openexchange.realtime.payload.PayloadTree#recursiveGetNumberOfNodes(com.openexchange.realtime.payload.PayloadTreeNode)}.
     */
    @Test
    public void testRecursiveGetNumberOfNodes() {
        assertEquals(5, treeWithRootNode.recursiveGetNumberOfNodes(filledTreePayloadTreeNode1));
        assertEquals(1, treeWithRootNode.recursiveGetNumberOfNodes(filledTreePayloadTreeNode2));
        assertEquals(2, treeWithRootNode.recursiveGetNumberOfNodes(filledTreePayloadTreeNode4));
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTree#getNamespaces()}.
     */
    @Test
    public void testGetElementPaths() {
        Set<ElementPath> expectedElementPaths = new HashSet<ElementPath>();
        expectedElementPaths.add(filledTreePayloadTreeNode1.getElementPath());
        expectedElementPaths.add(filledTreePayloadTreeNode2.getElementPath());
        expectedElementPaths.add(filledTreePayloadTreeNode3.getElementPath());
        expectedElementPaths.add(filledTreePayloadTreeNode4.getElementPath());
        expectedElementPaths.add(filledTreePayloadTreeNode5.getElementPath());
        expectedElementPaths.add(filledTreePayloadTreeNode6.getElementPath());
        Set<ElementPath> retrievedElementPaths = new HashSet<ElementPath>(filledTree.getElementPaths());
        assertEquals(expectedElementPaths, retrievedElementPaths);
    }

    /**
     * Test method for
     * {@link com.openexchange.realtime.payload.PayloadTree#recursivelyGetNamespaces(com.openexchange.realtime.payload.PayloadTreeNode)}.
     */
    @Test
    public void testRecursivelyGetElementPaths() {
        Set<ElementPath> expectedElementpaths = new HashSet<ElementPath>();
        expectedElementpaths.add(filledTreePayloadTreeNode2.getElementPath());
        expectedElementpaths.add(filledTreePayloadTreeNode3.getElementPath());
        Set<ElementPath> retrievedNamespaces = new HashSet<ElementPath>(filledTree.recursivelyGetElementPaths(filledTreePayloadTreeNode2));
        assertEquals(expectedElementpaths, retrievedNamespaces);
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTree#search(com.openexchange.realtime.util.ElementPath)}.
     */
    @Test
    public void testSearch() {
        ElementPath elementPath = filledTreePayloadTreeNode3.getElementPath();
        Collection<PayloadTreeNode> results = filledTree.search(elementPath);
        assertEquals(1, results.size());
        assertEquals(filledTreePayloadTreeNode3, results.toArray()[0]);
    }

    /**
     * Test method for {@link java.lang.Object#toString()}.
     */
    @Test
    public void testToString() {
        System.out.println(filledTree);
    }

    @Test
    public void testToStringRecursive() {
        System.out.println(filledTreePayloadTreeNode4.recursiveToString(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPayloadCreateWithNull() {
        PayloadTreeNode nullElement = null;
        new PayloadTreeNode(nullElement);
    }

    @Test
    public void testPayloadCreateWithoutElement() {
        PayloadTreeNode nullElement = new PayloadTreeNode();
        new PayloadTreeNode(nullElement);
    }
}
