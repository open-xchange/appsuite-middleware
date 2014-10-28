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

import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.user.UserService;


/**
 * {@link ModuleSupportImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ModuleSupportImpl implements ModuleSupport {

    private final ServiceLookup services;

    private final ModuleHandlerRegistry handlers;

    public ModuleSupportImpl(ServiceLookup services) {
        super();
        this.services = services;
        handlers = new ModuleHandlerRegistry(services);
    }

    @Override
    public TargetUpdate prepareUpdate(Session session, Connection writeCon) throws OXException {
        return new TargetUpdateImpl(session, writeCon, services, handlers);
    }

    @Override
    public TargetProxy load(ShareTarget target, Session session) throws OXException {
        if (target == null) {
            return null;
        }

        if (target.isFolder()) {
            User user = requireService(UserService.class, services).getUser(session.getUserId(), session.getContextId());
            UserizedFolder userizedFolder = requireService(FolderService.class, services).getFolder(FolderStorage.REAL_TREE_ID, target.getFolder(), session, null);
            return new FolderTargetProxy(userizedFolder, user);
        } else {
            return handlers.get(target.getModule()).loadTarget(target, session);
        }
    }

}
