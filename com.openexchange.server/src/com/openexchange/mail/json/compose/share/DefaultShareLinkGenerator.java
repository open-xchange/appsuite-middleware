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

package com.openexchange.mail.json.compose.share;

import java.util.Collections;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Strings;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.share.internal.ShareComposeLinkImpl;
import com.openexchange.mail.json.compose.share.spi.ShareLinkGenerator;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.Links;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DefaultShareLinkGenerator} - The default share link generator having no recipient-specific additionals.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DefaultShareLinkGenerator implements ShareLinkGenerator {

    private static final DefaultShareLinkGenerator INSTANCE = new DefaultShareLinkGenerator();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static DefaultShareLinkGenerator getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DefaultShareLinkGenerator}.
     */
    protected DefaultShareLinkGenerator() {
        super();
    }

    /**
     * Gets optional additional information that is supposed to be added to recipient's share link.
     * <p>
     * Serves as a hook for sub-classes.
     *
     * @param recipient The recipient to which the share link is ought to be sent
     * @param guest The guest
     * @param sourceTarget The share target for the folder
     * @param session The associated session
     * @return The optional additional information
     * @throws OXException If an error occurs while attempting to return optional additional information
     */
    protected Map<String, String> getAdditionals(Recipient recipient, GuestInfo guest, ShareTarget sourceTarget, Session session) throws OXException {
        return Collections.emptyMap();
    }

    @Override
    public boolean applicableFor(ComposeRequest composeRequest) {
        return true;
    }

    @Override
    public ShareComposeLink generateShareLink(Recipient recipient, GuestInfo guest, ShareTarget sourceTarget, HostData hostData, String queryString, Session session) throws OXException {
        // Generate share target path
        ShareTargetPath targetPath;
        {
            Map<String, String> additionals = getAdditionals(recipient, guest, sourceTarget, session);
            if (null == additionals || additionals.isEmpty()) {
                targetPath = new ShareTargetPath(sourceTarget.getModule(), sourceTarget.getFolder(), sourceTarget.getItem());
            } else {
                targetPath = new ShareTargetPath(sourceTarget.getModule(), sourceTarget.getFolder(), sourceTarget.getItem(), additionals);
            }
        }

        // Generate associated share link
        String url = guest.generateLink(hostData, targetPath);
        if (Strings.isNotEmpty(queryString)) {
            url = new StringBuilder(url).append('?').append(queryString).toString();
        }

        return new ShareComposeLinkImpl(null, url, "link-created");
    }

    @Override
    public ShareComposeLink generatePersonalShareLink(ShareTarget shareTarget, HostData hostData, String queryString, ServerSession session) {
        String module = Module.getForFolderConstant(shareTarget.getModule()).getName();
        String url = Links.generateInternalLink(module, shareTarget.getFolder(), shareTarget.getItem(), hostData);
        if (Strings.isNotEmpty(queryString)) {
            url = new StringBuilder(url).append('?').append(queryString).toString();
        }

        return new ShareComposeLinkImpl(null, url, "link-created");
    }

}
