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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.caching.Cache;
import com.openexchange.exception.OXException;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Loads a set of requested messages. Parameters are:
 * <dl>
 * <dt>messagingService</dt>
 * <dd>The messaging service id</dd>
 * <dt>account</dt>
 * <dd>The id of the messaging account</dd>
 * <dt>folder</dt>
 * <dd>The folder id to list the content for</dd>
 * <dt>columns</dt>
 * <dd>A comma separated list of MessagingFields that should be loaded.</dd>
 * </dl>
 * The body of the request must contain a JSONArray with the message IDs that are to be loaded. Returns a JSONArray containing a JSONArray
 * for every message that is to be loaded. The sub arrays consist of one entry for each requested field.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */

public class ListAction extends AbstractMessagingAction {

    private static final DisplayMode DISPLAY_MODE = DisplayMode.RAW;

    public ListAction(final MessagingServiceRegistry registry, final MessagingMessageWriter writer, final MessagingMessageParser parser) {
        super(registry, writer, parser);
    }

    public ListAction(final MessagingServiceRegistry registry, final MessagingMessageWriter writer, final MessagingMessageParser parser, final Cache cache) {
        super(registry, writer, parser, cache);
    }

    @Override
    protected AJAXRequestResult doIt(final MessagingRequestData req, final ServerSession session) throws JSONException, OXException {
        final MessagingField[] fields = req.getColumns();

        MessagingFolderAddress folder = null;
        final List<String> ids = new ArrayList<String>();
        for (final MessageAddress address : req.getMessageAddresses()) {
            if (folder == null) {
                folder = address.getLongFolder();
                ids.add(address.getId());
            } else if (folder.equals(address.getLongFolder())) {
                ids.add(address.getId());
            } else {
                throw MessagingExceptionCodes.INVALID_PARAMETER.create("folder", address.getLongFolder().toString());
            }
        }

        if (folder == null) {
            return new AJAXRequestResult(new JSONArray());
        }

        final MessagingMessageAccess messageAccess =
            req.getMessageAccess(folder.getMessagingService(), folder.getAccount(), session.getUserId(), session.getContextId());
        final List<MessagingMessage> messages = messageAccess.getMessages(folder.getFolder(), ids.toArray(new String[ids.size()]), fields);

        final JSONArray list = new JSONArray();
        for (final MessagingMessage messagingMessage : messages) {
            if (null != messagingMessage) {
                list.put(writer.writeFields(messagingMessage, fields, folder.getAccountAddress(), session, DISPLAY_MODE));
            }
        }

        return new AJAXRequestResult(list);
    }

}
