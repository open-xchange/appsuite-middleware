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

package com.openexchange.ajax.appointment.recurrence;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.xml.sax.SAXException;
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
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;

/**
 * Tests if bug 12595 appears again.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12595Test extends AbstractAJAXSession {

    private AJAXClient boss;

    private AJAXClient secretary;

    private AJAXClient thirdUser;

    private TimeZone secTZ;

    private FolderObject sharedFolder;

    private Appointment series;

    private Appointment exception;

    /**
     * Default constructor.
     * @param name test name.
     */
    public Bug12595Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        boss = getClient();
        secretary = new AJAXClient(User.User2);
        thirdUser = new AJAXClient(User.User3);
        secTZ = secretary.getValues().getTimeZone();
        sharePrivateFolder();
        createSeries();
        createException();
    }

    @Override
    protected void tearDown() throws Exception {
        deleteSeries();
        unsharePrivateFolder();
        thirdUser.logout();
        secretary.logout();
        super.tearDown();
    }

    public void testFindException() throws Throwable {
        final GetRequest request = new GetRequest(sharedFolder.getObjectID(),
            exception.getObjectID(), false);
        final GetResponse response = boss.execute(request);
        assertFalse("Change exception get lost.", response.hasError());
    }

    private void sharePrivateFolder() throws OXException, IOException,
        SAXException, JSONException {
        sharedFolder = new FolderObject(boss.getValues().getPrivateAppointmentFolder());
        sharedFolder.setModule(FolderObject.CALENDAR);
        final com.openexchange.ajax.folder.actions.GetRequest request =
            new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_OLD, sharedFolder
            .getObjectID(), new int[] { FolderObject.LAST_MODIFIED });
        final com.openexchange.ajax.folder.actions.GetResponse response =
            boss.execute(request);
        sharedFolder.setLastModified(response.getTimestamp());
        final OCLPermission perm1 = Create.ocl(boss.getValues().getUserId(),
            false, true,
            OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        final OCLPermission perm2 = Create.ocl(secretary.getValues().getUserId(),
            false, false,
            OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        sharedFolder.setPermissionsAsArray(new OCLPermission[] {
            perm1, perm2 });
        final com.openexchange.ajax.folder.actions.UpdateRequest request2 =
            new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, sharedFolder);
        final CommonInsertResponse response2 = boss.execute(request2);
        sharedFolder.setLastModified(response2.getTimestamp());
    }

    private void createSeries() throws OXException, IOException, SAXException,
        JSONException {
        series = new Appointment();
        series.setParentFolderID(sharedFolder.getObjectID());
        series.setTitle("test for bug 12595");
        final Calendar calendar = TimeTools.createCalendar(secTZ);
        series.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        series.setEndDate(calendar.getTime());
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setOccurrence(2);
        series.setParticipants(new Participant[] {
            new UserParticipant(boss.getValues().getUserId()),
            new UserParticipant(secretary.getValues().getUserId()),
            new UserParticipant(thirdUser.getValues().getUserId())
        });
        series.setIgnoreConflicts(true);
        final InsertRequest request = new InsertRequest(series, secTZ);
        final AppointmentInsertResponse response = secretary.execute(request);
        series.setObjectID(response.getId());
        series.setLastModified(response.getTimestamp());
    }

    private void createException() throws OXException, IOException,
        SAXException, JSONException, OXException {
        final GetRequest request = new GetRequest(sharedFolder.getObjectID(),
            series.getObjectID(), 2);
        final GetResponse response = secretary.execute(request);
        final Appointment occurrence = response.getAppointment(secTZ);
        exception = new Appointment();
        // TODO server gives private folder of secretary instead of boss' shared
        // folder.
        exception.setParentFolderID(sharedFolder.getObjectID());
        exception.setObjectID(occurrence.getObjectID());
        exception.setRecurrencePosition(occurrence.getRecurrencePosition());
        exception.setTitle("test for bug 12595 changed");
        exception.setLastModified(occurrence.getLastModified());
        final Calendar calendar = TimeTools.createCalendar(secTZ);
        calendar.setTime(occurrence.getEndDate());
        exception.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        exception.setEndDate(calendar.getTime());
        exception.setParticipants(new Participant[] {
            new UserParticipant(boss.getValues().getUserId()),
            new UserParticipant(secretary.getValues().getUserId())
        });
        exception.setIgnoreConflicts(true);
        final UpdateRequest request2 = new UpdateRequest(exception, secTZ);
        final UpdateResponse response2 = secretary.execute(request2);
        exception.setLastModified(response2.getTimestamp());
        exception.setObjectID(response2.getId());
    }

    private void deleteSeries() throws OXException, IOException, SAXException,
        JSONException {
        final GetRequest request = new GetRequest(series.getParentFolderID(),
            series.getObjectID());
        final GetResponse response = secretary.execute(request);
        final DeleteRequest request2 = new DeleteRequest(series.getObjectID(),
            series.getParentFolderID(), response.getTimestamp());
        secretary.execute(request2);
    }

    private void unsharePrivateFolder() throws OXException, IOException,
        SAXException, JSONException {
        final OCLPermission perm1 = Create.ocl(boss.getValues().getUserId(),
            false, true,
            OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        sharedFolder.setPermissionsAsArray(new OCLPermission[] {
            perm1 });
        final com.openexchange.ajax.folder.actions.UpdateRequest request =
            new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, sharedFolder);
        final CommonInsertResponse response = boss.execute(request);
        sharedFolder.setLastModified(response.getTimestamp());
    }
}
