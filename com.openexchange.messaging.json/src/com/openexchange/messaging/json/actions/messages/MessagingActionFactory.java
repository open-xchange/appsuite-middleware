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

package com.openexchange.messaging.json.actions.messages;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.caching.Cache;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MessagingActionFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingActionFactory implements AJAXActionServiceFactory {

    public static volatile MessagingActionFactory INSTANCE = null; // Initialized in Activator

    private Map<String, AJAXActionService> actions = null;
    private final Cache cache;
    private final MessagingMessageParser parser;
    private final MessagingServiceRegistry registry;

    public MessagingActionFactory(final MessagingServiceRegistry registry, final MessagingMessageWriter writer, final MessagingMessageParser parser, final Cache cache) {
        super();
        actions = new HashMap<String, AJAXActionService>(8);

        actions.put("all", new AllAction(registry, writer, parser, cache));
        actions.put("get", new GetAction(registry, writer, parser, cache));
        actions.put("list", new ListAction(registry, writer, parser, cache));
        actions.put("perform", new PerformAction(registry, writer, parser, cache));
        actions.put("send", new SendAction(registry, writer, parser, cache));
        actions.put("update", new UpdateAction(registry, writer, parser, cache));
        actions.put("updates", new UpdatesAction(registry, writer, parser, cache));

        this.parser = parser;
        this.cache = cache;
        this.registry = registry;
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

    public MessagingRequestData wrapRequest(final AJAXRequestData req, final ServerSession session) {
        return new MessagingRequestData(req, session, registry, parser, cache);
    }

}
