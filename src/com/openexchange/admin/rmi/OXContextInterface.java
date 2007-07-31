
package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.StorageException;


/**
 * This interface defines the Open-Xchange API Version 2 for creating and manipulating OX Contexts.
 *
 * @author cutmasta
 *
 */
public interface OXContextInterface extends Remote {

    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME = "OXContext_V2";

    /**
     * Create a new context.
     * @param ctx Context object
     * @param admin_user User data of administrative user account for this context
     * @param auth Credentials for authenticating against server.
     * 
     * @return Context object.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occured.
     * @throws ContextExistsException 
     */
    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, ContextExistsException;

    /**
     * Delete a context.<br>
     * Note: Deleting a context will delete all data whitch the context include (all users, groups, appointments, ... )
     * 
     * @param auth Credentials for authenticating against server.
     * @param ctx Context object
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * 
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occured.
     * @throws DatabaseUpdateException 
     * @throws InvalidDataException 
     */
    public void delete(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, DatabaseUpdateException, InvalidDataException;

    /**
     * Move all data of a context contained on the filestore to another filestore
     *         <p>
     *         This method returns immediately and the data is going to be copied
     *         in the background. To query the progress and the result of the actual
     *         task, the AdminJobExecutor interface must be used.
     *         
     * @param ctx Context object
     * @param dst_filestore_id Id of the Filestore to move the context in.
     * @param auth Credentials for authenticating against server.
     * @return Job id which can be used for retrieving progress information.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occured.
     * @throws NoSuchFilestoreException 
     * @throws NoSuchReasonException 
     * @throws OXContextException 
     */
    //public String moveContextFilestore(Context ctx, Filestore dst_filestore_id, MaintenanceReason reason, Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException;
    //* @param reason ID of the maintenance reason for disabling the context while the move is in progress.
    public int moveContextFilestore(final Context ctx, final Filestore dst_filestore_id, final Credentials auth) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, NoSuchFilestoreException, NoSuchReasonException, OXContextException;

    /**
     * Move all data of a context contained in a database to another database
     * 
     * @param ctx Context object
     * @param dst_database_id ID of a registered Database to move all data of this context in.
     * @param auth Credentials for authenticating against server.
     * @return String containing return queue id to query status of job.
     *         <p>
     *         This method returns immediately and the data is going to be copied
     *         in the background. To query the progress and the result of the actual
     *         task, the AdminJobExecutor interface must be used.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * 
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * 
     * @throws StorageException When an error in the subsystems occured.
     * @throws DatabaseUpdateException 
     * @throws OXContextException 
     */
    //    public int moveContextDatabase(Context ctx, Database dst_database_id, MaintenanceReason reason, Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException, DatabaseUpdateException, OXContextException;
    //    * @param reason ID of the maintenance reason for disabling the context while the move is in progress.
    public int moveContextDatabase(final Context ctx, final Database dst_database_id, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException, DatabaseUpdateException, OXContextException;

    /**
     * Disable given context.<br>
     * 
     * @param ctx Context object.
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occured.
     * @throws NoSuchReasonException 
     * @throws OXContextException 
     */
    //public void disable(Context ctx, MaintenanceReason reason, Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException, NoSuchReasonException, OXContextException;
    //* @param reason MaintenanceReason
    public void disable(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException, NoSuchReasonException, OXContextException;

    /**
     * Enable given context.
     * 
     * @param auth Credentials for authenticating against server.
     * @param ctx Context object.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException 
     */
    public void enable(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;

    /**
     * Search for contexts<br>
     * Use this for search a context or list all contexts.
     * 
     * @param auth Credentials for authenticating against server.
     * @param search_pattern Search pattern e.g "*mycontext*".
     * @return Contexts.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occured.
     */
    public Context[] list(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException;

    /**
     * Convenience method for listing all contexts
     * Use this for search a context or list all contexts.
     * 
     * @param auth Credentials for authenticating against server.
     * @param search_pattern Search pattern e.g "*mycontext*".
     * @return Contexts.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occured.
     */
    public Context[] listAll(final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException;

    /**
     * Disable all contexts.<br>
     * 
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * 
     * @throws StorageException When an error in the subsystems occured.
     * @throws NoSuchReasonException 
     */
    //public void disableAll(MaintenanceReason reason, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, NoSuchReasonException;
    //* @param reason MaintenanceReason
    public void disableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException, NoSuchReasonException;

    /**
     * Enable all contexts.
     * 
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws RemoteException General RMI Exception
     * 
     * @throws StorageException When an error in the subsystems occured.
     */
    public void enableAll(final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException;

    /**
     * Get specified context details
     * 
     * @param ctx With context ID set.
     * @param auth Credentials for authenticating against server.
     * @return Data for the requested context.
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public Context getData(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;
    
    /**
     * Change specified context!
     * 
     * This method currently modifies following data:
     * 
     * Login mappings - You can then login via usernam@loginmapping instead of username@contextID          
     *      
     * Context name in configdb - This is for better organization of contexts in your whole system.
     *  
     * Change filestore quota size - Change how much quota the context is allowed to use!
     * 
     * Change storage data informations - Change filestore infos for context. Normally NO need to change!
     * 
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public void change(final Context ctx, final Credentials auth) throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException, InvalidDataException;
    
    
    /**
     * Search for context on specified db.
     * 
     * @param db_host_url Database on which to search for contexts.
     * @param auth Credentials for authenticating against server.
     * @return Found contexts on the specified database.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * 
     * @throws StorageException When an error in the subsystems occured.
     */
    public Context[] listByDatabase(final Database db_host_url, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException;

    /**
     * Search for context which store data on specified filestore
     * 
     * @param filestore_url Filestore
     * @param auth Credentials for authenticating against server.
     * @return Contexts found on this filestore.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * 
     * @throws StorageException When an error in the subsystems occured.
     */
    public Context[] listByFilestore(final Filestore filestore_url, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException,InvalidDataException;
}
