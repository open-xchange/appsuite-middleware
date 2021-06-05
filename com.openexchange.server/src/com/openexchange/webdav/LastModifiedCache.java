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

package com.openexchange.webdav;

import java.util.Date;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link LastModifiedCache} - Simple cache for last-modified time stamps.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class LastModifiedCache {

    private final TIntObjectMap<LastModifiedMemory> storage;

    /**
     * Initializes a new {@link LastModifiedCache}.
     */
    public LastModifiedCache() {
        storage = new TIntObjectHashMap<LastModifiedMemory>();
    }

    /**
     * Returns the current last-modified time stamp for a given objectId, if the object has been changed in the meantime. Returns the given
     * lastModified, if the object has not been changed or the original lastModified is greater than the given one.
     *
     * @param objectId The object ID
     * @param lastModified The last-modified time stamp
     * @return The current valid last-modified time stamp for the given objectId
     */
    public long getLastModified(final int objectId, final long lastModified) {
        if (storage.containsKey(objectId)) {
            final LastModifiedMemory memory = storage.get(objectId);
            if (lastModified >= memory.getOriginal()) {
                return memory.getCurrent();
            }
        }
        return lastModified;
    }

    public Date getLastModified(final int objectId, final Date lastModified) {
        if (lastModified == null) {
            return null;
        }
        return new Date(getLastModified(objectId, lastModified.getTime()));
    }

    public void update(final int objectId, final int recurrenceId, final Date lastModified) {
        if (lastModified == null) {
            return;
        }

        if (recurrenceId != 0) {
            if (storage.containsKey(recurrenceId)) {
                storage.get(recurrenceId).setCurrent(lastModified.getTime());
            } else {
                storage.put(recurrenceId, new LastModifiedMemory(lastModified.getTime(), lastModified.getTime()));
            }
        }

        if (objectId != 0) {
            if (storage.containsKey(objectId)) {
                storage.get(objectId).setCurrent(lastModified.getTime());
            } else {
                storage.put(objectId, new LastModifiedMemory(lastModified.getTime(), lastModified.getTime()));
            }
        }
    }

    private static class LastModifiedMemory {

        private long original;

        private long current;

        public LastModifiedMemory(final long original, final long current) {
            this.original = original;
            this.current = current;
        }

        public long getOriginal() {
            return original;
        }

        public void setOriginal(final long original) {
            this.original = original;
        }

        public long getCurrent() {
            return current;
        }

        public void setCurrent(final long current) {
            this.current = current;
        }
    }
}
