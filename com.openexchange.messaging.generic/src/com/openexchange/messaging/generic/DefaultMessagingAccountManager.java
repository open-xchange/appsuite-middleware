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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.generic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.datatypes.genericonf.storage.GenericConfigStorageException;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.services.MessagingGenericServiceRegistry;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DefaultMessagingAccountManager} - The default messaging account manager.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public class DefaultMessagingAccountManager implements MessagingAccountManager {

    private final String serviceId;

    /**
     * Initializes a new {@link DefaultMessagingAccountManager}.
     * 
     * @param serviceId The messaging service identifier
     */
    public DefaultMessagingAccountManager(final String serviceId) {
        super();
        this.serviceId = serviceId;
    }

    private static final String SQL_SELECT =
        "SELECT confId, serviceId, displayName FROM messagingAccount WHERE cid = ? AND user = ? AND account = ?";

    public MessagingAccount getAccount(final int id, final Session session) throws MessagingException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        /*
         * Readable connection
         */
        final int contextId = session.getContextId();
        final Connection rc;
        try {
            rc = databaseService.getReadOnly(contextId);
        } catch (final DBPoolingException e) {
            throw new MessagingException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = rc.prepareStatement(SQL_SELECT);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, session.getUserId());
            stmt.setInt(pos, id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw MessagingExceptionCodes.ACCOUNT_NOT_FOUND.create(
                    Integer.valueOf(id),
                    Integer.valueOf(session.getUserId()),
                    Integer.valueOf(contextId));
            }
            final DefaultMessagingAccount account = new DefaultMessagingAccount();
            account.setDisplayName(rs.getString(3));
            {
                final GenericConfigurationStorageService genericConfStorageService = getService(GenericConfigurationStorageService.class);
                final Map<String, Object> configuration = new HashMap<String, Object>();
                genericConfStorageService.fill(rc, getContext(session), rs.getInt(1), configuration);
                account.setConfiguration(configuration);
            }
            account.setId(id);
            {
                final MessagingServiceRegistry registry = getService(MessagingServiceRegistry.class);
                account.setMessagingService(registry.getMessagingService(rs.getString(2)));
            }
            return account;
        } catch (final SQLException e) {
            throw MessagingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final GenericConfigStorageException e) {
            throw new MessagingException(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, rc);
        }
    }

    private static final String SQL_SELECT_ACCOUNTS =
        "SELECT account, confId, serviceId, displayName FROM messagingAccount WHERE cid = ? AND user = ? AND serviceId = ?";

    public List<MessagingAccount> getAccounts(final Session session) throws MessagingException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        /*
         * Readable connection
         */
        final int contextId = session.getContextId();
        final Connection rc;
        try {
            rc = databaseService.getReadOnly(contextId);
        } catch (final DBPoolingException e) {
            throw new MessagingException(e);
        }
        List<MessagingAccount> accounts;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = rc.prepareStatement(SQL_SELECT_ACCOUNTS);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, session.getUserId());
            stmt.setString(pos, serviceId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                accounts = new ArrayList<MessagingAccount>(4);
                final GenericConfigurationStorageService genericConfStorageService = getService(GenericConfigurationStorageService.class);
                final MessagingService messagingService;
                {
                    final MessagingServiceRegistry registry = getService(MessagingServiceRegistry.class);
                    messagingService = registry.getMessagingService(serviceId);
                }
                do {
                    final DefaultMessagingAccount account = new DefaultMessagingAccount();
                    account.setDisplayName(rs.getString(4));
                    final Map<String, Object> configuration = new HashMap<String, Object>();
                    genericConfStorageService.fill(rc, getContext(session), rs.getInt(2), configuration);
                    account.setConfiguration(configuration);
                    account.setId(rs.getInt(1));
                    account.setMessagingService(messagingService);
                    accounts.add(account);
                } while (rs.next());
            } else {
                accounts = Collections.emptyList();
            }
            return accounts;
        } catch (final SQLException e) {
            throw MessagingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final GenericConfigStorageException e) {
            throw new MessagingException(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, rc);
        }
    }

    private static final String SQL_INSERT =
        "INSERT INTO messagingAccount (cid, user, account, confId, serviceId, displayName) VALUES (?, ?, ?, ?, ?, ?)";

    public void addAccount(final MessagingAccount account, final Session session) throws MessagingException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        /*
         * Writable connection
         */
        final int contextId = session.getContextId();
        final Connection wc;
        try {
            wc = databaseService.getWritable(contextId);
            wc.setAutoCommit(false); // BEGIN
        } catch (final DBPoolingException e) {
            throw new MessagingException(e);
        } catch (final SQLException e) {
            throw MessagingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            /*
             * Save account configuration using generic conf
             */
            final int genericConfId;
            {
                final GenericConfigurationStorageService genericConfStorageService = getService(GenericConfigurationStorageService.class);
                genericConfId = genericConfStorageService.save(wc, getContext(session), account.getConfiguration());
            }
            /*
             * Insert account data
             */
            stmt = wc.prepareStatement(SQL_INSERT);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, session.getUserId());
            stmt.setInt(pos++, account.getId());
            stmt.setInt(pos++, genericConfId);
            stmt.setString(pos++, account.getMessagingService().getId());
            stmt.setString(pos, account.getDisplayName());
            stmt.executeUpdate();
            wc.commit(); // COMMIT
        } catch (final GenericConfigStorageException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MessagingException(e);
        } catch (final SQLException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MessagingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(wc);
            databaseService.backWritable(contextId, wc);
        }
    }

    private static final String SQL_DELETE = "DELETE FROM messagingAccount WHERE cid = ? AND user = ? AND account = ?";

    public void deleteAccount(final MessagingAccount account, final Session session) throws MessagingException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        /*
         * Writable connection
         */
        final int contextId = session.getContextId();
        final Connection wc;
        try {
            wc = databaseService.getWritable(contextId);
            wc.setAutoCommit(false); // BEGIN
        } catch (final DBPoolingException e) {
            throw new MessagingException(e);
        } catch (final SQLException e) {
            throw MessagingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            final int genericConfId;
            {
                ResultSet rs = null;
                try {
                    stmt = wc.prepareStatement(SQL_SELECT);
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, session.getUserId());
                    stmt.setInt(pos, account.getId());
                    rs = stmt.executeQuery();
                    if (!rs.next()) {
                        throw MessagingExceptionCodes.ACCOUNT_NOT_FOUND.create(
                            Integer.valueOf(account.getId()),
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(contextId));
                    }
                    genericConfId = rs.getInt(1);
                } finally {
                    DBUtils.closeSQLStuff(rs);
                }
            }
            DBUtils.closeSQLStuff(stmt);
            /*
             * Delete account configuration using generic conf
             */
            {
                final GenericConfigurationStorageService genericConfStorageService = getService(GenericConfigurationStorageService.class);
                genericConfStorageService.delete(wc, getContext(session), genericConfId);
            }
            /*
             * Delete account data
             */
            stmt = wc.prepareStatement(SQL_DELETE);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, session.getUserId());
            stmt.setInt(pos, account.getId());
            stmt.executeUpdate();
            wc.commit(); // COMMIT
        } catch (final GenericConfigStorageException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MessagingException(e);
        } catch (final SQLException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MessagingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(wc);
            databaseService.backWritable(contextId, wc);
        }
    }

    private static final String SQL_UPDATE = "UPDATE messagingAccount SET displayName = ? WHERE cid = ? AND user = ? AND account = ?";

    public void updateAccount(final MessagingAccount account, final Session session) throws MessagingException {
        final DatabaseService databaseService = getService(DatabaseService.class);
        /*
         * Writable connection
         */
        final int contextId = session.getContextId();
        final Connection wc;
        try {
            wc = databaseService.getWritable(contextId);
            wc.setAutoCommit(false); // BEGIN
        } catch (final DBPoolingException e) {
            throw new MessagingException(e);
        } catch (final SQLException e) {
            throw MessagingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            final int genericConfId;
            {
                ResultSet rs = null;
                try {
                    stmt = wc.prepareStatement(SQL_SELECT);
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, session.getUserId());
                    stmt.setInt(pos, account.getId());
                    rs = stmt.executeQuery();
                    if (!rs.next()) {
                        throw MessagingExceptionCodes.ACCOUNT_NOT_FOUND.create(
                            Integer.valueOf(account.getId()),
                            Integer.valueOf(session.getUserId()),
                            Integer.valueOf(contextId));
                    }
                    genericConfId = rs.getInt(1);
                } finally {
                    DBUtils.closeSQLStuff(rs);
                }
            }
            DBUtils.closeSQLStuff(stmt);
            /*
             * Update account configuration using generic conf
             */
            {
                final Map<String, Object> configuration = account.getConfiguration();
                if (null != configuration) {
                    final GenericConfigurationStorageService genericConfStorageService =
                        getService(GenericConfigurationStorageService.class);
                    genericConfStorageService.update(wc, getContext(session), genericConfId, configuration);
                }
            }
            /*
             * Update account data
             */
            final String displayName = account.getDisplayName();
            if (null != displayName) {
                stmt = wc.prepareStatement(SQL_UPDATE);
                int pos = 1;
                stmt.setString(pos++, displayName);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, session.getUserId());
                stmt.setInt(pos, account.getId());
                stmt.executeUpdate();
            }
            wc.commit(); // COMMIT
        } catch (final GenericConfigStorageException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw new MessagingException(e);
        } catch (final SQLException e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MessagingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(wc); // ROLL-BACK
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(wc);
            databaseService.backWritable(contextId, wc);
        }
    }

    private static <S> S getService(final Class<? extends S> clazz) throws MessagingException {
        try {
            return MessagingGenericServiceRegistry.getServiceRegistry().getService(clazz, true);
        } catch (final ServiceException e) {
            throw new MessagingException(e);
        }
    }

    private static Context getContext(final Session session) throws MessagingException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        try {
            return getService(ContextService.class).getContext(session.getContextId());
        } catch (final ContextException e) {
            throw new MessagingException(e);
        }
    }

}
