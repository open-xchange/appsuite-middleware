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

package com.openexchange.chronos.json.fields;

import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.json.converter.mapper.AlarmTriggerMapper;

/**
 * {@link ChronosAlarmTriggerJsonFields} contains all fields which are used by the {@link AlarmTriggerMapper}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ChronosAlarmTriggerJsonFields {

    /**
     * The action of the alarm. See {@link AlarmTrigger#getAction()}
     */
    public static final String ACTION = "action";
    /**
     * The alarm id of the alarm. See {@link AlarmTrigger#getAlarm()}
     */
    public static final String ALARM = "alarmId";
    /**
     * The event id. See {@link AlarmTrigger#getEventId()}
     */
    public static final String EVENT_ID = "eventId";
    /**
     * The recurrence id. See {@link AlarmTrigger#getRecurrenceId()}
     */
    public static final String RECURRENCE_ID = "recurrenceId";
    /**
     * The datetime of the alarm. See {@link AlarmTrigger#getTime()}
     */
    public static final String TIME = "time";
    /**
     * The folder of the event. See {@link AlarmTrigger#getFolder()}
     */
    public static final String FOLDER = "folder";

}
