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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.impl;

import java.util.EnumMap;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Trigger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.DefaultMapping;
import com.openexchange.groupware.tools.mappings.Mapping;

/**
 * {@link AlarmMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AlarmMapper extends DefaultMapper<Alarm, AlarmField> {

    private static final AlarmMapper INSTANCE = new AlarmMapper();

    /**
     * Gets the Alarm mapper instance.
     *
     * @return The Alarm mapper.
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

    public AbstractCollectionUpdate<Alarm, AlarmField> getAlarmUpdate(List<Alarm> originalAlarms, List<Alarm> newAlarms) throws OXException {
        return new AbstractCollectionUpdate<Alarm, AlarmField>(this, originalAlarms, newAlarms) {

            @Override
            protected boolean matches(Alarm alarm1, Alarm alarm2) {
                if (null == alarm1) {
                    return null == alarm2;
                } else if (null != alarm2) {
                    if (0 < alarm1.getId() && alarm1.getId() == alarm2.getId()) {
                        return true;
                    }
                    if (null != alarm1.getUid() && alarm1.getUid().equals(alarm2.getUid())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Copies data from one Alarm to another. Only <i>set</i> fields are transferred.
     *
     * @param from The source Alarm
     * @param to The destination Alarm
     * @param fields The fields to copy
     */
    @Override
    public void copy(Alarm from, Alarm to, AlarmField... fields) throws OXException {
        for (AlarmField field : fields) {
            Mapping<? extends Object, Alarm> mapping = get(field);
            if (mapping.isSet(from)) {
                mapping.copy(from, to);
            }
        }
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
    protected EnumMap<AlarmField, ? extends Mapping<? extends Object, Alarm>> getMappings() {
        EnumMap<AlarmField, Mapping<? extends Object, Alarm>> mappings = new EnumMap<AlarmField, Mapping<? extends Object, Alarm>>(AlarmField.class);
        mappings.put(AlarmField.TRIGGER, new DefaultMapping<Trigger, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return true;
            }

            @Override
            public void set(Alarm object, Trigger value) throws OXException {
                object.setTrigger(value);
            }

            @Override
            public Trigger get(Alarm object) {
                return object.getTrigger();
            }

            @Override
            public void remove(Alarm object) {
                //                object.removeTrigger();
                object.setTrigger(null);
            }
        });
        return mappings;
    }

}
