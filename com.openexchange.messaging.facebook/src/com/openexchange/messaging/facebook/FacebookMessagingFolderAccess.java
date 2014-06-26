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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.DefaultMessagingFolder;
import com.openexchange.messaging.DefaultMessagingPermission;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.Quota;
import com.openexchange.messaging.Quota.Type;
import com.openexchange.messaging.facebook.session.FacebookOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link FacebookMessagingFolderAccess} - The Facebook folder access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class FacebookMessagingFolderAccess extends AbstractFacebookAccess implements MessagingFolderAccess {

    private static interface FolderInfo {

        public MessagingFolder generateFolder(FacebookMessagingFolderAccess folderAccess) throws OXException;

        public MessagingFolder[] getSubfolders(FacebookMessagingFolderAccess folderAccess) throws OXException;

        public MessagingFolder[] getPath(FacebookMessagingFolderAccess folderAccess) throws OXException;
    }

    private static final Map<String, FolderInfo> FOLDER_INFO_MAP;

    static {
        final Map<String, FolderInfo> m = new HashMap<String, FolderInfo>(2);

        m.put(MessagingFolder.ROOT_FULLNAME, new FolderInfo() {

            @Override
            public MessagingFolder generateFolder(final FacebookMessagingFolderAccess folderAccess) throws OXException {
                return folderAccess.getRootFolder();
            }

            @Override
            public MessagingFolder[] getSubfolders(final FacebookMessagingFolderAccess folderAccess) throws OXException {
                return new MessagingFolder[] { folderAccess.generateWallFolder() };
            }

            @Override
            public MessagingFolder[] getPath(final FacebookMessagingFolderAccess folderAccess) throws OXException {
                return EMPTY_PATH;
            }
        });
        m.put(FacebookConstants.FOLDER_WALL, new FolderInfo() {

            @Override
            public MessagingFolder generateFolder(final FacebookMessagingFolderAccess folderAccess) throws OXException {
                return folderAccess.generateWallFolder();
            }

            @Override
            public MessagingFolder[] getSubfolders(final FacebookMessagingFolderAccess folderAccess) throws OXException {
                return EMPTY_PATH;
            }

            @Override
            public MessagingFolder[] getPath(final FacebookMessagingFolderAccess folderAccess) throws OXException {
                return new MessagingFolder[] { folderAccess.generateWallFolder() };
            }
        });
        FOLDER_INFO_MAP = Collections.unmodifiableMap(m);
    }

    /**
     * The constant to return or represent an empty path.
     */
    protected static final MessagingFolder[] EMPTY_PATH = new MessagingFolder[0];

    /**
     * Initializes a new {@link FacebookMessagingFolderAccess}.
     *
     * @param facebookOAuthAccess The Facebook OAuth access
     * @param messagingAccount The Facebook messaging account
     * @param session The associated session
     */
    public FacebookMessagingFolderAccess(final FacebookOAuthAccess facebookOAuthAccess, final MessagingAccount messagingAccount, final Session session) {
        super(facebookOAuthAccess, messagingAccount, session);
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    @Override
    public String createFolder(final MessagingFolder toCreate) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        return (KNOWN_FOLDER_IDS.contains(folderId));
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return null;
    }

    @Override
    public MessagingFolder getFolder(final String folderId) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return FOLDER_INFO_MAP.get(folderId).generateFolder(this);
    }

    @Override
    public Quota getMessageQuota(final String folderId) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return Quota.getUnlimitedQuota(Quota.Type.MESSAGE);

    }

    @Override
    public MessagingFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return FOLDER_INFO_MAP.get(folderId).getPath(this);
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folder)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folder,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return Quota.getUnlimitedQuotas(types);
    }

    private static final Set<String> CAPS = Collections.emptySet();

    @Override
    public MessagingFolder getRootFolder() throws OXException {
        return generateRootFolder();
    }

    private MessagingFolder generateRootFolder() throws OXException {
        try {
            /*
             * The collection of users
             */
            final int wallCount =
                performFQLQuery(new StringBuilder("SELECT wall_count FROM user WHERE uid = ").append(facebookUserId).toString()).getInt(
                    "wall_count");
            final DefaultMessagingFolder rootFolder = generateFolder(MessagingFolder.ROOT_FULLNAME, null, "Facebook", wallCount);
            rootFolder.setHoldsFolders(true);
            rootFolder.setSubfolders(true);
            rootFolder.setSubscribedSubfolders(true);
            return rootFolder;
        } catch (final JSONException e) {
            throw FacebookMessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static final int FQL_MAX_ROW_COUNT = 50;

    protected MessagingFolder generateWallFolder() throws OXException {
        final DefaultMessagingFolder wallFolder =
            generateFolder(
                FacebookConstants.FOLDER_WALL,
                MessagingFolder.ROOT_FULLNAME,
                I18n.getInstance().translate(getUserLocale(), NameStrings.NAME_WALL_FOLDER),
                FQL_MAX_ROW_COUNT);
        wallFolder.setRootFolder(false);
        return wallFolder;
    }

    private DefaultMessagingFolder generateFolder(final String folderId, final String parentId, final String name, final int messageCount) {
        final DefaultMessagingFolder dmf = new DefaultMessagingFolder();
        dmf.setId(folderId);
        dmf.setCapabilities(CAPS);
        dmf.setDefaultFolder(false);
        dmf.setDefaultFolderType(MessagingFolder.DefaultFolderType.NONE);
        dmf.setDeletedMessageCount(0);
        dmf.setExists(true);
        dmf.setHoldsFolders(false);
        dmf.setHoldsMessages(true);
        dmf.setMessageCount(messageCount);
        dmf.setName(name);
        dmf.setNewMessageCount(0);

        final DefaultMessagingPermission perm = DefaultMessagingPermission.newInstance();
        perm.setAdmin(false);
        final int[] arr = FacebookMessagingService.getStaticRootPerms();
        perm.setAllPermissions(arr[0], arr[1], arr[2], arr[3]);
        perm.setEntity(user);
        perm.setGroup(false);

        dmf.setOwnPermission(perm);
        dmf.setParentId(parentId);
        {
            final List<MessagingPermission> perms = new ArrayList<MessagingPermission>(1);
            perms.add(perm);
            dmf.setPermissions(perms);
        }
        dmf.setRootFolder(true);
        dmf.setSubfolders(false);
        dmf.setSubscribed(true);
        dmf.setSubscribedSubfolders(false);
        dmf.setUnreadMessageCount(0);
        return dmf;
    }

    @Override
    public String getSentFolder() throws OXException {
        return null;
    }

    @Override
    public String getSpamFolder() throws OXException {
        return null;
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return Quota.getUnlimitedQuota(Quota.Type.STORAGE);
    }

    @Override
    public MessagingFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(parentIdentifier)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                parentIdentifier,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        return FOLDER_INFO_MAP.get(parentIdentifier).getSubfolders(this);
    }

    @Override
    public String getTrashFolder() throws OXException {
        return null;
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

    @Override
    public String updateFolder(final String folderId, final MessagingFolder toUpdate) throws OXException {
        if (!KNOWN_FOLDER_IDS.contains(folderId)) {
            throw MessagingExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId,
                Integer.valueOf(id),
                FacebookMessagingService.getServiceId(),
                Integer.valueOf(user),
                Integer.valueOf(cid));
        }
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(FacebookMessagingService.getServiceId());
    }

}
