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
 *    trademarks of the OX Software GmbH group of companies.
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
            public Long start() {
                return Long.valueOf(start <= 0 ? Long.MIN_VALUE : start);
            }

            @Override
            public String getType() {
                return null == type || Strings.isEmpty(type) ? "star-rating-v1" : type;
            }

            @Override
            public Long end() {
                return Long.valueOf(end <= 0 ? Long.MAX_VALUE : end);
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
