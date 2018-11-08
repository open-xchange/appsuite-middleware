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

package com.openexchange.database.internal.change.custom;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.openexchange.database.Databases.closeSQLStuff;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.database.Databases;
import com.openexchange.database.SchemaInfo;
import com.openexchange.java.Sets;
import com.openexchange.java.Strings;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import liquibase.change.custom.CustomTaskChange;
import liquibase.change.custom.CustomTaskRollback;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * {@link ChangeFilestore2UserPrimaryKeyCustomTaskChange}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class ChangeFilestore2UserPrimaryKeyCustomTaskChange implements CustomTaskChange, CustomTaskRollback {

    /**
     * Initializes a new {@link ChangeFilestore2UserPrimaryKeyCustomTaskChange}.
     */
    public ChangeFilestore2UserPrimaryKeyCustomTaskChange() {
        super();
    }

    @Override
    public String getConfirmationMessage() {
        return "PRIMARY KEY successfully changed for filestore2user table";
    }

    @Override
    public void setUp() throws SetupException {
        // Nothing
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // Ignore
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException {
        // Ignore
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (!(databaseConnection instanceof JdbcConnection)) {
            throw new CustomChangeException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getName() + ", but of type: " + databaseConnection.getClass().getName());
        }

        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ChangeFilestore2UserPrimaryKeyCustomTaskChange.class);
        Connection configDbCon = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        int rollback = 0;
        try {
            if (existsPrimaryKey(configDbCon, "filestore2user", new String[] {"cid", "user"})) {
                // PRIMARY KEY already changed
                return;
            }

            Databases.startTransaction(configDbCon);
            rollback = 1;

            execute(configDbCon, logger);

            configDbCon.commit();
            rollback = 2;
        } catch (SQLException e) {
            logger.error("Failed to change PRIMARY KEY for \"filestore2user\" table", e);
            throw new CustomChangeException("SQL error", e);
        } catch (CustomChangeException e) {
            logger.error("Failed to change PRIMARY KEY for \"filestore2user\" table", e);
            throw e;
        } catch (RuntimeException e) {
            logger.error("Failed to change PRIMARY KEY for \"filestore2user\" table", e);
            throw new CustomChangeException("Runtime error", e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(configDbCon);
                }
                Databases.autocommit(configDbCon);
            }
        }
    }

    private void execute(Connection configDbCon, org.slf4j.Logger logger) throws CustomChangeException {
        try {
            Map<SchemaInfo, TIntObjectMap<TIntSet>> mapping = readInUsers(configDbCon, logger);
            if (false == mapping.isEmpty()) {
                Map<UserAndContext, Integer> user2filestore = determineFilestoreIdsFor(mapping, configDbCon, logger);
                updateFilestoreAssociation(user2filestore, configDbCon, logger);
            }

            changePrimaryKeyFromFilestore2UserTable(configDbCon, logger);
        } catch (SQLException e) {
            throw new CustomChangeException("SQL error", e);
        } catch (NoConnectionToDatabaseException e) {
            throw new CustomChangeException(e.getMessage(), e);
        }
    }

    private void changePrimaryKeyFromFilestore2UserTable(Connection configDbCon, org.slf4j.Logger logger) throws SQLException {
        // Reset PRIMARY KEY
        if (hasPrimaryKey(configDbCon, "filestore2user")) {
            dropPrimaryKey(configDbCon, "filestore2user");
        }
        createPrimaryKey(configDbCon, "filestore2user", new String[] {"cid", "user"});
        logger.info("PRIMARY KEY for \"filestore2user\" table successfully changed.");
    }

    private void updateFilestoreAssociation(Map<UserAndContext, Integer> user2filestore, Connection configDbCon, org.slf4j.Logger logger) throws SQLException {
        PreparedStatement stmt = null;
        try {
            logger.info("Updating \"filestore2user\" entries...");
            for (Map.Entry<UserAndContext, Integer> entry : user2filestore.entrySet()) {
                UserAndContext user = entry.getKey();
                stmt = configDbCon.prepareStatement("DELETE FROM filestore2user WHERE cid=? AND user=?");
                stmt.setInt(1, user.contextId);
                stmt.setInt(2, user.userId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;

                int filestoreId = entry.getValue().intValue();
                if (filestoreId > 0) {
                    stmt = configDbCon.prepareStatement("INSERT INTO filestore2user (cid, user, filestore_id) VALUES (?, ?, ?)");
                    stmt.setInt(1, user.contextId);
                    stmt.setInt(2, user.userId);
                    stmt.setInt(3, filestoreId);
                    stmt.executeUpdate();
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }
            }
            logger.info("Finished updating \"filestore2user\" entries");
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private Map<UserAndContext, Integer> determineFilestoreIdsFor(Map<SchemaInfo, TIntObjectMap<TIntSet>> mapping, Connection configDbCon, final org.slf4j.Logger logger) throws SQLException, NoConnectionToDatabaseException {
        logger.info("Reading users' file storage association by database schema...");

        TIntObjectMap<DatabaseHost> poolId2Host = new TIntObjectHashMap<DatabaseHost>(mapping.size());
        for (Set<SchemaInfo> partiton : Sets.partition(mapping.keySet(), 100)) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = configDbCon.prepareStatement(Databases.getIN("SELECT db_pool_id, url, driver, login, password, name FROM db_pool WHERE db_pool_id IN (", partiton.size()));
                int pos = 1;
                for (SchemaInfo schemaInfo : partiton) {
                    stmt.setInt(pos++, schemaInfo.getPoolId());
                }
                rs = stmt.executeQuery();
                while (rs.next()) {
                    int poolId = rs.getInt(1);
                    poolId2Host.put(poolId, new DatabaseHost(poolId, rs.getString(6), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
                }
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        }

        Map<DatabaseHost, Map<String, TIntObjectMap<TIntSet>>> host2Schemas = new LinkedHashMap<>(mapping.size());
        for (Map.Entry<SchemaInfo, TIntObjectMap<TIntSet>> entry : mapping.entrySet()) {
            SchemaInfo schemaInfo = entry.getKey();
            DatabaseHost databaseHost = poolId2Host.get(schemaInfo.getPoolId());

            Map<String, TIntObjectMap<TIntSet>> schema2Contexts = host2Schemas.get(databaseHost);
            if (null == schema2Contexts) {
                schema2Contexts = new LinkedHashMap<String, TIntObjectMap<TIntSet>>();
                host2Schemas.put(databaseHost, schema2Contexts);
            }

            schema2Contexts.put(schemaInfo.getSchema(), entry.getValue());
        }

        // Free some memory
        mapping.clear();
        poolId2Host = null;

        int numberOfTasks = host2Schemas.size();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(numberOfTasks);
        final Map<Thread, Thread> executers = new ConcurrentHashMap<Thread, Thread>(numberOfTasks);

        final AtomicReference<Exception> errorRef = new AtomicReference<Exception>();
        final Map<UserAndContext, Integer> map = new ConcurrentHashMap<UserAndContext, Integer>();
        for (Map.Entry<DatabaseHost, Map<String, TIntObjectMap<TIntSet>>> entry : host2Schemas.entrySet()) {
            final DatabaseHost databaseHost = entry.getKey();
            final Map<String, TIntObjectMap<TIntSet>> schema2Contexts = entry.getValue();
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    try {
                        try {
                            startLatch.await();
                        } catch (InterruptedException e) {
                            logger.info("Interrupted reading users' file storage association from database host {}", databaseHost, e);
                            return;
                        }

                        boolean keepOn = true;
                        int maxRunCount = 5;
                        int runCount = 0;
                        while (keepOn && runCount < maxRunCount) {
                            keepOn = false;
                            Connection con = null;
                            PreparedStatement stmt = null;
                            ResultSet rs = null;
                            try {
                                con = getSimpleSQLConnectionFor(databaseHost.url, databaseHost.driver, databaseHost.login, databaseHost.password);
                                NextSchema: for (Map.Entry<String, TIntObjectMap<TIntSet>> s2c : schema2Contexts.entrySet()) {
                                    if (Thread.interrupted()) {
                                        throw new InterruptedException();
                                    }

                                    String schema = s2c.getKey();
                                    TIntObjectMap<TIntSet> context2users = s2c.getValue();

                                    // Set schema
                                    try {
                                        con.setCatalog(schema);
                                    } catch (SQLException e) {
                                        // Such a schema does not exist. Add dummy entries to result map.
                                        logger.info("No such schema \"{}\" exists on database host {}. Pruning affected entries from \"filestore2user\" table.", schema, databaseHost, e);
                                        for (TIntObjectIterator<TIntSet> it = context2users.iterator(); it.hasNext();) {
                                            if (Thread.interrupted()) {
                                                throw new InterruptedException();
                                            }

                                            it.advance();
                                            int contextId = it.key();
                                            TIntSet userIds = it.value();
                                            Integer zero = Integer.valueOf(0);
                                            for (TIntIterator it2 = userIds.iterator(); it2.hasNext();) {
                                                int userId = it2.next();
                                                map.put(new UserAndContext(userId, contextId), zero);
                                            }
                                        }
                                        continue NextSchema;
                                    }

                                    // Schema is valid
                                    logger.info("Reading users' file storage association from \"{}\" database schema at host {}", schema, databaseHost);

                                    for (TIntObjectIterator<TIntSet> it = context2users.iterator(); it.hasNext();) {
                                        if (Thread.interrupted()) {
                                            throw new InterruptedException();
                                        }

                                        it.advance();
                                        int contextId = it.key();
                                        TIntSet userIds = it.value();

                                        stmt = con.prepareStatement(Databases.getIN("SELECT id, filestore_id FROM user WHERE cid=? AND id IN (", userIds.size()));
                                        int pos = 1;
                                        stmt.setInt(pos++, contextId);
                                        for (TIntIterator it2 = userIds.iterator(); it2.hasNext();) {
                                            int userId = it2.next();
                                            stmt.setInt(pos++, userId);
                                        }
                                        rs = stmt.executeQuery();
                                        while (rs.next()) {
                                            map.put(new UserAndContext(rs.getInt(1), contextId), Integer.valueOf(rs.getInt(2)));
                                        }
                                        Databases.closeSQLStuff(rs, stmt);
                                        rs = null;
                                        stmt = null;
                                    }
                                }
                                logger.info("Finished reading users' file storage association from database host {}", databaseHost);
                            } catch (InterruptedException e) {
                                logger.info("Interrupted reading users' file storage association from database host {}", databaseHost, e);
                            } catch (SQLException sqle) {
                                boolean doThrow = true;

                                if (sqle instanceof SQLNonTransientConnectionException) {
                                    SQLNonTransientConnectionException connectionException = (SQLNonTransientConnectionException) sqle;
                                    if (isTooManyConnections(connectionException) && (++runCount < maxRunCount)) {
                                        waitWithExponentialBackoff(runCount, 1000L);
                                        doThrow = false;
                                        keepOn = true;
                                    }
                                }

                                if (doThrow) {
                                    logger.info("Failed reading users' file storage association from database host {}", databaseHost, sqle);
                                    // Interrupt remaining threads & set exception reference
                                    for (Thread executer : executers.keySet()) {
                                        if (executer != Thread.currentThread()) {
                                            executer.interrupt();
                                        }
                                    }
                                    errorRef.set(sqle);
                                }
                            } catch (Exception e) {
                                logger.info("Failed reading users' file storage association from database host {}", databaseHost, e);
                                // Interrupt remaining threads & set exception reference
                                for (Thread executer : executers.keySet()) {
                                    if (executer != Thread.currentThread()) {
                                        executer.interrupt();
                                    }
                                }
                                errorRef.set(e);
                            } finally {
                                Databases.closeSQLStuff(rs, stmt);
                                if (null != con) {
                                    try {
                                        con.close();
                                    } catch (Exception e) {
                                        logger.warn("Failed to close connection to database host {}", databaseHost, e);
                                    }
                                }
                            }
                        } // End of while loop
                        // Leave run() method...
                    } finally {
                        completionLatch.countDown();
                        executers.remove(Thread.currentThread());
                    }
                }

                private boolean isTooManyConnections(SQLNonTransientConnectionException connectionException) {
                    String message = connectionException.getMessage();
                    return null != message && Strings.asciiLowerCase(message).indexOf("too many connections") >= 0;
                }

                private void waitWithExponentialBackoff(int retryCount, long baseMillis) {
                    long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * baseMillis) + ((long) (Math.random() * baseMillis)), TimeUnit.MILLISECONDS);
                    LockSupport.parkNanos(nanosToWait);
                }

            };
            Thread executer = new Thread(task);
            executers.put(executer, executer);
            executer.start();
        }

        try {
            logger.info("Awaiting completion of reading users' file storage association by database schema");
            startLatch.countDown();
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyMap();
        }

        Exception exception = errorRef.get();
        if (null != exception) {
            // Handle exception instance
            if (exception instanceof NoConnectionToDatabaseException) {
                throw (NoConnectionToDatabaseException) exception;
            }
            if (exception instanceof SQLException) {
                throw (SQLException) exception;
            }
            if (exception instanceof InterruptedException) {
                // Ignore
            }
            throw new RuntimeException(exception);
        }

        logger.info("Finished reading users' file storage association by database schema");
        return map;
    }

    private Map<SchemaInfo, TIntObjectMap<TIntSet>> readInUsers(Connection configDbCon, org.slf4j.Logger logger) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = configDbCon.prepareStatement("SELECT cid, user FROM filestore2user");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyMap();
            }

            // Read all users kept in filestore2user table
            TIntIntMap cid2user = new TIntIntHashMap();
            do {
                cid2user.put(rs.getInt(1), rs.getInt(2));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            logger.info("Collected all entries from \"filestore2user\" table");

            // Group them by database schema association
            TIntObjectMap<SchemaInfo> context2SchemaInfo = getSchemaAssociationsFor(cid2user.keys(), configDbCon);
            TIntSet nonExisting = null;
            Map<SchemaInfo, TIntObjectMap<TIntSet>> map = new LinkedHashMap<>();
            for (TIntIntIterator it = cid2user.iterator(); it.hasNext();) {
                it.advance();
                int contextId = it.key();
                int userId = it.value();

                SchemaInfo pas = context2SchemaInfo.get(contextId);
                if (null == pas) {
                    // No such context
                    if (null == nonExisting) {
                        nonExisting = new TIntHashSet();
                    }
                    nonExisting.add(contextId);
                } else {
                    TIntObjectMap<TIntSet> context2users = map.get(pas);
                    if (null == context2users) {
                        context2users = new TIntObjectHashMap<>();
                        map.put(pas, context2users);
                    }

                    TIntSet userIds = context2users.get(contextId);
                    if (null == userIds) {
                        userIds = new TIntHashSet();
                        context2users.put(contextId, userIds);
                    }

                    userIds.add(userId);
                }
            }
            logger.info("Grouped collected entries from \"filestore2user\" table by database schema association");

            // Check for non-existing
            if (null != nonExisting) {
                stmt = configDbCon.prepareStatement(Databases.getIN("DELETE FROM filestore2user WHERE cid IN (", nonExisting.size()));
                int pos = 1;
                for (int contextId : nonExisting.toArray()) {
                    stmt.setInt(pos++, contextId);
                }
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }

            // Return mapping
            return map;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private TIntObjectMap<SchemaInfo> getSchemaAssociationsFor(int[] contextIds, Connection configDbCon) throws SQLException {
        TIntObjectMap<SchemaInfo> context2SchemaInfo = new TIntObjectHashMap<SchemaInfo>(contextIds.length);
        for (int[] partition : partition(contextIds, 100)) {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = configDbCon.prepareStatement(Databases.getIN("SELECT write_db_pool_id, db_schema, cid FROM context_server2db_pool WHERE cid IN (", partition.length));
                for (int pos = 1; pos <= partition.length; pos++) {
                    stmt.setInt(pos, partition[pos - 1]);
                }
                result = stmt.executeQuery();
                while (result.next()) {
                    context2SchemaInfo.put(result.getInt(3)/*cid*/, SchemaInfo.valueOf(result.getInt(1)/*write_db_pool_id*/, result.getString(2)/*db_schema*/));
                }
            } finally {
                closeSQLStuff(result, stmt);
            }
        }
        return context2SchemaInfo;
    }

    static Connection getSimpleSQLConnectionFor(String url, String driver, String login, String password) throws NoConnectionToDatabaseException {
        String passwd = "";
        if (password != null) {
            passwd = password;
        }

        try {
            Class.forName(driver);
            DriverManager.setLoginTimeout(120);

            Properties defaults = new Properties();
            defaults.setProperty("useSSL", "false");
            defaults.put("user", login);
            defaults.put("password", passwd);

            return DriverManager.getConnection(url, defaults);
        } catch (ClassNotFoundException e) {
            throw new NoConnectionToDatabaseException("Database host " + extractHostName(url) + " is not accessible: No such driver class: " + driver, e);
        } catch (SQLException e) {
            throw new NoConnectionToDatabaseException("Database host " + extractHostName(url) + " is not accessible: " + e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private static final class DatabaseHost {

        final int dbId;
        final String name;
        final String url;
        final String driver;
        final String login;
        final String password;
        int hash = 0;

        DatabaseHost(int dbId, String name, String url, String driver, String login, String password) {
            super();
            this.dbId = dbId;
            this.name = name;
            this.url = url;
            this.driver = driver;
            this.login = login;
            this.password = password;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (h == 0) {
                h = 31 * 1 + dbId;
                hash = h;
            }
            return h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DatabaseHost)) {
                return false;
            }
            DatabaseHost other = (DatabaseHost) obj;
            if (dbId != other.dbId) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(32);
            if (name != null) {
                sb.append(name).append('@');
            }
            if (url != null) {
                sb.append(extractHostName(url));
            }
            if (sb.length() > 0) {
                sb.append(" (id=").append(dbId).append(')');
            } else {
                sb.append(dbId);
            }
            return sb.toString();
        }
    }

    /**
     * A user and context identifier pair
     */
    private static final class UserAndContext {

        /** The user identifier */
        final int userId;

        /** The context identifier */
        final int contextId;

        private final int hash;

        UserAndContext(int userId, int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;

            int prime = 31;
            int result = prime * 1 + contextId;
            result = prime * result + userId;
            hash = result;
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
            if (!(obj instanceof UserAndContext)) {
                return false;
            }
            UserAndContext other = (UserAndContext) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    }

    private static final class NoConnectionToDatabaseException extends Exception {

        private static final long serialVersionUID = 8920076916162330786L;

        NoConnectionToDatabaseException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    static String extractHostName(String jdbcUrl) {
        if (null == jdbcUrl) {
            return null;
        }

        String urlToParse = jdbcUrl;
        if (urlToParse.startsWith("jdbc:")) {
            urlToParse = urlToParse.substring(5);
        }

        try {
            return new URI(urlToParse).getHost();
        } catch (URISyntaxException e) {
            int start = urlToParse.indexOf("://");
            int end = urlToParse.indexOf('/', start + 1);
            return urlToParse.substring(start + 3, end);
        }
    }

    private static final boolean existsPrimaryKey(final Connection con, final String table, final String[] columns) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        final List<String> foundColumns = new ArrayList<String>();
        ResultSet result = null;
        try {
            result = metaData.getPrimaryKeys(null, null, table);
            while (result.next()) {
                final String columnName = result.getString(4);
                final int columnPos = result.getInt(5);
                while (foundColumns.size() < columnPos) {
                    foundColumns.add(null);
                }
                foundColumns.set(columnPos - 1, columnName);
            }
        } finally {
            closeSQLStuff(result);
        }
        boolean matches = columns.length == foundColumns.size();
        for (int i = 0; matches && i < columns.length; i++) {
            matches = columns[i].equalsIgnoreCase(foundColumns.get(i));
        }
        return matches;
    }

    private static final boolean hasPrimaryKey(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        // Get primary keys
        final ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, table);
        try {
            return primaryKeys.next();
        } finally {
            closeSQLStuff(primaryKeys);
        }
    }

    private static final void dropPrimaryKey(final Connection con, final String table) throws SQLException {
        final String sql = "ALTER TABLE `" + table + "` DROP PRIMARY KEY";
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private static final void createPrimaryKey(final Connection con, final String table, final String[] columns) throws SQLException {
        final int[] lengths = new int[columns.length];
        Arrays.fill(lengths, -1);
        createPrimaryKey(con, table, columns, lengths);
    }

    private static final void createPrimaryKey(final Connection con, final String table, final String[] columns, final int[] lengths) throws SQLException {
        createKey(con, table, columns, lengths, true, null);
    }

    private static final void createKey(final Connection con, final String table, final String[] columns, final int[] lengths, boolean primary, String name) throws SQLException {
        final StringBuilder sql = new StringBuilder("ALTER TABLE `");
        sql.append(table);
        sql.append("` ADD ");
        if (primary) {
            sql.append("PRIMARY ");
        }
        sql.append("KEY ");
        if (!primary && Strings.isNotEmpty(name)) {
            sql.append('`').append(name).append('`');
        }
        sql.append(" (");
        {
            final String column = columns[0];
            sql.append('`').append(column).append('`');
            final int len = lengths[0];
            if (len > 0) {
                sql.append('(').append(len).append(')');
            }
        }
        for (int i = 1; i < columns.length; i++) {
            final String column = columns[i];
            sql.append(',');
            sql.append('`').append(column).append('`');
            final int len = lengths[i];
            if (len > 0) {
                sql.append('(').append(len).append(')');
            }
        }
        sql.append(')');
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sql.toString());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private static List<int[]> partition(int[] original, int partitionSize) {
        checkNotNull(original, "Array must not be null");
        checkArgument(partitionSize > 0);
        int total = original.length;
        if (partitionSize >= total) {
            return Collections.singletonList(original);
        }

        // Create a list of sets to return.
        List<int[]> result = new ArrayList<>((total + partitionSize - 1) / partitionSize);

        // Create each new array.
        int stopIndex = 0;
        for (int startIndex = 0; startIndex + partitionSize <= total; startIndex += partitionSize) {
            stopIndex += partitionSize;
            result.add(java.util.Arrays.copyOfRange(original, startIndex, stopIndex));
        }
        if (stopIndex < total) {
            result.add(java.util.Arrays.copyOfRange(original, stopIndex, total));
        }
        return result;
    }

}
