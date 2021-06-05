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

package com.openexchange.filestore;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link QuotaFileStorageListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface QuotaFileStorageListener {

    /**
     * Called right before the usage is about to be incremented.
     * <p>
     * <b>Note</b>: This call-back occurs if increment does not violate quota limitation.<br>
     * Otherwise {@link #onQuotaExceeded()} is called.
     *
     * @param id The identifier of the file in storage causing the usage increment
     * @param toIncrement The value to increment
     * @param currentUsage The current quota usage
     * @param quota The quota limit
     * @param userId The (optional) user identifier or <code>0</code> (zero) if storage is not user-specific
     * @param contextId The context identifier
     * @throws OXException If increment must not occur and operation is supposed to be aborted
     */
    void onUsageIncrement(String id, long toIncrement, long currentUsage, long quota, int userId, int contextId) throws OXException;

    /**
     * Called right before the usage is about to be decremented.
     * <p>
     * <b>Note</b>: This call-back should not throw an exception.
     *
     * @param ids The identifiers of the files in storage causing the usage decrement
     * @param toDecrement The value to decrement
     * @param currentUsage The current quota usage
     * @param quota The quota limit
     * @param userId The (optional) user identifier or <code>0</code> (zero) if storage is not user-specific
     * @param contextId The context identifier
     */
    void onUsageDecrement(List<String> ids, long toDecrement, long currentUsage, long quota, int userId, int contextId);

    /**
     * Called in case a quota increment exceeds the quota limit and the operation is about to be aborted.
     * <p>
     * <b>Note</b>: This call-back should not throw an exception.
     *
     * @param optId The optional identifier of the file in storage causing the exceeded quota or <code>null</code>
     * @param toIncrement The value that exceeded the quota limit
     * @param currentUsage The current quota usage
     * @param quota The quota limit
     * @param userId The (optional) user identifier or <code>0</code> (zero) if storage is not user-specific
     * @param contextId The context identifier
     */
    void onQuotaExceeded(String optId, long toIncrement, long currentUsage, long quota, int userId, int contextId);

    /**
     * Called in case no quota is available for associated user and consequently the operation is about to be aborted.
     * <p>
     * <b>Note</b>: This call-back should not throw an exception.
     *
     * @param optId The optional identifier of the file in storage causing the exceeded quota or <code>null</code>
     * @param userId The (optional) user identifier or <code>0</code> (zero) if storage is not user-specific
     * @param contextId The context identifier
     */
    void onNoQuotaAvailable(String optId, int userId, int contextId);

}
