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
package com.openexchange.setuptools;

import com.openexchange.exception.OXException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.openexchange.calendar.CalendarSql;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class TestFolderToolkit {

    public int getStandardFolder(final int user, final Context ctx) {
        final FolderObject fo = getStandardFolderObject(user, ctx);
        if(fo == null) {
            return -1;
        }
        return fo.getObjectID();
    }

    private FolderObject getStandardFolderObject(final int user, final Context ctx) {
        final OXFolderAccess access = new OXFolderAccess(ctx);
        FolderObject fo = null;
        try {
            fo = access.getDefaultFolder(user, FolderObject.CALENDAR);
        } catch (final OXException e) {
            e.printStackTrace();
            return null;
        }
        return fo;
    }

    public FolderObject createPublicFolderFor(final Session session,final Context ctx, final String name, final int parent, final int...users) {
        Connection writecon = null;
        try {
        	writecon = DBPool.pickupWriteable(ctx);
	        final OXFolderManager oxma = OXFolderManager.getInstance(session, writecon, writecon);

            final ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(users.length);
            for(final int user : users) {
                final OCLPermission oclp = new OCLPermission();
                oclp.setEntity(user);
                oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                oclp.setFolderAdmin(true);
                permissions.add(oclp);
            }


            FolderObject fo = new FolderObject();
	        fo.setFolderName(name);
	        fo.setParentFolderID(parent);
	        fo.setModule(FolderObject.CALENDAR);
	        fo.setType(FolderObject.PUBLIC);
	        fo.setPermissions(permissions);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            return fo;
        } catch (final OXException e) {
            e.printStackTrace();
            return null;
        } finally {
        	if(writecon != null) {
                DBPool.pushWrite(ctx, writecon);
            }
        }
    }

    public FolderObject createPrivateFolderForSessionUser(final Session session,final Context ctx, final String name, final int parent) {
        Connection writecon = null;
        try {
        	writecon = DBPool.pickupWriteable(ctx);
	        final OXFolderManager oxma = OXFolderManager.getInstance(session, writecon, writecon);

            final ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(1);
            final OCLPermission oclp = new OCLPermission();
            oclp.setEntity(session.getUserId());
            oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            oclp.setFolderAdmin(true);
            permissions.add(oclp);

            FolderObject fo = new FolderObject();
	        fo.setFolderName(name);
	        fo.setParentFolderID(parent);
	        fo.setModule(FolderObject.CALENDAR);
	        fo.setType(FolderObject.PRIVATE);
	        fo.setPermissions(permissions);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            return fo;
        } catch (final OXException e) {
            e.printStackTrace();
            return null;
        } finally {
        	if(writecon != null) {
                DBPool.pushWrite(ctx, writecon);
            }
        }
    }

    public void removeAll(final Session session, final List<FolderObject> cleanFolders) {
        final OXFolderManager oxma;
        try {
            oxma = OXFolderManager.getInstance(session, new CalendarSql(session));
            for(final FolderObject folder : cleanFolders) {
                oxma.deleteFolder(folder, true, System.currentTimeMillis());
            }
        } catch (final OXException e) {
            e.printStackTrace();

        }
    }

    /**
     * Shares the standard private Calendar Folder to a given userId with admin permission.
     *
     * @param session
     * @param ctx
     * @param otherUserId
     */
    public void sharePrivateFolder(final Session session, final Context ctx, final int otherUserId) {
        final FolderObject fo = getStandardFolderObject(session.getUserId(), ctx);
        sharePrivateFolder(session, ctx, otherUserId, fo);
    }

    /**
     * Shares a given calendar folder to a given userId with Admin permisson.
     * @param sssion
     * @param ctx
     * @param otherUserId
     * @param folder
     */
    public void sharePrivateFolder(Session session, Context ctx, int otherUserId, FolderObject folder) {
        final OCLPermission oclp = new OCLPermission();
        oclp.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
        sharePrivateFolder(session, ctx, otherUserId, folder, oclp);
    }

    /**
     * Shares a given Calendar Folder to a given userId with the given permission.
     *
     * @param session
     * @param ctx
     * @param otherUserId
     * @param folder
     * @param oclp
     */
    public void sharePrivateFolder(final Session session, final Context ctx, final int otherUserId, final FolderObject folder, final OCLPermission oclp) {
        boolean mustAdd = true;
        final ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(folder.getPermissions());
        for (int i = 0, size = permissions.size(); i < size && mustAdd; i++) {
            final OCLPermission permission = permissions.get(i);
            if (permission.getEntity() == otherUserId) {
                mustAdd = false;
                permission.setAllPermission(
                    oclp.getFolderPermission(),
                    oclp.getReadPermission(),
                    oclp.getWritePermission(),
                    oclp.getDeletePermission());

            }
        }
        if (mustAdd) {
            oclp.setEntity(otherUserId);
            permissions.add(oclp);
        }
        folder.setPermissions(permissions);

        save(folder, ctx, session);
    }

    public void save(final FolderObject fo, final Context ctx, final Session session) {
        Connection writecon = null;
        try {
            writecon = DBPool.pickupWriteable(ctx);
            final OXFolderManager oxma = OXFolderManager.getInstance(session, writecon, writecon);
            oxma.updateFolder(fo, false, false, System.currentTimeMillis());
        } catch (final OXException e) {
            e.printStackTrace();
        } finally {
            if(writecon != null) {
                DBPool.pushWrite(ctx, writecon);
            }
        }
    }

    /**
     * Unshares the standard private Calendar Folder.
     *
     * @param session
     * @param ctx
     */
    public void unsharePrivateFolder(final Session session, final Context ctx) {
        final FolderObject fo = getStandardFolderObject(session.getUserId(), ctx);
        unsharePrivateFolder(session, ctx, fo);
    }

    /**
     * Unshares a given Calendar Folder.
     *
     * @param session
     * @param ctx
     * @param folder
     */
    public void unsharePrivateFolder(final Session session, final Context ctx, FolderObject folder) {
        final ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(folder.getPermissions());
        final ArrayList<OCLPermission> newPermissions = new ArrayList<OCLPermission>();
        final int userId = session.getUserId();
        for (int i = 0, size = permissions.size(); i < size; i++) {
            final OCLPermission permission = permissions.get(i);
            if (permission.getEntity() == userId) {
                newPermissions.add(permission);
            }
        }

        folder.setPermissions(newPermissions);

        save(folder, ctx, session);
    }
}
