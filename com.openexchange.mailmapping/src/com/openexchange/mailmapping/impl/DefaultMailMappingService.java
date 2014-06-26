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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mailmapping.impl;

import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.ResolvedMail;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;


/**
 * The {@link DefaultMailMappingService} tries to resolve an email address by consulting the loginmappings. The loginmapping of a context is supposed to point
 * to the domain name for an email address, while the mail address of the user should be the primary mail address or one of the aliases.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Simple code clean-up
 */
public class DefaultMailMappingService implements MailResolver {

    /** The service look-up */
    private final ServiceLookup services;

    /**
     * Initializes a new {@link DefaultMailMappingService}.
     *
     * @param mailMappingActivator
     */
     public DefaultMailMappingService(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public ResolvedMail resolve(String mail) throws OXException {
        if (Strings.isEmpty(mail)) {
            return null;
        }

        int atSign = mail.lastIndexOf('@');
        if (atSign <= 0 || atSign >= mail.length()) {
            // Does not seem to be a valid E-Mail address
            return null;
        }

        // Extract domain
        String domain = mail.substring(atSign + 1);

        // Acquire needed services
        ContextService contexts = services.getService(ContextService.class);
        if (null == contexts) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }
        UserService users = services.getService(UserService.class);
        if (null == users) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }

        // Map the domain name to a context id
        int cid = contexts.getContextId(domain);
        if (cid <= 0) {
            return null;
        }
        Context context = contexts.getContext(cid);
        // Search for a user with the mail address
        User found = users.searchUser(mail, context, true);
        if (found == null) {
            return null;
        }

        return new ResolvedMail(found.getId(), context.getContextId());
    }

}
