
package com.openexchange.admin.storage.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.PropertyHandlerExtended;

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
    
    protected static AdminCacheExtended cache = null;

    protected static PropertyHandlerExtended prop = null;

    static {
        cache = ClientAdminThreadExtended.cache;
        prop = cache.getProperties();
    }

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
     * @throws StorageException
     */
    public abstract void change(final Context ctx) throws StorageException;
    
    /**
     * @param ctx
     * @param admin_user
     * @throws StorageException
     */
    public abstract Context create(final Context ctx, final User admin_user) throws StorageException, InvalidDataException;

    /**
     * @param ctx
     * @throws StorageException
     */
    public abstract void delete(final Context ctx) throws StorageException;

    /**
     * @param search_pattern
     * @return
     * @throws StorageException
     */
    public abstract Context[] searchContext(final String search_pattern) throws StorageException;

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

    
}
