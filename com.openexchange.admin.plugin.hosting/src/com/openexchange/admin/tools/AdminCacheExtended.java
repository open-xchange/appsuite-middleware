package com.openexchange.admin.tools;

import java.sql.Connection;

import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolDBPoolExtension;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterfaceExtension;

public class AdminCacheExtended extends AdminCache {
    
    private PropertyHandlerExtended prop = null;
    private OXAdminPoolInterfaceExtension pool = null;
    
    // sql filenames order and directory

    public void initCacheExtended() {
        prop = new PropertyHandlerExtended(System.getProperties());
        pool = new OXAdminPoolDBPoolExtension(prop);
    }
    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public int getDBPoolIdForContextId(int context_id) throws PoolException {
        return pool.getDBPoolIdForContextId(context_id);
    }
    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public Connection getWRITEConnectionForPoolId(int db_pool_id,String db_schema) throws PoolException{
        return pool.getWRITEConnectionForPoolId(db_pool_id,db_schema);
    }
    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public void pushWRITEConnectionForPoolId(int db_pool_id,Connection conny) throws PoolException {
        pool.pushWRITEConnectionForPoolId(db_pool_id,conny);
    }
    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public void resetPoolMappingForContext(int context_id) throws PoolException {
        pool.resetPoolMappingForContext(context_id);
    }
    
    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public String getSchemeForContextId(int context_id) throws PoolException{
        return pool.getSchemeForContextId(context_id);
    }
    
    public PropertyHandlerExtended getProperties() {
        if (prop == null) {
            initCacheExtended();
        }
        return prop;
    }
}
