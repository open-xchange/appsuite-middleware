
/**
 *
 */
package com.openexchange.admin.storage.sqlStorage;

import com.openexchange.admin.rmi.exceptions.PoolException;

import java.sql.Connection;

/**
 *
 */
public interface OXAdminPoolInterfaceExtension extends OXAdminPoolInterface {
    
    // SPECIAL METHODS FOR CONTEXT MOVING
    public int getDBPoolIdForContextId(int context_id) throws PoolException;
    public Connection getWRITEConnectionForPoolId(int db_pool_id,String db_schema) throws PoolException;
    public void pushWRITEConnectionForPoolId(int db_pool_id,Connection con) throws PoolException;
    public void resetPoolMappingForContext(int context_id) throws PoolException;
    public String getSchemeForContextId(int context_id) throws PoolException;
            
}
