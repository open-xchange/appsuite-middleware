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

package com.openexchange.chronos.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.session.Session;

/**
 * {@link SingleFolderCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class SingleFolderCalendarAccess implements CalendarAccess {

    /** The constant folder identifier for single folder calendar accesses */
    public static final String FOLDER_ID = "0";

    protected final Session session;
    protected final CalendarFolder folder;
    protected final CalendarParameters parameters;

    protected CalendarAccount account;

    /**
     * Initializes a new {@link SingleFolderCalendarAccess}.
     *
     * @param session The user session
     * @param parameters The calendar parameters
     * @param account The calendar account
     * @param folder The folder to use
     */
    protected SingleFolderCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters, CalendarFolder folder) {
        super();
        this.session = session;
        this.account = account;
        this.parameters = parameters;
        this.folder = folder;
    }

    @Override
    public CalendarFolder getFolder(String folderId) throws OXException {
        checkFolderId(folderId);
        return folder;
    }

    @Override
    public List<CalendarFolder> getVisibleFolders() throws OXException {
        return Collections.singletonList(folder);
    }

    @Override
    public Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        checkFolderId(folderId);
        Event event = getEvent(eventId, recurrenceId);
        //        event.setFolderId(folderId);
        return event;
    }

    protected abstract Event getEvent(String eventId, RecurrenceId recurrenceId) throws OXException;

    protected abstract List<Event> getEvents() throws OXException;

    protected abstract CalendarAccountService getAccountService() throws OXException;

    @Override
    public List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        List<Event> events = new ArrayList<Event>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            checkFolderId(eventID.getFolderID());
            events.add(getEvent(eventID.getObjectID(), eventID.getRecurrenceID()));
        }
        return events;
    }

    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        checkFolderId(folderId);
        List<Event> events = new ArrayList<Event>();
        for (Event event : getEvents()) {
            if (CalendarUtils.isSeriesException(event) && seriesId.equals(event.getSeriesId())) {
                event.setFolderId(folderId);
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public List<Event> getEventsInFolder(String folderId) throws OXException {
        checkFolderId(folderId);
        List<Event> events = new ArrayList<Event>();
        for (Event event : getEvents()) {
            if (CalendarUtils.isInRange(event, getFrom(), getUntil(), TimeZones.UTC)) {
                //                event.setFolderId(folderId);
                events.add(event);
            }
        }
        return events;
    }

    protected Date getFrom() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    protected Date getUntil() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

    protected String checkFolderId(String folderId) throws OXException {
        if (false == folder.getId().equals(folderId)) {
            throw CalendarExceptionCodes.FOLDER_NOT_FOUND.create(folderId);
        }
        return folderId;
    }

    //    protected static DefaultCalendarFolder prepareFolder(ConversionService conversionService, Session session, CalendarAccount account) throws OXException {
    //        DefaultCalendarFolder folder = new DefaultCalendarFolder();
    //        folder.setId(FOLDER_ID);
    //        folder.setPermissions(Collections.singletonList(DefaultCalendarPermission.readOnlyPermissionsFor(account.getUserId())));
    //        folder.setLastModified(account.getLastModified());
    //        JSONObject userConfig = account.getUserConfiguration();
    //        if (null != userConfig) {
    //            folder.setExtendedProperties(parseExtendedProperties(conversionService, userConfig.optJSONArray("extendedProperties")));
    //        }
    //        return folder;
    //    }
    //
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
        //TODO: improve
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

}
