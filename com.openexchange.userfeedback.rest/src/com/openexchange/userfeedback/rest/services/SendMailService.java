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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
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

    @GET
    @Path("/{context-group}/{type}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM + ";charset=utf-8")
    public Response sendMail(@QueryParam("start") final long start, @QueryParam("end") final long end, @PathParam("type") final String type, @PathParam("context-group") final String contextGroup, @QueryParam("recipients") final String recipients) {
        ResponseBuilder builder = null;
        if (null == recipients || Strings.isEmpty(recipients)) {
            builder = Response.status(400).entity("Add recipients as comma-separated list");
            builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM + "; charset=utf-8");
            return builder.build();
        }
        Map<String, String> recipientsMap = new HashMap<>();
        InternetAddress[] addresses = null;
        try {
            addresses = QuotedInternetAddress.parse(recipients);
        } catch (AddressException e) {
            builder = Response.status(400).entity(e.getMessage());
            builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM + "; charset=utf-8");
            return builder.build();
        }
        for (InternetAddress address : addresses) {
            recipientsMap.put(address.getAddress(), address.getPersonal());
        }
        FeedbackMailService service = getService(FeedbackMailService.class);
        StringBuilder body = new StringBuilder().append("User feedback from ").append(new Date(start).toString()).append(" to ").append(new Date(end).toString()).append(".");
        FeedbackMailFilter filter = new FeedbackMailFilter(contextGroup, recipientsMap, "User Feedback", body.toString(), start, end, type);
        String response = service.sendFeedbackMail(filter);
        builder = Response.status(200);
        if (Strings.isEmpty(response)) {
            return builder.build();
        }
        builder.entity(response);
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM + "; charset=utf-8");
        return builder.build();
    }

}
