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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.utils.ProviderUtility.toSocketAddr;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.TIntArrayList;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.DBPoolingException;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.mail.MailException;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountExceptionFactory;
import com.openexchange.mailaccount.MailAccountExceptionMessages;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.mailaccount.json.fields.GetSwitch;
import com.openexchange.mailaccount.json.fields.MailAccountGetSwitch;
import com.openexchange.mailaccount.json.fields.SetSwitch;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.Collections.SmartIntArray;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbMailAccountStorage} - The relational database implementation of mail account storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbMailAccountStorage implements MailAccountStorageService {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(RdbMailAccountStorage.class);

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type VARCHAR.
     */
    private static final int TYPE_VARCHAR = Types.VARCHAR;

    private static final String SELECT_MAIL_ACCOUNT =
        "SELECT name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, unified_inbox, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname, personal FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_TRANSPORT_ACCOUNT =
        "SELECT name, url, login, password, send_addr, default_flag, personal FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_MAIL_ACCOUNTS = "SELECT id FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id";

    private static final String SELECT_BY_LOGIN = "SELECT id, user FROM user_mail_account WHERE cid = ? AND login = ?";

    private static final String SELECT_BY_PRIMARY_ADDR = "SELECT id, user FROM user_mail_account WHERE cid = ? AND primary_addr = ?";

    private static final String SELECT_ACCOUNT_BY_PRIMARY_ADDR =
        "SELECT id FROM user_mail_account WHERE cid = ? AND primary_addr = ? AND user = ?";

    private static final String DELETE_MAIL_ACCOUNT = "DELETE FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String DELETE_TRANSPORT_ACCOUNT = "DELETE FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_MAIL_ACCOUNT =
        "UPDATE user_mail_account SET name = ?, url = ?, login = ?, password = ?, primary_addr = ?, spam_handler = ?, trash = ?, sent = ?, drafts = ?, spam = ?, confirmed_spam = ?, confirmed_ham = ?, unified_inbox = ?, trash_fullname = ?, sent_fullname = ?, drafts_fullname = ?, spam_fullname = ?, confirmed_spam_fullname = ?, confirmed_ham_fullname = ?, personal = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String INSERT_MAIL_ACCOUNT =
        "INSERT INTO user_mail_account (cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, unified_inbox, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname, personal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_TRANSPORT_ACCOUNT =
        "UPDATE user_transport_account SET name = ?, url = ?, login = ?, password = ?, send_addr = ?, personal = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String INSERT_TRANSPORT_ACCOUNT =
        "INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag, personal) VALUES (?,?,?,?,?,?,?,?,?,?)";

    private static final String UPDATE_UNIFIED_INBOX_FLAG =
        "UPDATE user_mail_account SET unified_inbox = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_PERSONAL1 = "UPDATE user_mail_account SET personal = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_PERSONAL2 = "UPDATE user_transport_account SET personal = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_PASSWORD1 = "SELECT id, password FROM user_mail_account WHERE cid = ? AND user = ?";

    private static final String SELECT_PASSWORD2 = "SELECT id, password FROM user_transport_account WHERE cid = ? AND user = ?";

    private static final String UPDATE_PASSWORD1 = "UPDATE user_mail_account SET password = ?  WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_PASSWORD2 = "UPDATE user_transport_account SET password = ?  WHERE cid = ? AND id = ? AND user = ?";

    private static final String PARAM_POP3_STORAGE_FOLDERS = "com.openexchange.mailaccount.pop3Folders";

    private static Object getSessionLock(final Session session) {
        final Object lock = session.getParameter(Session.PARAM_LOCK);
        return null == lock ? session : lock;
    }

    private static void dropPOP3StorageFolders(final int userId, final int contextId) {
        final SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null != service) {
            for (final Session session : service.getSessions(userId, contextId)) {
                synchronized (getSessionLock(session)) {
                    session.setParameter(PARAM_POP3_STORAGE_FOLDERS, null);
                }
            }
        }
    }
    
    /**
     * Gets the POP3 storage folders for specified session.
     * 
     * @param session The session
     * @return The POP3 storage folder full names
     * @throws MailException If an error occurs
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getPOP3StorageFolders(final Session session) throws MailException {
        Set<String> set = (Set<String>) session.getParameter(PARAM_POP3_STORAGE_FOLDERS);
        if (null == set) {
            synchronized (getSessionLock(session)) {
                set = (Set<String>) session.getParameter(PARAM_POP3_STORAGE_FOLDERS);
                if (null == set) {
                    set = getPOP3StorageFolders0(session);
                    session.setParameter(PARAM_POP3_STORAGE_FOLDERS, set);
                }
            }
        }
        return set;
    }

    private static Set<String> getPOP3StorageFolders0(final Session session) throws MailException {
        final int contextId = session.getContextId();
        final Connection con;
        try {
            con = Database.get(contextId, false);
        } catch (final DBPoolingException e) {
            throw new MailException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT value FROM user_mail_account_properties WHERE cid = ? AND user = ? AND name = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, session.getUserId());
            stmt.setString(3, "pop3.path");
            rs = stmt.executeQuery();
            final Set<String> set = new HashSet<String>(4);
            while (rs.next()) {
                set.add(rs.getString(1));
            }
            return set;
        } catch (final SQLException e) {
            throw new MailException(MailException.Code.UNEXPECTED_ERROR, e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }

    private static void fillMailAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            fillMailAccount(mailAccount, id, user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    private static void fillMailAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid, final Connection con) throws MailAccountException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_MAIL_ACCOUNT);
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NOT_FOUND, I(id), I(user), I(cid));
            }
            mailAccount.setId(id);
            mailAccount.setLogin(result.getString(3));
            mailAccount.parseMailServerURL(result.getString(2));
            mailAccount.setName(result.getString(1));
            final String pw = result.getString(4);
            if (result.wasNull()) {
                mailAccount.setPassword(null);
            } else {
                mailAccount.setPassword(pw);
            }
            mailAccount.setPrimaryAddress(result.getString(5));
            mailAccount.setTrash(getOptionalString(result.getString(7)));
            mailAccount.setSent(getOptionalString(result.getString(8)));
            mailAccount.setDrafts(getOptionalString(result.getString(9)));
            mailAccount.setSpam(getOptionalString(result.getString(10)));
            mailAccount.setConfirmedSpam(getOptionalString(result.getString(11)));
            mailAccount.setConfirmedHam(getOptionalString(result.getString(12)));
            mailAccount.setSpamHandler(result.getString(13));
            mailAccount.setUnifiedINBOXEnabled(result.getInt(14) > 0);
            mailAccount.setTrashFullname(getOptionalString(result.getString(15)));
            mailAccount.setSentFullname(getOptionalString(result.getString(16)));
            mailAccount.setDraftsFullname(getOptionalString(result.getString(17)));
            mailAccount.setSpamFullname(getOptionalString(result.getString(18)));
            mailAccount.setConfirmedSpamFullname(getOptionalString(result.getString(19)));
            mailAccount.setConfirmedHamFullname(getOptionalString(result.getString(20)));
            final String pers = result.getString(21);
            if (result.wasNull()) {
                mailAccount.setPersonal(null);
            } else {
                mailAccount.setPersonal(pers);
            }
            mailAccount.setUserId(user);
            /*
             * Fill properties
             */
            fillProperties(mailAccount, cid, user, id, con);
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private static void fillTransportAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            fillTransportAccount(mailAccount, id, user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    private static void fillTransportAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid, final Connection con) throws MailAccountException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_TRANSPORT_ACCOUNT);
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            result = stmt.executeQuery();
            if (result.next()) {
                mailAccount.parseTransportServerURL(result.getString(2));
                {
                    final String transportLogin = result.getString(3);
                    if (result.wasNull()) {
                        mailAccount.setTransportLogin(null);
                    } else {
                        mailAccount.setTransportLogin(transportLogin);
                    }
                }
                {
                    final String transportPassword = result.getString(4);
                    if (result.wasNull()) {
                        mailAccount.setTransportPassword(null);
                    } else {
                        mailAccount.setTransportPassword(transportPassword);
                    }
                }
                final String pers = result.getString(7);
                if (!result.wasNull()) {
                    mailAccount.setPersonal(pers);
                }
            } else {
                // throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NOT_FOUND, I(id), I(user), I(cid));
                mailAccount.setTransportServer(null);
            }
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private static void fillProperties(final AbstractMailAccount mailAccount, final int cid, final int user, final int id, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT name, value FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos, id);
            rs = stmt.executeQuery();
            final Map<String, String> properties = new HashMap<String, String>();
            while (rs.next()) {
                final String name = rs.getString(1);
                if (!rs.wasNull()) {
                    final String value = rs.getString(2);
                    if (!rs.wasNull()) {
                        properties.put(name, value);
                    }
                }
            }
            mailAccount.setProperties(properties);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Initializes a new {@link RdbMailAccountStorage}.
     */
    RdbMailAccountStorage() {
        super();
    }

    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid) throws MailAccountException {
        deleteMailAccount(id, properties, user, cid, false);
    }

    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid, final boolean deletePrimary) throws MailAccountException {
        if (!deletePrimary && MailAccount.DEFAULT_ID == id) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NO_DEFAULT_DELETE, I(user), I(cid));
        }
        Connection con = null;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            con.setAutoCommit(false);
            deleteMailAccount(id, properties, user, cid, deletePrimary, con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } catch (final MailAccountException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionMessages.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(cid, true, con);
        }
    }

    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid, final boolean deletePrimary, final Connection con) throws MailAccountException {
        if (!deletePrimary && MailAccount.DEFAULT_ID == id) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NO_DEFAULT_DELETE, I(user), I(cid));
        }
        PreparedStatement stmt = null;
        try {
            final DeleteListenerRegistry registry = DeleteListenerRegistry.getInstance();
            registry.triggerOnBeforeDeletion(id, properties, user, cid, con);
            // First delete properties
            deleteProperties(cid, user, id, con);
            deleteTransportProperties(cid, user, id, con);
            // Then delete account data
            stmt = con.prepareStatement(DELETE_MAIL_ACCOUNT);
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            stmt = con.prepareStatement(DELETE_TRANSPORT_ACCOUNT);
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            stmt.executeUpdate();
            registry.triggerOnAfterDeletion(id, properties, user, cid, con);
            dropPOP3StorageFolders(user, cid);
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public MailAccount getDefaultMailAccount(final int user, final int cid, final Connection con) throws MailAccountException {
        return getMailAccount(MailAccount.DEFAULT_ID, user, cid, con);
    }

    public MailAccount getDefaultMailAccount(final int user, final int cid) throws MailAccountException {
        return getMailAccount(MailAccount.DEFAULT_ID, user, cid);
    }

    public MailAccount getMailAccount(final int id, final int user, final int cid, final Connection con) throws MailAccountException {
        final AbstractMailAccount retval = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount();
        fillMailAccount(retval, id, user, cid, con);
        fillTransportAccount(retval, id, user, cid, con);
        return retval;
    }

    public MailAccount getMailAccount(final int id, final int user, final int cid) throws MailAccountException {
        try {
            final Connection rcon;
            try {
                rcon = Database.get(cid, false);
            } catch (final DBPoolingException e) {
                throw new MailAccountException(e);
            }
            try {
                return getMailAccount(id, user, cid, rcon);
            } finally {
                Database.back(cid, false, rcon);
            }
        } catch (final MailAccountException mae) {
            if (MailAccountExceptionMessages.NOT_FOUND.getDetailNumber() != mae.getDetailNumber()) {
                throw mae;
            }
            /*
             * Read-only failed, retry with read-write connection
             */
            final Connection wcon;
            try {
                wcon = Database.get(cid, true);
            } catch (final DBPoolingException dbe) {
                throw new MailAccountException(dbe);
            }
            try {
                return getMailAccount(id, user, cid, wcon);
            } finally {
                Database.back(cid, true, wcon);
            }
        }
    }

    public MailAccount[] getUserMailAccounts(final int user, final int cid) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            return getUserMailAccounts(user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    public MailAccount[] getUserMailAccounts(final int user, final int cid, final Connection con) throws MailAccountException {
        final int[] ids = getUserMailAccountIDs(user, cid, con);
        final MailAccount[] retval = new MailAccount[ids.length];
        for (int i = 0; i < ids.length; i++) {
            retval[i] = getMailAccount(ids[i], user, cid, con);
        }
        return retval;
    }

    int[] getUserMailAccountIDs(final int user, final int cid) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            return getUserMailAccountIDs(user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    int[] getUserMailAccountIDs(final int user, final int cid, final Connection con) throws MailAccountException {
        final int[] ids;
        {
            PreparedStatement stmt = null;
            ResultSet result = null;
            final SmartIntArray sia = new SmartIntArray(8);
            try {
                stmt = con.prepareStatement(SELECT_MAIL_ACCOUNTS);
                stmt.setLong(1, cid);
                stmt.setLong(2, user);
                result = stmt.executeQuery();
                if (!result.next()) {
                    return new int[0];
                }
                do {
                    sia.append(result.getInt(1));
                } while (result.next());
            } catch (final SQLException e) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
            }
            ids = sia.toArray();
        }
        return ids;
    }

    public MailAccount[] resolveLogin(final String login, final int cid) throws MailAccountException {
        final int[][] idsAndUsers = resolveLogin2IDs(login, cid);
        final MailAccount[] retval = new MailAccount[idsAndUsers.length];
        for (int i = 0; i < idsAndUsers.length; i++) {
            final int[] idAndUser = idsAndUsers[i];
            retval[i] = getMailAccount(idAndUser[0], idAndUser[1], cid);
        }
        return retval;
    }

    int[][] resolveLogin2IDs(final String login, final int cid) throws MailAccountException {
        final int[] ids;
        final int[] users;
        {
            Connection con = null;
            try {
                con = Database.get(cid, false);
            } catch (final DBPoolingException e) {
                throw new MailAccountException(e);
            }
            PreparedStatement stmt = null;
            ResultSet result = null;
            final SmartIntArray idsArr = new SmartIntArray(8);
            final SmartIntArray usersArr = new SmartIntArray(8);
            try {
                stmt = con.prepareStatement(SELECT_BY_LOGIN);
                stmt.setLong(1, cid);
                stmt.setString(2, login);
                result = stmt.executeQuery();
                if (!result.next()) {
                    return new int[0][];
                }
                do {
                    idsArr.append(result.getInt(1));
                    usersArr.append(result.getInt(2));
                } while (result.next());
            } catch (final SQLException e) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
                Database.back(cid, false, con);
            }
            ids = idsArr.toArray();
            users = usersArr.toArray();
        }
        final int[][] retval = new int[ids.length][];
        for (int i = 0; i < ids.length; i++) {
            retval[i] = new int[] { ids[i], users[i] };
        }
        return retval;
    }

    public MailAccount[] resolveLogin(final String login, final InetSocketAddress server, final int cid) throws MailAccountException {
        final int[][] idsAndUsers = resolveLogin2IDs(login, cid);
        final List<MailAccount> l = new ArrayList<MailAccount>(idsAndUsers.length);
        for (final int[] idAndUser : idsAndUsers) {
            final MailAccount candidate = getMailAccount(idAndUser[0], idAndUser[1], cid);
            if (server.equals(toSocketAddr(candidate.generateMailServerURL(), 143))) {
                l.add(candidate);
            }
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    public MailAccount[] resolvePrimaryAddr(final String primaryAddress, final int cid) throws MailAccountException {
        final int[][] idsAndUsers = resolvePrimaryAddr2IDs(primaryAddress, cid);
        final List<MailAccount> l = new ArrayList<MailAccount>(idsAndUsers.length);
        for (final int[] idAndUser : idsAndUsers) {
            final MailAccount candidate = getMailAccount(idAndUser[0], idAndUser[1], cid);
            l.add(candidate);
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    int[][] resolvePrimaryAddr2IDs(final String primaryAddress, final int cid) throws MailAccountException {
        final int[] ids;
        final int[] users;
        {
            Connection con = null;
            try {
                con = Database.get(cid, false);
            } catch (final DBPoolingException e) {
                throw new MailAccountException(e);
            }
            PreparedStatement stmt = null;
            ResultSet result = null;
            final SmartIntArray idsArr = new SmartIntArray(8);
            final SmartIntArray usersArr = new SmartIntArray(8);
            try {
                stmt = con.prepareStatement(SELECT_BY_PRIMARY_ADDR);
                stmt.setLong(1, cid);
                stmt.setString(2, primaryAddress);
                result = stmt.executeQuery();
                if (!result.next()) {
                    return new int[0][];
                }
                do {
                    idsArr.append(result.getInt(1));
                    usersArr.append(result.getInt(2));
                } while (result.next());
            } catch (final SQLException e) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
                Database.back(cid, false, con);
            }
            ids = idsArr.toArray();
            users = usersArr.toArray();
        }
        final int[][] idsAndUsers = new int[ids.length][];
        for (int i = 0; i < ids.length; i++) {
            idsAndUsers[i] = new int[] { ids[i], users[i] };
        }
        return idsAndUsers;
    }

    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final String sessionPassword) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            con.setAutoCommit(false);
            updateMailAccount(mailAccount, attributes, user, cid, sessionPassword, con, false);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } catch (final MailAccountException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionMessages.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(cid, true, con);
        }
    }

    /**
     * Contains attributes which denote an account's default folders.
     */
    private static final EnumSet<Attribute> DEFAULT = EnumSet.of(
        Attribute.CONFIRMED_HAM_FULLNAME_LITERAL,
        Attribute.CONFIRMED_HAM_LITERAL,
        Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL,
        Attribute.CONFIRMED_SPAM_LITERAL,
        Attribute.DRAFTS_FULLNAME_LITERAL,
        Attribute.DRAFTS_LITERAL,
        Attribute.SENT_FULLNAME_LITERAL,
        Attribute.SENT_LITERAL,
        Attribute.SPAM_FULLNAME_LITERAL,
        Attribute.SPAM_LITERAL,
        Attribute.TRASH_FULLNAME_LITERAL,
        Attribute.TRASH_LITERAL);

    /**
     * Contains attributes which denote the fullnames of an account's default folders.
     */
    private static final EnumSet<Attribute> DEFAULT_FULL_NAMES = EnumSet.of(
        Attribute.CONFIRMED_HAM_FULLNAME_LITERAL,
        Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL,
        Attribute.DRAFTS_FULLNAME_LITERAL,
        Attribute.SENT_FULLNAME_LITERAL,
        Attribute.SPAM_FULLNAME_LITERAL,
        Attribute.TRASH_FULLNAME_LITERAL);

    /**
     * Contains attributes which are allowed to be edited for primary mail account.
     */
    private static final EnumSet<Attribute> PRIMARY_EDITABLE = EnumSet.of(
        Attribute.UNIFIED_INBOX_ENABLED_LITERAL,
        Attribute.PERSONAL_LITERAL);

    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final String sessionPassword, final Connection con, final boolean changePrimary) throws MailAccountException {
        final boolean rename;
        if (attributes.contains(Attribute.NAME_LITERAL)) {
            // Check name
            final String name = mailAccount.getName();
            if (!isValid(name)) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.INVALID_NAME, name);
            }
            // Check for rename operation
            rename = !name.equals(getMailAccount(mailAccount.getId(), user, cid, con).getName());
        } else {
            rename = false;
        }
        if (!changePrimary && (mailAccount.isDefaultFlag() || MailAccount.DEFAULT_ID == mailAccount.getId())) {
            final boolean containsUnifiedInbox = attributes.contains(Attribute.UNIFIED_INBOX_ENABLED_LITERAL);
            final boolean containsPersonal = attributes.contains(Attribute.PERSONAL_LITERAL);
            if (!containsUnifiedInbox && !containsPersonal) {
                /*
                 * Another attribute must not be changed
                 */
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NO_DEFAULT_UPDATE, I(user), I(cid));
            }
            /*
             * Ensure only allowed attributes should really be changed
             */
            final MailAccount storageVersion = getMailAccount(mailAccount.getId(), user, cid, con);
            /*
             * Initialize GET switches
             */
            final MailAccountGetSwitch storageGetSwitch = new MailAccountGetSwitch(storageVersion);
            final GetSwitch getSwitch = new GetSwitch(mailAccount);
            /*
             * Iterate attributes and compare their values except the one for Attribute.UNIFIED_INBOX_ENABLED_LITERAL and
             * Attribute.PERSONAL_LITERAL
             */
            for (final Attribute attribute : attributes) {
                /*
                 * Check for an attribute different from Attribute.UNIFIED_INBOX_ENABLED_LITERAL and Attribute.PERSONAL_LITERAL
                 */
                if (!PRIMARY_EDITABLE.contains(attribute)) {
                    final Object storageValue = attribute.doSwitch(storageGetSwitch);
                    final Object newValue = attribute.doSwitch(getSwitch);
                    if (null != storageValue && !storageValue.equals(newValue)) {
                        /*
                         * Another attribute must not be changed
                         */
                        throw MailAccountExceptionFactory.getInstance().create(
                            MailAccountExceptionMessages.NO_DEFAULT_UPDATE,
                            I(user),
                            I(cid));
                    }
                }
            }
            if (containsUnifiedInbox) {
                /*
                 * OK, update UNIFIED_INBOX_ENABLED flag.
                 */
                updateUnifiedINBOXEnabled(mailAccount.isUnifiedINBOXEnabled(), MailAccount.DEFAULT_ID, user, cid, con);
                /*
                 * Automatically check Unified INBOX existence
                 */
                if (mailAccount.isUnifiedINBOXEnabled()) {
                    final UnifiedINBOXManagement management = ServerServiceRegistry.getInstance().getService(UnifiedINBOXManagement.class);
                    if (null != management && !management.exists(user, cid, con)) {
                        management.createUnifiedINBOX(user, cid, con);
                    }
                }
            }
            if (containsPersonal) {
                /*
                 * OK, update PERSONAL string.
                 */
                updatePersonal(mailAccount.getPersonal(), MailAccount.DEFAULT_ID, user, cid, con);
            }
        } else {
            /*
             * Perform common update
             */
            PreparedStatement stmt = null;
            try {
                MailAccount storageVersion = null;
                if (prepareURL(attributes, Attribute.MAIL_URL_ATTRIBUTES, Attribute.MAIL_URL_LITERAL)) {
                    storageVersion = getMailAccount(mailAccount.getId(), user, cid, con);
                    final MailAccountGetSwitch getSwitch = new MailAccountGetSwitch(storageVersion);
                    final SetSwitch setSwitch = new SetSwitch(mailAccount);

                    for (final Attribute attribute : Attribute.MAIL_URL_ATTRIBUTES) {
                        if (!attributes.contains(attribute)) {
                            final Object value = attribute.doSwitch(getSwitch);
                            setSwitch.setValue(value);
                            attribute.doSwitch(setSwitch);
                        }
                    }
                }

                if (prepareURL(attributes, Attribute.TRANSPORT_URL_ATTRIBUTES, Attribute.TRANSPORT_URL_LITERAL)) {
                    if (storageVersion != null) {
                        storageVersion = getMailAccount(mailAccount.getId(), user, cid, con);
                    }
                    final MailAccountGetSwitch getSwitch = new MailAccountGetSwitch(storageVersion);
                    final SetSwitch setSwitch = new SetSwitch(mailAccount);

                    for (final Attribute attribute : Attribute.TRANSPORT_URL_ATTRIBUTES) {
                        if (!attributes.contains(attribute)) {
                            final Object value = attribute.doSwitch(getSwitch);
                            setSwitch.setValue(value);
                            attribute.doSwitch(setSwitch);
                        }
                    }
                }

                attributes.removeAll(Attribute.MAIL_URL_ATTRIBUTES);
                attributes.removeAll(Attribute.TRANSPORT_URL_ATTRIBUTES);

                String encryptedPassword = null; //

                List<Attribute> orderedAttributes = null;
                if (UpdateMailAccountBuilder.needsUpdate(attributes)) {
                    orderedAttributes = new ArrayList<Attribute>(attributes);

                    final UpdateMailAccountBuilder sqlBuilder = new UpdateMailAccountBuilder();
                    for (final Attribute attribute : orderedAttributes) {
                        attribute.doSwitch(sqlBuilder);
                    }

                    stmt = con.prepareStatement(sqlBuilder.getUpdateQuery());

                    final GetSwitch getter = new GetSwitch(mailAccount);
                    int pos = 1;
                    for (final Attribute attribute : orderedAttributes) {
                        if (!sqlBuilder.handles(attribute)) {
                            continue;
                        }
                        final Object value = attribute.doSwitch(getter);
                        if (Attribute.PASSWORD_LITERAL == attribute) {
                            try {
                                encryptedPassword = MailPasswordUtil.encrypt(mailAccount.getPassword(), sessionPassword);
                            } catch (final GeneralSecurityException e) {
                                throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(
                                    e,
                                    mailAccount.getLogin(),
                                    mailAccount.getMailServer(),
                                    Integer.valueOf(user),
                                    Integer.valueOf(cid));
                            }
                            setOptionalString(stmt, pos++, encryptedPassword);
                        } else if (Attribute.PERSONAL_LITERAL == attribute) {
                            final String personal = mailAccount.getPersonal();
                            if (isEmpty(personal)) {
                                stmt.setNull(pos++, TYPE_VARCHAR);
                            } else {
                                stmt.setString(pos++, personal);
                            }
                        } else if (DEFAULT.contains(attribute)) {
                            if (DEFAULT_FULL_NAMES.contains(attribute)) {
                                final String fullName = null == value ? "" : MailFolderUtility.prepareMailFolderParam((String) value).getFullname();
                                stmt.setString(pos++, fullName);
                            } else {
                                if (null == value) {
                                    stmt.setObject(pos++, "");
                                } else {
                                    stmt.setObject(pos++, value);
                                }
                            }
                        } else {
                            stmt.setObject(pos++, value);
                        }
                    }

                    stmt.setLong(pos++, cid);
                    stmt.setLong(pos++, mailAccount.getId());
                    stmt.setLong(pos++, user);

                    if (LOG.isDebugEnabled()) {
                        final String query = stmt.toString();
                        LOG.debug(new StringBuilder(query.length() + 32).append("Trying to perform SQL update query for attributes ").append(
                            orderedAttributes).append(" :\n").append(query.substring(query.indexOf(':') + 1)));
                    }

                    stmt.executeUpdate();
                    closeSQLStuff(stmt);

                }

                if (UpdateTransportAccountBuilder.needsUpdate(attributes)) {
                    if (orderedAttributes == null) {
                        orderedAttributes = new ArrayList<Attribute>(attributes);
                    }

                    final UpdateTransportAccountBuilder sqlBuilder = new UpdateTransportAccountBuilder();
                    for (final Attribute attribute : orderedAttributes) {
                        attribute.doSwitch(sqlBuilder);
                    }

                    stmt = con.prepareStatement(sqlBuilder.getUpdateQuery());

                    final GetSwitch getter = new GetSwitch(mailAccount);
                    int pos = 1;
                    for (final Attribute attribute : orderedAttributes) {
                        if (!sqlBuilder.handles(attribute)) {
                            continue;
                        }
                        final Object value = attribute.doSwitch(getter);
                        if (Attribute.TRANSPORT_PASSWORD_LITERAL == attribute) {
                            if (encryptedPassword == null) {
                                try {
                                    encryptedPassword = MailPasswordUtil.encrypt(mailAccount.getTransportPassword(), sessionPassword);
                                } catch (final GeneralSecurityException e) {
                                    throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(e, new Object[0]);
                                }
                            }
                            setOptionalString(stmt, pos++, encryptedPassword);
                        } else if (Attribute.PERSONAL_LITERAL == attribute) {
                            final String personal = mailAccount.getPersonal();
                            if (isEmpty(personal)) {
                                stmt.setNull(pos++, TYPE_VARCHAR);
                            } else {
                                stmt.setString(pos++, personal);
                            }
                        } else {
                            stmt.setObject(pos++, value);
                        }
                    }

                    stmt.setLong(pos++, cid);
                    stmt.setLong(pos++, mailAccount.getId());
                    stmt.setLong(pos++, user);
                    stmt.executeUpdate();
                    closeSQLStuff(stmt);

                }

                final Map<String, String> properties = mailAccount.getProperties();
                if (attributes.contains(Attribute.POP3_DELETE_WRITE_THROUGH_LITERAL)) {
                    updateProperty(cid, user, mailAccount.getId(), "pop3.deletewt", properties.get("pop3.deletewt"), con);
                }
                if (attributes.contains(Attribute.POP3_EXPUNGE_ON_QUIT_LITERAL)) {
                    updateProperty(cid, user, mailAccount.getId(), "pop3.expunge", properties.get("pop3.expunge"), con);
                }
                if (attributes.contains(Attribute.POP3_REFRESH_RATE_LITERAL)) {
                    updateProperty(cid, user, mailAccount.getId(), "pop3.refreshrate", properties.get("pop3.refreshrate"), con);
                }
                if (attributes.contains(Attribute.POP3_STORAGE_LITERAL)) {
                    updateProperty(cid, user, mailAccount.getId(), "pop3.storage", properties.get("pop3.storage"), con);
                }
                if (attributes.contains(Attribute.POP3_PATH_LITERAL)) {
                    updateProperty(cid, user, mailAccount.getId(), "pop3.path", properties.get("pop3.path"), con);
                }
                /*
                 * Drop POP3 storage folders if a rename was performed
                 */
                if (rename) {
                    dropPOP3StorageFolders(user, cid);
                }
            } catch (final SQLException e) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
            } finally {
                closeSQLStuff(null, stmt);
            }
            /*
             * Automatically check Unified INBOX existence
             */
            if (attributes.contains(Attribute.UNIFIED_INBOX_ENABLED_LITERAL) && mailAccount.isUnifiedINBOXEnabled()) {
                final UnifiedINBOXManagement management = ServerServiceRegistry.getInstance().getService(UnifiedINBOXManagement.class);
                if (null != management && !management.exists(user, cid, con)) {
                    management.createUnifiedINBOX(user, cid, con);
                }
            }
        }
    }

    private void updateUnifiedINBOXEnabled(final boolean unifiedINBOXEnabled, final int id, final int user, final int cid, final Connection con) throws MailAccountException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(UPDATE_UNIFIED_INBOX_FLAG);
            int pos = 1;
            stmt.setInt(pos++, unifiedINBOXEnabled ? 1 : 0);
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, id);
            stmt.setInt(pos++, user);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updatePersonal(final String personal, final int id, final int user, final int cid, final Connection con) throws MailAccountException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(UPDATE_PERSONAL1);
            int pos = 1;
            if (null == personal) {
                stmt.setNull(pos++, TYPE_VARCHAR);
            } else {
                stmt.setString(pos++, personal);
            }
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, id);
            stmt.setInt(pos++, user);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            // Transport table, too
            stmt = con.prepareStatement(UPDATE_PERSONAL2);
            pos = 1;
            if (null == personal) {
                stmt.setNull(pos++, TYPE_VARCHAR);
            } else {
                stmt.setString(pos++, personal);
            }
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, id);
            stmt.setInt(pos++, user);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updateProperty(final int cid, final int user, final int accountId, final String name, final String newValue, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ? AND name = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, name);
            stmt.executeUpdate();

            if (null != newValue && newValue.length() > 0) {
                closeSQLStuff(stmt);
                stmt = con.prepareStatement("INSERT INTO user_mail_account_properties (cid, user, id, name, value) VALUES (?, ?, ?, ?, ?)");
                pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, name);
                stmt.setString(pos++, newValue);
                stmt.executeUpdate();
            }
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void deleteProperties(final int cid, final int user, final int accountId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void deleteTransportProperties(final int cid, final int user, final int accountId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_transport_account_properties WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private boolean prepareURL(final Set<Attribute> attributes, final Set<Attribute> compareWith, final Attribute urlAttribute) {
        final EnumSet<Attribute> copy = attributes.isEmpty() ? EnumSet.noneOf(Attribute.class) : EnumSet.copyOf(attributes);
        if (copy.removeAll(compareWith)) {
            attributes.add(urlAttribute);
            // At least one of the mail url attributes is present in attributes
            if (attributes.containsAll(compareWith)) {
                // All mail url attributes are present
                return false;
            }
            // Not all are present
            return true;
        }
        // None are present
        return false;
    }

    public void updateMailAccount(final MailAccountDescription mailAccount, final int user, final int cid, final String sessionPassword) throws MailAccountException {
        if (mailAccount.isDefaultFlag() || MailAccount.DEFAULT_ID == mailAccount.getId()) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NO_DEFAULT_UPDATE, I(user), I(cid));
        }
        // Check name
        final String name = mailAccount.getName();
        if (!isValid(name)) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.INVALID_NAME, name);
        }
        Connection con = null;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        PreparedStatement stmt = null;
        try {
            con.setAutoCommit(false);
            {
                final String encryptedPassword;
                try {
                    encryptedPassword = MailPasswordUtil.encrypt(mailAccount.getPassword(), sessionPassword);
                } catch (final GeneralSecurityException e) {
                    throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(
                        e,
                        mailAccount.getLogin(),
                        mailAccount.getMailServer(),
                        Integer.valueOf(user),
                        Integer.valueOf(cid));
                }
                stmt = con.prepareStatement(UPDATE_MAIL_ACCOUNT);
                int pos = 1;
                stmt.setString(pos++, name);
                stmt.setString(pos++, mailAccount.generateMailServerURL());
                stmt.setString(pos++, mailAccount.getLogin());
                setOptionalString(stmt, pos++, encryptedPassword);
                stmt.setString(pos++, mailAccount.getPrimaryAddress());
                final String sh = mailAccount.getSpamHandler();
                if (null == sh) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, sh);
                }
                setOptionalString(stmt, pos++, mailAccount.getTrash());
                setOptionalString(stmt, pos++, mailAccount.getSent());
                setOptionalString(stmt, pos++, mailAccount.getDrafts());
                setOptionalString(stmt, pos++, mailAccount.getSpam());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedSpam());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedHam());
                stmt.setInt(pos++, mailAccount.isUnifiedINBOXEnabled() ? 1 : 0);
                setOptionalString(stmt, pos++, mailAccount.getTrashFullname());
                setOptionalString(stmt, pos++, mailAccount.getSentFullname());
                setOptionalString(stmt, pos++, mailAccount.getDraftsFullname());
                setOptionalString(stmt, pos++, mailAccount.getSpamFullname());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedSpamFullname());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedHamFullname());
                final String personal = mailAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                stmt.setLong(pos++, cid);
                stmt.setLong(pos++, mailAccount.getId());
                stmt.setLong(pos++, user);
                stmt.executeUpdate();
            }
            final String transportURL = mailAccount.generateTransportServerURL();
            if (null != transportURL) {
                final String encryptedTransportPassword;
                try {
                    encryptedTransportPassword = MailPasswordUtil.encrypt(mailAccount.getTransportPassword(), sessionPassword);
                } catch (final GeneralSecurityException e) {
                    throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(e, new Object[0]);
                }
                stmt.close();
                stmt = con.prepareStatement(UPDATE_TRANSPORT_ACCOUNT);
                int pos = 1;
                stmt.setString(pos++, name);
                stmt.setString(pos++, transportURL);
                stmt.setString(pos++, mailAccount.getLogin());
                setOptionalString(stmt, pos++, encryptedTransportPassword);
                stmt.setString(pos++, mailAccount.getPrimaryAddress());
                final String personal = mailAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                stmt.setLong(pos++, cid);
                stmt.setLong(pos++, mailAccount.getId());
                stmt.setLong(pos++, user);
                stmt.executeUpdate();
            }
            // Properties
            final Map<String, String> properties = mailAccount.getProperties();
            if (properties.containsKey("pop3.deletewt")) {
                updateProperty(cid, user, mailAccount.getId(), "pop3.deletewt", properties.get("pop3.deletewt"), con);
            }
            if (properties.containsKey("pop3.expunge")) {
                updateProperty(cid, user, mailAccount.getId(), "pop3.expunge", properties.get("pop3.expunge"), con);
            }
            if (properties.containsKey("pop3.refreshrate")) {
                updateProperty(cid, user, mailAccount.getId(), "pop3.refreshrate", properties.get("pop3.refreshrate"), con);
            }
            if (properties.containsKey("pop3.storage")) {
                updateProperty(cid, user, mailAccount.getId(), "pop3.storage", properties.get("pop3.storage"), con);
            }
            if (properties.containsKey("pop3.path")) {
                updateProperty(cid, user, mailAccount.getId(), "pop3.path", properties.get("pop3.path"), con);
            }
            con.commit();
            autocommit(con);
            /*
             * Automatically check Unified INBOX existence
             */
            if (mailAccount.isUnifiedINBOXEnabled()) {
                final UnifiedINBOXManagement management = ServerServiceRegistry.getInstance().getService(UnifiedINBOXManagement.class);
                if (null != management && !management.exists(user, cid, con)) {
                    management.createUnifiedINBOX(user, cid, con);
                }
            }
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } catch (final MailAccountException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionMessages.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            autocommit(con);
            Database.back(cid, true, con);
        }
    }

    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final String sessionPassword, final Connection con) throws MailAccountException {
        final int cid = ctx.getContextId();
        // Check for duplicate
        final String primaryAddress = mailAccount.getPrimaryAddress();
        if (-1 != getByPrimaryAddress(primaryAddress, user, cid, con)) {
            throw MailAccountExceptionFactory.getInstance().create(
                MailAccountExceptionMessages.CONFLICT_ADDR,
                primaryAddress,
                I(user),
                I(cid));
        }
        checkDuplicateMailAccount(mailAccount, user, cid, con);
        checkDuplicateTransportAccount(mailAccount, user, cid, con);
        // Check name
        final String name = mailAccount.getName();
        if (!isValid(name)) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.INVALID_NAME, name);
        }
        // Get ID
        final int id;
        if (mailAccount.isDefaultFlag()) {
            try {
                getDefaultMailAccount(user, cid, con);
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NO_DUPLICATE_DEFAULT);
            } catch (final MailAccountException e) {
                // Expected exception since no default account should exist
                if (LOG.isTraceEnabled()) {
                    LOG.trace(e.getMessage(), e);
                }
            }
            id = MailAccount.DEFAULT_ID;
        } else {
            try {
                id = IDGenerator.getId(ctx, com.openexchange.groupware.Types.MAIL_SERVICE, con);
            } catch (final SQLException e) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
            }
        }
        PreparedStatement stmt = null;
        try {
            {
                stmt = con.prepareStatement(INSERT_MAIL_ACCOUNT);
                final String encryptedPassword;
                if (sessionPassword == null) {
                    encryptedPassword = null;
                } else {
                    try {
                        encryptedPassword = MailPasswordUtil.encrypt(mailAccount.getPassword(), sessionPassword);
                    } catch (final GeneralSecurityException e) {
                        throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(
                            e,
                            mailAccount.getLogin(),
                            mailAccount.getMailServer(),
                            Integer.valueOf(user),
                            Integer.valueOf(ctx.getContextId()));
                    }
                }
                int pos = 1;
                // cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam,
                // confirmed_ham, spam_handler
                stmt.setLong(pos++, cid);
                stmt.setLong(pos++, id);
                stmt.setLong(pos++, user);
                stmt.setString(pos++, name);
                stmt.setString(pos++, mailAccount.generateMailServerURL());
                stmt.setString(pos++, mailAccount.getLogin());
                if (mailAccount.isDefaultFlag()) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    setOptionalString(stmt, pos++, encryptedPassword);
                }
                stmt.setString(pos++, primaryAddress);
                stmt.setInt(pos++, mailAccount.isDefaultFlag() ? 1 : 0);
                /*
                 * Default folder names: trash, sent, drafts, spam, confirmed_spam, confirmed_ham
                 */
                final DefaultFolderNamesProvider defaultFolderNamesProvider = new DefaultFolderNamesProvider(id, user, cid);
                {
                    final String[] defaultFolderNames = defaultFolderNamesProvider.getDefaultFolderNames(mailAccount, true);
                    setOptionalString(stmt, pos++, defaultFolderNames[StorageUtility.INDEX_TRASH]);
                    setOptionalString(stmt, pos++, defaultFolderNames[StorageUtility.INDEX_SENT]);
                    setOptionalString(stmt, pos++, defaultFolderNames[StorageUtility.INDEX_DRAFTS]);
                    setOptionalString(stmt, pos++, defaultFolderNames[StorageUtility.INDEX_SPAM]);
                    setOptionalString(stmt, pos++, defaultFolderNames[StorageUtility.INDEX_CONFIRMED_SPAM]);
                    setOptionalString(stmt, pos++, defaultFolderNames[StorageUtility.INDEX_CONFIRMED_HAM]);
                }
                /*
                 * Spam handler
                 */
                final String sh = mailAccount.getSpamHandler();
                if (null == sh) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, sh);
                }
                stmt.setInt(pos++, mailAccount.isUnifiedINBOXEnabled() ? 1 : 0);
                /*
                 * Default folder full names
                 */
                {
                    final String[] defaultFolderFullnames =
                        defaultFolderNamesProvider.getDefaultFolderFullnames(mailAccount, true);
                    setOptionalString(stmt, pos++, defaultFolderFullnames[StorageUtility.INDEX_TRASH]);
                    setOptionalString(stmt, pos++, defaultFolderFullnames[StorageUtility.INDEX_SENT]);
                    setOptionalString(stmt, pos++, defaultFolderFullnames[StorageUtility.INDEX_DRAFTS]);
                    setOptionalString(stmt, pos++, defaultFolderFullnames[StorageUtility.INDEX_SPAM]);
                    setOptionalString(stmt, pos++, defaultFolderFullnames[StorageUtility.INDEX_CONFIRMED_SPAM]);
                    setOptionalString(stmt, pos++, defaultFolderFullnames[StorageUtility.INDEX_CONFIRMED_HAM]);
                }
                /*
                 * Personal
                 */
                final String personal = mailAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                stmt.executeUpdate();
            }
            final String transportURL = mailAccount.generateTransportServerURL();
            if (null != transportURL) {
                stmt.close();
                final String encryptedTransportPassword;
                if (sessionPassword == null) {
                    encryptedTransportPassword = null;
                } else {
                    try {
                        encryptedTransportPassword = MailPasswordUtil.encrypt(mailAccount.getTransportPassword(), sessionPassword);
                    } catch (final GeneralSecurityException e) {
                        throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(e, new Object[0]);
                    }
                }
                // cid, id, user, name, url, login, password, send_addr, default_flag
                stmt = con.prepareStatement(INSERT_TRANSPORT_ACCOUNT);
                int pos = 1;
                stmt.setLong(pos++, cid);
                stmt.setLong(pos++, id);
                stmt.setLong(pos++, user);
                stmt.setString(pos++, name);
                stmt.setString(pos++, transportURL);
                stmt.setString(pos++, mailAccount.getTransportLogin());
                if (mailAccount.isDefaultFlag()) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    setOptionalString(stmt, pos++, encryptedTransportPassword);
                }
                stmt.setString(pos++, primaryAddress);
                stmt.setInt(pos++, mailAccount.isDefaultFlag() ? 1 : 0);
                final String personal = mailAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                stmt.executeUpdate();
            }
            // Properties
            final Map<String, String> properties = mailAccount.getProperties();
            if (!properties.isEmpty()) {
                if (properties.containsKey("pop3.deletewt")) {
                    updateProperty(cid, user, id, "pop3.deletewt", properties.get("pop3.deletewt"), con);
                }
                if (properties.containsKey("pop3.expunge")) {
                    updateProperty(cid, user, id, "pop3.expunge", properties.get("pop3.expunge"), con);
                }
                if (properties.containsKey("pop3.refreshrate")) {
                    updateProperty(cid, user, id, "pop3.refreshrate", properties.get("pop3.refreshrate"), con);
                }
                if (properties.containsKey("pop3.storage")) {
                    updateProperty(cid, user, id, "pop3.storage", properties.get("pop3.storage"), con);
                }
                if (properties.containsKey("pop3.path")) {
                    updateProperty(cid, user, id, "pop3.path", properties.get("pop3.path"), con);
                }
            }
            dropPOP3StorageFolders(user, cid);
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } catch (final MailException e) {
            throw new MailAccountException(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
        /*
         * Automatically check Unified INBOX existence
         */
        if (mailAccount.isUnifiedINBOXEnabled()) {
            final UnifiedINBOXManagement management = ServerServiceRegistry.getInstance().getService(UnifiedINBOXManagement.class);
            if (null != management && !management.exists(user, cid, con)) {
                management.createUnifiedINBOX(user, cid, con);
            }
        }
        return id;
    }

    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final String sessionPassword) throws MailAccountException {
        final int cid = ctx.getContextId();
        Connection con = null;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        final int retval;
        try {
            con.setAutoCommit(false);
            retval = insertMailAccount(mailAccount, user, ctx, sessionPassword, con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } catch (final MailAccountException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionMessages.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(cid, true, con);
        }
        return retval;
    }

    public int[] getByHostNames(final Collection<String> hostNames, final int user, final int cid) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            return getByHostNames(hostNames, user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    private int[] getByHostNames(final Collection<String> hostNames, final int user, final int cid, final Connection con) throws MailAccountException {
        if (null == hostNames || hostNames.isEmpty()) {
            return new int[0];
        }
        final Set<String> set = new HashSet<String>(hostNames.size());
        for (final String hostName : hostNames) {
            set.add(hostName.toLowerCase(Locale.ENGLISH));
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, url FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id");
            stmt.setLong(1, cid);
            stmt.setLong(2, user);
            result = stmt.executeQuery();
            if (!result.next()) {
                return new int[0];
            }
            final CustomMailAccount tmp = new CustomMailAccount();
            final TIntArrayList ids = new TIntArrayList(6);
            do {
                tmp.parseMailServerURL(result.getString(2));
                if (set.contains(tmp.getMailServer().toLowerCase(Locale.ENGLISH))) {
                    ids.add(result.getInt(1));
                }
            } while (result.next());
            if (ids.isEmpty()) {
                return new int[0];
            }
            final int[] array = ids.toNativeArray();
            Arrays.sort(array);
            return array;
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    public int getByPrimaryAddress(final String primaryAddress, final int user, final int cid) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        try {
            return getByPrimaryAddress(primaryAddress, user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    private int getByPrimaryAddress(final String primaryAddress, final int user, final int cid, final Connection con) throws MailAccountException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_ACCOUNT_BY_PRIMARY_ADDR);
            stmt.setLong(1, cid);
            stmt.setString(2, primaryAddress);
            stmt.setLong(3, user);
            result = stmt.executeQuery();
            if (!result.next()) {
                return -1;
            }
            final int id = result.getInt(1);
            if (result.next()) {
                throw MailAccountExceptionFactory.getInstance().create(
                    MailAccountExceptionMessages.CONFLICT_ADDR,
                    primaryAddress,
                    I(user),
                    I(cid));
            }
            return id;
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void checkDuplicateMailAccount(final MailAccountDescription mailAccount, final int user, final int cid, final Connection con) throws MailAccountException {
        final String server = mailAccount.getMailServer();
        if (isEmpty(server)) {
            /*
             * No mail server specified
             */
            return;
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, url, login FROM user_mail_account WHERE cid = ? AND user = ?");
            stmt.setLong(1, cid);
            stmt.setLong(2, user);
            result = stmt.executeQuery();
            if (!result.next()) {
                return;
            }
            InetAddress addr;
            try {
                addr = InetAddress.getByName(server);
            } catch (final UnknownHostException e) {
                LOG.warn(e.getMessage(), e);
                addr = null;
            }
            final int port = mailAccount.getMailPort();
            final String login = mailAccount.getLogin();
            do {
                final int id = (int) result.getLong(1);
                final AbstractMailAccount current = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount();
                current.parseMailServerURL(result.getString(2));
                if (checkMailServer(server, addr, current) && current.getMailPort() == port && login.equals(current.getLogin())) {
                    throw MailAccountExceptionMessages.DUPLICATE_MAIL_ACCOUNT.create(I(user), I(cid));
                }
            } while (result.next());
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private static boolean checkMailServer(final String server, final InetAddress addr, final AbstractMailAccount current) {
        final String mailServer = current.getMailServer();
        if (isEmpty(mailServer)) {
            return false;
        }
        if (null == addr) {
            /*
             * Check by server string
             */
            return server.equalsIgnoreCase(mailServer);
        }
        try {
            return addr.equals(InetAddress.getByName(mailServer));
        } catch (final UnknownHostException e) {
            LOG.warn(e.getMessage(), e);
            /*
             * Check by server string
             */
            return server.equalsIgnoreCase(mailServer);
        }
    }

    private void checkDuplicateTransportAccount(final MailAccountDescription mailAccount, final int user, final int cid, final Connection con) throws MailAccountException {
        final String server = mailAccount.getTransportServer();
        if (isEmpty(server)) {
            /*
             * No transport server specified
             */
            return;
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT id, url, login FROM user_transport_account WHERE cid = ? AND user = ?");
            stmt.setLong(1, cid);
            stmt.setLong(2, user);
            result = stmt.executeQuery();
            if (!result.next()) {
                return;
            }
            InetAddress addr;
            try {
                addr = InetAddress.getByName(server);
            } catch (final UnknownHostException e) {
                LOG.warn(e.getMessage(), e);
                addr = null;
            }
            final int port = mailAccount.getTransportPort();
            final String login = mailAccount.getTransportLogin();
            do {
                final int id = (int) result.getLong(1);
                final AbstractMailAccount current = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount();
                current.parseTransportServerURL(result.getString(2));
                if (checkTransportServer(server, addr, current) && current.getTransportPort() == port && login.equals(current.getTransportLogin())) {
                    throw MailAccountExceptionMessages.DUPLICATE_TRANSPORT_ACCOUNT.create(I(user), I(cid));
                }
            } while (result.next());
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private static boolean checkTransportServer(final String server, final InetAddress addr, final AbstractMailAccount current) {
        final String transportServer = current.getTransportServer();
        if (isEmpty(transportServer)) {
            return false;
        }
        if (null == addr) {
            /*
             * Check by server string
             */
            return server.equalsIgnoreCase(transportServer);
        }
        try {
            return addr.equals(InetAddress.getByName(transportServer));
        } catch (final UnknownHostException e) {
            LOG.warn(e.getMessage(), e);
            /*
             * Check by server string
             */
            return server.equalsIgnoreCase(transportServer);
        }
    }

    public MailAccount getTransportAccountForID(final int id, final int user, final int cid) throws MailAccountException {
        final MailAccount account = getMailAccount(id, user, cid);
        if (null == account.getTransportServer()) {
            return getDefaultMailAccount(user, cid);
        }
        return account;
    }

    public String checkCanDecryptPasswords(final int user, final int cid, final String secret) throws MailAccountException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int id = -1;
        try {
            con = Database.get(cid, false);
            stmt = con.prepareStatement(SELECT_PASSWORD1);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);

            rs = stmt.executeQuery();

            while (rs.next()) {
                id = rs.getInt(1);
                if(id != MailAccount.DEFAULT_ID) {
                    final String password = rs.getString(2);
                    if (password != null) {
                        MailPasswordUtil.decrypt(password, secret);
                    }
                }
            }

            rs.close();
            stmt.close();

            stmt = con.prepareStatement(SELECT_PASSWORD2);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);

            rs = stmt.executeQuery();

            while (rs.next()) {
                id = rs.getInt(1);
                if (id != MailAccount.DEFAULT_ID) {
                    final String password = rs.getString(2);
                    if (password != null) {
                        MailPasswordUtil.decrypt(password, secret);
                    }
                }
            }

        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } catch (final GeneralSecurityException e) {
            return "Failed on decrypting mail account password for account "+id+" user: "+user+", cid: "+cid;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (con != null) {
                Database.back(cid, false, con);
            }
        }
        return null;
    }

    public void migratePasswords(final int user, final int cid, final String oldSecret, final String newSecret) throws MailAccountException {
        Connection con = null;
        PreparedStatement select = null;
        PreparedStatement update = null;
        ResultSet rs = null;
        try {
            con = Database.get(cid, true);
            con.setAutoCommit(false);
            update = con.prepareStatement(UPDATE_PASSWORD1);
            update.setInt(2, cid);
            update.setInt(4, user);

            select = con.prepareStatement(SELECT_PASSWORD1);
            select.setInt(1, cid);
            select.setInt(2, user);

            rs = select.executeQuery();

            while (rs.next()) {
                final String password = rs.getString(2);
                final int id = rs.getInt(1);
                if (id != MailAccount.DEFAULT_ID) {
                    try {
                        // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                        MailPasswordUtil.decrypt(password, newSecret);
                    } catch (final GeneralSecurityException x) {
                        // We couldn't decrypt the password, so, let's try the oldSecret and do the migration
                        final String transcribed = MailPasswordUtil.encrypt(MailPasswordUtil.decrypt(password, oldSecret), newSecret);
                        update.setString(1, transcribed);
                        update.setInt(3, id);
                        update.executeUpdate();
                    }
                }
            }

            rs.close();
            select.close();
            update.close();

            update = con.prepareStatement(UPDATE_PASSWORD2);
            update.setInt(2, cid);
            update.setInt(4, user);

            select = con.prepareStatement(SELECT_PASSWORD2);
            select.setInt(1, cid);
            select.setInt(2, user);

            rs = select.executeQuery();

            while (rs.next()) {
                final String password = rs.getString(2);
                final String transcribed = MailPasswordUtil.encrypt(MailPasswordUtil.decrypt(password, oldSecret), newSecret);
                update.setString(1, transcribed);
                update.setInt(3, rs.getInt(1));
                update.executeUpdate();
            }
            con.commit();
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } catch (final GeneralSecurityException e) {
            throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(e, "", "", Integer.valueOf(user), Integer.valueOf(cid));
        } finally {
            DBUtils.closeSQLStuff(rs, select);
            DBUtils.closeSQLStuff(update);
            if (con != null) {
                try {
                    con.rollback();
                    con.setAutoCommit(true);
                } catch (final SQLException e) {
                    // Don't care
                }
                Database.back(cid, true, con);
            }
        }
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++ UTILITY METHOD(S) ++++++++++++++++++++++++++++++++++++
     */

    private static void setOptionalString(final PreparedStatement stmt, final int pos, final String string) throws SQLException {
        if (null == string) {
            stmt.setString(pos, "");
        } else {
            stmt.setString(pos, string);
        }
    }

    private static String getOptionalString(final String string) {
        return (null == string || string.length() == 0) ? null : string;
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final char[] chars = string.toCharArray();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < chars.length; i++) {
            isWhitespace = Character.isWhitespace(chars[i]);
        }
        return isWhitespace;
    }

    /**
     * Binary-sorted invalid characters: No control <code>\t\n\f\r</code> or punctuation <code>!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~</code>
     * except <code>'-'</code> and <code>'_'</code>.
     */
    private static final char[] CHARS_INVALID = {
        '\t', '\n', '\f', '\r', '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '.', '/', ':', ';', '<', '=', '>', '?', '@',
        '[', '\\', ']', '^', '`', '{', '|', '}', '~' };

    /**
     * Checks if specified name contains an invalid character.
     * 
     * @param name The name to check
     * @return <code>true</code> if name contains an invalid character; otherwsie <code>false</code>
     */
    private static boolean isValid(final String name) {
        /*
         * TODO: Re-think about invalid characters
         */
        if (null == name || 0 == name.length()) {
            return false;
        }
        
        if (true) {
            return true;
        }
        
        final char[] chars = name.toCharArray();
        boolean valid = true;
        boolean isWhitespace = true;
        for (int i = 0; valid && i < chars.length; i++) {
            final char c = chars[i];
            valid = (Arrays.binarySearch(CHARS_INVALID, c) < 0);
            isWhitespace &= Character.isWhitespace(c);
        }
        return !isWhitespace && valid;
    }

}
