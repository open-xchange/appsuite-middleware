
package com.openexchange.capabilities.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.capabilities.CapabilityExceptionCodes;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.internal.CapabilityServiceImpl;
import com.openexchange.exception.OXException;

/**
 * The {@link CapabilitiesRESTService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
@Path("/capabilities/v1/")
public class CapabilitiesRESTService {

    private final CapabilityServiceImpl capService;
    private final ResultConverter capabilitiesConverter;

    /**
     * Initializes a new {@link CapabilitiesRESTService}.
     */
    public CapabilitiesRESTService(CapabilityServiceImpl capService, ResultConverter capabilitiesConverter) {
        super();
        this.capService = capService;
        this.capabilitiesConverter = capabilitiesConverter;
    }

    /**
     * <pre>
     * GET /preliminary/capabilities/v1/all/{context}/{user}
     * &lt;JSON-content&gt;
     * </pre>
     */
    @GET
    @Path("/all/{context}/{user}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray all(@PathParam("context") int context, @PathParam("user") int user) throws OXException {
        try {
            CapabilitySet capabilities = capService.getCapabilities(user, context);
            AJAXRequestResult requestResult = new AJAXRequestResult(capabilities.asSet(), "capability");
            capabilitiesConverter.convert(null, requestResult, null/*ServerSessionAdapter.valueOf(user, context)*/, null);
            return (JSONArray) requestResult.getResultObject();
        } catch (RuntimeException e) {
            throw CapabilityExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
