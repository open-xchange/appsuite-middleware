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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForModifyingChangeExceptions extends ManagedAppointmentTest {

    private final int exceptionPosition = 2;

    private Changes changes;

    private Appointment update;

    private Appointment app;

    public TestsForModifyingChangeExceptions(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        app = generateDailyAppointment();
        app.setOccurrence(3);

        calendarManager.insert(app);

        changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, exceptionPosition);
        changes.put(Appointment.START_DATE, D("2/1/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("2/1/2008 2:00", utc));

        update = app.clone();
        changes.update(update);
        calendarManager.update(update);

    }

    public void testShouldNotAllowTurningAChangeExceptionIntoASeries() {
        Changes secondChange = new Changes();
        secondChange.put(Appointment.RECURRENCE_TYPE, Appointment.DAILY);
        secondChange.put(Appointment.INTERVAL, 1);

        Appointment secondUpdate = new Appointment();
        secondUpdate.setParentFolderID(update.getParentFolderID());
        secondUpdate.setObjectID(update.getObjectID());
        secondUpdate.setLastModified(update.getLastModified());
        secondChange.update(secondUpdate);

        calendarManager.update(secondUpdate);

        assertTrue("Should get exception when trying to make a change exception a series", calendarManager.hasLastException());
        int code = ((OXException) calendarManager.getLastException()).getCode();
        assertTrue("Should have correct exception", 99 == code || 4035 == code);
    }

    public void testDeletingAChangeException(){
        Appointment secondUpdate = new Appointment();
        secondUpdate.setParentFolderID(update.getParentFolderID());
        secondUpdate.setObjectID(update.getObjectID());
        secondUpdate.setLastModified(update.getLastModified());
        secondUpdate.setRecurrencePosition(exceptionPosition);
        secondUpdate.setRecurrenceType(Appointment.MONTHLY);

        calendarManager.delete(secondUpdate);

        assertFalse("Should get no error when trying to delete a change exception", calendarManager.hasLastException());
    }
}
