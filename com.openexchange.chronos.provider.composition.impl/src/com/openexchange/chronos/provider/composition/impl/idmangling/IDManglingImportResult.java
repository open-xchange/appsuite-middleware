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

import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.ImportResult;

/**
 * {@link IDManglingImportResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDManglingImportResult extends IDManglingErrorAwareCalendarResult implements ImportResult {

    @SuppressWarnings("hiding")
    private final ImportResult delegate;

    /**
     * Initializes a new {@link IDManglingImportResult}.
     *
     * @param delegate The result delegate
     * @param accountId The identifier of the calendar account the result originates in
     */
    public IDManglingImportResult(ImportResult delegate, int accountId) {
        super(delegate, accountId);
        this.delegate = delegate;
    }

    @Override
    public int getIndex() {
        return delegate.getIndex();
    }

    @Override
    public EventID getId() {
        return null != delegate.getId() ? IDMangling.getUniqueId(accountId, delegate.getId()) : null;
    }

    @Override
    public String toString() {
        return "IDManglingImportResult [accountId=" + accountId + ", delegate=" + delegate + "]";
    }

}
