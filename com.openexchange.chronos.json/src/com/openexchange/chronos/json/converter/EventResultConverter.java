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

package com.openexchange.chronos.json.converter;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.osgi.Tools.requireService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.writer.GroupWriter;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.chronos.json.fields.ChronosGeneralJsonFields;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.java.util.TimeZones;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.json.ResourceWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
/**
 * {@link EventResultConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventResultConverter implements ResultConverter {

    public static final String INPUT_FORMAT = "event";

    private static final ContactField CONTACT_FIELDS_TO_LOAD[] = { ContactField.SUR_NAME, ContactField.GIVEN_NAME, ContactField.TITLE, ContactField.DISPLAY_NAME, ContactField.IMAGE1_URL, ContactField.IMAGE1, ContactField.INTERNAL_USERID };
    private static final ContactField CONTACT_FIELDS_TO_SHOW[] = { ContactField.SUR_NAME, ContactField.GIVEN_NAME, ContactField.TITLE, ContactField.DISPLAY_NAME, ContactField.IMAGE1_URL, ContactField.IMAGE1 };
    private static final Logger LOG = LoggerFactory.getLogger(EventResultConverter.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link EventResultConverter}.
     *
     * @param services A service lookup reference
     */
    public EventResultConverter(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getInputFormat() {
        return INPUT_FORMAT;
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        /*
         * check and convert result object
         */
        Object resultObject = result.getResultObject();
        if (Event.class.isInstance(resultObject)) {
            /*
             * only one event to convert
             */
            resultObject = convertEvent((Event) resultObject, getTimeZoneID(requestData, session), session, getFields(requestData), isExtendedEntities(requestData));
        } else if (List.class.isInstance(resultObject)) {
            /*
             * convert list of events
             */
            resultObject = convertEvents((List<Event>) resultObject, getTimeZoneID(requestData, session), session, getFields(requestData), isExtendedEntities(requestData));
        } else {
            throw new UnsupportedOperationException();
        }
        result.setResultObject(resultObject, getOutputFormat());
    }

    protected JSONObject convertEvent(Event event, String timeZoneID, Session session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        if (null == event) {
            return null;
        }
        /*
         * determine relevant fields
         */
        Set<EventField> fields = new HashSet<EventField>();
        for (Entry<EventField, ? extends JsonMapping<? extends Object, Event>> entry : EventMapper.getInstance().getMappings().entrySet()) {
            JsonMapping<? extends Object, Event> mapping = entry.getValue();
            if ((null == requestedFields || requestedFields.contains(entry.getKey())) && mapping.isSet(event) && null != mapping.get(event)) {
                fields.add(entry.getKey());
            }
        }
        try {
            JSONObject result = EventMapper.getInstance().serialize(event, fields.toArray(new EventField[fields.size()]), timeZoneID, session);
            Map<Integer, JSONObject> contactsToLoad = new HashMap<Integer, JSONObject>();

            if(extendedEntities && result.has(ChronosJsonFields.ATTENDEES)) {
                JSONArray jsonArray = result.getJSONArray(ChronosJsonFields.ATTENDEES);
                Iterator<Object> iterator = jsonArray.iterator();
                ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                while(iterator.hasNext()) {
                    JSONObject attendee = (JSONObject) iterator.next();
                    int entity = attendee.optInt(ChronosJsonFields.Attendee.ENTITY);
                    if (0 >= entity) {
                        continue;
                    }
                    CalendarUserType cuType = new CalendarUserType(attendee.optString(ChronosJsonFields.Attendee.CU_TYPE, CalendarUserType.INDIVIDUAL.getValue()));
                    if (CalendarUserType.INDIVIDUAL.matches(cuType)) {
                        contactsToLoad.put(I(entity), attendee);
                    } else if (CalendarUserType.ROOM.matches(cuType) || CalendarUserType.RESOURCE.matches(cuType)) {
                        try {
                            Resource resource = requireService(ResourceService.class, services).getResource(entity, serverSession.getContext());
                            attendee.put(ChronosJsonFields.Attendee.RESOURCE, ResourceWriter.writeResource(resource));
                        } catch (OXException e) {
                            LOG.warn("Error getting underlying resource for attendee {}", attendee, e);
                        }
                    } else if (CalendarUserType.GROUP.matches(cuType)) {

                        try {
                            JSONObject jsonObject = new JSONObject();
                            Group group = requireService(GroupService.class, services).getGroup(serverSession.getContext(), entity);
                            new GroupWriter().writeGroup(group, jsonObject);
                            attendee.put(ChronosJsonFields.Attendee.GROUP, jsonObject);
                        } catch (OXException e) {
                            LOG.warn("Error getting underlying group for attendee {}", attendee, e);
                        }
                    }
                }
            }

            if (0 < contactsToLoad.size()) {
                SearchIterator<Contact> users = null;
                try {
                    users = requireService(ContactService.class, services).getUsers(session, I2i(contactsToLoad.keySet()), CONTACT_FIELDS_TO_LOAD);
                    while (users.hasNext()) {
                        Contact con = users.next();
                        JSONObject attendee = contactsToLoad.get(I(con.getInternalUserId()));
                        if (attendee == null) {
                            LOG.warn("Unable to find attendee for contact with id {}", I(con.getInternalUserId()));
                            continue;
                        }
                        attendee.put(ChronosJsonFields.Attendee.CONTACT, serialize(con, CalendarUtils.optTimeZone(timeZoneID, TimeZones.UTC), session));
                    }
                } catch (OXException e) {
                    LOG.warn("Error resolving contacts for internal user attendees", e);
                } finally {
                    SearchIterators.close(users);
                }
            }

            return result;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

    protected JSONArray convertEvents(List<Event> events, String timeZoneID, Session session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        if (null == events) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(events.size());
        for (Event event : events) {
            jsonArray.put(convertEvent(event, timeZoneID, session, requestedFields, extendedEntities));
        }
        return jsonArray;
    }

    protected static String getTimeZoneID(AJAXRequestData requestData, ServerSession session) {
        String timeZoneID = requestData.getParameter(ChronosGeneralJsonFields.TIMEZONE);
        return null == timeZoneID ? session.getUser().getTimeZone() : timeZoneID;
    }

    protected static Set<EventField> getFields(AJAXRequestData requestData) throws OXException {
        return EventMapper.getInstance().parseFields(requestData.getParameter(CalendarParameters.PARAMETER_FIELDS));
    }

    protected static boolean isExtendedEntities(AJAXRequestData requestData) throws OXException {
        Boolean value = requestData.getParameter("extendedEntities", Boolean.class, true);
        return null != value ? value.booleanValue() : false;
    }

    /**
     * Serializes contact data to JSON.
     *
     * @param contact The contact to serialize
     * @param timeZone The client time zone to consider
     * @param session The underlying session
     */
    private static JSONObject serialize(Contact contact, TimeZone timeZone, Session session) throws OXException, JSONException {
        JSONObject jsonObject = new JSONObject();
        for (ContactField field : CONTACT_FIELDS_TO_SHOW) {
            JsonMapping<? extends Object, Contact> mapping = ContactMapper.getInstance().get(field);
            if (mapping.isSet(contact)) {
                mapping.serialize(contact, jsonObject, timeZone, session);
            }
        }
        return jsonObject;
    }

}
