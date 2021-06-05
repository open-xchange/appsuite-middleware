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

package com.openexchange.messaging.json.actions.accounts;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.registry.MessagingServiceRegistry;


/**
 * {@link AccountActionFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AccountActionFactory implements AJAXActionServiceFactory {

    public static volatile AccountActionFactory INSTANCE; // Initialize in Activator

    private final Map<String, AJAXActionService> actions;

    public AccountActionFactory(final MessagingServiceRegistry registry) {
        ImmutableMap.Builder<String, AJAXActionService> builder = ImmutableMap.builder();
        builder.put("all", new AllAction(registry));
        builder.put("delete", new DeleteAction(registry));
        builder.put("get", new GetAction(registry));
        builder.put("getconfig", new GetConfigAction(registry));
        builder.put("new", new NewAction(registry));
        builder.put("update", new UpdateAction(registry));
        actions = builder.build();
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }
}
