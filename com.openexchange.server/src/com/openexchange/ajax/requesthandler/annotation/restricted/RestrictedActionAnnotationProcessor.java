
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

package com.openexchange.ajax.requesthandler.annotation.restricted;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AbstractAJAXActionAnnotationProcessor;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.annotation.NonNull;
import com.openexchange.authentication.application.exceptions.AppPasswordExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthScopeCheck;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RestrictedActionAnnotationProcessor}
 * Checks session for restricted authentication. If present, verifies scope
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.4
 */
public class RestrictedActionAnnotationProcessor extends AbstractAJAXActionAnnotationProcessor<RestrictedAction> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RestrictedActionAnnotationProcessor.class);

    /**
     * Initializes a new {@link RestrictedActionAnnotationProcessor}.
     */
    public RestrictedActionAnnotationProcessor() {
        super();
    }

    @Override
    protected Class<RestrictedAction> getAnnotation() {
        return RestrictedAction.class;
    }

    @Override
    protected void doProcess(RestrictedAction annotation, AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException {
        Object restrParam = session.getParameter(Session.PARAM_RESTRICTED);
        Object isOAuth = session.getParameter(Session.PARAM_IS_OAUTH);

        if (restrParam == null && isOAuth == null) {
            return; // Not a restricted session or an oAuth access, just return
        }
        RestrictedAction restrAction = action.getClass().getAnnotation(RestrictedAction.class);
        String requiredScope = restrAction.type().getScope(restrAction.module());

        // Return if grant all
        if (RestrictedAction.GRANT_ALL.equals(restrAction.module())) {
            return;
        }

        if (restrParam != null) {
            processAppPassswordRequest(requestData, restrParam, restrAction, requiredScope);
        } else if (isOAuth != null) {
            processOAuthRequest(requestData, action, restrAction, requiredScope, session);
        }
    }

    @SuppressWarnings("deprecation")
    private static final String OAUTH_SUB_MODULE = "/" + OAuthConstants.OAUTH_SERVLET_SUBPREFIX;

    /**
     * Verifies that the incoming request, which is based on a session with restricted capabilities, is authorized to perform the requested action.
     *
     * @param requestData The request data
     * @param restrParam The {@link Session#PARAM_RESTRICTED} session parameter
     * @param restrAction The {@link RestrictedAction}
     * @param requiredScope The required scope
     * @throws OXException In case the session is not authorized
     */
    private static void processAppPassswordRequest(AJAXRequestData requestData, Object restrParam, @NonNull RestrictedAction restrAction, String requiredScope) throws OXException {
        // Use AppPasswordAnnotationProcessor only on requests against the "normal" API and not against oauth/modules/ sub-path.
        // Can be removed once the sub-path is no longer used
        HttpServletRequest servletRequest = requestData.optHttpServletRequest();
        if(servletRequest != null) {
            if (servletRequest.getServletPath().startsWith(OAUTH_SUB_MODULE)) {
                return;
            }
        }

        // Check if this action requires full auth and rejects the request if not
        if (RestrictedAction.REQUIRES_FULL_AUTH.equals(restrAction.module())) {
            throw AppPasswordExceptionCodes.NOT_AUTHORIZED.create(requiredScope);
        }

        // Check the scopes of authentication for this session
        if (false == (restrParam instanceof String[])) {
            throw AppPasswordExceptionCodes.APPLICATION_PASSWORD_GENERIC_ERROR.create("Unkown restricted session type");
        }
        List<String> restrictedScopes = Arrays.asList((String[]) restrParam);
        LOG.debug("Restricted session hit for module " + requestData.getModule() + " action:" + requestData.getAction() + " required:" + requiredScope);

        if (!restrictedScopes.contains(requiredScope)) {
            throw AppPasswordExceptionCodes.NOT_AUTHORIZED.create(requiredScope);
        }
    }


    /**
     * Checks if the incoming oAuth request has sufficient permissions to perform annotated action.
     *
     * @param requestData The {@link AJAXRequestData}
     * @param action The {@link AJAXActionService}
     * @param restrAction The {@link RestrictedAction} annotation of the action
     * @param requiredScope Th required scope
     * @param session The session to check
     * @throws OXException in case the session is not authorized
     */
    private static void processOAuthRequest(AJAXRequestData requestData, AJAXActionService action, RestrictedAction restrAction, String requiredScope, ServerSession session) throws OXException {
        OAuthAccess oAuthAccess = requestData.getProperty(OAuthConstants.PARAM_OAUTH_ACCESS);

        if(oAuthAccess != null) {
            LOG.debug("OAuth access for module " + requestData.getModule() + " action:" + requestData.getAction() + " required:" + requiredScope);
            if (restrAction.hasCustomOAuthScopeCheck()) {
                for (Method method : action.getClass().getMethods()) {
                    if (method.isAnnotationPresent(OAuthScopeCheck.class)) {
                        if (hasScopeCheckSignature(method)) {
                            try {
                                if (((Boolean) method.invoke(action, requestData, session, oAuthAccess)).booleanValue()) {
                                    return;
                                }
                                break;
                            } catch (InvocationTargetException e) {
                                Throwable cause = e.getCause();
                                if (cause instanceof OXException) {
                                    throw (OXException) cause;
                                }

                                throw new OXException(cause);
                            } catch (IllegalAccessException | IllegalArgumentException e) {
                                LOG.error("Could not check scope", e);
                                throw new OXException(e);
                            }
                        }
                        LOG.warn("Method ''{}.{}'' is annotated with @OAuthScopeCheck but its signature is invalid!", action.getClass(), method.getName());
                    }
                }
                throw new OAuthInsufficientScopeException(requiredScope);
            } else if(oAuthAccess.getScope().has(requiredScope) == false) {
                throw new OAuthInsufficientScopeException(requiredScope);
            }
        }
    }

    /**
     * Checks whether the method annotated with @OAuthScopeCheck has the correct signature.
     *
     * @param method The method to check
     * @return <code>true</code> if the method is valid, <code>false</code> otherwise
     */
    private static boolean hasScopeCheckSignature(Method method) {
        if (Modifier.isPublic(method.getModifiers()) && method.getReturnType().isAssignableFrom(boolean.class)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 3) {
                return parameterTypes[0].isAssignableFrom(AJAXRequestData.class) &&
                       parameterTypes[1].isAssignableFrom(ServerSession.class) &&
                       parameterTypes[2].isAssignableFrom(OAuthAccess.class);
            }
        }

        return false;
    }
}
