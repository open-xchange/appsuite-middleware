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

package com.openexchange.chronos.storage.rdb;

import java.util.EnumMap;
import java.util.TimeZone;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.AlarmTriggerField;
import com.openexchange.chronos.RecurrenceId;
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
                alarmTrigger.setRecurrenceId(null == value ? null : new DefaultRecurrenceId(value));
            }

            @Override
            public boolean isSet(AlarmTrigger alarmTrigger) {
                return alarmTrigger.containsRecurrenceId();
            }

            @Override
            public String get(AlarmTrigger alarmTrigger) {
                RecurrenceId value = alarmTrigger.getRecurrenceId();
                return null == value ? null : value.getValue().toString();
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

        return mappings;
    }

    /**
     * @return
     */
    public static AlarmTriggerDBMapper getInstance() {
        return INSTANCE;
    }

}
