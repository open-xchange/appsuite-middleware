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

package com.openexchange.admin.storage.sqlStorage;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import java.sql.Connection;

import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;

/**
 * @author d7
 * @author cutmasta
 */
public abstract class OXToolSQLStorage extends OXToolStorageInterface {

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#primaryMailExists(int,
     *      java.lang.String)
     */
    public abstract void primaryMailExists(final Context ctx, final String primary_mail) throws StorageException, InvalidDataException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsContext(int)
     */
    public abstract boolean existsContext(final Context ctx) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsDatabase(int)
     */
    public abstract boolean existsDatabase(final int db_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsDatabase(java.lang.String)
     */
    public abstract boolean existsDatabase(final String db_name) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroup(int,
     *      int)
     */
    public abstract boolean existsGroup(final Context ctx, final int gid) throws StorageException;

    public abstract boolean existsContextLoginMappings(Context ctx, Connection configdb_connection) throws StorageException;

    public abstract boolean existsContextLoginMappings(Context ctx) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroup(int,
     *      int[])
     */
    public abstract boolean existsGroup(final Context ctx, final int[] gid) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroup(int,
     *      java.lang.String)
     */
    public abstract boolean existsGroup(final Context ctx, final String identifier) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroupMember(int,
     *      int, int[])
     */
    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final int[] user_ids) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroupMember(int,
     *      int, int)
     */
    public abstract boolean existsGroupMember(final Context ctx, final int group_ID, final int member_ID) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsReason(int)
     */
    public abstract boolean existsReason(final int rid) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsReason(java.lang.String)
     */
    public abstract boolean existsReason(final String reason) throws StorageException;

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsResourceAddress(com.openexchange.admin.rmi.dataobjects.Context,
     *      java.lang.String)
     */
    public abstract boolean existsResourceAddress(final Context ctx, final String address) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsResource(int,
     *      java.lang.String, int)
     */
    public abstract boolean existsResource(final Context ctx, final String identifier) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsResource(int,
     *      int)
     */
    public abstract boolean existsResource(final Context ctx, final int resource_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsServer(int)
     */
    public abstract boolean existsServer(final int server_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsServer(java.lang.String)
     */
    public abstract boolean existsServer(final String server_name) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsServerID(int,
     *      java.lang.String, java.lang.String)
     */
    public abstract boolean existsServerID(final int check_ID, final String table, final String field) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsStore(int)
     */
    public abstract boolean existsStore(final int store_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsStore(java.lang.String)
     */
    public abstract boolean existsStore(final String url) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      java.lang.String)
     */
    public abstract boolean existsUser(final Context ctx, final String username) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      int)
     */
    public abstract boolean existsUser(final Context ctx, final int uid) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      int[])
     */
    public abstract boolean existsUser(final Context ctx, final int[] user_ids) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#getAdminForContext(int,
     *      java.sql.Connection)
     */
    public abstract int getAdminForContext(final Context ctx, final Connection con) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#getDefaultGroupForContext(int,
     *      java.sql.Connection)
     */
    public abstract int getDefaultGroupForContext(final Context ctx, final Connection con) throws StorageException;

    public abstract int getDefaultGroupForContextWithOutConnection(final Context ctx) throws StorageException;
    
    public abstract int getGidNumberOfGroup(final Context ctx, final int group_id, final Connection con) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isContextAdmin(int,
     *      int)
     */
    public abstract boolean isContextAdmin(final Context ctx, final int user_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isContextEnabled(int)
     */
    public abstract boolean isContextEnabled(final Context ctx) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isMasterDatabase(int)
     */
    public abstract boolean isMasterDatabase(final int database_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#poolInUse(long)
     */
    public abstract boolean poolInUse(final int pool_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#serverInUse(long)
     */
    public abstract boolean serverInUse(final int server_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#storeInUse(long)
     */
    public abstract boolean storeInUse(final int store_id) throws StorageException;

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#schemaBeingLockedOrNeedsUpdate(com.openexchange.admin.rmi.dataobjects.Context)
     */
    public abstract boolean schemaBeingLockedOrNeedsUpdate(Context ctx) throws StorageException;

}
