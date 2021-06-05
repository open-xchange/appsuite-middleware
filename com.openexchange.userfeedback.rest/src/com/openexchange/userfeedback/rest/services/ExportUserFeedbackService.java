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

package com.openexchange.userfeedback.rest.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
import com.openexchange.userfeedback.export.ExportResult;
import com.openexchange.userfeedback.export.ExportResultConverter;
import com.openexchange.userfeedback.export.ExportType;
import com.openexchange.userfeedback.filter.DateOnlyFilter;

/**
 *
 * {@link ExportUserFeedbackService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/userfeedback/v1/export")
public class ExportUserFeedbackService extends AbstractUserFeedbackService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExportUserFeedbackService.class);

    public ExportUserFeedbackService(ServiceLookup services) {
        super(services);
    }

    @GET
    @Path("/{context-group}/{type}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public Response export(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup, @QueryParam("delimiter") final String delimiter) {
        Map<String, String> config = new HashMap<>();
        if (Strings.isNotEmpty(delimiter)) {
            config.put("delimiter", delimiter);
        }
        Response response = export(start, end, type, contextGroup, ExportType.CSV, config);
        return response;
    }

    @GET
    @Path("/{context-group}/{type}/raw")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportRaw(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup) {
        Response response = export(start, end, type, contextGroup, ExportType.RAW);
        return response;
    }

    private Response export(final long start, final long end, final String type, final String contextGroup, ExportType exportType) {
        return export(start, end, type, contextGroup, exportType, Collections.<String, String> emptyMap());
    }

    private Response export(final long start, final long end, final String type, final String contextGroup, ExportType exportType, Map<String, String> configuration) {
        FeedbackService feedbackService = this.getService(FeedbackService.class);

        try {
            validateParams(start, end);
            ExportResultConverter converter = feedbackService.export(contextGroup, new DateOnlyFilter(type, start, end), configuration);
            ExportResult export = converter.get(exportType);
            ResponseBuilder builder = Response.ok(export.getResult(), new MediaType(exportType.getMediaType(), exportType.getMediaSubType()));
            builder.encoding("UTF-8");
            return builder.build();
        } catch (OXException e) {
            JSONObject errorJson = generateError(e);
            if (FeedbackExceptionCodes.GLOBAL_DB_NOT_CONFIGURED.equals(e)) {
                LOG.error(DEFAULT_CONFIG_ERROR_MESSAGE, e);
                ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            } else if (FeedbackExceptionCodes.INVALID_PARAMETER_VALUE.equals(e)) {
                ResponseBuilder builder = Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            }
            ResponseBuilder builder = Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON);
            builder.entity(errorJson);
            return builder.build();
        }
    }
}
