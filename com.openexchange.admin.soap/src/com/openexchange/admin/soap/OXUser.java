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

import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * SOAP Service implementing RMI Interface OXUserInterface
 * 
 * @author choeger
 *
 */
/*
 * Note: cannot implement interface OXUserInterface because method
 * overloading is not supported
 */
public class OXUser extends OXSOAPRMIMapper {

    public OXUser() throws RemoteException {
        super(OXUserInterface.class);
    }

    /**
     * Same as {@link OXUserInterface#change(Context, User, Credentials)}
     * 
     * @param ctx
     * @param usrdata
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void change(Context ctx, User usrdata, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        reconnect();
        try {
            ((OXUserInterface)rmistub).change(ctx, usrdata, auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).change(ctx, usrdata, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#changeModuleAccess(Context, User, UserModuleAccess, Credentials)}
     * 
     * @param ctx
     * @param user
     * @param moduleAccess
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void changeByModuleAccess(Context ctx, User user, UserModuleAccess moduleAccess, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        reconnect();
        try {
            ((OXUserInterface)rmistub).changeModuleAccess(ctx, user, moduleAccess, auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).changeModuleAccess(ctx, user, moduleAccess, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#changeModuleAccess(Context, User, String, Credentials)}
     * 
     * @param ctx
     * @param user
     * @param access_combination_name
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void changeByModuleAccessName(Context ctx, User user, String access_combination_name, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        reconnect();
        try {
            ((OXUserInterface)rmistub).changeModuleAccess(ctx, user, access_combination_name, auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).changeModuleAccess(ctx, user, access_combination_name, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#create(Context, User, UserModuleAccess, Credentials)}
     * 
     * @param ctx
     * @param usrdata
     * @param access
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User createByModuleAccess(Context ctx, User usrdata, UserModuleAccess access, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXUserInterface)rmistub).create(ctx, usrdata, access, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUserInterface)rmistub).create(ctx, usrdata, access, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#create(Context, User, String, Credentials)}
     * 
     * @param ctx
     * @param usrdata
     * @param access_combination_name
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User createByModuleAccessName(Context ctx, User usrdata, String access_combination_name, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXUserInterface)rmistub).create(ctx, usrdata, access_combination_name, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUserInterface)rmistub).create(ctx, usrdata, access_combination_name, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#create(Context, User, Credentials)}
     * 
     * @param ctx
     * @param usrdata
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User create(Context ctx, User usrdata, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXUserInterface)rmistub).create(ctx, usrdata, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUserInterface)rmistub).create(ctx, usrdata, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#delete(Context, User[], Credentials)}
     * 
     * @param ctx
     * @param users
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void deleteMultiple(Context ctx, User[] users, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        reconnect();
        try {
            ((OXUserInterface)rmistub).delete(ctx, users, auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).delete(ctx, users, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#delete(Context, User, Credentials)}
     * 
     * @param ctx
     * @param user
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void delete(Context ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        reconnect();
        try {
            ((OXUserInterface)rmistub).delete(ctx, user, auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).delete(ctx, user, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#getAccessCombinationName(Context, User, Credentials)}
     * 
     * @param ctx
     * @param user
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public String getAccessCombinationName(Context ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        reconnect();
        try {
            return ((OXUserInterface)rmistub).getAccessCombinationName(ctx, user, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUserInterface)rmistub).getAccessCombinationName(ctx, user, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#getData(Context, User[], Credentials)}
     * 
     * @param ctx
     * @param users
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws NoSuchUserException
     * @throws DatabaseUpdateException
     */
    public User[] getMultipleData(Context ctx, User[] users, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXUserInterface)rmistub).getData(ctx, users, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUserInterface)rmistub).getData(ctx, users, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#getData(Context, User, Credentials)}
     * 
     * @param ctx
     * @param user
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws NoSuchUserException
     * @throws DatabaseUpdateException
     */
    public User getData(Context ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXUserInterface)rmistub).getData(ctx, user, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUserInterface)rmistub).getData(ctx, user, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#getModuleAccess(Context, User, Credentials)}
     * 
     * @param ctx
     * @param user
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public UserModuleAccess getModuleAccess(Context ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        reconnect();
        try {
            return ((OXUserInterface)rmistub).getModuleAccess(ctx, user, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUserInterface)rmistub).getModuleAccess(ctx, user, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#list(Context, String, Credentials)}
     * 
     * @param ctx
     * @param search_pattern
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User[] list(Context ctx, String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXUserInterface)rmistub).list(ctx, search_pattern, auth);
        } catch (ConnectException e) {
            reconnect(true);
            // If the reconnect is successful we try again
            return ((OXUserInterface)rmistub).list(ctx, search_pattern, auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#listAll(Context, Credentials)}
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
    public User[] listAll(Context ctx, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        reconnect();
        try {
            return ((OXUserInterface)rmistub).listAll(ctx, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUserInterface)rmistub).listAll(ctx, auth);
        }
    }

}
