/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
@SuppressWarnings("unused")
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
        setStartAndEndDates(master, "24.10.2008 01:00:00", "24.10.2008 05:00:00", false, tz);

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
        setStartAndEndDates(master, "27.03.2009 01:00:00", "27.03.2009 05:00:00", false, tz);

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
        setStartAndEndDates(master, "28.03.2009 02:15:00", "28.03.2009 02:45:00", false, tz);

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
        setStartAndEndDates(master, "25.10.2008 02:00:00", "25.10.2008 03:00:00", false, tz);

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
        setStartAndEndDates(master, "25.10.2008 01:00:00", "25.10.2008 02:00:00", false, tz);

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
