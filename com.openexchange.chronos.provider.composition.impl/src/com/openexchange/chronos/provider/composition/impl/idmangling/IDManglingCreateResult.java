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

import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CreateResult;

/**
 * {@link IDManglingCreateResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDManglingCreateResult implements CreateResult {

    private final CreateResult delegate;
    private final int accountId;

    /**
     * Initializes a new {@link IDManglingCreateResult}.
     *
     * @param delegate The result delegate
     * @param accountId The identifier of the calendar account the result originates in
     */
    public IDManglingCreateResult(CreateResult delegate, int accountId) {
        super();
        this.delegate = delegate;
        this.accountId = accountId;
    }

    @Override
    public Event getCreatedEvent() {
        return IDMangling.withUniqueID(delegate.getCreatedEvent(), accountId);
    }

    @Override
    public long getTimestamp() {
        return delegate.getTimestamp();
    }

}
