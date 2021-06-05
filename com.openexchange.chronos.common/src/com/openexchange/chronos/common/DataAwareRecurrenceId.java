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

import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.service.RecurrenceData;

/**
 * {@link DataAwareRecurrenceId}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DataAwareRecurrenceId extends DefaultRecurrenceId implements RecurrenceData {

    protected final RecurrenceData recurrenceData;

    /**
     * Initializes a new {@link DataAwareRecurrenceId}.
     *
     * @param delegate The underlying recurrence data of the corresponding series
     * @param value The recurrence-id value
     */
    public DataAwareRecurrenceId(RecurrenceData delegate, DateTime value) {
        super(value);
        this.recurrenceData = delegate;
    }

    @Override
    public String getRecurrenceRule() {
        return recurrenceData.getRecurrenceRule();
    }

    @Override
    public DateTime getSeriesStart() {
        return recurrenceData.getSeriesStart();
    }

    @Override
    public long[] getExceptionDates() {
        return recurrenceData.getExceptionDates();
    }

    @Override
    public long[] getRecurrenceDates() {
        return recurrenceData.getRecurrenceDates();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
