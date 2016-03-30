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
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.util.ElementPath;


/**
 * {@link PayloadTreeNodeTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PayloadTreeNodeTest {

    private PayloadTreeNode parentPayloadTreeNode;
    private PayloadTreeNode payloadTreeNode;
    private PayloadTreeNode payloadTreeNode1;
    private PayloadTreeNode payloadTreeNode2;
    private PayloadTreeNode payloadTreeNode3;
    private PayloadElement payloadElement1;
    private PayloadElement payloadElement2;
    private PayloadElement payloadElement3;
    private List<PayloadTreeNode> emptyPayloadTreeNodeList;
    private List<PayloadTreeNode> filledPayloadTreeNodeList;

    @Before
    public void initPayloadElement() {
        parentPayloadTreeNode = new PayloadTreeNode();
        payloadTreeNode = new PayloadTreeNode();
        payloadElement1 = new PayloadElement(1, "Integer", null, "testElement");
        payloadElement2 = new PayloadElement(2, "Integer", null, "testElement");
        payloadElement3 = new PayloadElement(3, "Integer", null, "testElement");
        payloadTreeNode1 = new PayloadTreeNode(payloadElement1);
        payloadTreeNode2 = new PayloadTreeNode(payloadElement2);
        payloadTreeNode3 = new PayloadTreeNode(payloadElement3);
        emptyPayloadTreeNodeList = new ArrayList<PayloadTreeNode>();
        filledPayloadTreeNodeList = new ArrayList<PayloadTreeNode>();
        filledPayloadTreeNodeList.add(payloadTreeNode1);
        filledPayloadTreeNodeList.add(payloadTreeNode2);
        filledPayloadTreeNodeList.add(payloadTreeNode3);
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#PayloadTreeNode()}.
     */
    @Test
    public void testPayloadTreeNode() {
        assertNull(payloadTreeNode.getParent());
        assertNull(payloadTreeNode.getData());
        assertFalse(payloadTreeNode.hasChildren());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#PayloadTreeNode(com.openexchange.realtime.payload.PayloadElement)}.
     */
    @Test
    public void testPayloadTreeNodePayloadElement() {
        PayloadTreeNode node = new PayloadTreeNode(payloadElement1);
        assertNull(node.getParent());
        assertEquals(payloadElement1, node.getPayloadElement());
        assertEquals(payloadElement1.getData(), node.getData());
        assertFalse(node.hasChildren());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#getParent()}.
     */
    @Test
    public void testGetParent() {
        assertNull(payloadTreeNode.getParent());
        payloadTreeNode.setParent(parentPayloadTreeNode);
        assertEquals(parentPayloadTreeNode, payloadTreeNode.getParent());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#setParent(com.openexchange.realtime.payload.PayloadTreeNode)}.
     */
    @Test
    public void testSetParent() {
        assertNull(payloadTreeNode.getParent());
        payloadTreeNode.setParent(parentPayloadTreeNode);
        assertEquals(parentPayloadTreeNode, payloadTreeNode.getParent());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#getPayloadElement()}.
     */
    @Test
    public void testGetPayloadElement() {
        payloadTreeNode.setPayloadElement(payloadElement1);
        assertEquals(payloadElement1, payloadTreeNode.getPayloadElement());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#getChildren()}.
     */
    @Test
    public void testGetChildren() {
        assertEquals(emptyPayloadTreeNodeList, new ArrayList(parentPayloadTreeNode.getChildren()));
        parentPayloadTreeNode.addChild(payloadTreeNode1);
        parentPayloadTreeNode.addChild(payloadTreeNode2);
        parentPayloadTreeNode.addChild(payloadTreeNode3);
        assertEquals(filledPayloadTreeNodeList, new ArrayList(parentPayloadTreeNode.getChildren()));
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#getNumberOfChildren()}.
     */
    @Test
    public void testGetNumberOfChildren() {
        assertEquals(0, parentPayloadTreeNode.getChildren().size());
        parentPayloadTreeNode.addChildren(filledPayloadTreeNodeList);
        assertEquals(3, parentPayloadTreeNode.getChildren().size());
        parentPayloadTreeNode.removeChild(payloadTreeNode1);
        assertEquals(2, parentPayloadTreeNode.getChildren().size());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#hasChildren()}.
     */
    @Test
    public void testHasChildren() {
        assertFalse(parentPayloadTreeNode.hasChildren());
        parentPayloadTreeNode.addChildren(filledPayloadTreeNodeList);
        assertTrue(parentPayloadTreeNode.hasChildren());
        parentPayloadTreeNode.removeChildren(filledPayloadTreeNodeList);
        assertFalse(parentPayloadTreeNode.hasChildren());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#setChildren(java.util.Collection)}.
     */
    @Test
    public void testSetChildren() {
        assertFalse(parentPayloadTreeNode.hasChildren());
        parentPayloadTreeNode.setChildren(filledPayloadTreeNodeList);
        assertTrue(parentPayloadTreeNode.hasChildren());
        assertEquals(filledPayloadTreeNodeList, new ArrayList(parentPayloadTreeNode.getChildren()));
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#addChild(com.openexchange.realtime.payload.PayloadTreeNode)}.
     */
    @Test
    public void testAddChild() {
        assertFalse(parentPayloadTreeNode.hasChildren());
        parentPayloadTreeNode.addChild(payloadTreeNode1);
        parentPayloadTreeNode.addChild(payloadTreeNode2);
        parentPayloadTreeNode.addChild(payloadTreeNode3);
        assertTrue(parentPayloadTreeNode.hasChildren());
        assertEquals(filledPayloadTreeNodeList, new ArrayList(parentPayloadTreeNode.getChildren()));
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#addChildren(java.util.Collection)}.
     */
    @Test
    public void testAddChildren() {
        assertFalse(parentPayloadTreeNode.hasChildren());
        parentPayloadTreeNode.addChildren(emptyPayloadTreeNodeList);
        assertFalse(parentPayloadTreeNode.hasChildren());
        parentPayloadTreeNode.addChildren(filledPayloadTreeNodeList);
        assertTrue(parentPayloadTreeNode.hasChildren());
        assertEquals(filledPayloadTreeNodeList, new ArrayList(parentPayloadTreeNode.getChildren()));
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#removeChild(com.openexchange.realtime.payload.PayloadTreeNode)}.
     */
    @Test
    public void testRemoveChild() {
        assertFalse(parentPayloadTreeNode.hasChildren());
        parentPayloadTreeNode.addChild(payloadTreeNode1);
        assertTrue(parentPayloadTreeNode.hasChildren());
        assertEquals(1,parentPayloadTreeNode.getNumberOfChildren());
        assertEquals(payloadTreeNode1,parentPayloadTreeNode.getChildren().toArray()[0]);
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#removeChildren(java.util.Collection)}.
     */
    @Test
    public void testRemoveChildren() {
        assertFalse(parentPayloadTreeNode.hasChildren());
        parentPayloadTreeNode.addChildren(filledPayloadTreeNodeList);
        assertTrue(parentPayloadTreeNode.hasChildren());
        emptyPayloadTreeNodeList.add(payloadTreeNode1);
        emptyPayloadTreeNodeList.add(payloadTreeNode2);
        parentPayloadTreeNode.removeChildren(emptyPayloadTreeNodeList);
        assertEquals(1, parentPayloadTreeNode.getNumberOfChildren());
        assertEquals(payloadTreeNode3, parentPayloadTreeNode.getChildren().toArray()[0]);
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#getData()}.
     */
    @Test
    public void testGetData() {
        assertEquals(payloadElement1.getData(), payloadTreeNode1.getData());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#setData(java.lang.Object, java.lang.String)}.
     */
    @Test
    public void testSetData() {
        payloadTreeNode1.setData(2, "Integer");
        assertEquals(2, payloadTreeNode1.getData());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#getElementName()}.
     */
    @Test
    public void testGetElementName() {
        assertEquals(payloadElement1.getElementName(), payloadTreeNode1.getElementName());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#getFormat()}.
     */
    @Test
    public void testGetFormat() {
        assertEquals(payloadElement1.getFormat(), payloadTreeNode1.getFormat());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#getElementPath()}.
     */
    @Test
    public void testGetElementPath() {
        assertEquals(new ElementPath(payloadElement1.getNamespace(), payloadElement1.getElementName()), payloadTreeNode1.getElementPath());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#getNamespace()}.
     */
    @Test
    public void testGetNamespace() {
        assertEquals(payloadElement1.getNamespace(), payloadTreeNode1.getNamespace());
    }

    /**
     * Test method for {@link com.openexchange.realtime.payload.PayloadTreeNode#equals(java.lang.Object)}.
     */
    @Test
    public void testEqualsObject() {
        assertEquals(new PayloadTreeNode(payloadElement1), new PayloadTreeNode(payloadElement1));
        assertEquals(new PayloadTreeNode(payloadElement1).addChild(payloadTreeNode2), new PayloadTreeNode(payloadElement1).addChild(payloadTreeNode2));
    }

}
