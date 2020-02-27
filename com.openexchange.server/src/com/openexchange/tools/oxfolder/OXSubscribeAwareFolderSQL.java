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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.tools.oxfolder;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.property.FolderSubscriptionHelper;

/**
 * {@link OXSubscribeAwareFolderSQL}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class OXSubscribeAwareFolderSQL {


    /**
     * Gets all visible folders for the given module. Unsubscribed folders are ignored.
     *
     * @param userId The user id
     * @param memberInGroups The memberInGroups
     * @param accessibleModules The accessible modules
     * @param module The module to retrieve
     * @param ctx The context
     * @return A list of available folders in the given module
     * @throws OXException
     */
    public static List<FolderObject> getAllVisibleFoldersOfModule(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int module, final Context ctx) throws OXException {
        Connection con = DBPool.pickup(ctx);
        SearchIterator<FolderObject> iter = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(userId, memberInGroups, accessibleModules, module, ctx, con);
        try {
            List<FolderObject> result = new ArrayList<>();
            while (iter.hasNext()) {
                FolderObject folder = iter.next();
                if (ServerServiceRegistry.getServize(FolderSubscriptionHelper.class, true).isSubscribed(Optional.ofNullable(con), ctx.getContextId(), userId, folder.getObjectID(), folder.getModule()).orElse(Boolean.TRUE) == Boolean.FALSE) {
                    continue;
                }
                result.add(folder);
            }
            return result;
        } finally {
            iter.close();
            Database.back(ctx.getContextId(), false, con);
        }
    }

}
