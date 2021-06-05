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

import java.util.List;
import com.openexchange.chronos.Event;

/**
 * {@link UpdatesResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface UpdatesResult extends TimestampedResult {

    /**
     * Gets a list of events that have been created or updated since the supplied client timestamp.
     *
     * @return A list of new and modified events, or an empty list if there were none, or <code>null</code> if not evaluated
     */
    List<Event> getNewAndModifiedEvents();

    /**
     * Gets a list of events that have been deleted since the supplied client timestamp.
     *
     * @return A list of deleted events, or an empty list if there were none, or <code>null</code> if not evaluated
     */
    List<Event> getDeletedEvents();

    /**
     * Gets a value indicating whether the result is truncated, i.e. there are even more new, modified or deleted events since the
     * supplied client timestamp, but the limit was exceeded.
     * <p/>
     * If the result is truncated, clients should use the value from {@link #getTimestamp()} for consecutive calls.
     *
     * @return <code>true</code> if the result is truncated, <code>false</code>, otherwise
     */
    boolean isTruncated();

    /**
     * Gets a value indicating whether the result is empty, i.e. there are no new, modified or deleted events since the supplied client
     * timestamp.
     *
     * @return <code>true</code> if the result is empty, <code>false</code>, otherwise
     */
    boolean isEmpty();

}
