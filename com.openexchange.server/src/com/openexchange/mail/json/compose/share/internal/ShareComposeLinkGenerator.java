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

package com.openexchange.mail.json.compose.share.internal;

import com.openexchange.exception.OXException;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.share.DefaultShareLinkGenerator;
import com.openexchange.mail.json.compose.share.Recipient;
import com.openexchange.mail.json.compose.share.ShareComposeLink;
import com.openexchange.mail.json.compose.share.spi.ShareLinkGenerator;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareTarget;

/**
 * {@link ShareComposeLinkGenerator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeLinkGenerator {

    private static final ShareComposeLinkGenerator INSTANCE = new ShareComposeLinkGenerator();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ShareComposeLinkGenerator getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ShareComposeLinkGenerator}.
     */
    private ShareComposeLinkGenerator() {
        super();
    }

    /**
     * Creates a share link for specified share information.
     *
     * @param recipient The recipient
     * @param shareTarget The share target
     * @param guest The guest
     * @param queryString The optional query string or <code>null</code>
     * @param composeRequest The compose request
     * @return The link
     * @throws OXException If link cannot be returned
     */
    public ShareComposeLink createShareLink(Recipient recipient, ShareTarget shareTarget, GuestInfo guest, String queryString, ComposeRequest composeRequest) throws OXException {
        ShareLinkGeneratorRegistry registry = ServerServiceRegistry.getInstance().getService(ShareLinkGeneratorRegistry.class);
        if (null == registry) {
            return DefaultShareLinkGenerator.getInstance().generateShareLink(recipient, guest, shareTarget, composeRequest.getRequest().getHostData(), queryString, composeRequest.getSession());
        }

        ShareLinkGenerator generator = registry.getShareLinkGeneratorFor(composeRequest);
        return generator.generateShareLink(recipient, guest, shareTarget, composeRequest.getRequest().getHostData(), queryString, composeRequest.getSession());
    }

    /**
     * Creates the personal share link for specified share target.
     *
     * @param shareTarget The share target
     * @param queryString The optional query string or <code>null</code>
     * @param composeRequest The associated compose request
     * @return The link
     * @throws OXException If link cannot be returned
     */
    public ShareComposeLink createPersonalShareLink(ShareTarget shareTarget, String queryString, ComposeRequest composeRequest) throws OXException {
        ShareLinkGeneratorRegistry registry = ServerServiceRegistry.getInstance().getService(ShareLinkGeneratorRegistry.class);
        if (null == registry) {
            return DefaultShareLinkGenerator.getInstance().generatePersonalShareLink(shareTarget, composeRequest.getRequest().getHostData(), queryString, composeRequest.getSession());
        }

        ShareLinkGenerator generator = registry.getShareLinkGeneratorFor(composeRequest);
        return generator.generatePersonalShareLink(shareTarget, composeRequest.getRequest().getHostData(), queryString, composeRequest.getSession());
    }

}
