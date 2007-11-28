
package com.openexchange.admin.storage.sqlStorage;

import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterfaceExtension;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.database.Database;
import java.sql.Connection;

import com.openexchange.server.impl.DBPoolingException;

public class OXAdminPoolDBPoolExtension extends OXAdminPoolDBPool implements OXAdminPoolInterfaceExtension {
    
    public OXAdminPoolDBPoolExtension(PropertyHandler prop) {
        super(prop);
    }
    
    public int getDBPoolIdForContextId(int context_id) throws PoolException {
        try{
            return Database.resolvePool(context_id,true);
        }catch(DBPoolingException db){
            throw new PoolException(""+db.getMessage());
        }
    }

    public Connection getWRITEConnectionForPoolId(int db_pool_id,String schema_name) throws PoolException {
        try{
            return Database.get(db_pool_id,schema_name);
        }catch(DBPoolingException db){
            throw new PoolException(""+db.getMessage());
        }
    }

    public void pushWRITEConnectionForPoolId(int db_pool_id,Connection conny) throws PoolException {        
        Database.back(db_pool_id,conny);
    }

    public void resetPoolMappingForContext(int context_id) throws PoolException {
        try{
            Database.reset(context_id);
        }catch(DBPoolingException db){
            throw new PoolException(""+db.getMessage());
        }
    }
    
   public String getSchemeForContextId(int context_id) throws PoolException{
       try{
            return Database.getSchema(context_id);
        }catch(DBPoolingException db){
            throw new PoolException(""+db.getMessage());
        }
   }
    
}
