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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.admin.tools;

import java.sql.Connection;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolDBPoolExtension;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterfaceExtension;

public class AdminCacheExtended extends AdminCache {

    private PropertyHandlerExtended prop = null;

    private OXAdminPoolInterfaceExtension pool = null;

    public AdminCacheExtended() {
        super();
    }

    // sql filenames order and directory

    public void initCacheExtended() {
        prop = new PropertyHandlerExtended(System.getProperties());
        pool = new OXAdminPoolDBPoolExtension();
        initPool(pool);
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

    @Override
    public PropertyHandlerExtended getProperties() {
        return prop;
    }
}
