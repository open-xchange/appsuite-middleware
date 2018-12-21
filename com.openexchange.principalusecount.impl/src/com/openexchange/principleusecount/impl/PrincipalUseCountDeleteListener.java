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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.principleusecount.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;


/**
 *
 * {@link PrincipalUseCountDeleteListener}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class PrincipalUseCountDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link PrincipalUseCountDeleteListener}.
     */
    public PrincipalUseCountDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        int type = event.getType();
        if (type == DeleteEvent.TYPE_CONTEXT) {
            handleContextDeletion(writeCon, event.getContext());
        }
        if (type == DeleteEvent.TYPE_GROUP || type == DeleteEvent.TYPE_RESOURCE) {
            handlePrincipalDeletion(writeCon, event.getContext(), event.getId());
        }

    }

    private void handleContextDeletion(Connection writeCon, Context ctx) throws OXException {
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();
            stmt.executeUpdate("DELETE FROM principalUseCount WHERE cid=" + ctx.getContextId());
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void handlePrincipalDeletion(Connection writeCon, Context ctx, int id) throws OXException {
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();
            stmt.executeUpdate("DELETE FROM principalUseCount WHERE cid=" + ctx.getContextId() + " AND principal=" + id);
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }
}
