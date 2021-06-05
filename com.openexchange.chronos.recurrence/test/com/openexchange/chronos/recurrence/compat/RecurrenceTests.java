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

package com.openexchange.chronos.recurrence.compat;

import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Recurrence;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.recurrence.TestRecurrenceConfig;
import com.openexchange.chronos.recurrence.service.RecurrenceServiceImpl;
import com.openexchange.chronos.service.RecurrenceService;

/**
 * {@link RecurrenceTests}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RecurrenceTests {

    private final RecurrenceService recurrenceService = new RecurrenceServiceImpl(new TestRecurrenceConfig());

    private class Validator {

        private int count;
        private final String check;

        Validator(String check) {
            this.check = check;
            this.count = 0;
        }

        Validator feed(String expect) {
            if (expect.equals("INTERVAL=1")) {
                if (check.contains("INTERVAL=1")) {
                    increaseCount(expect.length());
                } else if (check.contains("INTERVAL=")) {
                    Assert.fail("Expected \"" + expect + "\" to be part of " + check + "\".");
                }
            } else {
                Assert.assertTrue("Expected \"" + expect + "\" to be part of " + check + "\".", check.contains(expect));
                increaseCount(expect.length());
            }
            return this;
        }

        private void increaseCount(int expect) {
            if (count > 0) {
                count++;
            }
            count += expect;
        }

        Validator assertlength() {
            Assert.assertEquals("Overall length does not match.", check.length(), count);
            return this;
        }
    }

    @Before
    public void setUp() {
        // Nothing to do
    }

    @Test
    public void testSimpleRecurringRules() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Validator v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|", tz, false));
        v.feed("FREQ=DAILY").feed("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|e|1222992000000|o|3|", tz, false));
        v.feed("FREQ=DAILY").feed("INTERVAL=1").feed("COUNT=3").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|e|1223769600000|", tz, false));
        v.feed("FREQ=DAILY").feed("INTERVAL=1").feed("UNTIL=20081012T124500Z").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|1|a|8|s|1222840800000|", tz, false));
        v.feed("FREQ=WEEKLY").feed("BYDAY=WE").feed("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|1|a|40|s|1222840800000|e|1224028800000|o|5|", tz, false));
        v.feed("FREQ=WEEKLY").feed("BYDAY=WE,FR").feed("INTERVAL=1").feed("COUNT=5").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|2|a|62|s|1222840800000|e|1224460800000|", tz, false));
        v.feed("FREQ=WEEKLY").feed("BYDAY=MO,TU,WE,TH,FR").feed("INTERVAL=2").feed("UNTIL=20081020T060000Z").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|3|i|2|b|3|s|1223013600000|e|1238716800000|o|4|", tz, false));
        v.feed("FREQ=MONTHLY").feed("BYMONTHDAY=3").feed("INTERVAL=2").feed("COUNT=4").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|5|i|1|a|32|b|2|s|1223618400000|e|1229040000000|", tz, false));
        v.feed("FREQ=MONTHLY").feed("BYSETPOS=2").feed("BYDAY=FR").feed("INTERVAL=1").feed("UNTIL=20081212T060000Z").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|5|i|1|a|65|b|5|s|1225004400000|e|1228003200000|o|2|", tz, false));
        v.feed("FREQ=MONTHLY").feed("BYSETPOS=-1").feed("BYDAY=SU,SA").feed("INTERVAL=1").feed("COUNT=2").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|4|i|1|b|8|c|9|s|1223445600000|", tz, false));
        v.feed("FREQ=YEARLY").feed("BYMONTHDAY=8").feed("BYMONTH=10").feed("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|6|i|1|a|8|b|3|c|9|s|1224050400000|e|1350432000000|o|5|", tz, false));
        v.feed("FREQ=YEARLY").feed("BYDAY=WE").feed("BYSETPOS=3").feed("BYMONTH=10").feed("INTERVAL=1").feed("COUNT=5").assertlength();
    }

    @Test
    public void testSimpleRecurringRulesToPattern() throws Exception {
        TimeZone tz = TimeZone.getDefault();

        SeriesPattern pattern = Recurrence.getSeriesPattern(recurrenceService, new DefaultRecurrenceData("FREQ=DAILY;INTERVAL=1", new DateTime(tz, 1222865100000L), null));
        Assert.assertEquals("Wrong pattern.", "t|1|i|1|s|1222865100000|", pattern.toString());

        pattern = Recurrence.getSeriesPattern(recurrenceService, new DefaultRecurrenceData("FREQ=DAILY;INTERVAL=1;COUNT=3", new DateTime(1222865100000L), null));
        Assert.assertEquals("Wrong pattern.", "t|1|i|1|o|3|s|1222865100000|e|1222992000000|", pattern.toString());

        pattern = Recurrence.getSeriesPattern(recurrenceService, new DefaultRecurrenceData("FREQ=DAILY;INTERVAL=1;UNTIL=20081012T124500Z", new DateTime(1222865100000L), null));
        Assert.assertEquals("Wrong pattern.", "t|1|i|1|s|1222865100000|e|1223769600000|", pattern.toString());

        pattern = Recurrence.getSeriesPattern(recurrenceService, new DefaultRecurrenceData("FREQ=WEEKLY;INTERVAL=1;BYDAY=WE", new DateTime(1222840800000L), null));
        Assert.assertEquals("Wrong pattern.", "t|2|i|1|a|8|s|1222840800000|", pattern.toString());

        pattern = Recurrence.getSeriesPattern(recurrenceService, new DefaultRecurrenceData("FREQ=WEEKLY;INTERVAL=1;COUNT=5;BYDAY=WE,FR", new DateTime(1222840800000L), null));
        Assert.assertEquals("Wrong pattern.", "t|2|i|1|a|40|o|5|s|1222840800000|e|1224028800000|", pattern.toString());
    }

    @Test
    public void testTimeZoneRecurringRules() throws Exception {
        java.util.TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        Validator v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|", tz, false));
        v.feed("FREQ=DAILY").feed("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|e|1222992000000|o|3|", tz, false));
        v.feed("FREQ=DAILY").feed("INTERVAL=1").feed("COUNT=3").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|e|1223769600000|", tz, false));
        v.feed("FREQ=DAILY").feed("INTERVAL=1").feed("UNTIL=20081012T124500Z").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|1|a|8|s|1222840800000|", tz, false));
        v.feed("FREQ=WEEKLY").feed("BYDAY=WE").feed("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|1|a|40|s|1222840800000|e|1224028800000|o|5|", tz, false));
        v.feed("FREQ=WEEKLY").feed("BYDAY=WE,FR").feed("INTERVAL=1").feed("COUNT=5").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|2|a|62|s|1222840800000|e|1224460800000|", tz, false));
        v.feed("FREQ=WEEKLY").feed("BYDAY=MO,TU,WE,TH,FR").feed("INTERVAL=2").feed("UNTIL=20081020T060000Z").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|3|i|2|b|3|s|1223013600000|e|1238716800000|o|4|", tz, false));
        v.feed("FREQ=MONTHLY").feed("BYMONTHDAY=3").feed("INTERVAL=2").feed("COUNT=4").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|5|i|1|a|32|b|2|s|1223618400000|e|1229040000000|", tz, false));
        v.feed("FREQ=MONTHLY").feed("BYSETPOS=2").feed("BYDAY=FR").feed("INTERVAL=1").feed("UNTIL=20081212T070000Z").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|5|i|1|a|65|b|5|s|1225004400000|e|1228003200000|o|2|", tz, false));
        v.feed("FREQ=MONTHLY").feed("BYSETPOS=-1").feed("BYDAY=SU,SA").feed("INTERVAL=1").feed("COUNT=2").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|4|i|1|b|8|c|9|s|1223445600000|", tz, false));
        v.feed("FREQ=YEARLY").feed("BYMONTHDAY=8").feed("BYMONTH=10").feed("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|6|i|1|a|8|b|3|c|9|s|1224050400000|e|1350432000000|o|5|", tz, false));
        v.feed("FREQ=YEARLY").feed("BYDAY=WE").feed("BYSETPOS=3").feed("BYMONTH=10").feed("INTERVAL=1").feed("COUNT=5").assertlength();
    }
}
