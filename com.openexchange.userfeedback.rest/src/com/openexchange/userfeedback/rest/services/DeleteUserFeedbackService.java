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

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
import com.openexchange.userfeedback.filter.FeedbackFilter;

/**
 * {@link DeleteUserFeedbackService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.4
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/userfeedback/v1/")
public class DeleteUserFeedbackService extends AbstractUserFeedbackService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteUserFeedbackService.class);

    public DeleteUserFeedbackService(ServiceLookup services) {
        super(services);
    }

    @DELETE
    @Path("/{context-group}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup) {
        FeedbackService service = getService(FeedbackService.class);
        FeedbackFilter filter = new FeedbackFilter() {

            @Override
            public long start() {
                return start <= 0 ? Long.MIN_VALUE : start;
            }

            @Override
            public String getType() {
                return null == type || Strings.isEmpty(type) ? "star-rating-v1" : type;
            }

            @Override
            public long end() {
                return end <= 0 ? Long.MAX_VALUE : end;
            }

            @Override
            public boolean accept(FeedbackMetaData feedback) {
                if ((start == 0) && (end == 0)) {
                    return true;
                }
                long feedbackDate = feedback.getDate();
                if (start == 0) {
                    return feedbackDate < end;
                } else if (end == 0) {
                    return feedbackDate > start;
                } else {
                    return (feedbackDate < end) && (feedbackDate > start);
                }
            }
        };
        Response resp = null;
        try {
            validateParams(start, end);

            service.delete(contextGroup, filter);
            ResponseBuilder builder = Response.ok();
            JSONObject response = createResponse(filter, contextGroup);
            builder.entity(response);
            resp = builder.build();
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
        return resp;
    }

    private JSONObject createResponse(FeedbackFilter filter, String contextGroup) {
        JSONObject resp = new JSONObject();
        try {
            resp.put("successful", "true");
            resp.put("type", filter.getType());
            resp.put("contextGroup", contextGroup);
            if (filter.start() != Long.MIN_VALUE) {
                resp.put("start", filter.start());
            }
            if (filter.end() != Long.MAX_VALUE) {
                resp.put("end", filter.end());
            }
        } catch (JSONException e) {
            LOG.error("", e);
        }
        return resp;
    }
}
