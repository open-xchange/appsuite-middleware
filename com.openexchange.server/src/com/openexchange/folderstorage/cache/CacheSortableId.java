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

package com.openexchange.folderstorage.cache;

import com.openexchange.folderstorage.SortableId;

/**
 * {@link CacheSortableId}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CacheSortableId implements SortableId {

    private final String folderId;

    private final int ordinal;

    private final String name;

    /**
     * Initializes a new {@link CacheSortableId}.
     *
     * @param folderId The folder identifier
     * @param ordinal The ordinal
     * @param name The name
     */
    public CacheSortableId(final int folderId, final int ordinal, final String name) {
        super();
        this.folderId = String.valueOf(folderId);
        this.ordinal = ordinal;
        this.name = name;
    }

    /**
     * Initializes a new {@link CacheSortableId}.
     *
     * @param folderId The folder identifier
     * @param ordinal The ordinal
     */
    public CacheSortableId(final String folderId, final int ordinal, final String name) {
        super();
        this.folderId = folderId;
        this.ordinal = ordinal;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return folderId;
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public int compareTo(final SortableId o) {
        if (o instanceof CacheSortableId) {
            final int thisVal = ordinal;
            final int anotherVal = ((CacheSortableId) o).ordinal;
            return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
        }
        final int thisPrio = Priority.NORMAL.ordinal();
        final int anotherPrio = (o).getPriority().ordinal();
        return (thisPrio < anotherPrio ? 1 : (thisPrio == anotherPrio ? 0 : -1));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ordinal;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CacheSortableId)) {
            return false;
        }
        final CacheSortableId other = (CacheSortableId) obj;
        if (ordinal != other.ordinal) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("{folderId=").append(folderId).append(", ordinal=").append(ordinal).append('}').toString();
    }

}
