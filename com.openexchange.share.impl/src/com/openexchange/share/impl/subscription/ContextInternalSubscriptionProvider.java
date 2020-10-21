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

package com.openexchange.share.impl.subscription;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.share.subscription.ShareLinkState.INACCESSIBLE;
import static com.openexchange.share.subscription.ShareLinkState.SUBSCRIBED;
import static com.openexchange.share.subscription.ShareLinkState.UNSUBSCRIBED;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.Links;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult.Builder;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.share.subscription.ShareSubscriptionProvider;

/**
 * {@link ContextInternalSubscriptionProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ContextInternalSubscriptionProvider implements ShareSubscriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextInternalSubscriptionProvider.class);

    private static final String FOLDER = "folder";
    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContextInternalSubscriptionProvider}.
     * 
     * @param services The service lookup
     */
    public ContextInternalSubscriptionProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public int getRanking() {
        return 100;
    }

    @Override
    public boolean isSupported(Session session, String shareLink) {
        if (Strings.isEmpty(shareLink)) {
            return false;
        }
        return shareLink.contains(Links.PATH) && shareLink.contains(Links.FRAGMENT_APP) && shareLink.contains(FOLDER);
    }

    @Override
    public ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException {
        /*
         * Get folder ID from link
         */
        String folderId = null;
        URL url = getUrl(shareLink);
        String ref = url.getRef();
        if (Strings.isEmpty(ref)) {
            return incaccessible(shareLink);
        }
        if (ref.startsWith(Links.FRAGMENT_APP)) {
            ref = ref.substring(Links.FRAGMENT_APP.length());
        }
        for (String param : ref.split("&")) {
            if (Strings.isNotEmpty(param) && param.startsWith(FOLDER)) {
                String[] pair = param.split("=");
                if (null != pair && pair.length == 2) {
                    folderId = pair[1];
                }
                break;
            }
        }
        if (Strings.isEmpty(folderId)) {
            return incaccessible(shareLink);
        }

        /*
         * Access folder
         */
        FolderService folderService = services.getServiceSafe(FolderService.class);
        try {
            UserizedFolder folder = folderService.getFolder(String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID), folderId, session, null);
            int module = folder.getContentType().getModule();

            Builder builder = new Builder();
            builder.infos(new ShareSubscriptionInformation(folder.getAccountID(), Module.getForFolderConstant(module).getName(), folderId));
            if (folder.isSubscribed()) {
                builder.state(SUBSCRIBED);
            } else {
                builder.state(UNSUBSCRIBED).error(ShareSubscriptionExceptions.UNSUBSCRIEBED_FOLDER.create(folder.getID()));
            }
            return builder.build();
        } catch (OXException e) {
            LOGGER.debug("Unable to access the folder", e);
            return new ShareLinkAnalyzeResult(INACCESSIBLE, ShareExceptionCodes.NO_SHARE_PERMISSIONS.create(I(session.getUserId()), folderId, I(session.getContextId()), e), null);
        }
    }

    @Override
    public ShareSubscriptionInformation subscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        throw ShareSubscriptionExceptions.MISSING_PERMISSIONS.create();
    }

    @Override
    public ShareSubscriptionInformation remount(Session session, String shareLink, String shareName, String password) throws OXException {
        throw ShareSubscriptionExceptions.MISSING_PERMISSIONS.create();
    }

    @Override
    public boolean unsubscribe(Session session, String shareLink) throws OXException {
        return false;
    }

    /**
     * Transforms the string into an {@link URL}
     *
     * @param url The URL to transform
     * @return A {@link URL} or <code>null</code>
     * @throws OXException In case of invalid URL
     */
    private static URL getUrl(String url) throws OXException {
        try {
            if (false == url.startsWith("http")) { // includes 'https'
                return new URL("https://" + url);
            }
            return new URL(url);
        } catch (MalformedURLException e) {
            throw ShareSubscriptionExceptions.NOT_USABLE.create(url, e);
        }
    }

    private static ShareLinkAnalyzeResult incaccessible(String link) {
        return new ShareLinkAnalyzeResult(INACCESSIBLE, ShareExceptionCodes.INVALID_LINK.create(link), null);
    }

}
