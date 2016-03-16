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

import static org.junit.Assert.assertEquals;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.json.payload.converter.JSONToRealtimeExceptionConverter;
import com.openexchange.realtime.json.payload.converter.JSONToStackTraceElementConverter;
import com.openexchange.realtime.json.payload.converter.JSONToThrowableConverter;
import com.openexchange.realtime.payload.converter.sim.SimpleConverterSim;


/**
 * {@link JSONToRealtimeExceptionConverterTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class JSONToRealtimeExceptionConverterTest {
    String realtimeExceptionString = "{\"localizedMessage\":\"RT_STANZA-1005 Categories=ERROR Message='Your session is invalid.' exceptionID=448073662-1\",\"cause\":{\"message\":\"First throwable\",\"stackTrace\":[{\"fileName\":\"RealtimeExceptionToJSONConverterTest.java\",\"lineNumber\":83,\"className\":\"com.openexchange.realtime.atmosphere.payload.converter.RealtimeExceptionToJSONConverterTest\",\"methodName\":\"setUp\"},{\"fileName\":\"NativeMethodAccessorImpl.java\",\"lineNumber\":-2,\"className\":\"sun.reflect.NativeMethodAccessorImpl\",\"methodName\":\"invoke0\"},{\"fileName\":\"NativeMethodAccessorImpl.java\",\"lineNumber\":39,\"className\":\"sun.reflect.NativeMethodAccessorImpl\",\"methodName\":\"invoke\"},{\"fileName\":\"DelegatingMethodAccessorImpl.java\",\"lineNumber\":25,\"className\":\"sun.reflect.DelegatingMethodAccessorImpl\",\"methodName\":\"invoke\"},{\"fileName\":\"Method.java\",\"lineNumber\":597,\"className\":\"java.lang.reflect.Method\",\"methodName\":\"invoke\"},{\"fileName\":\"FrameworkMethod.java\",\"lineNumber\":44,\"className\":\"org.junit.runners.model.FrameworkMethod$1\",\"methodName\":\"runReflectiveCall\"},{\"fileName\":\"ReflectiveCallable.java\",\"lineNumber\":15,\"className\":\"org.junit.internal.runners.model.ReflectiveCallable\",\"methodName\":\"run\"},{\"fileName\":\"FrameworkMethod.java\",\"lineNumber\":41,\"className\":\"org.junit.runners.model.FrameworkMethod\",\"methodName\":\"invokeExplosively\"},{\"fileName\":\"RunBefores.java\",\"lineNumber\":27,\"className\":\"org.junit.internal.runners.statements.RunBefores\",\"methodName\":\"evaluate\"},{\"fileName\":\"RunAfters.java\",\"lineNumber\":31,\"className\":\"org.junit.internal.runners.statements.RunAfters\",\"methodName\":\"evaluate\"},{\"fileName\":\"BlockJUnit4ClassRunner.java\",\"lineNumber\":70,\"className\":\"org.junit.runners.BlockJUnit4ClassRunner\",\"methodName\":\"runChild\"},{\"fileName\":\"BlockJUnit4ClassRunner.java\",\"lineNumber\":44,\"className\":\"org.junit.runners.BlockJUnit4ClassRunner\",\"methodName\":\"runChild\"},{\"fileName\":\"ParentRunner.java\",\"lineNumber\":180,\"className\":\"org.junit.runners.ParentRunner\",\"methodName\":\"runChildren\"},{\"fileName\":\"ParentRunner.java\",\"lineNumber\":41,\"className\":\"org.junit.runners.ParentRunner\",\"methodName\":\"access$000\"},{\"fileName\":\"ParentRunner.java\",\"lineNumber\":173,\"className\":\"org.junit.runners.ParentRunner$1\",\"methodName\":\"evaluate\"},{\"fileName\":\"RunBefores.java\",\"lineNumber\":28,\"className\":\"org.junit.internal.runners.statements.RunBefores\",\"methodName\":\"evaluate\"},{\"fileName\":\"RunAfters.java\",\"lineNumber\":31,\"className\":\"org.junit.internal.runners.statements.RunAfters\",\"methodName\":\"evaluate\"},{\"fileName\":\"ParentRunner.java\",\"lineNumber\":220,\"className\":\"org.junit.runners.ParentRunner\",\"methodName\":\"run\"},{\"fileName\":\"JUnit4TestReference.java\",\"lineNumber\":50,\"className\":\"org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference\",\"methodName\":\"run\"},{\"fileName\":\"TestExecution.java\",\"lineNumber\":38,\"className\":\"org.eclipse.jdt.internal.junit.runner.TestExecution\",\"methodName\":\"run\"},{\"fileName\":\"RemoteTestRunner.java\",\"lineNumber\":467,\"className\":\"org.eclipse.jdt.internal.junit.runner.RemoteTestRunner\",\"methodName\":\"runTests\"},{\"fileName\":\"RemoteTestRunner.java\",\"lineNumber\":683,\"className\":\"org.eclipse.jdt.internal.junit.runner.RemoteTestRunner\",\"methodName\":\"runTests\"},{\"fileName\":\"RemoteTestRunner.java\",\"lineNumber\":390,\"className\":\"org.eclipse.jdt.internal.junit.runner.RemoteTestRunner\",\"methodName\":\"run\"},{\"fileName\":\"RemoteTestRunner.java\",\"lineNumber\":197,\"className\":\"org.eclipse.jdt.internal.junit.runner.RemoteTestRunner\",\"methodName\":\"main\"}]},\"plainLogMessage\":\"Your session is invalid.\",\"code\":1005,\"stackTrace\":[{\"fileName\":\"OXExceptionFactory.java\",\"lineNumber\":158,\"className\":\"com.openexchange.exception.OXExceptionFactory\",\"methodName\":\"create\"},{\"fileName\":\"RealtimeExceptionFactory.java\",\"lineNumber\":89,\"className\":\"com.openexchange.realtime.exception.RealtimeExceptionFactory\",\"methodName\":\"create\"},{\"fileName\":\"RealtimeExceptionCodes.java\",\"lineNumber\":211,\"className\":\"com.openexchange.realtime.exception.RealtimeExceptionCodes\",\"methodName\":\"create\"},{\"fileName\":\"RealtimeExceptionToJSONConverterTest.java\",\"lineNumber\":84,\"className\":\"com.openexchange.realtime.atmosphere.payload.converter.RealtimeExceptionToJSONConverterTest\",\"methodName\":\"setUp\"},{\"fileName\":\"NativeMethodAccessorImpl.java\",\"lineNumber\":-2,\"className\":\"sun.reflect.NativeMethodAccessorImpl\",\"methodName\":\"invoke0\"},{\"fileName\":\"NativeMethodAccessorImpl.java\",\"lineNumber\":39,\"className\":\"sun.reflect.NativeMethodAccessorImpl\",\"methodName\":\"invoke\"},{\"fileName\":\"DelegatingMethodAccessorImpl.java\",\"lineNumber\":25,\"className\":\"sun.reflect.DelegatingMethodAccessorImpl\",\"methodName\":\"invoke\"},{\"fileName\":\"Method.java\",\"lineNumber\":597,\"className\":\"java.lang.reflect.Method\",\"methodName\":\"invoke\"},{\"fileName\":\"FrameworkMethod.java\",\"lineNumber\":44,\"className\":\"org.junit.runners.model.FrameworkMethod$1\",\"methodName\":\"runReflectiveCall\"},{\"fileName\":\"ReflectiveCallable.java\",\"lineNumber\":15,\"className\":\"org.junit.internal.runners.model.ReflectiveCallable\",\"methodName\":\"run\"},{\"fileName\":\"FrameworkMethod.java\",\"lineNumber\":41,\"className\":\"org.junit.runners.model.FrameworkMethod\",\"methodName\":\"invokeExplosively\"},{\"fileName\":\"RunBefores.java\",\"lineNumber\":27,\"className\":\"org.junit.internal.runners.statements.RunBefores\",\"methodName\":\"evaluate\"},{\"fileName\":\"RunAfters.java\",\"lineNumber\":31,\"className\":\"org.junit.internal.runners.statements.RunAfters\",\"methodName\":\"evaluate\"},{\"fileName\":\"BlockJUnit4ClassRunner.java\",\"lineNumber\":70,\"className\":\"org.junit.runners.BlockJUnit4ClassRunner\",\"methodName\":\"runChild\"},{\"fileName\":\"BlockJUnit4ClassRunner.java\",\"lineNumber\":44,\"className\":\"org.junit.runners.BlockJUnit4ClassRunner\",\"methodName\":\"runChild\"},{\"fileName\":\"ParentRunner.java\",\"lineNumber\":180,\"className\":\"org.junit.runners.ParentRunner\",\"methodName\":\"runChildren\"},{\"fileName\":\"ParentRunner.java\",\"lineNumber\":41,\"className\":\"org.junit.runners.ParentRunner\",\"methodName\":\"access$000\"},{\"fileName\":\"ParentRunner.java\",\"lineNumber\":173,\"className\":\"org.junit.runners.ParentRunner$1\",\"methodName\":\"evaluate\"},{\"fileName\":\"RunBefores.java\",\"lineNumber\":28,\"className\":\"org.junit.internal.runners.statements.RunBefores\",\"methodName\":\"evaluate\"},{\"fileName\":\"RunAfters.java\",\"lineNumber\":31,\"className\":\"org.junit.internal.runners.statements.RunAfters\",\"methodName\":\"evaluate\"},{\"fileName\":\"ParentRunner.java\",\"lineNumber\":220,\"className\":\"org.junit.runners.ParentRunner\",\"methodName\":\"run\"},{\"fileName\":\"JUnit4TestReference.java\",\"lineNumber\":50,\"className\":\"org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference\",\"methodName\":\"run\"},{\"fileName\":\"TestExecution.java\",\"lineNumber\":38,\"className\":\"org.eclipse.jdt.internal.junit.runner.TestExecution\",\"methodName\":\"run\"},{\"fileName\":\"RemoteTestRunner.java\",\"lineNumber\":467,\"className\":\"org.eclipse.jdt.internal.junit.runner.RemoteTestRunner\",\"methodName\":\"runTests\"},{\"fileName\":\"RemoteTestRunner.java\",\"lineNumber\":683,\"className\":\"org.eclipse.jdt.internal.junit.runner.RemoteTestRunner\",\"methodName\":\"runTests\"},{\"fileName\":\"RemoteTestRunner.java\",\"lineNumber\":390,\"className\":\"org.eclipse.jdt.internal.junit.runner.RemoteTestRunner\",\"methodName\":\"run\"},{\"fileName\":\"RemoteTestRunner.java\",\"lineNumber\":197,\"className\":\"org.eclipse.jdt.internal.junit.runner.RemoteTestRunner\",\"methodName\":\"main\"}],\"logArgs\":[\"a'r'g/\\\"0\",\"arg1\",\"arg2\"]}";
    JSONObject realtimeExceptionJSON = null; 
    JSONToRealtimeExceptionConverter jsonToRealtimeExceptionConverter = null;
    SimpleConverterSim simpleConverter = null;
    RealtimeException sessionInvalidException = null;
    String arg0="a'r'g/\"0", arg1="arg1", arg2="arg2";

    @Before
    public void setUp() throws Exception {
        realtimeExceptionJSON = new JSONObject(realtimeExceptionString);
        jsonToRealtimeExceptionConverter = new JSONToRealtimeExceptionConverter();
        simpleConverter = new SimpleConverterSim();
        simpleConverter.registerConverter(new JSONToStackTraceElementConverter());
        simpleConverter.registerConverter(new JSONToThrowableConverter());
    }

    @Test
    public void testGetOutputFormat() {
        assertEquals(RealtimeException.class.getSimpleName(), jsonToRealtimeExceptionConverter.getOutputFormat());
    }

    @Test
    public void testConvert() throws OXException {
        RealtimeException realtimeException = RealtimeException.class.cast(jsonToRealtimeExceptionConverter.convert(
            realtimeExceptionJSON,
            null,
            simpleConverter));
        assertEquals(1005, realtimeException.getCode());
        assertEquals("Your session is invalid.", realtimeException.getPlainLogMessage());
        Object[] logArgs = realtimeException.getLogArgs();
        assertEquals(3, logArgs.length);
        assertEquals(arg0, logArgs[0]);
        assertEquals(arg1, logArgs[1]);
        assertEquals(arg2, logArgs[2]);
        StackTraceElement[] stackTrace = realtimeException.getStackTrace();
        assertEquals(27, stackTrace.length);
        StackTraceElement firstElement = stackTrace[0];
        assertEquals("com.openexchange.exception.OXExceptionFactory",firstElement.getClassName());
        assertEquals("create", firstElement.getMethodName());
        StackTraceElement lastElement = stackTrace[26];
        assertEquals("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner",lastElement.getClassName());
        assertEquals("RemoteTestRunner.java", lastElement.getFileName());
        
        Throwable cause = realtimeException.getCause();
        assertEquals("First throwable", cause.getMessage());
        StackTraceElement[] causeStackTrace = cause.getStackTrace();
        assertEquals(24, causeStackTrace.length);

        StackTraceElement firstCauseElement = causeStackTrace[0];
        assertEquals("com.openexchange.realtime.atmosphere.payload.converter.RealtimeExceptionToJSONConverterTest", firstCauseElement.getClassName());
        assertEquals("RealtimeExceptionToJSONConverterTest.java", firstCauseElement.getFileName());
        assertEquals("setUp", firstCauseElement.getMethodName());
        assertEquals(83, firstCauseElement.getLineNumber());
        
        StackTraceElement lastCauseElement = causeStackTrace[23];
        assertEquals("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", lastCauseElement.getClassName());
        assertEquals("RemoteTestRunner.java", lastCauseElement.getFileName());
        assertEquals("main", lastCauseElement.getMethodName());
        assertEquals(197, lastCauseElement.getLineNumber());
        
    }

    @Test
    public void testGetInputFormat() {
        assertEquals("json", jsonToRealtimeExceptionConverter.getInputFormat());
    }

}
