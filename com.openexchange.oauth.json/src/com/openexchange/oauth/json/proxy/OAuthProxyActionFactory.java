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

package com.openexchange.oauth.json.proxy;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.http.OAuthHTTPClientFactory;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link OAuthProxyActionFactory}
 */
public class OAuthProxyActionFactory implements AJAXActionServiceFactory {

    private final AJAXActionService proxyAction;

    public OAuthProxyActionFactory(OAuthService service, OAuthHTTPClientFactory clients) {
        proxyAction = new OAuthProxyAction(service, clients);
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        if (!"PUT".equals(action) && !"POST".equals(action)) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(action, "oauth/proxy");
        }
        return proxyAction;
    }

}
