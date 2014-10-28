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

package com.openexchange.share.impl.groupware;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.user.UserService;


/**
 * {@link TargetUpdateImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class TargetUpdateImpl implements TargetUpdate {

    private final Map<ShareTarget, TargetProxy> proxies = new HashMap<ShareTarget, TargetProxy>();

    private final ServiceLookup services;

    private final ModuleHandlerRegistry handlers;

    private final HandlerParameters parameters;

    private Map<Integer, List<ShareTarget>> objectsByModule;

    private List<ShareTarget> folderTargets;


    public TargetUpdateImpl(Session session, Connection writeCon, ServiceLookup services, ModuleHandlerRegistry handlers) throws OXException {
        super();
        this.services = services;
        this.handlers = handlers;
        parameters = new HandlerParameters();
        parameters.setSession(session);
        Context context = getContextService().getContext(session.getContextId());
        parameters.setContext(context);
        parameters.setUser(getUserService().getUser(writeCon, session.getUserId(), context));
        parameters.setWriteCon(writeCon);
        FolderServiceDecorator folderServiceDecorator = new FolderServiceDecorator();
        folderServiceDecorator.put(Connection.class.getName(), writeCon);
        parameters.setFolderServiceDecorator(folderServiceDecorator);
    }

    @Override
    public void prepare(List<ShareTarget> targets) throws OXException {
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

        Map<String, UserizedFolder> foldersById = loadFolderTargets(folderTargets, true);
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

        FolderService folderService = getFolderService();
        for (ShareTarget target : folderTargets) {
            TargetProxy proxy = get(target);
            if (proxy.wasModified()) {
                UserizedFolder folder = ((FolderTargetProxy) proxy).getFolder();
                folderService.updateFolder(folder, folder.getLastModified(), parameters.getSession(), parameters.getFolderServiceDecorator());
            }
        }

        for (int module : objectsByModule.keySet()) {
            List<ShareTarget> targets = objectsByModule.get(module);
            List<TargetProxy> modified = new ArrayList<TargetProxy>(targets.size());
            for (ShareTarget target : targets) {
                TargetProxy proxy = get(target);
                modified.add(proxy);
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

    private void loadObjectTargets(Map<Integer, List<ShareTarget>> objectsByModule, Map<String, UserizedFolder> foldersById, boolean checkPermissions) throws OXException {
        FolderService folderService = getFolderService();
        for (int module : objectsByModule.keySet()) {
            ModuleHandler handler = handlers.get(module);
            List<ShareTarget> targetList = objectsByModule.get(module);
            for (ShareTarget target : targetList) {
                if (!foldersById.containsKey(target.getFolder())) {
                    UserizedFolder folder = folderService.getFolder(FolderStorage.REAL_TREE_ID, target.getFolder(), parameters.getUser(), parameters.getContext(), parameters.getFolderServiceDecorator());
                    foldersById.put(folder.getID(), folder);
                }
            }

            List<TargetProxy> objects = handler.loadTargets(targetList, parameters);
            Iterator<ShareTarget> tlit = targetList.iterator();
            for (TargetProxy proxy : objects) {
                ShareTarget target = tlit.next();
                UserizedFolder parentFolder = foldersById.get(target.getFolder());
                if (checkPermissions && !canShareObject(parentFolder, proxy, handler)) {
                    throw ShareExceptionCodes.NO_SHARE_PERMISSIONS.create(parameters.getUser().getId(), proxy.getTitle(), parameters.getContext().getContextId());
                }

                proxies.put(target, proxy);
            }
        }
    }

    private Map<String, UserizedFolder> loadFolderTargets(List<ShareTarget> folderTargets, boolean checkPermissions) throws OXException {
        Map<String, UserizedFolder> foldersById = new HashMap<String, UserizedFolder>();
        FolderService folderService = getFolderService();
        for (ShareTarget folderTarget : folderTargets) {
            UserizedFolder folder = folderService.getFolder(FolderStorage.REAL_TREE_ID, folderTarget.getFolder(), parameters.getSession(), parameters.getFolderServiceDecorator());
            FolderTargetProxy proxy = new FolderTargetProxy(folder, parameters.getUser());
            if (checkPermissions && !canShareFolder(folder)) {
                throw ShareExceptionCodes.NO_SHARE_PERMISSIONS.create(parameters.getUser().getId(), proxy.getTitle(), parameters.getContext().getContextId());
            }

            foldersById.put(folder.getID(), folder);
            proxies.put(folderTarget, proxy);
        }

        return foldersById;
    }

    private boolean canShareFolder(UserizedFolder folder) {
        return folder.getOwnPermission().isAdmin();
    }

    private boolean canShareObject(UserizedFolder folder, TargetProxy proxy, ModuleHandler handler) {
        return handler.canShare(canShareFolder(folder), proxy, parameters);
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

}
