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

package com.openexchange.chronos.json.converter.mapper;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.LongMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;
import com.openexchange.java.Enums;
import com.openexchange.session.Session;

/**
 * {@link AlarmMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AlarmMapper extends DefaultJsonMapper<Alarm, AlarmField> {

    private static final AlarmMapper INSTANCE = new AlarmMapper();

    /**
     * Gets the alarm mapper instance.
     *
     * @return The alarm mapper instance.
     */
    public static AlarmMapper getInstance() {
        return INSTANCE;
    }

    private final AlarmField[] mappedFields;

    /**
     * Initializes a new {@link AlarmMapper}.
     */
    private AlarmMapper() {
        super();
        this.mappedFields = mappings.keySet().toArray(newArray(mappings.keySet().size()));
    }

    @Override
    public AlarmField[] getMappedFields() {
        return mappedFields;
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
    protected EnumMap<AlarmField, ? extends JsonMapping<? extends Object, Alarm>> createMappings() {
        EnumMap<AlarmField, JsonMapping<? extends Object, Alarm>> mappings = new EnumMap<AlarmField, JsonMapping<? extends Object, Alarm>>(AlarmField.class);
        mappings.put(AlarmField.ID, new IntegerMapping<Alarm>(ChronosJsonFields.Alarm.ID, null) {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsId();
            }

            @Override
            public void set(Alarm object, Integer value) throws OXException {
                object.setId(i(value));
            }

            @Override
            public Integer get(Alarm object) {
                return I(object.getId());
            }

            @Override
            public void remove(Alarm object) {
                object.removeId();
            }
        });
        mappings.put(AlarmField.UID, new StringMapping<Alarm>(ChronosJsonFields.UID, null) {

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
        mappings.put(AlarmField.RELATED_TO, new DefaultJsonMapping<RelatedTo, Alarm>(ChronosJsonFields.Alarm.RELATED_TO, null) {

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

            @Override
            public void deserialize(JSONObject from, Alarm to) throws JSONException, OXException {
                JSONObject jsonObject = from.optJSONObject(getAjaxName());
                if (null == jsonObject) {
                    to.setRelatedTo(null);
                } else {
                    String relType = jsonObject.optString("relType", null);
                    String value = jsonObject.getString("value");
                    to.setRelatedTo(new RelatedTo(relType, value));
                }
            }

            @Override
            public Object serialize(Alarm from, TimeZone timeZone, Session session) throws JSONException, OXException {
                RelatedTo relatedTo = get(from);
                if (null == relatedTo) {
                    return JSONObject.NULL;
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.putOpt("relType", relatedTo.getRelType());
                jsonObject.put("value", relatedTo.getValue());
                return jsonObject;
            }
        });
        mappings.put(AlarmField.ACKNOWLEDGED, new LongMapping<Alarm>(ChronosJsonFields.Alarm.ACK, null) {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsAcknowledged();
            }

            @Override
            public void set(Alarm object, Long value) throws OXException {
                object.setAcknowledged(new Date(value));
            }

            @Override
            public Long get(Alarm object) {
                Date acknowledged = object.getAcknowledged();
                return acknowledged != null ? acknowledged.getTime() : null;
            }

            @Override
            public void remove(Alarm object) {
                object.removeAcknowledged();
            }
        });
        mappings.put(AlarmField.ACTION, new StringMapping<Alarm>(ChronosJsonFields.Alarm.ACTION, null) {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsAction();
            }

            @Override
            public void set(Alarm object, String value) throws OXException {
                if (null == value) {
                    object.setAction(null);
                } else {
                    object.setAction(new AlarmAction(value));
                }
            }

            @Override
            public String get(Alarm object) {
                AlarmAction value = object.getAction();
                return null != value ? value.getValue() : null;
            }

            @Override
            public void remove(Alarm object) {
                object.removeAction();
            }
        });
        // AlarmField.REPEAT
        mappings.put(AlarmField.TRIGGER, new DefaultJsonMapping<Trigger, Alarm>(ChronosJsonFields.Alarm.TRIGGER, null) {

            @Override
            public boolean isSet(Alarm object) {
                return object.containsTrigger();
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
                object.removeTrigger();
            }

            @Override
            public void deserialize(JSONObject from, Alarm to) throws JSONException, OXException {
                JSONObject jsonObject = from.optJSONObject(getAjaxName());
                if (null == jsonObject) {
                    to.setTrigger(null);
                } else {
                    Trigger trigger = new Trigger();
                    if (jsonObject.has(ChronosJsonFields.Alarm.Trigger.RELATED)) {
                        trigger.setRelated(Enums.parse(Trigger.Related.class, jsonObject.getString(ChronosJsonFields.Alarm.Trigger.RELATED)));
                    }
                    if (jsonObject.has(ChronosJsonFields.Alarm.Trigger.DURATION)) {
                        trigger.setDuration(jsonObject.getString(ChronosJsonFields.Alarm.Trigger.DURATION));
                    }
                    if (jsonObject.has(ChronosJsonFields.Alarm.Trigger.DATE_TIME)) {
                        //TODO or use http api "timestamp" ?
                        DateTime dateTime = DateTime.parse("UTC", jsonObject.getString(ChronosJsonFields.Alarm.Trigger.DATE_TIME));
                        trigger.setDateTime(new Date(dateTime.getTimestamp()));
                    }
                    to.setTrigger(trigger);
                }
            }

            @Override
            public Object serialize(Alarm from, TimeZone timeZone, Session session) throws JSONException, OXException {
                Trigger trigger = get(from);
                if (null == trigger) {
                    return JSONObject.NULL;
                }
                JSONObject jsonObject = new JSONObject();
                if (null != trigger.getRelated()) {
                    jsonObject.put(ChronosJsonFields.Alarm.Trigger.RELATED, trigger.getRelated().name());
                }
                jsonObject.putOpt(ChronosJsonFields.Alarm.Trigger.DURATION, trigger.getDuration());
                if (null != trigger.getDateTime()) {
                    //TODO or use http api "timestamp" ?
                    DateTime dateTime = new DateTime(trigger.getDateTime().getTime());
                    jsonObject.put(ChronosJsonFields.Alarm.Trigger.DATE_TIME, dateTime.toString());
                }
                return jsonObject;
            }
        });
        mappings.put(AlarmField.EXTENDED_PROPERTIES, new ExtendedPropertiesMapping<Alarm>(ChronosJsonFields.EXTENDED_PROPERTIES, null) {

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
        mappings.put(AlarmField.ATTACHMENTS, new AttachmentsMapping<Alarm>(ChronosJsonFields.Alarm.ATTACHMENTS, null) {

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
        mappings.put(AlarmField.SUMMARY, new StringMapping<Alarm>(ChronosJsonFields.SUMMARY, null) {

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
        mappings.put(AlarmField.DESCRIPTION, new StringMapping<Alarm>(ChronosJsonFields.DESCRIPTION, null) {

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
        mappings.put(AlarmField.ATTENDEES, new AttendeesMapping<Alarm>(ChronosJsonFields.Alarm.ATTENDEES, null) {

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

        return mappings;
    }

}
