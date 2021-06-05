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

package com.openexchange.mail.json.compose.share;

import com.openexchange.groupware.upload.impl.UploadUtility;

/**
 * {@link StorageQuota}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class StorageQuota {

    /**
     * Singleton value for storages without quota limits
     */
    public static final StorageQuota UNLIMITED = new StorageQuota(0L, -1L);

    private final long usageBytes;

    private final long limitBytes;

    /**
     * Initializes a new {@link StorageQuota}.
     *
     * @param usageBytes The already used storage in bytes
     * @param limitBytes The storage limit in bytes
     */
    public StorageQuota(long usageBytes, long limitBytes) {
        this.usageBytes = usageBytes;
        this.limitBytes = limitBytes;
    }

    /**
     * Gets the storage usage
     *
     * @return The usage in bytes
     */
    public long getUsageBytes() {
        return usageBytes;
    }

    /**
     * Gets the storage limit
     *
     * @return The limit in bytes; negative if unlimited
     */
    public long getLimitBytes() {
        return limitBytes;
    }

    /**
     * Checks if given number of bytes would fit into the storage based on current usage and limit.
     *
     * @param bytes The supposed bytes to be additionally consumed
     * @return <code>true</code> if it would fit
     */
    public boolean hasAvailableSpace(long bytes) {
        if (limitBytes < 0) {
            return true;
        }

        return (usageBytes + bytes) <= limitBytes;
    }

    @Override
    public String toString() {
        String usage;
        if (usageBytes < 0) {
            usage = "unknown";
        } else {
            usage = UploadUtility.getSize(usageBytes, 0, false, true);
        }

        String limit;
        if (limitBytes < 0) {
            limit = "unlimited";
        } else {
            limit = UploadUtility.getSize(limitBytes, 0, false, true);
        }

        return "StorageQuota [" + usage + " / " + limit + "]";
    }


}
