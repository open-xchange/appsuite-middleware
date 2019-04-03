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

package com.openexchange.caldav;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.resources.EventResource;
import com.openexchange.chronos.Calendar;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.WebdavPath;

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
        "X-MOZ-FAKED-MASTER", "X-MOZ-SNOOZE", "X-MOZ-SNOOZE-TIME*", "X-MOZ-LASTACK"
    };

    private final WebdavPath url;
    private final Calendar calendar;
    private final String uid;
    private final List<Event> changeExceptions;
    private final Event event;

    /**
     * Initializes a new {@link CalDAVImport}.
     *
     * @param parent The associated event resource
     * @param inputStream The input stream to deserialize
     */
    public CalDAVImport(EventResource resource, InputStream inputStream) throws OXException {
        this(resource.getUrl(), importICal(resource, inputStream));
    }

    /**
     * Initializes a new {@link CalDAVImport}.
     *
     * @param url The resource's WebDAV path
     * @param importedCalendar The imported calendar as sent by the client
     */
    private CalDAVImport(WebdavPath url, ImportedCalendar importedCalendar) throws OXException {
        super();
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
            ExtendedProperty extendedProperty = CalendarUtils.optExtendedProperty(importedEvent, "X-MOZ-FAKED-MASTER");
            if (null != extendedProperty && "1".equals(extendedProperty.getValue())) {
                LOG.debug("Skipping event marked with \"X-MOZ-FAKED-MASTER\": {}", importedEvent);
                continue;
            }
            if (null == uid) {
                uid = importedEvent.getUid();
            } else if (false == uid.equals(importedEvent.getUid())) {
                throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "valid-calendar-object-resource", url, HttpServletResponse.SC_FORBIDDEN);
            }
            if (looksLikeException(importedEvent)) {
                changeExceptions.add(importedEvent);
            } else {
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
