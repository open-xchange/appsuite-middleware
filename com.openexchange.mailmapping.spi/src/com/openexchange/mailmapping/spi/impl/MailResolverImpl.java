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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.mailmapping.spi.impl;

import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.ResolveReply;
import com.openexchange.mailmapping.ResolvedMail;
import com.openexchange.mailmapping.spi.ContextResolver;
import com.openexchange.mailmapping.spi.ResolvedContext;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;


/**
 * {@link MailResolverImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MailResolverImpl implements MailResolver {

    private final ServiceLookup services;
    private final ServiceListing<ContextResolver> spis;

    /**
     * Initializes a new {@link MailResolverImpl}.
     */
    public MailResolverImpl(ServiceListing<ContextResolver> spis, ServiceLookup services) {
        super();
        this.spis = spis;
        this.services = services;
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

        for (ContextResolver spi : spis.getServiceList()) {
            ResolvedContext resolved = spi.resolveContext(mail);
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
