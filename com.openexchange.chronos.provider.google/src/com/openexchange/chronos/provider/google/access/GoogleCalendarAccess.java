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

package com.openexchange.chronos.provider.google.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Events;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.google.converter.GoogleEventConverter;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link GoogleCalendarAccess}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarAccess implements CalendarAccess {

    private final GoogleOAuthAccess oauthAccess;
    private final CalendarParameters parameters;
    private CalendarAccount account;

    private final Map<String, CalendarFolder> folders = new HashMap<>(1);
    private final Session session;

    /**
     * Initializes a new {@link GoogleCalendarAccess}.
     *
     * @param session The user session
     * @param account The calendar account
     * @param parameters The calendar parameters
     * @throws OXException
     */
    public GoogleCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters, boolean checkConfig) throws OXException {
        this.parameters = parameters;
        this.account = account;
        this.session = session;
        oauthAccess = new GoogleOAuthAccess(account, session);
        oauthAccess.initialize();
        if(checkConfig){
            initCalendarFolder();
        }
    }

    /**
    * Initializes the calendar folder and returns an updated internal configuration or null.
    *
    * @return the internal configuration
    * @throws OXException
    */
    public JSONObject initCalendarFolder() throws OXException {
        try {
            Calendar googleCal = (Calendar) oauthAccess.getClient().getClient();
            CalendarList calendars = googleCal.calendarList().list().execute();
            JSONObject internalConfiguration = account.getInternalConfiguration();
            JSONObject userConfiguration = account.getUserConfiguration();
            boolean changed = false;
            JSONObject newConfig = new JSONObject();
            for(CalendarListEntry entry: calendars.getItems()){

                // enabled==true if user config is 'true' or if internal config is 'true' and user config is empty
                Boolean internalCheck = checkConfig(internalConfiguration, entry.getId());
                Boolean userCheck = checkConfig(userConfiguration, entry.getId());
                if( (userCheck != null && userCheck)){
                    addEntry(entry, true, newConfig);
                    if(internalCheck == null || !internalCheck){
                        // new config
                       changed = true;
                    }
                } else if (userCheck==null && internalCheck != null && internalCheck) {
                    addEntry(entry, true, newConfig);
                } else {
                    addEntry(entry, false, newConfig);
                }
            }

            if(changed || getConfig(internalConfiguration).size() != newConfig.asMap().size()){
                internalConfiguration.put("folders", newConfig);
                userConfiguration.put("folders", newConfig);

                AdministrativeCalendarAccountService service = Services.getService(AdministrativeCalendarAccountService.class);
                account = service.updateAccount(session.getContextId(), session.getUserId(), account.getAccountId(), internalConfiguration, userConfiguration, account.getLastModified().getTime());
            }

            return internalConfiguration;
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private Map<String, Object> getConfig(JSONObject json) throws JSONException{
        if(json.has("folders")){
            return json.getJSONObject("folders").asMap();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Boolean checkConfig(JSONObject config, String key) throws JSONException{
        Map<String, Object> map = getConfig(config);
        return map == null ? null : !map.containsKey(key) ? null : Boolean.valueOf((boolean) ((Map<String, Object>) map.get(key)).get("enabled"));
    }

    private void addEntry(CalendarListEntry entry, boolean enabled, JSONObject newConfig) throws JSONException{
        folders.put(String.valueOf(entry.getId()), new DefaultCalendarFolder(entry.getId(), entry.getSummary()));
        JSONObject config = new JSONObject();
        config.put("enabled", enabled);
        newConfig.put(entry.getId(), config);
    }

    @Override
    public void close() {
        oauthAccess.dispose();
    }

    @Override
    public CalendarFolder getFolder(String folderId) throws OXException {
        return folders.get(folderId);
    }

    @Override
    public List<CalendarFolder> getVisibleFolders() throws OXException {
        return Collections.unmodifiableList(new ArrayList<>(folders.values()));
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        // TODO update folder or throw exception if not possible
        return folderId;
    }

    @Override
    public Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        try {
            Calendar googleCal = (Calendar) oauthAccess.getClient().getClient();
            com.google.api.services.calendar.model.Event event = googleCal.events().get(folderId, eventId).execute();
            return convertEvent(event);
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    private Event convertEvent(com.google.api.services.calendar.model.Event event) throws OXException{
        if(parameters.contains(CalendarParameters.PARAMETER_FIELDS)){
            EventField[] eventFields = parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
            return GoogleEventConverter.getInstance().convertToEvent(event, eventFields);
        }
        return GoogleEventConverter.getInstance().convertToEvent(event);
    }

    @Override
    public List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        List<Event> result = new ArrayList<>(eventIDs.size());
        for(EventID id: eventIDs){
            result.add(getEvent(id.getFolderID(), id.getObjectID(), id.getRecurrenceID()));
        }
        return result;
    }

    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        // TODO can be deleted?
        return null;
    }

    @Override
    public List<Event> getEventsInFolder(String folderId) throws OXException {
        try {
            Calendar googleCal = (Calendar) oauthAccess.getClient().getClient();
            com.google.api.services.calendar.Calendar.Events.List list = googleCal.events().list(folderId);

            Date startDate = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
            Date endDate = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
            DateTime startDateTime = new DateTime(startDate);
            list = list.setTimeMin(startDateTime);
            DateTime endDateTime = new DateTime(endDate);
            list = list.setTimeMax(endDateTime);

            if(parameters.contains(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES) && parameters.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class)){
                list.setSingleEvents(true);
            }

            Events events = list.execute();
            List<Event> result = new ArrayList<>(events.size());
            for(com.google.api.services.calendar.model.Event event : events.getItems()){
                result.add(convertEvent(event));
            }

            // Sort by order
            return result;
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

}
