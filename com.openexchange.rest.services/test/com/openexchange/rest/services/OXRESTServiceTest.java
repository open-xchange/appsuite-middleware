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

package com.openexchange.rest.services;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;

/**
 * {@link OXRESTServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXRESTServiceTest {
    // Methods about the request
    @Test
    public void testIsSet() {
        OXRESTService<Void> service = new OXRESTService<Void>();
        AJAXRequestData mock = mock(AJAXRequestData.class);
        service.setRequest(mock);
        
        when(mock.isSet("param")).thenReturn(true);
        when(mock.isSet("param2")).thenReturn(false);
        
        assertTrue(service.isSet("param"));
        assertFalse(service.isSet("param2"));
    }
    

    @Test
    public void testParamMethod() {
        OXRESTService<Void> service = new OXRESTService<Void>();
        AJAXRequestData mock = mock(AJAXRequestData.class);
        service.setRequest(mock);
        
        when(mock.getParameter("param")).thenReturn("value");
        
        assertEquals("value", service.param("param"));
    }
    
    @Test
    public void testParamTypeCoercion() throws OXException {
        OXRESTService<Void> service = new OXRESTService<Void>();
        AJAXRequestData mock = mock(AJAXRequestData.class);
        service.setRequest(mock);
        
        when(mock.getParameter("param", int.class)).thenReturn(12);
        
        assertEquals((Integer)12, service.param("param", int.class));
    }
    
    @Test
    public void testUrl() {
        OXRESTService<Void> service = new OXRESTService<Void>();
        AJAXRequestData mock = mock(AJAXRequestData.class);
        service.setRequest(mock);
        
        when(mock.constructURL("path", true)).thenReturn(new StringBuilder("constructedPath"));
        
        assertEquals("constructedPath", service.url("path"));
    }
    
    // Methods about the response
    // Higher level methods like #respond and #halt delegate to body, status and headers, so we
    // are only testing the lower level methods
    @Test
    public void testBody() throws JSONException {
        OXRESTService<Void> service = new OXRESTService<Void>();
        
        service.respond(Arrays.asList("Hello", "World"));
        Iterator<String> body = service.getResponse().getBody().iterator();
        
        assertEquals("Hello", body.next());
        assertEquals("World", body.next());
        assertFalse(body.hasNext());
        
        service.respond("Single String");
        assertEquals("Single String", service.getResponse().getBody().iterator().next());

        service.respond((Object) Arrays.asList("test", "test1", "test2"));
        JSONArray array = new JSONArray(service.getResponse().getBody().iterator().next());
        
        assertEquals(3, array.length());
        assertEquals("test", array.getString(0));
        assertEquals("test1", array.getString(1));
        assertEquals("test2", array.getString(2));
    }
    
    @Test
    public void testStatus() {
        OXRESTService<Void> service = new OXRESTService<Void>();
        service.status(404);
        
        assertEquals(404, service.getResponse().getStatus());
    }
    
    @Test
    public void testHeaders() {
        
    }
}
