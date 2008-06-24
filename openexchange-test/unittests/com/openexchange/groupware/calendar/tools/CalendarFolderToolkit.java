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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.groupware.calendar.tools;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarFolderToolkit {

    public int getStandardFolder(final int user, final Context ctx) {
        FolderObject fo = getStandardFolderObject(user, ctx);
        if(fo == null) {
            return -1;
        }
        return fo.getObjectID();
    }

    private FolderObject getStandardFolderObject(int user, Context ctx) {
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

    public FolderObject createPublicFolderFor(Session session,Context ctx, String name, int parent, int...users) {
        Connection writecon = null;
        try {
        	writecon = DBPool.pickupWriteable(ctx);
	        final OXFolderManager oxma = new OXFolderManagerImpl(session, writecon, writecon);

            ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(users.length);
            for(int user : users) {
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
        } catch (OXException e) {
            e.printStackTrace();
            return null;
        } catch (DBPoolingException e) {
            e.printStackTrace();
            return null;
        } finally {
        	if(writecon != null) {
                try {
                    DBPool.pushWrite(ctx, writecon);
                } catch (DBPoolingException e) {
                    //IGNORE
                }
            }
        }
    }

    public void removeAll(Session session, List<FolderObject> cleanFolders) {
        final OXFolderManager oxma;
        try {
            oxma = new OXFolderManagerImpl(session);
            for(FolderObject folder : cleanFolders) {
                oxma.deleteFolder(folder, true, System.currentTimeMillis());
            }
        } catch (OXException e) {
            e.printStackTrace();

        }
    }

    public void sharePrivateFolder(Session session, Context ctx, int otherUserId) {
        FolderObject fo = getStandardFolderObject(session.getUserId(), ctx);
        boolean mustAdd = true;
        ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(fo.getPermissions());
        for(int i = 0, size = permissions.size(); i < size && mustAdd; i++) {
            OCLPermission permission = permissions.get(i);
            if(permission.getEntity() == otherUserId) {
                mustAdd = false;
                permission.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);

            }
        }
        if(mustAdd) {
            final OCLPermission oclp = new OCLPermission();
            oclp.setEntity(otherUserId);
            oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
            permissions.add(oclp);
        }
        fo.setPermissions(permissions);

        save(fo, ctx, session);
    }

    private void save(FolderObject fo, Context ctx, Session session) {
        Connection writecon = null;
        try {
            writecon = DBPool.pickupWriteable(ctx);
            final OXFolderManager oxma = new OXFolderManagerImpl(session, writecon, writecon);
            oxma.updateFolder(fo, false, System.currentTimeMillis());
        } catch (OXException e) {
            e.printStackTrace();
        } catch (DBPoolingException e) {
            e.printStackTrace();
        } finally {
            if(writecon != null) {
                try {
                    DBPool.pushWrite(ctx, writecon);
                } catch (DBPoolingException e) {
                    //IGNORE
                }
            }
        }
    }

    public void unsharePrivateFolder(Session session, Context ctx) {
        FolderObject fo = getStandardFolderObject(session.getUserId(), ctx);
        ArrayList<OCLPermission> permissions = new ArrayList<OCLPermission>(fo.getPermissions());
        ArrayList<OCLPermission> newPermissions = new ArrayList<OCLPermission>();
        int userId = session.getUserId();
        for(int i = 0, size = permissions.size(); i < size; i++) {
            OCLPermission permission = permissions.get(i);
            if(permission.getEntity() == userId) {
                newPermissions.add(permission);
            }
        }

        fo.setPermissions(newPermissions);

        save(fo, ctx, session);

    }
}
