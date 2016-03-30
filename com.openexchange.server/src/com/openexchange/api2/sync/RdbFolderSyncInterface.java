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

package com.openexchange.api2.sync;

import static com.openexchange.tools.oxfolder.OXFolderUtility.folderModule2String;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderManager;

/**
 * RdbFolderSyncInterface
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RdbFolderSyncInterface implements FolderSyncInterface {

    /*
     * Members
     */
    private final int userId;

    private final Context ctx;

    private final Session session;

    private final User user;

    private final OXFolderAccess oxfolderAccess;

    private final UserConfiguration userConfiguration;

    public RdbFolderSyncInterface(final Session sessionObj, final Context ctx) throws OXException {
        this(sessionObj, ctx, null);
    }

    public RdbFolderSyncInterface(final Session session, final Context ctx, final OXFolderAccess oxfolderAccess) throws OXException {
        super();
        this.session = session;
        user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
        this.userId = user.getId();
        user.getGroups();
        this.ctx = ctx;
        this.oxfolderAccess = oxfolderAccess == null ? new OXFolderAccess(ctx) : oxfolderAccess;
        userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx);
    }

    @Override
    public int clearFolder(final FolderObject folder, final Date clientLastModified) throws OXException {
        try {
            if (folder.getType() == FolderObject.PUBLIC && !userConfiguration.hasFullPublicFolderAccess()) {
                throw OXFolderExceptionCode.NO_PUBLIC_FOLDER_WRITE_ACCESS.create(Integer.valueOf(session.getUserId()), Integer.valueOf(folder.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
            if (!folder.exists(ctx)) {
                throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(folder.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
            if (clientLastModified != null && oxfolderAccess.getFolderLastModified(folder.getObjectID()).after(clientLastModified)) {
                throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(folder.getObjectID()), Integer.valueOf(ctx.getContextId()));
            }
            final EffectivePermission effectivePerm = folder.getEffectiveUserPermission(userId, userConfiguration);
            if (!effectivePerm.hasModuleAccess(folder.getModule())) {
                throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(Integer.valueOf(session.getUserId()), folderModule2String(folder.getModule()), Integer.valueOf(ctx.getContextId()));
            }
            if (!effectivePerm.isFolderVisible()) {
                if (!effectivePerm.getUnderlyingPermission().isFolderVisible()) {
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(folder.getObjectID()), Integer.valueOf(session.getUserId()), Integer.valueOf(ctx.getContextId()));
                }
                throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(folder.getObjectID()), Integer.valueOf(session.getUserId()), Integer.valueOf(ctx.getContextId()));
            }
            final long lastModified = System.currentTimeMillis();
            OXFolderManager.getInstance(session, oxfolderAccess).clearFolder(folder, false, lastModified);
            return folder.getObjectID();
        } catch (final RuntimeException e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
        }
    }

}
