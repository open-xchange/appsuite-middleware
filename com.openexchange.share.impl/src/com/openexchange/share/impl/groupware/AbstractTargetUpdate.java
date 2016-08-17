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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetUpdate;


/**
 * {@link AbstractTargetUpdate}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class AbstractTargetUpdate implements TargetUpdate {

    protected final ServiceLookup services;

    protected final ModuleHandlerRegistry handlers;

    private Map<ShareTarget, TargetProxy> proxies;

    private Map<Integer, List<ShareTarget>> objectsByModule;

    private List<ShareTarget> folderTargets;


    protected AbstractTargetUpdate(ServiceLookup services, ModuleHandlerRegistry handlers) {
        super();
        this.services = services;
        this.handlers = handlers;
        this.proxies = Collections.emptyMap();
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

        proxies = prepareProxies(folderTargets, objectsByModule);
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
            throw new IllegalStateException("fetch() must be called on TargetHandler before update!");
        }

        List<TargetProxy> foldersToUpdate = new LinkedList<TargetProxy>();
        List<TargetProxy> foldersToTouch = new LinkedList<TargetProxy>();
        for (ShareTarget target : folderTargets) {
            TargetProxy proxy = get(target);
            if (proxy.wasModified()) {
                foldersToUpdate.add(proxy);
            } else if (proxy.wasTouched()) {
                foldersToTouch.add(proxy);
            }
        }
        updateFolders(foldersToUpdate);
        if (0 < foldersToTouch.size()) {
            touchFolders(foldersToTouch);
        }

        for (Entry<Integer, List<ShareTarget>> moduleEntry : objectsByModule.entrySet()) {
            List<ShareTarget> targets = moduleEntry.getValue();
            List<TargetProxy> modified = new ArrayList<TargetProxy>(targets.size());
            List<TargetProxy> touched = new ArrayList<TargetProxy>(targets.size());
            for (ShareTarget target : targets) {
                TargetProxy proxy = get(target);
                if (proxy.wasModified()) {
                    modified.add(proxy);
                } else if (proxy.wasTouched()) {
                    touched.add(proxy);
                }
            }

            int module = moduleEntry.getKey().intValue();
            if (!modified.isEmpty()) {
                ModuleHandler handler = handlers.get(module);
                handler.updateObjects(modified, getHandlerParameters());
            }
            if (!touched.isEmpty()) {
                ModuleHandler handler = handlers.get(module);
                handler.touchObjects(touched, getHandlerParameters());
            }
        }
    }

    @Override
    public void close() {
        // Nothing...
    }

    protected abstract Map<ShareTarget, TargetProxy> prepareProxies(List<ShareTarget> folderTargets, Map<Integer, List<ShareTarget>> objectsByModule) throws OXException;

    protected abstract void updateFolders(List<TargetProxy> proxies) throws OXException;

    protected abstract void touchFolders(List<TargetProxy> proxies) throws OXException;

    protected abstract HandlerParameters getHandlerParameters();

}
