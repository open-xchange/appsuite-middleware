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

package com.openexchange.mailmapping.spi.impl;

import java.util.List;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.mailmapping.MultipleMailResolver;
import com.openexchange.mailmapping.ResolveReply;
import com.openexchange.mailmapping.ResolvedMail;
import com.openexchange.mailmapping.spi.ContextResolver;
import com.openexchange.mailmapping.spi.ResolvedContext;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.UserService;


/**
 * {@link MailResolverImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MailResolverImpl implements MultipleMailResolver {

    private final ServiceLookup services;
    private final ServiceListing<ContextResolver> contextResolvers;

    /**
     * Initializes a new {@link MailResolverImpl}.
     */
    public MailResolverImpl(ServiceListing<ContextResolver> contextResolvers, ServiceLookup services) {
        super();
        this.contextResolvers = contextResolvers;
        this.services = services;
    }

    @Override
    public ResolvedMail[] resolveMultiple(String... mails) throws OXException {
        if (null == mails || mails.length == 0) {
            return new ResolvedMail[0];
        }

        // Acquire needed services
        ContextService contexts = services.getService(ContextService.class);
        if (null == contexts) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }
        UserService users = services.getService(UserService.class);
        if (null == users) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }
        List<ContextResolver> contextResolvers = this.contextResolvers.getServiceList();


        ResolvedMail[] results = new ResolvedMail[mails.length];
        for (int i = results.length; i-- > 0;) {
            results[i] = resolve(mails[i], contextResolvers, users, contexts);
        }
        return results;
    }

    @Override
    public ResolvedMail resolve(String mail) throws OXException {
        if (Strings.isEmpty(mail)) {
            return null;
        }

        // Acquire needed services
        ContextService contexts = services.getService(ContextService.class);
        if (null == contexts) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }
        UserService users = services.getService(UserService.class);
        if (null == users) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }

        return resolve(mail, contextResolvers.getServiceList(), users, contexts);
    }

    private ResolvedMail resolve(String mail, List<ContextResolver> contextResolvers, UserService users, ContextService contexts) throws OXException {
        for (ContextResolver contextResolver : contextResolvers) {
            ResolvedContext resolved = contextResolver.resolveContext(mail);
            if (resolved != null) {
                ResolveReply reply = resolved.getResolveReply();
                if (ResolveReply.ACCEPT.equals(reply)) {
                    // Return resolved instance
                    return lookUpInContext(mail, resolved.getContextID(), users, contexts);
                }
                if (ResolveReply.DENY.equals(reply)) {
                    // No further processing allowed
                    return null;
                }
                // Otherwise NEUTRAL reply; next in chain
            }
        }
        return null;
    }

    private ResolvedMail lookUpInContext(String mail, int contextId, UserService users, ContextService contexts) throws OXException {
        Context context = contexts.getContext(contextId);

        User found;
        try {
            found = users.searchUser(mail, context, true);
        } catch (OXException e) {
            if (!e.equalsCode(14, "USR")) {
                throw e;
            }
            found = null;
        }
        if (found == null) {
            return null;
        }

        return ResolvedMail.ACCEPT(found.getId(), contextId);
    }

}
