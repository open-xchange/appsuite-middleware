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

package com.openexchange.groupware.calendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.request.AppointmentRequest;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.search.AppointmentSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link SlowCalendarTests}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SlowCalendarTests extends AbstractCalendarTest {
    // Bug #11148
    public void testShouldSurviveLoadingInvalidPattern() throws Exception {
        final CalendarDataObject cdao = appointments.buildRecurringAppointment();

        appointments.save(cdao);
        clean.add(cdao);

        invalidatePattern(cdao);

        try {
            final CalendarDataObject reloaded = appointments.reload(cdao);
            assertTrue(reloaded.getRecurrenceType() == CalendarDataObject.NO_RECURRENCE);
            assertTrue(reloaded.getEndDate().getTime() < System.currentTimeMillis() + 240 * 3600000);
            // Check load by list requests

            final AppointmentSQLInterface sqlInterface = appointments.getCurrentAppointmentSQLInterface();
            final Date SUPER_START = new Date(-2);
            final Date SUPER_END = new Date(Long.MAX_VALUE);
            final int[] COLS = new int[] {
                CalendarDataObject.OBJECT_ID, CalendarDataObject.START_DATE, CalendarDataObject.END_DATE, CalendarDataObject.FOLDER_ID,
                CalendarDataObject.USERS };
            final int USER_ID = userId;
            final int FOLDER_ID = cdao.getParentFolderID();
            SearchIterator iter = sqlInterface.getActiveAppointments(USER_ID, SUPER_START, SUPER_END, COLS);
            assertContains(iter, cdao);

            iter = sqlInterface.getAppointmentsBetween(USER_ID, SUPER_START, SUPER_END, COLS, Appointment.OBJECT_ID, null);
            assertContains(iter, cdao);

            iter = sqlInterface.getAppointmentsBetweenInFolder(FOLDER_ID, COLS, SUPER_START, SUPER_END, Appointment.OBJECT_ID, null);
            assertContains(iter, cdao);

            final AppointmentSearchObject search = new AppointmentSearchObject();
            search.setFolderIDs(Collections.singleton(Integer.valueOf(cdao.getParentFolderID())));
            search.setQueries(Collections.singleton("*"));
            iter = sqlInterface.searchAppointments(search, Appointment.OBJECT_ID, null, COLS);
            assertContains(iter, cdao);

            iter = sqlInterface.getFreeBusyInformation(USER_ID, Participant.USER, SUPER_START, SUPER_END);
            assertContains(iter, cdao);

            iter = sqlInterface.getModifiedAppointmentsBetween(
                USER_ID,
                SUPER_START,
                SUPER_END,
                COLS,
                SUPER_START,
                Appointment.OBJECT_ID,
                Order.NO_ORDER);
            assertContains(iter, cdao);

            iter = sqlInterface.getObjectsById(new int[][] { { cdao.getObjectID(), cdao.getParentFolderID() } }, COLS);
            assertContains(iter, cdao);
            final AppointmentSearchObject searchObj = new AppointmentSearchObject();
            searchObj.setQueries(Collections.singleton("*"));
            searchObj.setFolderIDs(Collections.singleton(Integer.valueOf(cdao.getParentFolderID())));

            iter = sqlInterface.searchAppointments(searchObj, Appointment.OBJECT_ID, null, COLS);
            assertContains(iter, cdao);

            sqlInterface.hasAppointmentsBetween(SUPER_START, new Date(SUPER_START.getTime() + 3600000L * 24 * 30));

            // Check AppointmentRequest interface methods

            final StringBuilder cols = new StringBuilder();
            for (final int col : COLS) {
                cols.append(col).append(',');
            }
            cols.setLength(cols.length() - 1);
            final String COLS_STRING = cols.toString();

            // ALL
            final AppointmentRequest req = new AppointmentRequest(ServerSessionAdapter.valueOf(session));
            JSONObject requestData = json(
                AJAXServlet.PARAMETER_COLUMNS,
                COLS_STRING,
                AJAXServlet.PARAMETER_FOLDERID,
                String.valueOf(cdao.getParentFolderID()),
                AJAXServlet.PARAMETER_START,
                String.valueOf(SUPER_START.getTime()),
                AJAXServlet.PARAMETER_END,
                String.valueOf(SUPER_END.getTime()));
            JSONArray arr = req.actionAll(requestData);
            assertContains(arr, cdao);

            // Recurrence Master
            requestData.put(AppointmentRequest.RECURRENCE_MASTER, true);
            arr = req.actionAll(requestData);
            assertContains(arr, cdao);

            // Freebusy
            arr = req.actionFreeBusy(json(
                AJAXServlet.PARAMETER_ID,
                Integer.toString(userId),
                "type",
                Integer.toString(Participant.USER),
                AJAXServlet.PARAMETER_START,
                String.valueOf(SUPER_START.getTime()),
                AJAXServlet.PARAMETER_END,
                String.valueOf(SUPER_END.getTime())));
            assertContainsAsJSONObject(arr, cdao);

            // Get
            final JSONObject loaded = req.actionGet(json(
                AJAXServlet.PARAMETER_ID,
                Integer.toString(cdao.getObjectID()),
                AJAXServlet.PARAMETER_FOLDERID,
                Integer.toString(cdao.getParentFolderID())));
            assertEquals(loaded.getInt("id"), cdao.getObjectID());

            // Has
            req.actionHas(json(
                AJAXServlet.PARAMETER_START,
                String.valueOf(SUPER_START.getTime()),
                AJAXServlet.PARAMETER_END,
                String.valueOf(SUPER_START.getTime() + 3600000L * 24 * 30)));

            // List
            final JSONArray idArray = new JSONArray();
            idArray.put(json(AJAXServlet.PARAMETER_ID, cdao.getObjectID(), AJAXServlet.PARAMETER_FOLDERID, cdao.getParentFolderID()));
            final JSONObject jsonRequest = json(AJAXServlet.PARAMETER_COLUMNS, COLS_STRING, AJAXServlet.PARAMETER_DATA, idArray);
            arr = req.actionList(jsonRequest);
            assertContains(arr, cdao);
            // Recurrence Master
            jsonRequest.put(AppointmentRequest.RECURRENCE_MASTER, true);
            arr = req.actionList(jsonRequest);
            assertContains(arr, cdao);

            // New Appointments Search
            arr = req.actionNewAppointmentsSearch(json(
                AJAXServlet.PARAMETER_COLUMNS,
                COLS_STRING,
                AJAXServlet.PARAMETER_START,
                String.valueOf(SUPER_START.getTime()),
                AJAXServlet.PARAMETER_END,
                String.valueOf(SUPER_END.getTime()),
                "limit",
                Integer.MAX_VALUE));
            assertContains(arr, cdao);

            // Search
            requestData = json(AJAXServlet.PARAMETER_COLUMNS, COLS_STRING, AJAXServlet.PARAMETER_DATA, json(
                SearchFields.PATTERN,
                "*",
                AJAXServlet.PARAMETER_INFOLDER,
                cdao.getParentFolderID()), AJAXServlet.PARAMETER_SORT, Appointment.START_DATE, AJAXServlet.PARAMETER_ORDER, "ASC");
            arr = req.actionSearch(requestData);
            assertContains(arr, cdao);

            // With start and end date
            requestData.put(AJAXServlet.PARAMETER_START, SUPER_START.getTime());
            requestData.put(AJAXServlet.PARAMETER_END, SUPER_END.getTime());
            arr = req.actionSearch(requestData);
            assertContains(arr, cdao);

            // Recurrence Master
            requestData.remove(AJAXServlet.PARAMETER_START);
            requestData.remove(AJAXServlet.PARAMETER_END);
            requestData.put(AppointmentRequest.RECURRENCE_MASTER, true);

            arr = req.actionSearch(requestData);
            assertContains(arr, cdao);

            requestData.put(AJAXServlet.PARAMETER_START, SUPER_START.getTime());
            requestData.put(AJAXServlet.PARAMETER_END, SUPER_END.getTime());
            arr = req.actionSearch(requestData);
            assertContains(arr, cdao);

            // Updates

            requestData = json(
                AJAXServlet.PARAMETER_COLUMNS,
                COLS_STRING,
                AJAXServlet.PARAMETER_START,
                String.valueOf(SUPER_START.getTime()),
                AJAXServlet.PARAMETER_END,
                String.valueOf(SUPER_END.getTime()),
                AJAXServlet.PARAMETER_TIMESTAMP,
                0,
                AJAXServlet.PARAMETER_FOLDERID,
                cdao.getParentFolderID());
            arr = req.actionUpdates(requestData);
            assertContains(arr, cdao);

            // Recurrence Master
            requestData.put(AppointmentRequest.RECURRENCE_MASTER, true);
            arr = req.actionUpdates(requestData);
            assertContains(arr, cdao);

        } catch (final Exception x) {
            x.printStackTrace();
            fail(x.toString());
        }

    }

    private void invalidatePattern(final CalendarDataObject cdao) throws OXException {
        Connection con = null;
        PreparedStatement pstmt = null;

        final String invalidPattern = "t|6|i|1|a|32|b|21|c|3|s|" + (System.currentTimeMillis() + 240 * 3600000) + "|";
        try {
            con = Database.get(ctx, true);
            pstmt = con.prepareStatement("UPDATE prg_dates SET field06 = ? WHERE intfield01 = ?");
            pstmt.setString(1, invalidPattern);
            pstmt.setInt(2, cdao.getObjectID());
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (final SQLException e) {
                    // IGNORE
                }
            }
            if (con != null) {
                Database.back(ctx, true, con);
            }
        }
    }

}
