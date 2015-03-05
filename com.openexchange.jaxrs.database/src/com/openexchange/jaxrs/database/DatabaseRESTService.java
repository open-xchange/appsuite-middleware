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
    @Path("/configdb/readOnly")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
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
    @Path("/configdb/writable")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
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
    @Path("/oxdb/{ctxId}/readOnly")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
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
    @Path("/oxdb/{ctxId}/writable")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
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
}
