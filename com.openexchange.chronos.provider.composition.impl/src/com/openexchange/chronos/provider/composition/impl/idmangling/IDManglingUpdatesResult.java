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

package com.openexchange.chronos.provider.composition.impl.idmangling;

import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withUniqueIDs;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.UpdatesResult;

/**
 * {@link IDManglingUpdatesResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDManglingUpdatesResult implements UpdatesResult {

    private final UpdatesResult delegate;
    private final int accountId;

    /**
     * Initializes a new {@link IDManglingUpdatesResult}.
     *
     * @param delegate The result delegate
     * @param accountId The identifier of the calendar account the result originates in
     */
    public IDManglingUpdatesResult(UpdatesResult delegate, int accountId) {
        super();
        this.delegate = delegate;
        this.accountId = accountId;
    }

    @Override
    public long getTimestamp() {
        return delegate.getTimestamp();
    }

    @Override
    public boolean isTruncated() {
        return delegate.isTruncated();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public List<Event> getNewAndModifiedEvents() {
        return withUniqueIDs(delegate.getNewAndModifiedEvents(), accountId);
    }

    @Override
    public List<Event> getDeletedEvents() {
        return withUniqueIDs(delegate.getDeletedEvents(), accountId);
    }

    @Override
    public String toString() {
        return "IDManglingUpdatesResult [accountId=" + accountId + ", delegate=" + delegate + "]";
    }

}
