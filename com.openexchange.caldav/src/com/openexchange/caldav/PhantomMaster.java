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

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;

/**
 * {@link PhantomMaster}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class PhantomMaster extends Event {

    private final List<Event> detachedOccurrences;

    /**
     * Initializes a new {@link PhantomMaster}.
     *
     * @param detachedOccurrences The detached occurrences
     */
    public PhantomMaster(List<Event> detachedOccurrences) {
        super();
        this.detachedOccurrences = detachedOccurrences;
        setSummary("[Placeholder Item]");
        if (null != detachedOccurrences && 0 < detachedOccurrences.size()) {
            Event occurrence = detachedOccurrences.get(0);
            setUid(occurrence.getUid());
            setFilename(occurrence.getFilename());
            setFolderId(occurrence.getFolderId());
            setId(occurrence.getSeriesId());
            setSeriesId(occurrence.getSeriesId());
            setCreated(occurrence.getCreated());
            setCreatedBy(occurrence.getCreatedBy());
            setModifiedBy(occurrence.getModifiedBy());
            SortedSet<RecurrenceId> changeExceptionDates = new TreeSet<RecurrenceId>();
            Date lastModified = occurrence.getLastModified();
            long timestamp = occurrence.getTimestamp();
            for (Event event : detachedOccurrences) {
                lastModified = Tools.getLatestModified(lastModified, event);
                if (timestamp < event.getTimestamp()) {
                    timestamp = event.getTimestamp();
                }
                if (null != event.getRecurrenceId()) {
                    changeExceptionDates.add(event.getRecurrenceId());
                }
            }
            setLastModified(lastModified);
            setTimestamp(timestamp);
            setChangeExceptionDates(changeExceptionDates);
        }
    }

    /**
     * Gets the detachedOccurrences
     *
     * @return The detachedOccurrences
     */
    public List<Event> getDetachedOccurrences() {
        return detachedOccurrences;
    }

}
