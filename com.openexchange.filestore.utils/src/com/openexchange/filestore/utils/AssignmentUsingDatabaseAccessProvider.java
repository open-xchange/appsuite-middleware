/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.filestore.utils;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
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
