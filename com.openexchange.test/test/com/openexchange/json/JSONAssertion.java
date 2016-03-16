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

package com.openexchange.json;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;

/**
 * {@link JSONAssertion}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class JSONAssertion implements JSONCondition {

    public static final void assertValidates(final JSONAssertion assertion, final Object o) {
        assertNotNull("Object was null", o);
        if(!assertion.validate(o)) {
            fail(assertion.getComplaint());
        }
    }

    /**
     * Checks for equality of specified JSON values.
     *
     * @param jsonValue1 The first JSON value
     * @param jsonValue2 The second JSON value
     * @return <code>true</code> if equal; otherwise <code>false</code>
     */
    public static boolean equals(final JSONValue jsonValue1, final JSONValue jsonValue2) {
        if (jsonValue1 == jsonValue2) {
            return true;
        }
        if (null == jsonValue1) {
            if (null != jsonValue2) {
                return false;
            }
            return true; // Both null
        }
        if (null == jsonValue2) {
            return false;
        }
        if (jsonValue1.isArray()) {
            if (!jsonValue2.isArray()) {
                return false;
            }
            return getListFrom(jsonValue1.toArray()).equals(getListFrom(jsonValue2.toArray()));
        }
        if (jsonValue1.isObject()) {
            if (!jsonValue2.isObject()) {
                return false;
            }
            return getMapFrom(jsonValue1.toObject()).equals(getMapFrom(jsonValue2.toObject()));
        }
        return false;
    }

    /**
     * Checks for equality of specified JSON datas.
     *
     * @param jsonData1 The first JSON data
     * @param jsonData2 The second JSON data
     * @return <code>true</code> if equal; otherwise <code>false</code>
     */
    public static boolean equals(final Object jsonData1, final Object jsonData2) {
        if (jsonData1 == jsonData2) {
            return true;
        }
        if (null == jsonData1) {
            if (null != jsonData2) {
                return false;
            }
            return true; // Both null
        }
        if (null == jsonData2) {
            return false;
        }
        if (!jsonData1.getClass().equals(jsonData2.getClass())) {
            return false;
        }
        return getFrom(jsonData2).equals(getFrom(jsonData2));
    }

    private static List<Object> getListFrom(final JSONArray jsonArray) {
        final int length = jsonArray.length();
        final List<Object> list = new ArrayList<Object>(length);
        for (int i = 0; i < length; i++) {
            try {
                list.add(getFrom(jsonArray.get(i)));
            } catch (final JSONException e) {
                // Ignore
            }
        }
        return list;
    }

    private static Map<String, Object> getMapFrom(final JSONObject jsonObject) {
        final int length = jsonObject.length();
        final Map<String, Object> map = new HashMap<String, Object>(length);
        for (final Entry<String, Object> entry : jsonObject.entrySet()) {
            map.put(entry.getKey(), getFrom(entry.getValue()));
        }
        return map;
    }

    private static Object getFrom(final Object object) {
        if (object instanceof JSONArray) {
            return getListFrom((JSONArray) object);
        }
        if (object instanceof JSONObject) {
            return getMapFrom((JSONObject) object);
        }
        return object;
    }

    private Stack<JSONAssertion> stack = new Stack<JSONAssertion>();

    private final List<JSONCondition> conditions = new LinkedList<JSONCondition>();
    private String key;

    private String complaint;

    private int lastIndex;

    public JSONAssertion() {
        super();
    }

    private JSONAssertion (final Stack<JSONAssertion> stack) {
        super();
        this.stack = stack;
    }

    public JSONAssertion isObject() {
        if(!topmost()) {
            stack.peek().isObject();
        } else {
            conditions.add(new IsOfType(JSONObject.class));
        }
        return this;
    }

    public JSONAssertion hasKey(final String key) {
        if(!topmost()) {
            stack.peek().hasKey(key);
        } else {
            conditions.add(new HasKey(key));
            this.key = key;
        }
        return this;
    }

    public JSONAssertion withValue(final Object value) {

        if(!topmost()) {
            stack.peek().withValue(value);
        } else {
            conditions.add(new KeyValuePair(key, value));
        }
        return this;
    }

    public JSONAssertion withValueObject() {
        if(!topmost()) {
            stack.peek().withValueObject();
            return this;
        }
        final JSONAssertion stackElement = new JSONAssertion(stack);
        conditions.add(new ValueObject(key, stackElement));
        stack.push(stackElement);
        stackElement.isObject();
        return this;
    }

    public JSONAssertion withValueArray() {
        if(!topmost()) {
            stack.peek().withValueArray();
            return this;
        }
        final JSONAssertion stackElement = new JSONAssertion(stack);
        conditions.add(new ValueArray(key, stackElement));
        stack.push(stackElement);
        stackElement.isArray();
        return this;
    }

    public JSONAssertion atIndex(final int i) {
        if(!topmost()) {
            stack.peek().atIndex(i);
            return this;
        }
        this.lastIndex = i;
        conditions.add(new HasIndex(i));
        return null;
    }


    public JSONAssertion hasNoMoreKeys() {
        if(!stack.isEmpty()) {
            stack.pop();
        }
        return this;
    }

    public JSONAssertion isArray() {
        if(!topmost()) {
            stack.peek().isArray();
            return this;
        }
        conditions.add(new IsOfType(JSONArray.class));
        return this;
    }

    public JSONAssertion withValues(final Object...values) {
        if(!topmost()) {
            stack.peek().withValues(values);
            return this;
        }
        conditions.add(new WithValues(values));
        return this;
    }

    public JSONAssertion inAnyOrder() {
        if(!topmost()) {
            stack.peek().inAnyOrder();
            return this;
        }
        ((WithValues)conditions.get(conditions.size()-1)).ignoreOrder = true;
        hasNoMoreKeys();
        return this;
    }

    private boolean topmost() {
        if(stack.isEmpty()) {
            return true;
        }
        return stack.peek() == this;
    }

    public JSONAssertion inStrictOrder() {
        hasNoMoreKeys();
        return this;
    }


    public JSONAssertion objectEnds() {
        return hasNoMoreKeys();
    }

    @Override
    public boolean validate(final Object o) {
        for(final JSONCondition condition : conditions) {
            if(!condition.validate(o)) {
                complaint = condition.getComplaint();
                return false;
            }
        }
        return true;
    }

    @Override
    public String getComplaint() {
        return complaint;
    }


    private static final class IsOfType implements JSONCondition{
        private String complaint;
        private final Class type;

        public IsOfType(final Class type) {
            this.type = type;
        }

        @Override
        public boolean validate(final Object o) {
            final boolean isCorrectType = type.isInstance(o);
            if(!isCorrectType) {
                complaint = "Expected "+type.getName()+" was: "+o.getClass().getName();
            }
            return isCorrectType;
        }
        @Override
        public String getComplaint() {
            return complaint;
        }
    }

    private static final class HasKey implements JSONCondition {
        private final String key;
        private String complaint;
        private JSONObject object;

        public HasKey(final String key) {
            this.key = key;
        }

        @Override
        public boolean validate(final Object o) {
            if(!JSONObject.class.isInstance(o)) {
                this.complaint = o.getClass().getName()+" can not have key "+key;
                return false;
            }
            this.object = ((JSONObject)o);
            return ((JSONObject)o).has(key);
        }

        @Override
        public String getComplaint() {
            if(complaint != null) {
                return complaint;
            }
            return "Missing key: "+key+" in "+object;
        }

    }

    private static final class HasIndex implements JSONCondition {
        private final int index;

        public HasIndex(final int index) {
            this.index = index;
        }

        @Override
        public boolean validate(final Object o) {
            return ((JSONArray)o).length() > index;
        }

        @Override
        public String getComplaint() {
            return "Missing index: "+index;
        }

    }

    private static final class KeyValuePair implements JSONCondition {
        private final String key;
        private final Object value;
        private String complaint;

        public KeyValuePair(final String key, final Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean validate(final Object o) {
            try {
                final Object object = ((JSONObject)o).get(key);
                if(!equals(object, value)){
                    complaint = "Expected value "+value+" of class ("+value.getClass().getName()+") for key "+key+" but got "+object+" of class ("+object.getClass().getName()+")";
                    return false;
                }
                return true;
            } catch (final JSONException e) {
                return false;
            }
        }

        private boolean equals(final Object o1, final Object o2) {
            if(Number.class.isInstance(o1) && Number.class.isInstance(o2)) {
                if(isLongCompatible(o1) && isLongCompatible(o2)) {
                    return ((Number)o1).longValue() - ((Number)o2).longValue() == 0;
                }
            }
            return o1.equals(o2);
        }

        private boolean isLongCompatible(final Object o1) {
            for(final Class c : new Class[]{Long.class, Integer.class}) {
                if(c.isInstance(o1)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getComplaint() {
            return complaint;
        }
    }

    private static final class ValueObject implements JSONCondition {

        private final String key;
        private final JSONAssertion assertion;

        public ValueObject(final String key, final JSONAssertion assertion) {
            this.key = key;
            this.assertion = assertion;
        }

        @Override
        public String getComplaint() {
            return assertion.getComplaint();
        }

        @Override
        public boolean validate(final Object o) {
            try {
                final Object subObject = ((JSONObject)o).get(key);
                return assertion.validate(subObject);
            } catch (final JSONException x) {
                return false;
            }
        }

    }

    private static final class ValueArray implements JSONCondition {

        private final String key;
        private final JSONAssertion assertion;

        public ValueArray(final String key, final JSONAssertion assertion) {
            this.key = key;
            this.assertion = assertion;
        }

        @Override
        public String getComplaint() {
            return assertion.getComplaint();
        }

        @Override
        public boolean validate(final Object o) {
            try {
                final Object subObject = ((JSONObject)o).get(key);
                return assertion.validate(subObject);
            } catch (final JSONException x) {
                return false;
            }
        }

    }

    private static final class WithValues implements JSONCondition {
        private final Object[] values;
        private String complaint;

        public boolean ignoreOrder = false;

        public WithValues(final Object[] values) {
            this.values = values;
        }

        @Override
        public boolean validate(final Object o) {
            final JSONArray arr = (JSONArray) o;
            if(arr.length() != values.length) {
                complaint = "Lengths differ: expected "+values.length+" was: "+arr.length();
                return false;
            }
            if(!ignoreOrder) {
                for(int i = 0; i < values.length; i++) {
                    final Object expected = values[i];
                    Object actual;
                    try {
                        actual = arr.get(i);
                    } catch (final JSONException e) {
                        complaint = e.toString();
                        return false;
                    }
                    if(!expected.equals(actual)) {
                        complaint = "Expected "+expected+" got: "+actual+" at index "+i;
                        return false;
                    }
                }
            } else {
                final List<Object> expectedList = new ArrayList<Object>();
                for(int i = 0; i < values.length; i++) {
                    expectedList.add(values[i]);
                }

                for(int i = 0; i < values.length; i++) {
                    Object v;
                    try {
                        v = arr.get(i);
                    } catch (final JSONException e) {
                        complaint = e.toString();
                        return false;
                    }
                    if(!expectedList.contains(v)) {
                        complaint = "Did not expect "+v;
                        return false;
                    }
                    expectedList.remove(v);
                }
            }
            return true;
        }

        @Override
        public String getComplaint() {
            return complaint;
        }
    }



}
