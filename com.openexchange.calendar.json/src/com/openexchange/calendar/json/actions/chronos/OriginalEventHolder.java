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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.calendar.json.actions.chronos;

import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.exception.OXException;

/**
 * {@link OriginalEventHolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class OriginalEventHolder {

    private static final EventField[] FIELDS = { EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.END_DATE
    };

    private final EventConverter eventConverter;
    private final EventID originalEventId;

    private Event originalEvent;
    private RecurrenceData originalRecurrenceData;

    /**
     * Initializes a new {@link OriginalEventHolder}.
     *
     * @param eventConverter The associated event converter
     * @param originalEventId The identifier of the original event, or <code>null</code> if not available
     */
    OriginalEventHolder(EventConverter eventConverter, EventID originalEventId) {
        super();
        this.originalEventId = originalEventId;
        this.eventConverter = eventConverter;
    }

    Event get() throws OXException {
        if (null == originalEventId) {
            return null;
        }
        if (null == originalEvent) {
            originalEvent = eventConverter.getEvent(originalEventId, FIELDS);
        }
        return originalEvent;
    }

    RecurrenceData getRecurrenceData() throws OXException {
        if (null == originalRecurrenceData) {
            Event event = get();
            if (null == event) {
                return null;
            }
            if (event.getId().equals(event.getSeriesId())) {
                // series master
                originalRecurrenceData = new DefaultRecurrenceData(event);
            } else if (null == event.getSeriesId()) {
                // no recurrence (yet)
                DateTime startDate = event.getStartDate();
                originalRecurrenceData = new DefaultRecurrenceData(null, startDate.isAllDay(), startDate.getTimeZone().getID(), startDate.getTimestamp());
            } else {
                // recurrence data from fetched series master
                EventID masterEventId = new EventID(event.getFolderId(), event.getSeriesId());
                originalRecurrenceData = new DefaultRecurrenceData(eventConverter.getEvent(masterEventId, FIELDS));
            }
        }
        return originalRecurrenceData;

    }

}
