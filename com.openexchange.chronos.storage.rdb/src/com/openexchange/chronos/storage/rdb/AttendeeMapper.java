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
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.ParticipantRole;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.compat.ShownAsTransparency;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BooleanMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.Strings;

/**
 * {@link AttendeeMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeMapper extends DefaultDbMapper<Attendee, AttendeeField> {

    private static final AttendeeMapper INSTANCE = new AttendeeMapper();

    /**
     * Gets the mapper instance.
     *
     * @return The instance.
     */
    public static AttendeeMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link AttendeeMapper}.
     */
    private AttendeeMapper() {
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
        mappings.put(AttendeeField.ENTITY, new IntegerMapping<Attendee>("entity", "Entity") {

            @Override
            public void set(Attendee attendee, Integer value) {
                attendee.setEntity(null == value ? 0 : i(value));
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsEntity();
            }

            @Override
            public Integer get(Attendee attendee) {
                return I(attendee.getEntity());
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeEntity();
            }
        });
        mappings.put(AttendeeField.URI, new VarCharMapping<Attendee>("uri", "URI") {

            @Override
            public void set(Attendee attendee, String value) {
                attendee.setUri(value);
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsUri();
            }

            @Override
            public String get(Attendee attendee) {
                return attendee.getUri();
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeUri();
            }
        });
        mappings.put(AttendeeField.CN, new VarCharMapping<Attendee>("cn", "Common name") {

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
        mappings.put(AttendeeField.FOLDER_ID, new VarCharMapping<Attendee>("folder", "Folder ID") {

            @Override
            public void set(Attendee attendee, String value) {
                attendee.setFolderId(value);
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsFolderID();
            }

            @Override
            public String get(Attendee attendee) {
                return attendee.getFolderId();
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeFolderID();
            }
        });
        mappings.put(AttendeeField.CU_TYPE, new VarCharMapping<Attendee>("cuType", "Calendaruser Type") {

            @Override
            public void set(Attendee attendee, String value) {
                attendee.setCuType(null == value ? null : new CalendarUserType(value));
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsCuType();
            }

            @Override
            public String get(Attendee attendee) {
                CalendarUserType value = attendee.getCuType();
                return null == value ? null : value.getValue();
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeCuType();
            }
        });
        mappings.put(AttendeeField.ROLE, new VarCharMapping<Attendee>("role", "Role") {

            @Override
            public void set(Attendee attendee, String value) {
                attendee.setRole(null == value ? null : new ParticipantRole(value));
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsRole();
            }

            @Override
            public String get(Attendee attendee) {
                ParticipantRole value = attendee.getRole();
                return null == value ? null : value.getValue();
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeRole();
            }
        });
        mappings.put(AttendeeField.PARTSTAT, new VarCharMapping<Attendee>("partStat", "Participation Status") {

            @Override
            public void set(Attendee attendee, String value) {
                attendee.setPartStat(null == value ? null : new ParticipationStatus(value));
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsPartStat();
            }

            @Override
            public String get(Attendee attendee) {
                ParticipationStatus value = attendee.getPartStat();
                return null == value ? null : value.getValue();
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removePartStat();
            }
        });
        mappings.put(AttendeeField.RSVP, new BooleanMapping<Attendee>("rsvp", "RSVP") {

            @Override
            public void set(Attendee attendee, Boolean value) {
                attendee.setRsvp(value);
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsRsvp();
            }

            @Override
            public Boolean get(Attendee attendee) {
                return attendee.getRsvp();
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeRsvp();
            }
        });
        mappings.put(AttendeeField.COMMENT, new VarCharMapping<Attendee>("comment", "Comment") {

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
        mappings.put(AttendeeField.MEMBER, new VarCharMapping<Attendee>("member", "Member") {

            @Override
            public void set(Attendee attendee, String value) {
                String[] splitted = Strings.splitByComma(value);
                attendee.setMember(null != splitted ? Arrays.asList(splitted) : null);
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsMember();
            }

            @Override
            public String get(Attendee attendee) {
                List<String> value = attendee.getMember();
                if (null == value || 0 == value.size()) {
                    return null;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(value.get(0));
                for (int i = 1; i < value.size(); i++) {
                    stringBuilder.append(',').append(value.get(i));
                }
                return stringBuilder.toString();
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeMember();
            }
        });
        mappings.put(AttendeeField.TRANSP, new IntegerMapping<Attendee>("transp", "Transparency") {
            // 0 - TRANSPARENT, FREE
            // 1 - OPAQUE, RESERVED
            // 2 - OPAQUE, TEMPORARY
            // 3 - OPAQUE, ABSENT

            @Override
            public void set(Attendee attendee, Integer value) {
                if (null == value) {
                    attendee.setTransp(null);
                } else if (0 == value.intValue()) {
                    attendee.setTransp(TimeTransparency.TRANSPARENT);
                } else if (2 == value.intValue()) {
                    attendee.setTransp(ShownAsTransparency.TEMPORARY);
                } else if (3 == value.intValue()) {
                    attendee.setTransp(ShownAsTransparency.ABSENT);
                } else {
                    attendee.setTransp(TimeTransparency.OPAQUE);
                }
            }

            @Override
            public boolean isSet(Attendee attendee) {
                return attendee.containsTransp();
            }

            @Override
            public Integer get(Attendee attendee) {
                Transp value = attendee.getTransp();
                if (null == value) {
                    return null;
                } else if (Transp.TRANSPARENT.equals(value.getValue())) {
                    return I(0);
                } else if (ShownAsTransparency.TEMPORARY.equals(value)) {
                    return I(2);
                } else if (ShownAsTransparency.ABSENT.equals(value)) {
                    return I(3);
                } else {
                    return I(1);
                }
            }

            @Override
            public void remove(Attendee attendee) {
                attendee.removeTransp();
            }
        });
        mappings.put(AttendeeField.EXTENDED_PARAMETERS, new ExtendedPropertyParametersMapping<Attendee>("extendedParameters", "Extended Parameters") {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsExtendedParameters();
            }

            @Override
            public void set(Attendee object, List<ExtendedPropertyParameter> value) throws OXException {
                object.setExtendedParameters(value);
            }

            @Override
            public List<ExtendedPropertyParameter> get(Attendee object) {
                return object.getExtendedParameters();
            }

            @Override
            public void remove(Attendee object) {
                object.removeExtendedParameters();
            }
        });
        return mappings;
	}

}
