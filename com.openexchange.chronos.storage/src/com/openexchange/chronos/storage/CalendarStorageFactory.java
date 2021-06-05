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

package com.openexchange.chronos.storage;

import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorageFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarStorageFactory {

    /**
     * Initializes a new {@link CalendarStorage}.
     *
     * @param context The context
     * @param accountId The account identifier
     * @param entityResolver The entity resolver to use, or <code>null</code> if not available
     */
    CalendarStorage create(Context context, int accountId, EntityResolver entityResolver) throws OXException;

    /**
     * Initializes a new {@link CalendarStorage}.
     *
     * @param context The context
     * @param accountId The account identifier
     * @param entityResolver The entity resolver to use, or <code>null</code> if not available
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    CalendarStorage create(Context context, int accountId, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException;

    /**
     * Wraps a calendar storage into a special <i>resilient</i> calendar storage that tries to automatically handle SQL <i>truncation</i>
     * and <i>incorrect string</i> warnings by adjusting the affected strings, and retrying the operation. Additionally, no exceptions are
     * raised when trying to store properties or property values that are not supported by the storage.
     *
     * @param storage The calendar storage to wrap
     * @return The wrapped calendar storage
     * @see CalendarParameters#PARAMETER_IGNORE_STORAGE_WARNINGS
     */
    CalendarStorage makeResilient(CalendarStorage storage);

}
