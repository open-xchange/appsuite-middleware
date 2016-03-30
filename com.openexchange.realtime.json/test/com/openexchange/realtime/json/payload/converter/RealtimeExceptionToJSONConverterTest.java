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

package com.openexchange.realtime.json.payload.converter;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.json.payload.converter.RealtimeExceptionToJSONConverter;
import com.openexchange.realtime.json.payload.converter.StackTraceElementToJSONConverter;
import com.openexchange.realtime.json.payload.converter.ThrowableToJSONConverter;
import com.openexchange.realtime.payload.converter.sim.SimpleConverterSim;

/**
 * {@link RealtimeExceptionToJSONConverterTest}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RealtimeExceptionToJSONConverterTest {

    RealtimeExceptionToJSONConverter realtimeExceptionToJSONConverter = null;

    SimpleConverterSim simpleConverter = null;
    
    StackTraceElement[] stackTrace = null;

    Throwable throwable = null;

    RealtimeException sessionInvalidException = null;

    String arg0 = "a'r'g/\"0", arg1 = "arg1", arg2 = "arg2";

    @Before
    public void setUp() throws Exception {
        realtimeExceptionToJSONConverter = new RealtimeExceptionToJSONConverter();
        simpleConverter = new SimpleConverterSim();
        simpleConverter.registerConverter(new StackTraceElementToJSONConverter());
        simpleConverter.registerConverter(new ThrowableToJSONConverter());
        
        ArrayList<StackTraceElement> stackTraceList = new ArrayList<StackTraceElement>();
        stackTraceList.add(new StackTraceElement("com.openexchange.exception.OXExceptionFactory", "create", "OXExceptionFactory.java", 158));
        stackTraceList.add(new StackTraceElement("com.openexchange.realtime.exception.RealtimeExceptionFactory", "create", "RealtimeExceptionFactory.java", 89));
        stackTraceList.add(new StackTraceElement("com.openexchange.realtime.exception.RealtimeExceptionCodes", "create", "RealtimeExceptionCodes.java", 212));
        stackTraceList.add(new StackTraceElement("com.openexchange.realtime.atmosphere.payload.converter.RealtimeExceptionToJSONConverterTest", "setUp", "RealtimeExceptionToJSONConverterTest.java", 87));
        stackTraceList.add(new StackTraceElement("sun.reflect.NativeMethodAccessorImpl", "invoke0", "NativeMethodAccessorImpl.java", -2));
        stackTraceList.add(new StackTraceElement("sun.reflect.NativeMethodAccessorImpl", "invoke", "NativeMethodAccessorImpl.java", 39));
        stackTraceList.add(new StackTraceElement("sun.reflect.DelegatingMethodAccessorImpl", "invoke", "DelegatingMethodAccessorImpl.java", 25));
        stackTraceList.add(new StackTraceElement("java.lang.reflect.Method", "invoke", "Method.java", 597));
        stackTraceList.add(new StackTraceElement("org.junit.runners.model.FrameworkMethod$1", "runReflectiveCall", "FrameworkMethod.java", 44));
        stackTraceList.add(new StackTraceElement("org.junit.internal.runners.model.ReflectiveCallable", "run", "ReflectiveCallable.java", 15));
        stackTraceList.add(new StackTraceElement("org.junit.runners.model.FrameworkMethod", "invokeExplosively", "FrameworkMethod.java", 41));
        stackTraceList.add(new StackTraceElement("org.junit.internal.runners.statements.RunBefores", "evaluate", "RunBefores.java", 27));
        stackTraceList.add(new StackTraceElement("org.junit.internal.runners.statements.RunAfters", "evaluate", "RunAfters.java", 31));
        stackTraceList.add(new StackTraceElement("org.junit.runners.BlockJUnit4ClassRunner", "runChild", "BlockJUnit4ClassRunner.java", 70));
        stackTraceList.add(new StackTraceElement("org.junit.runners.BlockJUnit4ClassRunner", "runChild", "BlockJUnit4ClassRunner.java", 44));
        stackTraceList.add(new StackTraceElement("org.junit.runners.ParentRunner", "runChildren", "ParentRunner.java", 180));
        stackTraceList.add(new StackTraceElement("org.junit.runners.ParentRunner", "access$000", "ParentRunner.java", 41));
        stackTraceList.add(new StackTraceElement("org.junit.runners.ParentRunner$1", "evaluate", "ParentRunner.java", 173));
        stackTraceList.add(new StackTraceElement("org.junit.internal.runners.statements.RunBefores", "evaluate", "RunBefores.java", 28));
        stackTraceList.add(new StackTraceElement("org.junit.internal.runners.statements.RunAfters", "evaluate", "RunAfters.java", 31));
        stackTraceList.add(new StackTraceElement("org.junit.runners.ParentRunner", "run", "ParentRunner.java", 220));
        stackTraceList.add(new StackTraceElement("org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference", "run", "JUnit4TestReference.java", 50));
        stackTraceList.add(new StackTraceElement("org.eclipse.jdt.internal.junit.runner.TestExecution", "run", "TestExecution.java", 38));
        stackTraceList.add(new StackTraceElement("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", "runTests", "RemoteTestRunner.java", 467));
        stackTraceList.add(new StackTraceElement("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", "runTests", "RemoteTestRunner.java", 683));
        stackTraceList.add(new StackTraceElement("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", "run", "RemoteTestRunner.java", 390));
        stackTraceList.add(new StackTraceElement("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", "main", "RemoteTestRunner.java", 197));
        stackTrace = stackTraceList.toArray(new StackTraceElement[stackTraceList.size()]);
        throwable = new Throwable("First throwable");
        throwable.setStackTrace(stackTrace);
    }

    @Test
    public void testGetInputFormat() {
        assertEquals(RealtimeException.class.getSimpleName(), realtimeExceptionToJSONConverter.getInputFormat());
    }

    @Test
    public void testConvert() throws OXException, JSONException {
        sessionInvalidException = RealtimeExceptionCodes.SESSION_INVALID.create(throwable, arg0, arg1, arg2);
        sessionInvalidException.setStackTrace(stackTrace);
        Object object = realtimeExceptionToJSONConverter.convert(sessionInvalidException, null, simpleConverter);
        assertNotNull(object);
        assertTrue(object instanceof JSONObject);
        JSONObject realtimeExceptionJSON = JSONObject.class.cast(object);
        assertEquals("RT_STANZA", realtimeExceptionJSON.getString("prefix"));
        assertEquals(1005, realtimeExceptionJSON.getInt("code"));
        assertTrue(realtimeExceptionJSON.getString("localizedMessage").startsWith(
            "RT_STANZA-1005 Categories=ERROR Message='Your session is invalid.' exceptionID="));
        assertEquals("Your session is invalid.", realtimeExceptionJSON.getString("plainLogMessage"));

        JSONArray logArgs = realtimeExceptionJSON.getJSONArray("logArgs");
        assertEquals(arg0, logArgs.get(0));
        assertEquals(arg1, logArgs.get(1));
        assertEquals(arg2, logArgs.get(2));

        // Check begin and end of stacktrace
        JSONArray stackTrace = realtimeExceptionJSON.getJSONArray("stackTrace");
        assertEquals(27, stackTrace.length());

        JSONObject firstStackTraceElement = (JSONObject) stackTrace.get(0);
        assertEquals("OXExceptionFactory.java", firstStackTraceElement.getString("fileName"));
        assertEquals("com.openexchange.exception.OXExceptionFactory", firstStackTraceElement.getString("className"));
        assertEquals("create", firstStackTraceElement.getString("methodName"));

        JSONObject lastStackTraceElement = (JSONObject) stackTrace.get(26);
        assertEquals("RemoteTestRunner.java", lastStackTraceElement.getString("fileName"));
        assertEquals("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", lastStackTraceElement.getString("className"));

        // Check cause including begin and end of stacktrace
        JSONObject cause = (JSONObject) realtimeExceptionJSON.get("cause");
        assertEquals(cause.optString("message"), "First throwable");
        JSONArray causeTrace = cause.getJSONArray("stackTrace");
        assertEquals(27, causeTrace.length());
        
        JSONObject firstCauseStackTraceElement = (JSONObject) stackTrace.get(0);
        assertEquals("OXExceptionFactory.java", firstCauseStackTraceElement.getString("fileName"));
        assertEquals("com.openexchange.exception.OXExceptionFactory", firstCauseStackTraceElement.getString("className"));
        assertEquals("create", firstCauseStackTraceElement.getString("methodName"));

        JSONObject lastCauseStackTraceElement = (JSONObject) stackTrace.get(26);
        assertEquals("RemoteTestRunner.java", lastCauseStackTraceElement.getString("fileName"));
        assertEquals("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", lastCauseStackTraceElement.getString("className"));
    }
    
    @Test
    public void testNoArgsConvert() throws OXException, JSONException {
        sessionInvalidException = RealtimeExceptionCodes.SESSION_INVALID.create();
        sessionInvalidException.setStackTrace(stackTrace);
        Object object = realtimeExceptionToJSONConverter.convert(sessionInvalidException, null, simpleConverter);
        assertNotNull(object);
        assertTrue(object instanceof JSONObject);
        JSONObject realtimeExceptionJSON = JSONObject.class.cast(object);
        assertEquals("RT_STANZA", realtimeExceptionJSON.getString("prefix"));
        assertEquals(1005, realtimeExceptionJSON.getInt("code"));
        assertTrue(realtimeExceptionJSON.getString("localizedMessage").startsWith(
            "RT_STANZA-1005 Categories=ERROR Message='Your session is invalid.' exceptionID="));
        assertEquals("Your session is invalid.", realtimeExceptionJSON.getString("plainLogMessage"));

        JSONArray logArgs = realtimeExceptionJSON.optJSONArray("logArgs");
        assertTrue(logArgs.length() == 0);

        // Check begin and end of stacktrace
        JSONArray stackTrace = realtimeExceptionJSON.getJSONArray("stackTrace");
        assertEquals(27, stackTrace.length());

        JSONObject firstStackTraceElement = (JSONObject) stackTrace.get(0);
        assertEquals("OXExceptionFactory.java", firstStackTraceElement.getString("fileName"));
        assertEquals("com.openexchange.exception.OXExceptionFactory", firstStackTraceElement.getString("className"));
        assertEquals("create", firstStackTraceElement.getString("methodName"));

        JSONObject lastStackTraceElement = (JSONObject) stackTrace.get(26);
        assertEquals("RemoteTestRunner.java", lastStackTraceElement.getString("fileName"));
        assertEquals("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", lastStackTraceElement.getString("className"));

        // Check cause
        assertNull(realtimeExceptionJSON.opt("cause"));
    }

    @Test
    public void testGetOutputFormat() {
        assertEquals("json", realtimeExceptionToJSONConverter.getOutputFormat());
    }

}
