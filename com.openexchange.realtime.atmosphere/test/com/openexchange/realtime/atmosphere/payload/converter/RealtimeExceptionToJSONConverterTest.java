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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.atmosphere.payload.converter;

import static org.junit.Assert.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.payload.converter.sim.SimpleConverterSim;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;

/**
 * {@link RealtimeExceptionToJSONConverterTest}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RealtimeExceptionToJSONConverterTest {

    RealtimeExceptionToJSONConverter realtimeExceptionToJSONConverter = null;

    SimpleConverterSim simpleConverter = null;

    Throwable throwable = null;

    RealtimeException sessionInvalidException = null;

    String arg0 = "a'r'g/\"0", arg1 = "arg1", arg2 = "arg2";

    @Before
    public void setUp() throws Exception {
        realtimeExceptionToJSONConverter = new RealtimeExceptionToJSONConverter();
        simpleConverter = new SimpleConverterSim();
        simpleConverter.registerConverter(new StackTraceElementToJSONConverter());
        simpleConverter.registerConverter(new ThrowableToJSONConverter());
        throwable = new Throwable("First throwable");
        sessionInvalidException = RealtimeExceptionCodes.SESSION_INVALID.create(throwable, arg0, arg1, arg2);
    }

    @Test
    public void testGetInputFormat() {
        assertEquals(RealtimeException.class.getSimpleName(), realtimeExceptionToJSONConverter.getInputFormat());
    }

    @Test
    public void testConvert() throws OXException, JSONException {
        Object object = realtimeExceptionToJSONConverter.convert(sessionInvalidException, null, simpleConverter);
        assertNotNull(object);
        assertTrue(object instanceof JSONObject);
        JSONObject realtimeExceptionJSON = JSONObject.class.cast(object);
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
        assertEquals(24, causeTrace.length());

        JSONObject firstCauseStackTraceElement = (JSONObject) stackTrace.get(0);
        assertEquals("OXExceptionFactory.java", firstCauseStackTraceElement.getString("fileName"));
        assertEquals("com.openexchange.exception.OXExceptionFactory", firstCauseStackTraceElement.getString("className"));
        assertEquals("create", firstCauseStackTraceElement.getString("methodName"));

        JSONObject lastCauseStackTraceElement = (JSONObject) stackTrace.get(23);
        assertEquals("RemoteTestRunner.java", lastCauseStackTraceElement.getString("fileName"));
        assertEquals("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", lastCauseStackTraceElement.getString("className"));
    }

    @Test
    public void testGetOutputFormat() {
        assertEquals("json", realtimeExceptionToJSONConverter.getOutputFormat());
    }

}
