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

package com.openexchange.download.limit;

/**
 * {@link FileAccess} A generic class that contains information about file accesses in a defined time frame. This may contain either used or allowed values.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class FileAccess {

    private final int userId;
    private final int contextId;
    private long size;
    private int count;
    private long timeOfStartInMillis;
    private long timeOfEndInMillis;

    public FileAccess(int contextId, int userId, long start, long end, int counts, long size) {
        this.contextId = contextId;
        this.userId = userId;
        this.timeOfStartInMillis = start;
        this.timeOfEndInMillis = end;
        this.size = size;
        this.count = counts;
    }

    public long getSize() {
        return size;
    }

    public int getCount() {
        return count;
    }

    public int getUserId() {
        return userId;
    }

    public int getContextId() {
        return contextId;
    }

    public long getTimeOfStartInMillis() {
        return timeOfStartInMillis;
    }

    public long getTimeOfEndInMillis() {
        return timeOfEndInMillis;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setTimeOfStartInMillis(long timeOfStartInMillis) {
        this.timeOfStartInMillis = timeOfStartInMillis;
    }

    public void setTimeOfEndInMillis(long timeOfEndInMillis) {
        this.timeOfEndInMillis = timeOfEndInMillis;
    }

    public static boolean isDisabled(FileAccess allowed) {
        return allowed.getTimeOfEndInMillis() == allowed.getTimeOfStartInMillis();
    }

    public static boolean isExceeded(FileAccess allowed, FileAccess used) {
        if (isSizeExceeded(allowed, used) || isCountExceeded(allowed, used)) {
            return true;
        }
        return false;
    }

    public static boolean isCountExceeded(FileAccess allowed, FileAccess used) {
        int allowedCount = allowed.getCount();
        return (allowedCount > 0) && (used.getCount() >= allowedCount);
    }

    public static boolean isSizeExceeded(FileAccess allowed, FileAccess used) {
        long allowedSize = allowed.getSize();
        return (allowedSize > 0) && (used.getSize() >= allowedSize);
    }
}
