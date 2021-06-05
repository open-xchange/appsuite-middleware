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

package com.openexchange.subscribe.json.actions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class SubscriptionActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions = new ConcurrentHashMap<String, AJAXActionService>(12, 0.9f, 1);

    public SubscriptionActionFactory(final ServiceLookup services) {
        super();
        actions.put("new", new NewSubscriptionAction(services));
        actions.put("get", new GetSubscriptionAction(services));
        actions.put("all", new AllSubscriptionAction(services));
        actions.put("list", new ListSubscriptionAction(services));
        actions.put("update", new UpdateSubscriptionAction(services));
        actions.put("refresh", new RefreshSubscriptionAction(services));
        actions.put("delete", new DeleteSubscriptionAction(services));
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        if (actions.containsKey(action)) {
            return actions.get(action);
        }
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
    }

}
