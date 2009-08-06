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

package com.openexchange.eav.storage.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.eav.storage.db.exception.EAVStorageException;
import com.openexchange.eav.storage.db.exception.EAVStorageExceptionMessage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.SELECT;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.eav.storage.db.sql.PathIndex.*;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class PathIndex {

    /**
     * Returns the name of the path table, which is responsible for the given object.
     * 
     * @param con A connection
     * @param ctx the context
     * @param mod The module of the given object
     * @param id The object id
     * @return The table name or null if this object has no eav data
     * @throws EAVStorageException
     */
    public static String getPathTable(Connection con, Context ctx, int mod, int id) throws EAVStorageException {
        List<Object> values = new ArrayList<Object>();
        
        SELECT select = new SELECT(pathTable)
            .FROM(pathIndex)
            .WHERE(new EQUALS(cid, PLACEHOLDER)
                .AND(new EQUALS(module, PLACEHOLDER))
                .AND(new EQUALS(objectId, PLACEHOLDER)));
        
        values.add(ctx.getContextId());
        values.add(mod);
        values.add(id);

        StatementBuilder sb = new StatementBuilder();
        ResultSet rs = null;
        String retval = null;
        try {
            rs = sb.executeQuery(con, select, values);
            if (rs.next()) {
                retval = rs.getString(1);
            }
        } catch (SQLException e) {
            throw EAVStorageExceptionMessage.SQLException.create(e);
        } finally {
            try {
                sb.closePreparedStatement(null, rs);
            } catch (SQLException e) {
                throw EAVStorageExceptionMessage.SQLException.create(e);
            }
        }
        return retval;
    }
}
