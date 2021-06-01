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

import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.UpdatesResult;

/**
 * {@link DefaultUpdatesResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultUpdatesResult implements UpdatesResult {

    private final List<Event> newAndModifiedEvents;
    private final List<Event> deletedEvents;
    private final long timestamp;
    private final boolean truncated;

    /**
     * Initializes a new {@link DefaultUpdatesResult}.
     *
     * @param newAndModifiedEvents The list of new/modified events
     * @param deletedEvents The list of deleted events
     * @param timestamp The maximum timestamp of all events in the update result
     * @param truncated <code>true</code> if the result is truncated, <code>false</code>, otherwise
     */
    public DefaultUpdatesResult(List<Event> newAndModifiedEvents, List<Event> deletedEvents, long timestamp, boolean truncated) {
        super();
        this.newAndModifiedEvents = newAndModifiedEvents;
        this.deletedEvents = deletedEvents;
        this.timestamp = timestamp;
        this.truncated = truncated;
    }

    /**
     * Initializes a new {@link DefaultUpdatesResult}.
     *
     * @param newAndModifiedEvents The list of new/modified events
     * @param deletedEvents The list of deleted events
     */
    public DefaultUpdatesResult(List<Event> newAndModifiedEvents, List<Event> deletedEvents) {
        this(newAndModifiedEvents, deletedEvents, Math.max(getMaximumTimestamp(newAndModifiedEvents), getMaximumTimestamp(deletedEvents)), false);
    }

    @Override
    public List<Event> getNewAndModifiedEvents() {
        return newAndModifiedEvents;
    }

    @Override
    public List<Event> getDeletedEvents() {
        return deletedEvents;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isTruncated() {
        return truncated;
    }

    @Override
    public boolean isEmpty() {
        return isNullOrEmpty(deletedEvents) && isNullOrEmpty(newAndModifiedEvents);
    }

    @Override
    public String toString() {
        return "DefaultUpdatesResult [newAndModifiedEvents=" + newAndModifiedEvents + ", deletedEvents=" + deletedEvents + "]";
    }

    private static long getMaximumTimestamp(List<Event> events) {
        long timestamp = 0L;
        if (null != events) {
            for (Event event : events) {
                timestamp = Math.max(timestamp, event.getTimestamp());
            }
        }
        return timestamp;
    }

}
