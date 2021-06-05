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

import static com.openexchange.java.Autoboxing.I;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import org.junit.Before;
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
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class ManagedAppointmentTest extends AppointmentTest {

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

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        UserValues values = getClient().getValues();
        userTimeZone = values.getTimeZone();
        this.folder = ftm.generatePublicFolder("MAT_" + UUID.randomUUID().toString(), Module.CALENDAR.getFolderConstant(), values.getPrivateAppointmentFolder(), values.getUserId());
        this.folder = ftm.insertFolderOnServer(folder);

        int objectID = folder.getObjectID();
        this.negativeAssertionOnUpdate = new NegativeAssertionOnUpdate(catm, objectID);
        this.negativeAssertionOnCreate = new NegativeAssertionOnCreate(catm, objectID);
        this.negativeAssertionOnChangeException = new NegativeAssertionOnChangeException(catm, objectID);
        this.negativeAssertionOnDeleteException = new NegativeAssertionOnDeleteException(catm, objectID);
        this.positiveAssertionOnCreateAndUpdate = new PositiveAssertionOnCreateAndUpdate(catm, objectID);
        this.positiveAssertionOnCreate = new PositiveAssertionOnCreate(catm, objectID);
        this.positiveAssertionOnUpdate = new PositiveAssertionOnUpdateOnly(catm, objectID);
        this.positiveAssertionOnChangeException = new PositiveAssertionOnChangeException(catm, objectID);
        this.positiveAssertionOnDeleteException = new PositiveAssertionOnDeleteException(catm, objectID);
    }

    protected Appointment generateDailyAppointment() {
        Appointment app = AbstractAssertion.generateDefaultAppointment(folder.getObjectID());
        app.set(Appointment.RECURRENCE_TYPE, I(Appointment.DAILY));
        app.set(Appointment.INTERVAL, I(1));
        return app;
    }

    protected Appointment generateMonthlyAppointment() {
        Appointment app = AbstractAssertion.generateDefaultAppointment(folder.getObjectID());
        app.set(Appointment.RECURRENCE_TYPE, I(Appointment.MONTHLY));
        app.set(Appointment.INTERVAL, I(1));
        app.set(Appointment.DAY_IN_MONTH, I(1));
        return app;
    }

    protected Appointment generateYearlyAppointment() {
        Appointment app = AbstractAssertion.generateDefaultAppointment(folder.getObjectID());
        app.set(Appointment.RECURRENCE_TYPE, I(Appointment.YEARLY));
        app.set(Appointment.INTERVAL, I(1));
        app.set(Appointment.DAY_IN_MONTH, I(1));
        app.set(Appointment.MONTH, I(Calendar.JANUARY));
        return app;
    }

    protected Date D(String dateString) {
        return TimeTools.D(dateString);
    }

    protected Date D(String dateString, TimeZone tz) {
        return TimeTools.D(dateString, tz);
    }
}
