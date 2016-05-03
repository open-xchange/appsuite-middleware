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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedInboxManagementImpl} - The Unified Mail management implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxManagementImpl implements UnifiedInboxManagement {

    private static final String SQL_CHECK = "SELECT url FROM user_mail_account WHERE cid = ? AND user = ? AND name = ?";

    private static final String SQL_ENABLED = "SELECT 1 FROM user_mail_account WHERE cid = ? AND user = ? AND unified_inbox > 0";

    /**
     * Initializes a new {@link UnifiedInboxManagementImpl}.
     */
    public UnifiedInboxManagementImpl() {
        super();
    }

    @Override
    public void createUnifiedINBOX(int userId, int contextId) throws OXException {
        createUnifiedINBOX(userId, contextId, null);
    }

    @Override
    public void createUnifiedINBOX(int userId, int contextId, Connection con) throws OXException {
        try {
            MailAccountFacade mailAccountFacade = ServerServiceRegistry.getInstance().getService(MailAccountFacade.class, true);
            // Check if Unified Mail account already exists for given user
            if (exists(userId, contextId, con)) {
                // User already has the Unified Mail account set
                throw MailAccountExceptionCodes.DUPLICATE_UNIFIED_INBOX_ACCOUNT.create(
                    Integer.valueOf(userId),
                    Integer.valueOf(contextId));
            }
            Context ctx;
            {
                // Prefer context service
                ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class);
                if (null == contextService) {
                    ctx = ContextStorage.getStorageContext(contextId);
                } else {
                    ctx = contextService.getContext(contextId);
                }
            }
            // Create and fill appropriate description object
            MailAccountDescription mailAccountDescription = new MailAccountDescription();
            mailAccountDescription.setName(NAME_UNIFIED_INBOX);
            mailAccountDescription.setConfirmedHam("confirmed-ham");
            mailAccountDescription.setConfirmedSpam("confirmed-spam");
            mailAccountDescription.setDefaultFlag(false);
            mailAccountDescription.setDrafts("drafts");
            String login = getUserLogin(userId, ctx);
            mailAccountDescription.setLogin(login);
            mailAccountDescription.setMailPort(143);
            mailAccountDescription.setMailProtocol(PROTOCOL_UNIFIED_INBOX);
            mailAccountDescription.setMailSecure(false);
            mailAccountDescription.setMailServer("localhost");
            mailAccountDescription.setPassword("");
            mailAccountDescription.setPrimaryAddress(new StringBuilder(32).append(login).append("@unifiedinbox.com").toString());
            mailAccountDescription.setSent("sent");
            mailAccountDescription.setSpam("spam");
            mailAccountDescription.setSpamHandler("NoSpamHandler");
            // No transport settings
            mailAccountDescription.setTransportServer((String) null);
            mailAccountDescription.setTrash("trash");
            mailAccountDescription.setArchive("archive");
            // Create it
            if (null == con) {
                mailAccountFacade.insertMailAccount(mailAccountDescription, userId, ctx, null);
            } else {
                mailAccountFacade.insertMailAccount(mailAccountDescription, userId, ctx, null, con);
            }
            // Drop session parameters
            try {
                SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
                if (null != sessiondService) {
                    for (Session session : sessiondService.getSessions(userId, contextId)) {
                        session.setParameter("com.openexchange.mailaccount.unifiedMailAccountId", null);
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        } catch (OXException e) {
            throw e;
        }

    }

    @Override
    public void deleteUnifiedINBOX(int userId, int contextId) throws OXException {
        deleteUnifiedINBOX(userId, contextId, null);
    }

    @Override
    public void deleteUnifiedINBOX(int userId, int contextId, Connection con) throws OXException {
        try {
            MailAccountFacade mailAccountFacade = ServerServiceRegistry.getInstance().getService(MailAccountFacade.class, true);

            // Determine the ID of the Unified Mail account for given user
            MailAccount[] existingAccounts = mailAccountFacade.getUserMailAccounts(userId, contextId);
            int id = -1;
            for (int i = 0; i < existingAccounts.length && id < 0; i++) {
                MailAccount mailAccount = existingAccounts[i];
                if (UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                    id = mailAccount.getId();
                }
            }
            // Delete the Unified Mail account
            if (id >= 0) {
                dropSessionParameter(userId, contextId);
                if (null == con) {
                    mailAccountFacade.deleteMailAccount(id, Collections.<String, Object> emptyMap(), userId, contextId, false);
                } else {
                    mailAccountFacade.deleteMailAccount(id, Collections.<String, Object> emptyMap(), userId, contextId, false, con);
                }
            }
        } catch (OXException e) {
            throw e;
        }
    }

    private void dropSessionParameter(final int userId, final int contextId) {
        Task<Void> task = new AbstractTask<Void>() {

            @Override
            public Void call() {
                SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                if (null != service) {
                    for (Session session : service.getSessions(userId, contextId)) {
                        session.setParameter("com.openexchange.mailaccount.unifiedMailEnabled", null);
                    }
                }
                return null;
            }
        };
        ThreadPools.getThreadPool().submit(task);
    }

    @Override
    public boolean exists(int userId, int contextId) throws OXException {
        Connection con = Database.get(contextId, false);
        try {
            return exists(userId, contextId, con);
        } finally {
            Database.backAfterReading(contextId, con);
        }
    }

    @Override
    public boolean exists(int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            return exists(userId, contextId);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_CHECK);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, NAME_UNIFIED_INBOX);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String url = rs.getString(1);
                if (!rs.wasNull() && url != null && url.startsWith(UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX, 0)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        Boolean b = (Boolean) session.getParameter("com.openexchange.mailaccount.unifiedMailEnabled");
        if (null != b) {
            return b.booleanValue();
        }

        b = Boolean.valueOf(isEnabled(session.getUserId(), session.getContextId()));
        session.setParameter("com.openexchange.mailaccount.unifiedMailEnabled", b);
        return b.booleanValue();
    }

    @Override
    public boolean isEnabled(int userId, int contextId) throws OXException {
        Connection con = Database.get(contextId, false);
        try {
            return isEnabled(userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    @Override
    public boolean isEnabled(int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_ENABLED);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public int getUnifiedINBOXAccountID(Session session) throws OXException {
        Integer i = (Integer) session.getParameter("com.openexchange.mailaccount.unifiedMailAccountId");
        if (null != i) {
            return i.intValue();
        }
        int unifiedINBOXAccountId = getUnifiedINBOXAccountID(session.getUserId(), session.getContextId());
        session.setParameter("com.openexchange.mailaccount.unifiedMailAccountId", Integer.valueOf(unifiedINBOXAccountId));
        return unifiedINBOXAccountId;
    }

    @Override
    public int getUnifiedINBOXAccountID(int userId, int contextId) throws OXException {
        DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);
        Connection con = databaseService.getReadOnly(contextId);
        try {
            return getUnifiedINBOXAccountID(userId, contextId, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public int getUnifiedINBOXAccountID(int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            return getUnifiedINBOXAccountID(userId, contextId);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user_mail_account WHERE cid = ? AND user = ? AND url LIKE ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX + '%');
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public int getUnifiedINBOXAccountIDIfEnabled(int userId, int contextId) throws OXException {
        DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);
        Connection con = databaseService.getReadOnly(contextId);
        try {
            return getUnifiedINBOXAccountIDIfEnabled(userId, contextId, con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public int getUnifiedINBOXAccountIDIfEnabled(int userId, int contextId, Connection con) throws OXException {
        if (null == con) {
            return getUnifiedINBOXAccountIDIfEnabled(userId, contextId);
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id FROM user_mail_account WHERE cid = ? AND user = ? AND url LIKE ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX + '%');
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return -1;
            }
            int id = rs.getInt(1);
            DBUtils.closeSQLStuff(rs, stmt);

            stmt = con.prepareStatement(SQL_ENABLED);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            return rs.next() ? id : -1;
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    /*-
     * +++++++++++++++++++++++++++++++++++++++++++++ HELPERS +++++++++++++++++++++++++++++++++++++++++++++
     */

    private static String getUserLogin(int userId, Context ctx) throws OXException {
        UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class, true);
        return userService.getUser(userId, ctx).getLoginInfo();
    }

}
