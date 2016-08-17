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

package com.openexchange.ajax.requesthandler.oauth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AbstractAJAXActionAnnotationProcessor;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthScopeCheck;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link OAuthAnnotationProcessor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthAnnotationProcessor extends AbstractAJAXActionAnnotationProcessor<OAuthAction> {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthAnnotationProcessor.class);

    @Override
    protected Class<OAuthAction> getAnnotation() {
        return OAuthAction.class;
    }

    @Override
    protected void doProcess(OAuthAction annotation, AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException {
        OAuthAccess oAuthAccess = requestData.getProperty(OAuthConstants.PARAM_OAUTH_ACCESS);
        if (oAuthAccess == null) {
            return;
        }

        OAuthAction oAuthAction = action.getClass().getAnnotation(OAuthAction.class);
        String requiredScope = oAuthAction.value();
        if (OAuthAction.GRANT_ALL.equals(requiredScope)) {
            return;
        } else if (OAuthAction.CUSTOM.equals(requiredScope)) {
            for (Method method : action.getClass().getMethods()) {
                if (method.isAnnotationPresent(OAuthScopeCheck.class)) {
                    if (hasScopeCheckSignature(method)) {
                        try {
                            if ((boolean) method.invoke(action, requestData, session, oAuthAccess)) {
                                return;
                            }
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
                    } else {
                        LOG.warn("Method '" + action.getClass() + "." + method.getName() + "' is annotated with @OAuthScopeCheck but its signature is invalid!");
                    }
                }
            }

            throw new OAuthInsufficientScopeException();
        } else {
            if (!oAuthAccess.getScope().has(requiredScope)) {
                throw new OAuthInsufficientScopeException(requiredScope);
            }
        }
    }

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
