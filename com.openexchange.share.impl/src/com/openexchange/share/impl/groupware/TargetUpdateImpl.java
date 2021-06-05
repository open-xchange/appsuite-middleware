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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.FolderPermissionType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.PermissionTypeAwareFolder;
import com.openexchange.folderstorage.SetterAwareFolder;
import com.openexchange.folderstorage.UsedForSync;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.modules.Module;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.HandlerParameters;
import com.openexchange.share.core.ModuleHandler;
import com.openexchange.share.core.groupware.FolderTargetProxy;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.user.UserService;

/**
 * {@link TargetUpdateImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class TargetUpdateImpl extends AbstractTargetUpdate {

    private final HandlerParameters parameters;

    public TargetUpdateImpl(Session session, Connection writeCon, ServiceLookup services, ModuleExtensionRegistry<ModuleHandler> handlers) throws OXException {
        super(services, handlers);
        parameters = new HandlerParameters();
        parameters.setSession(session);
        Context context = getContextService().getContext(session.getContextId());
        parameters.setContext(context);
        parameters.setUser(getUserService().getUser(writeCon, session.getUserId(), context));
        parameters.setWriteCon(writeCon);
        FolderServiceDecorator folderServiceDecorator = new FolderServiceDecorator();
        folderServiceDecorator.put(Connection.class.getName(), writeCon);
        folderServiceDecorator.put(FolderServiceDecorator.PROPERTY_IGNORE_GUEST_PERMISSIONS, Boolean.TRUE.toString());
        parameters.setFolderServiceDecorator(folderServiceDecorator);
    }

    @Override
    protected HandlerParameters getHandlerParameters() {
        return parameters;
    }

    @Override
    protected Map<ShareTarget, TargetProxy> prepareProxies(List<ShareTarget> folderTargets, Map<Integer, List<ShareTarget>> objectsByModule) throws OXException {
        Map<ShareTarget, TargetProxy> proxies = new HashMap<ShareTarget, TargetProxy>(folderTargets.size() + objectsByModule.size(), 1.0F);
        Map<String, UserizedFolder> foldersById = loadFolderTargets(folderTargets, true, proxies);
        loadObjectTargets(objectsByModule, foldersById, true, proxies);
        return proxies;
    }

    @Override
    protected void updateFolders(List<TargetProxy> proxies) throws OXException {
        FolderService folderService = getFolderService();
        for (TargetProxy proxy : proxies) {
            FolderTargetProxy folderTargetProxy = ((FolderTargetProxy) proxy);
            UserizedFolder folder = folderTargetProxy.getFolder();
            FolderUpdate folderUpdate = new FolderUpdate();
            folderUpdate.setTreeID(folder.getTreeID());
            folderUpdate.setID(folder.getID());
            folderUpdate.setPermissions(folder.getPermissions());
            folderService.updateFolder(folderUpdate, folder.getLastModifiedUTC(), parameters.getSession(), parameters.getFolderServiceDecorator());

            if (folder.getContentType().getModule() == FolderObject.INFOSTORE) {
                // Add permission to sub folders
                FolderResponse<UserizedFolder[]> folderObjects = folderService.getSubfolders(folder.getTreeID(), folder.getID(), true, parameters.getSession(), parameters.getFolderServiceDecorator());

                List<Permission> appliedPermissions = folderTargetProxy.getAppliedPermissions();
                List<Permission> removedPermissions = folderTargetProxy.getRemovedPermissions();

                FolderServiceDecorator folderServiceDecorator;
                try {
                    folderServiceDecorator = parameters.getFolderServiceDecorator().clone();
                } catch (CloneNotSupportedException e) {
                    // should never occur
                    folderServiceDecorator = parameters.getFolderServiceDecorator();
                }
                for (UserizedFolder fol : folderObjects.getResponse()) {
                    updateSubfolder(folderService, fol, appliedPermissions, removedPermissions, folderServiceDecorator);
                }
            }
        }
    }

    private void updateSubfolder(FolderService folderService, UserizedFolder fol, List<Permission> appliedPermissions, List<Permission> removedPermissions, FolderServiceDecorator folderServiceDecorator) throws OXException {
        prepareInheritedPermissions(fol, appliedPermissions, removedPermissions);
        folderService.updateFolder(fol, fol.getLastModifiedUTC(), parameters.getSession(), folderServiceDecorator);

        FolderResponse<UserizedFolder[]> folderObjects = folderService.getSubfolders(fol.getTreeID(), fol.getID(), true, parameters.getSession(), parameters.getFolderServiceDecorator());
        for(UserizedFolder subFolder : folderObjects.getResponse()) {
            updateSubfolder(folderService, subFolder, appliedPermissions, removedPermissions, folderServiceDecorator);
        }


    }

    private static UserizedFolder prepareInheritedPermissions(UserizedFolder folder, List<Permission> added, List<Permission> removed) {
        Permission[] originalPermissions = folder.getPermissions();
        if (null == originalPermissions) {
            originalPermissions = new Permission[0];
        }

        List<Permission> filtered = new ArrayList<>(added.size());
        for (Permission add : added) {
            if (add.getType() == FolderPermissionType.LEGATOR) {
                Permission clone = (Permission) add.clone();
                clone.setPermissionLegator(folder.getParentID());
                clone.setType(FolderPermissionType.INHERITED);
                filtered.add(clone);
            }
        }

        for (Permission rem : removed) {
            if (rem.getType() == FolderPermissionType.LEGATOR) {
                rem.setPermissionLegator(String.valueOf(folder.getParentID()));
            }
            rem.setType(FolderPermissionType.INHERITED);
        }

        List<Permission> permissions = new ArrayList<>(originalPermissions.length + filtered.size());
        Collections.addAll(permissions, originalPermissions);
        permissions.addAll(filtered);
        permissions = removePermissions(permissions, removed);
        folder.setPermissions(permissions.toArray(new Permission[permissions.size()]));
        return folder;
    }

    protected static List<Permission> removePermissions(List<Permission> origPermissions, List<Permission> toRemove) {
        if (origPermissions == null || origPermissions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Permission> newPermissions = new ArrayList<Permission>(origPermissions);
        Iterator<Permission> it = newPermissions.iterator();
        while (it.hasNext()) {
            Permission permission = it.next();
            for (Permission removable : toRemove) {
                if (permission.isGroup() == removable.isGroup() && permission.getEntity() == removable.getEntity()) {
                    it.remove();
                    break;
                }
            }
        }

        return newPermissions;
    }

    @Override
    protected void touchFolders(List<TargetProxy> proxies) throws OXException {
        FolderService folderService = getFolderService();
        for (TargetProxy proxy : proxies) {
            UserizedFolder folder = ((FolderTargetProxy) proxy).getFolder();
            FolderUpdate folderUpdate = new FolderUpdate();
            folderUpdate.setTreeID(folder.getTreeID());
            folderUpdate.setID(folder.getID());
            folderService.updateFolder(folderUpdate, folder.getLastModifiedUTC(), parameters.getSession(), parameters.getFolderServiceDecorator());
        }
    }

    private void loadObjectTargets(Map<Integer, List<ShareTarget>> objectsByModule, Map<String, UserizedFolder> foldersById, boolean checkPermissions, Map<ShareTarget, TargetProxy> proxies) throws OXException {
        FolderService folderService = getFolderService();
        for (Entry<Integer, List<ShareTarget>> moduleEntry : objectsByModule.entrySet()) {
            int module = moduleEntry.getKey().intValue();
            ModuleHandler handler = handlers.get(module);
            List<ShareTarget> targetList = moduleEntry.getValue();
            for (ShareTarget target : targetList) {
                UserizedFolder folder = foldersById.get(target.getFolder());
                if (folder == null) {
                    folder = folderService.getFolder(FolderStorage.REAL_TREE_ID,
                        target.getFolder(),
                        parameters.getSession(),
                        parameters.getFolderServiceDecorator());
                    foldersById.put(folder.getID(), folder);
                }
            }

            List<TargetProxy> objects = handler.loadTargets(targetList, parameters);
            Iterator<ShareTarget> tlit = targetList.iterator();
            for (TargetProxy proxy : objects) {
                ShareTarget target = tlit.next();
                UserizedFolder parentFolder = foldersById.get(target.getFolder());
                if (checkPermissions && !canShareObject(parentFolder, proxy, handler)) {
                    throw ShareExceptionCodes.NO_SHARE_PERMISSIONS.create(
                        I(parameters.getUser().getId()),
                        proxy.getTitle(),
                        I(parameters.getContext().getContextId()));
                }

                proxies.put(target, proxy);
            }
        }
    }

    private Map<String, UserizedFolder> loadFolderTargets(List<ShareTarget> folderTargets, boolean checkPermissions, Map<ShareTarget, TargetProxy> proxies) throws OXException {
        Map<String, UserizedFolder> foldersById = new HashMap<>();
        FolderService folderService = getFolderService();
        for (ShareTarget folderTarget : folderTargets) {
            if (null != Module.getForFolderConstant(folderTarget.getModule())) {
                UserizedFolder folder = folderService.getFolder(FolderStorage.REAL_TREE_ID,
                    folderTarget.getFolder(),
                    parameters.getSession(),
                    parameters.getFolderServiceDecorator());
                FolderTargetProxy proxy = new FolderTargetProxy(folderTarget, folder);
                if (checkPermissions && !canShareFolder(folder)) {
                    throw ShareExceptionCodes.NO_SHARE_PERMISSIONS.create(
                        I(parameters.getUser().getId()),
                        proxy.getTitle(),
                        I(parameters.getContext().getContextId()));
                }

                foldersById.put(folder.getID(), folder);
                proxies.put(folderTarget, proxy);
            } else {
                proxies.put(folderTarget, new VirtualTargetProxy(folderTarget));
            }
        }

        return foldersById;
    }

    private boolean canShareFolder(UserizedFolder folder) {
        return folder.getOwnPermission().isAdmin();
    }

    private boolean canShareObject(UserizedFolder folder, TargetProxy proxy, ModuleHandler handler) {
        return canShareFolder(folder) ? true : handler.canShare(proxy, parameters);
    }

    private UserService getUserService() throws OXException {
        return getService(UserService.class);
    }

    private ContextService getContextService() throws OXException {
        return getService(ContextService.class);
    }

    private FolderService getFolderService() throws OXException {
        return getService(FolderService.class);
    }

    private <T> T getService(Class<T> clazz) throws OXException {
        return Tools.requireService(clazz, services);
    }

    private static final class FolderUpdate extends AbstractFolder implements SetterAwareFolder, PermissionTypeAwareFolder {

        private static final long serialVersionUID = -8615729293509593034L;

        private boolean containsSubscribed;
        private boolean containsUsedForSync;

        /**
         * Initializes a new {@link FolderUpdate}.
         */
        public FolderUpdate() {
            super();
            subscribed = true;
            usedForSync = UsedForSync.DEFAULT;
        }

        @Override
        public boolean isGlobalID() {
            return false;
        }

        @Override
        public void setSubscribed(boolean subscribed) {
            super.setSubscribed(subscribed);
            containsSubscribed = true;
        }

        @Override
        public boolean containsSubscribed() {
            return containsSubscribed;
        }

        @Override
        public void setUsedForSync(UsedForSync usedForSync) {
            super.setUsedForSync(usedForSync);
            containsUsedForSync = true;
        }

        @Override
        public boolean containsUsedForSync() {
            return containsUsedForSync;
        }

    }

}
