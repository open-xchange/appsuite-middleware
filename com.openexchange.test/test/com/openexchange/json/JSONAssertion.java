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

package com.openexchange.json;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link JSONAssertion}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class JSONAssertion implements JSONCondition {
    
    public static final void assertValidates(JSONAssertion assertion, Object o) {
        assertNotNull("Object was null", o);
        if(!assertion.validate(o)) {
            fail(assertion.getComplaint());
        }
    }
    
    private Stack<JSONAssertion> stack = new Stack<JSONAssertion>();
    
    private List<JSONCondition> conditions = new LinkedList<JSONCondition>();
    private String key;

    private String complaint;

    private int lastIndex;
    
    public JSONAssertion isObject() {
        if(!stack.isEmpty()) {
            stack.peek().isObject();
        } else {
            conditions.add(new IsOfType(JSONObject.class));
        }
        return this;
    }
    
    public JSONAssertion hasKey(String key) {
        if(!stack.isEmpty()) {
            stack.peek().hasKey(key);
        } else {
            conditions.add(new HasKey(key));
            this.key = key;
        }
        return this;
    }
    
    public JSONAssertion withValue(Object value) {
        if(!stack.isEmpty()) {
            stack.peek().withValue(value);
        } else {
            conditions.add(new KeyValuePair(key, value));
        }
        return this;
    }
    
    public JSONAssertion withValueObject() {
        if(!stack.isEmpty()) {
            stack.peek().withValueObject();
            return this;
        }
        JSONAssertion stackElement = new JSONAssertion();
        conditions.add(new ValueObject(key, stackElement));
        stackElement.isObject();
        stack.push(stackElement);
        return this;
    }
    
    public JSONAssertion withValueArray() {
        if(!stack.isEmpty()) {
            stack.peek().withValueArray();
            return this;
        }
        JSONAssertion stackElement = new JSONAssertion();
        conditions.add(new ValueArray(key, stackElement));
        stackElement.isArray();
        stack.push(stackElement);
        return this;
    }

    public JSONAssertion atIndex(int i) {
        if(!stack.isEmpty()) {
            stack.peek().atIndex(i);
            return this;
        }
        this.lastIndex = i;
        conditions.add(new HasIndex(i));
        return null;
    }

    
    public JSONAssertion hasNoMoreKeys() {
        if(!stack.isEmpty())
            stack.pop();
        return this;
    }
    
    public JSONAssertion isArray() {
        if(!stack.isEmpty()) {
            stack.peek().isArray();
            return this;
        }
        conditions.add(new IsOfType(JSONArray.class));
        return this;
    }
    
    public JSONAssertion withValues(Object...values) {
        if(!stack.isEmpty()) {
            stack.peek().withValues(values);
            return this;
        }
        conditions.add(new WithValues(values));
        return this;
    }
    
    public JSONAssertion inAnyOrder() {
        if(!stack.isEmpty()) {
            stack.peek().inAnyOrder();
            return this;
        }
        ((WithValues)conditions.get(conditions.size()-1)).ignoreOrder = true;
        return this;
    }
    
    
    public JSONAssertion objectEnds() {
        return hasNoMoreKeys();
    }
    
    public boolean validate(Object o) {
        for(JSONCondition condition : conditions) {
            if(!condition.validate(o)) {
                complaint = condition.getComplaint();
                return false;
            }
        }
        return true;
    }
    
    public String getComplaint() {
        return complaint;
    }
    
    
    private static final class IsOfType implements JSONCondition{
        private String complaint;
        private Class type;
        
        public IsOfType(Class type) {
            this.type = type;
        }
        
        public boolean validate(Object o) {
            boolean isCorrectType = type.isInstance(o);
            if(!isCorrectType) {
                complaint = "Expected "+type.getName()+" was: "+o.getClass().getName();
            }
            return isCorrectType;
        }
        public String getComplaint() {
            return complaint;
        }
    }
    
    private static final class HasKey implements JSONCondition {
        private String key;

        public HasKey(String key) {
            this.key = key;
        }
        
        public boolean validate(Object o) {
            return ((JSONObject)o).has(key);
        }
        
        public String getComplaint() {
            return "Missing key: "+key;
        }
        
    }
    
    private static final class HasIndex implements JSONCondition {
        private int index;

        public HasIndex(int index) {
            this.index = index;
        }
        
        public boolean validate(Object o) {
            return ((JSONArray)o).length() > index;
        }
        
        public String getComplaint() {
            return "Missing index: "+index;
        }
        
    }
    
    private static final class KeyValuePair implements JSONCondition {
        private String key;
        private Object value;
        private String complaint;

        public KeyValuePair(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public boolean validate(Object o) {
            try {
                Object object = ((JSONObject)o).get(key);
                if(!object.equals(value)){
                    complaint = "Expected value "+value+" of class ("+value.getClass().getName()+") for key "+key+" but got "+object+" of class ("+object.getClass().getName()+")";
                    return false;
                }
                return true;
            } catch (JSONException e) {
                return false;
            }
        }
        
        public String getComplaint() {
            return complaint;
        }
    }
    
    private static final class ValueObject implements JSONCondition {

        private String key;
        private JSONAssertion assertion;

        public ValueObject(String key, JSONAssertion assertion) {
            this.key = key;
            this.assertion = assertion;
        }

        public String getComplaint() {
            return assertion.getComplaint();
        }

        public boolean validate(Object o) {
            try {
                Object subObject = ((JSONObject)o).get(key);
                return assertion.validate(subObject);
            } catch (JSONException x) {
                return false;
            }
        }
        
    }
    
    private static final class ValueArray implements JSONCondition {

        private String key;
        private JSONAssertion assertion;

        public ValueArray(String key, JSONAssertion assertion) {
            this.key = key;
            this.assertion = assertion;
        }

        public String getComplaint() {
            return assertion.getComplaint();
        }

        public boolean validate(Object o) {
            try {
                Object subObject = ((JSONObject)o).get(key);
                return assertion.validate(subObject);
            } catch (JSONException x) {
                return false;
            }
        }
        
    }
    
    private static final class WithValues implements JSONCondition {
        private Object[] values;
        private String complaint;
        
        public boolean ignoreOrder = false;
        
        public WithValues(Object[] values) {
            this.values = values;
        }
        
        public boolean validate(Object o) {
            JSONArray arr = (JSONArray) o;
            if(arr.length() != values.length) {
                complaint = "Lengths differ: expected "+values.length+" was: "+arr.length();
                return false;
            }
            if(!ignoreOrder) {
                for(int i = 0; i < values.length; i++) {
                    Object expected = values[i];
                    Object actual;
                    try {
                        actual = arr.get(i);
                    } catch (JSONException e) {
                        complaint = e.toString();
                        return false;
                    }
                    if(!expected.equals(actual)) {
                        complaint = "Expected "+expected+" got: "+actual+" at index "+i;
                        return false;
                    }
                }
            } else {
                List<Object> expectedList = new ArrayList<Object>();
                for(int i = 0; i < values.length; i++) {
                    expectedList.add(values[i]);
                }
                
                for(int i = 0; i < values.length; i++) {
                    Object v;
                    try {
                        v = arr.get(i);
                    } catch (JSONException e) {
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
        
        public String getComplaint() {
            return complaint;
        }
    }

 

}
