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

package com.openexchange.groupware.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;

public class FolderTreeUtilImpl implements FolderTreeUtil {

    private static volatile Mode MODE;

    public FolderTreeUtilImpl(final DBProvider provider) {
        MODE = new CACHE_MODE(provider);
    }

    @Override
    public List<Integer> getPath(final int folderid, final Context ctx, final User user) throws OXException {
        final List<Integer> path = new ArrayList<Integer>();
        try {
            FolderObject folder = getFolder(folderid, ctx);
            path.add(Integer.valueOf(folder.getObjectID()));
            while (folder != null) {
                if (folder.getParentFolderID() == FolderObject.SYSTEM_ROOT_FOLDER_ID) {
                    path.add(Integer.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID));
                    folder = null;
                } else {
                    folder = getFolder(folder.getParentFolderID(), ctx);
                    path.add(Integer.valueOf(folder.getObjectID()));
                }
            }
        } catch (Exception e) {
            throw new OXException(e);
        }
        Collections.reverse(path);
        return path;
    }

    private FolderObject getFolder(final int folderid, final Context ctx) throws Exception {
        return MODE.getFolder(folderid, ctx);
    }

    static interface Mode {
        public FolderObject getFolder(int folderid, Context ctx) throws Exception;
    }

    private static final class CACHE_MODE implements Mode {

        private final DBProvider provider;

        public CACHE_MODE(final DBProvider provider) {
            this.provider = provider;
        }

        @Override
        public FolderObject getFolder(final int folderid, final Context ctx)  throws Exception{
            try {
                Connection readCon = null;
                try {
                    readCon = provider.getReadConnection(ctx);
                    if (FolderCacheManager.isEnabled()) {
                        return FolderCacheManager.getInstance().getFolderObject(folderid, true, ctx, readCon);
                    }
                    return FolderObject.loadFolderObjectFromDB(folderid, ctx, readCon);
                } finally {
                    provider.releaseReadConnection(ctx, readCon);
                }
            } catch (OXException e) {
                MODE = new NORMAL_MODE();
                return MODE.getFolder(folderid, ctx);
            }
        }
    }

    private static final class NORMAL_MODE implements Mode {

        @Override
        public FolderObject getFolder(final int folderid, final Context ctx) throws Exception {
            return FolderObject.loadFolderObjectFromDB(folderid, ctx);
        }

    }

}
