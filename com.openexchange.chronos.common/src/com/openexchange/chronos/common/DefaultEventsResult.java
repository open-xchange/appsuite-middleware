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

package com.openexchange.chronos.common;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultEventsResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultEventsResult implements EventsResult {

    private final List<Event> events;
    private final long timestamp;
    private final OXException error;

    public DefaultEventsResult(OXException error) {
        this(null, 0L, error);
    }

    public DefaultEventsResult(List<Event> events) {
        this(events, getMaximumTimestamp(events), null);
    }

    public DefaultEventsResult(List<Event> events, long timestamp) {
        this(events, timestamp, null);
    }

    protected DefaultEventsResult(List<Event> events, long timestamp, OXException error) {
        super();
        this.events = events;
        this.timestamp = timestamp;
        this.error = error;
    }

    @Override
    public List<Event> getEvents() {
        return events;
    }

    @Override
    public OXException getError() {
        return error;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    private static long getMaximumTimestamp(Collection<Event> events) {
        Date timestamp = CalendarUtils.getMaximumTimestamp(events);
        return null == timestamp ? 0L : timestamp.getTime();
    }

}
