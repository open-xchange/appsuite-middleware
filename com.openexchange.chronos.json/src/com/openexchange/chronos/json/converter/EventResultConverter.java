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
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.osgi.Tools.requireService;
import java.util.HashMap;
import java.util.HashSet;
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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.java.Strings;
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

    protected JSONObject convertEvent(Event event, String timeZoneID, ServerSession session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
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
            if (extendedEntities) {
                /*
                 * also resolve via email address for externally organized events
                 */
                JSONObject jsonOrganizer = result.optJSONObject(ChronosJsonFields.ORGANIZER);
                boolean resolveExternals = null != jsonOrganizer && 0 >= jsonOrganizer.optInt(ChronosJsonFields.Attendee.ENTITY);
                extendEntities(session, timeZoneID, result, resolveExternals);
            }
            return result;
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

    protected JSONArray convertEvents(List<Event> events, String timeZoneID, ServerSession session, Set<EventField> requestedFields, boolean extendedEntities) throws OXException {
        if (null == events) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(events.size());
        for (Event event : events) {
            jsonArray.put(convertEvent(event, timeZoneID, session, requestedFields, extendedEntities));
        }
        return jsonArray;
    }

    /**
     * Enriches the attendees within the supplied event with detailed information about the underlying groupware resource.
     *
     * @param session The session
     * @param timeZoneID The timezone identifier to use
     * @param jsonEvent The JSON representation of the event being converted
     * @param resolveExternals <code>true</code> to also try to resolve external attendees per email address, <code>false</code>, otherwise
     */
    private void extendEntities(ServerSession session, String timeZoneID, JSONObject jsonEvent, boolean resolveExternals) {
        Map<Integer, JSONObject> userAttendeesPerId = new HashMap<Integer, JSONObject>();
        Map<Integer, JSONObject> resourceAttendeesPerId = new HashMap<Integer, JSONObject>();
        Map<Integer, JSONObject> groupAttendeesPerId = new HashMap<Integer, JSONObject>();
        Map<String, JSONObject> userAttendeesPerEmail = new HashMap<String, JSONObject>();
        /*
         * collect resolvable attendees
         */
        JSONArray jsonAttendees = jsonEvent.optJSONArray(ChronosJsonFields.ATTENDEES);
        if (null != jsonAttendees) {
            for (int i = 0; i < jsonAttendees.length(); i++) {
                JSONObject jsonAttendee = jsonAttendees.optJSONObject(i);
                if (null == jsonAttendee) {
                    continue;
                }
                CalendarUserType cuType = new CalendarUserType(jsonAttendee.optString(ChronosJsonFields.Attendee.CU_TYPE, CalendarUserType.INDIVIDUAL.getValue()));
                int entity = jsonAttendee.optInt(ChronosJsonFields.Attendee.ENTITY, -1);
                if (0 <= entity) {
                    if (CalendarUserType.INDIVIDUAL.matches(cuType)) {
                        userAttendeesPerId.put(I(entity), jsonAttendee);
                    } else if (CalendarUserType.ROOM.matches(cuType) || CalendarUserType.RESOURCE.matches(cuType)) {
                        resourceAttendeesPerId.put(I(entity), jsonAttendee);
                    } else if (CalendarUserType.GROUP.matches(cuType)) {
                        groupAttendeesPerId.put(I(entity), jsonAttendee);
                    }
                } else if (resolveExternals && CalendarUserType.INDIVIDUAL.matches(cuType) && 
                    Strings.isEmpty(jsonAttendee.optString(ChronosJsonFields.Attendee.CN, null))) {
                    String email = CalendarUtils.optEMailAddress(jsonAttendee.optString(ChronosJsonFields.Attendee.URI, null));
                    if (Strings.isNotEmpty(email)) {
                        userAttendeesPerEmail.put(email, jsonAttendee);
                    }
                }
            }
        }
        /*
         * resolve & apply extended entity information
         */
        addGroupDetailsFromId(session.getContext(), groupAttendeesPerId);
        addResourceDetailsFromId(session.getContext(), resourceAttendeesPerId);
        addContactDetailsFromId(session, timeZoneID, userAttendeesPerId);
        addContactDetailsFromEmail(session, timeZoneID, userAttendeesPerEmail);
    }

    private void addGroupDetailsFromId(Context context, Map<Integer, JSONObject> attendeesPerId) {
        if (null == attendeesPerId || attendeesPerId.isEmpty()) {
            return;
        }
        for (Entry<Integer, JSONObject> entry : attendeesPerId.entrySet()) {
            try {
                Group group = requireService(GroupService.class, services).getGroup(context, i(entry.getKey()));
                JSONObject jsonObject = new JSONObject();
                new GroupWriter().writeGroup(group, jsonObject);
                entry.getValue().put(ChronosJsonFields.Attendee.GROUP, jsonObject);
            } catch (OXException | JSONException e) {
                LOG.warn("Error adding group details for attendee", e);
            }
        }
    }

    private void addResourceDetailsFromId(Context context, Map<Integer, JSONObject> attendeesPerId) {
        if (null == attendeesPerId || attendeesPerId.isEmpty()) {
            return;
        }
        for (Entry<Integer, JSONObject> entry : attendeesPerId.entrySet()) {
            try {
                Resource resource = requireService(ResourceService.class, services).getResource(i(entry.getKey()), context);
                entry.getValue().put(ChronosJsonFields.Attendee.RESOURCE, ResourceWriter.writeResource(resource));
            } catch (OXException | JSONException e) {
                LOG.warn("Error adding resource details for attendee", e);
            }
        }
    }

    private void addContactDetailsFromEmail(Session session, String timeZoneID, Map<String, JSONObject> attendeesPerEmail) {
        if (null == attendeesPerEmail || attendeesPerEmail.isEmpty()) {
            return;
        }
        for (Entry<String, JSONObject> entry : attendeesPerEmail.entrySet()) {
            ContactSearchObject contactSearch = new ContactSearchObject();
            contactSearch.setExactMatch(true);
            contactSearch.setAllEmail(entry.getKey());
            contactSearch.setOrSearch(true);
            contactSearch.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID);
            SearchIterator<Contact> users = null;
            try {
                users = requireService(ContactService.class, services).searchUsers(session, contactSearch, CONTACT_FIELDS_TO_LOAD, null);
                if (users.hasNext()) {
                    Contact con = users.next();
                    entry.getValue().put(ChronosJsonFields.Attendee.CONTACT, serialize(con, CalendarUtils.optTimeZone(timeZoneID, TimeZones.UTC), session));
                }
            } catch (OXException | JSONException e) {
                LOG.warn("Error adding contact details for internal user attendees", e);
            } finally {
                SearchIterators.close(users);
            }
        }
    }

    private void addContactDetailsFromId(Session session, String timeZoneID, Map<Integer, JSONObject> attendeesPerId) {
        if (null == attendeesPerId || attendeesPerId.isEmpty()) {
            return;
        }
        SearchIterator<Contact> users = null;
        try {
            users = requireService(ContactService.class, services).getUsers(session, I2i(attendeesPerId.keySet()), CONTACT_FIELDS_TO_LOAD);
            while (users.hasNext()) {
                Contact con = users.next();
                JSONObject attendee = attendeesPerId.get(I(con.getInternalUserId()));
                if (attendee == null) {
                    LOG.warn("Unable to find attendee for contact with id {}", I(con.getInternalUserId()));
                    continue;
                }
                attendee.put(ChronosJsonFields.Attendee.CONTACT, serialize(con, CalendarUtils.optTimeZone(timeZoneID, TimeZones.UTC), session));
            }
        } catch (OXException | JSONException e) {
            LOG.warn("Error adding contact details for internal user attendees", e);
        } finally {
            SearchIterators.close(users);
        }
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
