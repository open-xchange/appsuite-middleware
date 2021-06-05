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

package com.openexchange.chronos;

/**
 * {@link Repeat}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.6.2">RFC 5545, section 3.8.6.2</a>
 */
public class Repeat {

    private String duration;
    private int count;

    /**
     * Initializes a new {@link Repeat}.
     */
    public Repeat() {
        super();
    }

    /**
     * Initializes a new {@link Repeat}.
     *
     * @param count The number of additional repetitions the alarm will be triggered
     * @param duration The delay period after which the alarm will repeat
     */
    public Repeat(int count, String duration) {
        super();
        this.count = count;
        this.duration = duration;
    }

    /**
     * Gets the number of additional repetitions the alarm will be triggered. *
     *
     * @return The repetition count
     */
    public int getCount() {
        return count;
    }

    /**
     * Gets the delay period after which the alarm will repeat.
     *
     * @return The delay period
     */
    public String getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Repeat [count=" + count + ", duration=" + duration + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + count;
        result = prime * result + ((duration == null) ? 0 : duration.hashCode());
        return result;
    }

}
