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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.userfeedback.mail.FeedbackMailService;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;


/**
 * {@link SendMailService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.4
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/userfeedback/v1/mail")
public class SendMailService extends JAXRSService {

    public SendMailService(ServiceLookup services) {
        super(services);
    }

    @POST
    @Path("/{context-group}/{type}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response sendMail(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup,
        @QueryParam("subject") String subject, @QueryParam("body") String body, String recipients) {
        return sendMail(start, end, type, contextGroup, subject, body, "false", recipients);
    }

    @POST
    @Path("/{context-group}/{type}/pgp")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response sendMail(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup,
        @QueryParam("subject") String subject, @QueryParam("body") String body, @QueryParam("usePgp") String usePgp, String recipients) {

        boolean pgp = Boolean.parseBoolean(usePgp);
        if (pgp) {
            return Response.status(501).build();
        } else {
            ResponseBuilder builder = null;
            try {
                if (null == subject || Strings.isEmpty(subject)) {
                    StringBuilder sb = new StringBuilder();
                    SimpleDateFormat df = new SimpleDateFormat();
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    sb.append("User Feedback Report: ").append(df.format(new Date(TimeUnit.SECONDS.toMillis(start)))).append(" to ").append(df.format(new Date(TimeUnit.SECONDS.toMillis(end))));
                    subject = sb.toString();
                }
                if (null == body) {
                    body = "";
                }
                FeedbackMailService service = getService(FeedbackMailService.class);
                FeedbackMailFilter filter = new FeedbackMailFilter(contextGroup, parseCSV(recipients), subject, body, start, end, type);
                String response = service.sendFeedbackMail(filter);
                builder = Response.status(200);
                if (Strings.isEmpty(response)) {
                    return builder.build();
                }
                builder.entity(response);
                builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM + "; charset=utf-8");
                return builder.build();
            } catch (Exception e) {
                builder = Response.status(400).entity(recipients + " is not a valid file.");
                return builder.build();
            }
        }
    }

    private Map<String, String> parseCSV(String csv) throws Exception {
        String[] parsed = csv.split(",");
        if (parsed.length % 2 != 0) {
            throw new Exception();
        }
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < parsed.length; i++) {
            result.put(parsed[i], parsed[++i]);
        }
        return result;
    }

}
