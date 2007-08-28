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
package com.openexchange.admin.storage.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.PropertyHandler.PropertyFiles;

/**
 * This interface provides an abstraction to the storage of the user information
 *
 * @author d7
 * @author cutmasta
 *
 */
public abstract class OXUserStorageInterface {
    
    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class<? extends OXUserStorageInterface> implementingClass;

    private static final Log log = LogFactory.getLog(OXUserStorageInterface.class);
    
    protected static AdminCache cache = null;

    protected static PropertyHandler prop = null;

    static {
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();
    }

    /**
     * Creates a new instance implementing the group storage interface.
     * @return an instance implementing the group storage interface.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException Storage exception
     */
    public static OXUserStorageInterface getInstance() throws StorageException {
        synchronized (OXUserStorageInterface.class) {
            if (null == implementingClass) {
                String className = null;
                try {
                    className = prop.getString(PropertyFiles.ADMIN, AdminProperties.Storage.USER_STORAGE, null);
                } catch (final InvalidDataException e1) {
                    log.fatal("Invalid data in config file", e1);
                    AdminDaemon.shutdown();
                }
                if (null != className) {
                    try {
                        implementingClass = Class.forName(className).asSubclass(OXUserStorageInterface.class);
                    } catch (final ClassNotFoundException e) {
                        log.error(e.getMessage(), e);
                        throw new StorageException(e);
                    }
                } else {
                    final StorageException storageException = new StorageException("Property for user_storage not defined");
                    log.error(storageException.getMessage(), storageException);
                    throw storageException;
                }
            }
        }
        Constructor<? extends OXUserStorageInterface> cons;
        try {
            cons = implementingClass.getConstructor(new Class[] {});
            return cons.newInstance(new Object[] {});
        } catch (final SecurityException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e);
        } catch (final NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e);
        } catch (final IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e);
        } catch (final InstantiationException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e);
        } catch (final IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e);
        } catch (final InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e);
        }
    }

    /**
     * Retrieve the ModuleAccess for an user.
     *
     * @param context Context
     * @param user_id long containing the user id.     
     * @return UserModuleAccess containing the module access rights.
     * @throws StorageException
     *
     */
    public abstract UserModuleAccess getModuleAccess (final Context ctx,final int user_id)  throws StorageException;
    
    /**
     * Manipulate user module access within the given context.    
     * @param ctx Context object.
     * @param user_id long containing the user id.
     * @param moduleAccess UserModuleAccess containing module access.     
     *
     * @throws StorageException
     */
    public abstract void changeModuleAccess(final Context ctx,final int user_id,final UserModuleAccess moduleAccess) throws StorageException;
    
    /**
     * Retrieve user objects for a range of users identified by User.getUsername().
     *
     * @param context Context object.
     * @param users User[] with users to get data for. Attention: These objects will be cloned by a shallow copy, so
     * non native attributes will point to the same reference after this method
     * @return User[] containing result objects.     
     * @throws RemoteException
     *
     */
    public abstract User[] getData(final Context ctx, User[] users) throws StorageException;
    
    /**
     * Manipulate user data within the given context.
     *    
     * @param context Context in which the new user will be modified.
     * @param usrdata User containing user data. 
     * @throws StorageException
     */
    public abstract void change(final Context ctx,final User usrdata) throws StorageException;
    
    /**
     * Changes last modified data in database
     */
    public abstract void changeLastModified( final int user_id, final Context ctx, final Connection write_ox_con ) throws StorageException;
    
    /** 
     * Create new user in given connection with given contact and user id
     * If the uid number feature is active then also supply a correct uid_number(IDGenerator with Type UID_NUMBER).Else set this to -1
     * @throws InvalidDataException 
     */
    public abstract int create(final Context ctx, final User usrdata, final UserModuleAccess moduleAccess, final Connection write_ox_con, final int internal_user_id, final int contact_id,final int uid_number ) throws StorageException;
    
    /**
     * Create new user in context ctx
     * @throws InvalidDataException 
     */
    public abstract int create(final Context ctx,final User usrdata, final UserModuleAccess moduleAccess) throws StorageException;
    
    
    /**
     * Retrieve all user ids for a given context.
     *
     * @param ctx numerical context identifier     
     * @return long[] containing user ids. 
     * @throws StorageException
     *
     */
    public abstract int[] getAll(final Context ctx) throws StorageException;

    /**
     * Retrieve all user objects for a given context. Which match the given search_pattern
     *
     * @param ctx numerical context identifier     
     * @return long[] containing user ids. 
     * @throws StorageException
     *
     */
    public abstract User[] list(final Context ctx, final String search_pattern) throws StorageException;
    
    /**
     * Delete an user or multiple from given context in given connection
     */
    /**
     * Delete an user or multiple from given context in given connection
     */
    @Deprecated
    public abstract void delete(final Context ctx, final int[] user_ids, final Connection write_ox_con ) throws StorageException;

    /**
     * Delete an user or multiple from given context in given connection
     */
    public abstract void delete(final Context ctx, final User[] user_ids, final Connection write_ox_con ) throws StorageException;
    
    /**
     * Delete users in given context
     */
    @Deprecated
    public abstract void delete( final Context ctx, final int[] user_ids) throws StorageException;
    
    /**
     * Delete users in given context
     */
    public abstract void delete( final Context ctx, final User[] users) throws StorageException;

    /**
     * Delete one user in given context
     */
    public abstract void delete( final Context ctx, final User user) throws StorageException;
    
    /**
     * Fetch all data from current user  and add it to "del_user"
     */
    public abstract void createRecoveryData ( final Context ctx,final int user_id, final Connection write_ox_con ) throws StorageException;
    
    /**
     *  Delete from "del_user" for given context and user
     */
    public abstract void deleteRecoveryData ( final Context ctx,final int user_id, final Connection con ) throws StorageException;
    
    
    /**
     * Delete from "del_user" for given context
     */
    public abstract void deleteAllRecoveryData (final Context ctx, final Connection con ) throws StorageException;
    
    
}
