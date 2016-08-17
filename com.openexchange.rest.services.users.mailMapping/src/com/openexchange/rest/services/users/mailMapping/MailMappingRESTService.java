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

package com.openexchange.rest.services.users.mailMapping;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.MultipleMailResolver;
import com.openexchange.mailmapping.ResolvedMail;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.user.UserService;

/**
 * {@link MailMappingRESTService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.0
 */
@Path("/preliminary/utilities/mailResolver/v1")
@RoleAllowed(Role.BASIC_AUTHENTICATED)
public class MailMappingRESTService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailMappingRESTService}.
     */
    public MailMappingRESTService(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * GET /rest/utilities/mailResolver/resolve/[mail1];[mail2]
     *
     * Tries to resolve the given mail addresses to context and user IDs.
     * Returns an Object with the mail addresses as keys and objects with the keys "user" (for the userId) and "context" (for the contextId) as values.
     * If a mail address is unknown, the corresponding key will be missing in the response.
     */
    @GET
    @Path("/resolve/{mail}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject resolve(@PathParam("mail") PathSegment mail) throws OXException {
        // Get E-Mail addresses
        Set<String> mails = new LinkedHashSet<String>(8);
        mails.add(mail.getPath());
        mails.addAll(mail.getMatrixParameters().keySet());

        // Check required service
        MailResolver resolver = services.getService(MailResolver.class);
        if (null == resolver) {
            throw ServiceExceptionCode.absentService(MailResolver.class);
        }

        // Check required service
        UserService users = services.getService(UserService.class);
        if (null == users) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }

        // Resolve them...
        try {
            int size = mails.size();
            JSONObject response = new JSONObject(size);

            if (resolver instanceof MultipleMailResolver) {
                MultipleMailResolver multipleMailResolver = (MultipleMailResolver) resolver;

                String[] sMails = mails.toArray(new String[size]);
                ResolvedMail[] resolveds = multipleMailResolver.resolveMultiple(sMails);
                for (int i = 0; i < resolveds.length; i++) {
                    ResolvedMail resolved = resolveds[i];
                    if (resolved != null) {
                        User user = users.getUser(resolved.getUserID(), resolved.getContextID());
                        response.put(sMails[i], asJson(resolved, user));
                    }
                }
            } else {
                for (String m : mails) {
                    ResolvedMail resolved = resolver.resolve(m);
                    if (resolved != null) {
                        User user = users.getUser(resolved.getUserID(), resolved.getContextID());
                        response.put(m, asJson(resolved, user));
                    }
                }
            }

            return response;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage());
        }
    }

    private JSONObject asJson(ResolvedMail resolved, User user) throws JSONException {
        JSONObject resolvedJson = new JSONObject(4);
        resolvedJson.put("uid", resolved.getUserID());
        resolvedJson.put("cid", resolved.getContextID());
        resolvedJson.put("user", new JSONObject(4).put("language", user.getPreferredLanguage()).put("displayName", user.getDisplayName()));
        return resolvedJson;
    }

}
