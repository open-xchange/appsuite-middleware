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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.EnumMap;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ParticipantRole;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.DefaultMapping;
import com.openexchange.groupware.tools.mappings.Mapping;

/**
 * {@link AttendeeMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeMapper extends DefaultMapper<Attendee, AttendeeField> {

    private static final AttendeeMapper INSTANCE = new AttendeeMapper();

    /**
     * Gets the Attendee mapper instance.
     *
     * @return The Attendee mapper.
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
    protected EnumMap<AttendeeField, ? extends Mapping<? extends Object, Attendee>> getMappings() {
        EnumMap<AttendeeField, Mapping<? extends Object, Attendee>> mappings = new EnumMap<AttendeeField, Mapping<? extends Object, Attendee>>(AttendeeField.class);
        mappings.put(AttendeeField.URI, new DefaultMapping<String, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsUri();
            }

            @Override
            public void set(Attendee object, String value) throws OXException {
                object.setUri(value);
            }

            @Override
            public String get(Attendee object) {
                return object.getUri();
            }

            @Override
            public void remove(Attendee object) {
                object.removeUri();
            }
        });
        mappings.put(AttendeeField.CN, new DefaultMapping<String, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsCn();
            }

            @Override
            public void set(Attendee object, String value) throws OXException {
                object.setCn(value);
            }

            @Override
            public String get(Attendee object) {
                return object.getCn();
            }

            @Override
            public void remove(Attendee object) {
                object.removeCn();
            }
        });
        mappings.put(AttendeeField.ENTITY, new DefaultMapping<Integer, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsEntity();
            }

            @Override
            public void set(Attendee object, Integer value) throws OXException {
                object.setEntity(null == value ? 0 : i(value));
            }

            @Override
            public Integer get(Attendee object) {
                return I(object.getEntity());
            }

            @Override
            public void remove(Attendee object) {
                object.removeEntity();
            }
        });
        mappings.put(AttendeeField.SENT_BY, new DefaultMapping<String, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsSentBy();
            }

            @Override
            public void set(Attendee object, String value) throws OXException {
                object.setSentBy(value);
            }

            @Override
            public String get(Attendee object) {
                return object.getSentBy();
            }

            @Override
            public void remove(Attendee object) {
                object.removeSentBy();
            }
        });
        mappings.put(AttendeeField.CU_TYPE, new DefaultMapping<CalendarUserType, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsCuType();
            }

            @Override
            public void set(Attendee object, CalendarUserType value) throws OXException {
                object.setCuType(value);
            }

            @Override
            public CalendarUserType get(Attendee object) {
                return object.getCuType();
            }

            @Override
            public void remove(Attendee object) {
                object.removeCuType();
            }
        });
        mappings.put(AttendeeField.ROLE, new DefaultMapping<ParticipantRole, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsRole();
            }

            @Override
            public void set(Attendee object, ParticipantRole value) throws OXException {
                object.setRole(value);
            }

            @Override
            public ParticipantRole get(Attendee object) {
                return object.getRole();
            }

            @Override
            public void remove(Attendee object) {
                object.removeRole();
            }
        });
        mappings.put(AttendeeField.PARTSTAT, new DefaultMapping<ParticipationStatus, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsPartStat();
            }

            @Override
            public void set(Attendee object, ParticipationStatus value) throws OXException {
                object.setPartStat(value);
            }

            @Override
            public ParticipationStatus get(Attendee object) {
                return object.getPartStat();
            }

            @Override
            public void remove(Attendee object) {
                object.removePartStat();
            }
        });
        mappings.put(AttendeeField.COMMENT, new DefaultMapping<String, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsComment();
            }

            @Override
            public void set(Attendee object, String value) throws OXException {
                object.setComment(value);
            }

            @Override
            public String get(Attendee object) {
                return object.getComment();
            }

            @Override
            public void remove(Attendee object) {
                object.removeComment();
            }
        });
        mappings.put(AttendeeField.RSVP, new DefaultMapping<Boolean, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsPartStat();
            }

            @Override
            public void set(Attendee object, Boolean value) throws OXException {
                object.setRsvp(value);
            }

            @Override
            public Boolean get(Attendee object) {
                return object.getRsvp();
            }

            @Override
            public void remove(Attendee object) {
                object.removeRsvp();
            }
        });
        mappings.put(AttendeeField.FOLDER_ID, new DefaultMapping<Integer, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsFolderID();
            }

            @Override
            public void set(Attendee object, Integer value) throws OXException {
                object.setFolderID(null == value ? 0 : i(value));
            }

            @Override
            public Integer get(Attendee object) {
                return I(object.getFolderID());
            }

            @Override
            public void remove(Attendee object) {
                object.removeFolderID();
            }
        });
        mappings.put(AttendeeField.MEMBER, new DefaultMapping<String, Attendee>() {

            @Override
            public boolean isSet(Attendee object) {
                return object.containsMember();
            }

            @Override
            public void set(Attendee object, String value) throws OXException {
                object.setMember(value);
            }

            @Override
            public String get(Attendee object) {
                return object.getMember();
            }

            @Override
            public void remove(Attendee object) {
                object.removeMember();
            }
        });
        return mappings;
    }

}
