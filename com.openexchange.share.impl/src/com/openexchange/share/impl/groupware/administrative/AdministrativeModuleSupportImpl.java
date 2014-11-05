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
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.AdministrativeModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetUpdate;
import com.openexchange.share.impl.groupware.ModuleHandlerRegistry;
import com.openexchange.tools.oxfolder.OXFolderAccess;


/**
 * {@link AdministrativeModuleSupportImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AdministrativeModuleSupportImpl implements AdministrativeModuleSupport {

    private final ServiceLookup services;
    private final ModuleHandlerRegistry registry;

    /**
     * Initializes a new {@link AdministrativeModuleSupportImpl}.
     *
     * @param services A service lookup reference
     */
    public AdministrativeModuleSupportImpl(ServiceLookup services) {
        super();
        this.services = services;
        this.registry = new ModuleHandlerRegistry(services);
    }

    @Override
    public TargetUpdate prepareUpdate(int contextID, Connection writeCon) throws OXException {
        return new AdministrativeTargetUpdateImpl(services, contextID, writeCon, registry);
    }

    @Override
    public TargetProxy load(int contextID, ShareTarget target) throws OXException {
        if (null == target) {
            return null;
        }
        Context context = services.getService(ContextService.class).getContext(contextID);
        if (target.isFolder()) {
            int folderID;
            try {
                folderID = Integer.valueOf(target.getFolder());
            } catch (NumberFormatException e) {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            FolderObject folder = new OXFolderAccess(context).getFolderObject(folderID);
            return new AdministrativeFolderTargetProxy(folder);
        } else {
            return registry.get(target.getModule()).loadTarget(target, context);
        }
    }

}
