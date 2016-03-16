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
import com.openexchange.groupware.ldap.User;

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
            while(folder != null) {
                if(folder.getParentFolderID() == FolderObject.SYSTEM_ROOT_FOLDER_ID) {
                    path.add(Integer.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID));
                    folder = null;
                } else {
                    folder = getFolder(folder.getParentFolderID(), ctx);
                    path.add(Integer.valueOf(folder.getObjectID()));
                }
            }
        } catch (final Exception e) {
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
            } catch (final OXException e) {
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
