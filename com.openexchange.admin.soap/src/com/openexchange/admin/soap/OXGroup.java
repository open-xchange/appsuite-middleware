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
package com.openexchange.admin.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;

import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class OXGroup extends OXSOAPRMIMapper implements OXGroupInterface {

    public OXGroup() throws RemoteException {
        super(OXGroupInterface.class);
    }

    public void addMember(Context ctx, Group grp, User[] members, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, NoSuchGroupException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).addMember(ctx, grp, members, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public void change(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).change(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public Group create(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXGroupInterface)rmistub).create(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public void delete(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).delete(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public void delete(Context ctx, Group[] grps, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).delete(ctx, grps, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public Group getData(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            return ((OXGroupInterface)rmistub).getData(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public Group[] getData(Context ctx, Group[] grps, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchGroupException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXGroupInterface)rmistub).getData(ctx, grps, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public Group getDefaultGroup(Context ctx, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXGroupInterface)rmistub).getDefaultGroup(ctx, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public User[] getMembers(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            return ((OXGroupInterface)rmistub).getMembers(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public Group[] list(Context ctx, String pattern, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXGroupInterface)rmistub).list(ctx, pattern, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public Group[] listAll(Context ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXGroupInterface)rmistub).listAll(ctx, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public Group[] listGroupsForUser(Context ctx, User usr, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        reconnect();
        try {
            return ((OXGroupInterface)rmistub).listGroupsForUser(ctx, usr, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public void removeMember(Context ctx, Group grp, User[] members, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).removeMember(ctx, grp, members, auth);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }
}
