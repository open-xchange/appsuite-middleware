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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.helper.AbstractAssertion;
import com.openexchange.ajax.appointment.helper.AbstractPositiveAssertion;
import com.openexchange.ajax.appointment.helper.NegativeAssertionOnCreate;
import com.openexchange.ajax.appointment.helper.NegativeAssertionOnUpdate;
import com.openexchange.ajax.appointment.helper.PositiveAssertionOnCreate;
import com.openexchange.ajax.appointment.helper.PositiveAssertionOnCreateAndUpdate;
import com.openexchange.ajax.appointment.helper.PositiveAssertionOnDeleteException;
import com.openexchange.ajax.appointment.helper.PositiveAssertionOnUpdateOnly;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.ResourceTestManager;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class ManagedAppointmentTest extends AppointmentTest {

    protected CalendarTestManager calendarManager;

    protected FolderTestManager folderManager;

    protected ResourceTestManager resourceManager;

    protected FolderObject folder;

    protected TimeZone utc = TimeZone.getTimeZone("UTC");

    protected TimeZone userTimeZone;

    protected NegativeAssertionOnUpdate negativeAssertionOnUpdate;

    protected NegativeAssertionOnCreate negativeAssertionOnCreate;

    protected NegativeAssertionOnChangeException negativeAssertionOnChangeException;

    protected NegativeAssertionOnDeleteException negativeAssertionOnDeleteException;

    protected AbstractPositiveAssertion positiveAssertionOnCreate;

    protected PositiveAssertionOnCreateAndUpdate positiveAssertionOnCreateAndUpdate;

    protected PositiveAssertionOnUpdateOnly positiveAssertionOnUpdate;

    protected PositiveAssertionOnChangeException positiveAssertionOnChangeException;

    protected PositiveAssertionOnDeleteException positiveAssertionOnDeleteException;

    public ManagedAppointmentTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        calendarManager = new CalendarTestManager(getClient());
        folderManager = new FolderTestManager(getClient());
        resourceManager = new ResourceTestManager(getClient());
        UserValues values = getClient().getValues();
        userTimeZone = values.getTimeZone();
        this.folder = folderManager.generatePublicFolder(
            "MAT_"+ (new Date()).getTime(),
            Module.CALENDAR.getFolderConstant(),
            values.getPrivateAppointmentFolder(),
            values.getUserId());
        folder = folderManager.insertFolderOnServer(folder);

        this.negativeAssertionOnUpdate = new NegativeAssertionOnUpdate(calendarManager, folder.getObjectID());
        this.negativeAssertionOnCreate = new NegativeAssertionOnCreate(calendarManager, folder.getObjectID());
        this.negativeAssertionOnChangeException = new NegativeAssertionOnChangeException(calendarManager, folder.getObjectID());
        this.negativeAssertionOnDeleteException = new NegativeAssertionOnDeleteException(calendarManager, folder.getObjectID());
        this.positiveAssertionOnCreateAndUpdate = new PositiveAssertionOnCreateAndUpdate(calendarManager, folder.getObjectID());
        this.positiveAssertionOnCreate = new PositiveAssertionOnCreate(calendarManager, folder.getObjectID());
        this.positiveAssertionOnUpdate = new PositiveAssertionOnUpdateOnly(calendarManager, folder.getObjectID());
        this.positiveAssertionOnChangeException = new PositiveAssertionOnChangeException(calendarManager, folder.getObjectID());
        this.positiveAssertionOnDeleteException = new PositiveAssertionOnDeleteException(calendarManager, folder.getObjectID());

    }

    @Override
    protected void tearDown() throws Exception {
        try {
            calendarManager.cleanUp();
        } finally {
            try {
                folderManager.cleanUp();
            } finally {
                try {
                    resourceManager.cleanUp();
                } finally {
                    super.tearDown();
                }
            }
        }

    }

    protected Appointment generateDailyAppointment() {
        Appointment app = AbstractAssertion.generateDefaultAppointment(folder.getObjectID());
        app.set(Appointment.RECURRENCE_TYPE, Appointment.DAILY);
        app.set(Appointment.INTERVAL, 1);
        return app;
    }

    protected Appointment generateMonthlyAppointment() {
        Appointment app = AbstractAssertion.generateDefaultAppointment(folder.getObjectID());
        app.set(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        app.set(Appointment.INTERVAL, 1);
        app.set(Appointment.DAY_IN_MONTH, 1);
        return app;
    }

    protected Appointment generateYearlyAppointment() {
        Appointment app = AbstractAssertion.generateDefaultAppointment(folder.getObjectID());
        app.set(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        app.set(Appointment.INTERVAL, 1);
        app.set(Appointment.DAY_IN_MONTH, 1);
        app.set(Appointment.MONTH, Calendar.JANUARY);
        return app;
    }

    protected Date D(String dateString){
    	return TimeTools.D(dateString);
    }

    protected Date D(String dateString, TimeZone tz){
    	return TimeTools.D(dateString,tz);
    }
}
