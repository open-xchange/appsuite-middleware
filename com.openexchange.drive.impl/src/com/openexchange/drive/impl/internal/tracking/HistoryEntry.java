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

import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;

/**
 * {@link HistoryEntry}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class HistoryEntry {

    private final IntermediateSyncResult<? extends DriveVersion> syncResult;
    private final int hashCode;
    private final String path;

    /**
     * Initializes a new {@link HistoryEntry}.
     *
     * @param syncResult The sync result
     * @param path The path where this sync result was resulting from, or <code>null</code> if not relevant
     */
    public HistoryEntry(IntermediateSyncResult<? extends DriveVersion> syncResult, String path) {
        this(syncResult, calculateHash(path, syncResult), path);
    }

    private HistoryEntry(IntermediateSyncResult<? extends DriveVersion> syncResult, int hashCode, String path) {
        super();
        this.syncResult = syncResult;
        this.path = path;
        this.hashCode = hashCode;
    }

    /**
     * Gets path where this sync result was resulting from.
     *
     * @return The path, or <code>null</code> if not relevant
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets a compact view of the history entry, i.e. an entry that does not contain the complete sync result, but preserves the
     * hash code for comparisons.
     *
     * @return A new, compact {@link HistoryEntry} instance
     */
    public HistoryEntry compact() {
        return new HistoryEntry(null, hashCode, path);
    }

    /**
     * Gets the syncResult
     *
     * @return The syncResult
     */
    public IntermediateSyncResult<? extends DriveVersion> getSyncResult() {
        return syncResult;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HistoryEntry)) {
            return false;
        }
        HistoryEntry other = (HistoryEntry) obj;
        return hashCode == other.hashCode;
    }

    @Override
    public String toString() {
        if (null == syncResult) {
            return String.valueOf(hashCode);
        } else {
            StringBuilder StringBuilder = new StringBuilder();
            StringBuilder.append(hashCode).append(" (").append(syncResult.toString().replace('\n', ' ').trim());
            return StringBuilder.append(')').toString();
        }
    }

    private static int calculateHash(String path, IntermediateSyncResult<? extends DriveVersion> syncResult) {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((syncResult == null) ? 0 : syncResult.hashCode());
        return result;
    }

}
