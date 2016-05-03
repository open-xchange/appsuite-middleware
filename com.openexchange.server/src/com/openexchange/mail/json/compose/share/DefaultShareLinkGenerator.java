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
        if (!Strings.isEmpty(queryString)) {
            url = new StringBuilder(url).append('?').append(queryString).toString();
        }

        return new ShareComposeLinkImpl(null, url, "link-created");
    }

    @Override
    public ShareComposeLink generatePersonalShareLink(ShareTarget shareTarget, HostData hostData, String queryString, ServerSession session) {
        String module = Module.getForFolderConstant(shareTarget.getModule()).getName();
        String url = Links.generateInternalLink(module, shareTarget.getFolder(), shareTarget.getItem(), hostData);
        if (!Strings.isEmpty(queryString)) {
            url = new StringBuilder(url).append('?').append(queryString).toString();
        }

        return new ShareComposeLinkImpl(null, url, "link-created");
    }

}
