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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.share.impl.groupware;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.service.FolderCacheInvalidationService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.modules.Module;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.TargetProxy;
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

    public AdministrativeTargetUpdateImpl(ServiceLookup services, int contextID, Connection writeCon, ModuleHandlerRegistry handlers) throws OXException {
        super(services, handlers);
        this.contextID = contextID;
        this.connection = writeCon;
        parameters = new HandlerParameters();
        Context context = getContextService().getContext(contextID);
        parameters.setContext(context);
        parameters.setWriteCon(writeCon);
        folderAccess = new OXFolderAccess(connection, context);
    }

    @Override
    protected HandlerParameters getHandlerParameters() {
        return parameters;
    }

    @Override
    protected Map<ShareTarget, TargetProxy> prepareProxies(List<ShareTarget> folderTargets, Map<Integer, List<ShareTarget>> objectsByModule) throws OXException {
        Map<ShareTarget, TargetProxy> proxies = new HashMap<ShareTarget, TargetProxy>(folderTargets.size() + objectsByModule.size(), 1.0F);
        Map<String, FolderObject> foldersById = loadFolderTargets(folderTargets, proxies);
        loadObjectTargets(objectsByModule, foldersById, true, proxies);
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
            folderManager.updateFolder(folderTargetProxy.getFolder(), false, System.currentTimeMillis());
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

    @Override
    protected void touchFolders(List<TargetProxy> proxies) throws OXException {
        updateFolders(proxies);
    }

    private void loadObjectTargets(Map<Integer, List<ShareTarget>> objectsByModule, Map<String, FolderObject> foldersById, boolean checkPermissions, Map<ShareTarget, TargetProxy> proxies) throws OXException {
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
        Map<String, FolderObject> foldersById = new HashMap<>();
        for (ShareTarget folderTarget : folderTargets) {
            if (null != Module.getForFolderConstant(folderTarget.getModule())) {
                FolderObject folder;
                try {
                    folder = folderAccess.getFolderObject(Integer.parseInt(folderTarget.getFolder()));
                } catch (NumberFormatException e) {
                    throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
                AdministrativeFolderTargetProxy proxy = new AdministrativeFolderTargetProxy(folder);
                foldersById.put(folderTarget.getFolder(), folder);
                proxies.put(folderTarget, proxy);
            } else {
                proxies.put(folderTarget, new VirtualTargetProxy(folderTarget));
            }
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
