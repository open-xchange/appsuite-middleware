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

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.caching.Cache;
import com.openexchange.exception.OXException;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Returns all messages in a given folder. Parameters are:
 * <dl>
 * <dt>messagingService</dt>
 * <dd>The messaging service id</dd>
 * <dt>account</dt>
 * <dd>The id of the messaging account</dd>
 * <dt>folder</dt>
 * <dd>The folder id to list the content for</dd>
 * <dt>sort</dt>
 * <dd>(optional) the name of a MessagingField to sort the elements by</dd>
 * <dt>order</dt>
 * <dd>(optional) the sorting direction ('asc' for ascending, 'desc' for descending, defaults to ascending)</dd>
 * <dt>columns</dt>
 * <dd>A comma separated list of MessagingFields that should be loaded.</dd>
 * </dl>
 * Returns a JSONArray containing a JSONArray for every message in the folder. The sub arrays consist of one entry for each requested field.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AllAction extends AbstractMessagingAction {

    private static final DisplayMode DISPLAY_MODE = DisplayMode.RAW;

    public AllAction(final MessagingServiceRegistry registry, final MessagingMessageWriter writer, final MessagingMessageParser parser) {
        super(registry, writer, parser);
    }

    public AllAction(final MessagingServiceRegistry registry, final MessagingMessageWriter writer, final MessagingMessageParser parser, final Cache cache) {
        super(registry, writer, parser, cache);
    }

    @Override
    protected AJAXRequestResult doIt(final MessagingRequestData req, final ServerSession session) throws JSONException, OXException {

        final MessagingMessageAccess access = req.getMessageAccess(session.getUserId(), session.getContextId());

        final MessagingField sort = req.getSort();
        OrderDirection order = null;
        if (sort != null) {
            order = req.getOrder();
            if (order == null) {
                order = OrderDirection.ASC;
            }
        }

        final MessagingField[] columns = req.getColumns();
        final List<MessagingMessage> messages = access.getAllMessages(req.getFolderId(), IndexRange.NULL, sort, order, columns);
        final JSONArray results = new JSONArray();
        for (final MessagingMessage message : messages) {
            final JSONArray line = writer.writeFields(message, columns, req.getAccountAddress(), session, DISPLAY_MODE);
            results.put(line);
        }
        return new AJAXRequestResult(results);

    }

}
