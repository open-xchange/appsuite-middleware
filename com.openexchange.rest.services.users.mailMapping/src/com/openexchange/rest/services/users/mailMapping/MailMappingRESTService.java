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
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.MailResolverService;
import com.openexchange.mailmapping.ResolvedMail;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.user.User;
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
        MailResolverService resolver = services.getService(MailResolverService.class);
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

            String[] sMails = mails.toArray(new String[size]);
            ResolvedMail[] resolveds = resolver.resolveMultiple(sMails);
            for (int i = 0; i < resolveds.length; i++) {
                ResolvedMail resolved = resolveds[i];
                if (resolved != null) {
                    User user = users.getUser(resolved.getUserID(), resolved.getContextID());
                    response.put(sMails[i], asJson(resolved, user));
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
