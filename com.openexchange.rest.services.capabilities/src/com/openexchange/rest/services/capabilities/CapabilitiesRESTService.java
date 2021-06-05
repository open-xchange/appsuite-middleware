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

package com.openexchange.rest.services.capabilities;

import static com.openexchange.java.Autoboxing.I;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.capabilities.CapabilityExceptionCodes;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.json.CapabilitiesJsonWriter;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * The {@link CapabilitiesRESTService} allows clients to retrieve capabilities for arbitrary users.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @since v7.6.2
 */
@Path("/preliminary/capabilities/v1/")
@RoleAllowed(Role.BASIC_AUTHENTICATED)
public class CapabilitiesRESTService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CapabilitiesRESTService}.
     */
    public CapabilitiesRESTService(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * <pre>
     * GET /preliminary/capabilities/v1/all/{context}/{user}
     * </pre>
     */
    @GET
    @Path("/all/{context}/{user}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray all(@PathParam("context") int context, @PathParam("user") int user) throws OXException {
        CapabilityService capService = services.getOptionalService(CapabilityService.class);
        if (null == capService) {
            throw ServiceExceptionCode.absentService(CapabilityService.class);
        }

        if (context <= 0) {
            throw CapabilityExceptionCodes.INVALID_CONTEXT.create(I(context));
        }
        if (user <= 0) {
            throw CapabilityExceptionCodes.INVALID_USER.create(I(user));
        }

        try {
            CapabilitySet capabilities = capService.getCapabilities(user, context);
            return CapabilitiesJsonWriter.toJson(capabilities.asSet());
        } catch (JSONException e) {
            throw CapabilityExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }
}
