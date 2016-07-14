/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mail.json.compose.share.internal;

import com.openexchange.exception.OXException;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.share.DefaultShareLinkGenerator;
import com.openexchange.mail.json.compose.share.ShareComposeLink;
import com.openexchange.mail.json.compose.share.Recipient;
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
