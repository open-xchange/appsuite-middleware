/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.caldav.servlet;

import static com.openexchange.java.Autoboxing.b;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVOAuthScope;
import com.openexchange.dav.DAVServlet;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.login.Interface;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.webdav.protocol.WebdavMethod;

/**
 * The {@link CalDAV} servlet. It delegates all calls to the CaldavPerformer
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CalDAV extends DAVServlet {

    private static final long serialVersionUID = -7768308794451862636L;

    /** The required scope to access read-only CalDAV-related endpoints for restricted sessions (authenticated with app-specific passwords) */
    private static final String RESTRICTED_SCOPE_CALDAV_READ = "read_caldav";

    /** The required scope to access read-only CalDAV-related endpoints for restricted sessions (authenticated with app-specific passwords) */
    private static final String RESTRICTED_SCOPE_CALDAV_WRITE = "write_caldav";

    /**
     * Initializes a new {@link CalDAV}.
     *
     * @param performer The CalDAV performer
     */
    public CalDAV(CaldavPerformer performer) {
        super(performer, Interface.CALDAV);
    }

    @Override
    protected boolean checkPermission(HttpServletRequest request, WebdavMethod method, ServerSession session) {
        /*
         * check basic permissions of the session's user
         */
        boolean enabled = false;
        try {
            ComposedConfigProperty<Boolean> enabledProperty = performer.getFactory().requireService(ConfigViewFactory.class).getView(session.getUserId(), session.getContextId()).property("com.openexchange.caldav.enabled", Boolean.class);
            enabled = enabledProperty.isDefined() && b(enabledProperty.get());
        } catch (OXException e) {
            org.slf4j.LoggerFactory.getLogger(CalDAV.class).warn("Error obtaining 'enabled' property from config-cascade, assuming 'false'.", e);
            return false;
        }
        if (false == enabled || false == session.getUserPermissionBits().hasCalendar()) {
            return false;
        }
        /*
         * check that "caldav" scope is available when authenticated through OAuth
         */
        OAuthAccess oAuthAccess = (OAuthAccess) request.getAttribute(OAuthConstants.PARAM_OAUTH_ACCESS);
        if (null != oAuthAccess) {
            Scope scope = oAuthAccess.getScope();
            return scope.has(DAVOAuthScope.CALDAV.getScope());
        }
        /*
         * check that an "caldav" scope appropriate for the method is available when session is restricted (authenticated through app-specific password)
         */
        String restrictedScopes = (String) session.getParameter(Session.PARAM_RESTRICTED);
        if (null != restrictedScopes) {
            String requiredScope = null != method && method.isReadOnly() ? RESTRICTED_SCOPE_CALDAV_READ : RESTRICTED_SCOPE_CALDAV_WRITE;
            return Strings.splitByComma(restrictedScopes, new HashSet<String>()).contains(requiredScope);
        }
        /*
         * assume regularly authenticated *DAV session, otherwise
         */
        return true;
    }

}
