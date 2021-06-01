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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractAlarmTriggerTest;
import com.openexchange.ajax.chronos.factory.AlarmFactory;
import com.openexchange.ajax.chronos.factory.EventFactory;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.java.util.TimeZones;
import com.openexchange.testing.httpclient.models.AlarmTrigger;
import com.openexchange.testing.httpclient.models.AlarmTriggerResponse;
import com.openexchange.testing.httpclient.models.EventData;
import com.openexchange.time.TimeTools;

/**
 * {@link MWB1014Test}
 *
 * UI Error When Birthdays Disabled
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class MWB1014Test extends AbstractAlarmTriggerTest {

    private Map<String, String> neededConfigurations;

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return neededConfigurations;
    }

    @Override
    protected String getReloadables() {
        return "CapabilityReloadable";
    }

    @Test
    public void testGetTriggersWithDeactivatedBirthdaysCalendar() throws Exception {
        /*
         * lookup birthdays calendar once (to ensure birthdays calendar account gets auto-provisioned)
         */
        assertNotNull("no birthdays calendar folder found", optBirthdayCalendarFolder(foldersApi));
        /*
         * disable birthdays calendar provider for user & ensure that the folder is no longer listed
         */
        neededConfigurations = Collections.singletonMap("com.openexchange.calendar.birthdays.enabled", "false");
        setUpConfiguration();
        assertNull("birthdays calendar folder still found", optBirthdayCalendarFolder(foldersApi));
        /*
         * create an event with trigger in the user's default account
         */
        Date startDate = TimeTools.D("tomorrow at noon", TimeZones.UTC);
        Date endDate = CalendarUtils.add(startDate, Calendar.HOUR, 1);
        EventData toCreate = EventFactory.createSingleEventWithSingleAlarm(getCalendaruser(), getClass().getName(),
            DateTimeUtil.getDateTime(startDate.getTime()), DateTimeUtil.getDateTime(endDate.getTime()), AlarmFactory.createDisplayAlarm("-PT15M"), folderId);
        EventData event = eventManager.createEvent(toCreate, true);
        /*
         * get alarm triggers & expect no error (but allow a warning)
         */
        Date rangeEnd = TimeTools.D("next week at midnight", TimeZones.UTC);
        AlarmTriggerResponse alarmTriggerResponse = chronosApi.getAlarmTrigger(DateTimeUtil.getZuluDateTime(rangeEnd.getTime()).getValue(), null, null);
        List<AlarmTrigger> alarmTriggers = checkResponse(alarmTriggerResponse.getError(), alarmTriggerResponse.getErrorDesc(), alarmTriggerResponse.getCategories(), true, alarmTriggerResponse.getData());
        assertTrue("alarm not found", containsAlarm(alarmTriggers, folderId, null, event.getId()));
    }
}
