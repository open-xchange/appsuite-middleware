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

package com.openexchange.saml.oauth;

import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.saml.oauth.osgi.Services;

/**
 * {@link SAMLOAuthConfig}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class SAMLOAuthConfig {

    private static final String TOKEN_ENDPOINT_PROPERTY = "com.openexchange.saml.oauth.introspection";
    private static final String CLIENT_ID_PROPERTY = "com.openexchange.saml.oauth.clientId";
    private static final String CLIENT_SECRET_PROPERTY = "com.openexchange.saml.oauth.clientSecret";

    static String getIntrospectionEndpoint(int userId, int contextId) throws OXException{
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(userId, contextId);
        return view.get(TOKEN_ENDPOINT_PROPERTY, String.class);
    }

    static String getClientID(int userId, int contextId) throws OXException {
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(userId, contextId);
        return view.get(CLIENT_ID_PROPERTY, String.class);
    }

    static String getClientSecret(int userId, int contextId) throws OXException{
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(userId, contextId);
        return view.get(CLIENT_SECRET_PROPERTY, String.class);
    }

    public static boolean isConfigured(int userId, int contextId) throws OXException{
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(userId, contextId);
        return view.property(TOKEN_ENDPOINT_PROPERTY, String.class).isDefined() && view.property(CLIENT_ID_PROPERTY, String.class).isDefined() && view.property(CLIENT_SECRET_PROPERTY, String.class).isDefined();
    }

}
