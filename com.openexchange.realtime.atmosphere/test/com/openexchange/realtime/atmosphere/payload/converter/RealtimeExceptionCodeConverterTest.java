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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.DefaultConverter;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.exception.OXException;
import com.openexchange.java.JSON;
import com.openexchange.realtime.atmosphere.presence.converter.JSONToPresenceStateConverter;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link RealtimeExceptionCodeConverterTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RealtimeExceptionCodeConverterTest {

    @Test
    public void test() throws OXException {
        StackTraceElementToJSONConverter stackTraceElementToJSON = new StackTraceElementToJSONConverter();
        ThrowableToJSONConverter throwableToJSON = new ThrowableToJSONConverter();
        RealtimeExceptionToJSONConverter realtimeExceptiontoJSON = new RealtimeExceptionToJSONConverter();
        
        JSONToStackTraceElementConverter jsonToStackTraceElement = new JSONToStackTraceElementConverter();
        JSONToThrowableConverter jsonToThrowableConverter = new JSONToThrowableConverter();
        JSONToRealtimeExceptionConverter fromJSON = new JSONToRealtimeExceptionConverter();
        
        RealtimeException realtimeException = RealtimeExceptionCodes.SESSION_INVALID.create();
        Throwable t = new Throwable("Throwable message");
        JSONObject jsonThrowable = (JSONObject)throwableToJSON.convert(t, null, adaptPOJOConverter(stackTraceElementToJSON));
    }

    private SimpleConverter adaptPOJOConverter(final AbstractPOJOConverter pojoConverter) {
        return new SimpleConverter() {
            
            @Override
            public Object convert(String from, String to, Object data, ServerSession session) throws OXException {
                return pojoConverter.convert(data, null, null);
            }
        };
    }
    
    private SimpleConverter adaptJSONConverter(final AbstractJSONConverter jsonConverter) {
        return new SimpleConverter() {
            
            @Override
            public Object convert(String from, String to, Object data, ServerSession session) throws OXException {
                return jsonConverter.convert(data, null, null);
            }
        };
    }
    
    @Test
    public void testJSON() throws JSONException {
        JSONObject json = new JSONObject();
        String nullString = null;
        json.put("nullKey",JSONObject.NULL);
        Object object = json.get("nullKey");
    }
}
