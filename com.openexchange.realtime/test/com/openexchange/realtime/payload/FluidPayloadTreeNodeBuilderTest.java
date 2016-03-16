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

//static PayloadTreeNode.builder() method lets you shorten invocation via static import
import static com.openexchange.realtime.payload.PayloadTreeNode.builder;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * {@link FluidPayloadTreeNodeBuilderTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class FluidPayloadTreeNodeBuilderTest {

    /*
     * Build a single PayloadTreeNode. Can be created by simply giving the params needed for the PayloadElement constructor.
     * A PayloadTreeNode with an internal PayloadElement based on those params will then be created.
     *
     *            1
     */
    @Test
    public void testSinglePayloadTreeNode() {
        PayloadTreeNode builderNode = builder()
            .withPayload(1, "Integer", "testNameSpace", "testElement")
            .build();

        PayloadTreeNode handCraftedNode = new PayloadTreeNode(new PayloadElement(1, "Integer", "testNameSpace", "testElement"));
        assertEquals("builder- and handCraftedNode should be equal", builderNode, handCraftedNode);
    }


    /*
     * First level children can be added by simply giving the params needed for the PayloadElement constructor. A PayloadTreeNode with an
     * internal PayloadElement based on those params will then be created.
     *
     *            1
     *           /
     *          2
     */
    @Test
    public void testPayloadTreeNodeWithFirstLevelChild() {
        PayloadTreeNode builderNode = builder()
            .withPayload(1, "Integer", "testNameSpace", "testElement")
            .andChild(2, "Integer", "testNameSpace", "testElement")
            .build();

        PayloadTreeNode handCraftedNode = new PayloadTreeNode(new PayloadElement(1, "Integer", "testNameSpace", "testElement"));
        handCraftedNode.addChild(new PayloadTreeNode(new PayloadElement(2, "Integer", "testNameSpace", "testElement")));

        assertEquals("Builder- and handCraftedNode should be equal", builderNode, handCraftedNode);
    }

    /*
     * First level children can be added by simply giving the params needed for the PayloadElement constructor. A PayloadTreeNode with an
     * internal PayloadElement based on those params will then be created.
     *
     *            1
     *           /\
     *          2  3
     */
    @Test
    public void testPayloadTreeNodeWithFirstLevelChildren() {
        PayloadTreeNode builderNode = builder()
            .withPayload(new PayloadElement(1, "Integer", "testNameSpace", "testElement"))
            .andChild(new PayloadElement(2, "Integer", "testNameSpace", "testElement"))
            .andChild(new PayloadElement(3, "Integer", "testNameSpace", "testElement"))
            .build();

        PayloadTreeNode handCraftedNode = new PayloadTreeNode(new PayloadElement(1, "Integer", "testNameSpace", "testElement"));
        handCraftedNode.addChild(new PayloadTreeNode(new PayloadElement(2, "Integer", "testNameSpace", "testElement")));
        handCraftedNode.addChild(new PayloadTreeNode(new PayloadElement(3, "Integer", "testNameSpace", "testElement")));

        assertEquals("Builder- and handCraftedNode should be equal", builderNode, handCraftedNode);
    }


    /*
     * Multi level children can be added by combining withChild() and builder()
     *
     *            1
     *           /\
     *          2  4
     *         /   /\
     *        3   5  6
     */
    @Test
    public void testPayloadTreeNodeWithMultiLevelChildren() {
        PayloadTreeNode builderNode = builder()
            .withPayload(1, "Integer", "testNameSpace", "testElement")
            .andChild(builder()
                .withPayload(2, "Integer", "testNameSpace", "testElement")
                .andChild(3, "Integer", "testNameSpace", "testElement")
                .build())
            .andChild(builder()
                .withPayload(4, "Integer", "testNameSpace", "testElement")
                .andChild(5, "Integer", "testNameSpace", "testElement")
                .andChild(6, "Integer", "testNameSpace", "testElement")
                .build())
            .build();

        PayloadTreeNode handCraftedNode = new PayloadTreeNode(new PayloadElement(1, "Integer", "testNameSpace", "testElement"));

        PayloadTreeNode payloadTreeNode2 = new PayloadTreeNode(new PayloadElement(2, "Integer", "testNameSpace", "testElement"));
        PayloadTreeNode payloadTreeNode3 = new PayloadTreeNode(new PayloadElement(3, "Integer", "testNameSpace", "testElement"));

        payloadTreeNode2.addChild(payloadTreeNode3);
        handCraftedNode.addChild(payloadTreeNode2);

        PayloadTreeNode payloadTreeNode4 = new PayloadTreeNode(new PayloadElement(4, "Integer", "testNameSpace", "testElement"));
        PayloadTreeNode payloadTreeNode5 = new PayloadTreeNode(new PayloadElement(5, "Integer", "testNameSpace", "testElement"));
        PayloadTreeNode payloadTreeNode6 = new PayloadTreeNode(new PayloadElement(6, "Integer", "testNameSpace", "testElement"));

        payloadTreeNode4.addChild(payloadTreeNode5);
        payloadTreeNode4.addChild(payloadTreeNode6);
        handCraftedNode.addChild(payloadTreeNode4);

        assertEquals("Builder- and handCraftedNode should be equal", builderNode, handCraftedNode);
    }

    /*
     * Build a PayloadTree with the help of the fluent builder.
     */
    @Test
    public void testPayloadTree() {
        PayloadTree builderTree = new PayloadTree(
            PayloadTreeNode.builder()
            .withPayload(1, "Integer", "testNameSpace", "testElement")
            .andChild(builder()
                .withPayload(2, "Integer", "testNameSpace", "testElement")
                .andChild(3, "Integer", "testNameSpace", "testElement")
                .build())
            .andChild(builder()
                .withPayload(4, "Integer", "testNameSpace", "testElement")
                .andChild(5, "Integer", "testNameSpace", "testElement")
                .andChild(6, "Integer", "testNameSpace", "testElement")
                .build())
            .build()
            );

        assertEquals("Tree should hold 6 Elements", 6, builderTree.getNumberOfNodes());

        PayloadTreeNode handCraftedRootNode = new PayloadTreeNode(new PayloadElement(1, "Integer", "testNameSpace", "testElement"));

        PayloadTreeNode payloadTreeNode2 = new PayloadTreeNode(new PayloadElement(2, "Integer", "testNameSpace", "testElement"));
        PayloadTreeNode payloadTreeNode3 = new PayloadTreeNode(new PayloadElement(3, "Integer", "testNameSpace", "testElement"));
        payloadTreeNode2.addChild(payloadTreeNode3);
        handCraftedRootNode.addChild(payloadTreeNode2);

        PayloadTreeNode payloadTreeNode4 = new PayloadTreeNode(new PayloadElement(4, "Integer", "testNameSpace", "testElement"));
        PayloadTreeNode payloadTreeNode5 = new PayloadTreeNode(new PayloadElement(5, "Integer", "testNameSpace", "testElement"));
        PayloadTreeNode payloadTreeNode6 = new PayloadTreeNode(new PayloadElement(6, "Integer", "testNameSpace", "testElement"));
        payloadTreeNode4.addChild(payloadTreeNode5);
        payloadTreeNode4.addChild(payloadTreeNode6);
        handCraftedRootNode.addChild(payloadTreeNode4);

        PayloadTree handCraftedTree = new PayloadTree(handCraftedRootNode);

        assertEquals("BuilderTree and handcraftedTree should be equal",builderTree, handCraftedTree);
    }

}
