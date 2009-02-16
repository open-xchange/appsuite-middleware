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

package com.openexchange.ajax.kata;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.UpdatesRequest;
import com.openexchange.ajax.appointment.action.UpdatesResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.api.OXConflictException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link AppointmentKata}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class AppointmentKata {
    
    private int folderId;
    private AJAXClient client;
    private CalendarTestManager manager;

    public AppointmentKata(int folderId, AJAXClient client) {
        this.folderId = folderId;
        this.client = client;
        this.manager = new CalendarTestManager(client);
    }
    
    public void performKata(AppointmentObject createMe, AppointmentObject update, AppointmentObject expectedResult) throws OXException, JSONException, AjaxException, IOException, SAXException {
        create( createMe );
        try {
            checkWithReadMethods( createMe );
            
            update( createMe, update);
            
            takeOverIdentity(update, expectedResult);
            
            checkWithReadMethods( expectedResult );
            
        } finally {
            delete( createMe );
            checkIsGone( createMe );
        }
        
    }

    private void create(AppointmentObject createMe) {
        createMe.setParentFolderID(folderId);
        manager.insertAppointmentOnServer( createMe );
    }

    private void update(AppointmentObject createMe, AppointmentObject update) {
        manager.updateAppointmentOnServer( update );
    }

    private void delete(AppointmentObject createMe) {
        manager.deleteAppointmentOnServer( createMe );
    }

    private void checkWithReadMethods(AppointmentObject appointment) throws OXException, JSONException, AjaxException, IOException, SAXException {
        checkViaGet( appointment );
        checkViaAll( appointment );
        checkViaList( appointment );
        checkViaUpdates( appointment );
    }
    
    private void checkViaGet(AppointmentObject appointment) throws OXException, JSONException {
        AppointmentObject loaded = manager.getAppointmentFromServer( appointment );
        compare(appointment, loaded);
    }

    private void checkViaAll(AppointmentObject appointment) throws AjaxException, IOException, SAXException, JSONException {
        Object[][] rows = getViaAll( appointment );
        
        checkInList(appointment, rows, AppointmentObject.ALL_COLUMNS);
    }

    private TimeZone getTimeZone() throws AjaxException, IOException, SAXException, JSONException {
        return client.getValues().getTimeZone();
    }

    private void checkViaList(AppointmentObject appointment) throws AjaxException, IOException, SAXException, JSONException {
        ListRequest listRequest = new ListRequest(ListIDs.l(new int[]{appointment.getParentFolderID(), appointment.getObjectID()}), AppointmentObject.ALL_COLUMNS);
        CommonListResponse response = client.execute( listRequest );
        
        Object[][] rows = response.getArray();
    
        checkInList(appointment, rows, AppointmentObject.ALL_COLUMNS);
    }

    private void checkViaUpdates(AppointmentObject appointment) throws AjaxException, IOException, SAXException, JSONException, OXConflictException {
        UpdatesRequest updates = new UpdatesRequest(folderId, AppointmentObject.ALL_COLUMNS, new Date(0), true);
        UpdatesResponse response = client.execute( updates );
        
        List<AppointmentObject>  appointments = response.getAppointments(getTimeZone());
        
        checkInList(appointment, appointments);
  
    }

    private void checkIsGone(AppointmentObject appointment) throws AjaxException, IOException, SAXException, JSONException {
        Object[][] rows = getViaAll( appointment );
        
        checkNotInList(appointment, rows, AppointmentObject.ALL_COLUMNS);
    }
    
    private Object[][] getViaAll(AppointmentObject appointment) throws AjaxException, IOException, SAXException, JSONException {
        long rangeStart = appointment.getStartDate().getTime()-1000;
        long rangeEnd = appointment.getEndDate().getTime()+1000;
        AllRequest all = new AllRequest(folderId, AppointmentObject.ALL_COLUMNS, new Date(rangeStart), new Date(rangeEnd) ,getTimeZone(), true);
        CommonAllResponse response = client.execute( all );
        return response.getArray();
    }

    private void takeOverIdentity(AppointmentObject orig, AppointmentObject newApp) {
        newApp.setObjectID( orig.getObjectID() );
        newApp.setParentFolderID( orig.getParentFolderID());
        newApp.setLastModified( orig.getLastModified());
    }
    
    private void compare(AppointmentObject appointment, AppointmentObject loaded) {
        int[] columns = AppointmentObject.ALL_COLUMNS;
        for (int i = 0; i < columns.length; i++) {
            int col = columns[i];
            if(appointment.contains(col)) {
                assertEquals(col+" differs!", appointment.get(col), loaded.get(col));
            }
        }
    }
    
    private void checkInList(AppointmentObject appointment, Object[][] rows, int[] columns) {
        int idPos = findIDIndex(columns);
        
        for (int i = 0; i < rows.length; i++) {
            Object[] row = rows[i];
            int id = (Integer) row[idPos];
            if(id == appointment.getObjectID()) {
                compare(appointment, row, columns);
                return;
            }
        }
        
        fail("Object not found in response");
        
    }
    
    private void checkNotInList(AppointmentObject appointment, Object[][] rows, int[] columns) {
        int idPos = findIDIndex(columns);
        
        for (int i = 0; i < rows.length; i++) {
            Object[] row = rows[i];
            int id = (Integer) row[idPos];
            if(id == appointment.getObjectID()) {
                fail("Object not found in response");
            }
        }
    }

    
    private void compare(AppointmentObject appointment, Object[] row, int[] columns) {
        for (int i = 0; i < columns.length; i++) {
            int column = columns[i];
            if(appointment.contains( column )) {
                Object expected = appointment.get( column );
                Object actual = row[i];
                assertEquals(expected, actual);
            }
        }
    }
    
    private void checkInList(AppointmentObject appointment, List<AppointmentObject> appointments) {
        for (AppointmentObject appointmentFromList : appointments) {
            if(appointmentFromList.getObjectID() == appointment.getObjectID()) {
                compare(appointment, appointmentFromList);
                return;
            }
        }
        
        fail("Object not found in response");
    }


    private int findIDIndex(int[] columns) {
        for (int i = 0; i < columns.length; i++) {
            if(columns[i] == AppointmentObject.OBJECT_ID) {
                return i;
            }
        }
        fail("No ID column requested. This won't work");
        return -1;
    }

    public void tearDown() {
        manager.cleanUp();
    }
}
