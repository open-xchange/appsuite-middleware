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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.jaxrs.database.internal;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.jaxrs.JAXRSService;
import com.openexchange.jaxrs.database.DatabaseRESTService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AbstractDatabaseRESTService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractDatabaseRESTService extends JAXRSService implements DatabaseRESTService {

    /**
     * Initializes a new {@link AbstractDatabaseRESTService}.
     * 
     * @param services
     */
    protected AbstractDatabaseRESTService(ServiceLookup services) {
        super(services);
    }

    /**
     * Performs a query to the 'configdb'. Automatically extracts and parses the body of the request.
     * Supports both 'text/plain' and 'application/json' content types.
     * 
     * - text/plain
     * Supports only a single SQL query which is contained in the body of the request as text/plain.
     * - application/json
     * Supports multiple queries (as prepared statements) and are contained in the body of the request
     * as application/json.
     * 
     * For example:
     * <pre>
     * {
     * "someQuery" : {
     * "query" : "SELECT * FROM context WHERE cid = ?",
     * "params" : [1618]
     * },
     * "someOtherQuery" : {
     * "query" : "SELECT name,cid FROM context WHERE cid = ?",
     * "params" : [314]
     * }
     * }
     * </pre>
     * 
     * @return A JSONObject with the result set
     * @throws OXException If an error occurs
     */
    protected JSONObject performQueryConfigDB() throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.returnConnectionWhenDone(DatabaseAccessType.READ);
        performer.setConnection(getService(DatabaseService.class).getReadOnly());
        try {
            return performer.perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Performs an update query to the 'configdb'. Automatically extracts and parses the body of the request.
     * 
     * - text/plain:
     * Supports only a single SQL update statement which is contained in the body of the request as text/plain.
     * 
     * - application/json:
     * Supports multiple SQL update statements which are contained in the body of the request as application/json.
     * 
     * For example:
     * <pre>
     * {
     * "someUpdate" : {
     * "query" : "UPDATE context SET quota_max = ? WHERE cid = ?",
     * "params" : [1048576, 1618]
     * },
     * "someOtherUpdate" : {
     * "query" : "UPDATE context SET name = ? WHERE cid = ?",
     * "params" : ["PI is awesome", 314]
     * }
     * }
     * </pre>
     * 
     * @return A JSONObject with the outcome of the result (updated=1 or update=0)
     * @throws OXException If an error occurs
     */
    protected JSONObject performUpdateConfigDB() throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.returnConnectionWhenDone(DatabaseAccessType.WRITE);
        performer.setConnection(getService(DatabaseService.class).getWritable());
        try {
            return performer.perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Performs a query to an OX database with the specified context identifier.
     * 
     * @param ctxId
     * @return
     * @throws OXException
     */
    protected JSONObject performQueryOXDB(int ctxId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.returnConnectionWhenDone(DatabaseAccessType.READ, ctxId);
        performer.setConnection(getService(DatabaseService.class).getReadOnly(ctxId));
        try {
            return performer.perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Issues updates and inserts to an OX database with the specified context identifier
     * 
     * @param ctxId The context identifier
     * @return A JSONObject with the outcome of the result (updated=1 or update=0)
     * @throws OXException If an error occurs
     */
    protected JSONObject performUpdateOXDB(int ctxId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.returnConnectionWhenDone(DatabaseAccessType.WRITE, ctxId);
        performer.setConnection(getService(DatabaseService.class).getWritable(ctxId));
        try {
            return performer.perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Executes the transaction with the specified transaction identifier
     * 
     * @param txId The transaction identifier
     * @return
     * @throws OXException
     */
    protected JSONObject performQueryTransaction(String txId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        try {
            return performer.executeTransaction(txId);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Rolls back the transaction with the specified transaction identifier
     * 
     * @param txId The transaction identifier
     * @throws OXException
     */
    protected void performRollbackTransaction(String txId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.rollbackTransaction(txId);
    }

    /**
     * Commits the transaction with the specified transaction identifier
     * 
     * @param txId The transaction identifier
     * @throws OXException
     */
    protected void performCommitTransaction(String txId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.commitTransaction(txId);
    }

    /**
     * Query a monitored connection
     * 
     * @param readId The read pool identifier
     * @param writeId The write pool identifier
     * @param schema The schema name
     * @param partitionId The partition identifier
     * @throws OXException If the operation fails
     */
    protected JSONObject performQueryInMonitoredConnection(int readId, int writeId, String schema, int partitionId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.returnMonitoredConnectionWhenDone(DatabaseAccessType.READ, readId, writeId, schema, partitionId);
        performer.setConnection(getService(DatabaseService.class).getReadOnlyMonitored(readId, writeId, schema, partitionId));

        try {
            return performer.perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Update a monitored connection
     * 
     * @param readId The read pool identifier
     * @param writeId The write pool identifier
     * @param schema The schema name
     * @param partitionId The partition identifier
     * @throws OXException If the operation fails
     */
    protected JSONObject performUpdateInMonitoredConnection(int readId, int writeId, String schema, int partitionId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.returnMonitoredConnectionWhenDone(DatabaseAccessType.WRITE, readId, writeId, schema, partitionId);
        performer.setConnection(getService(DatabaseService.class).getWritableMonitored(readId, writeId, schema, partitionId));

        try {
            return performer.perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Initialize a new database schema with the specified name and the specified write pool identifier.
     * 
     * @param writePoolId The write pool identifier
     * @param schema The schema name
     * @throws OXException If the initialization of the new schema fails
     */
    protected void performInitSchema(int writeId, String schema) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.initSchema(writeId, schema);
    }

    /**
     * Inserts the partition identifiers to the replication monitor table
     * 
     * @param writeId The write identifier referencing the master db server
     * @param schema The name of the schema
     * @throws OXException If the operation fails
     */
    protected void performInsertPartitionIds(int writeId, String schema) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.insertPartitionIds(writeId, schema);
    }

    /**
     * Unlocks a schema/module combination.
     * 
     * @param ctxId The context identifier
     * @param module The module
     * @throws OXException If the operation fails
     */
    protected void performUnlock(int ctxId, String module) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.unlock(ctxId, module);
    }

    /**
     * Unlocks a schema/module combination for the specified context identifier.
     * 
     * @param readPoolId The read pool identifier
     * @param writePoolId The write pool identifier
     * @param schema The schema name
     * @param partitionId The partition identifier
     * @param module The module name
     * @throws OXException If the operation fails
     */
    protected void performUnlockMonitored(int readId, int writeId, String schema, int partitionId, String module) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.unlockMonitored(readId, writeId, schema, partitionId, module);
    }

    /**
     * Migrate from the specified version to the specified version
     * 
     * @param ctxId The context identifier
     * @param fromVersion Version updating from
     * @param toVersion Version updating to
     * @param module The module name
     * @throws OXException If the operation fails
     */
    protected JSONObject performMigrate(int ctxId, String fromVersion, String toVersion, String module) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        return performer.migrate(ctxId, fromVersion, toVersion, module);
    }

    /**
     * Migrate from the specified version to the specified version by using a monitored connection
     * 
     * @param readId The read identifier
     * @param writeId The write identifier
     * @param schema The name of the schema
     * @param partitionId The partition identifier
     * @param fromVersion Version updating from
     * @param toVersion Version updating to
     * @param module The module name
     * @throws OXException If the operation fails
     */
    protected JSONObject performMigrateMonitored(int readId, int writeId, String schema, int partitionId, String fromVersion, String toVersion, String module) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        return performer.migrateMonitored(readId, writeId, schema, partitionId, fromVersion, toVersion, module);
    }
}
