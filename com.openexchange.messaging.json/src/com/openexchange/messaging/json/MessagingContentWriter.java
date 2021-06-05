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

package com.openexchange.messaging.json;

import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * A MessagingContentWriter feels responsible for certain MessagingContents and can turn them into a JSON representation. By implementing
 * a MessagingContentWriter (and correspondingly a {@link MessagingContentParser} and registering it with a {@link MessagingMessageParser},
 * one can add special handling for special messaging contents.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessagingContentWriter {

    /**
     * Returns true if this content writer feels responsible for the messaging content. May orient itself along
     * the content type header in the part or the class of the content.
     */
    public boolean handles(MessagingPart part, MessagingContent content);

    /**
     * When multiple content writers feel responsible for a certain content the one with the highest ranking is used.
     */
    public int getRanking();

    /**
     * Turns the messaging content into its JSON representation
     */
    public Object write(MessagingPart part, MessagingContent content, ServerSession session, DisplayMode mode) throws OXException, JSONException;
}
