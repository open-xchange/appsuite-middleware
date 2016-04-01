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

import static org.slf4j.LoggerFactory.getLogger;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.carddav.Tools;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVServlet;
import com.openexchange.exception.OXException;
import com.openexchange.login.Interface;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * The {@link CalDAV} servlet. It delegates all calls to the CaldavPerformer
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CardDAV extends DAVServlet {

	private static final long serialVersionUID = -6381396333467867154L;

    /**
     * Initializes a new {@link CardDAV}.
     *
     * @param performer The CardDAV performer
     */
    public CardDAV(CarddavPerformer performer) {
        super(performer, Interface.CARDDAV);
    }

    /**
     * Gets a value indicating whether CardDAV is enabled for the supplied session.
     *
     * @param request the HTTP request
     * @param session The session to check permissions for
     * @return <code>true</code> if CardDAV is enabled, <code>false</code>, otherwise
     */
    @Override
    protected boolean checkPermission(HttpServletRequest request, ServerSession session) {
        if (false == session.getUserPermissionBits().hasContact()) {
            return false;
        }
        ConfigViewFactory configViewFactory = performer.getFactory().getOptionalService(ConfigViewFactory.class);
        if (null == configViewFactory) {
            getLogger(CardDAV.class).warn("Unable to access confic cascade, unable to check servlet permissions.");
            return false;
        }
        try {
            ConfigView configView = configViewFactory.getView(session.getUserId(), session.getContextId());
            ComposedConfigProperty<Boolean> property = configView.property("com.openexchange.carddav.enabled", boolean.class);
            if (property.isDefined() && property.get()) {
                OAuthAccess oAuthAccess = (OAuthAccess) request.getAttribute(OAuthConstants.PARAM_OAUTH_ACCESS);
                if (oAuthAccess == null) {
                    // basic auth took place
                    return true;
                } else {
                    return oAuthAccess.getScope().has(Tools.OAUTH_SCOPE);
                }
            }
        } catch (OXException e) {
            getLogger(CardDAV.class).error("Error checking if CardDAV is enabled for user {} in context {}: {}",
                session.getUserId(), session.getContextId(), e.getMessage(), e);
        }
        return false;
    }

}
