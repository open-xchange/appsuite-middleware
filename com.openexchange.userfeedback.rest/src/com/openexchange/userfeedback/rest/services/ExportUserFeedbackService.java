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
import com.openexchange.userfeedback.ExportResult;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.ExportType;
import com.openexchange.userfeedback.FeedbackService;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
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
