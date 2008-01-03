package com.openexchange.groupware.infostore.database.impl;

import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.api2.OXException;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.collections.Injector;

/**
 * Created by IntelliJ IDEA.
 * User: fla
 * Date: 03.01.2008
 * Time: 10:15:41
 * To change this template use File | Settings | File Templates.
 */
public interface InfostoreSecurity {
    @OXThrows(
			category = AbstractOXException.Category.USER_INPUT,
            desc = "The infoitem does not exist, so the permissions cannot be loaded.",
            exceptionId = 0,
            msg = "The requested item does not exist."
    )
    EffectiveInfostorePermission getInfostorePermission(int id, Context ctx, User user, UserConfiguration userConfig) throws OXException;

    EffectivePermission getFolderPermission(long folderId, Context ctx, User user, UserConfiguration userConfig) throws OXException;

    <L> L injectInfostorePermissions(int[] ids, Context ctx, User user, UserConfiguration userConfig, L list, Injector<L, EffectiveInfostorePermission> injector) throws OXException;

    @OXThrows(
			category = AbstractOXException.Category.CODE_ERROR,
            desc = "The client tries to put an infoitem into a non infoitem folder.",
            exceptionId = 2,
            msg = "The folder %d is not an Infostore folder"
    )
    void checkFolderId(long folderId, Context ctx) throws OXException;
}
