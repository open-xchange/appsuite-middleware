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

package com.openexchange.mailmapping.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.mailmapping.MultipleMailResolver;
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
public class DefaultMailMappingService implements MultipleMailResolver {

    /** The service look-up */
    private final ServiceLookup services;

    /** Whether to look-up only by domain */
    private final boolean lookUpByDomain;

    /** The list of external domains */
    private final Set<String> externalDomains;

    /**
     * Initializes a new {@link DefaultMailMappingService}.
     *
     * @param mailMappingActivator
     */
    public DefaultMailMappingService(ServiceLookup services) {
        super();
        this.services = services;
        ConfigurationService service = services.getService(ConfigurationService.class);
        lookUpByDomain = service.getBoolProperty("com.openexchange.mailmapping.lookUpByDomain", false);
        Set<Object> set = service.getFile("external-domains.properties").keySet();
        this.externalDomains = new HashSet<String>(set.size(), 0.9f);
        for (Object domain : set) {
            externalDomains.add(domain.toString());
        }
    }

    @Override
    public ResolvedMail[] resolveMultiple(String... mails) throws OXException {
        if (null == mails || mails.length == 0) {
            return new ResolvedMail[0];
        }

        ResolvedMail[] results = new ResolvedMail[mails.length];
        for (int i = results.length; i-- > 0;) {
            results[i] = resolve(mails[i]);
        }
        return results;
    }

    @Override
    public ResolvedMail resolve(String mail) throws OXException {
        return resolve(mail, lookUpByDomain);
    }

    /**
     * Resolves specified E-Mail address
     *
     * @param mail The E-Mail address to resolve
     * @param lookUpByDomain Whether look-up should happen by domain (not recommended as it requires appropriate login mappings) or by DB schema (reliably, but slower)
     * @return The resolved E-Mail address or <code>null</code> if it could not be resolved
     * @throws OXException If resolve operation fails
     */
    public ResolvedMail resolve(String mail, boolean lookUpByDomain) throws OXException {
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

        // Check against list of known external domains
        {
            String test = Strings.asciiLowerCase(domain);
            if (externalDomains.contains(test)) {
                return null;
            }
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

        return lookUpByDomain ? lookUpByDomain(mail, domain, contexts, users) : lookUpBySchema(mail, domain, users, contexts);
    }

    private ResolvedMail lookUpByDomain(String mail, String domain, ContextService contexts, UserService users) throws OXException {
        // Map the domain name to a context identifier
        int cid = contexts.getContextId(domain);
        if (cid <= 0) {
            return null;
        }

        // Search for a user with the mail address in that context
        return lookUpInContext(mail, cid, users, contexts);
    }

    private ResolvedMail lookUpBySchema(String mail, String domain, UserService users, ContextService contexts) throws OXException {
        ResolvedMail resolvedMail = lookUpByDomain(mail, domain, contexts, users);
        if (null != resolvedMail) {
            return resolvedMail;
        }

        DatabaseService databaseService = services.getService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }

        List<Integer> contextIds = contexts.getAllContextIds();
        Set<Integer> visited = new HashSet<Integer>(contextIds.size(), 0.9f);
        for (Integer contextId : contextIds) {
            if (visited.add(contextId)) {
                // Search for a user with the mail address in that context's schema
                resolvedMail = lookUpInSchema(mail, contextId.intValue(), databaseService);
                if (null != resolvedMail) {
                    return resolvedMail;
                }

                // Discard other contexts in that schema
                int[] contextsInSameSchema = databaseService.getContextsInSameSchema(contextId.intValue());
                if (null != contextsInSameSchema) {
                    for (int i = contextsInSameSchema.length; i-- > 0;) {
                        visited.add(Integer.valueOf(contextsInSameSchema[i]));
                    }
                }
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

    private ResolvedMail lookUpInSchema(String mail, int idOfContextInSchema, DatabaseService dbService) throws OXException {
        Connection con = dbService.getReadOnly(idOfContextInSchema);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT cid, id FROM user WHERE mail LIKE ?");
            stmt.setString(1, mail);
            result = stmt.executeQuery();
            if (result.next()) {
                return ResolvedMail.ACCEPT(result.getInt(2), result.getInt(1));
            }

            Databases.closeSQLStuff(result, stmt);
            stmt = con.prepareStatement("SELECT cid, user FROM user_alias WHERE alias LIKE ?");
            stmt.setString(1, mail);
            result = stmt.executeQuery();
            if (result.next()) {
                return ResolvedMail.ACCEPT(result.getInt(2), result.getInt(1));
            }
            return null;
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("USR");
        } finally {
            Databases.closeSQLStuff(result, stmt);
            dbService.backReadOnly(idOfContextInSchema, con);
        }
    }

}
