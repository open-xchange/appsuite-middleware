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

package com.openexchange.calendar;

import static com.openexchange.time.TimeTools.D;
import static com.openexchange.time.TimeTools.applyTimeZone;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;

/**
 * {@link AppointmentDiffTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AppointmentDiffTest {
    @Test
    public void testDifferentDates() {
        int[] fieldsToTest = new int[]{CalendarObject.START_DATE, CalendarObject.END_DATE};
        Date oldValue = D("8:00 PM");
        Date newValue = D("10:00 PM");

       expectDifference(fieldsToTest, oldValue, newValue);
    }

    @Test
    public void testSameDates() {
        int[] fieldsToTest = new int[]{CalendarObject.START_DATE, CalendarObject.END_DATE};
        Date value = D("8:00 PM");

        expectSame(fieldsToTest, value);
    }

    @Test
    public void testOneDateDiffers() {
        Appointment original = new Appointment();
        Appointment update = new Appointment();

        original.setStartDate(D("8:00 PM"));
        update.setStartDate(D("7:00 PM"));

        original.setEndDate(applyTimeZone(TimeZone.getTimeZone("GMT+1"), D("10:00 PM")));
        update.setEndDate(applyTimeZone(TimeZone.getTimeZone("UTC"), D("9:00 PM")));


        expectChanges(original, update, "start_date");

    }


    private void expectChanges(Appointment original, Appointment update, String...fields) {
        AppointmentDiff comparison = AppointmentDiff.compare(original, update);
        Set<String> expected = new HashSet<String>(fields.length);
        for (String string : fields) {
            expected.add(string);
        }

        for(FieldUpdate fieldUpdate : comparison.getUpdates()) {
            assertTrue("Got change for field "+fieldUpdate.getFieldName()+" but didn't expect it", expected.remove(fieldUpdate.getFieldName()));
        }

    }

    private void expectDifference(int[] fields, Object oldValue, Object newValue) {
        Appointment original = new Appointment();
        Appointment update = new Appointment();
        Set<Integer> expectedFields = new HashSet<Integer>();

        for (int i : fields) {
            original.set(i, oldValue);
            update.set(i, newValue);
            expectedFields.add(i);
        }

        AppointmentDiff comparison = AppointmentDiff.compare(original, update);

        for(FieldUpdate fieldUpdate : comparison.getUpdates()) {
            assertTrue("Got change for field "+fieldUpdate.getFieldName()+" but didn't expect it", expectedFields.remove(fieldUpdate.getFieldNumber()));
            assertEquals(oldValue, fieldUpdate.getOriginalValue());
            assertEquals(newValue, fieldUpdate.getNewValue());
        }

        assertTrue("Expected changes in fields "+expectedFields+" but they were not found", expectedFields.isEmpty());
    }


    private void expectSame(int[] fields, Object value) {
        Appointment original = new Appointment();
        Appointment update = new Appointment();

        for (int i : fields) {
            original.set(i, value);
            update.set(i, value);
        }

        AppointmentDiff comparison = AppointmentDiff.compare(original, update);

        assertTrue("Expected no changes, but these fields were found: "+comparison.getDifferingFieldNames(), comparison.getDifferingFieldNames().isEmpty());
        assertTrue("Expected no changes, but these fields were found: "+comparison.getDifferingFieldNames(), comparison.getUpdates().isEmpty());

    }

}
