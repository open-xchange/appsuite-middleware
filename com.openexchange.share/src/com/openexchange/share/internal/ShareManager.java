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

package com.openexchange.share.internal;

import com.openexchange.contact.ContactService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.session.Session;
import com.openexchange.share.Entity;
import com.openexchange.share.Share;
import com.openexchange.share.ShareRequest;
import com.openexchange.share.rdb.ShareStorage;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;


/**
 * {@link ShareManager}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.x.x
 */
public class ShareManager {

    public void createShare(ShareRequest shareRequest, Session session) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        for (Entity entity : shareRequest.getEntities()) {
            if (entity.getUserId() > 0) {
                createShareForUser(shareRequest, entity, serverSession);
            } else if (entity.getGroupId() >= 0) {
                createShareForGroup(shareRequest, entity, serverSession);
            } else if (entity.getContactId() > 0) {
                createShareForContact(shareRequest, entity, serverSession);
            } else if (entity.getMailAddress() != null) {
                createShareForMailAddress(shareRequest, entity, serverSession);
            } else {
                // throw exception
            }
        }
    }

    private void createShareForMailAddress(ShareRequest shareRequest, Entity entity, ServerSession session) throws OXException {

    }

    private Share createShare() {
        // TODO Auto-generated method stub
        return null;
    }

    private Folder modifyFolder(UserizedFolder folder, int guestId) {
        // TODO Auto-generated method stub
        return null;
    }

    private void createShareForContact(ShareRequest shareRequest, Entity entity, ServerSession serverSession) {
        // TODO Auto-generated method stub

    }

    private void createShareForGroup(ShareRequest shareRequest, Entity entity, ServerSession serverSession) {
        // TODO Auto-generated method stub

    }

    private void createShareForUser(ShareRequest shareRequest, Entity entity, ServerSession serverSession) {
        // TODO Auto-generated method stub

    }

    private ShareStorage getShareStorage() {
        return null;
    }

    private UserService getUserService() {
        return null;
    }

    private ContactService getContactService() {
        return null;
    }

    private FolderService getFolderService() {
        return null;
    }

    private DatabaseService getDatabaseService() {
        return null;
    }

}
