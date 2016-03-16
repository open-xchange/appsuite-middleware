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

package com.openexchange.ajax.importexport;

import java.io.StringReader;
import java.util.Calendar;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.ICalExportRequest;
import com.openexchange.ajax.importexport.actions.ICalExportResponse;
import com.openexchange.data.conversion.ical.Assert;
import com.openexchange.data.conversion.ical.ICalFile;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11871Test extends AbstractAJAXSession {

    /**
     * Default constructor.
     * @param name test name
     */
    public Bug11871Test(final String name) {
        super(name);
    }

    /**
     * Creates a daily appointment over several days and checks if the DTEND
     * contains the end of the first occurrence and not the end of the series.
     */
    public void testDtEndNotOnSeriesEnd() throws Throwable {
        final AJAXClient myClient = getClient();
        final int folderId = myClient.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = myClient.getValues().getTimeZone();
        final FolderObject folder = Create.createPrivateFolder("Bug 11871 test folder " + System.currentTimeMillis(),
            FolderObject.CALENDAR, myClient.getValues().getUserId());
        {
            folder.setParentFolderID(folderId);
            final CommonInsertResponse response = Executor.execute(myClient,
                new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
            folder.setObjectID(response.getId());
            folder.setLastModified(response.getTimestamp());
        }
        final Appointment appointment = new Appointment();
        final Calendar calendar = Calendar.getInstance(tz);
        {
            appointment.setTitle("Bug 11871 test appointment");
            appointment.setParentFolderID(folder.getObjectID());
            appointment.setIgnoreConflicts(true);
            appointment.setRecurrenceType(Appointment.DAILY);
            appointment.setInterval(1);
            appointment.setRecurrenceCount(5);
            appointment.setTimezone(tz.getID());

            calendar.set(Calendar.YEAR, 2008);
            calendar.set(Calendar.MONTH, Calendar.JULY);
            calendar.set(Calendar.DAY_OF_MONTH, 26);
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            appointment.setStartDate(calendar.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, 19);
            appointment.setEndDate(calendar.getTime());
            calendar.set(Calendar.MONTH, Calendar.AUGUST);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            appointment.setUntil(calendar.getTime());
            final InsertRequest request = new InsertRequest(appointment, tz);
            final CommonInsertResponse response = Executor.execute(myClient, request);
            appointment.setObjectID(response.getId());
            appointment.setLastModified(response.getTimestamp());
        }
        try {
            final ICalExportResponse response = myClient.execute(new ICalExportRequest(folder.getObjectID()));
            final ICalFile ical = new ICalFile(new StringReader(response.getICal()));
            Assert.assertStandardAppFields(ical, appointment.getStartDate(), appointment.getEndDate(), tz);
        } finally {
            Executor.execute(myClient, new DeleteRequest(appointment.getObjectID(),
                appointment.getParentFolderID(), appointment.getLastModified()));
            Executor.execute(myClient, new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(),
                folder.getLastModified()));
        }
    }
}
