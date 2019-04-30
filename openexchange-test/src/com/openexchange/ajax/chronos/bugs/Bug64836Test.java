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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.testing.httpclient.models.EventData;


/**
 * {@link Bug64836Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class Bug64836Test extends AbstractChronosTest {
    
    public Bug64836Test() {
        super();
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        eventManager.setIgnoreConflicts(true);        
    }
    
    @Test
    public void testBug() throws Exception {
        EventData series = EventFactory.createSeriesEvent(defaultUserApi.getCalUser().intValue(), "Bug 64836 Test", 2, defaultFolderId);
        EventData createdSeries = eventManager.createEvent(series);
        TimeZone timeZone = TimeZone.getTimeZone(createdSeries.getStartDate().getTzid());
        Date from = CalendarUtils.truncateTime(new Date(), timeZone);
        Date until = CalendarUtils.add(from, Calendar.DATE, 7, timeZone);
        List<EventData> events = eventManager.getAllEvents(createdSeries.getFolder(), from, until, true);
        events = getEventsByUid(events, createdSeries.getUid());
        assertEquals(2, events.size());

        String exceptionSummary = createdSeries.getSummary() + " first";
        for (EventData occurrence : events) {
            EventData exception = new EventData();
            exception.setSummary(exceptionSummary);
            exception.setFolder(occurrence.getFolder());
            exception.setId(occurrence.getId());
            exception.setRecurrenceId(occurrence.getRecurrenceId());
            eventManager.updateOccurenceEvent(exception, exception.getRecurrenceId(), true);
            exceptionSummary = createdSeries.getSummary() + " second";
        }
        
        eventManager.getEvent(defaultFolderId, createdSeries.getId());
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
    

}
