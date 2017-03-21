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
import javax.ws.rs.core.Response.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.JAXRSService;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SendMailService.class);

    public SendMailService(ServiceLookup services) {
        super(services);
    }

    @POST
    @Path("/{context-group}/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response sendMail(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup,
        @QueryParam("subject") String subject, @QueryParam("body") String body, String json) {
        return send(contextGroup, type, start, end, subject, body, json, false);
    }

    @POST
    @Path("/{context-group}/{type}/pgp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response sendMail(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup,
        @QueryParam("subject") String subject, @QueryParam("body") String body, @QueryParam("usePgp") String usePgp, String json) {
        return send(contextGroup, type, start, end, subject, body, json, true);
    }

    private Response send(String contextGroup, String type, long start, long end, String subject, String mailBody, String json, boolean usePgp) {
        JSONArray array = null;
        try {
            array = new JSONArray(json);
            ResponseBuilder builder = null;
            if (null == subject || Strings.isEmpty(subject)) {
                StringBuilder sb = new StringBuilder();
                SimpleDateFormat df = new SimpleDateFormat();
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                sb.append("User Feedback Report: ").append(df.format(new Date(TimeUnit.SECONDS.toMillis(start)))).append(" to ").append(df.format(new Date(TimeUnit.SECONDS.toMillis(end))));
                subject = sb.toString();
            }
            if (null == mailBody) {
                mailBody = "";
            }
            Map<String, String> recipients = new HashMap<>(array.length());
            Map<String, String> pgpKeys = new HashMap<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String address = object.getString("address");
                String displayName = object.getString("displayName");
                String pgpKey = object.optString("pgpKey");
                recipients.put(address, displayName);
                pgpKeys.put(address, pgpKey);
            }
            FeedbackMailService service = getService(FeedbackMailService.class);
            FeedbackMailFilter filter = new FeedbackMailFilter(contextGroup, recipients, subject, mailBody, start, end, type);
            String response = service.sendFeedbackMail(filter);
            builder = Response.status(Status.OK);
            if (Strings.isEmpty(response)) {
                return builder.build();
            }
            builder.entity(response);
            builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM + "; charset=utf-8");
            return builder.build();
        } catch (JSONException e) {
            LOG.error(e.getMessage());
            ResponseBuilder builder = Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (OXException e) {
            LOG.error("An error occurred while retrieving user feedback.", e);
            JSONObject errorJson = generateError(e);
            if (e.similarTo(FeedbackExceptionCodes.GLOBAL_DB_NOT_CONFIGURED)) {
                ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            } else if (e.similarTo(FeedbackExceptionCodes.INVALID_PARAMETER_VALUE)) {
                ResponseBuilder builder = Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            } else if (e.similarTo(FeedbackExceptionCodes.INVALID_EMAIL_ADDRESSES)) {
                ResponseBuilder builder = Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            } else if (e.similarTo(FeedbackExceptionCodes.INVALID_SMTP_CONFIGURATION)) {
                ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            } 
            ResponseBuilder builder = Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON);
            builder.entity(errorJson);
            return builder.build();
        }
    }

    private JSONObject generateError(OXException ex) {
        JSONObject main = new JSONObject();
        try {
            ResponseWriter.addException(main, ex);
        } catch (JSONException e) {
            LOG.error("Error while generating error for client.", e);
        }
        return main;
    }

}
