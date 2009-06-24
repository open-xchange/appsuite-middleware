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

package com.openexchange.test;

import static junit.framework.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link CalendarTestManager}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CalendarTestManager {

    private AJAXClient client;

    private List<Appointment> createdEntities = new ArrayList<Appointment>();

    private TimeZone timezone;

    public CalendarTestManager(AJAXClient client) {
        this.client = client;

        try {
            timezone = client.getValues().getTimeZone();
        } catch (AjaxException e) {
        } catch (IOException e) {
        } catch (SAXException e) {
        } catch (JSONException e) {
        } finally {
            if (timezone == null) {
                timezone = TimeZone.getTimeZone("Europe/Berlin");
            }
        }
    }

    public void insertAppointmentOnServer(Appointment appointment) {
        InsertRequest insertRequest = new InsertRequest(appointment, timezone);
        AppointmentInsertResponse insertResponse = execute(insertRequest);

        createdEntities.add(appointment);
        insertResponse.fillAppointment(appointment);
    }

    public void deleteAppointmentOnServer(Appointment appointment, boolean failOnError) {
        createdEntities.remove(appointment);
        DeleteRequest deleteRequest = new DeleteRequest(
            appointment.getObjectID(),
            appointment.getParentFolderID(),
            new Date(Long.MAX_VALUE),
            failOnError);
        execute(deleteRequest);
    }

    public void deleteAppointmentOnServer(Appointment appointment) {
        createdEntities.remove(appointment);
        appointment.setLastModified(new Date(Long.MAX_VALUE));
        DeleteRequest deleteRequest = new DeleteRequest(appointment);
        execute(deleteRequest);
    }

    public void cleanUp() {
        for (Appointment appointment : new ArrayList<Appointment>(createdEntities)) {
            deleteAppointmentOnServer(appointment);
        }
    }

    private <T extends AbstractAJAXResponse> T execute(final AJAXRequest<T> request) {
        try {
            return client.execute(request);
        } catch (AjaxException e) {
            fail("AjaxException during task creation: " + e.getLocalizedMessage());
        } catch (IOException e) {
            fail("IOException during task creation: " + e.getLocalizedMessage());
        } catch (SAXException e) {
            fail("SAXException during task creation: " + e.getLocalizedMessage());
        } catch (JSONException e) {
            fail("JsonException during task creation: " + e.getLocalizedMessage());
        }
        return null;
    }

    public int getPrivateFolder() throws AjaxException, IOException, SAXException, JSONException {
        return client.getValues().getPrivateAppointmentFolder();
    }

    /**
     * @param parentFolderID
     * @param objectID
     * @return
     * @throws JSONException
     * @throws OXException
     */
    public Appointment getAppointmentFromServer(int parentFolderID, int objectID) throws OXException, JSONException {
        GetRequest get = new GetRequest(parentFolderID, objectID);
        GetResponse response = execute(get);

        return response.getAppointment(timezone);
    }

    public Appointment getAppointmentFromServer(Appointment appointment) throws OXException, JSONException {
        GetRequest get = new GetRequest(appointment);
        GetResponse response = execute(get);

        return response.getAppointment(timezone);
    }
    
    public Appointment getAppointmentFromServer(Appointment appointment, boolean failOnError) throws OXException, JSONException {
        try {
            GetRequest get = new GetRequest(appointment.getParentFolderID(), appointment.getObjectID(), failOnError);
            GetResponse response = execute(get);        
            return response.getAppointment(timezone);
        } catch (OXException e){
            if(failOnError )
                throw e;
            return null;
        }
    }
    
    public Appointment getAppointmentFromServer(int parentFolderID, int objectID, boolean failOnError) throws OXException, JSONException {
        try {
            GetRequest get = new GetRequest(parentFolderID, objectID, failOnError);
            GetResponse response = execute(get);        
            return response.getAppointment(timezone);
        } catch (OXException e){
            if(failOnError )
                throw e;
            return null;
        }
    }
    
    /**
     * @param appointment
     * @return
     */
    public Appointment createIdentifyingCopy(Appointment appointment) {
        Appointment copy = new Appointment();
        copy.setObjectID(appointment.getObjectID());
        copy.setParentFolderID(appointment.getParentFolderID());
        copy.setLastModified(appointment.getLastModified());
        return copy;
    }

    public void updateAppointmentOnServer(Appointment updatedAppointment) {
        UpdateRequest updateRequest = new UpdateRequest(updatedAppointment, timezone);
        UpdateResponse updateResponse = execute(updateRequest);
        updatedAppointment.setLastModified(updateResponse.getTimestamp());
        for (Appointment createdAppoinment : createdEntities) {
            if (createdAppoinment.getObjectID() == updatedAppointment.getObjectID()) {
                createdAppoinment.setLastModified(updatedAppointment.getLastModified());
                continue;
            }
        }
    }

    /**
     * @param parentFolderID
     * @return
     */
    public Appointment[] getAllAppointmentsOnServer(int parentFolderID, Date start, Date end) {
        AllRequest request = new AllRequest(parentFolderID, Appointment.ALL_COLUMNS, start, end, timezone);
        CommonAllResponse response = execute(request);

        List<Appointment> appointments = new ArrayList<Appointment>();

        for (Object[] row : response.getArray()) {
            Appointment app = new Appointment();
            appointments.add(app);
            for (int i = 0; i < row.length; i++) {
                if (row[i] == null) {
                    continue;
                }
                if (Appointment.ALL_COLUMNS[i] == Appointment.LAST_MODIFIED_UTC) {
                    continue;
                }
                try {
                    app.set(Appointment.ALL_COLUMNS[i], row[i]);
                } catch (ClassCastException x) {
                    if (x.getMessage().equals("java.lang.Long")) {
                        if (!tryDate(app, Appointment.ALL_COLUMNS[i], (Long) row[i])) {
                            tryInteger(app, Appointment.ALL_COLUMNS[i], (Long) row[i]);
                        }
                    }
                }
            }
        }

        return appointments.toArray(new Appointment[appointments.size()]);
    }

    private boolean tryInteger(Appointment app, int field, Long value) {
        try {
            app.set(field, new Integer(value.intValue()));
            return true;
        } catch (ClassCastException x) {
            return false;
        }
    }

    private boolean tryDate(Appointment app, int field, Long value) {
        try {
            app.set(field, new Date(value));
            return true;
        } catch (ClassCastException x) {
            return false;
        }
    }

    public void clearFolder(int folderId, Date start, Date end) {
        for (Appointment app : getAllAppointmentsOnServer(folderId, start, end)) {
            deleteAppointmentOnServer(app);
        }
    }
}
