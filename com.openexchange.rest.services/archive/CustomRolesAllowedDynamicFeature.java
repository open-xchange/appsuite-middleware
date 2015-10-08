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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.jaxrs.security;

import java.io.IOException;
import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.model.AnnotatedMethod;

/**
 * <p>This class enforces authorization for all resources that are marked with
 * <ul>
 * <li>{@link javax.annotation.security.DenyAll}</li>
 * <li>{@link javax.annotation.security.PermitAll}</li>
 * <li>{@link javax.annotation.security.RolesAllowed}</li>
 * </ul></p>
 * <br>
 * <p>This implementation is based on org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature.
 * It returns HTTP status 401 with the WWW-Authenticate header set if a resources requires
 * authorization but the request is not even authenticated. If authentication is valid, but
 * the principal is not authorized (based on its role), HTTP status 403 is returned.</p>
 * <br>
 * <p>An additional extension was made to scan for {@link RolesAllowed} annotations on super
 * classes. That makes it possible to create abstract classes that define an authorization scope,
 * like {@link TrustedAppService} does.</p>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Provider
public class CustomRolesAllowedDynamicFeature implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext configuration) {
        AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        // DenyAll on the method take precedence over RolesAllowed and PermitAll
        if (am.isAnnotationPresent(DenyAll.class)) {
            configuration.register(new RolesAllowedRequestFilter());
            return;
        }

        // RolesAllowed on the method takes precedence over PermitAll
        RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
        if (ra != null) {
            configuration.register(new RolesAllowedRequestFilter(ra.value()));
            return;
        }

        // PermitAll takes precedence over RolesAllowed on the class
        if (am.isAnnotationPresent(PermitAll.class)) {
            // Do nothing.
            return;
        }

        // DenyAll can't be attached to classes

        // RolesAllowed on the class takes precedence over PermitAll
        Class<?> clazz = resourceInfo.getResourceClass();
        while (clazz != null && !clazz.equals(Object.class)) {
            ra = clazz.getAnnotation(RolesAllowed.class);
            if (ra != null) {
                configuration.register(new RolesAllowedRequestFilter(ra.value()));
                break;
            }
            clazz = clazz.getSuperclass();
        }
    }

    @Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
    private static class RolesAllowedRequestFilter implements ContainerRequestFilter {
        private final boolean denyAll;
        private final String[] rolesAllowed;

        RolesAllowedRequestFilter() {
            this.denyAll = true;
            this.rolesAllowed = null;
        }

        RolesAllowedRequestFilter(String[] rolesAllowed) {
            this.denyAll = false;
            this.rolesAllowed = (rolesAllowed != null) ? rolesAllowed : new String[] {};
        }

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            if (!denyAll) {
                SecurityContext securityContext = requestContext.getSecurityContext();
                if (securityContext.getUserPrincipal() == null) {
                    requestContext.abortWith(
                        Response.status(Status.UNAUTHORIZED)
                            .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"OX REST\", encoding=\"UTF-8\"")
                            .build());
                } else {
                    for (String role : rolesAllowed) {
                        if (securityContext.isUserInRole(role)) {
                            return;
                        }
                    }
                }
            }

            requestContext.abortWith(Response.status(Status.FORBIDDEN).build());
        }
    }

}
