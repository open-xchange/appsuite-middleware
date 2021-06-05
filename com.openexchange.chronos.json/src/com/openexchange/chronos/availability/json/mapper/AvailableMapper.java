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

package com.openexchange.chronos.availability.json.mapper;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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

    @SuppressWarnings("hiding")
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

    @Override
    public Available newInstance() {
        return new Available();
    }

    @Override
    public AvailableField[] newArray(int size) {
        return new AvailableField[size];
    }

    @Override
    protected EnumMap<AvailableField, ? extends JsonMapping<? extends Object, Available>> createMappings() {
        EnumMap<AvailableField, JsonMapping<? extends Object, Available>> mappings = new EnumMap<AvailableField, JsonMapping<? extends Object, Available>>(AvailableField.class);
        mappings.put(AvailableField.id, new IntegerMapping<Available>("id", I(DataObject.OBJECT_ID)) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.id);
            }

            @Override
            public void set(Available object, Integer value) throws OXException {
                if(value != null) {
                    object.setId(Integer.toString(value.intValue()));
                }
                else {
                    remove(object);
                }
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
        mappings.put(AvailableField.user, new IntegerMapping<Available>("user", I(Appointment.ORGANIZER_ID)) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.user);
            }

            @Override
            public void set(Available object, Integer value) throws OXException {
                if(value != null) {
                    object.setCalendarUser(value.intValue());
                }
                else {
                   remove(object);
                }
            }

            @Override
            public Integer get(Available object) {
                return I(object.getCalendarUser());
            }

            @Override
            public void remove(Available object) {
                object.removeCalendarUser();
            }
        });
        mappings.put(AvailableField.uid, new StringMapping<Available>("uid", I(Appointment.UID)) {

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
        mappings.put(AvailableField.dtstart, new DateTimeMapping<Available>("start", I(Appointment.START_DATE)) {

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
        mappings.put(AvailableField.dtend, new DateTimeMapping<Available>("end", I(Appointment.END_DATE)) {

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
        mappings.put(AvailableField.created, new LongMapping<Available>("created", I(Appointment.CREATION_DATE)) {

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
                return created == null ? null : L(created.getTime());
            }

            @Override
            public void remove(Available object) {
                object.removeCreated();
            }
        });
        mappings.put(AvailableField.lastModified, new LongMapping<Available>("modified", I(Appointment.LAST_MODIFIED)) {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.lastModified);
            }

            @Override
            public void set(Available object, Long value) throws OXException {
                object.setLastModified(value == null ? null : new Date(value.longValue()));
            }

            @Override
            public Long get(Available object) {
                return L(object.getLastModified().getTime());
            }

            @Override
            public void remove(Available object) {
                object.removeLastModified();
            }
        });
        mappings.put(AvailableField.rrule, new StringMapping<Available>("rrule", I(Appointment.RECURRENCE_TYPE)) {

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
