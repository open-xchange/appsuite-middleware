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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import com.openexchange.database.DBPoolingException;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountExceptionFactory;
import com.openexchange.mailaccount.MailAccountExceptionMessages;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedINBOXManagementImpl} - The Unified INBOX management implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXManagementImpl implements UnifiedINBOXManagement {

    private static final String SQL_CHECK = "SELECT url FROM user_mail_account WHERE cid = ? AND user = ? AND name = ?";

    private static final String SQL_ENABLED = "SELECT id FROM user_mail_account WHERE cid = ? AND user = ? AND unified_inbox > 0";

    /**
     * Initializes a new {@link UnifiedINBOXManagementImpl}.
     */
    public UnifiedINBOXManagementImpl() {
        super();
    }

    public void createUnifiedINBOX(final int userId, final int contextId) throws MailAccountException {
        createUnifiedINBOX(userId, contextId, null);
    }

    public void createUnifiedINBOX(final int userId, final int contextId, final Connection con) throws MailAccountException {
        try {
            final MailAccountStorageService storageService =
                ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            // Check if Unified INBOX account already exists for given user
            if (exists(userId, contextId, con)) {
                // User already has the Unified INBOX account set
                throw MailAccountExceptionMessages.DUPLICATE_UNIFIED_INBOX_ACCOUNT.create(
                    Integer.valueOf(userId),
                    Integer.valueOf(contextId));
            }
            final Context ctx;
            {
                // Prefer context service
                final ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class);
                if (null == contextService) {
                    ctx = ContextStorage.getStorageContext(contextId);
                } else {
                    ctx = contextService.getContext(contextId);
                }
            }
            // Create and fill appropriate description object
            final MailAccountDescription mailAccountDescription = new MailAccountDescription();
            mailAccountDescription.setName(NAME_UNIFIED_INBOX);
            mailAccountDescription.setConfirmedHam("confirmed-ham");
            mailAccountDescription.setConfirmedSpam("confirmed-spam");
            mailAccountDescription.setDefaultFlag(false);
            mailAccountDescription.setDrafts("drafts");
            final String login = getUserLogin(userId, ctx);
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
            // Create it
            if (null == con) {
                storageService.insertMailAccount(mailAccountDescription, userId, ctx, null);
            } else {
                storageService.insertMailAccount(mailAccountDescription, userId, ctx, null, con);
            }
        } catch (final ServiceException e) {
            throw new MailAccountException(e);
        } catch (final MailAccountException e) {
            throw new MailAccountException(e);
        } catch (final ContextException e) {
            throw new MailAccountException(e);
        }

    }

    public void deleteUnifiedINBOX(final int userId, final int contextId) throws MailAccountException {
        deleteUnifiedINBOX(userId, contextId, null);
    }

    public void deleteUnifiedINBOX(final int userId, final int contextId, final Connection con) throws MailAccountException {
        try {
            final MailAccountStorageService storageService =
                ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            // Determine the ID of the Unified INBOX account for given user
            final MailAccount[] existingAccounts = storageService.getUserMailAccounts(userId, contextId);
            int id = -1;
            for (int i = 0; i < existingAccounts.length && id < 0; i++) {
                final MailAccount mailAccount = existingAccounts[i];
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                    id = mailAccount.getId();
                }
            }
            // Delete the Unified INBOX account
            if (id >= 0) {
                if (null == con) {
                    storageService.deleteMailAccount(id, Collections.<String, Object> emptyMap(), userId, contextId, false);
                } else {
                    storageService.deleteMailAccount(id, Collections.<String, Object> emptyMap(), userId, contextId, false, con);
                }
            }
        } catch (final ServiceException e) {
            throw new MailAccountException(e);
        } catch (final MailAccountException e) {
            throw new MailAccountException(e);
        }
    }

    public boolean exists(final int userId, final int contextId) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(contextId, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            return exists(userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    public boolean exists(final int userId, final int contextId, final Connection con) throws MailAccountException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_CHECK);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, NAME_UNIFIED_INBOX);
            rs = stmt.executeQuery();
            while (rs.next()) {
                final String url = rs.getString(1);
                if (!rs.wasNull() && url != null && url.startsWith(UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX, 0)) {
                    return true;
                }
            }
            return false;
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    public boolean isEnabled(final int userId, final int contextId) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(contextId, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            return isEnabled(userId, contextId, con);
        } finally {
            Database.back(contextId, false, con);
        }
    }

    public boolean isEnabled(final int userId, final int contextId, final Connection con) throws MailAccountException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_ENABLED);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    public int getUnifiedINBOXAccountID(final int userId, final int contextId) throws MailAccountException {
        return getUnifiedINBOXAccountID(userId, contextId, null);
    }

    public int getUnifiedINBOXAccountID(final int userId, final int contextId, final Connection con) throws MailAccountException {
        try {
            final MailAccountStorageService storageService =
                ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            // Look-up the Unified INBOX account for given user
            final MailAccount[] existingAccounts;
            if (null == con) {
                existingAccounts = storageService.getUserMailAccounts(userId, contextId);
            } else {
                existingAccounts = storageService.getUserMailAccounts(userId, contextId, con);
            }
            for (final MailAccount mailAccount : existingAccounts) {
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                    return mailAccount.getId();
                }
            }
            return -1;
        } catch (final ServiceException e) {
            throw new MailAccountException(e);
        } catch (final MailAccountException e) {
            throw new MailAccountException(e);
        }
    }

    /*-
     * +++++++++++++++++++++++++++++++++++++++++++++ HELPERS +++++++++++++++++++++++++++++++++++++++++++++
     */

    private static String getUserLogin(final int userId, final Context ctx) throws MailAccountException {
        try {
            final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class, true);
            return userService.getUser(userId, ctx).getLoginInfo();
        } catch (final ServiceException e) {
            throw new MailAccountException(e);
        } catch (final UserException e) {
            throw new MailAccountException(e);
        }
    }

}
