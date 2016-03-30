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

package com.openexchange.file.storage.rdb.groupware;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.rdb.Services;
import com.openexchange.file.storage.rdb.internal.CachingFileStorageAccountStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FileStorageRdbDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageRdbDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link FileStorageRdbDeleteListener}.
     */
    public FileStorageRdbDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws com.openexchange.exception.OXException {
        if (DeleteEvent.TYPE_USER != event.getType()) {
            return;
        }

        int contextId = event.getContext().getContextId();
        PreparedStatement stmt = null;
        try {
            int userId = event.getId();

            class AccountAndService {
                final int accountId;
                final String serviceId;

                AccountAndService(int accountId, String serviceId) {
                    super();
                    this.accountId = accountId;
                    this.serviceId = serviceId;
                }
            }

            List<AccountAndService> accounts;
            TIntList confIds;
            {
                ResultSet rs = null;
                try {
                    stmt = writeCon.prepareStatement("SELECT confId, account, serviceId FROM filestorageAccount WHERE cid = ? AND user = ?");
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, userId);
                    rs = stmt.executeQuery();
                    accounts = new LinkedList<AccountAndService>();
                    confIds = new TIntArrayList(4);
                    while (rs.next()) {
                        confIds.add(rs.getInt(1));
                        accounts.add(new AccountAndService(rs.getInt(2), rs.getString(3)));
                    }
                } finally {
                    DBUtils.closeSQLStuff(rs);
                }
            }
            DBUtils.closeSQLStuff(stmt);
            stmt = null;

            // Invalidate cache
            if (!accounts.isEmpty()) {
                CachingFileStorageAccountStorage cache = CachingFileStorageAccountStorage.getInstance();
                for (AccountAndService accountAndService : accounts) {
                    cache.invalidate(accountAndService.serviceId, accountAndService.accountId, userId, contextId);
                }
            }

            // Delete account configurations using generic conf
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
                GenConfDelete gcd = new GenConfDelete();
                if (!confIds.forEach(gcd) && null != gcd.genConfError) {
                    throw gcd.genConfError;
                }
            }

            // Delete account data
            stmt = writeCon.prepareStatement("DELETE FROM filestorageAccount WHERE cid = ? AND user = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private <S> S getService(final Class<? extends S> clazz) throws OXException {
        try {
            return Services.getService(clazz);
        } catch (final IllegalStateException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
