/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.messaging.generic.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.messaging.generic.services.MessagingGenericServiceRegistry;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;

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
                    Databases.closeSQLStuff(rs);
                }
            }
            Databases.closeSQLStuff(stmt);
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
                        } catch (OXException e) {
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
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private <S> S getService(final Class<? extends S> clazz) throws OXException {
        try {
            return MessagingGenericServiceRegistry.getService(clazz);
        } catch (RuntimeException e) {
            throw new OXException(e);
        }
    }

}
