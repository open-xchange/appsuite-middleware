
package com.openexchange.admin.storage.sqlStorage;

import com.openexchange.admin.rmi.dataobjects.Server;

import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;

/**
 * This class implements the global storage interface and creates a layer between the abstract
 * storage definition and a storage in a SQL accessible database
 * 
 * @author d7
 * @author cutmasta 
 *
 */
public abstract class OXUtilSQLStorage extends OXUtilStorageInterface {

	abstract public int registerFilestore(final Filestore fstore)  throws StorageException;
	
	abstract public void changeFilestore(final Filestore fstore) throws StorageException;
	
	abstract public Filestore[] listFilestores( final String search_pattern )  throws StorageException;
	
	abstract public void unregisterFilestore( final int store_id ) throws StorageException;
	
	abstract public int addMaintenanceReason( final MaintenanceReason reason )  throws StorageException;
	
	abstract public void deleteMaintenanceReason(final int[] reason_ids) throws StorageException;
	
	abstract public MaintenanceReason[] getMaintenanceReasons(final int[] reason_id) throws StorageException;
	
	abstract public MaintenanceReason[] getAllMaintenanceReasons() throws StorageException;
	
	abstract public int registerDatabase(final Database db) throws StorageException;
	
	abstract public void createDatabase( final Database db) throws StorageException;
	
	abstract public void deleteDatabase(final Database db) throws StorageException;
	
	abstract public int registerServer( final String serverName ) throws StorageException;
	
	abstract public void unregisterDatabase( final int db_id ) throws StorageException;
	
	abstract public void unregisterServer( final int server_id ) throws StorageException;
	
	abstract public Database[] searchForDatabase( final String search_pattern ) throws StorageException;
	
	abstract public Server[] searchForServer( final String search_pattern ) throws StorageException;

}
