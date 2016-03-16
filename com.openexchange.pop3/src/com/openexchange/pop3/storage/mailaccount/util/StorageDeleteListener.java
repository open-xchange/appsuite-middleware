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

package com.openexchange.pop3.storage.mailaccount.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3Provider;
import com.openexchange.pop3.services.POP3ServiceRegistry;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.pop3.storage.mailaccount.RdbPOP3StorageProperties;
import com.openexchange.pop3.storage.mailaccount.RdbPOP3StorageTrashContainer;
import com.openexchange.pop3.storage.mailaccount.RdbPOP3StorageUIDLMap;
import com.openexchange.pop3.storage.mailaccount.SessionParameterNames;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link StorageDeleteListener} - Delete listener for mail account POP3 storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StorageDeleteListener implements MailAccountDeleteListener {

    /**
     * Initializes a new {@link StorageDeleteListener}.
     */
    public StorageDeleteListener() {
        super();
    }

    @Override
    public void onAfterMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        // Nothing to do
    }

    @Override
    public void onBeforeMailAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        try {
            /*
             * Check if account denotes a POP3 account
             */
            final String url;
            {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT url FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?");
                    stmt.setLong(1, cid);
                    stmt.setLong(2, id);
                    stmt.setLong(3, user);
                    rs = stmt.executeQuery();
                    if (!rs.next()) {
                        /*
                         * Strange...
                         */
                        return;
                    }
                    url = rs.getString(1).toLowerCase(Locale.ENGLISH);
                } catch (final SQLException e) {
                    throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    DBUtils.closeSQLStuff(rs, stmt);
                }
            }
            if (!url.startsWith(POP3Provider.PROTOCOL_POP3.getName())) {
                /*
                 * Not a POP3 account...
                 */
                return;
            }
            /*
             * Delete storage content for user if a valid session can be found
             */
            SessiondService sessiondService = POP3ServiceRegistry.getServiceRegistry().getService(SessiondService.class);
            if (null != sessiondService) {
                for (Session session : sessiondService.getSessions(user, cid)) {
                    final POP3Access pop3Access = POP3Access.newInstance(session, id);
                    final POP3Storage pop3Storage = pop3Access.getPOP3Storage();
                    pop3Storage.drop();

                    String key = SessionParameterNames.getStorageProperties(id);
                    session.setParameter(key, null);
                    key = SessionParameterNames.getTrashContainer(id);
                    session.setParameter(key, null);
                    key = SessionParameterNames.getUIDLMap(cid);
                    session.setParameter(key, null);
                }
            }
            /*
             * Drop database entries
             */
            final boolean restoreConstraints = disableForeignKeyChecks(con);
            try {
                RdbPOP3StorageProperties.dropProperties(id, user, cid, con);
                RdbPOP3StorageTrashContainer.dropTrash(id, user, cid, con);
                RdbPOP3StorageUIDLMap.dropIDs(id, user, cid, con);
            } finally {
                if (restoreConstraints) {
                    try {
                        enableForeignKeyChecks(con);
                    } catch (final SQLException e) {
                        org.slf4j.LoggerFactory.getLogger(StorageDeleteListener.class).error("", e);
                    }
                }
            }
        } catch (final OXException e) {
            throw e;
        }
    }

    private static boolean disableForeignKeyChecks(final Connection con) {
        if (null == con) {
            return false;
        }
        try {
            DBUtils.disableMysqlForeignKeyChecks(con);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    private static void enableForeignKeyChecks(final Connection con) throws SQLException {
        if (null == con) {
            return;
        }
        DBUtils.enableMysqlForeignKeyChecks(con);
    }

}
