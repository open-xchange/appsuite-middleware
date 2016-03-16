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

package com.openexchange.database.internal.wrapping;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import com.openexchange.database.internal.AssignmentImpl;
import com.openexchange.database.internal.Pools;
import com.openexchange.database.internal.ReplicationMonitor;

/**
 * {@link JDBC41ConnectionReturner}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class JDBC41ConnectionReturner extends JDBC4ConnectionReturner {

    public JDBC41ConnectionReturner(Pools pools, ReplicationMonitor monitor, AssignmentImpl assign, Connection delegate, boolean noTimeout, boolean write, boolean usedAsRead) {
        super(pools, monitor, assign, delegate, noTimeout, write, usedAsRead);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        checkForAlreadyClosed();
        delegate.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        checkForAlreadyClosed();
        delegate.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        checkForAlreadyClosed();
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        checkForAlreadyClosed();
        return delegate.getNetworkTimeout();
    }
}
