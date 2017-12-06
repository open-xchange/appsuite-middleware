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

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.google.GoogleCalendarConfigField;
import com.openexchange.chronos.provider.google.config.GoogleCalendarConfig;
import com.openexchange.chronos.provider.google.converter.GoogleEventConverter;
import com.openexchange.chronos.provider.google.converter.GoogleEventConverter.GoogleItemMapping;
import com.openexchange.chronos.provider.google.exception.GoogleExceptionCodes;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link GoogleCalendarAccess}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarAccess extends BasicCachingCalendarAccess {

    private final GoogleOAuthAccess oauthAccess;
    private final CalendarParameters parameters;

    private CalendarFolder folder;
    private final long refreshInterval;
    private final long requestTimeout;

    /**
     * Initializes a new {@link GoogleCalendarAccess}.
     *
     * @param session The user session
     * @param account The calendar account
     * @param parameters The calendar parameters
     * @throws OXException
     */
    public GoogleCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters, boolean checkConfig) throws OXException {
        super(session, account, parameters);
        this.parameters = parameters;
        this.account = account;
        refreshInterval = GoogleCalendarConfig.getResfrehInterval(session);
        requestTimeout = GoogleCalendarConfig.getRequestTimeout(session);
        oauthAccess = new GoogleOAuthAccess(account, session);
        oauthAccess.initialize();
        if (checkConfig) {
            if (account.getUserConfiguration().has(GoogleCalendarConfigField.MIGRATED)) {
                account.getUserConfiguration().remove(GoogleCalendarConfigField.MIGRATED);
                AdministrativeCalendarAccountService calendarAccountService = Services.getService(AdministrativeCalendarAccountService.class);
                this.account = calendarAccountService.updateAccount(session.getContextId(), session.getUserId(), account.getAccountId(), account.isEnabled(), null, account.getUserConfiguration(), System.currentTimeMillis());
            }
            initCalendarFolder();
        }
    }

    /**
     * Initializes the calendar folder and returns an updated internal configuration or null.
     *
     * @return the internal configuration or null if unchanged
     * @throws OXException
     */
    public JSONObject initCalendarFolder() throws OXException {
        try {
            JSONObject internalConfiguration = account.getInternalConfiguration();
            if (internalConfiguration == null) {
                internalConfiguration = new JSONObject();
            }

            JSONObject userConfiguration = account.getUserConfiguration();
            String folderId = null;
            if (userConfiguration.has(GoogleCalendarConfigField.FOLDER)) {
                folderId = userConfiguration.getString(GoogleCalendarConfigField.FOLDER);
            }
            Calendar googleCal = (Calendar) oauthAccess.getClient().getClient();
            CalendarList calendars = googleCal.calendarList().list().execute();


            if (internalConfiguration.has(GoogleCalendarConfigField.MIGRATED)) {
                internalConfiguration.remove(GoogleCalendarConfigField.MIGRATED);
                userConfiguration.remove(GoogleCalendarConfigField.MIGRATED);
            }

            for (CalendarListEntry entry : calendars.getItems()) {
                if (Strings.isEmpty(folderId) && entry.getPrimary()) {
                    folder = addEntry(entry, internalConfiguration);
                    internalConfiguration.put(GoogleCalendarConfigField.PRIMARY, Boolean.TRUE);
                    break;
                } else if (entry.getId().equals(folderId)) {
                    folder = addEntry(entry, internalConfiguration);
                    break;
                }
            }

            if (this.folder == null) {
                throw GoogleExceptionCodes.CALENDAR_NOT_FOUND.create(folderId);
            }

            return internalConfiguration;
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private CalendarFolder addEntry(CalendarListEntry entry, JSONObject config) throws JSONException, OXException {
        CalendarFolder result = prepareFolder(entry.getId(), entry.getSummary(), getCalendarColor(entry), entry.getDescription(), Services.getService(ConversionService.class));
        if (entry.getBackgroundColor() != null) {
            config.put(GoogleCalendarConfigField.COLOR, entry.getBackgroundColor());
        }
        if (entry.getDefaultReminders() != null) {
            JSONArray array = new JSONArray();
            for (EventReminder reminder : entry.getDefaultReminders()) {
                JSONObject json = new JSONObject(2);
                @SuppressWarnings("unchecked") GoogleItemMapping<EventReminder, Alarm> mapping = (GoogleItemMapping<EventReminder, Alarm>) GoogleEventConverter.getInstance().getMapping(EventField.ALARMS);
                Alarm alarm = mapping.convert(reminder);
                json.put("action", alarm.getAction().getValue());
                json.put("duration", alarm.getTrigger().getDuration());
                array.put(json);
            }
            config.put(GoogleCalendarConfigField.DEFAULT_REMINDER, array);
        }
        if (entry.getDescription() != null) {
            config.put(GoogleCalendarConfigField.DESCRIPTION, entry.getBackgroundColor());
        }
        return result;
    }

    private String getCalendarColor(CalendarListEntry entry) {
        if (entry.getBackgroundColor() != null) {
            return entry.getBackgroundColor();
        }
        if (entry.getForegroundColor() != null) {
            return entry.getForegroundColor();
        }

        if (entry.getColorId() != null) {
            return entry.getColorId();
        }
        return null;
    }

    @Override
    public void close() {
        oauthAccess.dispose();
    }

    /**
     * Merges incoming extended properties as passed from the client during an update operation into a collection of original extended
     * properties.
     * <p>/
     * Any new properties or attempts to modify <i>protected</i> properties are rejected implicitly.
     *
     * @param originalProperties The original properties
     * @param updatedProperties The updated properties
     * @return The merged properties
     */
    protected static ExtendedProperties merge(ExtendedProperties originalProperties, ExtendedProperties updatedProperties) throws OXException {
        ExtendedProperties mergedProperties = new ExtendedProperties(originalProperties);
        if (null != updatedProperties && 0 < updatedProperties.size()) {
            for (ExtendedProperty updatedProperty : updatedProperties) {
                ExtendedProperty originalProperty = originalProperties.get(updatedProperty.getName());
                if (null == originalProperty) {
                    throw OXException.noPermissionForFolder();
                }
                if (originalProperty.equals(updatedProperty)) {
                    continue;
                }
                if (CalendarFolderProperty.isProtected(originalProperty)) {
                    throw OXException.noPermissionForFolder();
                }
                mergedProperties.remove(originalProperty);
                mergedProperties.add(updatedProperty);
            }
        }
        return mergedProperties;
    }

    /**
     * Serializes an extended properties container to JSON.
     *
     * @param conversionService A reference to the conversion service
     * @param properties The properties to serialize
     * @return The serialized extended properties
     */
    protected static JSONObject writeExtendedProperties(ConversionService conversionService, ExtendedProperties properties) throws OXException {
        if (null != properties) {
            DataHandler dataHandler = conversionService.getDataHandler(DataHandlers.XPROPERTIES2JSON);
            ConversionResult result = dataHandler.processData(new SimpleData<ExtendedProperties>(properties), new DataArguments(), null);
            if (null != result && null != result.getData() && JSONObject.class.isInstance(result.getData())) {
                return (JSONObject) result.getData();
            }
        }
        return null;
    }

    private Event convertEvent(com.google.api.services.calendar.model.Event event) throws OXException {
        if (parameters.contains(CalendarParameters.PARAMETER_FIELDS)) {
            EventField[] eventFields = parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
            return GoogleEventConverter.getInstance().convertToEvent(event, eventFields);
        }
        return GoogleEventConverter.getInstance().convertToEvent(event);
    }

    GoogleEventsPage getEventsInFolder(String folderId, String token, boolean isSyncToken) throws OXException {
        try {
            Calendar googleCal = (Calendar) oauthAccess.getClient().getClient();
            com.google.api.services.calendar.Calendar.Events.List list = googleCal.events().list(folderId);

            if (token != null) {
                if (isSyncToken) {
                    list.setShowDeleted(true);
                    list.setSyncToken(token);
                } else {
                    list.setPageToken(token);
                }
            }

            Events events = list.execute();
            List<Event> result = new ArrayList<>(events.size());
            for (com.google.api.services.calendar.model.Event event : events.getItems()) {
                result.add(convertEvent(event));
            }
            if (events.getNextSyncToken() != null) {
                account.getInternalConfiguration().put(GoogleCalendarConfigField.SYNC_TOKEN, events.getNextSyncToken());
                return new GoogleEventsPage(result, null);
            } else {
                return new GoogleEventsPage(result, events.getNextPageToken());
            }
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public long getRetryAfterErrorInterval() {
        return requestTimeout;
    }

    private CalendarFolder prepareFolder(String folderId, String summary, String color, String description, ConversionService conversionService) throws OXException {
        DefaultCalendarFolder folder = new DefaultCalendarFolder(folderId, summary);
        folder.setPermissions(Collections.singletonList(DefaultCalendarPermission.readOnlyPermissionsFor(account.getUserId())));
        folder.setLastModified(account.getLastModified());

        JSONObject folderConfig = account.getInternalConfiguration() != null ? account.getInternalConfiguration().optJSONObject(folderId) : null;

        ExtendedProperties extendedProperties = null;
        if (folderConfig != null) {
            extendedProperties = parseExtendedProperties(conversionService, folderConfig.optJSONObject("extendedProperties"));
        }
        if (null == extendedProperties) {
            extendedProperties = new ExtendedProperties();
        }
        /*
         * always apply or overwrite protected defaults
         */
        extendedProperties.replace(SCHEDULE_TRANSP(TimeTransparency.OPAQUE, true));
        extendedProperties.replace(DESCRIPTION(description, true));
        extendedProperties.replace(USED_FOR_SYNC(Boolean.FALSE, true));
        /*
         * insert further defaults if missing
         */
        if (false == extendedProperties.contains(COLOR_LITERAL)) {
            extendedProperties.add(COLOR(color, false));
        }
        folder.setExtendedProperties(extendedProperties);

        return folder;
    }

    /**
     * Deserializes an extended properties container from JSON.
     *
     * @param conversionService A reference to the conversion service
     * @param jsonObject The JSON object to parse the properties from
     * @return The parsed extended properties
     */
    protected static ExtendedProperties parseExtendedProperties(ConversionService conversionService, JSONObject jsonObject) throws OXException {
        if (null != jsonObject) {
            DataHandler dataHandler = conversionService.getDataHandler(DataHandlers.JSON2XPROPERTIES);
            ConversionResult result = dataHandler.processData(new SimpleData<JSONObject>(jsonObject), new DataArguments(), null);
            if (null != result && null != result.getData() && ExtendedProperties.class.isInstance(result.getData())) {
                return (ExtendedProperties) result.getData();
            }
        }
        return null;
    }

    @Override
    protected String getName() {
        return GoogleCalendarAccess.class.getSimpleName();
    }

    @Override
    protected long getRefreshInterval() throws OXException {
        return refreshInterval;
    }

    @Override
    protected void handleExceptions(OXException e) {
        // Nothing to do
    }

    @Override
    protected ExternalCalendarResult getAllEvents() throws OXException {
        return new GoogleCalendarResult(this);
    }

    protected CalendarAccount getAccount() {
        return account;
    }
}
