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
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.impl.groupware.ShareModuleMapping;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult;
import com.openexchange.share.subscription.ShareLinkAnalyzeResult.Builder;
import com.openexchange.share.subscription.ShareLinkState;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.share.subscription.ShareSubscriptionInformation;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.tools.arrays.Collections;
import com.openexchange.tools.oxfolder.property.FolderSubscriptionHelper;

/**
 * {@link ContextInternalSubscriptionProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ContextInternalSubscriptionProvider implements ShareSubscriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextInternalSubscriptionProvider.class);

    private final static String PUBLIC_INFOSTORE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
    private final static String USER_INFOSTORE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);

    /** The identifiers of the folders that are not allowed to unsubscribe from */
    private static final Set<String> PARENT_FOLDER = Collections.unmodifiableSet(USER_INFOSTORE_FOLDER_ID, PUBLIC_INFOSTORE_FOLDER_ID);

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
        try {
            return null != ShareLinks.parseInternal(shareLink);
        } catch (OXException e) {
            LOGGER.debug("Unable to parse link", e);
        }
        return false;
    }

    @Override
    public ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException {
        ShareTarget target = ShareLinks.parseInternal(shareLink);
        if (Strings.isNotEmpty(target.getItem())) {
            return new ShareLinkAnalyzeResult(ShareLinkState.UNSUPPORTED, ShareSubscriptionExceptions.NOT_USABLE.create(shareLink), null);
        }
        /*
         * Access folder
         */
        FolderService folderService = services.getServiceSafe(FolderService.class);
        String folderId = target.getFolder();
        try {
            UserizedFolder folder = folderService.getFolder(String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID), folderId, session, null);
            folder = getRootFolder(session, folderService, folder);

            Builder builder = new Builder();
            String moduleName = getModuleName(folder.getContentType().getModule());
            builder.infos(new ShareSubscriptionInformation(folder.getAccountID(), moduleName, folderId));
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
        /*
         * Get the folder to subscribe
         */
        ShareTarget target = ShareLinks.parseInternal(shareLink);
        if (Strings.isNotEmpty(target.getItem())) {
            throw ShareExceptionCodes.INVALID_LINK.create(shareLink);
        }
        
        FolderService folderService = services.getServiceSafe(FolderService.class);
        String folderId = target.getFolder();
        UserizedFolder folder = folderService.getFolder(String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID), folderId, session, null);
        int module = folder.getContentType().getModule();
        if (module != target.getModule()) {
            throw ShareExceptionCodes.INVALID_LINK.create(shareLink);
        }
        String moduleName = getModuleName(module);

        /*
         * Check if already subscribed
         */
        if (folder.isSubscribed()) {
            return new ShareSubscriptionInformation(folder.getAccountID(), moduleName, folderId);
        }
        /*
         * Check if subscribe is allowed
         */
        folder = getRootFolder(session, folderService, folder);
        FolderSubscriptionHelper helper = services.getServiceSafe(FolderSubscriptionHelper.class);
        if (false == isSubscribable(helper, session.getUserId(), folder)) {
            throw ShareExceptionCodes.NO_SUBSCRIBE_PERMISSION.create(folderId);
        }
        /*
         * Subscribe to folder
         */
        try {
            int fid = Integer.parseInt(folder.getID());
            helper.setSubscribed(Optional.empty(), session.getContextId(), session.getUserId(), fid, folder.getContentType().getModule(), true);
        } catch (NumberFormatException e) {
            throw ShareSubscriptionExceptions.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }

        return new ShareSubscriptionInformation(folder.getAccountID(), moduleName, folderId);
    }

    @Override
    public ShareSubscriptionInformation resubscribe(Session session, String shareLink, String shareName, String password) throws OXException {
        throw ShareSubscriptionExceptions.MISSING_PERMISSIONS.create();
    }

    @Override
    public boolean unsubscribe(Session session, String shareLink) throws OXException {
        /*
         * Get the folder to subscribe
         */
        ShareTarget target = ShareLinks.parseInternal(shareLink);
        if (Strings.isNotEmpty(target.getItem())) {
            return false;
        }
        
        FolderService folderService = services.getServiceSafe(FolderService.class);
        String folderId = target.getFolder();
        UserizedFolder folder = folderService.getFolder(String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID), folderId, session, null);
        int module = folder.getContentType().getModule();
        if (module != target.getModule()) {
            return false;
        }
        /*
         * Check if folder can be unsubscribed
         */
        FolderSubscriptionHelper helper = services.getServiceSafe(FolderSubscriptionHelper.class);
        if (false == folder.isSubscribed()) {
            return true;
        }
        /*
         * Check if unsubscribe is allowed
         */
        folder = getRootFolder(session, folderService, folder);
        if (false == isSubscribable(helper, session.getUserId(), folder)) {
            throw ShareExceptionCodes.NO_UNSUBSCRIBE_FOLDER.create(folderId);
        }
        /*
         * Unsubscribe to folder
         */
        try {
            int fid = Integer.parseInt(folder.getID());
            helper.setSubscribed(Optional.empty(), session.getContextId(), session.getUserId(), fid, folder.getContentType().getModule(), false);
        } catch (NumberFormatException e) {
            throw ShareSubscriptionExceptions.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }
        return true;
    }

    /*
     * ============================== HELPERS ==============================
     */

    /**
     * Gets a value indicating whether the given folder can be subscribed or not
     *
     * @param subscriptionHelper The helper
     * @param userId The user identifier
     * @param folder The folder to check
     * @return <code>true</code> if the folder can be subscribe, <code>false</code> otherwise
     * @throws OXException
     */
    private static boolean isSubscribable(FolderSubscriptionHelper subscriptionHelper, int userId, UserizedFolder folder) throws OXException {
        int module = folder.getContentType().getModule();
        if (false == subscriptionHelper.isSubscribableModule(module)) {
            return false;
        }
        if (FolderObject.INFOSTORE == module) {
            if (PUBLIC_INFOSTORE_FOLDER_ID.equals(folder.getParentID())) {
                return true;
            }
            if (USER_INFOSTORE_FOLDER_ID.equals(folder.getParentID())) {
                return userId != folder.getCreatedBy();
            }
            return false;
        }
        if (folder.isDefault() && PrivateType.getInstance().equals(folder.getType())) {
            return false;
        }
        return true;
    }

    /**
     * Gets the correct folder to un-/subscribe to. This is the folder with either the {@link #USER_INFOSTORE_FOLDER_ID} or
     * {@link #PUBLIC_INFOSTORE_FOLDER_ID} as parent
     *
     * @param session The session
     * @param folderService The folder service
     * @param folder The folder to get correct parent for
     * @return The folder or a parent folder to un-/subscribe to
     * @throws OXException If path to parent can't be resolved
     */
    private UserizedFolder getRootFolder(Session session, FolderService folderService, UserizedFolder folder) throws OXException {
        if (PARENT_FOLDER.contains(folder.getParentID())) {
            return folder;
        }
        for (UserizedFolder f : folderService.getPath(folder.getTreeID(), folder.getID(), session, null).getResponse()) {
            if (PARENT_FOLDER.contains(f.getParentID())) {
                return f;
            }
        }
        return folder;
    }

    /**
     * Get the correct module name
     *
     * @param module The module
     * @return The name of the module
     */
    private String getModuleName(int module) {
        return ShareModuleMapping.moduleMapping2String(module);
    }

}
