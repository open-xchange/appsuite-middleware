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

package com.openexchange.calendar.json.actions.chronos;

import static org.slf4j.LoggerFactory.getLogger;
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

    /** The fields of the original event queried from the storage */
    private static final EventField[] FIELDS = {
        EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.END_DATE
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

    /**
     * Gets the original event data.
     *
     * @return The original event data, or <code>null</code> if not available
     */
    Event get() {
        if (null == originalEventId) {
            return null;
        }
        if (null == originalEvent) {
            try {
                originalEvent = eventConverter.getEvent(originalEventId, FIELDS);
            } catch (OXException e) {
                getLogger(OriginalEventHolder.class).debug("Error retrieving original data for event {}.", originalEventId, e);
            }
        }
        return originalEvent;
    }

    /**
     * Gets the recurrence data for the original event.
     *
     * @return The recurrence data, or <code>null</code> if not available
     */
    RecurrenceData getRecurrenceData() {
        if (null == originalRecurrenceData) {
            String seriesId = originalEventId.getObjectID();
            Event event = get();
            if (null != event) {
                if (event.getId().equals(event.getSeriesId())) {
                    // series master
                    seriesId = event.getSeriesId();
                    originalRecurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
                } else if (null == event.getSeriesId()) {
                    // no recurrence (yet)
                    seriesId = null;
                    originalRecurrenceData = new DefaultRecurrenceData(null, event.getStartDate(), null);
                } else {
                    seriesId = event.getSeriesId();
                }
            }
            if (null == originalRecurrenceData) {
                // recurrence data from fetched series master
                try {
                    RecurrenceData recurrenceData = eventConverter.loadRecurrenceData(seriesId);
                    originalRecurrenceData = new DefaultRecurrenceData(recurrenceData.getRecurrenceRule(), recurrenceData.getSeriesStart(), null);
                } catch (OXException e) {
                    getLogger(OriginalEventHolder.class).debug("Error retrieving original data for event {}.", seriesId, e);
                }
            }
        }
        return originalRecurrenceData;

    }

}
