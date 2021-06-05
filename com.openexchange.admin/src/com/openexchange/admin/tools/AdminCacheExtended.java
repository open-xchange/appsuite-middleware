/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.tools;

import java.sql.Connection;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolDBPoolExtension;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterfaceExtension;
import com.openexchange.database.SchemaInfo;

public class AdminCacheExtended extends AdminCache {

    private PropertyHandlerExtended prop = null;

    private OXAdminPoolInterfaceExtension pool = null;

    public AdminCacheExtended() {
        super();
    }

    // sql filenames order and directory

    public void initCacheExtended() {
        this.prop = new PropertyHandlerExtended(System.getProperties());
        this.pool = new OXAdminPoolDBPoolExtension();
        
        // Overwrite pool in parent class
        initPool(this.pool);
    }

    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public int getDBPoolIdForContextId(final int context_id) throws PoolException {
        return pool.getDBPoolIdForContextId(context_id);
    }

    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public Connection getWRITEConnectionForPoolId(final int db_pool_id,final String db_schema) throws PoolException{
        return pool.getWRITEConnectionForPoolId(db_pool_id,db_schema);
    }

    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public void pushWRITEConnectionForPoolId(final int db_pool_id,final Connection conny) throws PoolException {
        pool.pushWRITEConnectionForPoolId(db_pool_id,conny);
    }

    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public Connection getWRITENoTimeoutConnectionForPoolId(final int db_pool_id,final String db_schema) throws PoolException{
        return pool.getWRITENoTimeoutConnectionForPoolId(db_pool_id,db_schema);
    }

    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public void pushWRITENoTimeoutConnectionForPoolId(final int db_pool_id,final Connection conny) throws PoolException {
        pool.pushWRITENoTimeoutConnectionForPoolId(db_pool_id,conny);
    }

    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public String getSchemeForContextId(final int context_id) throws PoolException{
        return pool.getSchemeForContextId(context_id);
    }

    /**
     * ONLY USE IF YOU EXACTLY KNOW FOR WHAT THIS METHOD IS!!!
     */
    public SchemaInfo getSchemaInfoForContextId(final int context_id) throws PoolException{
        return pool.getSchemaInfoForContextId(context_id);
    }

    @Override
    public PropertyHandlerExtended getProperties() {
        return prop;
    }
}
