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

package com.openexchange.rest.services.security;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.security.Principal;
import java.util.Arrays;
import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.tools.servlet.http.Authorization.Credentials;


/**
 * <p>The {@link AuthenticationFilter} sets the requests {@link SecurityContext}
 * based on the provided authentication information. Requests that arent't authenticated
 * are aborted.</p>
 * <br>
 * <p>Currently authentication is only based on HTTP basic authentication with the
 * credentials defined via 'com.openexchange.rest.services.basic-auth' in server.properties.</p>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    private final boolean doFail;
    private final String authLogin;
    private final String authPassword;

    public AuthenticationFilter(String authLogin, String authPassword) {
        super();
        if (Strings.isEmpty(authLogin) || Strings.isEmpty(authPassword)) {
            doFail = true;
            this.authLogin = null;
            this.authPassword = null;
        } else {
            doFail = false;
            this.authLogin = authLogin.trim();
            this.authPassword = authPassword.trim();
        }
    }

    private <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        A annotation = resourceInfo.getResourceMethod().getAnnotation(annotationClass);
        if (null != annotation) {
            return annotation;
        }
        annotation = resourceInfo.getResourceClass().getAnnotation(annotationClass);
        if (null != annotation) {
            return annotation;
        }
        return null;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Check if all is permitted
        {
            PermitAll permitAll = getAnnotation(PermitAll.class);
            if (null != permitAll) {
                // Nothing to do
                return;
            }
        }

        // Check for a certain role
        {
            RoleAllowed roleAllowed = getAnnotation(RoleAllowed.class);
            if (null != roleAllowed) {
                Role role = roleAllowed.value();
                if (Role.BASIC_AUTHENTICATED == role) {
                    basicAuth(requestContext);
                    return;
                }

                // Role unknown...
                LOG.warn("Encountered unknown role '{}' in class {}", role.getId(), resourceInfo.getResourceClass().getName());
                deny(requestContext);
                return;
            }
        }

        // ... again using generic 'javax.annotation.security.RolesAllowed' annotation class
        {
            RolesAllowed rolesAllowed = getAnnotation(RolesAllowed.class);
            if (null != rolesAllowed) {
                String[] roles = rolesAllowed.value();
                if (hasRole(Role.BASIC_AUTHENTICATED.getId(), roles)) {
                    basicAuth(requestContext);
                    return;
                }

                // Other roles unknown...
                LOG.warn("Encountered unknown roles '{}' in class {}", Arrays.toString(roles), resourceInfo.getResourceClass().getName());
                deny(requestContext);
                return;
            }
        }

        // Check if all is denied
        {
            DenyAll denyAll = getAnnotation(DenyAll.class);
            if (null != denyAll) {
                deny(requestContext);
                return;
            }
        }

        // No suitable annotation found. Fall-back to "Basic-Authenticated" (as it was before)
        LOG.warn("Found no security annotation for class {}. Assuming \"Basic-Authenticated\"...", resourceInfo.getResourceClass().getName());
        basicAuth(requestContext);
    }

    private void basicAuth(ContainerRequestContext requestContext) {
        if (doFail) {
            LOG.error(
                "Denied incoming HTTP request to REST interface due to unset Basic-Auth configuration. " +
                "Please set properties 'com.openexchange.rest.services.basic-auth.login' and " +
                "'com.openexchange.rest.services.basic-auth.password' appropriately.",
                new Throwable("Denied request to REST interface"));
            deny(requestContext);
        } else {
            if (authenticated(requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))) {
                boolean secure = requestContext.getUriInfo().getRequestUri().getScheme().equals("https");
                Principal principal = new TrustedAppPrincipal(requestContext.getUriInfo().getBaseUri().getHost());
                requestContext.setSecurityContext(new SecurityContextImpl(principal, SecurityContext.BASIC_AUTH, secure));
            } else {
                requestContext.abortWith(Response.status(Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"OX REST\", encoding=\"UTF-8\"")
                    .build());
            }
        }
    }

    private void deny(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Status.FORBIDDEN).build());
    }

    private boolean hasRole(String roleToCheck, String[] roles) {
        if (null != roles) {
            for (String role : roles) {
                if (roleToCheck.equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean authenticated(String authHeader) {
        if (null == authHeader) {
            // Authorization header missing
            return false;
        }

        if (com.openexchange.tools.servlet.http.Authorization.checkForBasicAuthorization(authHeader)) {
            final Credentials creds = com.openexchange.tools.servlet.http.Authorization.decode(authHeader);
            if (!com.openexchange.tools.servlet.http.Authorization.checkLogin(creds.getPassword())) {
                // Empty password
                return false;
            }
            // Check parsed credentials
            return authLogin.equals(creds.getLogin()) && authPassword.equals(creds.getPassword());
        }

        // Unsupported auth scheme
        return false;
    }

}
