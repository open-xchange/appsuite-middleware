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

package com.openexchange.api2.sync;

import static com.openexchange.tools.oxfolder.OXFolderUtility.folderModule2String;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.user.User;

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
                throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(folder.getObjectID()), Integer.valueOf(session.getUserId()), Integer.valueOf(ctx.getContextId()));
            }
            final long lastModified = System.currentTimeMillis();
            OXFolderManager.getInstance(session, oxfolderAccess).clearFolder(folder, false, lastModified);
            return folder.getObjectID();
        } catch (RuntimeException e) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
        }
    }

}
