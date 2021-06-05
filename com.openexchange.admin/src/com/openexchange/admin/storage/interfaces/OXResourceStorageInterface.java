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
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;

/**
 * This interface provides an abstraction to the storage of the resource
 * information
 *
 * @author d7
 * @author cutmasta
 */
public abstract class OXResourceStorageInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXResourceStorageInterface.class);


    private static volatile OXResourceStorageInterface instance;

    /**
     * Creates a new instance implementing the group storage interface.
     * @return an instance implementing the group storage interface.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException Storage exception
     */
    public static OXResourceStorageInterface getInstance() throws StorageException {
        OXResourceStorageInterface inst = instance;
        if (null == inst) {
            synchronized (OXResourceStorageInterface.class) {
                inst = instance;
                if (null == inst) {
                    Class<? extends OXResourceStorageInterface> implementingClass;
                    AdminCache cache = ClientAdminThread.cache;
                    PropertyHandler prop = cache.getProperties();
                    final String className = prop.getProp(PropertyHandler.RESOURCE_STORAGE, null);
                    if (null != className) {
                        try {
                            implementingClass = Class.forName(className).asSubclass(OXResourceStorageInterface.class);
                        } catch (ClassNotFoundException e) {
                            log.error("", e);
                            throw new StorageException(e);
                        }
                    } else {
                        final StorageException storageException = new StorageException("Property for resource_storage not defined");
                        log.error("", storageException);
                        throw storageException;
                    }

                    Constructor<? extends OXResourceStorageInterface> cons;
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

    public abstract int create(final Context ctx, final Resource res) throws StorageException;

    /**
     * Change resource in datastore
     */
    public abstract void change(final Context ctx, final Resource res) throws StorageException;

    public abstract void changeLastModified(final int resource_id, final Context ctx, final Connection write_ox_con) throws StorageException;

    public abstract void delete(final Context ctx, final Resource resource) throws StorageException;

    public abstract Resource getData(final Context ctx, final Resource resource) throws StorageException;

    /**
     * fetch data from resource and insert in del_resource
     */
    public abstract void createRecoveryData(final int resource_id, final Context ctx, final Connection con) throws StorageException;

    /**
     * delete data from del_resource
     */
    public abstract void deleteRecoveryData(final int resource_id, final Context ctx, final Connection con) throws StorageException;

    /**
     * Delete all data from del_resource for context
     */
    public abstract void deleteAllRecoveryData(final Context ctx, final Connection con) throws StorageException;

    public abstract Resource[] list(final Context ctx, final String pattern) throws StorageException;
}
