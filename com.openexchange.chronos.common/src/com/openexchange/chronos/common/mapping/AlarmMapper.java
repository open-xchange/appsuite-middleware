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

package com.openexchange.chronos.common.mapping;

import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.Repeat;
import com.openexchange.chronos.Trigger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.DefaultMapping;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.java.Autoboxing;

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
        mappings.put(AlarmField.ID, new DefaultMapping<Integer, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsId();
            }

            @Override
            public void set(Alarm object, Integer value) throws OXException {
                object.setId(null != value ? Autoboxing.i(value) : 0);
            }

            @Override
            public Integer get(Alarm object) {
                return Autoboxing.I(object.getId());
            }

            @Override
            public void remove(Alarm object) {
                object.removeId();
            }
        });
        mappings.put(AlarmField.SUMMARY, new DefaultMapping<String, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsSummary();
            }

            @Override
            public void set(Alarm object, String value) throws OXException {
                object.setSummary(value);
            }

            @Override
            public String get(Alarm object) {
                return object.getSummary();
            }

            @Override
            public void remove(Alarm object) {
                object.removeSummary();
            }
        });
        mappings.put(AlarmField.DESCRIPTION, new DefaultMapping<String, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsDescription();
            }

            @Override
            public void set(Alarm object, String value) throws OXException {
                object.setDescription(value);
            }

            @Override
            public String get(Alarm object) {
                return object.getDescription();
            }

            @Override
            public void remove(Alarm object) {
                object.removeDescription();
            }
        });
        mappings.put(AlarmField.UID, new DefaultMapping<String, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsUid();
            }

            @Override
            public void set(Alarm object, String value) throws OXException {
                object.setUid(value);
            }

            @Override
            public String get(Alarm object) {
                return object.getUid();
            }

            @Override
            public void remove(Alarm object) {
                object.removeUid();
            }
        });
        mappings.put(AlarmField.UID, new DefaultMapping<String, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsUid();
            }

            @Override
            public void set(Alarm object, String value) throws OXException {
                object.setUid(value);
            }

            @Override
            public String get(Alarm object) {
                return object.getUid();
            }

            @Override
            public void remove(Alarm object) {
                object.removeUid();
            }
        });
        mappings.put(AlarmField.RELATED_TO, new DefaultMapping<RelatedTo, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsRelatedTo();
            }

            @Override
            public void set(Alarm object, RelatedTo value) throws OXException {
                object.setRelatedTo(value);
            }

            @Override
            public RelatedTo get(Alarm object) {
                return object.getRelatedTo();
            }

            @Override
            public void remove(Alarm object) {
                object.removeRelatedTo();
            }
        });
        mappings.put(AlarmField.REPEAT, new DefaultMapping<Repeat, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsRepeat();
            }

            @Override
            public void set(Alarm object, Repeat value) throws OXException {
                object.setRepeat(value);
            }

            @Override
            public Repeat get(Alarm object) {
                return object.getRepeat();
            }

            @Override
            public void remove(Alarm object) {
                object.removeRepeat();
            }
        });
        mappings.put(AlarmField.TRIGGER, new DefaultMapping<Trigger, Alarm>() {

            @Override
            public void copy(Alarm from, Alarm to) throws OXException {
                Trigger value = get(from);
                set(to, null == value ? null : new Trigger(value));
            }

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
                object.setTrigger(null);
            }
        });
        mappings.put(AlarmField.ACTION, new DefaultMapping<AlarmAction, Alarm>() {

            @Override
            public void copy(Alarm from, Alarm to) throws OXException {
                AlarmAction value = get(from);
                set(to, value);
            }

            @Override
            public boolean isSet(Alarm object) {
                return null != object.getAction();
            }

            @Override
            public void set(Alarm object, AlarmAction value) throws OXException {
                object.setAction(value);
            }

            @Override
            public AlarmAction get(Alarm object) {
                return object.getAction();
            }

            @Override
            public void remove(Alarm object) {
                object.setAction(null);
            }
        });
        mappings.put(AlarmField.ACKNOWLEDGED, new DefaultMapping<Date, Alarm>() {

            @Override
            public void copy(Alarm from, Alarm to) throws OXException {
                Date value = get(from);
                set(to, value);
            }

            @Override
            public boolean isSet(Alarm object) {
                return null != object.getAcknowledged();
            }

            @Override
            public void set(Alarm object, Date value) throws OXException {
                object.setAcknowledged(value);
            }

            @Override
            public Date get(Alarm object) {
                return object.getAcknowledged();
            }

            @Override
            public void remove(Alarm object) {
                object.setAcknowledged(null);
            }
        });
        mappings.put(AlarmField.ATTACHMENTS, new DefaultMapping<List<Attachment>, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsAttachments();
            }

            @Override
            public void set(Alarm object, List<Attachment> value) throws OXException {
                object.setAttachments(value);
            }

            @Override
            public List<Attachment> get(Alarm object) {
                return object.getAttachments();
            }

            @Override
            public void remove(Alarm object) {
                object.removeAttachments();
            }
        });
        mappings.put(AlarmField.ATTENDEES, new DefaultMapping<List<Attendee>, Alarm>() {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsAttendees();
            }

            @Override
            public void set(Alarm object, List<Attendee> value) throws OXException {
                object.setAttendees(value);
            }

            @Override
            public List<Attendee> get(Alarm object) {
                return object.getAttendees();
            }

            @Override
            public void remove(Alarm object) {
                object.removeAttendees();
            }
        });
        mappings.put(AlarmField.EXTENDED_PROPERTIES, new DefaultMapping<ExtendedProperties, Alarm>() {

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
