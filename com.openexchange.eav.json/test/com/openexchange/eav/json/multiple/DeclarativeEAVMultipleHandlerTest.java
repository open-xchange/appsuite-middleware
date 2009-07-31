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

package com.openexchange.eav.json.multiple;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.eav.AbstractNode;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVStorage;
import com.openexchange.eav.EAVUnitTest;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.json.JSONAssertion;
import com.openexchange.sim.DynamicSim;
import com.openexchange.sim.Expectation;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.groupware.ldap.MockUser;

/**
 * {@link DeclarativeEAVMultipleHandlerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class DeclarativeEAVMultipleHandlerTest extends EAVUnitTest {
    
    protected Context ctx = new SimContext(1);
    private String action;
    private JSONObject request;
    private Object expectedResponse;
    
    private List<DynamicSim> dynamicSims = new LinkedList<DynamicSim>();
    protected Object response;
    private Integer expectedException = null;
    private String timezone = TimeZone.getDefault().getID();
    
    @Override
    protected void setUp() throws Exception {
        dynamicSims.clear();
    }
    
    protected JSONObject PARAMS(Object...params) throws JSONException {
        JSONObject object = new JSONObject();
        String key = null;
        for(Object p : params) {
            if(key == null) {
                key = p.toString();
            } else {
                object.put(key, p);
                key = null;
            }
        }
        
        return object;
    }
    
    protected String BODY(Object body) {
        return body.toString();
    }
    
    public void setUserTimeZone(String tz) {
        this.timezone = tz;
    }
    
    protected void runRequest() throws AbstractOXException, JSONException {
        EAVMultipleHandler multipleHandler = new EAVMultipleHandler();
        multipleHandler.setStorage(DynamicSim.compose(EAVStorage.class, dynamicSims));
        
        try {
            MockUser user = new MockUser();
            user.setTimeZone(timezone);
            response = multipleHandler.performRequest(action, request, new ServerSessionAdapter(null, ctx, user));
            if(expectedException != null) {
                fail("Expected Exception with detailNumber: "+expectedException);
            }
        } catch (AbstractOXException x) {
            if(expectedException == null) {
                throw x;
            }
            assertEquals((int)expectedException, x.getDetailNumber());
        }
        
        if(expectedResponse != null) {
            if(JSONAssertion.class.isInstance(expectedResponse)) {
                JSONAssertion.assertValidates((JSONAssertion) expectedResponse, response);
            } else {
                assertEquals("Response did not match", expectedResponse, response);
            }
        }
        
        for(DynamicSim sim : dynamicSims) {
            assertTrue("Missing method call "+sim.getExpectation(), sim.wasCalled());
            
        }
    
    }

    protected void expectException(int detailNumber) {
        this.expectedException = detailNumber;
    }
    
    protected void expectResponseData(Object data) {
        this.expectedResponse = data;
    }
    
    protected void ignoreResponseData() {
        expectedResponse = null;
    }

    protected void expectStorageCall(String methodName, Object...args) {
        dynamicSims.add(new DynamicSim(new EAVArgumentExpectation(methodName, args)));
    }
    
    protected void andReturn(Object retval) {
        dynamicSims.get(dynamicSims.size()-1).setReturnValue(retval);
    }
    

    protected void duringRequest(String action, JSONObject params) throws JSONException {
        duringRequest(action, params, null);
    }
    
    protected void duringRequest(String action, JSONObject params, String body) throws JSONException {
        this.action = action;
        if(body != null) {
            params.put(ResponseFields.DATA, new JSONObject("{dummy : "+body+"}").get("dummy"));
        }
        this.request = params;
    }

    private static final class EAVArgumentExpectation implements Expectation {
        private String methodName = null;
        private Object[] args = null;
        
        private EAVArgumentExpectation(String methodName, Object[] args) {
            super();
            this.methodName = methodName;
            this.args = args;
        }

        public int getArgumentLength() {
            return args.length;
        }

        public Object getArgument(int i) {
            return args[i];    
        }

        public String getMethodName() {
            return methodName;
        }

        public void verify(Method method, Object[] args) {
            String methodName = method.getName();
            assertEquals(getMethodName(), methodName);
            
            int index = 0;
            
            assertEquals(getArgumentLength(), args.length);
            
            for(Object arg : args) {
                Object expectedArg = getArgument(index++);
                if(AbstractNode.class.isInstance(arg)) {
                    assertEquals(methodName, (AbstractNode) expectedArg, (AbstractNode) arg);
                } else {
                    assertEquals(methodName, expectedArg, arg);
                }
            }            
        }
        
        public String toString() {
            return methodName;
        }
        
    }
    
    
}
