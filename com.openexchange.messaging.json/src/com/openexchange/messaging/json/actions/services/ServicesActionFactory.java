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

package com.openexchange.messaging.json.actions.services;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.messaging.registry.MessagingServiceRegistry;

/**
 * {@link ServicesActionFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ServicesActionFactory implements AJAXActionServiceFactory {

    public static volatile ServicesActionFactory INSTANCE; // Initialized in Activator

    private final Map<String, AJAXActionService> actions;

    public ServicesActionFactory(final MessagingServiceRegistry registry) {
        actions = new HashMap<String, AJAXActionService>(2);
        actions.put("all", new AllAction(registry));
        actions.put("get", new GetAction(registry));
    }

    @Override
    public AJAXActionService createActionService(final String action) {
        return actions.get(action);
    }
}
