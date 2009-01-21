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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.container;

import java.util.Set;
import junit.framework.TestCase;
import static com.openexchange.groupware.calendar.TimeTools.D;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class AppointmentObjectTest extends CalendarObjectTest {

    public void testCloneShouldNotChangeContainsStatus() {
        AppointmentObject a = new AppointmentObject();
        assertFalse(a.containsShownAs());
        AppointmentObject b = (AppointmentObject) a.clone();
        assertFalse(b.containsShownAs());
    }

    public void testFindDifferingFields() {
        AppointmentObject dataObject = getAppointmentObject();
        AppointmentObject otherDataObject = getAppointmentObject();

        otherDataObject.setAlarm(12);
        assertDifferences(dataObject, otherDataObject, AppointmentObject.ALARM);

        otherDataObject.setFullTime(true);
        assertDifferences(dataObject, otherDataObject, AppointmentObject.ALARM, AppointmentObject.FULL_TIME);

        otherDataObject.setLocation("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            AppointmentObject.ALARM,
            AppointmentObject.FULL_TIME,
            AppointmentObject.LOCATION);

        otherDataObject.setRecurringStart(D("24/02/2008 10:00").getTime());
        assertDifferences(
            dataObject,
            otherDataObject,
            AppointmentObject.ALARM,
            AppointmentObject.FULL_TIME,
            AppointmentObject.LOCATION,
            AppointmentObject.RECURRENCE_START);

        otherDataObject.setShownAs(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            AppointmentObject.ALARM,
            AppointmentObject.FULL_TIME,
            AppointmentObject.LOCATION,
            AppointmentObject.RECURRENCE_START,
            AppointmentObject.SHOWN_AS);

        otherDataObject.setTimezone("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            AppointmentObject.ALARM,
            AppointmentObject.FULL_TIME,
            AppointmentObject.LOCATION,
            AppointmentObject.RECURRENCE_START,
            AppointmentObject.SHOWN_AS,
            AppointmentObject.TIMEZONE);

    }
    
    

    public AppointmentObject getAppointmentObject() {
        AppointmentObject object = new AppointmentObject();

        fillAppointmentObject(object);

        return object;
    }

    public void fillAppointmentObject(AppointmentObject object) {
        super.fillCalendarObject(object);

        object.setAlarm(-12);

        object.setFullTime(false);

        object.setIgnoreConflicts(false);

        object.setLocation("Bla");

        object.setRecurringStart(D("24/02/2007 10:00").getTime());

        object.setShownAs(-12);

        object.setTimezone("Bla");

    }
}
