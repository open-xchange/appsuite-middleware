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
package com.openexchange.admin.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.dataobjects.Context;
import com.openexchange.admin.soap.dataobjects.Group;
import com.openexchange.admin.soap.dataobjects.User;

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
public class OXGroup extends OXSOAPRMIMapper {

    public OXGroup() throws RemoteException {
        super(OXGroupInterface.class);
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
     */
    public void addMember(Context ctx, Group grp, User[] members, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, NoSuchGroupException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).addMember(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), SOAPUtils.soapUsers2Users(members), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXGroupInterface)rmistub).addMember(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), SOAPUtils.soapUsers2Users(members), auth);
        }
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
     */
    public void change(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).change(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXGroupInterface)rmistub).change(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth);
        }
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
     */
    public Group create(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, NoSuchUserException, StorageException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return new Group(((OXGroupInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new Group(((OXGroupInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth));
        }
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
     */
    public void delete(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXGroupInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth);
        }
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
     */
    public void deleteMultiple(Context ctx, Group[] grps, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroups2Groups(grps), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXGroupInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroups2Groups(grps), auth);
        }
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
     */
    public Group getData(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            return new Group(((OXGroupInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new Group(((OXGroupInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth));
        }
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
     */
    public Group[] getMultipleData(Context ctx, Group[] grps, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchGroupException, DatabaseUpdateException {
        reconnect();
        try {
            return SOAPUtils.groups2SoapGroups(((OXGroupInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroups2Groups(grps), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.groups2SoapGroups(((OXGroupInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroups2Groups(grps), auth));
        }
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
     */
    public Group getDefaultGroup(Context ctx, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return new Group(((OXGroupInterface)rmistub).getDefaultGroup(SOAPUtils.soapContext2Context(ctx), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new Group(((OXGroupInterface)rmistub).getDefaultGroup(SOAPUtils.soapContext2Context(ctx), auth));
        }
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
     */
    public User[] getMembers(Context ctx, Group grp, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
        reconnect();
        try {
            return SOAPUtils.users2SoapUsers(((OXGroupInterface)rmistub).getMembers(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.users2SoapUsers(((OXGroupInterface)rmistub).getMembers(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), auth));
        }
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
     */
    public Group[] list(Context ctx, String pattern, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return SOAPUtils.groups2SoapGroups(((OXGroupInterface)rmistub).list(SOAPUtils.soapContext2Context(ctx), pattern, auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.groups2SoapGroups(((OXGroupInterface)rmistub).list(SOAPUtils.soapContext2Context(ctx), pattern, auth));
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
     */
    public Group[] listAll(Context ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return SOAPUtils.groups2SoapGroups(((OXGroupInterface)rmistub).listAll(SOAPUtils.soapContext2Context(ctx), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.groups2SoapGroups(((OXGroupInterface)rmistub).listAll(SOAPUtils.soapContext2Context(ctx), auth));
        }
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
     */
    public Group[] listGroupsForUser(Context ctx, User usr, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        reconnect();
        try {
            return SOAPUtils.groups2SoapGroups(((OXGroupInterface)rmistub).listGroupsForUser(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usr), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.groups2SoapGroups(((OXGroupInterface)rmistub).listGroupsForUser(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usr), auth));
        }
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
     */
    public void removeMember(Context ctx, Group grp, User[] members, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchGroupException, NoSuchUserException {
        reconnect();
        try {
            ((OXGroupInterface)rmistub).removeMember(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), SOAPUtils.soapUsers2Users(members), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXGroupInterface)rmistub).removeMember(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapGroup2Group(grp), SOAPUtils.soapUsers2Users(members), auth);
        }
    }
}
