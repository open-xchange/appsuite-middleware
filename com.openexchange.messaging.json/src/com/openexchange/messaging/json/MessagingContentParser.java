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

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingContent;


/**
 * A MessagingContentParser feels responsible for certain JSON structures and can turn them into a valid MessagingContent. By implementing
 * a MessagingContentParser (and correspondingly a {@link MessagingContentWriter} and registering it with a {@link MessagingMessageParser},
 * one can add special handling for special messaging contents.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessagingContentParser {
    /**
     * If multiple parsers feel responsible for a certain content object, the one with the highest ranking wins.
     */
    public int getRanking();

    /**
     * Determine whether this parser can handle the given JSON representation of a messaging content. Will usually orient
     * itself along the (already parsed) content type in the given message.
     */
    public boolean handles(MessagingBodyPart message, Object content) throws OXException;

    /**
     * Turns the given content into a messaging content.
     */
    public MessagingContent parse(MessagingBodyPart message, Object content, MessagingInputStreamRegistry registry) throws JSONException, OXException, IOException;
}
