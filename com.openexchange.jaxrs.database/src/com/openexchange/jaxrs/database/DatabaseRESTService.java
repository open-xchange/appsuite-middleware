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

package com.openexchange.jaxrs.database;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.jaxrs.JAXRSService;
import com.openexchange.jaxrs.database.internal.DatabaseAccessType;
import com.openexchange.jaxrs.database.internal.DatabaseRESTPerformer;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link DatabaseRESTService} exposes database access via an HTTP API. See doc/README.md for all the details.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.0
 */
@Path("/database/v1")
public class DatabaseRESTService extends JAXRSService {

    /*
     * TODO:
     * - Separate methods for consuming text/plain and application/json
     */

    /**
     * Initializes a new {@link DatabaseRESTService}.
     */
    public DatabaseRESTService(ServiceLookup services) {
        super(services);
    }

    /**
     * Performs a query to the configdb.
     * 
     * @return A JSONObject with the result set
     * @throws OXException If an error occurs
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configdb/readOnly")
    public JSONObject queryConfigDB() throws OXException {
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
     * Performs an update query to the configdb
     * 
     * @return A JSONObject with the outcome of the result (updated=1 or update=0)
     * @throws OXException If an error occurs
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configdb/writable")
    public JSONObject updateConfigDB() throws OXException {
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
     * Performs a query to an OX database with the specified context identifier
     * 
     * @param ctxId The context identifier
     * @return A JSONObject with the result set
     * @throws OXException If an error occurs
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/oxdb/{ctxId}/readOnly")
    public JSONObject queryOXDB(@PathParam("ctxId") int ctxId) throws OXException {
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
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/oxdb/{ctxId}/writable")
    public JSONObject updateOXDB(@PathParam("ctxId") int ctxId) throws OXException {
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
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/transaction/{transactionId}")
    public JSONObject queryTransaction(@PathParam("txId") String txId) throws OXException {
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
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/transaction/{transactionId}/rollback")
    public void rollbackTransaction(@PathParam("txId") String txId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.rollbackTransaction(txId);
    }

    /**
     * Commits the transaction with the specified transaction identifier
     * 
     * @param txId The transaction identifier
     * @throws OXException
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/transaction/{transactionId}/commit")
    public void commitTransaction(@PathParam("txId") String txId) throws OXException {
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
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/readOnly")
    public void queryInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.returnMonitoredConnectionWhenDone(DatabaseAccessType.READ, readId, writeId, schema, partitionId);
        performer.setConnection(getService(DatabaseService.class).getReadOnlyMonitored(readId, writeId, schema, partitionId));

        try {
            performer.perform();
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Query a monitored connection
     * 
     * @param readId The read pool identifier
     * @param writeId The write pool identifier
     * @param schema The schema name
     * @throws OXException If the operation fails
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/readOnly")
    public void queryInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException {
        queryInMonitoredConnection(readId, writeId, schema, 0);
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
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/writable")
    public void updateInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.returnMonitoredConnectionWhenDone(DatabaseAccessType.WRITE, readId, writeId, schema, partitionId);
        performer.setConnection(getService(DatabaseService.class).getWritableMonitored(readId, writeId, schema, partitionId));

        try {
            performer.perform();
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
     * @throws OXException If the operation fails
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/writable")
    public void updateInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException {
        updateInMonitoredConnection(readId, writeId, schema, 0);
    }

        
    /**
     * Initialize a new database schema with the specified name and the specified write pool identifier.
     * 
     * @param writePoolId The write pool identifier
     * @param schema The schema name
     * @throws OXException If the initialization of the new schema fails
     */
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/init/w/{writeId}/{schema}")
    public void initSchema(@PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException {
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
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/w/{writeId}/{schema}/partitions")
    public void insertPartitionIds(@PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException {
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
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/unlock/for/{ctxId}/andModule/{module}")
    public void unlock(@PathParam("ctxId") int ctxId, @PathParam("module") String module) throws OXException {
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
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/unlock/pool/r/{readId}/w/{writeId}/{schema}/andModule/{module}")
    public void unlockMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("module") String module) throws OXException {
        unlockMonitored(readId, writeId, schema, 0, module);
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
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/unlock/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/andModule/{module}")
    public void unlockMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId, @PathParam("module") String module) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.unlockMonitored(readId, writeId, schema, partitionId, module);
    }

    /**
     * Perform an initial migration to the specified version
     * 
     * @param ctxId The context identifier
     * @param toVersion Version updating to
     * @param module The module name
     * @throws OXException If the operation fails
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/{ctxId}/to/{toVersion}/forModule/{module}")
    public void initialiMigration(@PathParam("ctxId") int ctxId, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        migrate(ctxId, "", toVersion, module);
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
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/{ctxId}/from/{fromVersion}/to/{toVersion}/forModule/{module}")
    public void migrate(@PathParam("ctxId") int ctxId, @PathParam("fromVersion") String fromVersion, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.migrate(ctxId, fromVersion, toVersion, module);
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
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/from/{fromVersion}/to/{toVersion}/forModule/{module}")
    public void migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId, @PathParam("fromVersion") String fromVersion, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.migrateMonitored(readId, writeId, schema, partitionId, fromVersion, toVersion, module);
    }

    /**
     * Migrate from the specified version to the specified version by using a monitored connection
     * 
     * @param readId The read identifier
     * @param writeId The write identifier
     * @param schema The name of the schema
     * @param toVersion Version updating to
     * @param module The module name
     * @throws OXException If the operation fails
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/to/{toVersion}/forModule/{module}")
    public void migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        migrateMonitored(readId, writeId, schema, 0, "", toVersion, module);
    }

    /**
     * Migrate from the specified version to the specified version by using a monitored connection
     * 
     * @param readId The read identifier
     * @param writeId The write identifier
     * @param schema The name of the schema
     * @param partitionId The partition identifier
     * @param toVersion Version updating to
     * @param module The module name
     * @throws OXException If the operation fails
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/to/{toVersion}/forModule/{module}")
    public void migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        migrateMonitored(readId, writeId, schema, partitionId, "", toVersion, module);
    }

    /**
     * Migrate from the specified version to the specified version by using a monitored connection
     * 
     * @param readId The read identifier
     * @param writeId The write identifier
     * @param schema The name of the schema
     * @param fromVersion Version updating from
     * @param toVersion Version updating to
     * @param module The module name
     * @throws OXException If the operation fails
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/from/{fromVersion}/to/{toVersion}/forModule/{module}")
    public void migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("fromVersion") String fromVersion, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        DatabaseRESTPerformer performer = new DatabaseRESTPerformer(getAJAXRequestData());
        performer.migrateMonitored(readId, writeId, schema, 0, fromVersion, toVersion, module);
    }

}
