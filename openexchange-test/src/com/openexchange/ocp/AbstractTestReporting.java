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

package com.openexchange.ocp;

import static com.openexchange.ocp.OCPConfig.Property.BRAND;
import static com.openexchange.ocp.OCPConfig.Property.DATABASE_PASSWORD;
import static com.openexchange.ocp.OCPConfig.Property.DATABASE_URL;
import static com.openexchange.ocp.OCPConfig.Property.DATABASE_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableList;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link AbstractTestReporting}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.12.0
 */
public abstract class AbstractTestReporting {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTestReporting.class);

    /**
     * Initializes a new {@link AbstractTestReporting}.
     */
    public AbstractTestReporting() {
        super();
    }

    /**
     * Initialises the test configuration and creates one context for the tests
     * 
     * @throws Exception if an error occurs during initialisation of the configuration
     */
    @BeforeClass
    public static void setUpEnvironment() throws OXException {
        AJAXConfig.init();
        OCPConfig.init();
    }

    /**
     * Initialises the test case
     *
     * @throws Exception if an error is occurred
     */
    @Before
    public void setUp() throws Exception {
        postSetup();
    }

    /**
     * Clens-up the test case
     * 
     * @throws Exception if an error is occurred
     */
    @After
    public void tearDown() throws Exception {
        postTearDown();
    }

    /**
     * Performs post-setup tasks
     */
    protected abstract void postSetup() throws Exception;

    /**
     * Performs post-tear down tasks
     */
    protected abstract void postTearDown() throws Exception;

    //////////////////////////////// Assertion Helpers //////////////////////////

    /**
     * Retrieves and asserts the amount of reporting events
     * 
     * @param table The SQL table
     * @param exepectedAmount the expected amount of reporting events
     * @return The retrieved reporting events
     */
    protected List<DatabaseReportingEvent> getAndAssertReportingEvents(String table, int exepectedAmount) {
        List<DatabaseReportingEvent> events = getReportingEventsFrom(table);
        assertEquals("The expected amount of provisioning events do not match", exepectedAmount, events.size());
        return events;
    }

    /**
     * Asserts that the specified event is of the expected type
     * 
     * @param event The event
     * @param contextId The contextId
     * @param expectedEventType The expected event type
     * @param expectedReseller TODO
     */
    protected void assertEvent(DatabaseReportingEvent event, int contextId, int expectedEventType, String expectedReseller) {
        assertEquals("The context id does not match", event.getContextId(), contextId);
        assertEquals("The event type does not match", event.getEventType(), expectedEventType);
        assertEquals("The reseller does not match", event.getReseller(), expectedReseller);
    }

    //////////////////////////////// Database Helpers ///////////////////////////////////////////

    /**
     * Retrieves a connection to the pre-configured {@link OCPConfig.Property#BRAND} schema
     *
     * @return The connection
     * @throws SQLException if an SQL error is occurred
     */
    Connection getConnection() throws SQLException {
        String server = OCPConfig.getProperty(DATABASE_URL);
        String schema = OCPConfig.getProperty(BRAND);
        Properties connectionProps = new Properties();
        connectionProps.put("user", OCPConfig.getProperty(DATABASE_USERNAME));
        connectionProps.put("password", OCPConfig.getProperty(DATABASE_PASSWORD));

        return DriverManager.getConnection("jdbc:mysql://" + server + "/" + schema, connectionProps);
    }

    /**
     * Cleans up the specified tables
     */
    protected void cleanUp(String table) {
        truncateTable(table);
        if (countEntriesIn("id", table) > 0) {
            LOG.warn("The table {} is not truncated or contains entries from previous tests", table);
        }
    }

    /**
     * Truncates the specified table
     *
     * @param table The table to truncate
     */
    void truncateTable(String table) {
        PreparedStatement stmt = null;
        try (Connection connection = getConnection()) {
            stmt = connection.prepareStatement("TRUNCATE " + table);
            stmt.execute();
        } catch (SQLException e) {
            LOG.warn("Unable to truncate table {}", table, e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Counts the rows in the specified table
     *
     * @param countColumn The column to count
     * @param table The table name
     * @return The amount of rows or -1 if an error is occurred
     */
    int countEntriesIn(String countColumn, String table) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try (Connection connection = getConnection()) {
            stmt = connection.prepareStatement("SELECT count(" + countColumn + ") AS r FROM " + table);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("r") : 0;
        } catch (SQLException e) {
            LOG.warn("Unable to truncate table {}", table, e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
        return -1;
    }

    /**
     * Retrieves all reporting events from the specified table
     *
     * @param table The table
     * @return A {@link List} with all events or an empty {@link List}
     *         if no events were found. Never <code>null</code>.
     */
    List<DatabaseReportingEvent> getReportingEventsFrom(String table) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try (Connection connection = getConnection()) {
            stmt = connection.prepareStatement("SELECT * FROM " + table);
            rs = stmt.executeQuery();

            List<DatabaseReportingEvent> reportingEvents = new LinkedList<>();
            while (rs.next()) {
                reportingEvents.add(convertToEvent(rs));
            }
            return reportingEvents;
        } catch (SQLException e) {
            LOG.error("{}", e);
            fail("An SQL error occurred.");
            return ImmutableList.of(); //Will never return
        } finally {
            Databases.closeSQLStuff(stmt, rs);
        }
    }

    /**
     * Converts the specified {@link ResultSet} to a {@link DatabaseReportingEvent}
     * 
     * @param rs The {@link ResultSet} to convert
     * @return The {@link DatabaseReportingEvent}
     * @throws SQLException if an SQL error is occurred
     */
    private DatabaseReportingEvent convertToEvent(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int cid = rs.getInt("context_id");
        int uid = rs.getInt("user_id");
        String reseller = rs.getString("reseller");
        int eventType = rs.getInt("event_type");
        long timestamp = rs.getLong("event_timestamp");
        return new DatabaseReportingEvent(id, reseller, cid, uid, eventType, timestamp);
    }
}
