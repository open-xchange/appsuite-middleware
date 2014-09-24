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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.storage.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.storage.StorageParameters;


/**
 * {@link ConnectionProvider}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ConnectionProvider {

    static enum ConnectionMode {
        READ, WRITE;
    }

    private final Connection connection;
    private final ConnectionMode mode;
    private final boolean external;
    private final DatabaseService dbService;
    private final int contextId;

    /**
     * Initializes a new {@link ConnectionProvider}.
     *
     * @param dbService The database service
     * @param parameters the storage parameters
     * @param mode The required connection mode
     * @param contextId The context ID
     * @throws OXException
     */
    public ConnectionProvider(DatabaseService dbService, StorageParameters parameters, ConnectionMode mode, int contextId) throws OXException {
        super();
        Connection connection = null;
        if (parameters != null) {
            connection = parameters.get(Connection.class.getName());
        }

        boolean external = true;
        if (connection == null) {
            external = false;
            if (mode == ConnectionMode.READ) {
                connection = dbService.getReadOnly(contextId);
            } else {
                connection = dbService.getWritable(contextId);
            }
        } else {
            try {
                if (mode == ConnectionMode.WRITE && connection.isReadOnly()) {
                    external = false;
                    connection = dbService.getWritable(contextId);
                }
            } catch (SQLException e) {
                throw ShareExceptionCodes.DB_ERROR.create(e, e.getMessage());
            }
        }

        this.dbService = dbService;
        this.connection = connection;
        this.external = external;
        this.contextId = contextId;
        this.mode = mode;
    }

    public Connection get() {
        return connection;
    }

    public void close() {
        if (!external) {
            if (mode == ConnectionMode.READ) {
                dbService.backReadOnly(contextId, connection);
            } else {
                dbService.backWritable(contextId, connection);
            }
        }
    }

}
