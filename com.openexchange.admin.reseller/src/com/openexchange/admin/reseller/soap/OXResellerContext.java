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
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.OXSOAPRMIMapper;


/**
 * @author choeger
 *
 */
public class OXResellerContext extends OXSOAPRMIMapper {

    public OXResellerContext() throws RemoteException {
        super(OXContextInterface.class);
    }

    private void changeWrapper(ResellerContext ctx, UserModuleAccess access, String access_combination_name, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        if( access == null && access_combination_name == null ) {
            ((OXContextInterface)rmistub).change(cin, auth);
        } else if( access != null ) {
            ((OXContextInterface)rmistub).changeModuleAccess(cin, access, auth);
        } else if( access_combination_name != null ) {
            ((OXContextInterface)rmistub).changeModuleAccess(cin, access_combination_name, auth);   
        }
    }
    /**
     * Same as {@link OXContextInterface#change(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void change(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, null, null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, null, null, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#changeModuleAccess(Context, UserModuleAccess, Credentials)}
     * 
     * @param ctx
     * @param access
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void changeModuleAccess(ResellerContext ctx, UserModuleAccess access, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, access, null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, access, null, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#changeModuleAccess(Context, String, Credentials)}
     * 
     * @param ctx
     * @param access_combination_name
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void changeModuleAccessByName(ResellerContext ctx, String access_combination_name, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            changeWrapper(ctx, null, access_combination_name, auth);
        } catch( ConnectException e) {
            reconnect(true);
            changeWrapper(ctx, null, access_combination_name, auth);
        }
    }

    private ResellerContext createWrapper(ResellerContext ctx, User admin_user, UserModuleAccess access, String access_combination_name, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, DuplicateExtensionException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        Context res = null;
        if( access == null && access_combination_name == null ) {
            res = ((OXContextInterface)rmistub).create(cin, admin_user, auth);
        } else if( access != null ) {
            res = ((OXContextInterface)rmistub).create(cin, admin_user, access, auth);
        } else if( access_combination_name != null ) {
            res = ((OXContextInterface)rmistub).create(cin, admin_user, access_combination_name, auth);
        }
        return new ResellerContext(res);
    }
    /**
     * Same as {@link OXContextInterface#create(Context, User, Credentials)}
     * 
     * @param ctx
     * @param admin_user
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws ContextExistsException
     * @throws DuplicateExtensionException 
     */
    public ResellerContext create(ResellerContext ctx, User admin_user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, admin_user, null, null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, admin_user, null, null, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#create(Context, User, String, Credentials)}
     * 
     * @param ctx
     * @param admin_user
     * @param access_combination_name
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws ContextExistsException
     * @throws DuplicateExtensionException 
     */
    public ResellerContext createModuleAccessByName(ResellerContext ctx, User admin_user, String access_combination_name, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, admin_user, null, access_combination_name, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, admin_user, null, access_combination_name, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#create(Context, User, UserModuleAccess, Credentials)}
     * 
     * @param ctx
     * @param admin_user
     * @param access
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws ContextExistsException
     * @throws DuplicateExtensionException 
     */
    public ResellerContext createModuleAccess(ResellerContext ctx, User admin_user, UserModuleAccess access, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, DuplicateExtensionException {
        reconnect();
        try {
            return createWrapper(ctx, admin_user, access, null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return createWrapper(ctx, admin_user, access, null, auth);
        }
    }

    private void deleteWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        ((OXContextInterface)rmistub).delete(cin, auth);
    }
    /**
     * Same as {@link OXContextInterface#delete(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws DatabaseUpdateException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void delete(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            deleteWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            deleteWrapper(ctx, auth);
        }
    }

    private void disableWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        if( ctx != null ) {
            Context cin = ResellerContextUtil.resellerContext2Context(ctx);
            ((OXContextInterface)rmistub).disable(cin, auth);       
        } else {
            ((OXContextInterface)rmistub).disableAll(auth);       
        }
    }
    /**
     * Same as {@link OXContextInterface#disable(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws NoSuchReasonException
     * @throws OXContextException
     * @throws DuplicateExtensionException 
     */
    public void disable(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException, DuplicateExtensionException {
        reconnect();
        try {
            disableWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            disableWrapper(ctx, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#disableAll(Credentials)}
     * 
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws NoSuchReasonException
     * @throws OXContextException 
     * @throws NoSuchContextException 
     * @throws DuplicateExtensionException 
     */
    public void disableAll(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException, DuplicateExtensionException, NoSuchContextException, OXContextException {
        reconnect();
        try {
            disableWrapper(null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            disableWrapper(null, auth);
        }
    }

    private void downgradeWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        ((OXContextInterface)rmistub).downgrade(cin, auth);
    }
    /**
     * Same as {@link OXContextInterface#downgrade(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws DatabaseUpdateException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void downgrade(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            downgradeWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            downgradeWrapper(ctx, auth);
        }
    }

    private void enableWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        if( ctx != null ) {
            Context cin = ResellerContextUtil.resellerContext2Context(ctx);
            ((OXContextInterface)rmistub).enable(cin, auth);       
        } else {
            ((OXContextInterface)rmistub).enableAll(auth);       
        }
    }
    /**
     * Same as {@link OXContextInterface#enable(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public void enable(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            enableWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            enableWrapper(ctx, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#enableAll(Credentials)}
     * 
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException 
     * @throws NoSuchContextException 
     * @throws DuplicateExtensionException 
     */
    public void enableAll(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, DuplicateExtensionException, NoSuchContextException, InvalidDataException {
        reconnect();
        try {
            enableWrapper(null, auth);
        } catch( ConnectException e) {
            reconnect(true);
            enableWrapper(null, auth);
        }
    }

    private String getAccessCombinationNameWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXContextInterface)rmistub).getAccessCombinationName(cin, auth);
    }
    /**
     * Same as {@link OXContextInterface#getAccessCombinationName(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public String getAccessCombinationName(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            return getAccessCombinationNameWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return getAccessCombinationNameWrapper(ctx, auth);
        }
    }

    private ResellerContext getDataWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        Context res = ((OXContextInterface)rmistub).getData(cin, auth);
        return new ResellerContext(res);
    }
    /**
     * Same as {@link OXContextInterface#getData(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public ResellerContext getData(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            return getDataWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return getDataWrapper(ctx, auth);
        }
    }

    private UserModuleAccess getModuleAccessWrapper(ResellerContext ctx, Credentials auth) throws DuplicateExtensionException, RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        Context cin = ResellerContextUtil.resellerContext2Context(ctx);
        return ((OXContextInterface)rmistub).getModuleAccess(cin, auth);
    }
    /**
     * Same as {@link OXContextInterface#getModuleAccess(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DuplicateExtensionException 
     */
    public UserModuleAccess getModuleAccess(ResellerContext ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DuplicateExtensionException {
        reconnect();
        try {
            return getModuleAccessWrapper(ctx, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return getModuleAccessWrapper(ctx, auth);
        }
    }

    private ResellerContext[] listWrapper(String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        final Context[] allctx = ((OXContextInterface)rmistub).list(search_pattern, auth);
        final ResellerContext []ret = new ResellerContext[allctx.length];
        for(int i=0; i<allctx.length; i++) {
            ret[i] = new ResellerContext(allctx[i]);
        }
        return ret;
    }
    /**
     * Same as {@link OXContextInterface#list(String, Credentials)}
     * 
     * @param search_pattern
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    public ResellerContext[] list(String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return listWrapper(search_pattern, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return listWrapper(search_pattern, auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#listAll(Credentials)}
     * 
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    public ResellerContext[] listAll(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return listWrapper("*", auth);
        } catch( ConnectException e) {
            reconnect(true);
            return listWrapper("*", auth);
        }
    }

    private ResellerContext[] listByDatabaseWrapper(Database db, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException{
        final Context[] allctx = ((OXContextInterface)rmistub).listByDatabase(db, auth);
        final ResellerContext []ret = new ResellerContext[allctx.length];
        for(int i=0; i<allctx.length; i++) {
            ret[i] = new ResellerContext(allctx[i]);
        }
        return ret;
    }
    /**
     * Same as {@link OXContextInterface#listByDatabase(Database, Credentials)}
     * 
     * @param db
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws NoSuchDatabaseException
     */
    public ResellerContext[] listByDatabase(Database db, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException {
        reconnect();
        try {
            return listByDatabaseWrapper(db, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return listByDatabaseWrapper(db, auth);
        }
    }

    private ResellerContext[] listByFilestoreWrapper(Filestore fs, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchFilestoreException {
        final Context[] allctx = ((OXContextInterface)rmistub).listByFilestore(fs, auth);
        final ResellerContext []ret = new ResellerContext[allctx.length];
        for(int i=0; i<allctx.length; i++) {
            ret[i] = new ResellerContext(allctx[i]);
        }
        return ret;
    }
    /**
     * Same as {@link OXContextInterface#listByFilestore(Filestore, Credentials)}
     * 
     * @param fs
     * @param auth
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     * @throws NoSuchFilestoreException
     */
    public ResellerContext[] listByFilestore(Filestore fs, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchFilestoreException {
        reconnect();
        try {
            return listByFilestoreWrapper(fs, auth);
        } catch( ConnectException e) {
            reconnect(true);
            return listByFilestoreWrapper(fs, auth);
        }
    }


}
