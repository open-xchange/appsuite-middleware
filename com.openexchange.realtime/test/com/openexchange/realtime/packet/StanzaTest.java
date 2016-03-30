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

package com.openexchange.realtime.packet;

import static com.openexchange.realtime.payload.PayloadTreeNode.builder;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import java.util.TreeSet;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.ElementPath;


/**
 * {@link StanzaTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class StanzaTest {

    private static Stanza stanza;
    
    @BeforeClass
    public static void setUp() throws Exception {
        stanza = new Message();

        PayloadTree treeA = new PayloadTree(
            PayloadTreeNode.builder()
            .withoutPayload("", "treeA")
            .andChild(builder()
                .withPayload(2, "Integer", "com.openexchange.realtime.testNameSpaceA", "testElement2")
                .andChild(3, "Integer", "com.openexchange.realtime.testNameSpaceA", "testElement3")
                .build())
            .andChild(builder()
                .withPayload(4, "Integer", "com.openexchange.realtime.testNameSpaceA", "testElement4")
                .andChild(5, "Integer", "com.openexchange.realtime.testNameSpaceA", "testElement5")
                .andChild(6, "Integer", "com.openexchange.realtime.testNameSpaceA", "testElement6")
                .build())
            .build()
            );
        stanza.addPayload(treeA);
        
        PayloadTree treeB = new PayloadTree(
            PayloadTreeNode.builder()
            .withoutPayload("", "treeB")
            .andChild(builder()
                .withPayload(2, "Integer", "com.openexchange.realtime.testNameSpaceB", "testElement2")
                .andChild(3, "Integer", "com.openexchange.realtime.testNameSpaceB", "testElement3")
                .build())
            .andChild(builder()
                .withPayload(4, "Integer", "com.openexchange.realtime.testNameSpaceB", "testElement4")
                .andChild(5, "Integer", "com.openexchange.realtime.testNameSpaceB", "testElement5")
                .andChild(6, "Integer", "com.openexchange.realtime.testNameSpaceB", "testElement6")
                .build())
            .build()
            );
        stanza.addPayload(treeB);

        PayloadTree treeC = new PayloadTree(
            PayloadTreeNode.builder()
            .withoutPayload("", "treeC")
            .andChild(builder()
                .withPayload(2, "Integer", "com.openexchange.realtime.testNameSpaceC", "testElement2")
                .andChild(3, "Integer", "com.openexchange.realtime.testNameSpaceC", "testElement3")
                .build())
            .andChild(builder()
                .withPayload(4, "Integer", "com.openexchange.realtime.testNameSpaceC", "testElement4")
                .andChild(5, "Integer", "com.openexchange.realtime.testNameSpaceC", "testElement5")
                .andChild(6, "Integer", "com.openexchange.realtime.testNameSpaceC", "testElement6")
                .build())
            .build()
            );
        stanza.addPayload(treeC);
    }

    @Test
    public void testFilterPayloadElementsFromAllTrees() {
        Collection<PayloadElement> payloadElements = stanza.filterPayloadElements(
              new ElementPath("com.openexchange.realtime.testNameSpaceC", "testElement6")
            , new ElementPath("com.openexchange.realtime.testNameSpaceA", "testElement2")
            , new ElementPath("com.openexchange.realtime.testNameSpaceB", "testElement2")
            , new ElementPath("com.openexchange.realtime.testNameSpaceB", "testElement4")
            );
        TreeSet<PayloadElement> results = new TreeSet<PayloadElement>(payloadElements);
        assertTrue(results.size() == 4);
        PayloadElement currentElement = null;
        currentElement = results.pollFirst();
        assertTrue(currentElement.getNamespace().equals("com.openexchange.realtime.testNameSpaceA") && currentElement.getElementName().equals("testElement2"));
        currentElement = results.pollFirst();
        assertTrue(currentElement.getNamespace().equals("com.openexchange.realtime.testNameSpaceB") && currentElement.getElementName().equals("testElement2"));
        currentElement = results.pollFirst();
        assertTrue(currentElement.getNamespace().equals("com.openexchange.realtime.testNameSpaceB") && currentElement.getElementName().equals("testElement4"));
        currentElement = results.pollFirst();
        assertTrue(currentElement.getNamespace().equals("com.openexchange.realtime.testNameSpaceC") && currentElement.getElementName().equals("testElement6"));
    }

    @Test
    public void testFilterPayloadElementsFromSpecificTree() {
        //Filter treeA
        TreeSet<PayloadElement> results = new TreeSet<PayloadElement> (stanza.filterPayloadElementsFromTree(new ElementPath("treeA")
          , new ElementPath("com.openexchange.realtime.testNameSpaceC", "testElement6")
          , new ElementPath("com.openexchange.realtime.testNameSpaceA", "testElement2")
          , new ElementPath("com.openexchange.realtime.testNameSpaceB", "testElement2")
          , new ElementPath("com.openexchange.realtime.testNameSpaceB", "testElement4")
          ));
        assertTrue(results.size() == 1);
        PayloadElement currentElement = null;
        currentElement = results.pollFirst();
        assertTrue(currentElement.getNamespace().equals("com.openexchange.realtime.testNameSpaceA") && currentElement.getElementName().equals("testElement2"));

        //Filter treeB
        results = new TreeSet<PayloadElement> (stanza.filterPayloadElementsFromTree(new ElementPath("treeB")
              , new ElementPath("com.openexchange.realtime.testNameSpaceC", "testElement6")
              , new ElementPath("com.openexchange.realtime.testNameSpaceA", "testElement2")
              , new ElementPath("com.openexchange.realtime.testNameSpaceB", "testElement2")
              , new ElementPath("com.openexchange.realtime.testNameSpaceB", "testElement4")
        ));
        assertTrue(results.size() == 2);
        currentElement = results.pollFirst();
        assertTrue(currentElement.getNamespace().equals("com.openexchange.realtime.testNameSpaceB") && currentElement.getElementName().equals("testElement2"));
        currentElement = results.pollFirst();
        assertTrue(currentElement.getNamespace().equals("com.openexchange.realtime.testNameSpaceB") && currentElement.getElementName().equals("testElement4"));

        //Filter treeC
        results = new TreeSet<PayloadElement> (stanza.filterPayloadElementsFromTree(new ElementPath("treeC")
            , new ElementPath("com.openexchange.realtime.testNameSpaceC", "testElement6")
            , new ElementPath("com.openexchange.realtime.testNameSpaceA", "testElement2")
            , new ElementPath("com.openexchange.realtime.testNameSpaceB", "testElement2")
            , new ElementPath("com.openexchange.realtime.testNameSpaceB", "testElement4")
        ));
        assertTrue(results.size() == 1);
        currentElement = results.pollFirst();
        assertTrue(currentElement.getNamespace().equals("com.openexchange.realtime.testNameSpaceC") && currentElement.getElementName().equals("testElement6"));
    }

}
