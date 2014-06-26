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
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.dataobjects.Context;
import com.openexchange.admin.soap.dataobjects.User;
import com.openexchange.admin.soap.dataobjects.UserModuleAccess;

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
            ((OXUserInterface)rmistub).change(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usrdata), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).change(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usrdata), auth);
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
            ((OXUserInterface)rmistub).changeModuleAccess(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), SOAPUtils.soapModuleAccess2ModuleAccess(moduleAccess), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).changeModuleAccess(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), SOAPUtils.soapModuleAccess2ModuleAccess(moduleAccess), auth);
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
            ((OXUserInterface)rmistub).changeModuleAccess(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), access_combination_name, auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).changeModuleAccess(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), access_combination_name, auth);
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
            return new User(((OXUserInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usrdata), SOAPUtils.soapModuleAccess2ModuleAccess(access), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new User(((OXUserInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usrdata), SOAPUtils.soapModuleAccess2ModuleAccess(access), auth));
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
            return new User(((OXUserInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usrdata), access_combination_name, auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new User(((OXUserInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usrdata), access_combination_name, auth));
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
            return new User(((OXUserInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usrdata), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new User(((OXUserInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(usrdata), auth));
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
            ((OXUserInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUsers2Users(users), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUsers2Users(users), auth);
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
            ((OXUserInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), auth);
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
            return ((OXUserInterface)rmistub).getAccessCombinationName(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUserInterface)rmistub).getAccessCombinationName(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), auth);
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
            return SOAPUtils.users2SoapUsers(((OXUserInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUsers2Users(users), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.users2SoapUsers(((OXUserInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUsers2Users(users), auth));
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
            return new User(((OXUserInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new User(((OXUserInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), auth));
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
            return new UserModuleAccess(((OXUserInterface)rmistub).getModuleAccess(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new UserModuleAccess(((OXUserInterface)rmistub).getModuleAccess(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(user), auth));
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
            return SOAPUtils.users2SoapUsers(((OXUserInterface)rmistub).list(SOAPUtils.soapContext2Context(ctx), search_pattern, auth));
        } catch (ConnectException e) {
            reconnect(true);
            // If the reconnect is successful we try again
            return SOAPUtils.users2SoapUsers(((OXUserInterface)rmistub).list(SOAPUtils.soapContext2Context(ctx), search_pattern, auth));
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
            return SOAPUtils.users2SoapUsers(((OXUserInterface)rmistub).listAll(SOAPUtils.soapContext2Context(ctx), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.users2SoapUsers(((OXUserInterface)rmistub).listAll(SOAPUtils.soapContext2Context(ctx), auth));
        }
    }


    /**
     * This method changes module Permissions for all (!) users in all (!) contexts. This can be filtered by already existing access combinations.
     * If no filter is given, all users are changed.
     *
     * @param filter The call affects only users with exactly this access combination. This is either a String representing a defined module access combination or an Integer (masked as String) for direct definitions. null for no filter.
     * @param addAccess Access rights to be added
     * @param removeAccess Access rights to be removed
     * @param auth Credentials for authenticating against server. Must be the master Admin.
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public void changeModuleAccessGlobal(String filter, UserModuleAccess addAccess, UserModuleAccess removeAccess, Credentials auth) throws RemoteException, InvalidCredentialsException, StorageException, InvalidDataException {
        reconnect();
        try {
            ((OXUserInterface)rmistub).changeModuleAccessGlobal(filter, SOAPUtils.soapModuleAccess2ModuleAccess(addAccess), SOAPUtils.soapModuleAccess2ModuleAccess(removeAccess), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUserInterface)rmistub).changeModuleAccessGlobal(filter, SOAPUtils.soapModuleAccess2ModuleAccess(addAccess), SOAPUtils.soapModuleAccess2ModuleAccess(removeAccess), auth);
        }
    }

    /**
     * Same as {@link OXUserInterface#getContextAdmin(Context, Credentials)}
     *
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException
     *//*
    public User getContextAdmin(Context ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            return new User(((OXUserInterface)rmistub).getContextAdmin(SOAPUtils.soapContext2Context(ctx), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new User(((OXUserInterface)rmistub).getContextAdmin(SOAPUtils.soapContext2Context(ctx), auth));
        }
    }*/

}
