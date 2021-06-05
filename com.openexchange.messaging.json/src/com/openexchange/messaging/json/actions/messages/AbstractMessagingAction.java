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

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.caching.Cache;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * Common superclass for all messaging actions providing common services (the registry, a writer and a parser for messages) to subclasses. Subclasses must implement
 * the {@link #doIt(MessagingRequestData, ServerSession)}
 * @see MessagingRequestData
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMessagingAction implements AJAXActionService {

    protected MessagingServiceRegistry registry;
    protected MessagingMessageWriter writer;
    protected MessagingMessageParser parser;
    private Cache cache;

    public AbstractMessagingAction(final MessagingServiceRegistry registry, final MessagingMessageWriter writer, final MessagingMessageParser parser) {
        this(registry, writer, parser, null);
    }

    public AbstractMessagingAction(final MessagingServiceRegistry registry, final MessagingMessageWriter writer, final MessagingMessageParser parser, final Cache cache) {
        this.registry = registry;
        this.writer = writer;
        this.parser = parser;
        this.cache = cache;
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        final MessagingRequestData req = new MessagingRequestData(requestData, session, registry, parser, cache);
        try {
            final AJAXRequestResult result = doIt(req, session);
            return result;
        } catch (JSONException e) {
            throw MessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            req.cleanUp();
        }
    }

    protected abstract AJAXRequestResult doIt(MessagingRequestData messagingRequestData, ServerSession session) throws JSONException, IOException, OXException;


    public void setCache(final Cache cache) {
        this.cache = cache;
    }
}
