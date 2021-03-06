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

package com.openexchange.share.impl.groupware;

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Autoboxing;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.ModuleAdjuster;
import com.openexchange.share.core.ModuleHandler;
import com.openexchange.share.core.groupware.AdministrativeFolderTargetProxy;
import com.openexchange.share.core.groupware.FolderTargetProxy;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.share.groupware.spi.AccessibleModulesExtension;
import com.openexchange.share.groupware.spi.FolderHandlerModuleExtension;
import com.openexchange.share.impl.osgi.AccessibleModulesExtensionTracker;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.user.User;
import com.openexchange.user.UserExceptionCode;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ModuleSupportImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ModuleSupportImpl implements ModuleSupport {

    private final ServiceLookup services;
    private final ModuleExtensionRegistry<ModuleHandler> handlers;
    private final ModuleExtensionRegistry<ModuleAdjuster> adjusters;
    private final ModuleExtensionRegistry<FolderHandlerModuleExtension> folderExtensions;
    private final ServiceListing<AccessibleModulesExtension> accessibleModulesExtensions;

    /**
     * Initializes a new {@link ModuleSupportImpl}.
     */
    public ModuleSupportImpl(ServiceLookup services, ModuleExtensionRegistry<FolderHandlerModuleExtension> folderExtensions, AccessibleModulesExtensionTracker accessibleModulesExtensions, ModuleExtensionRegistry<ModuleHandler> handlerRegistry, ModuleExtensionRegistry<ModuleAdjuster> adjusterRegistry) {
        super();
        this.services = services;
        this.handlers = handlerRegistry;
        this.adjusters = adjusterRegistry;
        this.folderExtensions = folderExtensions;
        this.accessibleModulesExtensions = accessibleModulesExtensions;
    }

    @Override
    public TargetUpdate prepareUpdate(Session session, Connection writeCon) throws OXException {
        return new TargetUpdateImpl(session, writeCon, services, handlers);
    }

    @Override
    public TargetUpdate prepareAdministrativeUpdate(int contextID, Connection writeCon) throws OXException {
        return new AdministrativeTargetUpdateImpl(services, contextID, writeCon, handlers, folderExtensions);
    }

    @Override
    public TargetProxy load(ShareTarget target, Session session) throws OXException {
        if (target == null) {
            return null;
        }

        if (Module.getForFolderConstant(target.getModule()) == null) {
            return new VirtualTargetProxy(target);
        }

        if (target.isFolder()) {
            UserizedFolder folder = requireService(FolderService.class, services).getFolder(FolderStorage.REAL_TREE_ID, target.getFolderToLoad(), session, null);
            return new FolderTargetProxy(target, folder);
        }

        ModuleHandler moduleHandler = handlers.get(target.getModule());
        return moduleHandler.loadTarget(target, session);
    }

    @Override
    public boolean isVisible(int module, String folder, String item, int contextID, int guestID) throws OXException {
        if (item != null) {
            ModuleHandler moduleHandler = handlers.get(module);
            return moduleHandler.isVisible(folder, item, contextID, guestID);
        }

        if (null == Module.getForFolderConstant(module)) {
            return true;
        }

        FolderHandlerModuleExtension folderHandler = folderExtensions.opt(module);
        if (null != folderHandler && folderHandler.isApplicableFor(contextID, folder)) {
            return folderHandler.isVisible(folder, item, contextID, guestID);
        }

        try {
            UserService userService = requireService(UserService.class, services);
            Context context = userService.getContext(contextID);
            User user = userService.getUser(guestID, context);
            requireService(FolderService.class, services).getFolder(FolderStorage.REAL_TREE_ID, folder, user, context, null);
            return true;
        } catch (OXException e) {
            if (FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.equals(e) || UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean mayAdjust(ShareTarget target, Session session) throws OXException {
        if (null == target) {
            return false;
        }
        if (!target.isFolder()) {
            return handlers.get(target.getModule()).mayAdjust(target, session);
        }

        if (null == Module.getForFolderConstant(target.getModule())) {
            return true;
        }

        try {
            UserizedFolder folder = requireService(FolderService.class, services).getFolder(FolderStorage.REAL_TREE_ID, target.getFolder(), session, null);
            return folder.getOwnPermission().isAdmin();
        } catch (OXException e) {
            if (FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean exists(int module, String folder, String item, int contextID, int guestID) throws OXException {
        if (item != null) {
            return handlers.get(module).exists(folder, item, contextID, guestID);
        }

        if (null == Module.getForFolderConstant(module)) {
            return true;
        }

        FolderHandlerModuleExtension folderHandler = folderExtensions.opt(module);
        if (null != folderHandler && folderHandler.isApplicableFor(contextID, folder)) {
            return folderHandler.exists(folder, item, contextID, guestID);
        }

        try {
            UserService userService = requireService(UserService.class, services);
            Context context = userService.getContext(contextID);
            User user = userService.getUser(guestID, context);
            return (null != requireService(FolderService.class, services).getFolder(FolderStorage.REAL_TREE_ID, folder, user, context, null));
        } catch (OXException e) {
            if (FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.equals(e)) {
                return true;
            }
            if (FolderExceptionErrorMessage.NOT_FOUND.equals(e)) {
                return false;
            }
            if (FolderExceptionErrorMessage.INVALID_FOLDER_ID.equals(e)) {
                return false;
            }
            if (FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public TargetProxy resolveTarget(ShareTargetPath targetPath, int contextId, int guestId) throws OXException {
        FolderHandlerModuleExtension folderHandler = folderExtensions.opt(targetPath.getModule());
        if (null != folderHandler && folderHandler.isApplicableFor(contextId, targetPath.getFolder())) {
            TargetProxy resolvedTarget = folderHandler.resolveTarget(targetPath, contextId, guestId);
            if (null != resolvedTarget) {
                return resolvedTarget;
            }
        }
        TargetProxy proxy = loadAsAdmin(targetPath.getModule(), targetPath.getFolder(), targetPath.getItem(), contextId, guestId);
        return proxy;
    }

    @Override
    public String getShareModule(int moduleId) {
        return ShareModuleMapping.moduleMapping2String(moduleId);
    }

    @Override
    public int getShareModuleId(String module) {
        return ShareModuleMapping.moduleMapping2int(module);
    }

    @Override
    public List<TargetProxy> listTargets(int contextID, int guestID) throws OXException {
        return listTargets(contextID, guestID, Autoboxing.I2i(ShareModuleMapping.getModuleIDs()));
    }

    @Override
    public List<TargetProxy> listTargets(int contextID, int guestID, int module) throws OXException {
        return listTargets(contextID, guestID, new int[] { module });
    }

    @Override
    public Collection<Integer> getAccessibleModules(int contextID, int guestID) throws OXException {
        Set<Integer> accessibleModules = new HashSet<Integer>();
        Context context = requireService(ContextService.class, services).getContext(contextID);
        User user = requireService(UserService.class, services).getUser(guestID, context);
        UserPermissionBits permissionBits = requireService(UserPermissionService.class, services).getUserPermissionBits(guestID, context);
        for (Integer moduleID : ShareModuleMapping.getModuleIDs()) {
            if (OXFolderIteratorSQL.hasVisibleFoldersOfModule(
                user.getId(), user.getGroups(), permissionBits.getAccessibleModules(), Autoboxing.i(moduleID), context, true, null)) {
                accessibleModules.add(moduleID);
            } else {
                ModuleHandler handler = handlers.opt(Autoboxing.i(moduleID));
                if (null != handler && handler.hasTargets(contextID, guestID)) {
                    accessibleModules.add(moduleID);
                }
            }
        }
        for (AccessibleModulesExtension extension : accessibleModulesExtensions.getServiceList()) {
            accessibleModules = extension.extendAccessibleModules(accessibleModules, contextID, guestID);
        }
        return accessibleModules;
    }

    @Override
    public ShareTargetPath getPath(ShareTarget target, Session session) throws OXException {
        if (target.isFolder()) {
            // Currently we don't substitute any folder object IDs, so there is no need to load the according folder.
            return new ShareTargetPath(target.getModule(), target.getFolder(), target.getItem());
        }

        return handlers.get(target.getModule()).getPath(target, session);
    }

    @Override
    public ShareTargetPath getPath(ShareTarget target, int contextID, int guestID) throws OXException {
        if (target.isFolder()) {
            // Currently we don't substitute any folder object IDs, so there is no need to load the according folder.
            return new ShareTargetPath(target.getModule(), target.getFolder(), target.getItem());
        }
        return handlers.get(target.getModule()).getPath(target, contextID, guestID);
    }

    @Override
    public ShareTarget adjustTarget(ShareTarget target, Session session, int targetUserId, Connection connection) throws OXException {
        if (target.isFolder()) {
           ModuleAdjuster adjuster = adjusters.opt(target.getModule());
           if (null == adjuster) {
               return new ShareTarget(target);
           }
           return adjuster.adjustTarget(target, session, targetUserId, connection);
        }
       return handlers.get(target.getModule()).adjustTarget(target, session, targetUserId, connection);
    }

    @Override
    public ShareTarget adjustTarget(ShareTarget target, int contextId, int requestUserId, int targetUserId, Connection connection) throws OXException {
        if (target.isFolder()) {
            ModuleAdjuster adjuster = adjusters.opt(target.getModule());
            if (null == adjuster) {
                return new ShareTarget(target);
            }
            return adjuster.adjustTarget(target, contextId, requestUserId, targetUserId, connection);
        }
        return handlers.get(target.getModule()).adjustTarget(target, contextId, requestUserId, targetUserId, connection);
    }

    @Override
    public boolean isPublic(ShareTarget target, Session session) throws OXException {
        FolderService folderService = requireService(FolderService.class, services);
        UserizedFolder[] path = folderService.getPath(FolderStorage.REAL_TREE_ID, target.getFolder(), session, null).getResponse();
        return FolderTools.isPublicFolder(path);
    }

    private List<TargetProxy> listTargets(int contextID, int guestID, int[] moduleIDs) throws OXException {
        List<TargetProxy> shareTargets = new ArrayList<>();
        Context context = requireService(ContextService.class, services).getContext(contextID);
        User user = requireService(UserService.class, services).getUser(guestID, context);
        UserPermissionBits permissionBits = requireService(UserPermissionService.class, services).getUserPermissionBits(guestID, context);
        for (int moduleID : moduleIDs) {
            shareTargets.addAll(listTargets(context, user, permissionBits, moduleID));
        }
        return shareTargets;
    }

    private List<TargetProxy> listTargets(Context context, User user, UserPermissionBits permissionBits, int moduleID) throws OXException {
        List<TargetProxy> shareTargets = new ArrayList<>();
        /*
         * get available folder targets for the module
         */
        SearchIterator<FolderObject> searchIterator = null;
        try {
            searchIterator = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(user.getId(), user.getGroups(), permissionBits.getAccessibleModules(), moduleID, context);
            while (searchIterator.hasNext()) {
                FolderObject folder = searchIterator.next();
                if (FolderObject.SYSTEM_TYPE == folder.getType() || FolderObject.MIN_FOLDER_ID > folder.getObjectID()) {
                    continue;
                }
                /*
                 * Filter out system permissions
                 */
                boolean canRead = false;
                for (OCLPermission p : folder.getPermissions()) {
                    if (!p.isGroupPermission() && !p.isSystem() && p.getEntity() == user.getId() && (p.canReadOwnObjects() || p.canReadAllObjects())) {
                        canRead = true;
                        break;
                    }
                }
                if (canRead) {
                    shareTargets.add(new AdministrativeFolderTargetProxy(folder));
                }
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        /*
         * get targets from a registered module handler, too
         */
        ModuleHandler handler = handlers.opt(moduleID);
        if (null != handler) {
            shareTargets.addAll(handler.listTargets(context.getContextId(), user.getId()));
        }
        return shareTargets;
    }

    private TargetProxy loadAsAdmin(int module, String folderId, String item, int contextID, int guestID) throws OXException {
        Context context = services.getService(ContextService.class).getContext(contextID);
        if (Module.getForFolderConstant(module) == null) {
            return new VirtualTargetProxy(module, folderId, item, "virtual");
        }

        int folderID;
        try {
            folderID = Integer.parseInt(folderId);
        } catch (NumberFormatException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

        OXFolderAccess folderAccess = new OXFolderAccess(context);
        FolderObject folder = folderAccess.getFolderObject(folderID);
        return item == null ? new AdministrativeFolderTargetProxy(folder) : handlers.get(module).loadTarget(folderId, item, context, guestID);
    }

}
