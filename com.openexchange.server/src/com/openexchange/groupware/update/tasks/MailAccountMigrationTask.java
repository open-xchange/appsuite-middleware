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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.RdbUserStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link MailAccountMigrationTask} - Migrates mail account data kept in user table to mail account table.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountMigrationTask extends UpdateTaskAdapter {

    public MailAccountMigrationTask() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 40;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.HIGH.priority;
    }

    @Override
    public String[] getDependencies() {
        return new String[] { MailAccountCreateTablesTask.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int contextId = params.getContextId();
        final Map<Integer, List<Integer>> m = getAllUsers(contextId);
        final ProgressState state = params.getProgressState();
        state.setTotal(m.size());
        for (final Iterator<Map.Entry<Integer, List<Integer>>> it = m.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<Integer, List<Integer>> me = it.next();
            final int currentContextId = me.getKey().intValue();
            try {
                iterateUsersPerContext(me.getValue(), currentContextId);
            } catch (final OXException e) {
                final StringBuilder sb = new StringBuilder(128);
                sb.append("MailAccountMigrationTask experienced an error while migrating mail accounts for users in context ");
                sb.append(currentContextId);
                sb.append(":\n");
                sb.append(e.getMessage());
                final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailAccountMigrationTask.class));
                LOG.error(sb.toString(), e);
            }
            state.incrementState();
        }
    }

    private static Map<Integer, List<Integer>> getAllUsers(final int contextId) throws OXException {
        final Connection writeCon = Database.get(contextId, true);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("SELECT cid, id FROM user");
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyMap();
            }
            final Map<Integer, List<Integer>> m = new HashMap<Integer, List<Integer>>();
            do {
                final Integer cid = Integer.valueOf(rs.getInt(1));
                final Integer user = Integer.valueOf(rs.getInt(2));
                final List<Integer> l;
                if (!m.containsKey(cid)) {
                    l = new ArrayList<Integer>();
                    m.put(cid, l);
                } else {
                    l = m.get(cid);
                }
                l.add(user);
            } while (rs.next());
            return m;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(contextId, true, writeCon);
        }
    }

    private static boolean existsPrimaryMailAccount(final int userId, final int contextId) throws OXException {
        final Connection writeCon = Database.get(contextId, true);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = writeCon.prepareStatement("SELECT id FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, MailAccount.DEFAULT_ID);
            stmt.setInt(3, userId);
            result = stmt.executeQuery();
            return result.next();
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            Database.back(contextId, true, writeCon);
        }
    }

    private static void iterateUsersPerContext(final List<Integer> users, final int contextId) throws OXException {
        final Context ctx = new ContextImpl(contextId);
        final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailAccountMigrationTask.class));
        // First check (and possibly insert) a sequence for specified context
        checkAndInsertMailAccountSequence(ctx);
        final StringBuilder sb = new StringBuilder(256);
        for (final Integer userId : users) {
            // Check for default account
            if (existsPrimaryMailAccount(userId.intValue(), contextId)) {
                if (LOG.isInfoEnabled()) {
                    sb.setLength(0);
                    LOG.info(sb.append("Default mail account already exists for user ").append(userId).append(" in context ").append(
                        ctx.getContextId()));
                }
                continue;
            }
            // Default account does not exist
            if (LOG.isTraceEnabled()) {
                sb.setLength(0);
                LOG.trace(sb.append("Creating default mail account for user ").append(userId).append(" in context ").append(
                    ctx.getContextId()));
            }
            // Create default account
            final User user = loadUser(ctx, userId.intValue());
            final UserSettingMail usm = loadUserSettingMail(ctx, userId.intValue());
            try {
                handleUser(user, getNameProvderFromUSM(usm), ctx, sb, LOG);
            } catch (final OXException e) {
                LOG.error("Default mail account for user " + user.getId() + " in context " + contextId + " could not be created", e);
            }
        }
    }

    private static User loadUser(final Context ctx, final int userId) throws OXException, OXException {
        final Connection con = Database.get(ctx, true);
        try {
            final UserStorage userStorage = new RdbUserStorage();
            return userStorage.getUser(ctx, userId, con);
        } finally {
            Database.back(ctx, true, con);
        }
    }

    private static UserSettingMail loadUserSettingMail(final Context ctx, final int userId) throws OXException {
        final Connection con = Database.get(ctx, true);
        try {
            return UserSettingMailStorage.getInstance().getUserSettingMail(userId, ctx, con);
        } finally {
            Database.back(ctx, true, con);
        }
    }

    private static void handleUser(final User user, final FolderNameProvider folderNameProvdider, final Context ctx, final StringBuilder sb, final org.apache.commons.logging.Log LOG) throws OXException, OXException {
        /*
         * Insert
         */
        final MailAccountDescription account = createAccountDescription(user, folderNameProvdider);
        insertDefaultMailAccount(account, user.getId(), ctx);
        if (LOG.isInfoEnabled()) {
            sb.setLength(0);
            LOG.info(sb.append("Created default mail account for user ").append(user.getId()).append(" in context ").append(
                ctx.getContextId()));
        }
    }

    private static MailAccountDescription createAccountDescription(final User user, final FolderNameProvider folderNameProvdider) throws OXException {
        final MailAccountDescription account = new MailAccountDescription();
        account.setDefaultFlag(true);
        account.setConfirmedHam(prepareNonNullString(folderNameProvdider.getConfirmedHam()));
        account.setConfirmedSpam(prepareNonNullString(folderNameProvdider.getConfirmedSpam()));
        account.setDrafts(prepareNonNullString(folderNameProvdider.getDrafts()));
        account.setId(MailAccount.DEFAULT_ID);
        account.setLogin(prepareNonNullString(user.getImapLogin()));
        account.parseMailServerURL(prepareNonNullString(user.getImapServer()));
        account.setName(MailFolder.DEFAULT_FOLDER_NAME);
        account.setPassword(null);
        account.setPrimaryAddress(prepareNonNullString(user.getMail()));
        account.setSent(prepareNonNullString(folderNameProvdider.getSent()));
        account.setSpam(prepareNonNullString(folderNameProvdider.getSpam()));
        account.setSpamHandler(SpamHandler.SPAM_HANDLER_FALLBACK); // TODO: Obtain spam handler
        account.parseTransportServerURL(prepareNonNullString(user.getSmtpServer()));
        account.setTrash(prepareNonNullString(folderNameProvdider.getTrash()));
        return account;
    }

    private static String prepareNonNullString(final String string) {
        return null == string ? "" : string;
    }

    private static interface FolderNameProvider {

        String getTrash();

        String getSent();

        String getDrafts();

        String getSpam();

        String getConfirmedSpam();

        String getConfirmedHam();
    }

    private static final FolderNameProvider DEFAULT_NAME_PROVIDER = new FolderNameProvider() {

        @Override
        public String getConfirmedHam() {
            return MailStrings.CONFIRMED_HAM;
        }

        @Override
        public String getConfirmedSpam() {
            return MailStrings.CONFIRMED_SPAM;
        }

        @Override
        public String getDrafts() {
            return MailStrings.DRAFTS;
        }

        @Override
        public String getSent() {
            return MailStrings.SENT;
        }

        @Override
        public String getSpam() {
            return MailStrings.SPAM;
        }

        @Override
        public String getTrash() {
            return MailStrings.TRASH;
        }
    };

    private static FolderNameProvider getNameProvderFromUSM(final UserSettingMail usm) {
        if (null == usm) {
            return DEFAULT_NAME_PROVIDER;
        }
        return new FolderNameProvider() {

            @Override
            public String getConfirmedHam() {
                return usm.getConfirmedHam();
            }

            @Override
            public String getConfirmedSpam() {
                return usm.getConfirmedSpam();
            }

            @Override
            public String getDrafts() {
                return usm.getStdDraftsName();
            }

            @Override
            public String getSent() {
                return usm.getStdSentName();
            }

            @Override
            public String getSpam() {
                return usm.getStdSpamName();
            }

            @Override
            public String getTrash() {
                return usm.getStdTrashName();
            }
        };
    }

    private static void insertDefaultMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx) throws OXException {
        final int cid = ctx.getContextId();
        final int id = MailAccount.DEFAULT_ID;
        Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        try {
            stmt =
                con.prepareStatement("INSERT INTO user_mail_account (cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            int pos = 1;
            // cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam,
            // confirmed_ham, spam_handler
            stmt.setLong(pos++, cid);
            stmt.setLong(pos++, id);
            stmt.setLong(pos++, user);
            stmt.setString(pos++, mailAccount.getName());
            stmt.setString(pos++, mailAccount.generateMailServerURL());
            stmt.setString(pos++, mailAccount.getLogin());
            stmt.setNull(pos++, Types.VARCHAR);
            stmt.setString(pos++, mailAccount.getPrimaryAddress());
            stmt.setInt(pos++, 1);
            stmt.setString(pos++, mailAccount.getTrash());
            stmt.setString(pos++, mailAccount.getSent());
            stmt.setString(pos++, mailAccount.getDrafts());
            stmt.setString(pos++, mailAccount.getSpam());
            stmt.setString(pos++, mailAccount.getConfirmedSpam());
            stmt.setString(pos++, mailAccount.getConfirmedHam());
            final String sh = mailAccount.getSpamHandler();
            if (null == sh) {
                stmt.setNull(pos++, Types.VARCHAR);
            } else {
                stmt.setString(pos++, sh);
            }
            stmt.setString(pos++, "");
            stmt.setString(pos++, "");
            stmt.setString(pos++, "");
            stmt.setString(pos++, "");
            stmt.setString(pos++, "");
            stmt.setString(pos++, "");
            stmt.executeUpdate();
            stmt.close();
            // cid, id, user, name, url, login, password, send_addr, default_flag
            stmt =
                con.prepareStatement("INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag) VALUES (?,?,?,?,?,?,?,?,?)");
            pos = 1;
            stmt.setLong(pos++, cid);
            stmt.setLong(pos++, id);
            stmt.setLong(pos++, user);
            stmt.setString(pos++, mailAccount.getName());
            stmt.setString(pos++, mailAccount.generateTransportServerURL());
            stmt.setString(pos++, mailAccount.getLogin());
            stmt.setNull(pos++, Types.VARCHAR);
            stmt.setString(pos++, mailAccount.getPrimaryAddress());
            stmt.setInt(pos++, 1);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            Database.back(cid, true, con);
        }
    }

    private static void checkAndInsertMailAccountSequence(final Context ctx) throws OXException {
        final int cid = ctx.getContextId();
        final Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT * FROM sequence_mail_service WHERE cid = ?");
            stmt.setLong(1, cid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                // Sequence table already contains an entry for specified context
                return;
            }
            rs.close();
            stmt.close();
            stmt = con.prepareStatement("INSERT INTO sequence_mail_service (cid, id) VALUES (?, ?)");
            stmt.setLong(1, cid);
            stmt.setLong(2, 0);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, true, con);
        }
    }
}
