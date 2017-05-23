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
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceLookup;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
import com.openexchange.userfeedback.mail.FeedbackMailService;
import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;

/**
 * {@link SendUserFeedbackService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.4
 */
@RoleAllowed(Role.BASIC_AUTHENTICATED)
@Path("/userfeedback/v1/send")
public class SendUserFeedbackService extends AbstractUserFeedbackService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SendUserFeedbackService.class);

    public SendUserFeedbackService(ServiceLookup services) {
        super(services);
    }

    @POST
    @Path("/{context-group}/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMail(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup, String json) {
        JSONObject requestBody = null;
        String subject = null;
        String body = null;
        boolean compress = false;
        Map<String, String> recipients = null;
        Map<String, String> pgpKeys = null;
        try {
            validateParams(start, end);
            requestBody = new JSONObject(json);
            if (requestBody.hasAndNotNull("subject")) {
                subject = requestBody.getString("subject");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("User Feedback Report");
                if (start > 0L || end > 0L) {
                    SimpleDateFormat df = new SimpleDateFormat();
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    if (start > 0L) {
                        sb.append(" from ").append(df.format(new Date(start)));
                    }
                    if (end > 0L) {
                        sb.append(" to ").append(df.format(new Date(end)));
                    }
                }
                subject = sb.toString();
            }
            if (requestBody.hasAndNotNull("body")) {
                body = requestBody.getString("body");
            } else {
                body = "";
            }
            if (requestBody.hasAndNotNull("compress")) {
                compress = requestBody.getBoolean("compress");
            }
            JSONArray array = requestBody.getJSONArray("recipients");
            recipients = new HashMap<>(array.length());
            pgpKeys = new HashMap<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String address = object.getString("address");
                String displayName = "";
                if (object.hasAndNotNull("displayName")) {
                    displayName = object.getString("displayName");
                }
                if (object.hasAndNotNull("pgp_key")) {
                    String pgpKey = object.optString("pgp_key");
                    pgpKeys.put(address, pgpKey);
                }
                recipients.put(address, displayName);
            }
            return send(contextGroup, type, start, end, subject, body, compress, recipients, pgpKeys);
        } catch (JSONException e) {
            ResponseBuilder builder = Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON);
            builder.entity(e.getMessage());
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
            } else if (FeedbackExceptionCodes.INVALID_EMAIL_ADDRESSES.equals(e)) {
                ResponseBuilder builder = Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            } else if (FeedbackExceptionCodes.INVALID_EMAIL_ADDRESSES_PGP.equals(e)) {
                ResponseBuilder builder = Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            } else if (FeedbackExceptionCodes.INVALID_SMTP_CONFIGURATION.equals(e)) {
                LOG.error(DEFAULT_CONFIG_ERROR_MESSAGE, e);
                ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            } else if (FeedbackExceptionCodes.INVALID_PGP_CONFIGURATION.equals(e)) {
                LOG.error(DEFAULT_CONFIG_ERROR_MESSAGE, e);
                ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON);
                builder.entity(errorJson);
                return builder.build();
            }
            ResponseBuilder builder = Response.status(Status.NOT_FOUND).type(MediaType.APPLICATION_JSON);
            builder.entity(errorJson);
            return builder.build();
        }
    }

    private Response send(String contextGroup, String type, long start, long end, String subject, String mailBody, boolean compress, Map<String, String> recipients, Map<String, String> pgpKeys) throws OXException {
        ResponseBuilder builder = null;
        FeedbackMailService service = getService(FeedbackMailService.class);
        FeedbackMailFilter filter = new FeedbackMailFilter(contextGroup, recipients, pgpKeys, subject, mailBody, start, end, type, compress);
        String response = service.sendFeedbackMail(filter);
        builder = Response.status(Status.OK);
        if (Strings.isEmpty(response)) {
            return builder.build();
        }
        builder.entity(response);
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).type(MediaType.TEXT_PLAIN_TYPE);
        return builder.build();
    }
}
