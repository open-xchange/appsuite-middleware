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

package com.openexchange.share.impl.groupware.administrative;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.service.FolderCacheInvalidationService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.share.impl.groupware.HandlerParameters;
import com.openexchange.share.impl.groupware.ModuleHandler;
import com.openexchange.share.impl.groupware.ModuleHandlerRegistry;
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
public class AdministrativeTargetUpdateImpl implements TargetUpdate {

    private final Map<ShareTarget, TargetProxy> proxies;
    private final ServiceLookup services;
    private final int contextID;
    private final Connection connection;
    private Map<Integer, List<ShareTarget>> objectsByModule;
    private List<ShareTarget> folderTargets;
    private final ModuleHandlerRegistry handlers;
    private final HandlerParameters parameters;
    private final OXFolderAccess folderAccess;

    //TODO: abstract TargetUpdate base class
    public AdministrativeTargetUpdateImpl(ServiceLookup services, int contextID, Connection writeCon, ModuleHandlerRegistry handlers) throws OXException {
        super();
        this.services = services;
        this.contextID = contextID;
        this.connection = writeCon;
        this.handlers = handlers;
        proxies = new HashMap<ShareTarget, TargetProxy>();
        parameters = new HandlerParameters();
        Context context = getContextService().getContext(contextID);
        parameters.setContext(context);
        parameters.setWriteCon(writeCon);
        folderAccess = new OXFolderAccess(connection, context);
    }

    @Override
    public void fetch(Collection<ShareTarget> targets) throws OXException {
        objectsByModule = new HashMap<Integer, List<ShareTarget>>();
        folderTargets = new LinkedList<ShareTarget>();
        for (ShareTarget target : targets) {
            if (target.isFolder()) {
                folderTargets.add(target);
            } else {
                int module = target.getModule();
                List<ShareTarget> targetList = objectsByModule.get(module);
                if (targetList == null) {
                    targetList = new LinkedList<ShareTarget>();
                    objectsByModule.put(module, targetList);
                }
                targetList.add(target);
            }
        }
        Map<String, FolderObject> foldersById = loadFolderTargets(folderTargets);
        loadObjectTargets(objectsByModule, foldersById, true);
    }

    @Override
    public TargetProxy get(ShareTarget target) {
        if (target == null) {
            return null;
        }
        return proxies.get(target);
    }

    @Override
    public void run() throws OXException {
        if (objectsByModule == null || folderTargets == null) {
            throw new IllegalStateException("prefetch() must be called on TargetHandler before update!");
        }
        for (ShareTarget target : folderTargets) {
            TargetProxy proxy = get(target);
            if (proxy.wasModified()) {
                /*
                 * perform folder update, impersonated as folder owner
                 */
                AdministrativeFolderTargetProxy folderTargetProxy = (AdministrativeFolderTargetProxy) proxy;
                Session syntheticOwnerSession = ServerSessionAdapter.valueOf(folderTargetProxy.getOwner(), contextID);
                OXFolderManager folderManager = OXFolderManager.getInstance(syntheticOwnerSession, folderAccess, connection, connection);
                folderManager.updateFolder(folderTargetProxy.getFolder(), false, System.currentTimeMillis());
                /*
                 * clear some additional caches for all potentially affected users that are not covered when updating through folder manager
                 */
                FolderCacheInvalidationService invalidationService = services.getService(FolderCacheInvalidationService.class);
                for (Integer affectedUser : folderTargetProxy.getAffectedUsers()) {
                    ServerSession syntheticSession = ServerSessionAdapter.valueOf(affectedUser.intValue(), contextID);
                    invalidationService.invalidateSingle(target.getFolder(), FolderStorage.REAL_TREE_ID, syntheticSession);
                }
            }
        }

        for (int module : objectsByModule.keySet()) {
            List<ShareTarget> targets = objectsByModule.get(module);
            List<TargetProxy> modified = new ArrayList<TargetProxy>(targets.size());
            for (ShareTarget target : targets) {
                TargetProxy proxy = get(target);
                if (proxy.wasModified()) {
                    modified.add(proxy);
                }
            }

            if (!modified.isEmpty()) {
                ModuleHandler handler = handlers.get(module);
                handler.updateObjects(modified, parameters);
            }
        }

    }

    @Override
    public void close() {

    }

    private void loadObjectTargets(Map<Integer, List<ShareTarget>> objectsByModule, Map<String, FolderObject> foldersById, boolean checkPermissions) throws OXException {
        for (int module : objectsByModule.keySet()) {
            ModuleHandler handler = handlers.get(module);
            List<ShareTarget> targetList = objectsByModule.get(module);
            for (ShareTarget target : targetList) {
                if (!foldersById.containsKey(target.getFolder())) {
                    FolderObject folder;
                    try {
                        folder = folderAccess.getFolderObject(Integer.valueOf(target.getFolder()));
                    } catch (NumberFormatException e) {
                        throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                    foldersById.put(target.getFolder(), folder);
                }
            }

            List<TargetProxy> objects = handler.loadTargets(targetList, parameters);
            Iterator<ShareTarget> tlit = targetList.iterator();
            for (TargetProxy proxy : objects) {
                proxies.put(tlit.next(), proxy);
            }
        }
    }

    private Map<String, FolderObject> loadFolderTargets(List<ShareTarget> folderTargets) throws OXException {
        Map<String, FolderObject> foldersById = new HashMap<String, FolderObject>();
        for (ShareTarget folderTarget : folderTargets) {
            FolderObject folder;
            try {
                folder = folderAccess.getFolderObject(Integer.valueOf(folderTarget.getFolder()));
            } catch (NumberFormatException e) {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            AdministrativeFolderTargetProxy proxy = new AdministrativeFolderTargetProxy(folder);
            foldersById.put(folderTarget.getFolder(), folder);
            proxies.put(folderTarget, proxy);
        }

        return foldersById;
    }

    private ContextService getContextService() throws OXException {
        return getService(ContextService.class);
    }

    private <T> T getService(Class<T> clazz) throws OXException {
        return Tools.requireService(clazz, services);
    }

}
