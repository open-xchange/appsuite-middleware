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
package com.openexchange.admin.storage.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;


/**
 * This interface provides an abstraction to the storage of the group
 * information
 *
 * @author d7
 * @author cutmasta
 *
 */
public abstract class OXGroupStorageInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXGroupStorageInterface.class);

    private static volatile OXGroupStorageInterface instance;

    /**
     * Creates a new instance implementing the group storage interface.
     * @return an instance implementing the group storage interface.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException Storage exception
     */
    public static OXGroupStorageInterface getInstance() throws StorageException {
        OXGroupStorageInterface inst = instance;
        if (null == inst) {
            synchronized (OXGroupStorageInterface.class) {
                inst = instance;
                if (null == inst) {
                    Class<? extends OXGroupStorageInterface> implementingClass;
                    AdminCache cache = ClientAdminThread.cache;
                    PropertyHandler prop = cache.getProperties();
                    final String className = prop.getProp(PropertyHandler.GROUP_STORAGE, null);
                    if (null != className) {
                        try {
                            implementingClass = Class.forName(className).asSubclass(OXGroupStorageInterface.class);
                        } catch (ClassNotFoundException e) {
                            log.error("", e);
                            throw new StorageException(e);
                        }
                    } else {
                        final StorageException storageException = new StorageException("Property for group_storage not defined");
                        log.error("", storageException);
                        throw storageException;
                    }

                    Constructor<? extends OXGroupStorageInterface> cons;
                    try {
                        cons = implementingClass.getConstructor(new Class[] {});
                        inst = cons.newInstance(new Object[] {});
                        instance = inst;
                    } catch (SecurityException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (NoSuchMethodException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (IllegalArgumentException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (InstantiationException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (IllegalAccessException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (InvocationTargetException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    }
                }
            }
        }
        return inst;
    }

    /**
     * Create new group in given context
     *
     * @return int with the id of the created group
     */
    public abstract int create(final Context ctx, final Group grp) throws StorageException;

    /**
     * List all groups mathcing pattern in context ctx
     *
     */
    public abstract Group[] list(final Context ctx, final String pattern) throws StorageException;

    /**
     * Get group by context and id
     */
    public abstract Group get(final Context ctx, final Group grp) throws StorageException;

    /**
     * Get groups for a specified user!
     *
     */
    public abstract Group[] getGroupsForUser(final Context ctx, final User usr) throws StorageException;

    /**
     * Edit group data
     */
    public abstract void change(final Context ctx, final Group grp) throws StorageException;

    /**
     * Adds a new member to the group.
     */
    public abstract void addMember(final Context ctx, final int grp_id, final User[] members) throws StorageException;

    /**
     * Removes member from group
     */
    public abstract void removeMember(final Context ctx, final int grp_id, final User[] members) throws StorageException;

    /**
     * Delete group from context
     *
     * @param ctx
     * @param grps
     * @throws StorageException
     */
    public abstract void delete(final Context ctx, final Group[] grps) throws StorageException;

    /**
     * Get all members of group grp_id in context ctx
     */
    public abstract User[] getMembers(final Context ctx, final int grp_id) throws StorageException;

    /**
     * Removes entry in del_groups for group group_id and context ctx
     */
    public abstract void deleteRecoveryData(final Context ctx, final int group_id, Connection con) throws StorageException;

    /**
     * Deletes all recoevery data for context ctx
     */
    public abstract void deleteAllRecoveryData(final Context ctx, Connection con) throws StorageException;
}
