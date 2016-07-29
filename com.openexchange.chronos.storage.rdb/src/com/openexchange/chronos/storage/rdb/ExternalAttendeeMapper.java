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
                return null == attendee.getPartStat() ? null : I(Event2Appointment.getConfirm(attendee.getPartStat()));
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
