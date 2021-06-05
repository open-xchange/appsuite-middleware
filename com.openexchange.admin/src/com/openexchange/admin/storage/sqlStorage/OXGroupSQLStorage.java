/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */
package com.openexchange.admin.storage.sqlStorage;

import java.sql.Connection;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXGroupStorageInterface;

/**
 * This class implements the global storage interface and creates a layer
 * between the abstract storage definition and a storage in a SQL accessible
 * database
 *
 * @author d7
 * @author cutmasta
 */
/**
 * @author d7
 *
 */
public abstract class OXGroupSQLStorage extends OXGroupStorageInterface {

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#addMember(com.openexchange.admin.rmi.dataobjects.Context,
     *      int, User[])
     */
    @Override
    abstract public void addMember(final Context ctx, final int grp_id, final User[] members) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#change(com.openexchange.admin.rmi.dataobjects.Context,
     *      com.openexchange.admin.rmi.dataobjects.Group)
     */
    @Override
    abstract public void change(final Context ctx, final Group grp) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#create(com.openexchange.admin.rmi.dataobjects.Context,
     *      com.openexchange.admin.rmi.dataobjects.Group)
     */
    @Override
    abstract public int create(final Context ctx, final Group grp) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#getMembers(com.openexchange.admin.rmi.dataobjects.Context,
     *      int)
     */
    @Override
    abstract public User[] getMembers(final Context ctx, final int grp_id) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#list(com.openexchange.admin.rmi.dataobjects.Context,
     *      java.lang.String)
     */
    @Override
    abstract public Group[] list(final Context ctx, final String pattern) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#removeMember(com.openexchange.admin.rmi.dataobjects.Context,
     *      int, User[])
     */
    @Override
    abstract public void removeMember(final Context ctx, final int grp_id, final User[] members) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#deleteRecoveryData(com.openexchange.admin.rmi.dataobjects.Context,
     *      int, java.sql.Connection)
     */
    @Override
    abstract public void deleteRecoveryData(final Context ctx, final int group_id, Connection con) throws StorageException;

    /**
     * @see com.openexchange.admin.storage.interfaces.OXGroupStorageInterface#deleteAllRecoveryData(com.openexchange.admin.rmi.dataobjects.Context,
     *      java.sql.Connection)
     */
    @Override
    abstract public void deleteAllRecoveryData(final Context ctx, Connection con) throws StorageException;

}
