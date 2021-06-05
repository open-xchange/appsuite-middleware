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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderPermissionType;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.service.FolderCacheInvalidationService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.modules.Module;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.HandlerParameters;
import com.openexchange.share.core.ModuleHandler;
import com.openexchange.share.core.groupware.AdministrativeFolderTargetProxy;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.spi.FolderHandlerModuleExtension;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AdministrativeTargetUpdateImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class AdministrativeTargetUpdateImpl extends AbstractTargetUpdate {

    private final int contextID;
    private final Connection connection;
    private final HandlerParameters parameters;
    private final OXFolderAccess folderAccess;
	private final ModuleExtensionRegistry<FolderHandlerModuleExtension> folderExtensions;

    public AdministrativeTargetUpdateImpl(ServiceLookup services, int contextID, Connection writeCon, ModuleExtensionRegistry<ModuleHandler> handlers, ModuleExtensionRegistry<FolderHandlerModuleExtension> folderExtensions) throws OXException {
        super(services, handlers);
        this.contextID = contextID;
        this.connection = writeCon;
        parameters = new HandlerParameters();
        Context context = getContextService().getContext(contextID);
        parameters.setContext(context);
        parameters.setWriteCon(writeCon);
        folderAccess = new OXFolderAccess(connection, context);
        this.folderExtensions = folderExtensions;
    }

    @Override
    protected HandlerParameters getHandlerParameters() {
        return parameters;
    }

    @Override
    protected Map<ShareTarget, TargetProxy> prepareProxies(List<ShareTarget> folderTargets, Map<Integer, List<ShareTarget>> objectsByModule) throws OXException {
        Map<ShareTarget, TargetProxy> proxies = new HashMap<ShareTarget, TargetProxy>(folderTargets.size() + objectsByModule.size(), 1.0F);
        Map<String, FolderObject> foldersById = loadFolderTargets(folderTargets, proxies);
        loadObjectTargets(objectsByModule, foldersById, proxies);
        return proxies;
    }

    @Override
    protected void updateFolders(List<TargetProxy> proxies) throws OXException {
        for (TargetProxy proxy : proxies) {
            /*
             * perform folder update, impersonated as folder owner
             */
            AdministrativeFolderTargetProxy folderTargetProxy = (AdministrativeFolderTargetProxy) proxy;
            Session syntheticOwnerSession = ServerSessionAdapter.valueOf(folderTargetProxy.getOwner(), contextID);
            syntheticOwnerSession.setParameter("com.openexchange.share.administrativeUpdate", Boolean.TRUE);
            syntheticOwnerSession.setParameter(Connection.class.getName() + '@' + Thread.currentThread().getId(), connection);
            OXFolderManager folderManager = OXFolderManager.getInstance(syntheticOwnerSession, folderAccess, connection, connection);
            FolderObject folder = folderTargetProxy.getFolder();
            folderManager.updateFolder(folder, false, System.currentTimeMillis());

            if (folder.getModule() == FolderObject.INFOSTORE) {
                ContextService ctxService = services.getService(ContextService.class);
                // Add permission to sub folders
                List<Integer> subfolderIds;
                try {
                    subfolderIds = folder.getSubfolderIds(true, ctxService.getContext(contextID));
                } catch (SQLException e) {
                    throw ShareExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                }

                List<OCLPermission> appliedPermissions = folderTargetProxy.getAppliedPermissions();
                List<OCLPermission> removedPermissions = folderTargetProxy.getRemovedPermissions();

                for (Integer id : subfolderIds) {
                    FolderObject sub = folderAccess.getFolderObject(id.intValue());
                    prepareInheritedPermissions(sub, appliedPermissions, removedPermissions);
                    folderManager.updateFolder(sub, false, true, sub.getLastModified().getTime());
                }
            }

            /*
             * clear some additional caches for all potentially affected users that are not covered when updating through folder manager
             */
            FolderCacheInvalidationService invalidationService = services.getService(FolderCacheInvalidationService.class);
            for (Integer affectedUser : folderTargetProxy.getAffectedUsers()) {
                ServerSession syntheticSession = ServerSessionAdapter.valueOf(affectedUser.intValue(), contextID);
                invalidationService.invalidateSingle(proxy.getID(), FolderStorage.REAL_TREE_ID, syntheticSession);
            }
        }

    }

    private static FolderObject prepareInheritedPermissions(FolderObject folder, List<OCLPermission> added, List<OCLPermission> removed) {
        List<OCLPermission> originalPermissions = folder.getPermissions();
        if (null == originalPermissions) {
            originalPermissions = new ArrayList<>();
        }

        String parentId = String.valueOf(folder.getParentFolderID());
        List<OCLPermission> filtered = new ArrayList<>(added.size());
        for (OCLPermission add : added) {
            if (add.getType() == FolderPermissionType.LEGATOR) {
                add.setPermissionLegator(parentId);
                add.setType(FolderPermissionType.INHERITED);
                filtered.add(add);
            }
        }

        for (OCLPermission rem : removed) {
            if (rem.getType() == FolderPermissionType.LEGATOR) {
                rem.setPermissionLegator(parentId);
            }
            rem.setType(FolderPermissionType.INHERITED);
        }

        List<OCLPermission> permissions = new ArrayList<>(originalPermissions.size() + filtered.size());
        permissions.addAll(originalPermissions);
        permissions.addAll(filtered);
        permissions = removePermissions(permissions, removed);
        folder.setPermissions(permissions);
        return folder;
    }

    protected static List<OCLPermission> removePermissions(List<OCLPermission> origPermissions, List<OCLPermission> toRemove) {
        if (origPermissions == null || origPermissions.isEmpty()) {
            return Collections.emptyList();
        }

        List<OCLPermission> newPermissions = new ArrayList<OCLPermission>(origPermissions);
        Iterator<OCLPermission> it = newPermissions.iterator();
        while (it.hasNext()) {
            OCLPermission permission = it.next();
            for (OCLPermission removable : toRemove) {
                if (permission.isGroupPermission() == removable.isGroupPermission() && permission.getEntity() == removable.getEntity()) {
                    it.remove();
                    break;
                }
            }
        }

        return newPermissions;
    }

    @Override
    protected void touchFolders(List<TargetProxy> proxies) throws OXException {
        updateFolders(proxies);
    }

    private void loadObjectTargets(Map<Integer, List<ShareTarget>> objectsByModule, Map<String, FolderObject> foldersById, Map<ShareTarget, TargetProxy> proxies) throws OXException {
        for (Entry<Integer, List<ShareTarget>> moduleEntry : objectsByModule.entrySet()) {
            int module = moduleEntry.getKey().intValue();
            ModuleHandler handler = handlers.get(module);
            List<ShareTarget> targetList = moduleEntry.getValue();
            for (ShareTarget target : targetList) {
                FolderObject folder = foldersById.get(target.getFolder());
                if (folder == null) {
                    try {
                        folder = folderAccess.getFolderObject(Integer.parseInt(target.getFolder()));
                        foldersById.put(target.getFolder(), folder);
                    } catch (NumberFormatException e) {
                        throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                }
            }

            List<TargetProxy> objects = handler.loadTargets(targetList, parameters);
            Iterator<ShareTarget> tlit = targetList.iterator();
            for (TargetProxy proxy : objects) {
                proxies.put(tlit.next(), proxy);
            }
        }
    }

    private Map<String, FolderObject> loadFolderTargets(List<ShareTarget> folderTargets, Map<ShareTarget, TargetProxy> proxies) throws OXException {
        Map<String, FolderObject> foldersById = new HashMap<String, FolderObject>(folderTargets.size());
        for (ShareTarget folderTarget : folderTargets) {
            TargetProxy proxy = optExtendedProxy(folderTarget);
            if (null != proxy) {
                proxies.put(folderTarget, proxy);
            } else if (null != Module.getForFolderConstant(folderTarget.getModule())) {
                FolderObject folder;
                try {
                    folder = folderAccess.getFolderObject(Integer.parseInt(folderTarget.getFolder()));
                } catch (NumberFormatException e) {
                    throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
                foldersById.put(folderTarget.getFolder(), folder);
                proxies.put(folderTarget, new AdministrativeFolderTargetProxy(folder));
            } else {
                proxies.put(folderTarget, new VirtualTargetProxy(folderTarget));
            }
        }
        return foldersById;
    }

    private TargetProxy optExtendedProxy(ShareTarget folderTarget) throws OXException {
        FolderHandlerModuleExtension folderHandler = folderExtensions.opt(folderTarget.getModule());
        if (null != folderHandler && folderHandler.isApplicableFor(contextID, folderTarget.getFolder())) {
            TargetProxy proxy = folderHandler.resolveTarget(folderTarget, contextID);
            if (null != proxy) {
                return proxy;
            }
        }
        return null;
    }

    private ContextService getContextService() throws OXException {
        return getService(ContextService.class);
    }

    private <T> T getService(Class<T> clazz) throws OXException {
        return Tools.requireService(clazz, services);
    }

}
