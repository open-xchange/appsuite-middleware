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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.openexchange.exception.OXException;

/**
 * {@link DatabaseRESTService}. Defines the REST API for the Database Service.
 * 
 * The implemented methods MUST also be annotated accordingly. The annotation '@Consumes' must also be specified
 * on the implementation level.
 * 
 * Path: /database/v1
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@Path("/database/v1")
public interface DatabaseRESTService {

    /**
     * Performs a query to the 'configdb'.
     * 
     * @return A Response with the result set
     * @throws OXException If an error occurs
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configdb/readOnly")
    Response queryConfigDB() throws OXException;

    /**
     * Performs an update to the 'configdb'.
     * 
     * @return
     * @throws OXException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configdb/writable")
    Response updateConfigDB() throws OXException;

    /**
     * Performs a query to an OX database with the specified context identifier.
     * 
     * @param ctxId The context identifier
     * @return A Response with the result set
     * @throws OXException If an error occurs
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/oxdb/{ctxId}/readOnly")
    Response queryOXDB(@PathParam("ctxId") int ctxId) throws OXException;

    /**
     * Issues updates and inserts to an OX database with the specified context identifier. If multiple updates are
     * sent in a request to a writable database, they are considered to be part of one transaction. A transaction
     * is automatically committed after all requests have been sent. If an error occurs, the transaction is rolled
     * back and all changes are undone.
     * 
     * The transaction can be kept open at the end of an request to get a transaction that spans multiple requests.
     * This is useful, if, for example, a value is to be retrieved, do a computation on it, and then write it back.
     * A transaction will be kept open, if the URL parameter "keepOpen" with the parameter to "true" is set in the
     * request.
     * 
     * @param ctxId The context identifier
     * @return A Response with the outcome of the result (updated=1 or update=0)
     * @throws OXException If an error occurs
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/oxdb/{ctxId}/writable")
    Response updateOXDB(@PathParam("ctxId") int ctxId) throws OXException;

    /**
     * Uses the open transaction to execute further queries or updates.
     * 
     * @param txId The transaction identifier
     * @return
     * @throws OXException If an error occurs
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/transaction/{transactionId}")
    Response queryTransaction(@PathParam("transactionId") String txId) throws OXException;

    /**
     * Rolls back the transaction with the specified transaction identifier. Simply returns a 200 status code
     * when done. If a transaction can not be found (on all requests in the /transaction/ namespace), a 404
     * is returned. Transactions are automatically closed and rolled back, if they weren't contacted in a 2 minute
     * interval.
     * 
     * @param txId The transaction identifier
     * @throws OXException If an error occurs
     */
    @GET
    @Path("/transaction/{transactionId}/rollback")
    void rollbackTransaction(@PathParam("transactionId") String txId) throws OXException;

    /**
     * Commits the transaction with the specified transaction identifier. Simply returns a 200 status code
     * when done. If a transaction can not be found (on all requests in the /transaction/ namespace), a 404
     * is returned. Transactions are automatically closed and rolled back, if they weren't contacted in a 2 minute
     * interval.
     * 
     * @param txId The transaction identifier
     * @throws OXException If an error occurs
     */
    @GET
    @Path("/transaction/{transactionId}/commit")
    void commitTransaction(@PathParam("transactionId") String txId) throws OXException;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/readOnly")
    Response queryInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId) throws OXException;

    /**
     * Query a monitored connection
     * 
     * @param readId The read pool identifier
     * @param writeId The write pool identifier
     * @param schema The schema name
     * @throws OXException If the operation fails
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/readOnly")
    Response queryInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/writable")
    Response updateInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId) throws OXException;

    /**
     * Update a monitored connection
     * 
     * @param readId The read pool identifier
     * @param writeId The write pool identifier
     * @param schema The schema name
     * @throws OXException If the operation fails
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/writable")
    Response updateInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException;

    /**
     * Initialize a new database schema with the specified name and the specified write pool identifier.
     * 
     * @param writePoolId The write pool identifier
     * @param schema The schema name
     * @throws OXException If the initialization of the new schema fails
     */
    @GET
    @Path("/init/w/{writeId}/{schema}")
    void initSchema(@PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException;

    /**
     * Inserts the partition identifiers to the replication monitor table
     * 
     * @param writeId The write identifier referencing the master db server
     * @param schema The name of the schema
     * @throws OXException If the operation fails
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/w/{writeId}/{schema}/partitions")
    void insertPartitionIds(@PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException;

    /**
     * Unlocks a schema/module combination.
     * 
     * @param ctxId The context identifier
     * @param module The module
     * @throws OXException If the operation fails
     */
    @GET
    @Path("/unlock/for/{ctxId}/andModule/{module}")
    void unlock(@PathParam("ctxId") int ctxId, @PathParam("module") String module) throws OXException;

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
    @Path("/unlock/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/andModule/{module}")
    void unlockMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId, @PathParam("module") String module) throws OXException;

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
    @Path("/unlock/pool/r/{readId}/w/{writeId}/{schema}/andModule/{module}")
    void unlockMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("module") String module) throws OXException;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/{ctxId}/from/{fromVersion}/to/{toVersion}/forModule/{module}")
    Response migrate(@PathParam("ctxId") int ctxId, @PathParam("fromVersion") String fromVersion, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException;

    /**
     * Perform an initial migration to the specified version
     * 
     * @param ctxId The context identifier
     * @param toVersion Version updating to
     * @param module The module name
     * @throws OXException If the operation fails
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/{ctxId}/to/{toVersion}/forModule/{module}")
    Response initialiMigration(@PathParam("ctxId") int ctxId, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/from/{fromVersion}/to/{toVersion}/forModule/{module}")
    Response migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId, @PathParam("fromVersion") String fromVersion, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/to/{toVersion}/forModule/{module}")
    Response migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/to/{toVersion}/forModule/{module}")
    Response migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/from/{fromVersion}/to/{toVersion}/forModule/{module}")
    Response migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("fromVersion") String fromVersion, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException;
}
