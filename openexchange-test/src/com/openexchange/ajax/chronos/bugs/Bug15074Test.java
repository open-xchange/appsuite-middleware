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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.factory.EventFactory.RecurringFrequency;
import com.openexchange.ajax.chronos.factory.RRuleFactory.RRuleBuilder;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.testing.httpclient.models.EventData;

/**
 *
 * {@link Bug15074Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Bug15074Test extends AbstractChronosTest {

    /**
     * Initializes a new {@link Bug15074Test}.
     */
    public Bug15074Test() {
        super();
    }

    @Test
    public void testBug() throws Exception {
        EventData event = EventFactory.createSingleTwoHourEvent(getCalendaruser(), "Bug15074Test", folderId);
        Calendar start = Calendar.getInstance();
        start.set(2007, Calendar.DECEMBER, 7, 0, 0, 0);
        event.setStartDate(DateTimeUtil.getDateTime(start));

        Calendar end = Calendar.getInstance();
        end.set(2007, Calendar.DECEMBER, 8, 0, 0, 0);
        event.setEndDate(DateTimeUtil.getDateTime(end));

        String rrule = RRuleBuilder.create().addFrequency(RecurringFrequency.YEARLY).addInterval(1).addByMonth(Calendar.DECEMBER).addByMonthDay(7).build();
        event.setRrule(rrule);
        EventData createEvent = eventManager.createEvent(event, true);

        Calendar cal = Calendar.getInstance();
        Date from = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date until = cal.getTime();
        List<EventData> allEvents = eventManager.getAllEvents(from, until, true, folderId);
        assertEquals(1, allEvents.size());
        assertEquals(createEvent.getId(), allEvents.get(0).getId());
    }

}
