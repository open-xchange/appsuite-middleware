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

package com.openexchange.chronos.provider.caching;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;

/**
 * Container holding the external events (if available) and additional information, for instance if there have been updates
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ExternalCalendarResult {

    private final List<Event> events;

    private final List<OXException> warnings = new ArrayList<>();

    private final boolean updated;

    /**
     * 
     * Initializes a new {@link ExternalCalendarResult}.
     * 
     * @param updated Indicates if the resource content has changed. <code>true</code> if changed; otherwise <code>false</code>
     * @param events The {@link Event}s of the external calendar if upToDate parameter is false
     */
    public ExternalCalendarResult(boolean updated, List<Event> events) {
        super();
        this.updated = updated;
        this.events = events;
    }

    public List<Event> getEvents() {
        return this.events;
    }

    public void addWarnings(List<OXException> warnings) {
        this.warnings.addAll(warnings);
    }

    public List<OXException> getWarnings() {
        return warnings;
    }

    /**
     * Indicates whether the resource is up to date or not. If the result is already up-to-date the events field can (and should!) be ignored.
     */
    public boolean isUpdated() {
        return updated;
    }
}
