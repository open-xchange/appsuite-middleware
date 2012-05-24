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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal;

import java.sql.Connection;
import com.openexchange.database.Assignment;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;


/**
 * {@link MockDBService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MockDBService implements DatabaseService {
    
    private final DBProvider dbProvider;
    
    public MockDBService(final DBProvider dbProvider) {
        super();
        this.dbProvider = dbProvider;
    }

    @Override
    public Connection getReadOnly() throws OXException {
        return dbProvider.getReadConnection(null);
    }

    @Override
    public Connection getWritable() throws OXException {
        return dbProvider.getWriteConnection(null);
    }

    @Override
    public void backReadOnly(final Connection con) {
        dbProvider.releaseReadConnection(null, con);
    }

    @Override
    public void backWritable(final Connection con) {
        dbProvider.releaseWriteConnection(null, con);
    }

    @Override
    public int[] listContexts(final int poolId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getServerId() throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Connection getReadOnly(final Context ctx) throws OXException {
        return dbProvider.getReadConnection(null);
    }

    @Override
    public Connection getReadOnly(final int contextId) throws OXException {
        return dbProvider.getReadConnection(null);
    }

    @Override
    public Connection getWritable(final Context ctx) throws OXException {
        return dbProvider.getWriteConnection(null);
    }

    @Override
    public Connection getWritable(final int contextId) throws OXException {
        return dbProvider.getWriteConnection(null);
    }

    @Override
    public Connection getForUpdateTask(final int contextId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Connection get(final int poolId, final String schema) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Connection getNoTimeout(final int poolId, final String schema) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void backNoTimeoout(final int poolId, final Connection con) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void backReadOnly(final Context ctx, final Connection con) {
        dbProvider.releaseReadConnection(null, con);
        
    }

    @Override
    public void backReadOnly(final int contextId, final Connection con) {
        dbProvider.releaseReadConnection(null, con);
        
    }

    @Override
    public void backWritable(final Context ctx, final Connection con) {
        dbProvider.releaseWriteConnection(null, con);        
    }

    @Override
    public void backWritable(final int contextId, final Connection con) {
        dbProvider.releaseWriteConnection(null, con);  
    }

    @Override
    public void backForUpdateTask(final int contextId, final Connection con) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void back(final int poolId, final Connection con) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getWritablePool(final int contextId) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getSchemaName(final int contextId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int[] getContextsInSameSchema(final int contextId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void invalidate(final int contextId) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeAssignment(Connection con, Assignment assignment) {
        // TODO Auto-generated method stub
        
    }
}
