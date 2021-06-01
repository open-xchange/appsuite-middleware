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

package com.openexchange.chronos.schedjoules.api.auxiliary;

import java.util.List;
import com.openexchange.chronos.Event;

/**
 * {@link SchedJoulesCalendar}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCalendar {

    private final List<Event> events;
    private final String eTag;
    private final String name;
    private final long lastModified;

    /**
     * Initialises a new {@link SchedJoulesCalendar}.
     */
    public SchedJoulesCalendar(String name, List<Event> events, String eTag, long lastModified) {
        super();
        this.name = name;
        this.events = events;
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    /**
     * Gets the eTag
     *
     * @return The eTag
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the events
     *
     * @return The events
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the lastModified
     *
     * @return The lastModified
     */
    public long getLastModified() {
        return lastModified;
    }
}
