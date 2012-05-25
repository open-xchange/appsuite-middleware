/**
 * 
 */
package com.openexchange.admin.contextrestore.storage.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;

import com.openexchange.admin.contextrestore.dataobjects.VersionInformation;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore.Parser.PoolIdSchemaAndVersionInfo;
import com.openexchange.admin.contextrestore.tools.PropertyHandlerExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This factory class provides access to the right storage layer
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public abstract class OXContextRestoreStorageInterface {
    
    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class<? extends OXContextRestoreStorageInterface> implementingClass;

    private static final Log log = LogFactory.getLog(OXContextRestoreStorageInterface.class);

    protected static PropertyHandlerExtended prop = new PropertyHandlerExtended(System.getProperties());
    
    /**
     * Creates a new instance implementing the group storage interface.
     * @return an instance implementing the group storage interface.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException Storage exception
     */
    public static OXContextRestoreStorageInterface getInstance() throws StorageException {
        synchronized (OXContextRestoreStorageInterface.class) {
            if (null == implementingClass) {
                final String className = prop.getProp(PropertyHandlerExtended.CONTEXT_RESTORE_STORAGE, null);
                if (null != className) {
                    try {
                        implementingClass = Class.forName(className).asSubclass(OXContextRestoreStorageInterface.class);
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
        Constructor<? extends OXContextRestoreStorageInterface> cons;
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

    
    public abstract String restorectx(final Context ctx, final PoolIdSchemaAndVersionInfo poolidandschema) throws SQLException, FileNotFoundException, IOException, OXContextRestoreException, StorageException;
    
    public abstract void checkVersion(final PoolIdSchemaAndVersionInfo poolIdAndSchema) throws SQLException, OXContextRestoreException, StorageException;
}
