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
import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.RdbUserStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.ServiceException;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link MailAccountMigrationTask} - Migrates mail account data kept in user table to mail account table.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public final class MailAccountMigrationTask implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailAccountMigrationTask.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(MailAccountMigrationTask.class);

    public MailAccountMigrationTask() {
        super();
    }

    public int addedWithVersion() {
        return 34;
    }

    public int getPriority() {
        return UpdateTaskPriority.HIGH.priority;
    }

    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
        final Map<Integer, List<Integer>> m = getAllUsers(contextId);

        for (final Iterator<Map.Entry<Integer, List<Integer>>> it = m.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<Integer, List<Integer>> me = it.next();
            iterateUsersPerContext(me.getValue(), me.getKey().intValue());
        }
    }

    private static Map<Integer, List<Integer>> getAllUsers(final int contextId) throws UpdateException {
        final Connection writeCon;
        try {
            writeCon = Database.get(contextId, false);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
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
            throw createSQLError(e);
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(contextId, false, writeCon);
        }
    }

    private static void iterateUsersPerContext(final List<Integer> users, final int contextId) throws UpdateException {
        final Context ctx = new ContextImpl(contextId);
        // First check (and possibly insert) a sequence for specified context
        checkAndInsertMailAccountSequence(ctx);
        // Proceed with user data migration to new mail account tables
        try {
            final StringBuilder sb = new StringBuilder(256);
            for (final Integer userId : users) {
                final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                    MailAccountStorageService.class,
                    true);
                // Check for default account
                try {
                    storageService.getDefaultMailAccount(userId.intValue(), ctx.getContextId());
                    if (LOG.isInfoEnabled()) {
                        sb.setLength(0);
                        LOG.info(sb.append("Default mail account already exists for user ").append(userId).append(" in context ").append(
                            ctx.getContextId()));
                    }
                    continue;
                } catch (final MailAccountException e) {
                    // Expected exception since default account should not exist
                    if (LOG.isTraceEnabled()) {
                        sb.setLength(0);
                        LOG.trace(sb.append("Creating default mail account for user ").append(userId).append(" in context ").append(
                            ctx.getContextId()));
                    }
                }
                // Create default account
                final User user = new RdbUserStorage().getUser(userId.intValue(), ctx);
                final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(userId.intValue(), ctx);
                try {
                    handleUser(user, getNameProvderFromUSM(usm), ctx, sb);
                } catch (final UpdateException e) {
                    LOG.error(
                        "Default mail account for user " + user.getDisplayName() + " in context " + contextId + " could not be created",
                        e);
                }
            }
        } catch (final LdapException e) {
            throw new UpdateException(e);
        } catch (final ServiceException e) {
            throw new UpdateException(e);
        }
    }

    private static void handleUser(final User user, final FolderNameProvider folderNameProvdider, final Context ctx, final StringBuilder sb) throws UpdateException {
        /*
         * Insert
         */
        final MailAccountDescription account = createAccountDescription(user, folderNameProvdider);
        insertDefaultMailAccount(account, user.getId(), ctx);
        if (LOG.isInfoEnabled()) {
            sb.setLength(0);
            LOG.info(sb.append("Created default mail account for user ").append(user.getDisplayName()).append(" in context ").append(
                ctx.getContextId()));
        }
    }

    private static MailAccountDescription createAccountDescription(final User user, final FolderNameProvider folderNameProvdider) {
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

    @OXThrowsMultiple(
        category = { Category.CODE_ERROR },
        desc = { "" },
        exceptionId = { 1 },
        msg = { "A SQL error occurred while performing task MailAccountCreateTablesTask: %1$s." }
    )
    private static UpdateException createSQLError(final SQLException e) {
        return EXCEPTION.create(1, e, e.getMessage());
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

        public String getConfirmedHam() {
            return UserSettingMail.STD_CONFIRMED_HAM;
        }

        public String getConfirmedSpam() {
            return UserSettingMail.STD_CONFIRMED_SPAM;
        }

        public String getDrafts() {
            return UserSettingMail.STD_DRAFTS;
        }

        public String getSent() {
            return UserSettingMail.STD_SENT;
        }

        public String getSpam() {
            return UserSettingMail.STD_SPAM;
        }

        public String getTrash() {
            return UserSettingMail.STD_TRASH;
        }
    };

    private static FolderNameProvider getNameProvderFromUSM(final UserSettingMail usm) {
        if (null == usm) {
            return DEFAULT_NAME_PROVIDER;
        }
        return new FolderNameProvider() {

            public String getConfirmedHam() {
                return usm.getConfirmedHam();
            }

            public String getConfirmedSpam() {
                return usm.getConfirmedSpam();
            }

            public String getDrafts() {
                return usm.getStdDraftsName();
            }

            public String getSent() {
                return usm.getStdSentName();
            }

            public String getSpam() {
                return usm.getStdSpamName();
            }

            public String getTrash() {
                return usm.getStdTrashName();
            }
        };
    }

    private static void insertDefaultMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx) throws UpdateException {
        final int cid = ctx.getContextId();
        final int id = MailAccount.DEFAULT_ID;
        Connection con = null;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO user_mail_account (cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
            stmt = con.prepareStatement("INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag) VALUES (?,?,?,?,?,?,?,?,?)");
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
            throw createSQLError(e);
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private static void checkAndInsertMailAccountSequence(final Context ctx) throws UpdateException {
        final int cid = ctx.getContextId();
        Connection con = null;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
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
            throw createSQLError(e);
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, true, con);
        }
    }

}
