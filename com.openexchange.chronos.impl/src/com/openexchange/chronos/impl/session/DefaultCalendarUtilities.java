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

package com.openexchange.chronos.impl.session;

import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.mapping.DefaultEventUpdate;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.MappedIncorrectString;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DefaultCalendarUtilities}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultCalendarUtilities implements CalendarUtilities {

    private final CalendarSession session;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link DefaultCalendarUtilities}.
     *
     * @param services A service lookup reference
     */
    public DefaultCalendarUtilities(ServiceLookup services) {
        this(services, null);
    }

    /**
     * Initializes a new {@link DefaultCalendarUtilities}.
     *
     * @param services A service lookup reference
     * @param session The underlying calendar session, or <code>null</code> if not bound to a specific session
     */
    public DefaultCalendarUtilities(ServiceLookup services, CalendarSession session) {
        super();
        this.services = services;
        this.session = session;
    }

    @Override
    public EventUpdate compare(Event original, Event update, boolean considerUnset, EventField... ignoredFields) throws OXException {
        return new DefaultEventUpdate(original, update, considerUnset, ignoredFields);
    }

    @Override
    public boolean handleIncorrectString(OXException e, Event event) {
        boolean hasReplaced = false;
        if (null != event) {
            try {
                hasReplaced |= MappedIncorrectString.replace(e.getProblematics(), event, "");
            } catch (ClassCastException | OXException x1) {
                // also try attendees
                List<Attendee> attendees = event.getAttendees();
                if (null != attendees) {
                    for (Attendee attendee : attendees) {
                        try {
                            hasReplaced |= MappedIncorrectString.replace(e.getProblematics(), attendee, "");
                        } catch (ClassCastException | OXException x2) {
                            // ignore
                        }
                    }
                }
            }
        }
        return hasReplaced;
    }

    @Override
    public boolean handleIncorrectString(OXException e, List<Attendee> attendees) {
        boolean hasReplaced = false;
        if (null != attendees) {
            for (Attendee attendee : attendees) {
                try {
                    hasReplaced |= MappedIncorrectString.replace(e.getProblematics(), attendee, "");
                } catch (ClassCastException | OXException x) {
                    // ignore
                }
            }
        }
        return hasReplaced;
    }

    @Override
    public boolean handleDataTruncation(OXException e, Event event) {
        boolean hasTrimmed = false;
        if (null != event) {
            try {
                hasTrimmed |= MappedTruncation.truncate(e.getProblematics(), event);
            } catch (ClassCastException | OXException x) {
                // ignore
            }
        }
        return hasTrimmed;
    }

    @Override
    public boolean handleDataTruncation(OXException e, List<Attendee> attendees) {
        boolean hasTrimmed = false;
        if (null != attendees) {
            for (Attendee attendee : attendees) {
                try {
                    hasTrimmed |= MappedTruncation.truncate(e.getProblematics(), attendee);
                } catch (ClassCastException | OXException x) {
                    // ignore
                }
            }
        }
        return hasTrimmed;
    }

    @Override
    public Event copyEvent(Event event, EventField... fields) throws OXException {
        return EventMapper.getInstance().copy(event, null, fields);
    }

    @Override
    public EntityResolver getEntityResolver(int contextId) throws OXException {
        if (null != session && session.getContextId() == contextId) {
            return session.getEntityResolver();
        }
        return new DefaultEntityResolver(contextId, services);
    }

    @Override
    public Comparator<Event> getComparator(final SortOrder[] sortOrders, TimeZone timeZone) {
        return new Comparator<Event>() {

            @Override
            public int compare(Event event1, Event event2) {
                if (null == event1) {
                    return null == event2 ? 0 : -1;
                }
                if (null == event2) {
                    return 1;
                }
                if (null == sortOrders || 0 == sortOrders.length) {
                    return 0;
                }
                int comparison = 0;
                for (SortOrder sortOrder : sortOrders) {
                    Mapping<? extends Object, Event> mapping = EventMapper.getInstance().opt(sortOrder.getBy());
                    if (null == mapping) {
                        org.slf4j.LoggerFactory.getLogger(DefaultCalendarUtilities.class).warn("Can't compare by {} due to missing mapping", sortOrder.getBy());
                        continue;
                    }
                    comparison = mapping.compare(event1, event2);
                    if (0 != comparison) {
                        return sortOrder.isDescending() ? -1 * comparison : comparison;
                    }
                }
                return comparison;
            }
        };
    }

    @Override
    public TimeZone selectTimeZone(int calendarUserId, TimeZone timeZone, TimeZone originalTimeZone) throws OXException {
        return Utils.selectTimeZone(session, calendarUserId, timeZone, originalTimeZone);
    }

    @Override
    public void adjustTimeZones(int calendarUserId, Event event, Event originalEvent) throws OXException {
        Consistency.adjustTimeZones(session, calendarUserId, event, originalEvent);
    }

}
