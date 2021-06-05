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

package com.openexchange.chronos.json.converter.mapper;

import static com.openexchange.java.Autoboxing.L;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.AlarmTriggerField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.json.fields.ChronosAlarmTriggerJsonFields;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;

/**
 * {@link AlarmTriggerMapper}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmTriggerMapper extends DefaultJsonMapper<AlarmTrigger, AlarmTriggerField> {

    private static final AlarmTriggerMapper INSTANCE = new AlarmTriggerMapper();

    @SuppressWarnings("hiding")
    private final AlarmTriggerField[] mappedFields;

    /**
     * Gets the alarm trigger mapper instance.
     *
     * @return The alarm trigger mapper instance
     */
    public static AlarmTriggerMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link AlarmTriggerMapper}.
     */
    private AlarmTriggerMapper() {
        super();
        this.mappedFields = mappings.keySet().toArray(newArray(mappings.keySet().size()));
    }

    @Override
    public AlarmTriggerField[] getMappedFields() {
        return mappedFields;
    }

    public AlarmTriggerField[] getAssignedFields(AlarmTrigger trigger, AlarmTriggerField... mandatoryFields) {
        if (null == trigger) {
            throw new IllegalArgumentException("trigger");
        }
        Set<AlarmTriggerField> setFields = new HashSet<AlarmTriggerField>();
        for (Entry<AlarmTriggerField, ? extends JsonMapping<? extends Object, AlarmTrigger>> entry : getMappings().entrySet()) {
            JsonMapping<? extends Object, AlarmTrigger> mapping = entry.getValue();
            if (mapping.isSet(trigger)) {
                AlarmTriggerField field = entry.getKey();
                setFields.add(field);
            }
        }
        if (null != mandatoryFields) {
            setFields.addAll(Arrays.asList(mandatoryFields));
        }
        return setFields.toArray(newArray(setFields.size()));
    }

    @Override
    public AlarmTrigger newInstance() {
        return new AlarmTrigger();
    }

    @Override
    public AlarmTriggerField[] newArray(int size) {
        return new AlarmTriggerField[size];
    }

    @Override
    protected EnumMap<AlarmTriggerField, ? extends JsonMapping<? extends Object, AlarmTrigger>> createMappings() {
        EnumMap<AlarmTriggerField, JsonMapping<? extends Object, AlarmTrigger>> mappings =
            new EnumMap<AlarmTriggerField, JsonMapping<? extends Object, AlarmTrigger>>(AlarmTriggerField.class);
        mappings.put(AlarmTriggerField.ACTION, new StringMapping<AlarmTrigger>(ChronosAlarmTriggerJsonFields.ACTION, null) {

            @Override
            public boolean isSet(AlarmTrigger object) {
                return object.containsAction();
            }

            @Override
            public void set(AlarmTrigger object, String value) {
                object.setAction(value);
            }

            @Override
            public String get(AlarmTrigger object) {
                return object.getAction();
            }

            @Override
            public void remove(AlarmTrigger object) {
                object.removeAction();
            }
        });

        mappings.put(AlarmTriggerField.ALARM_ID, new IntegerMapping<AlarmTrigger>(ChronosAlarmTriggerJsonFields.ALARM, null) {

            @Override
            public boolean isSet(AlarmTrigger object) {
                return object.containsAlarm();
            }

            @Override
            public void set(AlarmTrigger object, Integer value) {
                object.setAlarm(value);
            }

            @Override
            public Integer get(AlarmTrigger object) {
                return object.getAlarm();
            }

            @Override
            public void remove(AlarmTrigger object) {
                object.removeAlarm();
            }
        });

        mappings.put(AlarmTriggerField.EVENT_ID, new StringMapping<AlarmTrigger>(ChronosAlarmTriggerJsonFields.EVENT_ID, null) {

            @Override
            public boolean isSet(AlarmTrigger object) {
                return object.containsEventId();
            }

            @Override
            public void set(AlarmTrigger object, String value) {
                object.setEventId(value);
            }

            @Override
            public String get(AlarmTrigger object) {
                return object.getEventId();
            }

            @Override
            public void remove(AlarmTrigger object) {
                object.removeEventId();
            }
        });

        mappings.put(AlarmTriggerField.RECURRENCE_ID, new StringMapping<AlarmTrigger>(ChronosAlarmTriggerJsonFields.RECURRENCE_ID, null) {

            @Override
            public boolean isSet(AlarmTrigger object) {
                return object.containsRecurrenceId();
            }

            @Override
            public void set(AlarmTrigger object, String value) {
                object.setRecurrenceId(null == value ? null : new DefaultRecurrenceId(CalendarUtils.decode(value)));
            }

            @Override
            public String get(AlarmTrigger object) {
                RecurrenceId value = object.getRecurrenceId();
                return null == value ? null : CalendarUtils.encode(value.getValue());
            }

            @Override
            public void remove(AlarmTrigger object) {
                object.removeRecurrenceId();
            }
        });

        mappings.put(AlarmTriggerField.FOLDER, new StringMapping<AlarmTrigger>(ChronosAlarmTriggerJsonFields.FOLDER, null) {

            @Override
            public boolean isSet(AlarmTrigger object) {
                return object.containsFolder();
            }

            @Override
            public void set(AlarmTrigger object, String value) {
                object.setFolder(value);
            }

            @Override
            public String get(AlarmTrigger object) {
                return object.getFolder();
            }

            @Override
            public void remove(AlarmTrigger object) {
                object.removeFolder();
            }
        });

        mappings.put(AlarmTriggerField.TIME, new StringMapping<AlarmTrigger>(ChronosAlarmTriggerJsonFields.TIME, null) {

            @Override
            public boolean isSet(AlarmTrigger object) {
                return object.containsTime();
            }

            @Override
            public void set(AlarmTrigger object, String value) {
                DateTime dateTime = DateTime.parse("UTC", value);
                object.setTime(L(dateTime.getTimestamp()));
            }

            @Override
            public String get(AlarmTrigger object) {
                DateTime result = new DateTime(object.getTime().longValue());
                return result.toString();
            }

            @Override
            public void remove(AlarmTrigger object) {
                object.removeTime();
            }
        });

        return mappings;
    }



}
