
package com.openexchange.admin.storage.sqlStorage;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;

/**
 * This class implements the global storage interface and creates a layer
 * between the abstract storage definition and a storage in a SQL accessible
 * database
 * 
 * @author d7
 * 
 */
public abstract class OXContextSQLStorage extends OXContextStorageInterface {

    abstract public void delete(final Context ctx) throws StorageException;

    abstract public void disableAll(final MaintenanceReason reason) throws StorageException;

    abstract public void disable(final Context ctx, final MaintenanceReason reason) throws StorageException;

    abstract public void enableAll() throws StorageException;

    abstract public void enable(final Context ctx) throws StorageException;

    abstract public Context create( final Context ctx, final User admin_user, final long quota_max) throws StorageException,InvalidDataException;
    
    abstract public Context getData(final Context ctx) throws StorageException;

    abstract public void moveDatabaseContext(final Context ctx, final Database target_database_id, final MaintenanceReason reason) throws StorageException;

    abstract public String moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final MaintenanceReason reason) throws StorageException;;

    abstract public Context[] searchContext(final String search_pattern) throws StorageException;

    abstract public Context[] searchContextByDatabase(final Database db_host) throws StorageException;

    abstract public Context[] searchContextByFilestore(final Filestore filestore) throws StorageException;

}
