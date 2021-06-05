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

package com.openexchange.find.basic.calendar;

import java.util.Date;

/**
 * {@link RangeOption}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RangeOption {

    /** Empty sort options */
    public static final RangeOption EMPTY = new RangeOption();

    private Date from;
    private Date until;

    /**
     * Initializes a new {@link RangeOption}.
     */
    public RangeOption() {
        super();
    }

    /**
     * Sets the from/to range based on the supplied <i>from</i> and <i>until</i> values..
     *
     * @param from The lower inclusive limit of the queried range, or <code>null</code> if not set
     * @param until The upper exclusive limit of the queried range, or <code>null</code> if not set
     * @return A self reference
     */
    public RangeOption setRange(Date from, Date until) {
        this.from = from;
        this.until = until;
        return this;
    }

    /**
     * Gets the lower inclusive limit of the queried range.
     *
     * @return The lower inclusive limit of the queried range, or <code>null</code> if not set
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Gets the upper exclusive limit of the queried range.
     *
     * @return The upper exclusive limit of the queried range, or <code>null</code> if not set
     */
    public Date getUntil() {
        return until;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((until == null) ? 0 : until.hashCode());
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
        RangeOption other = (RangeOption) obj;
        if (from == null) {
            if (other.from != null) {
                return false;
            }
        } else if (!from.equals(other.from)) {
            return false;
        }
        if (until == null) {
            if (other.until != null) {
                return false;
            }
        } else if (!until.equals(other.until)) {
            return false;
        }
        return true;
    }

}
