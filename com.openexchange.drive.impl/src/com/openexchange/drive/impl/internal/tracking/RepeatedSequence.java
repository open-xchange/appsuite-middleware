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

package com.openexchange.drive.impl.internal.tracking;

import java.util.List;


/**
 * {@link RepeatedSequence}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RepeatedSequence<T> {

    private final List<T> sequence;
    private final int repetitions;

    /**
     * Initializes a new {@link RepeatedSequence}.
     *
     * @param sequence The sequence
     * @param repetitions The repetitions
     */
    public RepeatedSequence(List<T> sequence, int repetitions) {
        super();
        this.sequence = sequence;
        this.repetitions = repetitions;
    }

    /**
     * Gets the sequence
     *
     * @return The sequence
     */
    public List<T> getSequence() {
        return sequence;
    }

    /**
     * Gets the repetitions
     *
     * @return The repetitions
     */
    public int getRepetitions() {
        return repetitions;
    }

    @Override
    public String toString() {
        StringBuilder StringBuilder = new StringBuilder();
        StringBuilder.append("RepeatedSequence: ").append(repetitions).append("x :\n");
        for (int i = 0; i < sequence.size(); i++) {
            StringBuilder.append(" (").append(i + 1).append(") ").append(sequence.get(i)).append('\n');
        }
        return StringBuilder.toString();
    }

}
