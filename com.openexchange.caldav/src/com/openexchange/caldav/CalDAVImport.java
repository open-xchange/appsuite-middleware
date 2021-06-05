/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.caldav;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.clientfields.Lightning;
import com.openexchange.caldav.resources.EventResource;
import com.openexchange.chronos.Calendar;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link CalDAVImport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalDAVImport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalDAVImport.class);

    private static final String[] EXTRA_PROPERTIES = {
        "X-CALENDARSERVER-ATTENDEE-COMMENT", "X-CALENDARSERVER-PRIVATE-COMMENT", "X-CALENDARSERVER-ACCESS",
        Lightning.X_MOZ_FAKED_MASTER.getId(), Lightning.X_MOZ_SNOOZE.getId(), Lightning.X_MOZ_SNOOZE_TIME.getId() +"*", Lightning.X_MOZ_LASTACK.getId()
    };

    private final WebdavPath url;
    private final Calendar calendar;
    private final String uid;
    private final List<Event> changeExceptions;
    private final Event event;

    /**
     * Initializes a new {@link CalDAVImport}.
     *
     * @param resource The associated event resource
     * @param inputStream The input stream to deserialize
     */
    public CalDAVImport(EventResource resource, InputStream inputStream) throws OXException {
        this(resource, importICal(resource, inputStream));
    }

    /**
     * Initializes a new {@link CalDAVImport}.
     *
     * @param resource The associated event resource
     * @param importedCalendar The imported calendar as sent by the client
     */
    private CalDAVImport(EventResource resource, ImportedCalendar importedCalendar) throws OXException {
        super();
        WebdavPath url = resource.getUrl();
        /*
         * ensure there are events
         */
        List<Event> importedEvents = importedCalendar.getEvents();
        if (null == importedEvents || importedEvents.isEmpty()) {
            throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "supported-calendar-component", url, HttpServletResponse.SC_FORBIDDEN);
        }
        /*
         * distinguish between change exceptions and normal events
         */
        List<Event> changeExceptions = new ArrayList<Event>();
        Event event = null;
        String uid = null;
        for (Event importedEvent : importedEvents) {
            /*
             * skip any X-MOZ-FAKED-MASTER appointments
             */
            ExtendedProperty extendedProperty = CalendarUtils.optExtendedProperty(importedEvent, Lightning.X_MOZ_FAKED_MASTER.getId());
            if (null != extendedProperty && "1".equals(extendedProperty.getValue())) {
                LOG.debug("Skipping event marked with \"{}\": {}", Lightning.X_MOZ_FAKED_MASTER.getId(), importedEvent);
                continue;
            }
            /*
             * take over first or check consecutive event UID
             */
            if (null == uid) {
                uid = importedEvent.getUid();
            } else if (false == uid.equals(importedEvent.getUid())) {
                throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "valid-calendar-object-resource", url, HttpServletResponse.SC_FORBIDDEN);
            }
            if (looksLikeException(importedEvent)) {
                /*
                 * take over change exception event
                 */
                changeExceptions.add(importedEvent);
            } else {
                /*
                 * check against min-/max-date-time, if configured
                 */
                if (Boolean.parseBoolean(resource.getFactory().getConfigValue("com.openexchange.caldav.interval.strict", "false"))) {
                    if (false == CalendarUtils.isInRange(importedEvent, resource.getParent().getMinDateTime(), null, TimeZones.UTC) && (
                        null == importedEvent.getRecurrenceRule() || false == resource.getFactory().requireService(RecurrenceService.class).iterateEventOccurrences(
                            importedEvent, resource.getParent().getMinDateTime(), null).hasNext())) {
                        throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "min-date-time", url, HttpServletResponse.SC_FORBIDDEN);
                    }
                    if (false == CalendarUtils.isInRange(importedEvent, null, resource.getParent().getMaxDateTime(), TimeZones.UTC)) {
                        throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "max-date-time", url, HttpServletResponse.SC_FORBIDDEN);
                    }
                }
                /*
                 * take over series master or non-recurring event
                 */
                if (null == event) {
                    event = importedEvent;
                } else {
                    throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "valid-calendar-object-resource", url, HttpServletResponse.SC_FORBIDDEN);
                }
            }
        }
        this.url = url;
        this.calendar = importedCalendar;
        this.event = event;
        this.changeExceptions = changeExceptions;
        this.uid = uid;
    }
    
    /**
     * Initializes a new {@link CalDAVImport}.
     *
     * @param url The resource's WebDAV path
     * @param calendar The calendar of the import
     * @param event The event of the import, or <code>null</code> if only change exceptions are available
     * @param changeExceptions The change exceptions, or an empty list if there are none.
     */
    public CalDAVImport(WebdavPath url, Calendar calendar, Event event, List<Event> changeExceptions) {
        super();
        this.url = url;
        this.calendar = calendar;
        this.event = event;
        this.changeExceptions = changeExceptions;
        this.uid = getUID(event, changeExceptions);
    }

    public Calendar getCalender() {
        return calendar;
    }

    /**
     * Gets the common unique identifier of all event resources in the import.
     *
     * @return The unique identifier
     */
    public String getUID() {
        return uid;
    }

    /**
     * Gets the separate resource filename extracted from the WebDAV path in case it differs from the event resource's UID.
     *
     * @return The filename, or <code>null</code> if it matches the UID
     */
    public String getFilename() {
        String resourceName = extractResourceName(url);
        return null != resourceName && false == resourceName.equals(uid) ? resourceName : null;
    }

    /**
     * Gets the event of the import, which represents either the <i>only</i> event for non-recurring events, or the series master event.
     *
     * @return The event, or <code>null</code> if only change exception events are available
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Gets the change exceptions of the import.
     *
     * @return The change exceptions, or an empty list if there are none.
     */
    public List<Event> getChangeExceptions() {
        return changeExceptions;
    }

    /**
     * Initializes a new calendar object resource bundling the imported event data.
     * 
     * @return The calendar object resource
     * @throws WebdavProtocolException <code>CALDAV:valid-calendar-object-resource</code> precondition exception if no valid calendar object resource can be created
     */
    public CalendarObjectResource asCalendarObjectResource() throws WebdavProtocolException {
        try {
            return new DefaultCalendarObjectResource(event, changeExceptions);
        } catch (IllegalArgumentException e) {
            throw new PreconditionException(OXException.general("", e), DAVProtocol.CAL_NS.getURI(), "valid-calendar-object-resource", url, HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Gets all imported events.
     *
     * @return The events, or an empty list if there are none.
     */
    public List<Event> getEvents() {
        List<Event> events = new ArrayList<Event>(changeExceptions.size() + 1);
        if (null != event) {
            events.add(event);
        }
        events.addAll(changeExceptions);
        return events;
    }

    private static boolean looksLikeException(Event event) {
        return null != event.getRecurrenceId();
    }

    private static ImportedCalendar importICal(EventResource resource, InputStream inputStream) throws OXException {
        ICalService iCalService = resource.getFactory().requireService(ICalService.class);
        ICalParameters iCalParameters = EventPatches.applyIgnoredProperties(resource, iCalService.initParameters());
        iCalParameters.set(ICalParameters.EXTRA_PROPERTIES, EXTRA_PROPERTIES);
        return iCalService.importICal(inputStream, iCalParameters);
    }

    private static String extractResourceName(WebdavPath url) {
        String name = url.name();
        if (Strings.isNotEmpty(name)) {
            if (name.toLowerCase().endsWith(Tools.EXTENSION_ICS)) {
                name = name.substring(0, name.length() - Tools.EXTENSION_ICS.length());
            }
        }
        return name;
    }

    private static String getUID(Event event, List<Event> changeExceptions) {
        if (null != event) {
            return event.getUid();
        }
        if (null != changeExceptions && 0 < changeExceptions.size()) {
            return changeExceptions.get(0).getUid();
        }
        return null;
    }

}
