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

package com.openexchange.calendar.printing;

import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPToolTest extends AbstractDateTest {

    private CPTool tool;

    private final CPType[] nonBlockTypes = new CPType[] { CPType.DAYVIEW, CPType.WEEKVIEW, CPType.MONTHLYVIEW, CPType.YEARLYVIEW };

    @Override
    protected void setUp() throws Exception {
        tool = new CPTool();
        tool.setCalendar(CPCalendar.getCalendar());
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testShouldRecognizeLegitimateTemplateTypes() {
        for (CPType type : nonBlockTypes) {
            checkBlockTemplate(false, type.getName() + "/someTemplate");
            checkBlockTemplate(false, type.getNumber() + "/someTemplate");
        }

        CPType type = CPType.WORKWEEKVIEW;
        checkBlockTemplate(true, type.getName() + "/someTemplate");
        checkBlockTemplate(true, type.getNumber() + "/someTemplate");
    }

    @Test
    public void testShouldNotBeConfusedByMisleadingTemplateNames() {
        CPType evil = CPType.WORKWEEKVIEW;
        for (CPType type : nonBlockTypes) {
            checkBlockTemplate(false, type.getName() + "/" + evil.getName() + "someTemplate");
            checkBlockTemplate(false, type.getName() + "/" + "someTemplate" + evil.getName());
            checkBlockTemplate(false, evil.getName() + "/" + type.getName() + "/" + "someTemplate");
            checkBlockTemplate(false, type.getNumber() + "/" + evil.getNumber() + "someTemplate");
            checkBlockTemplate(false, type.getNumber() + "/" + "someTemplate" + evil.getNumber());
            checkBlockTemplate(false, evil.getNumber() + "/" + type.getNumber() + "/" + "someTemplate");

            checkBlockTemplate(true, type.getName() + "/" + evil.getName() + "/" + "someTemplate");
            checkBlockTemplate(true, evil.getName() + "/" + "someTemplate" + type.getName());
            checkBlockTemplate(true, type.getName() + "/" + evil.getName() + "/" + "someTemplate");
            checkBlockTemplate(true, evil.getNumber() + "/" + type.getNumber() + "someTemplate");
            checkBlockTemplate(true, evil.getNumber() + "/" + "someTemplate" + type.getNumber());
            checkBlockTemplate(true, type.getNumber() + "/" + evil.getNumber() + "/" + "someTemplate");
        }
    }

    @Test
    public void testShouldSplitAHundredDayAppointment() {
        final int amount = 5, offset = 10;

        Appointment app = new Appointment();
        app.setTitle("Appointment spanning " +amount+" days");
        Calendar cal = getCalendar();
        cal.set(Calendar.YEAR, 2007);

        cal.set(Calendar.DAY_OF_YEAR, offset);
        app.setStartDate(cal.getTime());

        cal.set(Calendar.DAY_OF_YEAR, amount+offset-1);
        app.setEndDate(cal.getTime());

        List<Appointment> apps = tool.splitIntoSingleDays(app);
        assertEquals("Should make a lot of single day appointments", amount, apps.size());

        for (int i = 0; i < amount; i++) {
            Appointment temp = apps.get(i);
            cal.setTime(temp.getStartDate());
            int startDay = cal.get(Calendar.DAY_OF_YEAR);
            cal.setTime(temp.getEndDate());
            int endDay = cal.get(Calendar.DAY_OF_YEAR);
            assertEquals("Should contain exactly the right day in the sequence", i+offset, endDay);
            assertEquals("Every single appointment should only span one day, but appointment #"+i+" misbehaves. ", startDay, endDay);
        }
    }

    @Test
    public void testShouldWorkWithoutSeriesAlso() {
        Appointment app = new Appointment();
        app.setTitle("Single day appointment");

        Calendar cal = getCalendar();
        cal.set(Calendar.YEAR, 2007);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        app.setStartDate(cal.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 16);
        app.setEndDate(cal.getTime());

        List<Appointment> apps = tool.splitIntoSingleDays(app);
        assertEquals("Should only produce one appointment", 1, apps.size());
        Appointment actual = apps.get(0);
        assertEquals("Should not change start date", app.getStartDate(), actual.getStartDate());
        assertEquals("Should not change end date", app.getEndDate(), actual.getEndDate());
    }

    @Test
    public void testShouldSplitAnAppointmentSpanningNewYear() {
        Appointment app = new Appointment();
        app.setTitle("Single day appointment");

        Calendar cal = getCalendar();
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        app.setEndDate(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        app.setStartDate(cal.getTime());

        List<Appointment> apps = tool.splitIntoSingleDays(app);
        assertEquals("Should only produce one appointment", 2, apps.size());
    }

    @Test
    public void testShouldSplitEvenWhenDayOfYearForStartDateIsBiggerThanForEndDate() {
        Appointment app = new Appointment();
        app.setTitle("Appointment starting late in one year and ending early in the following");

        Calendar cal = getCalendar();
        cal.set(Calendar.YEAR, 2007);
        cal.set(Calendar.DAY_OF_YEAR, 300);
        app.setStartDate(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 100);
        app.setEndDate(cal.getTime());

        List<Appointment> apps = tool.splitIntoSingleDays(app);
        assertEquals("Should only produce one appointment", 101, apps.size());

    }

    @Test
    public void testShouldSplitReallyLongAppointment() {
        Appointment app = new Appointment();
        app.setTitle("Long appointment");

        Calendar cal = getCalendar();
        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        app.setStartDate(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 400);
        app.setEndDate(cal.getTime());

        List<Appointment> apps = tool.splitIntoSingleDays(app);
        assertEquals("Should only produce one appointment", 401, apps.size());

    }

    @Test
    public void testShouldRetainDayTimeForFirstAndLastAppointmentInExpandedSeries() {
        Appointment app = new Appointment();
        app.setTitle("Single day appointment");

        Calendar cal = getCalendar();
        cal.set(Calendar.YEAR, 2007);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        app.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        app.setEndDate(cal.getTime());

        List<Appointment> apps = tool.splitIntoSingleDays(app);
        assertEquals("Should only produce two appointments", 2, apps.size());
        Appointment actual = apps.get(0);
        assertEquals("Should not change start date on first", app.getStartDate(), actual.getStartDate());
        actual = apps.get(1);
        assertEquals("Should not change end date on last", app.getEndDate(), actual.getEndDate());
    }

    @Test
    public void testShouldSplit23HourTwoDayAppointmentProperly() {
        Appointment app = new Appointment();
        app.setTitle("Two-day long appointment");

        Calendar cal = getCalendar();
        cal.set(Calendar.YEAR, 2007);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 2);
        app.setStartDate(cal.getTime());
        cal.set(Calendar.DAY_OF_YEAR, 2);
        cal.set(Calendar.HOUR_OF_DAY, 1);
        app.setEndDate(cal.getTime());

        List<Appointment> apps = tool.splitIntoSingleDays(app);
        assertEquals("Should produce two appointments", 2, apps.size());
    }

    @Test
    public void testShouldSplit47HourThreeDayAppointmentProperly() {
        Appointment app = new Appointment();
        app.setTitle("Two-day long appointment");

        Calendar cal = getCalendar();
        cal.set(Calendar.YEAR, 2007);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 2);
        app.setStartDate(cal.getTime());
        cal.set(Calendar.DAY_OF_YEAR, 3);
        cal.set(Calendar.HOUR_OF_DAY, 1);
        app.setEndDate(cal.getTime());

        List<Appointment> apps = tool.splitIntoSingleDays(app);
        for(Appointment temp: apps) {
            System.out.println(temp.getStartDate() + " / " + temp.getEndDate());
        }
        assertEquals("Should produce three appointments", 3, apps.size());
    }

    private void checkBlockTemplate(boolean expected, String templateName) {
        CPParameters params = new CPParameters();
        params.setTemplate(templateName);
        assertEquals("Checking template '" + templateName + "'", expected, tool.isBlockTemplate(params));
    }

}
