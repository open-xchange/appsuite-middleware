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

import java.util.SortedSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.UnmodifiableEvent;

/**
 * {@link EventOccurrence}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventOccurrence extends UnmodifiableEvent {

    private final RecurrenceId recurrenceId;
    private final DateTime startDate;
    private final DateTime endDate;

    /**
     * Initializes a new {@link EventOccurrence}.
     *
     * @param seriesMaster The parent series master event
     * @param recurrenceId The occurrence's recurrence identifier
     */
    public EventOccurrence(Event seriesMaster, RecurrenceId recurrenceId) {
        super(seriesMaster);
        this.recurrenceId = recurrenceId;
        this.startDate = CalendarUtils.calculateStart(seriesMaster, recurrenceId);
        this.endDate = CalendarUtils.calculateEnd(seriesMaster, recurrenceId);
    }

    @Override
    public RecurrenceId getRecurrenceId() {
        return recurrenceId;
    }

    @Override
    public boolean containsRecurrenceId() {
        return true;
    }

    @Override
    public DateTime getStartDate() {
        return startDate;
    }

    @Override
    public boolean containsStartDate() {
        return true;
    }

    @Override
    public DateTime getEndDate() {
        return endDate;
    }

    @Override
    public boolean containsEndDate() {
        return true;
    }

    @Override
    public SortedSet<RecurrenceId> getRecurrenceDates() {
        return null;
    }

    @Override
    public boolean containsRecurrenceDates() {
        return false;
    }

    @Override
    public SortedSet<RecurrenceId> getChangeExceptionDates() {
        return null;
    }

    @Override
    public boolean containsChangeExceptionDates() {
        return false;
    }

    @Override
    public SortedSet<RecurrenceId> getDeleteExceptionDates() {
        return null;
    }

    @Override
    public boolean containsDeleteExceptionDates() {
        return false;
    }

}
