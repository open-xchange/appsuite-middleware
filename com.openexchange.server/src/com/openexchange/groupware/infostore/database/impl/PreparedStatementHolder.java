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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * {@link PreparedStatementHolder}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PreparedStatementHolder {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PreparedStatementHolder.class);

    private final Connection writeConnection;
    private final Map<String, PreparedStatement> statements = new HashMap<String, PreparedStatement>();

    public PreparedStatementHolder(Connection writeConnection) {
        this.writeConnection = writeConnection;
    }

    public void execute(String sql, Object...replacements) throws SQLException {
        PreparedStatement statement = get(sql);
        for(int i = 0, size = replacements.length; i < size; i++) {
            statement.setObject(i+1, replacements[i]);
        }
        statement.executeUpdate();
    }

    private PreparedStatement get(String sql) throws SQLException {
        if (statements.containsKey(sql)) {
            return statements.get(sql);
        } else {
            PreparedStatement prepped = writeConnection.prepareStatement(sql);
            statements.put(sql, prepped);
            return prepped;
        }
    }

    public void close() {
        for(PreparedStatement stmt : statements.values()) {
            try {
                stmt.close();
            } catch (SQLException x) {
                LOG.error("", x);
            }
        }
    }

    public Connection getConnection() {
        return writeConnection;
    }


}
