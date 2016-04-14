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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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


import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Resource;
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
                        } catch (final ClassNotFoundException e) {
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
                    } catch (final SecurityException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final NoSuchMethodException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalArgumentException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InstantiationException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalAccessException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InvocationTargetException e) {
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
