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

package com.openexchange.objectusecount;

import java.sql.Connection;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;


/**
 * {@link BatchIncrementArguments}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class BatchIncrementArguments extends IncrementArguments {

    /**
     * A builder for an {@code BatchIncrementArguments} instance.
     */
    public static class Builder {

        private final TObjectIntMap<ObjectAndFolder> counts;
        private Connection con;

        /**
         * Initializes a new {@link BatchIncrementArguments.Builder}.
         */
        public Builder() {
            super();
            counts = new TObjectIntHashMap<>(6, 0.9F, 0);
        }

        /**
         * Sets the connection
         *
         * @param con The connection to use or <code>null</code>
         * @return This builder
         */
        public Builder setCon(Connection con) {
            this.con = con;
            return this;
        }

        /**
         * Adds the specified object and folder identifier pair to this builder
         *
         * @param objectId The object identifier
         * @param folderId The folder identifier
         * @return This builder
         */
        public Builder add(int objectId, int folderId) {
            ObjectAndFolder key = new ObjectAndFolder(objectId, folderId);
            int count = counts.get(key);
            counts.put(key, count + 1);
            return this;
        }

        /**
         * Creates the appropriate {@code UpdateProperties} instance
         *
         * @return The instance
         */
        public BatchIncrementArguments build() {
            final ImmutableMap.Builder<ObjectAndFolder, Integer> b = ImmutableMap.builder();
            counts.forEachEntry(new TObjectIntProcedure<ObjectAndFolder>() {

                @Override
                public boolean execute(ObjectAndFolder key, int count) {
                    b.put(key, Integer.valueOf(count));
                    return true;
                }
            });
            return new BatchIncrementArguments(b.build(), con);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------- //

    private final Map<ObjectAndFolder, Integer> counts;

    /**
     * Initializes a new {@link BatchIncrementArguments}.
     *
     * @param counts The object counts
     * @param con The connection to use or <code>null</code>
     */
    BatchIncrementArguments(Map<ObjectAndFolder, Integer> counts, Connection con) {
        super(con, null, 0, 0, 0, false);
        this.counts = counts;
    }

    /**
     * Gets the (immutable) counts
     *
     * @return The counts
     */
    public Map<ObjectAndFolder, Integer> getCounts() {
        return counts;
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    /** Represents an object/folder identifier pair */
    public static final class ObjectAndFolder {

        private final int objectId;
        private final int folderId;
        private final int hash;

        /**
         * Initializes a new {@link ObjectAndFolder}.
         *
         * @param objectId The object identifier
         * @param folderId The folder identifier
         */
        public ObjectAndFolder(int objectId, int folderId) {
            super();
            this.objectId = objectId;
            this.folderId = folderId;

            int prime = 31;
            int result = prime * 1 + objectId;
            result = prime * result + folderId;
            this.hash = result;
        }

        /**
         * Gets the object identifier
         *
         * @return The object identifier
         */
        public int getObjectId() {
            return objectId;
        }

        /**
         * Gets the folder identifier
         *
         * @return The folder identifier
         */
        public int getFolderId() {
            return folderId;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof ObjectAndFolder)) {
                return false;
            }

            ObjectAndFolder other = (ObjectAndFolder) obj;
            if (objectId != other.objectId) {
                return false;
            }
            if (folderId != other.folderId) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ObjectAndFolder [objectId=").append(objectId).append(", folderId=").append(folderId).append("]");
            return builder.toString();
        }
    }

}
