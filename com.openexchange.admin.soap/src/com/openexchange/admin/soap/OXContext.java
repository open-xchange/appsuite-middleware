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
package com.openexchange.admin.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.dataobjects.Context;
import com.openexchange.admin.soap.dataobjects.Database;
import com.openexchange.admin.soap.dataobjects.Filestore;
import com.openexchange.admin.soap.dataobjects.User;
import com.openexchange.admin.soap.dataobjects.UserModuleAccess;


/**
 * SOAP Service implementing RMI Interface OXContextInterface
 * 
 * @author choeger
 *
 */
/*
 * Note: cannot implement interface OXContextInterface because method
 * overloading is not supported
 */
public class OXContext extends OXSOAPRMIMapper {

    public OXContext() throws RemoteException {
        super(OXContextInterface.class);
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
     */
    public void change(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        reconnect();
        try {
            ((OXContextInterface)rmistub).change(SOAPUtils.soapContext2Context(ctx), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            ((OXContextInterface)rmistub).change(SOAPUtils.soapContext2Context(ctx), auth);
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
     */
    public void changeModuleAccess(final Context ctx, final UserModuleAccess access, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        reconnect();
        try {
            ((OXContextInterface)rmistub).changeModuleAccess(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapModuleAccess2ModuleAccess(access), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            ((OXContextInterface)rmistub).changeModuleAccess(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapModuleAccess2ModuleAccess(access), auth);
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
     */
    public void changeModuleAccessByName(final Context ctx, final String access_combination_name, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        reconnect();
        try {
            ((OXContextInterface)rmistub).changeModuleAccess(SOAPUtils.soapContext2Context(ctx), access_combination_name, auth);
        } catch( final ConnectException e) {
            reconnect(true);
            ((OXContextInterface)rmistub).changeModuleAccess(SOAPUtils.soapContext2Context(ctx), access_combination_name, auth);
        }
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
     */
    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        reconnect();
        try {
            return new Context(((OXContextInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(admin_user), auth));
        } catch( final ConnectException e) {
            reconnect(true);
            return new Context(((OXContextInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(admin_user), auth));
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
     */
    public Context createModuleAccessByName(final Context ctx, final User admin_user, final String access_combination_name, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        reconnect();
        try {
            return new Context(((OXContextInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(admin_user), access_combination_name, auth));
        } catch( final ConnectException e) {
            reconnect(true);
            return new Context(((OXContextInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(admin_user), access_combination_name, auth));
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
     */
    public Context createModuleAccess(final Context ctx, final User admin_user, final UserModuleAccess access, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException {
        reconnect();
        try {
            return new Context(((OXContextInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(admin_user), SOAPUtils.soapModuleAccess2ModuleAccess(access), auth));
        } catch( final ConnectException e) {
            reconnect(true);
            return new Context(((OXContextInterface)rmistub).create(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapUser2User(admin_user), SOAPUtils.soapModuleAccess2ModuleAccess(access), auth));
        }
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
     */
    public void delete(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        reconnect();
        try {
            ((OXContextInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            ((OXContextInterface)rmistub).delete(SOAPUtils.soapContext2Context(ctx), auth);
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
     */
    public void disable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchReasonException, OXContextException {
        reconnect();
        try {
            ((OXContextInterface)rmistub).disable(SOAPUtils.soapContext2Context(ctx), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            ((OXContextInterface)rmistub).disable(SOAPUtils.soapContext2Context(ctx), auth);
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
     */
    public void disableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchReasonException {
        reconnect();
        try {
            ((OXContextInterface)rmistub).disableAll(auth);
        } catch( final ConnectException e) {
            reconnect(true);
            ((OXContextInterface)rmistub).disableAll(auth);
        }
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
     */
    public void downgrade(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException {
        reconnect();
        try {
            ((OXContextInterface)rmistub).downgrade(SOAPUtils.soapContext2Context(ctx), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            ((OXContextInterface)rmistub).downgrade(SOAPUtils.soapContext2Context(ctx), auth);
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
     */
    public void enable(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        reconnect();
        try {
            ((OXContextInterface)rmistub).enable(SOAPUtils.soapContext2Context(ctx), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            ((OXContextInterface)rmistub).enable(SOAPUtils.soapContext2Context(ctx), auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#enableAll(Credentials)}
     * 
     * @param auth
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     */
    public void enableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException {
        reconnect();
        try {
            ((OXContextInterface)rmistub).enableAll(auth);
        } catch( final ConnectException e) {
            reconnect(true);
            ((OXContextInterface)rmistub).enableAll(auth);
        }
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
     */
    public String getAccessCombinationName(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        reconnect();
        try {
            return ((OXContextInterface)rmistub).getAccessCombinationName(SOAPUtils.soapContext2Context(ctx), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            return ((OXContextInterface)rmistub).getAccessCombinationName(SOAPUtils.soapContext2Context(ctx), auth);
        }
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
     */
    public Context getData(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        reconnect();
        try {
            return new Context(((OXContextInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), auth));
        } catch( final ConnectException e) {
            reconnect(true);
            return new Context(((OXContextInterface)rmistub).getData(SOAPUtils.soapContext2Context(ctx), auth));
        }
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
     */
    public UserModuleAccess getModuleAccess(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        reconnect();
        try {
            return new UserModuleAccess(((OXContextInterface)rmistub).getModuleAccess(SOAPUtils.soapContext2Context(ctx), auth));
        } catch( final ConnectException e) {
            reconnect(true);
            return new UserModuleAccess(((OXContextInterface)rmistub).getModuleAccess(SOAPUtils.soapContext2Context(ctx), auth));
        }
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
    public Context[] list(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return SOAPUtils.contexts2SoapContexts(((OXContextInterface)rmistub).list(search_pattern, auth));
        } catch( final ConnectException e) {
            reconnect(true);
            return SOAPUtils.contexts2SoapContexts(((OXContextInterface)rmistub).list(search_pattern, auth));
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
    public Context[] listAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return SOAPUtils.contexts2SoapContexts(((OXContextInterface)rmistub).listAll(auth));
        } catch( final ConnectException e) {
            reconnect(true);
            return SOAPUtils.contexts2SoapContexts(((OXContextInterface)rmistub).listAll(auth));
        }
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
    public Context[] listByDatabase(final Database db, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchDatabaseException {
        reconnect();
        try {
            return SOAPUtils.contexts2SoapContexts(((OXContextInterface)rmistub).listByDatabase(SOAPUtils.soapDatabase2Database(db), auth));
        } catch( final ConnectException e) {
            reconnect(true);
            return SOAPUtils.contexts2SoapContexts(((OXContextInterface)rmistub).listByDatabase(SOAPUtils.soapDatabase2Database(db), auth));
        }
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
    public Context[] listByFilestore(final Filestore fs, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchFilestoreException {
        reconnect();
        try {
            return SOAPUtils.contexts2SoapContexts(((OXContextInterface)rmistub).listByFilestore(SOAPUtils.soapFilestore2Filestore(fs), auth));
        } catch( final ConnectException e) {
            reconnect(true);
            return SOAPUtils.contexts2SoapContexts(((OXContextInterface)rmistub).listByFilestore(SOAPUtils.soapFilestore2Filestore(fs), auth));
        }
    }

    /**
     * Same as {@link OXContextInterface#moveContextDatabase(Context, Database, Credentials)}
     * 
     * @param ctx
     * @param dst_database_id
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     * @throws OXContextException
     */
    public int moveContextDatabase(final Context ctx, final Database dst_database_id, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, OXContextException {
        reconnect();
        try {
            return ((OXContextInterface)rmistub).moveContextDatabase(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapDatabase2Database(dst_database_id), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            return ((OXContextInterface)rmistub).moveContextDatabase(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapDatabase2Database(dst_database_id), auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#moveContextFilestore(Context, Filestore, Credentials)}
     * 
    * @param ctx
     * @param dst_filestore_id
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     * @throws NoSuchFilestoreException
     * @throws NoSuchReasonException
     * @throws OXContextException
     */
    public int moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException {
        reconnect();
        try {
            return ((OXContextInterface)rmistub).moveContextFilestore(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapFilestore2Filestore(dst_filestore_id), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            return ((OXContextInterface)rmistub).moveContextFilestore(SOAPUtils.soapContext2Context(ctx), SOAPUtils.soapFilestore2Filestore(dst_filestore_id), auth);
        }
    }

    /**
     * Same as {@link OXContextInterface#getAdminId(Context, Credentials)}
     * 
     * @param ctx
     * @param auth
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws NoSuchContextException 
     */
    public int getAdminId(final Context ctx, final Credentials auth) throws RemoteException, InvalidCredentialsException, StorageException, NoSuchContextException {
        reconnect();
        try {
            return ((OXContextInterface)rmistub).getAdminId(SOAPUtils.soapContext2Context(ctx), auth);
        } catch( final ConnectException e) {
            reconnect(true);
            return ((OXContextInterface)rmistub).getAdminId(SOAPUtils.soapContext2Context(ctx), auth);
        }
    }

}
