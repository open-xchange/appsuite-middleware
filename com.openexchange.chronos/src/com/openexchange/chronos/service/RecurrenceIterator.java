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

package com.openexchange.chronos.service;

import java.util.Iterator;

/**
 * {@link RecurrenceIterator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface RecurrenceIterator<T> extends Iterator<T> {

    /**
     * Gets the current, <code>1</code>-based position of the iterator in the recurrence set.
     *
     * @return The position, or <code>0</code> if there are no occurrences at all
     */
    int getPosition();

    /**
     * Gets a value indicating whether the current element represents the <i>first</i> one in the recurrence set.
     *
     * @return <code>true</code> if the element is the first one, <code>false</code>, otherwise
     */
    boolean isFirstOccurrence();

    /**
     * Gets a value indicating whether the current element represents the <i>last</i> one in the recurrence set.
     *
     * @return <code>true</code> if the element is the last one, <code>false</code>, otherwise
     */
    boolean isLastOccurrence();

}
