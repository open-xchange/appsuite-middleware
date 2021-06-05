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

package com.openexchange.oauth.provider.json;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.oauth.provider.json.actions.AllAction;
import com.openexchange.oauth.provider.json.actions.RevokeAction;
import com.openexchange.server.ServiceLookup;


/**
 * {@link OAuthProviderActionFactory}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthProviderActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    public OAuthProviderActionFactory(ServiceLookup services) {
        super();
        actions = new HashMap<>();
        actions.put("all", new AllAction(services));
        actions.put("revoke", new RevokeAction(services));
    }

    @Override
    public AJAXActionService createActionService(String action) {
        return actions.get(action);
    }
}
