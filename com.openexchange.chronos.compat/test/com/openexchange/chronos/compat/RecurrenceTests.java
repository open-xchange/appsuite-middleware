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

package com.openexchange.chronos.compat;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.chronos.compat.internal.SeriesPattern;
import net.fortuna.ical4j.model.TimeZone;

/**
 * {@link RecurrenceTests}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RecurrenceTests {

    private class Validator {

        private int count;
        private String check;

        Validator(String check) {
            System.out.println(check);
            this.check = check;
            this.count = 0;
        }

        Validator _(String expect) {
            Assert.assertTrue("Expected \"" + expect + "\" to be part of " + check + "\".", check.contains(expect));
            if (count > 0) {
                count++;
            }
            count += expect.length();
            return this;
        }
        
        Validator assertlength() {
            Assert.assertEquals("Overall length does not match.", check.length(), count);
            return this;
        }
    }

    @Before
    public void setUp() {}

    @Test
    public void testSimpleRecurringRules() {
        java.util.TimeZone tz = TimeZone.getTimeZone("UTC");
        Validator v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|", tz, false));
        v._("FREQ=DAILY")._("INTERVAL=1").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|e|1222992000000|o|3|", tz, false));
        v._("FREQ=DAILY")._("INTERVAL=1")._("COUNT=3").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|e|1223769600000|", tz, false));
        v._("FREQ=DAILY")._("INTERVAL=1")._("UNTIL=20081012T124500Z").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|1|a|8|s|1222840800000|", tz, false));
        v._("FREQ=WEEKLY")._("BYDAY=WE")._("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|1|a|40|s|1222840800000|e|1224028800000|o|5|", tz, false));
        v._("FREQ=WEEKLY")._("BYDAY=WE,FR")._("INTERVAL=1")._("COUNT=5").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|2|a|62|s|1222840800000|e|1224460800000|", tz, false));
        v._("FREQ=WEEKLY")._("BYDAY=MO,TU,WE,TH,FR")._("INTERVAL=2")._("UNTIL=20081020T060000Z").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|3|i|2|b|3|s|1223013600000|e|1238716800000|o|4|", tz, false));
        v._("FREQ=MONTHLY")._("BYMONTHDAY=3")._("INTERVAL=2")._("COUNT=4").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|5|i|1|a|32|b|2|s|1223618400000|e|1229040000000|", tz, false));
        v._("FREQ=MONTHLY")._("BYSETPOS=2")._("BYDAY=FR")._("INTERVAL=1")._("UNTIL=20081212T060000Z").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|5|i|1|a|65|b|5|s|1225004400000|e|1228003200000|o|2|", tz, false));
        v._("FREQ=MONTHLY")._("BYSETPOS=-1")._("BYDAY=SU,SA")._("INTERVAL=1")._("COUNT=2").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|4|i|1|b|8|c|9|s|1223445600000|", tz, false));
        v._("FREQ=YEARLY")._("BYMONTHDAY=8")._("BYMONTH=10")._("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|6|i|1|a|8|b|3|c|9|s|1224050400000|e|1350432000000|o|5|", tz, false));
        v._("FREQ=YEARLY")._("BYDAY=WE")._("BYSETPOS=3")._("BYMONTH=10")._("INTERVAL=1")._("COUNT=5").assertlength();
    }
    
    @Test
    public void testSimpleRecurringRulesToPattern() {
        Calendar cal = GregorianCalendar.getInstance();
        
        cal.setTimeInMillis(1222865100000L);
        String pattern = Recurrence.generatePattern("FREQ=DAILY;INTERVAL=1", cal);
        Assert.assertEquals("Wrong pattern.", "t|1|i|1|s|1222865100000|", pattern.toString());
        
        cal.setTimeInMillis(1222865100000L);
        pattern = Recurrence.generatePattern("FREQ=DAILY;INTERVAL=1;COUNT=3", cal);
        Assert.assertEquals("Wrong pattern.", "t|1|i|1|s|1222865100000|e|1222992000000|o|3|", pattern.toString());
        
        cal.setTimeInMillis(1222865100000L);
        pattern = Recurrence.generatePattern("FREQ=DAILY;INTERVAL=1;UNTIL=20081012T124500Z", cal);
        Assert.assertEquals("Wrong pattern.", "t|1|i|1|s|1222865100000|e|1223769600000|", pattern.toString());
        
        cal.setTimeInMillis(1222840800000L);
        pattern = Recurrence.generatePattern("FREQ=WEEKLY;INTERVAL=1;BYDAY=WE", cal);
        Assert.assertEquals("Wrong pattern.", "t|2|i|1|a|8|s|1222840800000|", pattern.toString());
        
        cal.setTimeInMillis(1222840800000L);
        pattern = Recurrence.generatePattern("FREQ=WEEKLY;INTERVAL=1;COUNT=5;BYDAY=WE,FR", cal);
        Assert.assertEquals("Wrong pattern.", "t|2|i|1|a|40|s|1222840800000|e|1224028800000|o|5|", pattern.toString());
    }
    
    @Test
    public void testTimeZoneRecurringRules() {
        java.util.TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        Validator v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|", tz, false));
        v._("FREQ=DAILY")._("INTERVAL=1").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|e|1222992000000|o|3|", tz, false));
        v._("FREQ=DAILY")._("INTERVAL=1")._("COUNT=3").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|1|i|1|s|1222865100000|e|1223769600000|", tz, false));
        v._("FREQ=DAILY")._("INTERVAL=1")._("UNTIL=20081012T104500Z").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|1|a|8|s|1222840800000|", tz, false));
        v._("FREQ=WEEKLY")._("BYDAY=WE")._("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|1|a|40|s|1222840800000|e|1224028800000|o|5|", tz, false));
        v._("FREQ=WEEKLY")._("BYDAY=WE,FR")._("INTERVAL=1")._("COUNT=5").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|2|i|2|a|62|s|1222840800000|e|1224460800000|", tz, false));
        v._("FREQ=WEEKLY")._("BYDAY=MO,TU,WE,TH,FR")._("INTERVAL=2")._("UNTIL=20081020T040000Z").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|3|i|2|b|3|s|1223013600000|e|1238716800000|o|4|", tz, false));
        v._("FREQ=MONTHLY")._("BYMONTHDAY=3")._("INTERVAL=2")._("COUNT=4").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|5|i|1|a|32|b|2|s|1223618400000|e|1229040000000|", tz, false));
        v._("FREQ=MONTHLY")._("BYSETPOS=2")._("BYDAY=FR")._("INTERVAL=1")._("UNTIL=20081212T050000Z").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|5|i|1|a|65|b|5|s|1225004400000|e|1228003200000|o|2|", tz, false));
        v._("FREQ=MONTHLY")._("BYSETPOS=-1")._("BYDAY=SU,SA")._("INTERVAL=1")._("COUNT=2").assertlength();
        
        v = new Validator(Recurrence.getRecurrenceRule("t|4|i|1|b|8|c|9|s|1223445600000|", tz, false));
        v._("FREQ=YEARLY")._("BYMONTHDAY=8")._("BYMONTH=10")._("INTERVAL=1").assertlength();

        v = new Validator(Recurrence.getRecurrenceRule("t|6|i|1|a|8|b|3|c|9|s|1224050400000|e|1350432000000|o|5|", tz, false));
        v._("FREQ=YEARLY")._("BYDAY=WE")._("BYSETPOS=3")._("BYMONTH=10")._("INTERVAL=1")._("COUNT=5").assertlength();
    }
}
