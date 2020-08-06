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

package com.openexchange.carddav.servlet;

import static com.openexchange.java.Autoboxing.b;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVOAuthScope;
import com.openexchange.dav.DAVServlet;
import com.openexchange.exception.OXException;
import com.openexchange.login.Interface;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.webdav.protocol.WebdavMethod;

/**
 * The {@link CardDAV} servlet. It delegates all calls to the CaldavPerformer
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CardDAV extends DAVServlet {

    private static final long serialVersionUID = -6381396333467867154L;

    /** The required scope to access read-only CardDAV-related endpoints for restricted sessions (authenticated with app-specific passwords) */
    private static final String RESTRICTED_SCOPE_CARDDAV_READ = "read_carddav";

    /** The required scope to access read-only CardDAV-related endpoints for restricted sessions (authenticated with app-specific passwords) */
    private static final String RESTRICTED_SCOPE_CARDDAV_WRITE = "write_carddav";

    /**
     * Initializes a new {@link CardDAV}.
     *
     * @param performer The CardDAV performer
     */
    public CardDAV(CarddavPerformer performer) {
        super(performer, Interface.CARDDAV);
    }

    @Override
    protected boolean checkPermission(HttpServletRequest request, WebdavMethod method, ServerSession session) {
        /*
         * check basic permissions of the session's user
         */
        boolean enabled = false;
        try {
            ComposedConfigProperty<Boolean> enabledProperty = performer.getFactory().requireService(ConfigViewFactory.class).getView(session.getUserId(), session.getContextId()).property("com.openexchange.carddav.enabled", Boolean.class);
            enabled = enabledProperty.isDefined() && b(enabledProperty.get());
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(CardDAV.class).warn("Error obtaining 'enabled' property from config-cascade, assuming 'false'.", e);
            return false;
        }
        if (false == enabled || false == session.getUserPermissionBits().hasContact()) {
            return false;
        }
        /*
         * check that "carddav" scope is available when authenticated through OAuth
         */
        OAuthAccess oAuthAccess = (OAuthAccess) request.getAttribute(OAuthConstants.PARAM_OAUTH_ACCESS);
        if (null != oAuthAccess) {
            Scope scope = oAuthAccess.getScope();
            return scope.has(DAVOAuthScope.CARDDAV.getScope());
        }
        /*
         * check that an "carddav" scope appropriate for the method is available when session is restricted (authenticated through app-specific password)
         */
        String[] restrictedScopes = (String[]) session.getParameter(Session.PARAM_RESTRICTED);
        if (null != restrictedScopes) {
            String requiredScope = null != method && method.isReadOnly() ? RESTRICTED_SCOPE_CARDDAV_READ : RESTRICTED_SCOPE_CARDDAV_WRITE;
            return com.openexchange.tools.arrays.Arrays.contains(restrictedScopes, requiredScope);
        }
        /*
         * assume regularly authenticated *DAV session, otherwise
         */
        return true;
    }

}
