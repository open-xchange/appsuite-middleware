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

package com.openexchange.ajax.appointment;

import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link CalendarTestManagerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class CalendarTestManagerTest extends AbstractAJAXSession{

    private CalendarTestManager manager;

    /**
     * Initializes a new {@link CalendarTestManagerTest}.
     * @param name
     */
    public CalendarTestManagerTest(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        this.manager = new CalendarTestManager(getClient());
    }
    
    public void testCreate() throws Exception {
        AppointmentObject appointment = new AppointmentObject();
        appointment.setParentFolderID( getClient().getValues().getPrivateAppointmentFolder() );
        appointment.setTitle(getName());
        appointment.setStartDate(new Date());
        appointment.setEndDate(new Date());
        
        manager.insertAppointmentOnServer(appointment);
        
        assertExists( appointment );
        
        manager.cleanUp();
        
        assertDoesNotExist( appointment );
        
    }
    
    public void testRemove() throws Exception {
        AppointmentObject appointment = new AppointmentObject();
        appointment.setParentFolderID( getClient().getValues().getPrivateAppointmentFolder() );
        appointment.setTitle(getName());
        appointment.setStartDate(new Date());
        appointment.setEndDate(new Date());
        
        manager.insertAppointmentOnServer(appointment);
        
        assertExists( appointment );
        
        manager.deleteAppointmentOnServer(appointment);
        
        assertDoesNotExist(appointment);
    }
    
    public void assertExists(AppointmentObject appointment) {
        GetRequest get = new GetRequest(appointment.getParentFolderID(), appointment.getObjectID(), false);
        try {
            GetResponse response = getClient().execute(get);
            assertFalse(response.hasError());
        } catch (AjaxException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        } catch (SAXException e) {
            fail(e.toString());
        } catch (JSONException e) {
            fail(e.toString());
        }
    }
    
    public void assertDoesNotExist(AppointmentObject appointment) {
        GetRequest get = new GetRequest(appointment.getParentFolderID(), appointment.getObjectID(), false);
        try {
            GetResponse response = getClient().execute(get);
            assertTrue(response.hasError());
            assertTrue(response.getResponse().getErrorMessage().contains("not found.")); // Brittle
        } catch (AjaxException e) {
            fail(e.toString());
        } catch (IOException e) {
            fail(e.toString());
        } catch (SAXException e) {
            fail(e.toString());
        } catch (JSONException e) {
            fail(e.toString());
        }
    }

}
