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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.rest.services.EndpointAuthenticator;
import com.openexchange.rest.services.RequestContext;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;
import com.openexchange.rest.services.security.authenticator.DefaultEndpointAuthenticator;
import com.openexchange.rest.services.security.authenticator.MasterAdminEndpointAuthenticator;
import com.openexchange.tools.servlet.http.Authorization.Credentials;

/**
 * <p>The {@link AuthenticationFilter} sets the requests {@link SecurityContext}
 * based on the provided authentication information. Requests that arent't authenticated
 * are aborted.</p>
 * <br>
 * <p>Currently authentication is only based on HTTP basic authentication with the
 * credentials defined via 'com.openexchange.rest.services.basic-auth' in server.properties
 * or via the OX master admin password defined in the <code>mpasswd</code> file.</p>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    // -------------------------------------------------------------------------------------------------------------------------------------

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private ResourceContext resourceContext;

    private final EndpointAuthenticator defaultAuthenticator;
    private final EndpointAuthenticator masterAdminAuthenticator;

    /**
     * Initialises a new {@link AuthenticationFilter}.
     * 
     * @param configurationService The {@link ConfigurationService} instance
     */
    public AuthenticationFilter(ConfigurationService configurationService) {
        defaultAuthenticator = initialiseDefaultAuthenticator(configurationService);
        masterAdminAuthenticator = initialiseMasterAdminAuthenticator(configurationService);
    }

    /**
     * Initialises the master admin authenticator
     * 
     * @param configurationService The {@link ConfigurationService}
     * @return The master admin {@link EndpointAuthenticator}
     */
    // Shamelessly copied from AdminCache. Maybe we need a MasterAdminAuthService?
    private EndpointAuthenticator initialiseMasterAdminAuthenticator(ConfigurationService configurationService) {
        File file = configurationService.getFileByName("mpasswd");
        if (null == file) {
            return null;
        }

        try (BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8), 2048)) {
            String line = null;
            while ((line = bf.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.indexOf(':') < 0) {
                    continue;
                }
                // Ok seems to be a line with user:pass entry
                String[] user_pass_combination = line.split(":");
                if (user_pass_combination.length != 3) {
                    LOG.warn("Invalid mpasswd format.");
                }
                return new MasterAdminEndpointAuthenticator();
            }
        } catch (IOException e) {
            LOG.warn("Error processing master auth file: mpasswd", e);
            return null;
        }
        return null;
    }

    /**
     * Initialises the default {@link EndpointAuthenticator}
     * 
     * @param configurationService The {@link ConfigurationService}
     * @return The default {@link EndpointAuthenticator}
     */
    private EndpointAuthenticator initialiseDefaultAuthenticator(ConfigurationService configurationService) {
        String authLogin = configurationService.getProperty("com.openexchange.rest.services.basic-auth.login");
        String authPassword = configurationService.getProperty("com.openexchange.rest.services.basic-auth.password");
        if (Strings.isEmpty(authLogin) || Strings.isEmpty(authPassword)) {
            return null;
        }
        return new DefaultEndpointAuthenticator(authLogin.trim(), authPassword.trim());
    }

    /**
     * Gets the {@link Annotation} from the specified class
     * 
     * @param annotationClass The class containing the annotation
     * @return The {@link Annotation} {@link A}
     */
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

    /**
     * Acquires the {@link EndpointAuthenticator} from the {@link ResourceContext}
     * 
     * @return The {@link EndpointAuthenticator}
     */
    private EndpointAuthenticator acquireAuthenticator() {
        Object resourceInstance = resourceContext.getResource(resourceInfo.getResourceClass());
        if (EndpointAuthenticator.class.isInstance(resourceInstance)) {
            return (EndpointAuthenticator) resourceInstance;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ws.rs.container.ContainerRequestFilter#filter(javax.ws.rs.container.ContainerRequestContext)
     */
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
                switch (role) {
                    case BASIC_AUTHENTICATED:
                        basicAuth(requestContext);
                        return;
                    case INDIVIDUAL_BASIC_AUTHENTICATED:
                        EndpointAuthenticator authenticator = acquireAuthenticator();
                        if (null == authenticator) {
                            LOG.warn("Detected role '{}' in class {}, but that end-point does not implement interface {}", Role.INDIVIDUAL_BASIC_AUTHENTICATED.getId(), resourceInfo.getResourceClass().getName(), EndpointAuthenticator.class.getName());
                            deny(requestContext);
                            return;
                        }

                        authenticatorAuth(authenticator, requestContext, resourceInfo.getResourceMethod());
                        return;
                    case MASTER_ADMIN_AUTHENTICATED:
                        if (null == masterAdminAuthenticator) {
                            LOG.warn("Unable to perform master authentication");
                            deny(requestContext);
                            return;
                        }
                        masterAuth(requestContext, resourceInfo.getResourceMethod());
                        return;
                    default:
                        // Role unknown...
                        LOG.warn("Encountered unknown role '{}' in class {}", role.getId(), resourceInfo.getResourceClass().getName());
                        deny(requestContext);
                        return;
                }
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
                if (hasRole(Role.INDIVIDUAL_BASIC_AUTHENTICATED.getId(), roles)) {
                    EndpointAuthenticator authenticator = acquireAuthenticator();
                    if (null == authenticator) {
                        LOG.warn("Detected role '{}' in class {}, but that end-point does not implement interface {}", Role.INDIVIDUAL_BASIC_AUTHENTICATED.getId(), resourceInfo.getResourceClass().getName(), EndpointAuthenticator.class.getName());
                        deny(requestContext);
                        return;
                    }

                    authenticatorAuth(authenticator, requestContext, resourceInfo.getResourceMethod());
                    return;
                }
                if (hasRole(Role.MASTER_ADMIN_AUTHENTICATED.getId(), roles)) {
                    if (null == masterAdminAuthenticator) {
                        LOG.warn("Unable to perform master authentication");
                        deny(requestContext);
                        return;
                    }
                    masterAuth(requestContext, resourceInfo.getResourceMethod());
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

    /**
     * Performs basic authentication with the default OX REST credentials
     * 
     * @param requestContext The {@link RequestContext}
     */
    private void basicAuth(ContainerRequestContext requestContext) {
        if (defaultAuthenticator == null) {
            LOG.error("Denied incoming HTTP request to REST interface due to unset Basic-Auth configuration. " + "Please set properties 'com.openexchange.rest.services.basic-auth.login' and " + "'com.openexchange.rest.services.basic-auth.password' appropriately.", new Throwable("Denied request to REST interface"));
            deny(requestContext);
        } else {
            authenticatorAuth(defaultAuthenticator, requestContext, null);
        }
    }

    /**
     * Performs the authentication with the specified {@link EndpointAuthenticator}
     * 
     * @param authenticator The {@link EndpointAuthenticator} to perform the authenticationw ith
     * @param requestContext The {@link RequestContext}
     * @param invokedMethod The invoked REST method
     */
    private void authenticatorAuth(EndpointAuthenticator authenticator, ContainerRequestContext requestContext, Method invokedMethod) {
        if (authenticator.permitAll(invokedMethod)) {
            // Nothing to do
            return;
        }

        String authHeader = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        Credentials credentials = acquireCredentialsFromAuthHeader(authHeader);
        if (null == credentials) {
            reflectAuthenticated(false, authenticator.getRealmName(), requestContext);
            return;
        }

        boolean authenticated = authenticator.authenticate(credentials.getLogin(), credentials.getPassword(), invokedMethod);
        reflectAuthenticated(authenticated, authenticator.getRealmName(), requestContext);
        return;
    }

    /**
     * Performs basic authentication with the OX master admin credentials
     * 
     * @param requestContext The {@link RequestContext}
     * @param invokedMethod The invoked REST method
     */
    private void masterAuth(ContainerRequestContext requestContext, Method invokedMethod) {
        if (masterAdminAuthenticator == null) {
            LOG.error("Denied incoming HTTP request to REST interface due to misconfigured master authentication. Please review the 'mpasswd' file.", new Throwable("Denied request to REST interface"));
            deny(requestContext);
        } else {
            authenticatorAuth(masterAdminAuthenticator, requestContext, null);
        }
    }

    /**
     * Reflects the authentication status to the specified {@link RequestContext}
     * 
     * @param authenticated whether the authentication was successful
     * @param realm The realm of the authentication
     * @param requestContext The {@link RequestContext}
     */
    private void reflectAuthenticated(boolean authenticated, String realm, ContainerRequestContext requestContext) {
        if (false == authenticated) {
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, new StringBuilder(32).append("Basic realm=\"").append(realm).append("\", encoding=\"UTF-8\"").toString()).build());
            return;
        }

        boolean secure = requestContext.getUriInfo().getRequestUri().getScheme().equals("https");
        Principal principal = new TrustedAppPrincipal(requestContext.getUriInfo().getBaseUri().getHost());
        requestContext.setSecurityContext(new SecurityContextImpl(principal, SecurityContext.BASIC_AUTH, secure));
    }

    /**
     * Denies the request due to failed or missing authentication
     * 
     * @param requestContext The {@link RequestContext}
     */
    private void deny(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Status.FORBIDDEN).build());
    }

    /**
     * Checks whether the specified {@link Role} is enlisted in the specified array of roles
     * 
     * @param roleToCheck The role to check
     * @param roles The array of roles
     * @return <code>true</code> if the role is enlisted; <code>false</code> otherwise
     */
    private boolean hasRole(String roleToCheck, String[] roles) {
        if (null == roles) {
            return false;
        }
        for (String role : roles) {
            if (roleToCheck.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts the credentials from the Authorization header
     * 
     * @param authHeader The authorization header
     * @return The {@link Credentials}
     */
    private Credentials acquireCredentialsFromAuthHeader(String authHeader) {
        if (null == authHeader) {
            // Authorization header missing
            return null;
        }

        if (com.openexchange.tools.servlet.http.Authorization.checkForBasicAuthorization(authHeader)) {
            final Credentials creds = com.openexchange.tools.servlet.http.Authorization.decode(authHeader);
            if (!com.openexchange.tools.servlet.http.Authorization.checkLogin(creds.getPassword())) {
                // Empty password
                return null;
            }
            // Check parsed credentials
            return creds;
        }

        // Unsupported auth scheme
        return null;
    }
}
