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
public class SubscriptionSourcesActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions = new ConcurrentHashMap<String, AJAXActionService>(6, 0.9f, 1);

    /**
     * Initializes a new {@link SubscriptionSourcesActionFactory}.
     *
     * @param services The servie look-up
     */
    public SubscriptionSourcesActionFactory(final ServiceLookup services) {
        super();
        // someone decided to describe this one way and implement it another ... This works for both
        actions.put("listSources", new ListSourcesAction(services));
        actions.put("all", new ListSourcesAction(services));
        actions.put("getSource", new GetSourceAction(services));
        actions.put("get", new GetSourceAction(services));
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        if (actions.containsKey(action)) {
            return actions.get(action);
        }
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
    }

}
