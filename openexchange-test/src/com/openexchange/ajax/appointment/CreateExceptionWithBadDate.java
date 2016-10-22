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

package com.openexchange.ajax.appointment;

import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link CreateExceptionWithBadDate}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class CreateExceptionWithBadDate extends AbstractAJAXSession {

    private CalendarTestManager ctm;
    private Appointment series;
    private Appointment exception;

    public CreateExceptionWithBadDate(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ctm = new CalendarTestManager(client);
        series = new Appointment();
        series.setTitle("Bug 48165 Test - series");
        series.setStartDate(TimeTools.D("01.08.2016 09:00"));
        series.setEndDate(TimeTools.D("01.08.2016 10:00"));
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setIgnoreConflicts(true);
        series.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        ctm.insert(series);

        exception = new Appointment();
        exception.setObjectID(series.getObjectID());
        exception.setParentFolderID(series.getParentFolderID());
        exception.setLastModified(series.getLastModified());
        exception.setRecurrencePosition(3);
        exception.setIgnoreConflicts(false);
    }

    public void testExcplicitWrongEndDate() throws Exception {
        exception.setStartDate(TimeTools.D("03.08.2016 11:00"));
        exception.setEndDate(TimeTools.D("03.08.2016 09:00"));
        ctm.update(exception);
        assertTrue("Expected error.", ctm.getLastResponse().hasError());
        OXException e = ctm.getLastResponse().getException();
        assertTrue("Wrong exception.", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
    }

    public void testImplicitWrongEndDate() throws Exception {
        exception.setStartDate(TimeTools.D("03.08.2016 11:00"));
        ctm.update(exception);
        assertTrue("Expected error.", ctm.getLastResponse().hasError());
        OXException e = ctm.getLastResponse().getException();
        assertTrue("Wrong exception.", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
    }

    public void testExcplicitWrongStartDate() throws Exception {
        exception.setStartDate(TimeTools.D("03.08.2016 11:00"));
        exception.setEndDate(TimeTools.D("03.08.2016 07:00"));
        ctm.update(exception);
        assertTrue("Expected error.", ctm.getLastResponse().hasError());
        OXException e = ctm.getLastResponse().getException();
        assertTrue("Wrong exception.", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
    }

    public void testImplicitWrongStartDate() throws Exception {
        exception.setEndDate(TimeTools.D("03.08.2016 07:00"));
        ctm.update(exception);
        assertTrue("Expected error.", ctm.getLastResponse().hasError());
        OXException e = ctm.getLastResponse().getException();
        assertTrue("Wrong exception.", e.similarTo(OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create()));
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }

}
