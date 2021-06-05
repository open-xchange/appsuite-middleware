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

package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link CreateExceptionWithBadDate}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class CreateExceptionWithBadDate extends AbstractAJAXSession {

    private Appointment series;
    private Appointment exception;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        series = new Appointment();
        series.setTitle("Bug 48165 Test - series");
        series.setStartDate(TimeTools.D("01.08.2016 09:00"));
        series.setEndDate(TimeTools.D("01.08.2016 10:00"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setIgnoreConflicts(true);
        series.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        catm.insert(series);

        exception = new Appointment();
        exception.setObjectID(series.getObjectID());
        exception.setParentFolderID(series.getParentFolderID());
        exception.setLastModified(series.getLastModified());
        exception.setRecurrencePosition(3);
        exception.setIgnoreConflicts(false);
    }

    @Test
    public void testExcplicitWrongEndDate() {
        exception.setStartDate(TimeTools.D("03.08.2016 11:00"));
        exception.setEndDate(TimeTools.D("03.08.2016 09:00"));
        catm.update(exception);
        assertTrue("Expected error.", catm.getLastResponse().hasError());
        OXException e = catm.getLastResponse().getException();
        assertTrue("Wrong exception.", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
    }

    @Test
    public void testImplicitWrongEndDate() {
        exception.setStartDate(TimeTools.D("03.08.2016 11:00"));
        catm.update(exception);
        assertTrue("Expected error.", catm.getLastResponse().hasError());
        OXException e = catm.getLastResponse().getException();
        assertTrue("Wrong exception.", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
    }

    @Test
    public void testExcplicitWrongStartDate() {
        exception.setStartDate(TimeTools.D("03.08.2016 11:00"));
        exception.setEndDate(TimeTools.D("03.08.2016 07:00"));
        catm.update(exception);
        assertTrue("Expected error.", catm.getLastResponse().hasError());
        OXException e = catm.getLastResponse().getException();
        assertTrue("Wrong exception.", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
    }

    @Test
    public void testImplicitWrongStartDate() {
        exception.setEndDate(TimeTools.D("03.08.2016 07:00"));
        catm.update(exception);
        assertTrue("Expected error.", catm.getLastResponse().hasError());
        OXException e = catm.getLastResponse().getException();
        assertTrue("Wrong exception.", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
    }
}
