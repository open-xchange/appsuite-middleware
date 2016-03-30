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

package com.openexchange.mailaccount.internal;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.procedure.TIntProcedure;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCode;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailAccountDeleteListener} - {@link DeleteListener} for mail account storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link MailAccountDeleteListener}.
     */
    public MailAccountDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon) throws OXException {
        if (deleteEvent.getType() == DeleteEvent.TYPE_USER) {
            final MailAccountStorageService storageService =
                ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            final int user = deleteEvent.getId();
            final int cid = deleteEvent.getContext().getContextId();
            final OXException[] cup = new OXException[1];
            final TIntList ids = getUserMailAccountIDs(user, cid, writeCon);
            ids.forEach(new TIntProcedure() {

                @Override
                public boolean execute(final int accountId) {
                    try {
                        storageService.deleteMailAccount(accountId, Collections.<String, Object> emptyMap(), user, cid, true, writeCon);
                        return true;
                    } catch (final OXException e) {
                        cup[0] = e;
                        return false;
                    } catch (final RuntimeException e) {
                        cup[0] = MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                        return false;
                    }
                }
            });
            final OXException err = cup[0];
            if (null != err) {
                throw err;
            }
        }
    }

    private TIntList getUserMailAccountIDs(final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id");
            stmt.setLong(1, cid);
            stmt.setLong(2, user);
            result = stmt.executeQuery();
            if (!result.next()) {
                return new TIntLinkedList();
            }
            final TIntList ids = new TIntArrayList(8);
            do {
                ids.add(result.getInt(1));
            } while (result.next());
            return ids;
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

}
