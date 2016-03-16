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

package com.openexchange.messaging.generic.groupware;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.messaging.generic.services.MessagingGenericServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link MessagingGenericDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingGenericDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link MessagingGenericDeleteListener}.
     */
    public MessagingGenericDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws com.openexchange.exception.OXException {
        if (DeleteEvent.TYPE_USER != event.getType()) {
            return;
        }
        final DatabaseService databaseService = getService(DatabaseService.class);
        /*
         * Writable connection
         */
        final int contextId = event.getContext().getContextId();
        PreparedStatement stmt = null;
        try {
            final int userId = event.getId();
            final TIntList confIds;
            {
                ResultSet rs = null;
                try {
                    stmt = writeCon.prepareStatement("SELECT confId FROM messagingAccount WHERE cid = ? AND user = ?");
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, userId);
                    rs = stmt.executeQuery();
                    confIds = new TIntArrayList(4);
                    while (rs.next()) {
                        confIds.add(rs.getInt(1));
                    }
                } finally {
                    DBUtils.closeSQLStuff(rs);
                }
            }
            DBUtils.closeSQLStuff(stmt);
            /*
             * Delete account configurations using generic conf
             */
            if (!confIds.isEmpty()) {
                final GenericConfigurationStorageService genericConfStorageService = getService(GenericConfigurationStorageService.class);
                final Context context = event.getContext();
                class GenConfDelete implements TIntProcedure {

                    OXException genConfError;

                    @Override
                    public boolean execute(final int confId) {
                        try {
                            genericConfStorageService.delete(writeCon, context, confId);
                            return true;
                        } catch (final OXException e) {
                            genConfError = e;
                            return false;
                        }
                    }
                }
                final GenConfDelete gcd = new GenConfDelete();
                if (!confIds.forEach(gcd) && null != gcd.genConfError) {
                    throw gcd.genConfError;
                }
            }
            /*
             * Delete account data
             */
            stmt = writeCon.prepareStatement("DELETE FROM messagingAccount WHERE cid = ? AND user = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private <S> S getService(final Class<? extends S> clazz) throws OXException {
        try {
            return MessagingGenericServiceRegistry.getService(clazz);
        } catch (final RuntimeException e) {
            throw new OXException(e);
        }
    }

}
