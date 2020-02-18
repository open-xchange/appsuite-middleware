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

package com.openexchange.gdpr.dataexport.internal;

import static org.junit.Assert.assertTrue;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DayOfWeekTimeRanges;
import com.openexchange.gdpr.dataexport.TimeOfTheDay;
import com.openexchange.gdpr.dataexport.TimeRange;

/**
 * {@link DataExportConfigTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportConfigTest {

    /**
     * Initializes a new {@link DataExportConfigTest}.
     */
    public DataExportConfigTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testScheduleParser1() {
        DataExportConfig config = DataExportConfig.builder().parse("Sun-Sat 0-24").build();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            assertTrue(config.getRangesOfTheWeek().containsKey(dayOfWeek));
        }
    }

    @Test
    public void testScheduleParser2() {
        DataExportConfig config = DataExportConfig.builder().parse("Mon-Wed 0-24").build();

        assertTrue(config.getRangesOfTheWeek().containsKey(DayOfWeek.MONDAY));
        assertTrue(config.getRangesOfTheWeek().containsKey(DayOfWeek.TUESDAY));
        assertTrue(config.getRangesOfTheWeek().containsKey(DayOfWeek.WEDNESDAY));
    }

    @Test
    public void testScheduleParser3() {
        DataExportConfig config = DataExportConfig.builder().parse("Mon-Sun 0-6").build();

        Map<DayOfWeek, DayOfWeekTimeRanges> rangesOfTheWeek = config.getRangesOfTheWeek();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            assertTrue(rangesOfTheWeek.containsKey(dayOfWeek));
        }

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            DayOfWeekTimeRanges dayOfWeekTimeRanges = rangesOfTheWeek.get(dayOfWeek);

            List<TimeRange> ranges = dayOfWeekTimeRanges.getRanges();
            assertTrue(ranges.size() == 1);

            TimeRange timeRange = ranges.get(0);
            assertTrue(timeRange.getStart().equals(new TimeOfTheDay(0, 0, 0)));
            assertTrue(timeRange.getEnd().equals(new TimeOfTheDay(6, 0, 0)));
        }
    }

}
