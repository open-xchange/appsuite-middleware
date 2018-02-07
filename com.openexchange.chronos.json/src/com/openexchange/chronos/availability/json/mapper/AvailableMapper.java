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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.availability.json.mapper;

import java.util.Date;
import java.util.EnumMap;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.json.converter.mapper.DateTimeMapping;
import com.openexchange.chronos.service.AvailableField;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.LongMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;

/**
 * {@link AvailableMapper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AvailableMapper extends DefaultJsonMapper<Available, AvailableField> {

    private static final AvailableMapper INSTANCE = new AvailableMapper();

    private final AvailableField[] mappedFields;

    /**
     * Gets the mapper instance
     *
     * @return The mapper instance
     */
    public static AvailableMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initialises a new {@link AvailableMapper}
     */
    public AvailableMapper() {
        super();
        mappedFields = mappings.keySet().toArray(newArray(mappings.keySet().size()));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.tools.mappings.Factory#newInstance()
     */
    @Override
    public Available newInstance() {
        return new Available();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.tools.mappings.ArrayFactory#newArray(int)
     */
    @Override
    public AvailableField[] newArray(int size) {
        return new AvailableField[size];
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.tools.mappings.database.DefaultDbMapper#createMappings()
     */
    @Override
    protected EnumMap<AvailableField, ? extends JsonMapping<? extends Object, Available>> createMappings() {
        EnumMap<AvailableField, JsonMapping<? extends Object, Available>> mappings = new EnumMap<AvailableField, JsonMapping<? extends Object, Available>>(AvailableField.class);
        mappings.put(AvailableField.id, new IntegerMapping<Available>("id", DataObject.OBJECT_ID) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.id);
            }

            @Override
            public void set(Available object, Integer value) throws OXException {
                object.setId(Integer.toString(value));
            }

            @Override
            public Integer get(Available object) {
                return Integer.valueOf(object.getId());
            }

            @Override
            public void remove(Available object) {
                object.removeId();
            }

        });
        mappings.put(AvailableField.user, new IntegerMapping<Available>("user", Appointment.ORGANIZER_ID) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.user);
            }

            @Override
            public void set(Available object, Integer value) throws OXException {
                object.setCalendarUser(value);
            }

            @Override
            public Integer get(Available object) {
                return object.getCalendarUser();
            }

            @Override
            public void remove(Available object) {
                object.removeCalendarUser();
            }
        });
        mappings.put(AvailableField.uid, new StringMapping<Available>("uid", Appointment.UID) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.uid);
            }

            @Override
            public void set(Available object, String value) throws OXException {
                object.setUid(value);
            }

            @Override
            public String get(Available object) {
                return object.getUid();
            }

            @Override
            public void remove(Available object) {
                object.removeUid();
            }

        });
        mappings.put(AvailableField.dtstart, new DateTimeMapping<Available>("start", Appointment.START_DATE) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.dtstart);
            }

            @Override
            public void set(Available object, DateTime value) throws OXException {
                object.setStartTime(value);
            }

            @Override
            public DateTime get(Available object) {
                return object.getStartTime();
            }

            @Override
            public void remove(Available object) {
                object.removeStartTime();
            }

        });
        mappings.put(AvailableField.dtend, new DateTimeMapping<Available>("end", Appointment.END_DATE) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.dtend);
            }

            @Override
            public void set(Available object, DateTime value) throws OXException {
                object.setEndTime(value);
            }

            @Override
            public DateTime get(Available object) {
                return object.getEndTime();
            }

            @Override
            public void remove(Available object) {
                object.removeEndTime();
            }
        });
        mappings.put(AvailableField.created, new LongMapping<Available>("created", Appointment.CREATION_DATE) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.created);
            }

            @Override
            public void set(Available object, Long value) throws OXException {
                object.setCreated(value == null ? null : new Date(value.longValue()));
            }

            @Override
            public Long get(Available object) {
                Date created = object.getCreated();
                return created == null ? null : created.getTime();
            }

            @Override
            public void remove(Available object) {
                object.removeCreated();
            }
        });
        mappings.put(AvailableField.lastModified, new LongMapping<Available>("modified", Appointment.LAST_MODIFIED) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.lastModified);
            }

            @Override
            public void set(Available object, Long value) throws OXException {
                object.setLastModified(value == null ? null : new Date(value));
            }

            @Override
            public Long get(Available object) {
                return object.getLastModified().getTime();
            }

            @Override
            public void remove(Available object) {
                object.removeLastModified();
            }
        });
        mappings.put(AvailableField.rrule, new StringMapping<Available>("rrule", Appointment.RECURRENCE_TYPE) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.rrule);
            }

            @Override
            public void set(Available object, String value) throws OXException {
                object.setRecurrenceRule(value);
            }

            @Override
            public String get(Available object) {
                return object.getRecurrenceRule();
            }

            @Override
            public void remove(Available object) {
                object.removeRecurrenceRule();
            }
        });
        return mappings;
    }

    /**
     * Gets the mappedFields
     *
     * @return The mappedFields
     */
    @Override
    public AvailableField[] getMappedFields() {
        return mappedFields;
    }

}
