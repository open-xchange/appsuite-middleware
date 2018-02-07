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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.java.Autoboxing.L;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.EnumMap;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.Repeat;
import com.openexchange.chronos.Trigger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMultiMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;

/**
 * {@link AlarmMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AlarmMapper extends DefaultDbMapper<Alarm, AlarmField> {

    private static final AlarmMapper INSTANCE = new AlarmMapper();

    /**
     * Gets the mapper instance.
     *
     * @return The instance.
     */
    public static AlarmMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link AlarmMapper}.
     */
    private AlarmMapper() {
        super();
    }

	@Override
    public Alarm newInstance() {
        return new Alarm();
	}

	@Override
    public AlarmField[] newArray(int size) {
        return new AlarmField[size];
	}

	@Override
	protected EnumMap<AlarmField, DbMapping<? extends Object, Alarm>> createMappings() {
		EnumMap<AlarmField, DbMapping<? extends Object, Alarm>> mappings = new
			EnumMap<AlarmField, DbMapping<? extends Object, Alarm>>(AlarmField.class);

        mappings.put(AlarmField.ID, new IntegerMapping<Alarm>("id", "Alarm ID") {

            @Override
            public void set(Alarm alarm, Integer value) {
                alarm.setId(null == value ? 0 : value.intValue());
            }

            @Override
            public boolean isSet(Alarm alarm) {
                return alarm.containsId();
            }

            @Override
            public Integer get(Alarm alarm) {
                return Integer.valueOf(alarm.getId());
            }

            @Override
            public void remove(Alarm alarm) {
                alarm.removeId();
            }
        });
        mappings.put(AlarmField.UID, new VarCharMapping<Alarm>("uid", "UID") {

            @Override
            public void set(Alarm alarm, String value) {
                alarm.setUid(value);
            }

            @Override
            public boolean isSet(Alarm alarm) {
                return alarm.containsUid();
            }

            @Override
            public String get(Alarm alarm) {
                return alarm.getUid();
            }

            @Override
            public void remove(Alarm alarm) {
                alarm.removeUid();
            }
        });
        mappings.put(AlarmField.RELATED_TO, new VarCharMapping<Alarm>("relatedTo", "Related-To") {

            @Override
            public void set(Alarm alarm, String value) {
                if (null == value) {
                    alarm.setRelatedTo(null);
                } else {
                    String[] splitted = Strings.splitByColon(value);
                    alarm.setRelatedTo(new RelatedTo(splitted[0], splitted[1]));
                }
            }

            @Override
            public boolean isSet(Alarm alarm) {
                return alarm.containsRelatedTo();
            }

            @Override
            public String get(Alarm alarm) {
                RelatedTo value = alarm.getRelatedTo();
                if (null == value) {
                    return null;
                }
                if (null == value.getRelType()) {
                    return ':' + value.getValue();
                }
                return value.getRelType() + ':' + value.getValue();
            }

            @Override
            public void remove(Alarm alarm) {
                alarm.removeRelatedTo();
            }
        });
        mappings.put(AlarmField.ACKNOWLEDGED, new BigIntMapping<Alarm>("acknowledged", "Acknowledged") {

            @Override
            public void set(Alarm alarm, Long value) {
                alarm.setAcknowledged(null == value ? null : new Date(value.longValue()));
            }

            @Override
            public boolean isSet(Alarm alarm) {
                return alarm.containsAcknowledged();
            }

            @Override
            public Long get(Alarm alarm) {
                Date value = alarm.getAcknowledged();
                return null == value ? null : L(value.getTime());
            }

            @Override
            public void remove(Alarm alarm) {
                alarm.removeAcknowledged();
            }
        });
        mappings.put(AlarmField.ACTION, new VarCharMapping<Alarm>("action", "Action") {

            @Override
            public void set(Alarm alarm, String value) {
                alarm.setAction(new AlarmAction(value));
            }

            @Override
            public boolean isSet(Alarm alarm) {
                return alarm.containsAction();
            }

            @Override
            public String get(Alarm alarm) {
                AlarmAction value = alarm.getAction();
                return null == value ? null : value.getValue();
            }

            @Override
            public void remove(Alarm alarm) {
                alarm.removeAction();
            }
        });
        mappings.put(AlarmField.REPEAT, new VarCharMapping<Alarm>("repetition", "Repeat") {

            @Override
            public void set(Alarm alarm, String value) {
                if (null == value) {
                    alarm.setRepeat(null);
                } else {
                    String[] splitted = Strings.splitByColon(value);
                    alarm.setRepeat(new Repeat(Integer.parseInt(splitted[0]), splitted[1]));
                }
            }

            @Override
            public boolean isSet(Alarm alarm) {
                return alarm.containsRepeat();
            }

            @Override
            public String get(Alarm alarm) {
                Repeat value = alarm.getRepeat();
                if (null == value) {
                    return null;
                }
                return value.getCount() + ":" + value.getDuration();
            }

            @Override
            public void remove(Alarm alarm) {
                alarm.removeRepeat();
            }
        });
        mappings.put(AlarmField.TRIGGER, new DefaultDbMultiMapping<Trigger, Alarm>(new String[] { "triggerRelated", "triggerDuration", "triggerDate" }, "Trigger") {

            @Override
            public Trigger get(ResultSet resultSet, String[] columnLabels) throws SQLException {
                Trigger.Related related = Enums.parse(Trigger.Related.class, resultSet.getString(columnLabels[0]), null);
                String duration = resultSet.getString(columnLabels[1]);
                long date = resultSet.getLong(columnLabels[2]);
                if (false == resultSet.wasNull()) {
                    Trigger trigger = new Trigger(new Date(date));
                    trigger.setRelated(related);
                    return trigger;
                } else if (null != duration) {
                    Trigger trigger = new Trigger(duration);
                    trigger.setRelated(related);
                    return trigger;
                }
                return null;
            }

            @Override
            public int set(PreparedStatement statement, int parameterIndex, Alarm alarm) throws SQLException {
                Trigger value = isSet(alarm) ? get(alarm) : null;
                if (null == value) {
                    statement.setNull(parameterIndex, Types.VARCHAR);
                    statement.setNull(1 + parameterIndex, Types.VARCHAR);
                    statement.setNull(2 + parameterIndex, Types.BIGINT);
                } else {
                    statement.setString(parameterIndex, null == value.getRelated() ? null : value.getRelated().name());
                    statement.setString(1 + parameterIndex, value.getDuration());
                    if (null == value.getDateTime()) {
                        statement.setNull(2 + parameterIndex, Types.BIGINT);
                    } else {
                        statement.setLong(2 + parameterIndex, value.getDateTime().getTime());
                    }
                }
                return 3;
            }

            @Override
            public boolean isSet(Alarm alarm) {
                return alarm.containsTrigger();
            }

            @Override
            public void set(Alarm alarm, Trigger value) throws OXException {
                alarm.setTrigger(value);
            }

            @Override
            public Trigger get(Alarm alarm) {
                return alarm.getTrigger();
            }

            @Override
            public void remove(Alarm alarm) {
                alarm.removeTrigger();
            }
        });
        mappings.put(AlarmField.EXTENDED_PROPERTIES, new ExtendedPropertiesMapping<Alarm>("extendedProperties", "Extended Properties") {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsExtendedProperties();
            }

            @Override
            public void set(Alarm object, ExtendedProperties value) throws OXException {
                object.setExtendedProperties(value);
            }

            @Override
            public ExtendedProperties get(Alarm object) {
                return object.getExtendedProperties();
            }

            @Override
            public void remove(Alarm object) {
                object.removeExtendedProperties();
            }
        });

        return mappings;
	}

}
