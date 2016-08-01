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

import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug38079Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.2
 */
public class Bug47012Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;
    private Appointment app;

    public Bug47012Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ctm = new CalendarTestManager(client);
        app = new Appointment();
        app.setTitle("Bug 47012 Test");
        app.setStartDate(TimeTools.D("07.07.2016 08:00"));
        app.setEndDate(TimeTools.D("07.07.2016 09:00"));
        app.setRecurrenceType(Appointment.WEEKLY);
        app.setDays(Appointment.THURSDAY);
        app.setInterval(1);
        app.setIgnoreConflicts(true);
        app.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        ctm.insert(app);
    }

    public void testChangeStartDate() throws Exception {
        Appointment changeException = ctm.createIdentifyingCopy(app);
        changeException.setRecurrencePosition(1);
        changeException.setStartDate(TimeTools.D("07.07.2016 08:30"));
        ctm.update(changeException);
        
        Appointment loadedException = ctm.get(changeException.getParentFolderID(), changeException.getObjectID());
        assertEquals("Wrong start date.", changeException.getStartDate(), loadedException.getStartDate());
        assertEquals("Wrong end date.", TimeTools.D("07.07.2016 09:00"), loadedException.getEndDate());
    }

    public void testChangeEndDate() throws Exception {
        Appointment changeException = ctm.createIdentifyingCopy(app);
        changeException.setRecurrencePosition(1);
        changeException.setEndDate(TimeTools.D("07.07.2016 09:30"));
        ctm.update(changeException);
        
        Appointment loadedException = ctm.get(changeException.getParentFolderID(), changeException.getObjectID());
        assertEquals("Wrong start date.", TimeTools.D("07.07.2016 08:00"), loadedException.getStartDate());
        assertEquals("Wrong end date.", changeException.getEndDate(), loadedException.getEndDate());
    }

    public void testChangeStartAndEndDate() throws Exception {
        Appointment changeException = ctm.createIdentifyingCopy(app);
        changeException.setRecurrencePosition(1);
        changeException.setStartDate(TimeTools.D("07.07.2016 08:30"));
        changeException.setEndDate(TimeTools.D("07.07.2016 09:30"));
        ctm.update(changeException);
        
        Appointment loadedException = ctm.get(changeException.getParentFolderID(), changeException.getObjectID());
        assertEquals("Wrong start date.", changeException.getStartDate(), loadedException.getStartDate());
        assertEquals("Wrong end date.", changeException.getEndDate(), loadedException.getEndDate());
    }

    public void testMakeFulltime() throws Exception {
        Appointment changeException = ctm.createIdentifyingCopy(app);
        changeException.setRecurrencePosition(1);
        changeException.setFullTime(true);
        ctm.update(changeException);
        
        Appointment loadedException = ctm.get(changeException.getParentFolderID(), changeException.getObjectID());
        assertTrue("Expected fulltime appointment.", loadedException.getFullTime());
        assertEquals("Wrong start date.", TimeTools.D("07.07.2016 00:00"), loadedException.getStartDate());
        assertEquals("Wrong end date.", TimeTools.D("08.07.2016 00:00"), loadedException.getEndDate());
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }

}
