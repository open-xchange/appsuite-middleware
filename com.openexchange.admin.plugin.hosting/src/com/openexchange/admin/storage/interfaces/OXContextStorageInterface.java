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

package com.openexchange.admin.storage.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.PropertyHandlerExtended;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.pipesnfilters.Filter;

/**
 * This interface provides an abstraction to the storage of the context
 * information
 *
 * @author d7
 *
 */
public abstract class OXContextStorageInterface {

    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class<? extends OXContextStorageInterface> implementingClass;

    private static final Log log = LogFactory.getLog(OXContextStorageInterface.class);

    protected static final AdminCacheExtended cache = ClientAdminThreadExtended.cache;

    protected static final PropertyHandlerExtended prop = cache.getProperties();

    /**
     * Creates a new instance implementing the group storage interface.
     * @return an instance implementing the group storage interface.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException Storage exception
     */
    public static OXContextStorageInterface getInstance() throws StorageException {
        synchronized (OXContextStorageInterface.class) {
            if (null == implementingClass) {
                final String className = prop.getProp(PropertyHandlerExtended.CONTEXT_STORAGE, null);
                if (null != className) {
                    try {
                        implementingClass = Class.forName(className).asSubclass(OXContextStorageInterface.class);
                    } catch (final ClassNotFoundException e) {
                        log.error(e.getMessage(), e);
                        throw new StorageException(e);
                    }
                } else {
                    final StorageException storageException = new StorageException("Property for context_storage not defined");
                    log.error(storageException.getMessage(), storageException);
                    throw storageException;
                }
            }
        }
        Constructor<? extends OXContextStorageInterface> cons;
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
     * Move data of context to target database
     *
     * @param ctx
     * @param target_database_id
     * @param reason
     * @throws StorageException
     */
    public abstract void moveDatabaseContext(final Context ctx, final Database target_database_id, final MaintenanceReason reason) throws StorageException;

    /**
     * @param ctx
     * @param dst_filestore_id
     * @param reason
     * @return
     * @throws StorageException
     */
    public abstract String moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final MaintenanceReason reason) throws StorageException;

    /**
     * @param ctx Context with Filestore data set!
     * @throws StorageException
     */
    public abstract void changeStorageData(final Context ctx) throws StorageException;

    /**
     * @param ctx
     * @return a context object
     * @throws StorageException
     */
    public abstract Context getData(final Context ctx) throws StorageException;

    /**
     * @param ctx
     * @return a context object
     * @throws StorageException
     */
    public abstract Context[] getData(final Context[] ctx) throws StorageException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void change(final Context ctx) throws StorageException;

    /**
     * Changes specified context's quota.
     *
     * @param ctx The context
     * @param module The module
     * @param quota The quota to set
     * @param auth The credentials
     * @throws StorageException
     */
    public abstract void changeQuota(Context ctx, String module, long quota, Credentials auth) throws StorageException;

    /**
     * Changes specified context's capabilities.
     *
     * @param ctx The context
     * @param capsToAdd The capabilities to add
     * @param capsToRemove The capabilities to remove
     * @param auth The credentials
     * @throws StorageException
     */
    public abstract void changeCapabilities(Context ctx, Set<String> capsToAdd, Set<String> capsToRemove, Credentials auth) throws StorageException;

    /**
     * @param ctx
     * @param admin_user
     * @param access
     * @throws StorageException
     */
    public abstract Context create(final Context ctx, final User admin_user, final UserModuleAccess access) throws StorageException, InvalidDataException;


    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void delete(final Context ctx) throws StorageException;

    /**
     * @param search_pattern
     * @param filters
     * @param loaders
     * @return
     * @throws StorageException
     */
    public abstract Context[] listContext(final String search_pattern, List<Filter<Integer, Integer>> filters, List<Filter<Context, Context>> loaders) throws StorageException;

    /**
     * @param ctx
     * @param reason
     * @throws StorageException
     */
    public abstract void disable(final Context ctx, final MaintenanceReason reason) throws StorageException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void enable(final Context ctx) throws StorageException;

    /**
     * @param reason
     * @throws StorageException
     */
    public abstract void disableAll(final MaintenanceReason reason) throws StorageException;

    /**
     * @throws StorageException
     */
    public abstract void enableAll() throws StorageException;

    /**
     * @param db_host
     * @return
     * @throws StorageException
     */
    public abstract Context[] searchContextByDatabase(final Database db_host) throws StorageException;

    /**
     * @param filestore
     * @return
     * @throws StorageException
     */
    public abstract Context[] searchContextByFilestore(final Filestore filestore) throws StorageException;

    /**
     * This method deletes all inaccessible data in a context.
     * @param ctx Context.
     * @throws StorageException if some problem occurs.
     */
    public abstract void downgrade(final Context ctx) throws StorageException;

    /**
     * @param reason
     * @param additionaltable
     * @param sqlconjunction
     * @throws StorageException
     */
    public abstract void disableAll(final MaintenanceReason reason, final String additionaltable, String sqlconjunction) throws StorageException;

    /**
     * @param additionaltable
     * @param sqlconjunction
     * @throws StorageException
     */
    public abstract void enableAll(final String additionaltable, final String sqlconjunction) throws StorageException;


}
