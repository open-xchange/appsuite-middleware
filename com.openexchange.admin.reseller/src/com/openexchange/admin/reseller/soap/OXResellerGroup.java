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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
package com.openexchange.admin.reseller.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;

import com.openexchange.admin.reseller.soap.dataobjects.ResellerContext;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.OXSOAPRMIMapper;

/**
 * SOAP Service implementing RMI Interface OXGroupInterface 
 *
 * @author choeger
 *
 */
/*
 * Note: cannot implement interface OXGroupInterface because method
 * overloading is not supported
 */
public class OXResellerGroup extends OXSOAPRMIMapper {

    public OXResellerGroup() throws RemoteException {
        super(OXGroupInterface.class);
    }

    private void addMemberWrapper(ResellerContext ctx, Group grp, User[] members, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, NoSuchGroupException, DuplicateExtensionException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        ((OXGroupInterface)rmistub).addMember(cin, grp, members, auth);
    }
    /**
     * Same as {@link OXGroupInterface#addMember(Context, Group, User[], Credentials)}
     * 
     * @param ctx
     * @param grp
     * @param members
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     * @throws NoSuchGroupException
     * @throws DuplicateExtensionException 
     */
    public void addMember(ResellerContext ctx, Group grp, User[] members, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, NoSuchGroupException, DuplicateExtensionException {
        reconnect();
        try {
            addMemberWrapper(ctx, grp, members, auth);
        } catch (ConnectException e) {
            reconnect(true);
            addMemberWrapper(ctx, grp, members, auth);
        }
    }

    private void changeWrapper(ResellerContext ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, DuplicateExtensionException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        ((OXGroupInterface)rmistub).change(cin, grp, auth);
    }
    /**
     * Same as {@link OXGroupInterface#change(Context, Group, Credentials)}
     * 
     * @param ctx
     * @param grp
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws NoSuchUserException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     * @throws DuplicateExtensionException 
     */
    public void change(ResellerContext ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, grp, auth);
        }
    }

    private Group createWrapper(ResellerContext ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXGroupInterface)rmistub).create(cin, grp, auth);
    }
    /**
     * Same as {@link OXGroupInterface#create(Context, Group, Credentials)}
     * 
     * @param ctx
     * @param grp
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws NoSuchUserException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws DuplicateExtensionException 
     */
    public Group create(ResellerContext ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, grp, auth);
        }
    }

    private void deleteWrapper(ResellerContext ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, DuplicateExtensionException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        ((OXGroupInterface)rmistub).delete(cin, grp, auth);
    }
    /**
     * Same as {@link OXGroupInterface#delete(Context, Group, Credentials)}
     * 
     * @param ctx
     * @param grp
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     * @throws DuplicateExtensionException 
     */
    public void delete(ResellerContext ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, DuplicateExtensionException {
        reconnect();
        try {
            deleteWrapper(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
            deleteWrapper(ctx, grp, auth);
        }
    }

    private void deleteMultipleWrapper(ResellerContext ctx, Group[] grps, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        ((OXGroupInterface)rmistub).delete(cin, grps, auth);
    }
    /**
     * Same as {@link OXGroupInterface#delete(Context, Group[], Credentials)}
     * 
     * @param ctx
     * @param grps
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     * @throws DuplicateExtensionException 
     */
    public void deleteMultiple(ResellerContext ctx, Group[] grps, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, DuplicateExtensionException {
        reconnect();
        try {
            deleteMultipleWrapper(ctx, grps, auth);
        } catch (ConnectException e) {
            reconnect(true);
            deleteMultipleWrapper(ctx, grps, auth);
        }
    }

    private Group getDataWrapper(ResellerContext ctx, Group grp, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXGroupInterface)rmistub).getData(cin, grp, auth);
    }
    /**
     * Same as {@link OXGroupInterface#getData(Context, Group, Credentials)}
     * 
     * @param ctx
     * @param grp
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     * @throws DuplicateExtensionException 
     */
    public Group getData(ResellerContext ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, DuplicateExtensionException {
        reconnect();
        try {
            return getDataWrapper(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return getDataWrapper(ctx, grp, auth);
        }
    }

    private Group[] getMultipleDataWrapper(ResellerContext ctx, Group[] grps, Credentials auth) throws DuplicateExtensionException, RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchGroupException, DatabaseUpdateException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXGroupInterface)rmistub).getData(cin, grps, auth);
    }
    /**
     * Same as {@link OXGroupInterface#getData(Context, Group[], Credentials)}
     * 
     * @param ctx
     * @param grps
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws NoSuchGroupException
     * @throws DatabaseUpdateException
     * @throws DuplicateExtensionException 
     */
    public Group[] getMultipleData(ResellerContext ctx, Group[] grps, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchGroupException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return getMultipleDataWrapper(ctx, grps, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return getMultipleDataWrapper(ctx, grps, auth);
        }
    }

    private Group getDefaultGroupWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXGroupInterface)rmistub).getDefaultGroup(cin, auth);
    }
    /**
     * Same as {@link OXGroupInterface#getDefaultGroup(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws DuplicateExtensionException 
     */
    public Group getDefaultGroup(ResellerContext ctx, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return getDefaultGroupWrapper(ctx, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return getDefaultGroupWrapper(ctx, auth);
        }
    }

    private User[] getMembersWrapper(ResellerContext ctx, Group grp, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXGroupInterface)rmistub).getMembers(cin, grp, auth);
    }
    /**
     * Same as {@link OXGroupInterface#getMembers(Context, Group, Credentials)}
     * 
     * @param ctx
     * @param grp
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     * @throws DuplicateExtensionException 
     */
    public User[] getMembers(ResellerContext ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, DuplicateExtensionException {
        reconnect();
        try {
            return getMembersWrapper(ctx, grp, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return getMembersWrapper(ctx, grp, auth);
        }
    }

    private Group[] listWrapper(ResellerContext ctx, String pattern, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXGroupInterface)rmistub).list(cin, pattern, auth);
    }
    /**
     * Same as {@link OXGroupInterface#list(Context, String, Credentials)}
     * 
     * @param ctx
     * @param pattern
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws DuplicateExtensionException 
     */
    public Group[] list(ResellerContext ctx, String pattern, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return listWrapper(ctx, pattern, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return listWrapper(ctx, pattern, auth);
        }
    }

    /**
     * Same as {@link OXGroupInterface#listAll(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws DuplicateExtensionException 
     */
    public Group[] listAll(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return listWrapper(ctx, "*", auth);
        } catch (ConnectException e) {
            reconnect(true);
            return listWrapper(ctx, "*", auth);
        }
    }

    private Group[] listGroupsForUserWrapper(ResellerContext ctx, User usr, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXGroupInterface)rmistub).listGroupsForUser(cin, usr, auth);
    }
    /**
     * Same as {@link OXGroupInterface#listGroupsForUser(Context, User, Credentials)}
     * 
     * @param ctx
     * @param usr
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     * @throws DuplicateExtensionException 
     */
    public Group[] listGroupsForUser(ResellerContext ctx, User usr, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        reconnect();
        try {
            return listGroupsForUserWrapper(ctx, usr, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return listGroupsForUserWrapper(ctx, usr, auth);
        }
    }

    private void removeMemberWrapper(ResellerContext ctx, Group grp, User[] members, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        ((OXGroupInterface)rmistub).removeMember(cin, grp, members, auth);
    }
    /**
     * Same as {@link OXGroupInterface#removeMember(Context, Group, User[], Credentials)}
     * 
     * @param ctx
     * @param grp
     * @param members
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchGroupException
     * @throws NoSuchUserException
     * @throws DuplicateExtensionException 
     */
    public void removeMember(ResellerContext ctx, Group grp, User[] members, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException, DuplicateExtensionException {
        reconnect();
        try {
            removeMemberWrapper(ctx, grp, members, auth);
        } catch (ConnectException e) {
            reconnect(true);
            removeMemberWrapper(ctx, grp, members, auth);
        }
    }
}
