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

package com.openexchange.mail.headercache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.mail.headercache.services.HeaderCacheServiceRegistry;
import com.openexchange.server.OXException;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link HeaderCacheDeleteListener} 
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderCacheDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link HeaderCacheDeleteListener}.
     */
    public HeaderCacheDeleteListener() {
        super();
    }

    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws DeleteFailedException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            final DatabaseService databaseService = getDBService();
            final int contextId = event.getContext().getContextId();
            final Connection wc;
            try {
                wc = databaseService.getWritable(contextId);
                wc.setAutoCommit(false); // BEGIN;
            } catch (final DBPoolingException e) {
                throw new DeleteFailedException(e);
            } catch (final SQLException e) {
                throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getMessage());
            }
            final int user = event.getId();
            PreparedStatement stmt = null;
            try {
                /*
                 * Drop headers
                 */
                stmt = wc.prepareStatement("DELETE FROM headersAsBlob WHERE cid = ? AND user = ?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.executeUpdate();
                DBUtils.closeSQLStuff(stmt);
                /*
                 * Drop UUIDs
                 */
                stmt = wc.prepareStatement("DELETE FROM mailUUID WHERE cid = ? AND user = ?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.executeUpdate();
                wc.commit(); // COMMIT
            } catch (final SQLException e) {
                DBUtils.rollback(wc); // ROLL-BACK
                throw new DeleteFailedException(DeleteFailedException.Code.SQL_ERROR, e, e.getMessage());
            } catch (final Exception e) {
                DBUtils.rollback(wc); // ROLL-BACK
                throw new DeleteFailedException(DeleteFailedException.Code.ERROR, e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
                DBUtils.autocommit(wc);
                databaseService.backWritable(contextId, wc);
            }
        }
    }

    private static DatabaseService getDBService() throws DeleteFailedException {
        final DatabaseService databaseService;
        try {
            databaseService = HeaderCacheServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final OXException e) {
            throw new DeleteFailedException(e);
        }
        return databaseService;
    }

}
