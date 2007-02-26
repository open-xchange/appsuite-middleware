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

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;

public abstract class OXToolStorageInterface {

    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class<? extends OXToolStorageInterface> implementingClass;

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
                    } catch (ClassNotFoundException e) {
                        throw new StorageException(e);
                    }
                } else {
                    throw new StorageException("Property for context_storage not defined");
                }
            }
        }
        Constructor<? extends OXToolStorageInterface> cons;
        try {
            cons = implementingClass.getConstructor(new Class[] {});
            return cons.newInstance(new Object[] {});
        } catch (SecurityException e) {
            throw new StorageException(e);
        } catch (NoSuchMethodException e) {
            throw new StorageException(e);
        } catch (IllegalArgumentException e) {
            throw new StorageException(e);
        } catch (InstantiationException e) {
            throw new StorageException(e);
        } catch (IllegalAccessException e) {
            throw new StorageException(e);
        } catch (InvocationTargetException e) {
            throw new StorageException(e);
        }
    }

    
    public abstract void checkPrimaryMail(final Context ctx, final String primary_mail) throws StorageException, InvalidDataException;

    public abstract boolean existsResourceGroup(final Context ctx, final String identifier, final int resource_group) throws StorageException;

    public abstract boolean existsResource(final Context ctx, final String identifier) throws StorageException;

    public abstract boolean existsResource(final Context ctx, final int resource_id) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final int gid) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final int[] gid) throws StorageException;

    public abstract boolean existsGroup(final Context ctx, final String identifier) throws StorageException;

    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final int[] user_ids) throws StorageException;

    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final int member_ID) throws StorageException;

    public abstract boolean existsUser(final Context ctx, final String username) throws StorageException;

    public abstract boolean existsUser(final Context ctx, final int uid) throws StorageException;

    public abstract boolean isMasterDatabase(final int database_id) throws StorageException;

    public abstract boolean existsReason(final int rid) throws StorageException;

    public abstract boolean existsReason(final String reason) throws StorageException;

    public abstract boolean existsUser(final Context ctx, final int[] user_ids) throws StorageException;

    public abstract boolean existsContext(final Context ctx) throws StorageException;

    public abstract boolean isContextAdmin(final Context ctx, final int user_id) throws StorageException;

    public abstract int getAdminForContext(final Context ctx, final Connection con) throws StorageException;

    public abstract int getDefaultGroupForContext(final Context ctx, final Connection con) throws StorageException;

    public abstract boolean existsServerID(final int check_ID, final String table, final String field) throws StorageException;

    public abstract boolean existsServer(final int server_id) throws StorageException;

    public abstract boolean existsServer(final String server_name) throws StorageException;

    public abstract boolean existsDatabase(final int db_id) throws StorageException;

    public abstract boolean existsStore(final int store_id) throws StorageException;

    public abstract boolean existsStore(final String url) throws StorageException;

    public abstract boolean existsDatabase(final String db_name) throws StorageException;

    public abstract boolean isContextEnabled(final Context ctx) throws StorageException;

    public abstract boolean storeInUse(final int store_id) throws StorageException;

    public abstract boolean poolInUse(final int pool_id) throws StorageException;

    public abstract boolean serverInUse(final int server_id) throws StorageException;
}
