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

package com.openexchange.database.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.internal.wrapping.ConnectionReturnerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.pooling.PoolingException;

/**
 * Fetches a connection without timeouts and optionally selects the wanted schema.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class NotimeoutFetchAndSchema implements FetchAndSchema {

    private final ReplicationMonitor monitor;
    private final boolean setSchema;

    /**
     * Initializes a new {@link NotimeoutFetchAndSchema}.
     *
     * @param monitor The replication monitor
     * @param setSchema <code>true</code> to set the schema name when getting the connection, <code>false</code>, otherwise
     */
    NotimeoutFetchAndSchema(ReplicationMonitor monitor, boolean setSchema) {
        super();
        this.monitor = monitor;
        this.setSchema = setSchema;
    }

    @Override
    public Connection get(Pools pools, AssignmentImpl assign, boolean write, boolean usedAsRead) throws PoolingException, OXException {
        final int poolId;
        if (write) {
            poolId = assign.getWritePoolId();
        } else {
            poolId = assign.getReadPoolId();
        }
        final ConnectionPool pool = pools.getPool(poolId);
        final Connection retval = pool.getWithoutTimeout();
        if (setSchema) {
            try {
                final String schema = assign.getSchema();
                if (null != schema && !retval.getCatalog().equals(schema)) {
                    retval.setCatalog(schema);
                }
            } catch (SQLException e) {
                pool.backWithoutTimeout(retval);
                throw DBPoolingExceptionCodes.SCHEMA_FAILED.create(e);
            }
        }
        return ConnectionReturnerFactory.createConnection(pools, monitor, assign, retval, true, write, usedAsRead);
    }
}
