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

package com.openexchange.chronos.provider.internal.share;

import static com.openexchange.chronos.provider.internal.Constants.QUALIFIED_ACCOUNT_ID;
import static com.openexchange.chronos.provider.internal.share.IDMangling.getRelativeFolderId;
import static com.openexchange.chronos.provider.internal.share.IDMangling.getUniqueFolderId;
import static com.openexchange.chronos.provider.internal.share.IDMangling.optRelativeFolderId;
import static com.openexchange.osgi.Tools.requireService;
import java.util.Collection;
import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.groupware.AdministrativeFolderTargetProxy;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.spi.FolderHandlerModuleExtension;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link CalendarFolderHandlerModuleExtension}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarFolderHandlerModuleExtension implements FolderHandlerModuleExtension {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarFolderHandlerModuleExtension}.
     *
     * @param services A service lookup reference
     */
    public CalendarFolderHandlerModuleExtension(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Collection<String> getModules() {
        return Collections.singleton(Module.CALENDAR.getName());
    }

    @Override
    public boolean isApplicableFor(int contextId, String folder) {
        return null != folder && (folder.startsWith(QUALIFIED_ACCOUNT_ID) || null != optRelativeFolderId(folder));
    }

    @Override
    public boolean isVisible(String folder, String item, int contextID, int guestID) throws OXException {
        if (null != item) {
            return false;
        }
        try {
            getFolder(getRelativeFolderId(folder), contextID, guestID);
            return true;
        } catch (OXException e) {
            if (FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean exists(String folder, String item, int contextID, int guestID) throws OXException {
        if (null != item) {
            return false;
        }
        try {
            return null != getFolder(getRelativeFolderId(folder), contextID, guestID);
        } catch (OXException e) {
            if (FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.equals(e)) {
                return true;
            }
            if (FolderExceptionErrorMessage.NOT_FOUND.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public TargetProxy resolveTarget(ShareTargetPath targetPath, int contextId, int guestId) throws OXException {
        if (null != targetPath.getItem()) {
            return null;
        }
        String folderId = targetPath.getFolder();
        ShareTarget target = new ShareTarget(targetPath.getModule(), getUniqueFolderId(folderId), getRelativeFolderId(folderId), targetPath.getItem());
        FolderObject folder = getFolderObject(folderId, contextId);
        return new AdministrativeFolderTargetProxy(folder, target);
    }

    @Override
    public TargetProxy resolveTarget(ShareTarget folderTarget, int contextId) throws OXException {
        if (null != folderTarget.getItem()) {
            return null;
        }
        String folderId = folderTarget.getFolder();
        ShareTarget target = new ShareTarget(folderTarget.getModule(), getUniqueFolderId(folderId), getRelativeFolderId(folderId), folderTarget.getItem());
        FolderObject folder = getFolderObject(folderId, contextId);
        return new AdministrativeFolderTargetProxy(folder, target);
    }

    private UserizedFolder getFolder(String folderId, int contextId, int guestId) throws OXException {
        UserService userService = requireService(UserService.class, services);
        Context context = userService.getContext(contextId);
        User user = userService.getUser(guestId, context);
        return requireService(FolderService.class, services).getFolder(FolderStorage.REAL_TREE_ID, getRelativeFolderId(folderId), user, context, null);
    }

    private FolderObject getFolderObject(String folderId, int contextId) throws OXException {
        Context context = requireService(UserService.class, services).getContext(contextId);
        int numericalId;
        try {
            numericalId = Integer.parseInt(getRelativeFolderId(folderId));
        } catch (NumberFormatException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return new OXFolderAccess(context).getFolderObject(numericalId);
    }

}
