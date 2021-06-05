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

import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventID;

/**
 * {@link DeleteResultImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DeleteResultImpl implements DeleteResult {

    private final long timestamp;
    private final Event event;

    /**
     * Initializes a new {@link DeleteResultImpl}.
     *
     * @param timestamp The timestamp
     * @param event The original event
     */
    public DeleteResultImpl(long timestamp, Event originalEvent) {
        super();
        this.timestamp = timestamp;
        this.event = originalEvent;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public EventID getEventID() {
        return CalendarUtils.getEventID(event);
    }

    @Override
    public Event getOriginal() {
        return event;
    }

    @Override
    public String toString() {
        return "DeleteResult [original=" + event + "]";
    }

}
