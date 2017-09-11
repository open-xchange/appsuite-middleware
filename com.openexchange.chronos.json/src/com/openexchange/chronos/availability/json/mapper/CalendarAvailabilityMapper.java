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

package com.openexchange.chronos.availability.json.mapper;

import java.util.EnumMap;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.service.AvailabilityField;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;

/**
 * {@link CalendarAvailabilityMapper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CalendarAvailabilityMapper extends DefaultJsonMapper<Availability, AvailabilityField> {

    private static final CalendarAvailabilityMapper INSTANCE = new CalendarAvailabilityMapper();

    private final AvailabilityField[] mappedFields;

    /**
     * Gets the {@link CalendarAvailabilityMapper} instance.
     *
     * @return The {@link CalendarAvailabilityMapper} instance.
     */
    public static CalendarAvailabilityMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initialises a new {@link CalendarAvailabilityMapper}.
     */
    public CalendarAvailabilityMapper() {
        super();
        mappedFields = mappings.keySet().toArray(newArray(mappings.keySet().size()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.tools.mappings.Factory#newInstance()
     */
    @Override
    public Availability newInstance() {
        return new Availability();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.tools.mappings.ArrayFactory#newArray(int)
     */
    @Override
    public AvailabilityField[] newArray(int size) {
        return new AvailabilityField[size];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper#createMappings()
     */
    @Override
    protected EnumMap<AvailabilityField, ? extends JsonMapping<? extends Object, Availability>> createMappings() {
        EnumMap<AvailabilityField, JsonMapping<? extends Object, Availability>> mappings = new EnumMap<AvailabilityField, JsonMapping<? extends Object, Availability>>(AvailabilityField.class);
        // TODO: add mappings
        mappings.put(AvailabilityField.id, new StringMapping<Availability>("id", DataObject.OBJECT_ID) {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.id);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setId(value);
            }

            @Override
            public String get(Availability object) {
                return object.getId();
            }

            @Override
            public void remove(Availability object) {
                object.removeId();
            }
        });

        mappings.put(AvailabilityField.uid, new StringMapping<Availability>("uid", Appointment.UID) {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.uid);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setUid(value);
            }

            @Override
            public String get(Availability object) {
                return object.getUid();
            }

            @Override
            public void remove(Availability object) {
                object.removeUid();
            }
        });
        mappings.put(AvailabilityField.busytype, new StringMapping<Availability>("busyType", 12) {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.busytype);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setBusyType(BusyType.parseFromString(value));
            }

            @Override
            public String get(Availability object) {
                return object.getBusyType().getValue();
            }

            @Override
            public void remove(Availability object) {
                object.removeBusyType();
            }
        });
        mappings.put(AvailabilityField.description, new StringMapping<Availability>("description", Appointment.TITLE) {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.description);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setDescription(value);
            }

            @Override
            public String get(Availability object) {
                return object.getDescription();
            }

            @Override
            public void remove(Availability object) {
                object.removeDescription();
            }
        });
        mappings.put(AvailabilityField.summary, new StringMapping<Availability>("summary", Appointment.NOTE) {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.summary);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setDescription(value);
            }

            @Override
            public String get(Availability object) {
                return object.getDescription();
            }

            @Override
            public void remove(Availability object) {
                object.removeDescription();
            }
        });
        return mappings;
    }

    /**
     * Gets the mappedFields
     *
     * @return The mappedFields
     */
    public AvailabilityField[] getMappedFields() {
        return mappedFields;
    }

}
