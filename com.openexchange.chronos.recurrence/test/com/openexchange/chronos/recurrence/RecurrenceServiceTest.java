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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.recurrence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.recurrence.service.RecurrenceServiceImpl;
import com.openexchange.chronos.service.RecurrenceService;

/**
 * {@link RecurrenceServiceTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public abstract class RecurrenceServiceTest {

    protected RecurrenceService service;

    public RecurrenceServiceTest() {

    }

    @Before
    public void setUp() {
        service = new RecurrenceServiceImpl();
    }

    @After
    public void tearDown() {}

    protected void compareInstanceWithMaster(Event master, Event instance, Date start, Date end) {
        assertNotNull("Master must not be null.", master);
        assertNotNull("Instance must not be null", instance);
        Event clone = master.clone();

        clone.removeId();
        clone.removeRecurrenceRule();
        clone.removeDeleteExceptionDates();
        clone.removeChangeExceptionDates();
        clone.setStartDate(start);
        clone.setEndDate(end);

        boolean equals = clone.equals(instance);
        assertTrue("Not equal.", equals);
    }

    protected String getUntilZulu(Calendar c) {
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        String month = Integer.toString(c.get(Calendar.MONTH) + 1);
        String year = Integer.toString(c.get(Calendar.YEAR));
        String dayOfMonth = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
        if (dayOfMonth.length() == 1) {
            dayOfMonth = "0" + dayOfMonth;
        }
        String hourOfDay = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
        if (hourOfDay.length() == 1) {
            hourOfDay = "0" + hourOfDay;
        }
        String minute = Integer.toString(c.get(Calendar.MINUTE));
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String second = Integer.toString(c.get(Calendar.SECOND));
        if (second.length() == 1) {
            second = "0" + second;
        }
        return "" + year + month + dayOfMonth + "T" + hourOfDay + minute + second + "Z";
    }

}
