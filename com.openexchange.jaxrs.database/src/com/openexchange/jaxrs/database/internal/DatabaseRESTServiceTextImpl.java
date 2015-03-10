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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DatabaseRESTServiceTextImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@Path("/database/v1")
public class DatabaseRESTServiceTextImpl extends AbstractDatabaseRESTService {

    /**
     * Initializes a new {@link DatabaseRESTServiceTextImpl}.
     * 
     * @param services
     */
    public DatabaseRESTServiceTextImpl(ServiceLookup services) {
        super(services);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configdb/readOnly")
    public Response queryConfigDB() throws OXException {
        return performQueryConfigDB();
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configdb/writable")
    public Response updateConfigDB() throws OXException {
        return performUpdateConfigDB();
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/oxdb/{ctxId}/readOnly")
    public Response queryOXDB(@PathParam("ctxId") int ctxId) throws OXException {
        return performQueryOXDB(ctxId);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/oxdb/{ctxId}/writable")
    public Response updateOXDB(@PathParam("ctxId") int ctxId) throws OXException {
        return performUpdateOXDB(ctxId);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/transaction/{transactionId}")
    public Response queryTransaction(@PathParam("transactionId") String txId) throws OXException {
        return performQueryTransaction(txId);
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/transaction/{transactionId}/rollback")
    public void rollbackTransaction(@PathParam("transactionId") String txId) throws OXException {
        performRollbackTransaction(txId);
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/transaction/{transactionId}/commit")
    public void commitTransaction(@PathParam("transactionId") String txId) throws OXException {
        performCommitTransaction(txId);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/readOnly")
    public Response queryInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId) throws OXException {
        return performQueryInMonitoredConnection(readId, writeId, schema, partitionId);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/readOnly")
    public Response queryInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException {
        return performQueryInMonitoredConnection(readId, writeId, schema, 0);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/writable")
    public Response updateInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId) throws OXException {
        return performUpdateInMonitoredConnection(readId, writeId, schema, partitionId);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pool/r/{readId}/w/{writeId}/{schema}/writable")
    public Response updateInMonitoredConnection(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException {
        return performUpdateInMonitoredConnection(readId, writeId, schema, 0);
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/init/w/{writeId}/{schema}")
    public void initSchema(@PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException {
        performInitSchema(writeId, schema);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/pool/w/{writeId}/{schema}/partitions")
    public void insertPartitionIds(@PathParam("writeId") int writeId, @PathParam("schema") String schema) throws OXException {
        performInsertPartitionIds(writeId, schema);
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/unlock/for/{ctxId}/andModule/{module}")
    public void unlock(@PathParam("ctxId") int ctxId, @PathParam("module") String module) throws OXException {
        performUnlock(ctxId, module);
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/unlock/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/andModule/{module}")
    public void unlockMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId, @PathParam("module") String module) throws OXException {
        performUnlockMonitored(readId, writeId, schema, partitionId, module);
    }
    
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/unlock/pool/r/{readId}/w/{writeId}/{schema}/andModule/{module}")
    public void unlockMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("module") String module) throws OXException {
        performUnlockMonitored(readId, writeId, schema, 0, module);
    }
    
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/{ctxId}/from/{fromVersion}/to/{toVersion}/forModule/{module}")
    public Response migrate(@PathParam("ctxId") int ctxId, @PathParam("fromVersion") String fromVersion, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        return performMigrate(ctxId, fromVersion, toVersion, module);
    }
    
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/{ctxId}/to/{toVersion}/forModule/{module}")
    public Response initialiMigration(@PathParam("ctxId") int ctxId, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        return performMigrate(ctxId, "", toVersion, module);
    }
    
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/from/{fromVersion}/to/{toVersion}/forModule/{module}")
    public Response migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId, @PathParam("fromVersion") String fromVersion, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        return performMigrateMonitored(readId, writeId, schema, partitionId, fromVersion, toVersion, module);
    }
    
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/to/{toVersion}/forModule/{module}")
    public Response migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        return performMigrateMonitored(readId, writeId, schema, 0, "", toVersion, module);
    }
    
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/{partitionId}/to/{toVersion}/forModule/{module}")
    public Response migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("partitionId") int partitionId, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        return performMigrateMonitored(readId, writeId, schema, partitionId, "", toVersion, module);
    }
    
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/migration/for/pool/r/{readId}/w/{writeId}/{schema}/from/{fromVersion}/to/{toVersion}/forModule/{module}")
    public Response migrateMonitored(@PathParam("readId") int readId, @PathParam("writeId") int writeId, @PathParam("schema") String schema, @PathParam("fromVersion") String fromVersion, @PathParam("toVersion") String toVersion, @PathParam("module") String module) throws OXException {
        return performMigrateMonitored(readId, writeId, schema, 0, fromVersion, toVersion, module);
    }
}
