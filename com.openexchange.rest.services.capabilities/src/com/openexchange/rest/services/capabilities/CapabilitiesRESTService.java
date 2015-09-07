
package com.openexchange.rest.services.capabilities;

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
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * The {@link CapabilitiesRESTService} - Allows clients to retrieve capabilities for arbitrary users
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@Path("/capabilities/v1/")
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

        try {
            CapabilitySet capabilities = capService.getCapabilities(user, context);
            return CapabilitiesJsonWriter.toJson(capabilities.asSet());
        } catch (JSONException e) {
            throw CapabilityExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
