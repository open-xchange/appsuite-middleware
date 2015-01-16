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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.Strings;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AbstractAJAXActionAnnotationProcessor;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link OAuthAnnotationProcessor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthAnnotationProcessor extends AbstractAJAXActionAnnotationProcessor<OAuthAction> {

    /*
     * From https://tools.ietf.org/html/rfc6749#section-3.3:
     *   scope       = scope-token *( SP scope-token )
     *   scope-token = 1*( %x21 / %x23-5B / %x5D-7E )
     */
    private static final Pattern PREFIXED_OAUTH_SCOPE = Pattern.compile("(r_|w_|rw_)([\\x21\\x23-\\x5b\\x5d-\\x7e]+)");

    @Override
    protected Class<OAuthAction> getAnnotation() {
        return OAuthAction.class;
    }

    @Override
    protected void doProcess(OAuthAction annotation, AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException {
        if (OAuthConstants.AUTH_TYPE.equals(session.getParameter("com.openexchange.authType"))) {
            OAuthAction oAuthAction = action.getClass().getAnnotation(OAuthAction.class);
            String requiredScope = oAuthAction.value();
            if (!OAuthAction.GRANT_ALL.equals(requiredScope)) {
                String sessionScopes = (String) session.getParameter("com.openexchange.oauth.scopes");
                if (Strings.isNullOrEmpty(sessionScopes)) {
                    throw new OAuthInsufficientScopeException(requiredScope);
                }

                String[] split = sessionScopes.split("\\s");
                Set<String> sessionScopeSet;
                if (split.length == 1) {
                    sessionScopeSet = Collections.singleton(split[0]);
                } else {
                    sessionScopeSet = new HashSet<>();
                    for (String scope : split) {
                        sessionScopeSet.add(scope);
                    }
                }

                if (!oAuthScopeSatisfied(requiredScope, sessionScopeSet)) {
                    throw new OAuthInsufficientScopeException(requiredScope);
                }
            }
        }
    }

    private static boolean oAuthScopeSatisfied(String requiredScope, Set<String> sessionScopes) {
        if (sessionScopes.contains(requiredScope)) {
            return true;
        }

        Matcher prefixedScopeMatcher = PREFIXED_OAUTH_SCOPE.matcher(requiredScope);
        if (prefixedScopeMatcher.matches()) {
            String prefix = prefixedScopeMatcher.group(1);
            String scope = prefixedScopeMatcher.group(2);
            switch (prefix) {
                case "r_":
                    return sessionScopes.contains("rw_" + scope);
                case "w_":
                    return sessionScopes.contains("rw_" + scope);
                case "rw_":
                    return sessionScopes.contains("r_" + scope) && sessionScopes.contains("w_" + scope);
            }
        }

        return false;
    }

}
