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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public EAVSetTransformation TRANS(String name, EAVSetTransformation... children) {
        EAVSetTransformation transformation = new EAVSetTransformation(name);
        transformation.addChildren(children);
        return transformation;
    }

    public EAVSetTransformation TRANS(String name, TransformList list) {
        return TRANS(name, list, null);
    }

    public EAVSetTransformation TRANS(String name, TransformList list1, TransformList list2) {
        EAVSetTransformation transformation = new EAVSetTransformation(name);
        transformation.setType(list1.type);
        for (TransformList transformList : Arrays.asList(list1, list2)) {
            if (transformList == null) {
                continue;
            }
            switch (transformList.operation) {
            case ADD:
                transformation.setAdd(transformList.payload);
                break;
            case REMOVE:
                transformation.setRemove(transformList.payload);
                break;
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

    public TransformList REMOVE(Boolean... values) {
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

    public TransformList REMOVE(Number... values) {
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
            node.setPayload(type, values[0]);
        } else {
            node.setPayload(type, values);
        }
        return node;
    }

    // Binaries

    public EAVNode N(String name, byte[]... values) {
        EAVNode node = new EAVNode(name);
        if (values.length == 1) {
            node.setPayload(new ByteArrayInputStream(values[0]));
        } else {
            node.setPayload(wrap(values));
        }
        return node;
    }

    public EAVNode MULTISET(String name, byte[]... values) {
        EAVNode node = new EAVNode(name);
        node.setPayload(wrap(values));
        return node;
    }

    public EAVNode SET(String name, byte[]... values) {
        EAVNode node = new EAVNode(name);
        node.setPayload(EAVContainerType.SET, wrap(values));
        return node;
    }

    public TransformList ADD(byte[]... values) {
        return new TransformList(EAVType.NUMBER, ADD, wrap(values));
    }

    public TransformList REMOVE(byte[]... values) {
        return new TransformList(EAVType.NUMBER, REMOVE, wrap(values));
    }

    private InputStream[] wrap(byte[]... values) {
        InputStream[] retval = new InputStream[values.length];
        int index = 0;
        for (byte[] bs : values) {
            retval[index++] = new ByteArrayInputStream(bs);
        }
        return retval;
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

    public TransformList REMOVE(EAVType type, Number... values) {
        if (!ACCEPTABLE_NUMBER_TYPES.contains(type)) {
            throw new IllegalArgumentException("Numbers can not be of type " + type.name());
        }
        return new TransformList(type, REMOVE, values);
    }

    /*
     * Type Nodes
     */

    public EAVTypeMetadataNode TYPE(String name, EAVTypeMetadataNode... children) {
        EAVTypeMetadataNode node = new EAVTypeMetadataNode(name);
        node.addChildren(children);
        return node;
    }

    public EAVTypeMetadataNode TYPE(String name, EAVType type) {
        EAVTypeMetadataNode node = new EAVTypeMetadataNode(name);
        node.setType(type);
        return node;
    }

    public EAVTypeMetadataNode TYPE(String name, EAVType type, EAVContainerType cType) {
        EAVTypeMetadataNode node = new EAVTypeMetadataNode(name);
        node.setType(type);
        node.setContainerType(cType);
        return node;
    }

    public EAVTypeMetadataNode TYPE(String name, EAVType type, EAVContainerType cType, Map<String, Object> options) {
        EAVTypeMetadataNode node = new EAVTypeMetadataNode(name);
        node.setType(type);
        node.setContainerType(cType);
        node.setOptions(options);
        return node;
    }

    public EAVTypeMetadataNode TYPE(String name, EAVType type, Map<String, Object> options) {
        EAVTypeMetadataNode node = new EAVTypeMetadataNode(name);
        node.setType(type);
        node.setOptions(options);
        return node;
    }

    public static <T extends AbstractNode<T>>  void assertEquals(AbstractNode<T> expected, AbstractNode<T> actual) {
        assertEquals("", expected, actual);
    }

    public static <T extends AbstractNode<T>> void assertEquals(String message, AbstractNode<T> expected, AbstractNode<T> actual) {
        if(expected.getClass() != actual.getClass()) {
            fail("Classes don't match: "+expected.getClass()+" != "+actual.getClass());
        }
        if (expected == actual) {
            return;
        }
        if (expected == null) {
            fail(message + ": expected was null, actual was: " + treeString(actual));
        }
        if (actual == null) {
            fail(message + ": " + treeString(expected) + " expected, but was null");
        }

        List<T> serializedExpected = serialize(expected);
        List<T> serializedActual = serialize(actual);

        if (serializedExpected.size() != serializedActual.size()) {
            failComparison(message, expected, actual);
        }

        for (int i = 0, size = serializedExpected.size(); i < size; i++) {
            T expectedNode = serializedExpected.get(i);
            EAVPath relativePath = expectedNode.getRelativePath(expected.getPath());
            T actualNode = actual.resolve(relativePath);

            if (actualNode == null) {
                failComparison(message, expected, actual);
            }

            if (expectedNode.getName() == null) {
                if (actualNode.getName() != null) {
                    failComparison(message, expected, actual);
                }
            } else {
                if (!expectedNode.getName().equals(actualNode.getName())) {
                    failComparison(message, expected, actual);
                }
            }

            if(!comparePayloads(expectedNode, actualNode)) {
                failComparison(message, expected, actual);
            }
        }

    }
    
   

    private static <T extends AbstractNode<T>>  List<T> serialize(AbstractNode<T> node) {
        final List<T> collected = new ArrayList<T>();
        node.visit(new AbstractNodeVisitor<T>() {

            public void visit(int index, T node) {
                collected.add(node);
            }
        });
        return collected;
    }

    private static <T extends AbstractNode<T>>  void failComparison(String message, AbstractNode<T> expected, AbstractNode<T> actual) {
        assertEquals(message, treeString(expected), treeString(actual));
        fail(message);
    }

    private static <T extends AbstractNode<T>> String treeString(AbstractNode<T> node) {
        final StringBuilder builder = new StringBuilder("\n");
        sortChildren(node);
        node.visit(new AbstractNodeVisitor<T>() {

            public void visit(int index, T node) {
                for (int i = 0; i < index; i++) {
                    builder.append("    ");
                }
                builder.append(node.getName());
                builder.append(" : ");
                builder.append(printPayload(node));
                builder.append('\n');
            }
        });

        return builder.toString();
    }
    
    private static <T extends AbstractNode<T>> void sortChildren(AbstractNode<T> node) {
        if(node.isLeaf()) {
            return;
        }
        Collections.sort(node.getChildren(), new Comparator<AbstractNode<T>>() {

            public int compare(AbstractNode<T> o1, AbstractNode<T> o2) {
                if(o1 == null) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
            
        });
        for(AbstractNode<T> child : node.getChildren()) {
            sortChildren(child);
        }
    }
    
    

    protected void assertType(EAVTypeMetadataNode types, EAVType type, String... pathElements) {
        assertType(types, type, EAVContainerType.SINGLE, pathElements);
    }

    protected void assertType(EAVTypeMetadataNode types, EAVType type, EAVContainerType cType, String... pathElements) {
        EAVPath path = new EAVPath(pathElements);
        assertEquals(type, types.resolve(path).getType());
        assertEquals(cType, types.resolve(path).getContainerType());
    }
    
    protected void assertTransformation(EAVSetTransformation transformation,EAVType type, TransformList...lists) {
        assertSame(transformation.getType(), type);
        EAVMultipleCompare compare = new EAVMultipleCompare();
        
        for (TransformList transformList : lists) {
            Object[] payload = null;
            switch(transformList.operation) {
            case ADD: 
                payload = transformation.getAdd();
                break;
            case REMOVE:
                payload = transformation.getRemove();
                break;
            }
            
            if(! (Boolean) transformation.getType().doSwitch(compare, transformList.payload, payload)) {
                fail("Payloads differ");
            }
        }
    }


    protected static Map<String, Object> M(String... strings) {
        if (strings.length % 2 != 0) {
            throw new IllegalArgumentException("Please provide key value pairs");
        }

        Map<String, Object> retval = new HashMap<String, Object>();
        String key = null;
        for (String string : strings) {
            if (key == null) {
                key = string;
            } else {
                retval.put(key, string);
                key = null;
            }
        }
        return retval;
    }
    
    // Pluggable
    
    private static <T extends AbstractNode<T>> boolean comparePayloads(AbstractNode<T> node1, AbstractNode<T> node2) {
        return COMPARISON_STRATEGIES.get(node1.getClass()).comparePayloads(node1, node2);
    }
    
    
    private static <T extends AbstractNode<T>> String printPayload(AbstractNode<T> node) {
        return COMPARISON_STRATEGIES.get(node.getClass()).printPayload(node);
    }
    
    private static Map<Class, ComparisonStrategy> COMPARISON_STRATEGIES = new HashMap<Class, ComparisonStrategy>() {{
        put(EAVNode.class, new EAVNodeComparisonStrategy());
        put(EAVTypeMetadataNode.class, new EAVTypeMetadataComparisonStrategy());
    }};
    
    private static interface ComparisonStrategy<T extends AbstractNode<T>> {
        public String printPayload(T node);
        public boolean comparePayloads(T node1, T node2);
    }
    
    private static class EAVNodeComparisonStrategy implements ComparisonStrategy<EAVNode> {

        public boolean comparePayloads(EAVNode expectedNode, EAVNode actualNode) {
            if (!expectedNode.getType().equals(actualNode.getType())) {
                return false;
            }
            if (expectedNode.getContainerType() != actualNode.getContainerType()) {
                return false;
            }
            EAVTypeSwitcher compare = null;
            if (expectedNode.isMultiple()) {
                compare = new EAVMultipleCompare();
            } else {
                compare = new EAVPayloadCompare();
            }
            boolean equalPayloads = (Boolean) expectedNode.getType().doSwitch(compare, expectedNode.getPayload(), actualNode.getPayload());
            if (!equalPayloads) {
                return false;
            }
            return true;
            
        }

        public String printPayload(EAVNode node) {
            EAVTypeSwitcher pp = null;
            if(node.isMultiple()) {
                pp = new EAVMultiplePrettyPrint();
            } else {
                pp = new EAVValuePrettyPrint();
            }
            return (String) node.getType().doSwitch(pp, node.getPayload());
        }
        
    }
    
    private static class EAVTypeMetadataComparisonStrategy implements ComparisonStrategy<EAVTypeMetadataNode> {

        public boolean comparePayloads(EAVTypeMetadataNode node1, EAVTypeMetadataNode node2) {
            boolean optionsMatch = node1.getOptions() == node2.getOptions();
            if(!optionsMatch && (node1.getOptions() == null || node2.getOptions() == null)) {
                optionsMatch = false;
            } else {
                optionsMatch = node1.getOptions().equals(node2.getOptions());
            }
            
            
            return node1.getContainerType() == node2.getContainerType() && node1.getType() == node2.getType() && optionsMatch;
        }

        public String printPayload(EAVTypeMetadataNode node) {
            return node.getTypeDescription()+"  "+node.getOptions();
        }
        
    }
    

}
