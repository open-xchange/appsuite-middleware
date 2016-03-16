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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Calendar;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug30414Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug30414Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;
    private Appointment series;
    private Appointment single;
    private int nextYear;

    public Bug30414Test(String name) {
        super(name);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ctm = new CalendarTestManager(client);
        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        
        single = new Appointment();
        single.setTitle("Bug 30414 single appointment.");
        single.setStartDate(D("03.02." + nextYear + " 08:00"));
        single.setEndDate(D("03.02." + nextYear + " 09:00"));
        single.setIgnoreConflicts(true);
        single.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        ctm.insert(single);
        
        series = new Appointment();
        series.setTitle("Bug 30414 series appointment.");
        series.setStartDate(D("01.02." + nextYear + " 08:00"));
        series.setEndDate(D("01.02." + nextYear + " 09:00"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(2);
        series.setOccurrence(3);
        series.setIgnoreConflicts(true);
        series.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        ctm.insert(series);
    }
    
    @Test
    public void testBug30414() throws Exception {
        Appointment exception2 = ctm.createIdentifyingCopy(series);
        exception2.setStartDate(D("02.02." + nextYear + " 08:00"));
        exception2.setEndDate(D("02.02." + nextYear + " 09:00"));
        exception2.setRecurrencePosition(2);
        Appointment exception = series.clone();
        exception.removeRecurrenceType();
        exception.removeInterval();
        exception.removeOccurrence();
        exception.setIgnoreConflicts(false);
        exception.setStartDate(D("02.02." + nextYear + " 08:00"));
        exception.setEndDate(D("02.02." + nextYear + " 09:00"));
        exception.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception.setRecurrencePosition(2);
        ctm.update(exception);
        
        List<ConflictObject> conflicts = ((UpdateResponse) ctm.getLastResponse()).getConflicts();
        boolean foundBadConflict = false;
        if (conflicts != null) {
            for (ConflictObject co : conflicts) {
                if (co.getId() == single.getObjectID()) {
                    foundBadConflict = true;
                    break;
                }
            }
        }
        assertFalse("Found conflict", foundBadConflict);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }

}
