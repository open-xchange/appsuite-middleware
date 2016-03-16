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

package com.openexchange.snippet.rdb.groupware;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.snippet.ReferenceType;
import com.openexchange.snippet.rdb.RdbSnippetManagement;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbSnippetDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbSnippetDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link RdbSnippetDeleteListener}.
     */
    public RdbSnippetDeleteListener() {
        super();
    }

    private TIntList getIds(int userId, int contextId, Connection writeCon) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                stmt = writeCon.prepareStatement("SELECT id FROM snippet WHERE cid = ? AND user = ? AND refType=" + ReferenceType.GENCONF.getType());
                stmt.setInt(2, userId);
            } else {
                stmt = writeCon.prepareStatement("SELECT id FROM snippet WHERE cid = ? AND refType=" + ReferenceType.GENCONF.getType());
            }
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();

            TIntList ids = new TIntArrayList(4);
            while (rs.next()) {
                ids.add(Integer.parseInt(rs.getString(1)));
            }
            return ids;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws com.openexchange.exception.OXException {
        if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            try {
                int contextId = event.getContext().getContextId();
                RdbSnippetManagement.deleteForContext(contextId, writeCon);
            } catch (final RuntimeException e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            }
            return;
        }

        if (DeleteEvent.TYPE_USER == event.getType()) {
            /*
             * Writable connection
             */
            final int contextId = event.getContext().getContextId();
            PreparedStatement stmt = null;
            try {
                final int userId = event.getId();
                TIntList ids = getIds(userId, contextId, writeCon);
                if (ids.isEmpty()) {
                    return;
                }
                /*
                 * Delete them
                 */
                final AtomicReference<OXException> error = new AtomicReference<OXException>();
                ids.forEach(new TIntProcedure() {

                    @Override
                    public boolean execute(final int id) {
                        try {
                            RdbSnippetManagement.deleteSnippet(id, userId, contextId, writeCon);
                            return true;
                        } catch (final OXException e) {
                            error.set(e);
                            return false;
                        }
                    }
                });
                final OXException e = error.get();
                if (null != e) {
                    throw e;
                }
            } catch (final SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } catch (final RuntimeException e) {
                throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        }
    }

}
