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

package com.openexchange.chronos.storage.rdb;

import java.util.EnumMap;
import java.util.TimeZone;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.AlarmTriggerField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.BooleanMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;

/**
 * {@link AlarmTriggerDBMapper}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmTriggerDBMapper extends DefaultDbMapper<AlarmTrigger, AlarmTriggerField> {

    private static final AlarmTriggerDBMapper INSTANCE = new AlarmTriggerDBMapper();

    @Override
    public AlarmTrigger newInstance() {
        return new AlarmTrigger();
    }

    @Override
    public AlarmTriggerField[] newArray(int size) {
       return new AlarmTriggerField[size];
    }

    @Override
    protected EnumMap<AlarmTriggerField, ? extends DbMapping<? extends Object, AlarmTrigger>> createMappings() {
        EnumMap<AlarmTriggerField, DbMapping<? extends Object, AlarmTrigger>> mappings = new EnumMap<AlarmTriggerField, DbMapping<? extends Object, AlarmTrigger>>(AlarmTriggerField.class);

        mappings.put(AlarmTriggerField.USER_ID, new IntegerMapping<AlarmTrigger>("user", "User ID") {

            @Override
            public void set(AlarmTrigger alarmTrigger, Integer value) {
                alarmTrigger.setUserId(value);
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsUserId();
            }

            @Override
            public Integer get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.getUserId();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeUserId();
            }
        });

        mappings.put(AlarmTriggerField.EVENT_ID, new VarCharMapping<AlarmTrigger>("eventId", "Event ID") {

            @Override
            public void set(AlarmTrigger alarmTrigger, String value) {
                alarmTrigger.setEventId(value);
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsEventId();
            }

            @Override
            public String get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.getEventId();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeEventId();
            }
        });

        mappings.put(AlarmTriggerField.FOLDER, new VarCharMapping<AlarmTrigger>("folder", "Folder") {

            @Override
            public void set(AlarmTrigger alarmTrigger, String value) {
                alarmTrigger.setFolder(value);
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsFolder();
            }

            @Override
            public String get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.getFolder();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeFolder();
            }
        });

        mappings.put(AlarmTriggerField.ALARM_ID, new IntegerMapping<AlarmTrigger>("alarm", "Alarm ID") {

            @Override
            public void set(AlarmTrigger alarmTrigger, Integer value) {
                alarmTrigger.setAlarm(value);
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsAlarm();
            }

            @Override
            public Integer get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.getAlarm();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeAlarm();
            }
        });


        mappings.put(AlarmTriggerField.ACTION, new VarCharMapping<AlarmTrigger>("action", "Action") {

            @Override
            public void set(AlarmTrigger alarmTrigger, String value) {
                alarmTrigger.setAction(value);
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsAction();
            }

            @Override
            public String get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.getAction();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeAction();
            }
        });

        mappings.put(AlarmTriggerField.TIME, new BigIntMapping<AlarmTrigger>("triggerDate", "Time") {

            @Override
            public void set(AlarmTrigger alarmTrigger, Long value) {
                alarmTrigger.setTime(value);
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsTime();
            }

            @Override
            public Long get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.getTime();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeTime();
            }
        });

        mappings.put(AlarmTriggerField.RELATED_TIME, new BigIntMapping<AlarmTrigger>("relatedTime", "Related time") {

            @Override
            public void set(AlarmTrigger alarmTrigger, Long value) {
                alarmTrigger.setRelatedTime(value);
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsRelatedTime();
            }

            @Override
            public Long get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.getRelatedTime();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeRelatedTime();
            }
        });

        mappings.put(AlarmTriggerField.PUSHED, new BooleanMapping<AlarmTrigger>("pushed", "Pushed") {

            @Override
            public void set(AlarmTrigger alarmTrigger, Boolean value) {
                alarmTrigger.setPushed(value);
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsPushed();
            }

            @Override
            public Boolean get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.isPushed();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removePushed();
            }
        });


        mappings.put(AlarmTriggerField.RECURRENCE_ID, new VarCharMapping<AlarmTrigger>("recurrence", "Recurrence ID") {

            @Override
            public void set(AlarmTrigger alarmTrigger, String value) {
                alarmTrigger.setRecurrenceId(null == value ? null : new DefaultRecurrenceId(CalendarUtils.decode(value)));
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsRecurrenceId();
            }

            @Override
            public String get(AlarmTrigger alarmTrigger) {
                RecurrenceId value = alarmTrigger.getRecurrenceId();
                return null == value ? null : CalendarUtils.encode(value.getValue());
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeRecurrenceId();
            }
        });

        mappings.put(AlarmTriggerField.FLOATING_TIMEZONE, new VarCharMapping<AlarmTrigger>("floatingTimezone", "Floating Timezone") {

            @Override
            public void set(AlarmTrigger alarmTrigger, String value) {
                alarmTrigger.setTimezone(TimeZone.getTimeZone(value));
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsTimezone();
            }

            @Override
            public String get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.getTimezone().getID();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeTimezone();
            }
        });

        mappings.put(AlarmTriggerField.PROCESSED, new BigIntMapping<AlarmTrigger>("processed", "Processed") {

            @Override
            public void set(AlarmTrigger alarmTrigger, Long value) {
                alarmTrigger.setProcessed(value);
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsProcessed();
            }

            @Override
            public Long get(AlarmTrigger alarmTrigger) {
                return alarmTrigger.getProcessed();
            }

            @Override
            public void remove(AlarmTrigger alarmTrigger) {
                alarmTrigger.removeProcessed();
            }
        });
        return mappings;
    }

    /**
     * @return
     */
    public static AlarmTriggerDBMapper getInstance() {
        return INSTANCE;
    }

}
