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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.storage.sqlStorage;

import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.tools.PropertyHandler;
import java.sql.Connection;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OXAdminPoolDBPool implements OXAdminPoolInterface {
    
    private Log log = LogFactory.getLog(this.getClass());
    
    public OXAdminPoolDBPool(PropertyHandler prop) {
        super();
    }
    
    public Connection getConnectionForConfigDB() throws PoolException {
        Connection con = null;
        try{
            con =  DBPool.pickupWriteable();
        }catch(DBPoolingException ep){
            if (log.isErrorEnabled()) {
            log.error("Error pickup configdb database write connection from ox pool!",ep);
            }
            throw new PoolException(""+ep.getMessage());
        }
        return con;
    }
    
    public Connection getConnectionForContext(int context_id) throws PoolException {
        Context ctx = new ContextImpl(context_id);
        Connection con = null;
        try{
            con =  DBPool.pickupWriteable(ctx);
        }catch(DBPoolingException ep){
            if (log.isErrorEnabled()) {
            log.error("Error pickup oxdb database write connection from ox pool!",ep);
            }
            throw new PoolException(""+ep.getMessage());
        }
        return con;
    }
    
    public boolean pushConnectionForConfigDB(Connection con) throws PoolException {
        boolean ret = false;
        
        try {
            if(con != null && !con.getAutoCommit() && !con.isClosed() ){
                con.setAutoCommit(true);
            }
            ret = DBPool.pushWrite(con);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
            log.error("Error pushing configdb write connection to pool!",e);
            }
            throw new PoolException(""+e.getMessage());
        }
        return ret;
    }
    
    public boolean pushConnectionForContext(int context_id, Connection con) throws PoolException {
        boolean ret = false;
        Context ctx = new ContextImpl(context_id);
        
        try {
            if(con != null && !con.getAutoCommit() && !con.isClosed() ){
                con.setAutoCommit(true);
            }
            ret = DBPool.pushWrite(ctx,con);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
            log.error("Error pushing ox db write connection to pool!",e);
            }
            throw new PoolException(""+e.getMessage());
        }
        return ret;
    }
}
