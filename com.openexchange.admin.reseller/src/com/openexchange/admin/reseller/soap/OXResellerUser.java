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
import com.openexchange.admin.lib.rmi.OXUserInterface;
import com.openexchange.admin.reseller.soap.dataobjects.ResellerContext;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.OXSOAPRMIMapper;

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
public class OXResellerUser extends OXSOAPRMIMapper {

    public OXResellerUser() throws RemoteException {
        super(OXUserInterface.class);
    }

    private void changeWrapper(ResellerContext ctx, User usrdata, UserModuleAccess access, String access_combination_name, Credentials auth) throws DuplicateExtensionException, RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        if( access == null && access_combination_name == null ) {
            ((OXUserInterface)rmistub).change(cin, usrdata, auth);
        } else if( access != null ) {
            ((OXUserInterface)rmistub).changeModuleAccess(cin, usrdata, access, auth);
        } else if( access_combination_name != null ) {
            ((OXUserInterface)rmistub).changeModuleAccess(cin, usrdata, access_combination_name, auth);
        }
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
     * @throws DuplicateExtensionException 
     */
    public void change(ResellerContext ctx, User usrdata, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, usrdata, null, null, auth);
        } catch (ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, usrdata, null, null, auth);
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
     * @throws DuplicateExtensionException 
     */
    public void changeByModuleAccess(ResellerContext ctx, User user, UserModuleAccess moduleAccess, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, user, moduleAccess, null, auth);
        } catch (ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, user, moduleAccess, null, auth);
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
     * @throws DuplicateExtensionException 
     */
    public void changeByModuleAccessName(ResellerContext ctx, User user, String access_combination_name, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, user, null, access_combination_name, auth);
        } catch (ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, user, null, access_combination_name, auth);
        }
    }

    private User createWrapper(ResellerContext ctx, User usrdata, UserModuleAccess access, String access_combination_name, Credentials auth) throws DuplicateExtensionException, RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        if( access == null && access_combination_name == null ) {
            return ((OXUserInterface)rmistub).create(cin, usrdata, auth);
        } else if( access != null ) {
            return ((OXUserInterface)rmistub).create(cin, usrdata, access, auth);
        } else if( access_combination_name != null ) {
            return ((OXUserInterface)rmistub).create(cin, usrdata, access_combination_name, auth);
        }
        return null;
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
     * @throws DuplicateExtensionException 
     */
    public User createByModuleAccess(ResellerContext ctx, User usrdata, UserModuleAccess access, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, usrdata, access, null, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, usrdata, access, null, auth);
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
     * @throws DuplicateExtensionException 
     */
    public User createByModuleAccessName(ResellerContext ctx, User usrdata, String access_combination_name, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, usrdata, null, access_combination_name, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, usrdata, null, access_combination_name, auth);
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
     * @throws DuplicateExtensionException 
     */
    public User create(ResellerContext ctx, User usrdata, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, usrdata, null, null, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, usrdata, null, null, auth);
        }
    }

    private void deleteMultipleWrapper(ResellerContext ctx, User[] users, Credentials auth) throws DuplicateExtensionException, RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        ((OXUserInterface)rmistub).delete(cin, users, auth);
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
     * @throws DuplicateExtensionException 
     */
    public void deleteMultiple(ResellerContext ctx, User[] users, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        reconnect();
        try {
            deleteMultipleWrapper(ctx, users, auth);
        } catch (ConnectException e) {
            reconnect(true);
            deleteMultipleWrapper(ctx, users, auth);
        }
    }

    private void deleteWrapper(ResellerContext ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        ((OXUserInterface)rmistub).delete(cin, user, auth);
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
     * @throws DuplicateExtensionException 
     */
    public void delete(ResellerContext ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        reconnect();
        try {
            deleteWrapper(ctx, user, auth);
        } catch (ConnectException e) {
            reconnect(true);
            deleteWrapper(ctx, user, auth);
        }
    }

    private String getAccessCombinationNameWrapper(ResellerContext ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXUserInterface)rmistub).getAccessCombinationName(cin, user, auth);
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
     * @throws DuplicateExtensionException 
     */
    public String getAccessCombinationName(ResellerContext ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        reconnect();
        try {
            return getAccessCombinationNameWrapper(ctx, user, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return getAccessCombinationNameWrapper(ctx, user, auth);
        }
    }

    private User[] getMultipleDataWrapper(ResellerContext ctx, User[] users, Credentials auth) throws DuplicateExtensionException, RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXUserInterface)rmistub).getData(cin, users, auth);   
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
     * @throws DuplicateExtensionException 
     */
    public User[] getMultipleData(ResellerContext ctx, User[] users, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return getMultipleDataWrapper(ctx, users, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return getMultipleDataWrapper(ctx, users, auth);
        }
    }

    private User getDataWrapper(ResellerContext ctx, User user, Credentials auth) throws DuplicateExtensionException, RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXUserInterface)rmistub).getData(cin, user, auth);
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
     * @throws DuplicateExtensionException 
     */
    public User getData(ResellerContext ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return getDataWrapper(ctx, user, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return getDataWrapper(ctx, user, auth);
        }
    }

    private UserModuleAccess getModuleAccessWrapper(ResellerContext ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXUserInterface)rmistub).getModuleAccess(cin, user, auth);
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
     * @throws DuplicateExtensionException 
     */
    public UserModuleAccess getModuleAccess(ResellerContext ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, DuplicateExtensionException {
        reconnect();
        try {
            return getModuleAccessWrapper(ctx, user, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return getModuleAccessWrapper(ctx, user, auth);
        }
    }

    private User[] listWrapper(ResellerContext ctx, String search_pattern, Credentials auth) throws DuplicateExtensionException, RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        if( search_pattern != null ) {
            return ((OXUserInterface)rmistub).list(cin, search_pattern, auth);
        } else {
            return ((OXUserInterface)rmistub).listAll(cin, auth);            
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
     * @throws DuplicateExtensionException 
     */
    public User[] list(ResellerContext ctx, String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return listWrapper(ctx, search_pattern, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return listWrapper(ctx, search_pattern, auth);
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
     * @throws DuplicateExtensionException 
     */
    public User[] listAll(ResellerContext ctx, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, DuplicateExtensionException {
        reconnect();
        try {
            return listWrapper(ctx, null, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return listWrapper(ctx, null, auth);
        }
    }

}
