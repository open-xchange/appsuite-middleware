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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;

import static com.openexchange.eav.TransformList.*;

/**
 * {@link EAVUnitTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EAVUnitTest extends TestCase {

    // Array Transformations
    
    public EAVSetTransformation TRANS(String name, EAVSetTransformation...children) {
        EAVSetTransformation transformation = new EAVSetTransformation(name);
        transformation.addChildren(children);
        return transformation;
    }
    
    public EAVSetTransformation TRANS(String name,  TransformList list) {
        return TRANS(name, list, null);
    }
    
    public EAVSetTransformation TRANS(String name,  TransformList list1, TransformList list2) {
        EAVSetTransformation transformation = new EAVSetTransformation(name);
        transformation.setType(list1.type);
        for(TransformList transformList : Arrays.asList(list1, list2)) {
            if(transformList == null) {
                continue;
            }
            switch(transformList.operation) {
            case ADD : transformation.setAdd(transformList.payload); break;
            case REMOVE : transformation.setRemove(transformList.payload); break;
            }
        }
        return transformation;
    }
    
    // Objects

    public EAVNode N(String name, EAVNode... children) {
        EAVNode node = new EAVNode(name);
        node.addChildren(children);
        return node;
    }

    public EAVNode EMPTY_OBJECT(String name) {
        EAVNode node = new EAVNode(name);
        return node;
    }

    public EAVNode NULL(String name) {
        return EAVNode.nullNode(name);
    }

    // Strings
    public EAVNode N(String name, String... values) {
        EAVNode node = new EAVNode(name);
        if (values.length == 1) {
            node.setPayload(values[0]);
        } else {
            node.setPayload(values);
        }
        return node;
    }

    public EAVNode MULTISET(String name, String... values) {
        EAVNode node = new EAVNode(name);
        node.setPayload(values);
        return node;
    }

    public EAVNode SET(String name, String... values) {
        EAVNode node = new EAVNode(name);
        node.setPayload(EAVContainerType.SET, values);
        return node;
    }

    public TransformList ADD(String... strings) {
        return new TransformList(EAVType.STRING, ADD, strings);
    }

    public TransformList REMOVE(String... strings) {
        return new TransformList(EAVType.STRING, REMOVE, strings);
    }

    // Booleans

    public EAVNode N(String name, Boolean... values) {
        EAVNode node = new EAVNode(name);
        if (values.length == 1) {
            node.setPayload(values[0]);
        } else {
            node.setPayload(values);
        }
        return node;
    }

    public EAVNode MULTISET(String name, Boolean... values) {
        EAVNode node = new EAVNode(name);
        node.setPayload(values);
        return node;
    }

    public EAVNode SET(String name, Boolean... values) {
        EAVNode node = new EAVNode(name);
        node.setPayload(EAVContainerType.SET, values);
        return node;
    }
    
    public TransformList ADD(Boolean... values) {
        return new TransformList(EAVType.BOOLEAN, ADD, values);
    }

    public TransformList REMOVE(Boolean...values) {
        return new TransformList(EAVType.BOOLEAN, REMOVE, values);
    }


    // Numbers

    public EAVNode N(String name, Number... values) {
        EAVNode node = new EAVNode(name);
        if (values.length == 1) {
            node.setPayload(values[0]);
        } else {
            node.setPayload(values);
        }
        return node;
    }

    public EAVNode MULTISET(String name, Number... values) {
        EAVNode node = new EAVNode(name);
        node.setPayload(values);
        return node;
    }

    public EAVNode SET(String name, Number... values) {
        EAVNode node = new EAVNode(name);
        node.setPayload(EAVType.NUMBER, EAVContainerType.SET, values);
        return node;
    }
    
    public TransformList ADD(Number... values) {
        return new TransformList(EAVType.NUMBER, ADD, values);
    }

    public TransformList REMOVE(Number...values) {
        return new TransformList(EAVType.NUMBER, REMOVE, values);
    }


    // Other Numbers (Date and Time)

    private static final Set<EAVType> ACCEPTABLE_NUMBER_TYPES = EnumSet.of(EAVType.DATE, EAVType.TIME, EAVType.NUMBER);

    public EAVNode N(String name, EAVType type, Number... values) {
        if (!ACCEPTABLE_NUMBER_TYPES.contains(type)) {
            throw new IllegalArgumentException("Numbers can not be of type " + type.name());
        }
        EAVNode node = new EAVNode(name);
        if (values.length == 1) {
            node.setPayload(values[0]);
        } else {
            node.setPayload(values);
        }
        return node;
    }

    public EAVNode MULTISET(String name, EAVType type, Number... values) {
        if (!ACCEPTABLE_NUMBER_TYPES.contains(type)) {
            throw new IllegalArgumentException("Numbers can not be of type " + type.name());
        }
        EAVNode node = new EAVNode(name);
        node.setPayload(type, values);
        return node;
    }

    public EAVNode SET(String name, EAVType type, Number... values) {
        if (!ACCEPTABLE_NUMBER_TYPES.contains(type)) {
            throw new IllegalArgumentException("Numbers can not be of type " + type.name());
        }
        EAVNode node = new EAVNode(name);
        node.setPayload(type, EAVContainerType.SET, values);
        return node;
    }
    
    public TransformList ADD(EAVType type, Number... values) {
        if (!ACCEPTABLE_NUMBER_TYPES.contains(type)) {
            throw new IllegalArgumentException("Numbers can not be of type " + type.name());
        }
        return new TransformList(type, ADD, values);
    }

    public TransformList REMOVE(EAVType type, Number...values) {
        if (!ACCEPTABLE_NUMBER_TYPES.contains(type)) {
            throw new IllegalArgumentException("Numbers can not be of type " + type.name());
        }
        return new TransformList(type, REMOVE, values);
    }


    public static void assertEquals(EAVNode expected, EAVNode actual) {
        assertEquals("", expected, actual);
    }

    public static void assertEquals(String message, EAVNode expected, EAVNode actual) {
        if (expected == actual) {
            return;
        }
        if (expected == null) {
            fail(message + ": expected was null, actual was: " + treeString(actual));
        }
        if (actual == null) {
            fail(message + ": " + treeString(expected) + " expected, but was null");
        }

        List<EAVNode> serializedExpected = serialize(expected);
        List<EAVNode> serializedActual = serialize(actual);

        if (serializedExpected.size() != serializedActual.size()) {
            failComparison(message, expected, actual);
        }

        for (int i = 0, size = serializedExpected.size(); i < size; i++) {
            EAVNode expectedNode = serializedExpected.get(i);
            EAVNode actualNode = serializedActual.get(i);

            if (!expectedNode.getRelativePath(expected).equals(actualNode.getRelativePath(actual))) {
                failComparison(message, expected, actual);
            }

            if (!expectedNode.getType().equals(actualNode.getType())) {
                failComparison(message, expected, actual);
            }
            if (expectedNode.isMultiple() != actualNode.isMultiple()) {
                failComparison(message, expected, actual);
            }
            EAVTypeSwitcher compare = null;
            if (expectedNode.isMultiple()) {
                compare = new EAVMultipleCompare();
            } else {
                compare = new EAVPayloadCompare();
            }
            boolean equalPayloads = (Boolean) expectedNode.getType().doSwitch(compare, expectedNode.getPayload(), actualNode.getPayload());
            if (!equalPayloads) {
                failComparison(message, expected, actual);
            }
        }

    }

    private static List<EAVNode> serialize(EAVNode node) {
        final List<EAVNode> collected = new ArrayList<EAVNode>();
        node.visit(new EAVNodeVisitor() {

            public void visit(int index, EAVNode node) {
                collected.add(node);
            }
        });
        return collected;
    }

    private static void failComparison(String message, EAVNode expected, EAVNode actual) {
        assertEquals(message, treeString(expected), treeString(actual));
        fail(message);
    }

    private static String treeString(EAVNode node) {
        final StringBuilder builder = new StringBuilder("\n");
        final EAVValuePrettyPrint prettyPrinter = new EAVValuePrettyPrint();
        final EAVMultiplePrettyPrint prettyPrinterMultiple = new EAVMultiplePrettyPrint();

        node.visit(new EAVNodeVisitor() {

            public void visit(int index, EAVNode node) {
                for (int i = 0; i < index; i++) {
                    builder.append("    ");
                }
                builder.append(node.getName());
                String pretty = (String) node.getType().doSwitch(
                    node.isMultiple() ? prettyPrinterMultiple : prettyPrinter,
                    node.getPayload());
                if (pretty != null) {
                    builder.append(" : ").append(pretty);
                }
                builder.append('\n');
            }
        });

        return builder.toString();
    }

}
