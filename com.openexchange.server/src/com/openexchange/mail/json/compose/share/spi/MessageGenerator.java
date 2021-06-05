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

package com.openexchange.mail.json.compose.share.spi;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.Applicable;
import com.openexchange.mail.json.compose.share.ShareComposeMessageInfo;
import com.openexchange.mail.json.compose.share.ShareReference;

/**
 * {@link MessageGenerator} - Generates appropriate compose messages for internal/external recipients.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface MessageGenerator extends Applicable {

    /**
     * Generates the transport messages for given attributes.
     *
     * @param info The message info providing the link and target recipients
     * @param shareReference The share reference
     * @param cidMapping Maps file ids to previews' content ids
     * @param moreFiles 
     * @return The generated messages
     * @throws OXException If messages cannot be generated
     */
    List<ComposedMailMessage> generateTransportMessagesFor(ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> cidMapping, int moreFiles) throws OXException;

    /**
     * Generates the messages, that is supposed to be added to standard sent folder.
     *
     * @param info The message info providing the link and user's recipient
     * @param shareReference The share reference to apply
     * @param cidMapping Maps file ids to previews' content ids
     * @param moreFiles 
     * @return The generated message
     * @throws OXException If message cannot be generated
     */
    ComposedMailMessage generateSentMessageFor(ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> cidMapping, int moreFiles) throws OXException;

}
