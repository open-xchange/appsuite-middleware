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

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;

public abstract class OXToolStorageInterface {

    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class<? extends OXToolStorageInterface> implementingClass;

    private static final Log log = LogFactory.getLog(OXToolStorageInterface.class);
    
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
    public static OXToolStorageInterface getInstance() throws StorageException {
        synchronized (OXToolStorageInterface.class) {
            if (null == implementingClass) {
                final String className = prop.getProp(PropertyHandler.TOOL_STORAGE, null);
                if (null != className) {
                    try {
                        implementingClass = Class.forName(className).asSubclass(OXToolStorageInterface.class);
                    } catch (final ClassNotFoundException e) {
                        log.error(e.getMessage(), e);
                        throw new StorageException(e);
                    }
                } else {
                    final StorageException storageException = new StorageException("Property for tool_storage not defined");
                    log.error(storageException.getMessage(), storageException);
                    throw storageException;
                }
            }
        }
        Constructor<? extends OXToolStorageInterface> cons;
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
     * Checks if given domain is used by any user,group or resource as mailaddress in given context.
     * @param domain
     * @return
     * @throws StorageException
     */
    public abstract boolean domainInUse(final Context ctx,final String domain) throws StorageException;

    /**
     * Checks if given domain is used as mail address of any group in given context.
     * @param domain
     * @return Groups which use this domain.null if no group uses this domain.
     * @throws StorageException
     */
    public abstract Group[] domainInUseByGroup(final Context ctx,final String domain) throws StorageException;
    
    /**
     * Checks if given domain is used as mail address of any resource in given context.
     * @param domain
     * @return Resources which use this domain. null if no resource uses this domain.
     * @throws StorageException
     */
    public abstract Resource[] domainInUseByResource(final Context ctx,final String domain) throws StorageException;

    /**
     * Checks if given domain is used as alias or primary mail address of any user in given context.
     * @param domain
     * @return Users which use this domain. null if no user uses this domain.
     * @throws StorageException
     */
    public abstract User[] domainInUseByUser(final Context ctx,final String domain) throws StorageException;

    /**
     * Although this method get's a context Object it will only look after the cid
     * 
     * @param ctx
     * @return
     * @throws StorageException
     */
    public abstract boolean existsContext(final Context ctx) throws StorageException;

    public abstract boolean existsContextLoginMappings(final Context ctx) throws StorageException;

    public abstract boolean existsContextLoginMappings(final Context ctx, final Connection configdb_connection) throws StorageException;

    public abstract boolean existsDatabase(final int db_id) throws StorageException;    

    public abstract boolean existsGroup(final Context ctx, final Group[] grps) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final Group grp) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final int gid) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final int[] gid) throws StorageException;    

    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final int member_ID) throws StorageException;

    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final int[] user_ids) throws StorageException;

    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final User[] users) throws StorageException;

    public abstract boolean existsReason(final int rid) throws StorageException;

    public abstract boolean existsReason(final String reason) throws StorageException;

    public abstract boolean existsResource(final Context ctx, final int resource_id) throws StorageException;
    
    public abstract boolean existsResourceAddress(final Context ctx, final String address) throws StorageException;

    public abstract boolean existsResourceAddress(Context ctx, String address,Integer resource_id) throws StorageException;

    public abstract boolean existsServer(final int server_id) throws StorageException;   
    
    public abstract boolean existsServerID(final int check_ID, final String table, final String field) throws StorageException;

    public abstract boolean existsStore(final int store_id) throws StorageException;

    public abstract boolean existsStore(final String url) throws StorageException;

    public abstract boolean existsUser(final Context ctx, final int uid) throws StorageException;

    public abstract boolean existsUser(final Context ctx, final int[] user_ids) throws StorageException;    

    public abstract boolean existsUser(final Context ctx, final User[] users) throws StorageException;

    public abstract boolean existsUser(final Context ctx, final User user) throws StorageException;    
    
    
    /**
     * Checks via group id and group name if it already exists in this context. Should be used in change method!
     * @param ctx
     * @param grp
     * @return
     * @throws StorageException
     */
    public abstract boolean existsGroupName(final Context ctx, final Group grp) throws StorageException;
    
    
    /**
     * Checks if given name is already used for a group in given context.Should be used in create method!
     * @param ctx
     * @param groupName
     * @return
     * @throws StorageException
     */
    public abstract boolean existsGroupName(final Context ctx, final String groupName) throws StorageException;
    
    /**
     * Checks via user id and user name if it already exists in this context. Should be used in change method!
     * @param ctx
     * @param usr
     * @return
     * @throws StorageException
     */
    public abstract boolean existsUserName(final Context ctx, final User usr) throws StorageException;
    
    /**
     * Checks if given name is already used for an user in given context.Should be used in create method!
     * @param ctx
     * @param userName
     * @return
     * @throws StorageException
     */
    public abstract boolean existsUserName(final Context ctx, final String userName) throws StorageException;
    
    /**
     * Checks via server id and server name if it already exists. Should be used in change method!
     * @param srv
     * @return
     * @throws StorageException
     */
    public abstract boolean existsServerName(final Server srv) throws StorageException;
    
    /**
     * Checks if given name is already used!Should be used in create method!
     * @param serverName
     * @return
     * @throws StorageException
     */
    public abstract boolean existsServerName(final String serverName) throws StorageException;
    
    /**
     * Checks via database id and database name if it already exists. Should be used in change method!
     * @param db
     * @return
     * @throws StorageException
     */
    public abstract boolean existsDatabaseName(final Database db) throws StorageException;
    
    /**
     *  Checks if given name is already used!Should be used in create method!
     * @param databaseName
     * @return
     * @throws StorageException
     */
    public abstract boolean existsDatabaseName(final String databaseName) throws StorageException;
    
    /**
     * Checks via resource id and resource name if it already exists. Should be used in change method!
     * @param ctx
     * @param res
     * @return
     * @throws StorageException
     */
    public abstract boolean existsResourceName(final Context ctx, final Resource res) throws StorageException;
    
    /**
     *  Checks if given name is already used for resource in given context!Should be used in create method!
     * @param ctx
     * @param resourceName
     * @return
     * @throws StorageException
     */
    public abstract boolean existsResourceName(final Context ctx, final String resourceName) throws StorageException;
            
    /**
     * Checks via context id and context name if it already exists. Should be used in change method!
     * @param ctx
     * @return
     * @throws StorageException
     */
    public abstract boolean existsContextName(final Context ctx) throws StorageException;
    
    
    /**
     * Checks if given context name already exists!Should be used in create method!
     * @param contextName
     * @return
     * @throws StorageException
     */
    public abstract boolean existsContextName(final String contextName) throws StorageException;

    public abstract int getAdminForContext(final Context ctx, final Connection con) throws StorageException;
    
    public abstract int getContextIDByContextname(final String ctxname) throws StorageException;

    public abstract int getDatabaseIDByDatabasename(final String dbname) throws StorageException;
    
    public abstract int getDefaultGroupForContext(final Context ctx, final Connection con) throws StorageException;
    
    public abstract int getDefaultGroupForContextWithOutConnection(final Context ctx) throws StorageException;

    public abstract int getGidNumberOfGroup(final Context ctx,final int group_id, final Connection con) throws StorageException;

    public abstract int getGroupIDByGroupname(final Context ctx,final String groupname) throws StorageException;

    public abstract String getGroupnameByGroupID(final Context ctx,final int group_id) throws StorageException;

    public abstract int getResourceIDByResourcename(final Context ctx,final String resourcename) throws StorageException;
    
    public abstract String getResourcenameByResourceID(final Context ctx,final int resource_id) throws StorageException;
    
    public abstract int getUserIDByUsername(final Context ctx,final String username) throws StorageException;
    
    public abstract String getUsernameByUserID(final Context ctx,final int user_id) throws StorageException;
    
    public abstract boolean isContextAdmin(final Context ctx, final int user_id) throws StorageException;
    
    public abstract boolean isContextEnabled(final Context ctx) throws StorageException;
    
    public abstract boolean existsDisplayName(final Context ctx, final User usr) throws StorageException;
    
    public abstract boolean isMasterDatabase(final int database_id) throws StorageException;
    
    public abstract boolean isUserSettingMailBitSet(final Context ctx, final User user, final int bit, final Connection con) throws StorageException;
    
    public abstract boolean poolInUse(final int pool_id) throws StorageException;
    
    public abstract void primaryMailExists(final Context ctx, final String primary_mail) throws StorageException, InvalidDataException;
    
    public abstract boolean schemaBeingLockedOrNeedsUpdate(final Context ctx) throws StorageException;
    
    public abstract boolean schemaBeingLockedOrNeedsUpdate(final int writePoolId, final String schema) throws StorageException;

    public abstract boolean serverInUse(final int server_id) throws StorageException;
    
    public abstract void setUserSettingMailBit(final Context ctx, final User user, final int bit, final Connection con) throws StorageException;

    public abstract boolean storeInUse(final int store_id) throws StorageException;
    
    public abstract void unsetUserSettingMailBit(final Context ctx, final User user, final int bit, final Connection con) throws StorageException;
}
