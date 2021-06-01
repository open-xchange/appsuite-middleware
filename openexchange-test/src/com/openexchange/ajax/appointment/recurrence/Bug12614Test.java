/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment.recurrence;

import static org.junit.Assert.assertFalse;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * Tests if bug 12614 appears again.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12614Test extends AbstractAJAXSession {

    private AJAXClient boss;

    private AJAXClient secretary;

    private AJAXClient thirdUser;

    private TimeZone secTZ;

    private FolderObject sharedFolder;

    private Appointment series;

    private Appointment exception;

    /**
     * Default constructor.
     *
     * @param name test name.
     */
    public Bug12614Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        boss = getClient();
        secretary = testUser2.getAjaxClient();
        thirdUser = testContext.acquireUser().getAjaxClient();

        assertFalse("Could not acquire enough users", boss == null);
        assertFalse("Could not acquire enough users", secretary == null );
        assertFalse("Could not acquire enough users", thirdUser == null);

        secTZ = secretary.getValues().getTimeZone();
        bossSharesPrivateFolder();
        secretaryCreatesSeries();
        secretaryCreatesException();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(3).build();
    }

    @Test
    public void testDeleteException() throws Throwable {
        final DeleteRequest request = new DeleteRequest(exception.getObjectID(), exception.getParentFolderID(), exception.getLastModified(), false);
        final CommonDeleteResponse response = secretary.execute(request);
        final OXException e = response.getException();
        final String cause = e == null ? "" : e.toString();
        assertFalse("Secretary is not able to delete a change exception in a " + "shared folder: " + cause, response.hasError());
    }

    private void bossSharesPrivateFolder() throws OXException, IOException, JSONException {
        sharedFolder = new FolderObject(boss.getValues().getPrivateAppointmentFolder());
        sharedFolder.setModule(FolderObject.CALENDAR);
        final com.openexchange.ajax.folder.actions.GetRequest request = new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_OLD, sharedFolder.getObjectID(), new int[] { FolderObject.LAST_MODIFIED });
        final com.openexchange.ajax.folder.actions.GetResponse response = boss.execute(request);
        sharedFolder.setLastModified(response.getTimestamp());
        final OCLPermission perm1 = Create.ocl(boss.getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        final OCLPermission perm2 = Create.ocl(secretary.getValues().getUserId(), false, false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        sharedFolder.setPermissionsAsArray(new OCLPermission[] { perm1, perm2 });
        final com.openexchange.ajax.folder.actions.UpdateRequest request2 = new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, sharedFolder);
        final CommonInsertResponse response2 = boss.execute(request2);
        sharedFolder.setLastModified(response2.getTimestamp());
    }

    private void secretaryCreatesSeries() throws OXException, IOException, JSONException {
        series = new Appointment();
        series.setParentFolderID(sharedFolder.getObjectID());
        series.setTitle("test for bug 12614");
        final Calendar calendar = TimeTools.createCalendar(secTZ);
        series.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        series.setEndDate(calendar.getTime());
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setOccurrence(3);
        series.setParticipants(new Participant[] { new UserParticipant(boss.getValues().getUserId()), new UserParticipant(thirdUser.getValues().getUserId())
        });
        series.setIgnoreConflicts(true);
        final InsertRequest request = new InsertRequest(series, secTZ);
        final AppointmentInsertResponse response = secretary.execute(request);
        series.setObjectID(response.getId());
        series.setLastModified(response.getTimestamp());
    }

    private void secretaryCreatesException() throws OXException, IOException, JSONException {
        final int recurrence_position = 2;
        final GetRequest request = new GetRequest(sharedFolder.getObjectID(), series.getObjectID(), recurrence_position);
        final GetResponse response = secretary.execute(request);
        final Appointment occurrence = response.getAppointment(secTZ);
        exception = new Appointment();
        exception.setParentFolderID(occurrence.getParentFolderID());
        exception.setObjectID(occurrence.getObjectID());
        exception.setRecurrencePosition(occurrence.getRecurrencePosition());
        exception.setTitle("test for bug 12614 changed");
        exception.setLastModified(occurrence.getLastModified());
        final Calendar calendar = TimeTools.createCalendar(secTZ);
        calendar.setTime(occurrence.getEndDate());
        exception.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        exception.setEndDate(calendar.getTime());
        exception.setIgnoreConflicts(true);
        final UpdateRequest request2 = new UpdateRequest(exception, secTZ);
        final UpdateResponse response2 = secretary.execute(request2);
        exception.setLastModified(response2.getTimestamp());
        exception.setObjectID(response2.getId());
    }

}
