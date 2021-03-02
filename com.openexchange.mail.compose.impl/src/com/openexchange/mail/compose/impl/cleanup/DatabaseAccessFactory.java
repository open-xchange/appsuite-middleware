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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.mail.compose.impl.cleanup;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.database.Assignment;
import com.openexchange.database.AssignmentFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DatabaseAccessFactory} - Creates the appropriate database access for clean-up task.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class DatabaseAccessFactory {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabaseAccessFactory.class);
    }

    private static final DatabaseAccessFactory INSTANCE = new DatabaseAccessFactory();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static DatabaseAccessFactory getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DatabaseAccessFactory}.
     */
    private DatabaseAccessFactory() {
        super();
    }

    /**
     * Gets the database access for a clean-up task run.
     *
     * @param representativeContextId The representative context identifier associated with a certain database schema.
     * @param services The service look-up used to obtain services
     * @return The database access
     * @throws OXException If database access cannot be created
     */
    public DatabaseAccess createDatabaseAccessFor(int representativeContextId, ServiceLookup services) throws OXException {
        // Acquire required database service
        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);

        // Check for assignment factory availability
        AssignmentFactory optAssignmentFactory = services.getOptionalService(AssignmentFactory.class);
        if (optAssignmentFactory != null) {
            try {
                Assignment assignment = optAssignmentFactory.get(representativeContextId);
                if (assignment != null) {
                    return new AssignmentUsingDatabaseAccess(assignment, representativeContextId, databaseService);
                }
            } catch (Exception e) {
                LoggerHolder.LOG.warn("Failed to initialize assignment-using database access", e);
            }
        }

        // As last resort...
        return new ContextIdUsingDatabaseAccess(representativeContextId, databaseService, services);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class ContextIdUsingDatabaseAccess implements DatabaseAccess {

        private final int representativeContextId;
        private final DatabaseService databaseService;
        private final ServiceLookup services;
        private String schema;

        /**
         * Initializes a new {@link ContextIdUsingDatabaseAccess}.
         *
         * @param representativeContextId The representative context identifier to access context-associated schema
         * @param databaseService The database service
         * @param services The service look-up used to obtain services
         */
        ContextIdUsingDatabaseAccess(int representativeContextId, DatabaseService databaseService, ServiceLookup services) {
            super();
            this.services = services;
            this.representativeContextId = representativeContextId;
            this.databaseService = databaseService;
            schema = null;
        }

        @Override
        public int getRepresentativeContextId() {
            return representativeContextId;
        }

        @Override
        public Optional<String> getSchema() {
            String schema = this.schema;
            if (schema == null) {
                ContextService contextService = services.getOptionalService(ContextService.class);
                if (contextService == null) {
                    return Optional.empty();
                }

                try {
                    Map<PoolAndSchema, List<Integer>> associations = contextService.getSchemaAssociationsFor(Collections.singletonList(I(representativeContextId)));
                    schema = associations.keySet().iterator().next().getSchema();
                    this.schema = schema;
                } catch (Exception e) {
                    return Optional.empty();
                }
            }
            return Optional.of(schema);
        }

        @Override
        public Connection acquireReadOnly() throws OXException {
            return databaseService.getReadOnly(representativeContextId);
        }

        @Override
        public void releaseReadOnly(Connection con) {
            if (null != con) {
                databaseService.backReadOnly(representativeContextId, con);
            }
        }

        @Override
        public Connection acquireWritable() throws OXException {
            return databaseService.getWritable(representativeContextId);
        }

        @Override
        public void releaseWritable(Connection con, boolean forReading) {
            if (null != con) {
                if (forReading) {
                    databaseService.backWritableAfterReading(representativeContextId, con);
                } else {
                    databaseService.backWritable(representativeContextId, con);
                }
            }
        }

    }

    private static class AssignmentUsingDatabaseAccess implements DatabaseAccess {

        private final Assignment assignment;
        private final DatabaseService databaseService;
        private final int representativeContextId;

        /**
         * Initializes a new {@link AssignmentUsingDatabaseAccess}.
         *
         * @param assignment The database assignment
         * @param representativeContextId The representative context identifier to access context-associated schema
         * @param databaseService The database service
         */
        AssignmentUsingDatabaseAccess(Assignment assignment, int representativeContextId, DatabaseService databaseService) {
            super();
            this.assignment = assignment;
            this.representativeContextId = representativeContextId;
            this.databaseService = databaseService;
        }

        @Override
        public int getRepresentativeContextId() {
            return representativeContextId;
        }

        @Override
        public Optional<String> getSchema() {
            return Optional.of(assignment.getSchema());
        }

        @Override
        public Connection acquireReadOnly() throws OXException {
            return databaseService.getReadOnly(assignment, false);
        }

        @Override
        public void releaseReadOnly(Connection con) {
            if (null != con) {
                Databases.close(con);
            }
        }

        @Override
        public Connection acquireWritable() throws OXException {
            return databaseService.getWritable(assignment, false);
        }

        @Override
        public void releaseWritable(Connection con, boolean forReading) {
            if (null != con) {
                Databases.close(con);
            }
        }

    }

}
