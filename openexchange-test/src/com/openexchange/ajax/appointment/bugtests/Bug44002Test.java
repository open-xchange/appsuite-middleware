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

package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug44002Test}
 * 
 * Ui change (don't send all appointment fields on update but only real changes) revealed this bug.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug44002Test extends AbstractAJAXSession {

    private Appointment conflict;
    private Appointment series;
    private CalendarTestManager ctm;

    public Bug44002Test(String name) {
        super(name);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ctm = new CalendarTestManager(client);
        conflict = new Appointment();
        series = new Appointment();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        c.add(Calendar.DAY_OF_MONTH, 7);
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        // Series next Thursday, weekly
        series.setTitle("Series next Thursday, weekly.");
        series.setStartDate(new Date(c.getTimeInMillis()));
        c.add(Calendar.HOUR_OF_DAY, 1);
        series.setEndDate(new Date(c.getTimeInMillis()));
        series.setRecurrenceType(Appointment.WEEKLY);
        series.setDays(Appointment.THURSDAY);
        series.setInterval(1);
        series.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        series.setIgnoreConflicts(true);
        series.setAlarm(15);

        conflict.setTitle("Not matching appointment");
        c.add(Calendar.DAY_OF_MONTH, 1);
        conflict.setStartDate(new Date(c.getTimeInMillis()));
        c.add(Calendar.HOUR_OF_DAY, 1);
        conflict.setEndDate(new Date(c.getTimeInMillis()));
        conflict.setIgnoreConflicts(true);
        conflict.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        conflict.setAlarm(15);

        ctm.insert(series);
        ctm.insert(conflict);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testBug44002() throws Exception {
        boolean old = false; // Switch to old UI behaviour for debugging purposes
        if (old) {
            series.setRecurrenceType(Appointment.NO_RECURRENCE);
            series.removeInterval();
            series.removeDays();
            ctm.update(series);
        } else {
            Appointment updateForSeries = new Appointment();
            updateForSeries.setParentFolderID(series.getParentFolderID());
            updateForSeries.setObjectID(series.getObjectID());
            updateForSeries.setRecurrenceType(Appointment.NO_RECURRENCE);
            updateForSeries.setLastModified(series.getLastModified());
            updateForSeries.setIgnoreConflicts(false);
            ctm.update(updateForSeries);
        }
        List<ConflictObject> conflicts = ctm.getLastResponse().getConflicts();
        if (conflicts != null) {
            for (ConflictObject conf : conflicts) {
                if (conf.getId() == conflict.getObjectID()) {
                    fail("Should not conflict with appointment.");
                }
            }
        }
    }

    @After
    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }
}
