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

package com.openexchange.chronos.compat;

import java.util.Date;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.common.DataAwareRecurrenceId;
import com.openexchange.chronos.service.RecurrenceData;

/**
 * {@link PositionAwareRecurrenceId}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class PositionAwareRecurrenceId extends DataAwareRecurrenceId {

    private final int recurrencePosition;
    private final Date recurrenceDatePosition;

    /**
     * Initializes a new {@link PositionAwareRecurrenceId}.
     *
     * @param recurrenceData The underlying recurrence data of the corresponding series
     * @param value The recurrence-id value
     * @param recurrencePosition The legacy, 1-based recurrence position
     * @param recurrenceDatePosition The legacy recurrence date position
     */
    public PositionAwareRecurrenceId(RecurrenceData recurrenceData, DateTime value, int recurrencePosition, Date recurrenceDatePosition) {
        super(recurrenceData, value);
        this.recurrencePosition = recurrencePosition;
        this.recurrenceDatePosition = recurrenceDatePosition;
    }

    /**
     * Gets the formerly used recurrence position, i.e. the 1-based, sequential position in the series where the original occurrence
     * would have been.
     *
     * @return The recurrence position
     */
    public int getRecurrencePosition() {
        return recurrencePosition;
    }

    /**
     * Gets the formerly used recurrence date position, i.e. the date where the original occurrence would have been, as UTC date with
     * truncated time fraction.
     *
     * @return The legacy recurrence date position
     */
    public Date getRecurrenceDatePosition() {
        return recurrenceDatePosition;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
