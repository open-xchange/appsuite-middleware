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

package com.openexchange.health.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.openexchange.health.MWHealthCheckResult;
import com.openexchange.health.MWHealthCheckService;
import com.openexchange.health.MWHealthState;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;


/**
 * {@link MWHealthCheckReadynessEndpoint}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v8.0.0
 */
@Path("/ready")
@RoleAllowed(Role.INDIVIDUAL_BASIC_AUTHENTICATED)
public class MWHealthCheckReadynessEndpoint extends MWHealthAbstractEndpoint {

    /**
     * Initializes a new {@link MWHealthCheckReadynessEndpoint}.
     *
     * @param services The {@link ServiceLookup}
     */
    public MWHealthCheckReadynessEndpoint(ServiceLookup services) {
        super(services);
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response ready() {
        MWHealthCheckService service = services.getService(MWHealthCheckService.class);
        MWHealthCheckResult result = null;
        if (null == service) {
            MWHealthCheckService.LOG.error("Health Status: DOWN (MWHealthCheckService is unavailable)");
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        try {
            result = service.check();
        } catch (RuntimeException e) {
            MWHealthCheckService.LOG.error("Health Status: DOWN ({})", e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        MWHealthState status = result.getStatus();
        if (MWHealthState.DOWN.equals(status)) {
            MWHealthCheckService.LOG.debug("Health Status: DOWN, call /health for more information");
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
        MWHealthCheckService.LOG.debug("Health Status: UP");
        return Response.status(Status.OK).build();
    }

}
