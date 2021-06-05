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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.mail.json.compose.Applicable;
import com.openexchange.mail.json.compose.share.Recipient;
import com.openexchange.mail.json.compose.share.ShareComposeLink;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ShareLinkGenerator} - An interface that determines how a link to a shared folder is supposed to be generated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface ShareLinkGenerator extends Applicable {

    /**
     * Generates the share link for specified recipient.
     *
     * @param recipient The recipient
     * @param guest The guest
     * @param sourceTarget The share target to the folder
     * @param hostData The associated host data
     * @param queryString The optional query string or <code>null</code>
     * @param session The associated session
     * @return The share link
     * @throws OXException If generating the share link fails
     */
    ShareComposeLink generateShareLink(Recipient recipient, GuestInfo guest, ShareTarget sourceTarget, HostData hostData, String queryString, Session session) throws OXException;

    /**
     * Generates the personal share link for specified session's user.
     *
     * @param shareTarget The share target
     * @param hostData The host data
     * @param queryString The optional query string or <code>null</code>
     * @param session The associated session
     * @return The share link
     * @throws OXException If generating the share link fails
     */
    ShareComposeLink generatePersonalShareLink(ShareTarget shareTarget, HostData hostData, String queryString, ServerSession session);
}
