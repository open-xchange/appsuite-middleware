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

package com.openexchange.chronos.storage.rdb.legacy;

import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RecurrenceRange;
import com.openexchange.chronos.storage.CalendarStorage;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class StoredRecurrenceId implements RecurrenceId {

    private final int recurrencePosition;

    /**
     * Initializes a new {@link StoredRecurrenceId}.
     *
     * @param recurrencePosition The legacy, 1-based recurrence position
     */
    public StoredRecurrenceId(int recurrencePosition) {
        super();
        this.recurrencePosition = recurrencePosition;
    }

    @Override
    public DateTime getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RecurrenceRange getRange() {
        return null;
    }

    /**
     * Gets the legacy, 1-based recurrence position
     *
     * @return The recurrence position
     */
    public int getRecurrencePosition() {
        return recurrencePosition;
    }

    @Override
    public boolean matches(RecurrenceId other) {
        return equals(other);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + recurrencePosition;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StoredRecurrenceId other = (StoredRecurrenceId) obj;
        if (recurrencePosition != other.recurrencePosition) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(RecurrenceId other) {
        if (null == other) {
            return 1;
        }
        if (StoredRecurrenceId.class.isInstance(other)) {
            return Integer.compare(getRecurrencePosition(), ((StoredRecurrenceId) other).getRecurrencePosition());
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(RecurrenceId other, TimeZone timeZone) {
        return compareTo(other);
    }

}
