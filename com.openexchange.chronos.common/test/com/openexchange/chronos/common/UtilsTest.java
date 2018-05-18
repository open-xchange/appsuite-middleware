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

package com.openexchange.chronos.common;

import static com.openexchange.chronos.common.CalendarUtils.getDuration;
import static com.openexchange.chronos.common.CalendarUtils.shiftRecurrenceId;
import static org.dmfs.rfc5545.DateTime.parse;
import static org.junit.Assert.assertEquals;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
/**
 * {@link UtilsTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UtilsTest  {

    @Test
    public void testShiftRecurrenceIdDate() throws Exception {
        assertEquals(new DefaultRecurrenceId("20180103"), shiftRecurrenceId(new DefaultRecurrenceId("20180103"), parse("20180101"), parse("20180101")));
        assertEquals(new DefaultRecurrenceId("20180203"), shiftRecurrenceId(new DefaultRecurrenceId("20180103"), parse("20180101"), parse("20180201")));
        assertEquals(new DefaultRecurrenceId("20180103"), shiftRecurrenceId(new DefaultRecurrenceId("20180203"), parse("20180201"), parse("20180101")));
        assertEquals(new DefaultRecurrenceId("20180603"), shiftRecurrenceId(new DefaultRecurrenceId("20180103"), parse("20180101"), parse("20180601")));
        assertEquals(new DefaultRecurrenceId("20180103"), shiftRecurrenceId(new DefaultRecurrenceId("20180603"), parse("20180601"), parse("20180101")));
    }

    @Test
    public void testShiftRecurrenceIdDateTimeWithSameTimeZone() throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals(new DefaultRecurrenceId("20180103T130000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180103T130000Z"), parse(timeZone, "20180101T140000"), parse(timeZone, "20180101T140000")));
        assertEquals(new DefaultRecurrenceId("20180203T130000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180103T130000Z"), parse(timeZone, "20180101T140000"), parse(timeZone, "20180201T140000")));
        assertEquals(new DefaultRecurrenceId("20180103T130000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180203T130000Z"), parse(timeZone, "20180201T140000"), parse(timeZone, "20180101T140000")));
        assertEquals(new DefaultRecurrenceId("20180603T120000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180103T130000Z"), parse(timeZone, "20180101T140000"), parse(timeZone, "20180601T140000")));
        assertEquals(new DefaultRecurrenceId("20180103T130000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180603T120000Z"), parse(timeZone, "20180601T140000"), parse(timeZone, "20180101T140000")));
    }

    @Test
    public void testGetDurationDate() throws Exception {
        assertEquals("P0D", getDuration(DateTime.parse("20171205"), DateTime.parse("20171205")).toString());
        assertEquals("P1D", getDuration(DateTime.parse("20171205"), DateTime.parse("20171206")).toString());
        assertEquals("-P1D", getDuration(DateTime.parse("20171206"), DateTime.parse("20171205")).toString());
        assertEquals("P365D", getDuration(DateTime.parse("20161206"), DateTime.parse("20171206")).toString());
        assertEquals("-P365D", getDuration(DateTime.parse("20171206"), DateTime.parse("20161206")).toString());
        assertEquals("P731D", getDuration(DateTime.parse("20151206"), DateTime.parse("20171206")).toString());
        assertEquals("-P731D", getDuration(DateTime.parse("20171206"), DateTime.parse("20151206")).toString());
    }

    @Test
    public void testGetDurationDateTimeWithSameTimeZone() throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals("P0D", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171205T160000")).toString());
        assertEquals("PT1M", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171205T160100")).toString());
        assertEquals("-PT1M", getDuration(DateTime.parse(timeZone, "20171205T160100"), DateTime.parse(timeZone, "20171205T160000")).toString());
        assertEquals("PT3H30M", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171205T193000")).toString());
        assertEquals("-PT3H30M", getDuration(DateTime.parse(timeZone, "20171205T193000"), DateTime.parse(timeZone, "20171205T160000")).toString());
        assertEquals("PT8H", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171206T000000")).toString());
        assertEquals("-PT8H", getDuration(DateTime.parse(timeZone, "20171206T000000"), DateTime.parse(timeZone, "20171205T160000")).toString());
        assertEquals("PT11H", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171206T030000")).toString());
        assertEquals("-PT11H", getDuration(DateTime.parse(timeZone, "20171206T030000"), DateTime.parse(timeZone, "20171205T160000")).toString());
    }

    @Test
    public void testGetDurationDateTimeWithMixedTimeZone() throws Exception {
        TimeZone timeZone1 = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone timeZone2 = TimeZone.getTimeZone("America/New_York");
        assertEquals("P0D", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T100000")).toString());
        assertEquals("PT1M", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T100100")).toString());
        assertEquals("-PT1M", getDuration(DateTime.parse(timeZone1, "20171205T160100"), DateTime.parse(timeZone2, "20171205T100000")).toString());
        assertEquals("PT3H30M", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T133000")).toString());
        assertEquals("-PT3H30M", getDuration(DateTime.parse(timeZone1, "20171205T193000"), DateTime.parse(timeZone2, "20171205T100000")).toString());
        assertEquals("PT8H", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T180000")).toString());
        assertEquals("-PT8H", getDuration(DateTime.parse(timeZone1, "20171206T000000"), DateTime.parse(timeZone2, "20171205T100000")).toString());
        assertEquals("PT11H", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T210000")).toString());
        assertEquals("-PT11H", getDuration(DateTime.parse(timeZone1, "20171206T030000"), DateTime.parse(timeZone2, "20171205T100000")).toString());
    }

    @Test
    public void testGetDurationDateAndDateTime() throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals("P0D", getDuration(DateTime.parse("20171205"), DateTime.parse(timeZone, "20171205T000000")).toString());
        assertEquals("P1D", getDuration(DateTime.parse("20171205"), DateTime.parse(timeZone, "20171206T000000")).toString());
        assertEquals("-P1D", getDuration(DateTime.parse("20171206"), DateTime.parse(timeZone, "20171205T000000")).toString());
        assertEquals("PT1M", getDuration(DateTime.parse("20171206"), DateTime.parse(timeZone, "20171206T000100")).toString());
        assertEquals("-PT1M", getDuration(DateTime.parse("20171206"), DateTime.parse(timeZone, "20171205T235900")).toString());
        assertEquals("PT3H30M", getDuration(DateTime.parse("20171205"), DateTime.parse(timeZone, "20171205T033000")).toString());
        assertEquals("-PT3H30M", getDuration(DateTime.parse("20171206"), DateTime.parse(timeZone, "20171205T203000")).toString());
    }

    @Test
    public void testGetDurationDateTimeAndDate() throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals("P0D", getDuration(DateTime.parse(timeZone, "20171205T000000"), DateTime.parse("20171205")).toString());
        assertEquals("P1D", getDuration(DateTime.parse(timeZone, "20171205T000000"), DateTime.parse("20171206")).toString());
        assertEquals("-P1D", getDuration(DateTime.parse(timeZone, "20171206T000000"), DateTime.parse("20171205")).toString());
        assertEquals("PT1M", getDuration(DateTime.parse(timeZone, "20171205T235900"), DateTime.parse("20171206")).toString());
        assertEquals("-PT1M", getDuration(DateTime.parse(timeZone, "20171206T000100"), DateTime.parse("20171206")).toString());
        assertEquals("PT3H30M", getDuration(DateTime.parse(timeZone, "20171205T203000"), DateTime.parse("20171206")).toString());
        assertEquals("-PT3H30M", getDuration(DateTime.parse(timeZone, "20171205T033000"), DateTime.parse("20171205")).toString());
    }

}
