
package com.openexchange.rest.services.html;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link HtmlRESTService} allows clients to process HTML content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Path("/preliminary/htmlproc/v1/")
public class HtmlRESTService {

    private final HtmlService htmlService;

    public HtmlRESTService(HtmlService htmlService) {
        super();
        this.htmlService = htmlService;
    }

    /**
     * <pre>
     * PUT /rest/htmlproc/v1/sanitize
     * &lt;HTML-content&gt;
     * </pre>
     *
     * Retrieves the sanitized version of passed content.<br>
     */
    @PUT
    @Path("/sanitize")
    @Consumes("text/*")
    @Produces(MediaType.TEXT_HTML)
    public String getSanitizedHtmlString(String data) throws OXException {
        return htmlService.sanitize(data, null, true, new boolean[1], null);
    }

    /**
     * <pre>
     * PUT /rest/htmlproc/v1/sanitize
     * &lt;HTML-content&gt;
     * </pre>
     *
     * Retrieves the sanitized version of passed content.<br>
     */
    @PUT
    @Path("/sanitize")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getSanitizedHtmlJSON(JSONObject data) throws OXException {
        if (data == null || data.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        try {
            final String sanitized = htmlService.sanitize(data.getString("content"), null, true, new boolean[1], null);
            return new JSONObject(2).put("content", sanitized);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * <pre>
     * PUT /rest/htmlproc/v1/sanitizeKeepImages
     * &lt;HTML-content&gt;
     * </pre>
     *
     * Retrieves the sanitized version of passed content.<br>
     */
    @PUT
    @Path("/sanitizeKeepImages")
    @Consumes("text/*")
    @Produces(MediaType.TEXT_HTML)
    public String getSanitizedHtmlWithoutExternalImages(String data) throws OXException {
        return htmlService.sanitize(data, null, false, new boolean[1], null);
    }

    /**
     * <pre>
     * PUT /rest/htmlproc/v1/sanitizeKeepImages
     * &lt;HTML-content&gt;
     * </pre>
     *
     * Retrieves the sanitized version of passed content.<br>
     */
    @PUT
    @Path("/sanitizeKeepImages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getSanitizedHtmlWithoutExternalImages(JSONObject data) throws OXException {
        if (data == null || data.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        try {
            final String sanitized = htmlService.sanitize(data.getString("content"), null, false, new boolean[1], null);
            return new JSONObject(2).put("content", sanitized);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
