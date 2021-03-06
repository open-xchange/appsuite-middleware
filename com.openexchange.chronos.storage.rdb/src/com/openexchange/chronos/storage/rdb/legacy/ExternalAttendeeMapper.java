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

package com.openexchange.chronos.storage.rdb.legacy;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.EnumMap;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;

/**
 * {@link ExternalAttendeeMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ExternalAttendeeMapper extends DefaultDbMapper<Attendee, AttendeeField> {

    private static final ExternalAttendeeMapper INSTANCE = new ExternalAttendeeMapper();

    /**
     * Gets the mapper instance.
     *
     * @return The instance.
     */
    public static ExternalAttendeeMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link ExternalAttendeeMapper}.
     */
    private ExternalAttendeeMapper() {
        super();
    }

	@Override
    public Attendee newInstance() {
        return new Attendee();
	}

	@Override
    public AttendeeField[] newArray(int size) {
        return new AttendeeField[size];
	}

	@Override
	protected EnumMap<AttendeeField, DbMapping<? extends Object, Attendee>> createMappings() {
		EnumMap<AttendeeField, DbMapping<? extends Object, Attendee>> mappings = new
			EnumMap<AttendeeField, DbMapping<? extends Object, Attendee>>(AttendeeField.class);
        mappings.put(AttendeeField.URI, new VarCharMapping<Attendee>("mailAddress", "URI") {

            @Override
            public void set(Attendee attendee, String value) {
                attendee.setUri(Appointment2Event.getURI(value));
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsUri();
            }

            @Override
            public String get(Attendee attendee) {
                return Event2Appointment.getEMailAddress(attendee.getUri());
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeUri();
            }
        });
        mappings.put(AttendeeField.CN, new VarCharMapping<Attendee>("displayName", "Common name") {

            @Override
            public void set(Attendee attendee, String value) {
                attendee.setCn(value);
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsCn();
            }

            @Override
            public String get(Attendee attendee) {
                return attendee.getCn();
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeCn();
            }
        });
        mappings.put(AttendeeField.PARTSTAT, new IntegerMapping<Attendee>("confirm", "Participation status") {

            @Override
            public void set(Attendee attendee, Integer value) {
                attendee.setPartStat(null == value ? null : Appointment2Event.getParticipationStatus(i(value)));
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsPartStat();
            }

            @Override
            public Integer get(Attendee attendee) {
                return I(null == attendee.getPartStat() ? 0 : Event2Appointment.getConfirm(attendee.getPartStat()));
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removePartStat();
            }
        });
        mappings.put(AttendeeField.COMMENT, new VarCharMapping<Attendee>("reason", "Comment") {

            @Override
            public void set(Attendee attendee, String value) {
                attendee.setComment(value);
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsComment();
            }

            @Override
            public String get(Attendee attendee) {
                return attendee.getComment();
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeComment();
            }
        });
        return mappings;
	}

}
