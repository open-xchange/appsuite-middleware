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
