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

package com.openexchange.filestore.utils;

import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.openexchange.database.Assignment;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DatabaseAccess;
import com.openexchange.filestore.DatabaseAccessProvider;


/**
 * {@link AssignmentUsingDatabaseAccessProvider} - A database access provider which associates certain file storage and prefix tuples with a given assignment.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class AssignmentUsingDatabaseAccessProvider implements DatabaseAccessProvider {

    /** A file storage identifier and prefix tuple */
    public static final class FileStorageAndPrefix {

        private final int fileStorageId;
        private final String prefix;
        private int hash = 0;

        /**
         * Initializes a new {@link AssignmentUsingDatabaseAccessProvider.FileStorageAndPrefix}.
         */
        public FileStorageAndPrefix(int fileStorageId, String prefix) {
            super();
            this.fileStorageId = fileStorageId;
            this.prefix = prefix;
        }

        /**
         * Gets the file storage identifier
         *
         * @return The file storage identifier
         */
        public int getFileStorageId() {
            return fileStorageId;
        }

        /**
         * Gets the prefix
         *
         * @return The prefix
         */
        public String getPrefix() {
            return prefix;
        }

        @Override
        public int hashCode() {
            int result = hash;
            if (result == 0) {
                int prime = 31;
                result = prime * 1 + fileStorageId;
                result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
                hash = result;
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != FileStorageAndPrefix.class) {
                return false;
            }
            FileStorageAndPrefix other = (FileStorageAndPrefix) obj;
            if (fileStorageId != other.fileStorageId) {
                return false;
            }
            if (prefix == null) {
                if (other.prefix != null) {
                    return false;
                }
            } else if (!prefix.equals(other.prefix)) {
                return false;
            }
            return true;
        }
    }

    // --------------------------------------------------------------------------------

    private final AssignmentUsingDatabaseAccess databaseAccess;
    private final SetMultimap<Integer, String> multimap;

    /**
     * Initializes a new {@link AssignmentUsingDatabaseAccessProvider}.
     *
     * @param assignment The database assignment to use
     * @param databaseService The database service
     * @param tuples The file storage identifier and prefix tuples to associated with given assignment
     * @throws IllegalArgumentException If given tuples are <code>null</code> or empty
     */
    public AssignmentUsingDatabaseAccessProvider(Assignment assignment, DatabaseService databaseService, FileStorageAndPrefix... tuples) {
        super();
        if (null == tuples || tuples.length == 0) {
            throw new IllegalArgumentException("Given tuples must not be emtpy");
        }

        SetMultimap<Integer, String> multimap = SetMultimapBuilder.hashKeys(tuples.length).hashSetValues(1).build();
        for (FileStorageAndPrefix tuple : tuples) {
            if (null != tuple) {
                multimap.put(Integer.valueOf(tuple.getFileStorageId()), tuple.getPrefix());
            }
        }
        if (multimap.isEmpty()) {
            throw new IllegalArgumentException("Given tuples must not be emtpy");
        }
        this.multimap = multimap;
        this.databaseAccess = new AssignmentUsingDatabaseAccess(assignment, databaseService);
    }

    @Override
    public DatabaseAccess getAccessFor(int fileStorageId, String prefix) throws OXException {
        return multimap.containsEntry(Integer.valueOf(fileStorageId), prefix) ? databaseAccess : null;
    }

}
