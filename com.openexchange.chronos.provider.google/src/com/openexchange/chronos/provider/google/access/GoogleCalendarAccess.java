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

import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Events;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarConstants;
import com.openexchange.chronos.provider.google.GoogleCalendarConfigField;
import com.openexchange.chronos.provider.google.config.GoogleCalendarConfig;
import com.openexchange.chronos.provider.google.converter.GoogleEventConverter;
import com.openexchange.chronos.provider.google.exception.GoogleExceptionCodes;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.java.Strings;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link GoogleCalendarAccess}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarAccess extends BasicCachingCalendarAccess {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleCalendarAccess.class);

    private final GoogleOAuthAccess oauthAccess;
    private boolean initialized = false;
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
        super(session, account, parameters, null);
        refreshInterval = GoogleCalendarConfig.getResfrehInterval(session);
        requestTimeout = GoogleCalendarConfig.getRetryOnErrorInterval(session);
        try {
            oauthAccess = new GoogleOAuthAccess(account.getUserConfiguration().getInt(GoogleCalendarConfigField.OAUTH_ID), session);
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        init(checkConfig);
    }

    private void init(boolean checkConfig) throws OXException {
        if (!initialized) {
            oauthAccess.initialize();
            initialized = true;

            if (checkConfig) {
                initCalendarFolder(session);
            }
        }
    }

    /**
     * Initializes the calendar folder
     *
     * @param session
     *
     * @throws OXException
     */
    public void initCalendarFolder(Session session) throws OXException {
        try {
            JSONObject internalConfiguration = account.getInternalConfiguration();
            if (internalConfiguration == null) {
                internalConfiguration = new JSONObject();
            }

            if (internalConfiguration.has(GoogleCalendarConfigField.OLD_FOLDER)) {
                FolderService folderService = Services.getService(FolderService.class);
                try {
                    folderService.deleteFolder("0", internalConfiguration.getString(GoogleCalendarConfigField.OLD_FOLDER), new Date(), session, new FolderServiceDecorator());
                    internalConfiguration.remove(GoogleCalendarConfigField.OLD_FOLDER);
                } catch (OXException e) {
                    // ignore
                    LOG.debug("{}", e.getMessage(), e);
                }
            }

            if (internalConfiguration.has(GoogleCalendarConfigField.FOLDER)) {
                return;
            }

            JSONObject userConfiguration = account.getUserConfiguration();
            String folderId = null;
            if (userConfiguration.has(GoogleCalendarConfigField.FOLDER)) {
                folderId = userConfiguration.getString(GoogleCalendarConfigField.FOLDER);
            }
            Calendar googleCal = (Calendar) oauthAccess.getClient().getClient();
            CalendarList calendars = googleCal.calendarList().list().execute();

            boolean found = false;
            for (Iterator<CalendarListEntry> it = calendars.getItems().iterator(); !found && it.hasNext();) {
                CalendarListEntry entry = it.next();
                if (Strings.isEmpty(folderId) && (null != entry.getPrimary() && entry.getPrimary().booleanValue())) {
                    internalConfiguration.put(GoogleCalendarConfigField.FOLDER, entry.getId());
                    internalConfiguration.put(GoogleCalendarConfigField.PRIMARY, Boolean.TRUE);
                    found = true;
                } else if (entry.getId().equals(folderId)) {
                    internalConfiguration.put(GoogleCalendarConfigField.FOLDER, entry.getId());
                    found = true;
                }
            }

            if (!found) {
                throw GoogleExceptionCodes.CALENDAR_NOT_FOUND.create(folderId);
            }
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
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
            if (null != dataHandler) {
                ConversionResult result = dataHandler.processData(new SimpleData<ExtendedProperties>(properties), new DataArguments(), null);
                if (null != result && null != result.getData() && JSONObject.class.isInstance(result.getData())) {
                    return (JSONObject) result.getData();
                }
            }
        }
        return null;
    }

    private Event convertEvent(com.google.api.services.calendar.model.Event event) throws OXException {
        return GoogleEventConverter.getInstance().convertToEvent(event);
    }

    GoogleEventsPage getEventsInFolder(String folderId, String token, boolean isSyncToken) throws OXException {
        init(true);
        try {
            Calendar googleCal = (Calendar) oauthAccess.getClient().getClient();
            com.google.api.services.calendar.Calendar.Events.List list = googleCal.events().list(folderId);
            list.setAlwaysIncludeEmail(Boolean.TRUE);

            if (token != null) {
                if (isSyncToken) {
                    list.setShowDeleted(Boolean.TRUE);
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
            }
            return new GoogleEventsPage(result, events.getNextPageToken());
        } catch (IOException e) {
            throw CalendarExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public long getRetryAfterErrorInterval(OXException e) {
        if (e == null || e.getExceptionCode() == null || CalendarExceptionCodes.AUTH_FAILED.equals(e) || OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.getPrefix().equals(e.getPrefix())) {
            return BasicCachingCalendarConstants.MINIMUM_DEFAULT_RETRY_AFTER_ERROR_INTERVAL;
        }
        return requestTimeout;
    }

    /**
     * Deserializes an extended properties container from JSON.
     *
     * @param conversionService A reference to the conversion service
     * @param jsonObject The JSON object to parse the properties from
     * @return The parsed extended properties or <code>null</code> if there aren't any.
     */
    protected @Nullable static ExtendedProperties parseExtendedProperties(ConversionService conversionService, JSONObject jsonObject) throws OXException {
        if (null == jsonObject) {
            return null;
        }
        DataHandler dataHandler = conversionService.getDataHandler(DataHandlers.JSON2XPROPERTIES);
        if (null == dataHandler) {
            return null;
        }
        ConversionResult result = dataHandler.processData(new SimpleData<JSONObject>(jsonObject), new DataArguments(), null);
        if (null != result && null != result.getData() && ExtendedProperties.class.isInstance(result.getData())) {
            return (ExtendedProperties) result.getData();
        }
        return null;
    }

    @Override
    protected long getRefreshInterval() {
        return refreshInterval;
    }

    @Override
    public ExternalCalendarResult getAllEvents() throws OXException {
        init(true);
        return new GoogleCalendarResult(this);
    }

    @Override
    public CalendarSettings getSettings() {
        CalendarSettings settings = super.getSettings();
        settings.getExtendedProperties().replace(USED_FOR_SYNC(Boolean.FALSE, true)); // never synchronizable
        return settings;
    }

    @Override
    public @Nullable List<OXException> getWarnings() {
        // TODO handle warnings
        return null;
    }
}
