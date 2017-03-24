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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.filter.FeedbackFilter;


/**
 * {@link DeleteUserFeedbackService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.4
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/userfeedback/v1/delete")
public class DeleteUserFeedbackService extends JAXRSService {

    public DeleteUserFeedbackService(ServiceLookup services) {
        super(services);
    }

    @DELETE
    @Path("/{context-group}/{type}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup) {
        FeedbackService service = getService(FeedbackService.class);
        FeedbackFilter filter = new FeedbackFilter() {

            @Override
            public Long start() {
                if (start <= 0) {
                    return Long.MIN_VALUE;
                }
                return start;
            }

            @Override
            public String getType() {
                if (null == type || Strings.isEmpty(type)) {
                    return "star-rating-v1";
                }
                return type;
            }

            @Override
            public Long end() {
                if (end <= 0) {
                    return Long.MAX_VALUE;
                }
                return end;
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
            service.delete(contextGroup, filter);
            ResponseBuilder builder = Response.ok();
            builder.entity(new GenericEntity<String>(getPositiveRespone(filter, contextGroup), String.class));
            resp = builder.build();
        } catch (OXException e) {
            resp = Response.serverError().entity(e.getMessage()).build();
        }
        return resp;
    }

    private String getPositiveRespone(FeedbackFilter filter, String contextGroup) {
        String result = "Feedback data deleted for type: " + filter.getType() + " ,context group: " + contextGroup + (filter.start() != Long.MIN_VALUE ? (" ,start time: " + filter.start()) : "" ) + (filter.end() != Long.MAX_VALUE ? (" ,end time: " + filter.end()) : "");
        return result;
    }

}
