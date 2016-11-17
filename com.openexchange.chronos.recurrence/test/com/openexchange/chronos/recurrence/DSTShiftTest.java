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

import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.time.TimeTools;

/**
 * {@link DSTShiftTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class DSTShiftTest extends AbstractSingleTimeZoneTest {

    private Date oct_26_2008_02_00_CEST;
    private Date oct_26_2008_02_30_CEST;
    private Date oct_26_2008_03_00_CEST; // Does not really exist.
    private Date oct_26_2008_02_00_CET;
    private Date oct_26_2008_02_30_CET;
    private Date oct_26_2008_03_00_CET;

    private Date mar_29_2009_02_00_CET; // Does not really exist.
    private Date mar_29_2009_03_00_CEST;

    public DSTShiftTest() {
        super("Europe/Berlin");
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        oct_26_2008_02_00_CEST = new Date(1224979200000L);
        oct_26_2008_02_30_CEST = new Date(1224981000000L);
        oct_26_2008_03_00_CEST = oct_26_2008_02_00_CET = new Date(1224982800000L);
        oct_26_2008_02_30_CET = new Date(1224984600000L);
        oct_26_2008_03_00_CET = new Date(1224986400000L);

        mar_29_2009_03_00_CEST = mar_29_2009_02_00_CET = new Date(1238288400000L);
    }

    @Test
    public void dstToNormal() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("24.10.2008 01:00:00", tz));
        master.setEndDate(TimeTools.D("24.10.2008 05:00:00", tz));
        master.setTimeZone(timeZone);

        Iterator<Event> instances = service.calculateInstances(master, null, null, null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("24.10.2008 01:00:00", tz), TimeTools.D("24.10.2008 05:00:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("25.10.2008 01:00:00", tz), TimeTools.D("25.10.2008 05:00:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("26.10.2008 01:00:00", tz), TimeTools.D("26.10.2008 04:00:00", tz)); // Shift
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, TimeTools.D("27.10.2008 01:00:00", tz), TimeTools.D("27.10.2008 05:00:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, TimeTools.D("28.10.2008 01:00:00", tz), TimeTools.D("28.10.2008 05:00:00", tz));
                    break outer;
                default:
                    break;
            }
        }
    }

    @Test
    public void normalToDst() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("27.03.2009 01:00:00", tz));
        master.setEndDate(TimeTools.D("27.03.2009 05:00:00", tz));
        master.setTimeZone(timeZone);

        Iterator<Event> instances = service.calculateInstances(master, null, null, null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("27.03.2009 01:00:00", tz), TimeTools.D("27.03.2009 05:00:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("28.03.2009 01:00:00", tz), TimeTools.D("28.03.2009 05:00:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("29.03.2009 01:00:00", tz), TimeTools.D("29.03.2009 06:00:00", tz)); // Shift
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, TimeTools.D("30.03.2009 01:00:00", tz), TimeTools.D("30.03.2009 05:00:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, TimeTools.D("31.03.2009 01:00:00", tz), TimeTools.D("31.03.2009 05:00:00", tz));
                    break outer;
                default:
                    break;
            }
        }
    }

    @Test
    public void forsakenTimes() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("28.03.2009 02:15:00", tz));
        master.setEndDate(TimeTools.D("28.03.2009 02:45:00", tz));
        master.setTimeZone(timeZone);

        Iterator<Event> instances = service.calculateInstances(master, null, null, null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("28.03.2009 02:15:00", tz), TimeTools.D("28.03.2009 02:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("29.03.2009 03:15:00", tz), TimeTools.D("29.03.2009 03:45:00", tz)); // Shift
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("30.03.2009 02:15:00", tz), TimeTools.D("30.03.2009 02:45:00", tz));
                    break outer;
                default:
                    break;
            }
        }
    }

    @Test
    public void ambiguousStart() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("25.10.2008 02:00:00", tz));
        master.setEndDate(TimeTools.D("25.10.2008 03:00:00", tz));
        master.setTimeZone(timeZone);

        Iterator<Event> instances = service.calculateInstances(master, null, null, null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("25.10.2008 02:00:00", tz), TimeTools.D("25.10.2008 03:00:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, oct_26_2008_02_00_CET, oct_26_2008_03_00_CET); // Shift
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("27.10.2008 02:00:00", tz), TimeTools.D("27.10.2008 03:00:00", tz));
                    break outer;
                default:
                    break;
            }
        }
    }

    @Test
    public void ambiguousEnd() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("25.10.2008 01:00:00", tz));
        master.setEndDate(TimeTools.D("25.10.2008 02:00:00", tz));
        master.setTimeZone(timeZone);

        Iterator<Event> instances = service.calculateInstances(master, null, null, null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("25.10.2008 01:00:00", tz), TimeTools.D("25.10.2008 02:00:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("26.10.2008 01:00:00", tz), oct_26_2008_02_00_CEST); // Shift
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("27.10.2008 01:00:00", tz), TimeTools.D("27.10.2008 02:00:00", tz));
                    break outer;
                default:
                    break;
            }
        }
    }

}
