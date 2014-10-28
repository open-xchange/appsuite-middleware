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

package com.openexchange.share.json.actions;

import static com.openexchange.osgi.Tools.requireService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.Pair;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.Share;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractPerformer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class AbstractPerformer<T> {

    protected final ServerSession session;

    protected final ServiceLookup services;

    protected AbstractPerformer(ServerSession session, ServiceLookup services) {
        super();
        this.session = session;
        this.services = services;
    }

    protected abstract T perform() throws OXException;

    protected ModuleSupport getModuleSupport() throws OXException {
        return requireService(ModuleSupport.class, services);
    }

    /**
     * Gets the share service.
     *
     * @return The share service
     * @throws OXException if the service is unavailable
     */
    protected ShareService getShareService() throws OXException {
        return requireService(ShareService.class, services);
    }

    /**
     * Takes a list of {@link Share}s and splits them up into two maps. The first map
     * contains all folder targets mapped to their according module. The second map contains
     * all object targets, again mapped to their according module.
     *
     * @param targets The targets to share
     * @return A {@link Pair} with the folders as first and the objects as second entry.
     */
    protected Pair<Map<Integer, List<ShareTarget>>, Map<Integer, List<ShareTarget>>> distinguishTargets(List<ShareTarget> targets) {
        Map<Integer, List<ShareTarget>> folders = new HashMap<Integer, List<ShareTarget>>();
        Map<Integer, List<ShareTarget>> objects = new HashMap<Integer, List<ShareTarget>>();
        for (ShareTarget target : targets) {
            int module = target.getModule();
            List<ShareTarget> finalTargets;
            if (target.isFolder()) {
                finalTargets = folders.get(module);
                if (finalTargets == null) {
                    finalTargets = new LinkedList<ShareTarget>();
                    folders.put(module, finalTargets);
                }
            } else {
                finalTargets = objects.get(module);
                if (finalTargets == null) {
                    finalTargets = new LinkedList<ShareTarget>();
                    objects.put(module, finalTargets);
                }
            }

            finalTargets.add(target);
        }

        return new Pair<Map<Integer,List<ShareTarget>>, Map<Integer,List<ShareTarget>>>(folders, objects);
    }

}
