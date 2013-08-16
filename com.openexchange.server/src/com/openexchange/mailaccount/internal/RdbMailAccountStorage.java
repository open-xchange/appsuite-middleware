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

package com.openexchange.mailaccount.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.utils.ProviderUtility.toSocketAddr;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.idn.IDNA;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.IMailAccessCache;
import com.openexchange.mail.utils.DefaultFolderNamesProvider;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mail.utils.MailPasswordUtil;
import com.openexchange.mail.utils.ProviderUtility;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.json.fields.GetSwitch;
import com.openexchange.mailaccount.json.fields.MailAccountGetSwitch;
import com.openexchange.mailaccount.json.fields.SetSwitch;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.SetableSession;
import com.openexchange.session.SetableSessionFactory;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.Collections.SmartIntArray;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbMailAccountStorage} - The relational database implementation of mail account storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbMailAccountStorage implements MailAccountStorageService {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(RdbMailAccountStorage.class));

    private static final SecretEncryptionStrategy<GenericProperty> STRATEGY = MailPasswordUtil.STRATEGY;

    /**
     * The constant in the Java programming language, sometimes referred to as a type code, that identifies the generic SQL type VARCHAR.
     */
    private static final int TYPE_VARCHAR = Types.VARCHAR;

    private static final String SELECT_MAIL_ACCOUNT =
        "SELECT name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, unified_inbox, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname, personal, replyTo, archive, archive_fullname FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_TRANSPORT_ACCOUNT =
        "SELECT name, url, login, password, send_addr, default_flag, personal, replyTo FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_MAIL_ACCOUNTS = "SELECT id, url FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id";

    private static final String SELECT_BY_LOGIN = "SELECT id, user FROM user_mail_account WHERE cid = ? AND login = ?";

    private static final String SELECT_BY_PRIMARY_ADDR = "SELECT id, user FROM user_mail_account WHERE cid = ? AND primary_addr = ?";

    private static final String SELECT_ACCOUNT_BY_PRIMARY_ADDR =
        "SELECT id FROM user_mail_account WHERE cid = ? AND primary_addr = ? AND user = ?";

    private static final String DELETE_MAIL_ACCOUNT = "DELETE FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String DELETE_TRANSPORT_ACCOUNT = "DELETE FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_MAIL_ACCOUNT =
        "UPDATE user_mail_account SET name = ?, url = ?, login = ?, password = ?, primary_addr = ?, spam_handler = ?, trash = ?, sent = ?, drafts = ?, spam = ?, confirmed_spam = ?, confirmed_ham = ?, unified_inbox = ?, trash_fullname = ?, sent_fullname = ?, drafts_fullname = ?, spam_fullname = ?, confirmed_spam_fullname = ?, confirmed_ham_fullname = ?, personal = ?, replyTo = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String INSERT_MAIL_ACCOUNT =
        "INSERT INTO user_mail_account (cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, unified_inbox, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname, personal, replyTo, archive, archive_fullname) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_TRANSPORT_ACCOUNT =
        "UPDATE user_transport_account SET name = ?, url = ?, login = ?, password = ?, send_addr = ?, personal = ?, replyTo = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String INSERT_TRANSPORT_ACCOUNT =
        "INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag, personal, replyTo) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    private static final String UPDATE_UNIFIED_INBOX_FLAG =
        "UPDATE user_mail_account SET unified_inbox = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_PERSONAL1 = "UPDATE user_mail_account SET personal = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_PERSONAL2 = "UPDATE user_transport_account SET personal = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_EXISTS_FOR_USER1 = "SELECT 1 FROM user_mail_account WHERE cid = ? AND user = ? AND id > 0 LIMIT 1";

    private static final String SELECT_EXISTS_FOR_USER2 = "SELECT 1 FROM user_transport_account WHERE cid = ? AND user = ? AND id > 0 LIMIT 1";

    private static final String SELECT_PASSWORD1 = "SELECT id, password, login, url FROM user_mail_account WHERE cid = ? AND user = ?";

    private static final String SELECT_PASSWORD2 = "SELECT id, password, login, url FROM user_transport_account WHERE cid = ? AND user = ?";

    private static final String UPDATE_PASSWORD1 = "UPDATE user_mail_account SET password = ?  WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_PASSWORD2 = "UPDATE user_transport_account SET password = ?  WHERE cid = ? AND id = ? AND user = ?";

    private static final String PARAM_POP3_STORAGE_FOLDERS = "com.openexchange.mailaccount.pop3Folders";

    private static <V> V performSynchronized(final Callable<V> task, final Session session) throws Exception {
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null == lock) {
            lock = Session.EMPTY_LOCK;
        }
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

    private static void dropPOP3StorageFolders(final int userId, final int contextId) {
        final SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null != service) {
            for (final Session session : service.getSessions(userId, contextId)) {
                session.setParameter(PARAM_POP3_STORAGE_FOLDERS, null);
            }
        }
    }

    @Override
    public void invalidateMailAccount(final int id, final int user, final int cid) throws OXException {
        // Nothing to do
    }

    @Override
    public void invalidateMailAccounts(final int user, final int cid) throws OXException {
        // Nothing to do
    }

    /**
     * Gets the POP3 storage folders for specified session.
     *
     * @param session The session
     * @return The POP3 storage folder full names
     * @throws OXException If an error occurs
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getPOP3StorageFolders(final Session session) throws OXException {
        Set<String> set = (Set<String>) session.getParameter(PARAM_POP3_STORAGE_FOLDERS);
        if (null == set) {
            try {
                final Callable<Set<String>> task = new Callable<Set<String>>() {

                    @Override
                    public Set<String> call() throws OXException {
                        Set<String> set = (Set<String>) session.getParameter(PARAM_POP3_STORAGE_FOLDERS);
                        if (null == set) {
                            set = getPOP3StorageFolders0(session);
                            session.setParameter(PARAM_POP3_STORAGE_FOLDERS, set);
                        }
                        return set;
                    }
                };
                set = performSynchronized(task, session);
            } catch (final OXException e) {
                throw e;
            } catch (final Exception e) {
                throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return set;
    }

    static Set<String> getPOP3StorageFolders0(final Session session) throws OXException {
        final int contextId = session.getContextId();
        final Connection con = Database.get(contextId, false);
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
            if (null != stmt && LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new com.openexchange.java.StringAllocator().append("\n\tFailed mail account statement:\n\t").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }

    private void fillMailAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid) throws OXException {
        final Connection con = Database.get(cid, false);
        try {
            fillMailAccount(mailAccount, id, user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    private void fillMailAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_MAIL_ACCOUNT);
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw MailAccountExceptionCodes.NOT_FOUND.create(I(id), I(user), I(cid));
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
            /*
             * Default folder names
             */
            mailAccount.setTrash(getOptionalString(result.getString(7)));
            mailAccount.setSent(getOptionalString(result.getString(8)));
            mailAccount.setDrafts(getOptionalString(result.getString(9)));
            mailAccount.setSpam(getOptionalString(result.getString(10)));
            mailAccount.setConfirmedSpam(getOptionalString(result.getString(11)));
            mailAccount.setConfirmedHam(getOptionalString(result.getString(12)));
            mailAccount.setArchive(getOptionalString(result.getString(23)));
            /*
             * Spam handler name
             */
            mailAccount.setSpamHandler(result.getString(13));
            /*
             * Unified mail enabled
             */
            mailAccount.setUnifiedINBOXEnabled(result.getInt(14) > 0);
            /*-
             * Default folder full names
             *
             * Full names for: Trash, Sent, Drafts, and Spam
             */
            {
                final Session session = SessiondService.SERVICE_REFERENCE.get().getAnyActiveSessionForUser(user, cid);
                if (null != session) {
                    final String parameterName = MailSessionParameterNames.getParamDefaultFolderArray();
                    final String[] fullNames = MailSessionCache.getInstance(session).getParameter(id, parameterName);
                    String s = getOptionalString(result.getString(15));
                    mailAccount.setTrashFullname(s == null ? (null == fullNames ? null : fullNames[StorageUtility.INDEX_TRASH]) : s);
                    s = getOptionalString(result.getString(16));
                    mailAccount.setSentFullname(s == null ? (null == fullNames ? null : fullNames[StorageUtility.INDEX_SENT]) : s);
                    s = getOptionalString(result.getString(17));
                    mailAccount.setDraftsFullname(s == null ? (null == fullNames ? null : fullNames[StorageUtility.INDEX_DRAFTS]) : s);
                    s = getOptionalString(result.getString(18));
                    mailAccount.setSpamFullname(s == null ? (null == fullNames ? null : fullNames[StorageUtility.INDEX_SPAM]) : s);
                    s = getOptionalString(result.getString(24));
                    mailAccount.setArchiveFullname(s);
                }
            }
            /*
             * Full names for confirmed-spam and confirmed-ham
             */
            mailAccount.setConfirmedSpamFullname(getOptionalString(result.getString(19)));
            mailAccount.setConfirmedHamFullname(getOptionalString(result.getString(20)));
            final String pers = result.getString(21);
            if (result.wasNull()) {
                mailAccount.setPersonal(null);
            } else {
                mailAccount.setPersonal(pers);
            }
            final String replyTo = result.getString(22);
            if (result.wasNull()) {
                mailAccount.setReplyTo(null);
            } else {
                mailAccount.setReplyTo(replyTo);
            }
            mailAccount.setUserId(user);
            /*
             * Fill properties
             */
            fillProperties(mailAccount, cid, user, id, con);
        } catch (final SQLException e) {
            if (null != stmt && LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new com.openexchange.java.StringAllocator().append("\n\tFailed mail account statement:\n\t").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void fillTransportAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid) throws OXException {
        final Connection con = Database.get(cid, false);
        try {
            fillTransportAccount(mailAccount, id, user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    private void fillTransportAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid, final Connection con) throws OXException {
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
                final String replyTo = result.getString(8);
                if (!result.wasNull()) {
                    mailAccount.setReplyTo(replyTo);
                }
            } else {
                // throw MailAccountExceptionMessages.NOT_FOUND, I(id), I(user), I(cid));
                mailAccount.setTransportServer((String) null);
            }
        } catch (final SQLException e) {
            if (null != stmt && LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new com.openexchange.java.StringAllocator().append("\n\tFailed mail account statement:\n\t").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
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
            if (rs.next()) {
                final Map<String, String> properties = new HashMap<String, String>(8, 1);
                do {
                    final String name = rs.getString(1);
                    if (!rs.wasNull()) {
                        final String value = rs.getString(2);
                        if (!rs.wasNull()) {
                            properties.put(name, value);
                        }
                    }
                } while (rs.next());
                // Add aliases, too
                if (MailAccount.DEFAULT_ID == id) {
                    properties.put("addresses", getAliases(user, cid, mailAccount));
                }
                mailAccount.setProperties(properties);
            } else {
                // Add aliases, too
                if (MailAccount.DEFAULT_ID == id) {
                    Map<String, String> properties = new HashMap<String, String>(8, 1);
                    properties.put("addresses", getAliases(user, cid, mailAccount));
                    mailAccount.setProperties(properties);
                } else {
                    mailAccount.setProperties(Collections.<String, String> emptyMap());
                }
            }
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }
    
    private static String getAliases(int user, int cid, AbstractMailAccount mailAccount) {
        StringAllocator sb = new StringAllocator(128);
        sb.append(mailAccount.getPrimaryAddress());
        Set<String> s = new HashSet<String>(4);
        s.add(mailAccount.getPrimaryAddress());
        for (String alias : UserStorage.getStorageUser(user, cid).getAliases()) {
            if (s.add(alias)) {
                sb.append(", ").append(alias);
            }
        }
        return sb.toString();
    }

    /**
     * Initializes a new {@link RdbMailAccountStorage}.
     */
    RdbMailAccountStorage() {
        super();
    }

    @Override
    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid) throws OXException {
        deleteMailAccount(id, properties, user, cid, false);
    }

    @Override
    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid, final boolean deletePrimary) throws OXException {
        if (!deletePrimary && MailAccount.DEFAULT_ID == id) {
            throw MailAccountExceptionCodes.NO_DEFAULT_DELETE.create(I(user), I(cid));
        }
        dropPOP3StorageFolders(user, cid);
        final Connection con = Database.get(cid, true);
        try {
            con.setAutoCommit(false);
            deleteMailAccount(id, properties, user, cid, deletePrimary, con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(cid, true, con);
        }
    }

    @Override
    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid, final boolean deletePrimary, final Connection con) throws OXException {
        if (!deletePrimary && MailAccount.DEFAULT_ID == id) {
            throw MailAccountExceptionCodes.NO_DEFAULT_DELETE.create(I(user), I(cid));
        }
        dropPOP3StorageFolders(user, cid);
        final boolean restoreConstraints = disableForeignKeyChecks(con);
        PreparedStatement stmt = null;
        try {
            // A POP3 account?
            final String pop3Path = getPOP3Path(id, user, cid, con);
            if (null != pop3Path) {
                try {
                    cleanseFromPrimary(pop3Path, user, cid);
                } catch (final OXException e) {
                    LOG.warn("Couldn't delete POP3 backup folders in primary mail account", e);
                }
            }
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
        } catch (final SQLException e) {
            final String className = e.getClass().getName();
            if ((null != className) && className.endsWith("MySQLIntegrityConstraintViolationException")) {
                try {
                    if (handleConstraintViolationException(e, id, user, cid, con)) {
                        /*
                         * Retry & return
                         */
                        deleteMailAccount(id, properties, user, cid, deletePrimary, con);
                        return;
                    }
                } catch (final RuntimeException re) {
                    LOG.debug(re.getMessage(), re);
                }
            }
            /*
             * Indicate SQL error
             */
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            if (restoreConstraints) {
                try {
                    enableForeignKeyChecks(con);
                } catch (final SQLException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private static String getPOP3Path(final int id, final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT url FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?");
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            rs = stmt.executeQuery();
            if (!rs.next() || !rs.getString(1).startsWith("pop3")) {
                return null;
            }
            closeSQLStuff(rs, stmt);
            stmt = con.prepareStatement("SELECT value FROM user_mail_account_properties WHERE cid = ? AND id = ? AND user = ? AND name = ?");
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            stmt.setString(4, "pop3.path");
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } catch (final SQLException e) {
            if (null != stmt && LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new com.openexchange.java.StringAllocator().append("\n\tFailed mail account statement:\n\t").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static void cleanseFromPrimary(final String path, final int user, final int cid) throws OXException {
        if (isEmpty(path)) {
            return;
        }
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> defaultMailAccess = null;
        try {
            defaultMailAccess = MailAccess.getInstance(user, cid);
            defaultMailAccess.connect(false);
            defaultMailAccess.getFolderStorage().deleteFolder(path, true);
        } finally {
            if (null != defaultMailAccess) {
                defaultMailAccess.close(false);
            }
        }
    }

    private static final Pattern PATTERN_CONSTRAINT_VIOLATION;

    static {
        /*
         * Specify regex quotes
         */
        final String quote1 = Pattern.quote("Cannot delete or update a parent row: a foreign key constraint fails (`");
        final String quote2 = Pattern.quote("`, CONSTRAINT `");
        final String quote3 = Pattern.quote("` FOREIGN KEY (");
        final String quote4 = Pattern.quote(") REFERENCES `user_mail_account` (`cid`, `user`, `id`))");
        /*
         * Compose pattern
         */
        PATTERN_CONSTRAINT_VIOLATION =
            Pattern.compile(quote1 + "([^`]+)" + quote2 + "[^`]+" + quote3 + "([^)]+)" + quote4, Pattern.CASE_INSENSITIVE);
    }

    private static boolean handleConstraintViolationException(final SQLException e, final int id, final int user, final int cid, final Connection con) throws OXException {
        final Matcher m = PATTERN_CONSTRAINT_VIOLATION.matcher(e.getMessage());
        if (!m.matches()) {
            return false;
        }
        /*
         * Check rows
         */
        final String[] rows = m.group(2).replaceAll(Pattern.quote("`"), "").split(" *, *");
        if (rows.length != 3) {
            return false;
        }
        final Set<String> set = new HashSet<String>(Arrays.asList(rows));
        set.removeAll(Arrays.asList("cid", "user", "id"));
        if (!set.isEmpty()) {
            return false;
        }
        /*
         * Get table name
         */
        String tableName = m.group(1);
        final int pos = tableName.indexOf('/') + 1;
        if (pos > 0) {
            tableName = tableName.substring(pos);
        }
        return dropReferenced(id, user, cid, tableName, con);
    }

    private static boolean dropReferenced(final int id, final int user, final int cid, final String tableName, final Connection con) throws OXException {
        final boolean transactional;
        try {
            transactional = !con.getAutoCommit();
            if (transactional) {
                rollback(con);
                con.setAutoCommit(true);
            }
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Delete referenced
         */
        final String sql =
            new com.openexchange.java.StringAllocator(64).append("DELETE FROM ").append(tableName).append(" WHERE cid = ? AND id = ? and user = ?").toString();
        PreparedStatement stmt = null;
        boolean retval = false;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            stmt.executeUpdate();
            retval = true;
        } catch (final SQLException e) {
            LOG.warn("Couldn't delete referenced entries with: " + sql, e);
        } catch (final Exception e) {
            LOG.warn("Couldn't delete referenced entries with: " + sql, e);
        } finally {
            closeSQLStuff(stmt);
        }
        /*
         * Restore transaction state
         */
        try {
            if (transactional) {
                con.setAutoCommit(false);
            }
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
        return retval;
    }

    public MailAccount getDefaultMailAccount(final int user, final int cid, final Connection con) throws OXException {
        return getMailAccount(MailAccount.DEFAULT_ID, user, cid, con);
    }

    @Override
    public MailAccount getDefaultMailAccount(final int user, final int cid) throws OXException {
        return getMailAccount(MailAccount.DEFAULT_ID, user, cid);
    }

    public MailAccount getMailAccount(final int id, final int user, final int cid, final Connection con) throws OXException {
        if (null == con) {
            return getMailAccount(id, user, cid);
        }
        final AbstractMailAccount retval = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount();
        fillMailAccount(retval, id, user, cid, con);
        fillTransportAccount(retval, id, user, cid, con);
        return retval;
    }

    @Override
    public MailAccount getMailAccount(final int id, final int user, final int cid) throws OXException {
        final Connection rcon = Database.get(cid, false);
        try {
            return getMailAccount(id, user, cid, rcon);
        } finally {
            Database.back(cid, false, rcon);
        }
    }

    @Override
    public MailAccount[] getUserMailAccounts(final int user, final int cid) throws OXException {
        final Connection con = Database.get(cid, false);
        try {
            return getUserMailAccounts(user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    @Override
    public MailAccount[] getUserMailAccounts(final int user, final int cid, final Connection con) throws OXException {
        final int[] ids = getUserMailAccountIDs(user, cid, con);
        final MailAccount[] retval = new MailAccount[ids.length];
        for (int i = 0; i < ids.length; i++) {
            retval[i] = getMailAccount(ids[i], user, cid, con);
        }
        return retval;
    }

    int[] getUserMailAccountIDs(final int user, final int cid) throws OXException {
        final Connection con = Database.get(cid, false);
        try {
            return getUserMailAccountIDs(user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    int[] getUserMailAccountIDs(final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_MAIL_ACCOUNTS);
            stmt.setLong(1, cid);
            stmt.setLong(2, user);
            result = stmt.executeQuery();
            if (!result.next()) {
                return new int[0];
            }
            final TIntList ids = new TIntArrayList(8);
            do {
                final String url = result.getString(2);
                if (null != MailProviderRegistry.getRealMailProvider(ProviderUtility.extractProtocol(url, URIDefaults.IMAP.getProtocol()))) {
                    ids.add(result.getInt(1));
                }
            } while (result.next());
            return ids.toArray();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public MailAccount[] resolveLogin(final String login, final int cid) throws OXException {
        final int[][] idsAndUsers = resolveLogin2IDs(login, cid);
        final MailAccount[] retval = new MailAccount[idsAndUsers.length];
        for (int i = 0; i < idsAndUsers.length; i++) {
            final int[] idAndUser = idsAndUsers[i];
            retval[i] = getMailAccount(idAndUser[0], idAndUser[1], cid);
        }
        return retval;
    }

    int[][] resolveLogin2IDs(final String login, final int cid) throws OXException {
        final int[] ids;
        final int[] users;
        {
            final Connection con = Database.get(cid, false);
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
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
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

    @Override
    public MailAccount[] resolveLogin(final String login, final InetSocketAddress server, final int cid) throws OXException {
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

    @Override
    public MailAccount[] resolvePrimaryAddr(final String primaryAddress, final int cid) throws OXException {
        final int[][] idsAndUsers = resolvePrimaryAddr2IDs(primaryAddress, cid);
        final List<MailAccount> l = new ArrayList<MailAccount>(idsAndUsers.length);
        for (final int[] idAndUser : idsAndUsers) {
            final MailAccount candidate = getMailAccount(idAndUser[0], idAndUser[1], cid);
            l.add(candidate);
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    int[][] resolvePrimaryAddr2IDs(final String primaryAddress, final int cid) throws OXException {
        final int[] ids;
        final int[] users;
        {
            final Connection con = Database.get(cid, false);
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
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
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

    @Override
    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final Session session) throws OXException {
        updateMailAccount(mailAccount, attributes, user, cid, session, false);
    }

    private void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final Session session, final boolean changePrimary) throws OXException {
        final Connection con = Database.get(cid, true);
        try {
            con.setAutoCommit(false);
            updateMailAccount(mailAccount, attributes, user, cid, session, con, changePrimary);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
     * Contains attributes which denote the full names of an account's default folders.
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
        Attribute.PERSONAL_LITERAL,
        Attribute.REPLY_TO_LITERAL,
        Attribute.ARCHIVE_LITERAL,
        Attribute.ARCHIVE_FULLNAME_LITERAL);

    @Override
    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final Session session, final Connection con, final boolean changePrimary) throws OXException {
        if (null == con) {
            updateMailAccount(mailAccount, attributes, user, cid, session, changePrimary);
            return;
        }
        dropPOP3StorageFolders(user, cid);
        if (attributes.contains(Attribute.NAME_LITERAL)) {
            // Check name
            final String name = mailAccount.getName();
            if (!isValid(name)) {
                throw MailAccountExceptionCodes.INVALID_NAME.create(name);
            }
        }
        if (!changePrimary && (mailAccount.isDefaultFlag() || MailAccount.DEFAULT_ID == mailAccount.getId())) {
            final boolean containsUnifiedInbox = attributes.contains(Attribute.UNIFIED_INBOX_ENABLED_LITERAL);
            final boolean containsPersonal = attributes.contains(Attribute.PERSONAL_LITERAL);
            final boolean containsReplyTo = attributes.contains(Attribute.REPLY_TO_LITERAL);
            final boolean containsArchive = attributes.contains(Attribute.ARCHIVE_LITERAL);
            final boolean containsArchiveFullName = attributes.contains(Attribute.ARCHIVE_FULLNAME_LITERAL);
            if (!containsUnifiedInbox && !containsPersonal && !containsReplyTo && !containsArchive && !containsArchiveFullName) {
                /*
                 * Another attribute must not be changed
                 */
                throw MailAccountExceptionCodes.NO_DEFAULT_UPDATE.create(I(user), I(cid));
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
             * Iterate attributes and compare their values except the one for Attribute.UNIFIED_INBOX_ENABLED_LITERAL,
             * Attribute.PERSONAL_LITERAL and Attribute.REPLY_TO_LITERAL
             */
            for (final Attribute attribute : attributes) {
                /*
                 * Check for an attribute different from Attribute.UNIFIED_INBOX_ENABLED_LITERAL and Attribute.PERSONAL_LITERAL
                 */
                if (!PRIMARY_EDITABLE.contains(attribute)) {
                    final Object storageValue = attribute.doSwitch(storageGetSwitch);
                    final Object newValue = attribute.doSwitch(getSwitch);
                    if (null != storageValue && (Attribute.PASSWORD_LITERAL.equals(attribute) ? null != newValue : !(DEFAULT_FULL_NAMES.contains(attribute) ? MailFolderUtility.prepareMailFolderParam(storageValue.toString()).equals(MailFolderUtility.prepareMailFolderParam(newValue.toString())) : storageValue.equals(newValue)))) {
                        /*
                         * Another attribute must not be changed
                         */
                        throw MailAccountExceptionCodes.NO_DEFAULT_UPDATE.create(I(user), I(cid));
                    }
                }
            }
            if (containsUnifiedInbox) {
                /*
                 * OK, update UNIFIED_INBOX_ENABLED flag.
                 */
                updateUnifiedINBOXEnabled(mailAccount.isUnifiedINBOXEnabled(), MailAccount.DEFAULT_ID, user, cid, con);
                /*
                 * Automatically check Unified Mail existence
                 */
                if (mailAccount.isUnifiedINBOXEnabled()) {
                    final UnifiedInboxManagement management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
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
            if (containsReplyTo) {
                /*
                 * OK, update reply-to string.
                 */
                updateReplyTo(mailAccount.getReplyTo(), MailAccount.DEFAULT_ID, user, cid, con);
            }
            if (containsArchive) {
                updateArchive(mailAccount.getArchive(), MailAccount.DEFAULT_ID, user, cid, con);
            }
            if (containsArchiveFullName) {
                updateArchiveFullName(mailAccount.getArchiveFullname(), MailAccount.DEFAULT_ID, user, cid, con);
            }
        } else {
            /*
             * Perform common update
             */
            PreparedStatement stmt = null;
            ResultSet rs = null;
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
                    checkDuplicateMailAccount(mailAccount, new TIntHashSet(new int[] {mailAccount.getId()}), user, cid, con);
                } else if (attributes.contains(Attribute.MAIL_URL_LITERAL)) {
                    checkDuplicateMailAccount(mailAccount, new TIntHashSet(new int[] {mailAccount.getId()}), user, cid, con);
                }

                if (prepareURL(attributes, Attribute.TRANSPORT_URL_ATTRIBUTES, Attribute.TRANSPORT_URL_LITERAL)) {
                    if (null == storageVersion) {
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
                    checkDuplicateTransportAccount(mailAccount, new TIntHashSet(new int[] {mailAccount.getId()}), user, cid, con);
                } else if (attributes.contains(Attribute.TRANSPORT_URL_LITERAL)) {
                    checkDuplicateTransportAccount(mailAccount, new TIntHashSet(new int[] {mailAccount.getId()}), user, cid, con);
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
                            encryptedPassword = encrypt(mailAccount.getPassword(), session);
                            setOptionalString(stmt, pos++, encryptedPassword);
                        } else if (Attribute.PERSONAL_LITERAL == attribute) {
                            final String personal = mailAccount.getPersonal();
                            if (isEmpty(personal)) {
                                stmt.setNull(pos++, TYPE_VARCHAR);
                            } else {
                                stmt.setString(pos++, personal);
                            }
                        } else if (Attribute.REPLY_TO_LITERAL == attribute) {
                            final String replyTo = mailAccount.getReplyTo();
                            if (isEmpty(replyTo)) {
                                stmt.setNull(pos++, TYPE_VARCHAR);
                            } else {
                                stmt.setString(pos++, replyTo);
                            }
                        } else if (Attribute.ARCHIVE_LITERAL == attribute) {
                            final String s = mailAccount.getArchive();
                            if (isEmpty(s)) {
                                stmt.setString(pos++, "");
                            } else {
                                stmt.setString(pos++, s);
                            }
                        } else if (Attribute.ARCHIVE_FULLNAME_LITERAL == attribute) {
                            final String s = mailAccount.getArchiveFullname();
                            if (isEmpty(s)) {
                                stmt.setString(pos++, "");
                            } else {
                                stmt.setString(pos++, MailFolderUtility.prepareMailFolderParam(s).getFullname());
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
                        LOG.debug(new com.openexchange.java.StringAllocator(query.length() + 32).append("Trying to perform SQL update query for attributes ").append(
                            orderedAttributes).append(" :\n").append(query.substring(query.indexOf(':') + 1)));
                    }

                    stmt.executeUpdate();
                    closeSQLStuff(stmt);

                }

                if (UpdateTransportAccountBuilder.needsUpdate(attributes)) {
                    if (orderedAttributes == null) {
                        orderedAttributes = new ArrayList<Attribute>(attributes);
                    }
                    /*
                     * Check existence of transport entry
                     */
                    stmt = con.prepareStatement("SELECT 1 FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?");
                    int pos = 1;
                    stmt.setLong(pos++, cid);
                    stmt.setLong(pos++, mailAccount.getId());
                    stmt.setLong(pos++, user);
                    rs = stmt.executeQuery();
                    final boolean exists = rs.next();
                    closeSQLStuff(rs, stmt);

                    if (exists) {
                        final UpdateTransportAccountBuilder sqlBuilder = new UpdateTransportAccountBuilder();
                        for (final Attribute attribute : orderedAttributes) {
                            attribute.doSwitch(sqlBuilder);
                        }

                        stmt = con.prepareStatement(sqlBuilder.getUpdateQuery());

                        final GetSwitch getter = new GetSwitch(mailAccount);
                        pos = 1;
                        for (final Attribute attribute : orderedAttributes) {
                            if (!sqlBuilder.handles(attribute)) {
                                continue;
                            }
                            final Object value = attribute.doSwitch(getter);
                            if (Attribute.TRANSPORT_PASSWORD_LITERAL == attribute) {
                                if (encryptedPassword == null) {
                                    encryptedPassword = encrypt(mailAccount.getPassword(), session);
                                }
                                setOptionalString(stmt, pos++, encryptedPassword);
                            } else if (Attribute.TRANSPORT_LOGIN_LITERAL == attribute) {
                                setOptionalString(stmt, pos++, (String) value);
                            } else if (Attribute.TRANSPORT_URL_LITERAL == attribute) {
                                setOptionalString(stmt, pos++, (String) value);
                            } else if (Attribute.PERSONAL_LITERAL == attribute) {
                                final String personal = mailAccount.getPersonal();
                                if (isEmpty(personal)) {
                                    stmt.setNull(pos++, TYPE_VARCHAR);
                                } else {
                                    stmt.setString(pos++, personal);
                                }
                            } else if (Attribute.REPLY_TO_LITERAL == attribute) {
                                final String replyTo = mailAccount.getReplyTo();
                                if (isEmpty(replyTo)) {
                                    stmt.setNull(pos++, TYPE_VARCHAR);
                                } else {
                                    stmt.setString(pos++, replyTo);
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
                            LOG.debug(new com.openexchange.java.StringAllocator(query.length() + 32).append("Trying to perform SQL update query for attributes ").append(
                                orderedAttributes).append(" :\n").append(query.substring(query.indexOf(':') + 1)));
                        }

                        stmt.executeUpdate();
                        closeSQLStuff(stmt);
                    } else {
                        /*
                         * Such an entry does not exist
                         */
                        final String transportURL = mailAccount.generateTransportServerURL();
                        if (null != transportURL) {
                            stmt.close();
                            final String encryptedTransportPassword;
                            if (session == null) {
                                encryptedTransportPassword = null;
                            } else {
                                encryptedTransportPassword = encrypt(mailAccount.getPassword(), session);
                            }
                            // cid, id, user, name, url, login, password, send_addr, default_flag
                            stmt = con.prepareStatement(INSERT_TRANSPORT_ACCOUNT);
                            pos = 1;
                            stmt.setLong(pos++, cid);
                            stmt.setLong(pos++, mailAccount.getId());
                            stmt.setLong(pos++, user);
                            setOptionalString(stmt, pos++, mailAccount.getName());
                            stmt.setString(pos++, transportURL);
                            if (null == mailAccount.getTransportLogin()) {
                                stmt.setString(pos++, "");
                            } else {
                                stmt.setString(pos++, mailAccount.getTransportLogin());
                            }
                            setOptionalString(stmt, pos++, encryptedTransportPassword);
                            setOptionalString(stmt, pos++, mailAccount.getPrimaryAddress());
                            stmt.setInt(pos++, 0); // default flag
                            final String personal = mailAccount.getPersonal();
                            if (isEmpty(personal)) {
                                stmt.setNull(pos++, TYPE_VARCHAR);
                            } else {
                                stmt.setString(pos++, personal);
                            }
                            final String replyTo = mailAccount.getReplyTo();
                            if (isEmpty(replyTo)) {
                                stmt.setNull(pos++, TYPE_VARCHAR);
                            } else {
                                stmt.setString(pos++, replyTo);
                            }

                            if (LOG.isDebugEnabled()) {
                                final String query = stmt.toString();
                                LOG.debug(new com.openexchange.java.StringAllocator(query.length() + 32).append("Trying to perform SQL insert query for attributes ").append(
                                    orderedAttributes).append(" :\n").append(query.substring(query.indexOf(':') + 1)));
                            }

                            stmt.executeUpdate();
                            closeSQLStuff(stmt);
                        }
                    }
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
            } catch (final SQLException e) {
                if (null != stmt && LOG.isDebugEnabled()) {
                    final String sql = stmt.toString();
                    LOG.debug(new com.openexchange.java.StringAllocator().append("\n\tFailed mail account statement:\n\t").append(sql.substring(sql.indexOf(": ") + 2)).toString());
                }
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(rs, stmt);
            }
            /*
             * Automatically check Unified Mail existence
             */
            if (attributes.contains(Attribute.UNIFIED_INBOX_ENABLED_LITERAL) && mailAccount.isUnifiedINBOXEnabled()) {
                final UnifiedInboxManagement management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
                if (null != management && !management.exists(user, cid, con)) {
                    management.createUnifiedINBOX(user, cid, con);
                }
            }
        }
    }

    private void updateUnifiedINBOXEnabled(final boolean unifiedINBOXEnabled, final int id, final int user, final int cid, final Connection con) throws OXException {
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
            if (null != stmt && LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new com.openexchange.java.StringAllocator().append("\n\tFailed mail account statement:\n\t").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updatePersonal(final String personal, final int id, final int user, final int cid, final Connection con) throws OXException {
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
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updateReplyTo(final String replyTo, final int id, final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user_mail_account SET replyTo = ? WHERE cid = ? AND id = ? AND user = ?");
            int pos = 1;
            if (null == replyTo) {
                stmt.setNull(pos++, TYPE_VARCHAR);
            } else {
                stmt.setString(pos++, replyTo);
            }
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, id);
            stmt.setInt(pos++, user);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            // Transport table, too
            stmt = con.prepareStatement("UPDATE user_transport_account SET replyTo = ? WHERE cid = ? AND id = ? AND user = ?");
            pos = 1;
            if (null == replyTo) {
                stmt.setNull(pos++, TYPE_VARCHAR);
            } else {
                stmt.setString(pos++, replyTo);
            }
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, id);
            stmt.setInt(pos++, user);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updateArchive(final String archive, final int id, final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user_mail_account SET archive = ? WHERE cid = ? AND id = ? AND user = ?");
            int pos = 1;
            if (null == archive) {
                stmt.setNull(pos++, TYPE_VARCHAR);
            } else {
                stmt.setString(pos++, archive);
            }
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, id);
            stmt.setInt(pos++, user);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void updateArchiveFullName(final String archiveFullName, final int id, final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE user_mail_account SET archive_fullname = ? WHERE cid = ? AND id = ? AND user = ?");
            int pos = 1;
            if (null == archiveFullName) {
                stmt.setNull(pos++, TYPE_VARCHAR);
            } else {
                stmt.setString(pos++, archiveFullName);
            }
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, id);
            stmt.setInt(pos++, user);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
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

    @Override
    public void updateMailAccount(final MailAccountDescription mailAccount, final int user, final int cid, final Session session) throws OXException {
        if (mailAccount.isDefaultFlag() || MailAccount.DEFAULT_ID == mailAccount.getId()) {
            throw MailAccountExceptionCodes.NO_DEFAULT_UPDATE.create(I(user), I(cid));
        }
        // Check name
        final String name = mailAccount.getName();
        if (!isValid(name)) {
            throw MailAccountExceptionCodes.INVALID_NAME.create(name);
        }
        dropPOP3StorageFolders(user, cid);
        final Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        try {
            checkDuplicateMailAccount(mailAccount, new TIntHashSet(new int[] {mailAccount.getId()}), user, cid, con);
            checkDuplicateTransportAccount(mailAccount, new TIntHashSet(new int[] {mailAccount.getId()}), user, cid, con);
            con.setAutoCommit(false);
            {
                final String encryptedPassword = encrypt(mailAccount.getPassword(), session);
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
                final String replyTo = mailAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
                }
                stmt.setLong(pos++, cid);
                stmt.setLong(pos++, mailAccount.getId());
                stmt.setLong(pos++, user);
                stmt.executeUpdate();
            }
            final String transportURL = mailAccount.generateTransportServerURL();
            if (null != transportURL) {
                final String encryptedTransportPassword = encrypt(mailAccount.getTransportPassword(), session);
                stmt.close();
                stmt = con.prepareStatement(UPDATE_TRANSPORT_ACCOUNT);
                int pos = 1;
                stmt.setString(pos++, name);
                stmt.setString(pos++, transportURL);
                setOptionalString(stmt, pos++, mailAccount.getTransportLogin());
                setOptionalString(stmt, pos++, encryptedTransportPassword);
                stmt.setString(pos++, mailAccount.getPrimaryAddress());
                final String personal = mailAccount.getPersonal();
                if (isEmpty(personal)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, personal);
                }
                final String replyTo = mailAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
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
             * Automatically check Unified Mail existence
             */
            if (mailAccount.isUnifiedINBOXEnabled()) {
                final UnifiedInboxManagement management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
                if (null != management && !management.exists(user, cid, con)) {
                    management.createUnifiedINBOX(user, cid, con);
                }
            }
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            autocommit(con);
            Database.back(cid, true, con);
        }
    }

    @Override
    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final Session session, final Connection con) throws OXException {
        final int cid = ctx.getContextId();
        final boolean isUnifiedMail = mailAccount.getMailProtocol().startsWith(UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX, 0);
        final String primaryAddress = mailAccount.getPrimaryAddress();
        final String name = mailAccount.getName();
        if (!isUnifiedMail) {
            // Check for duplicate
            if (-1 != getByPrimaryAddress(primaryAddress, user, cid, con)) {
                throw MailAccountExceptionCodes.CONFLICT_ADDR.create(primaryAddress, I(user), I(cid));
            }
            checkDuplicateMailAccount(mailAccount, null, user, cid, con);
            checkDuplicateTransportAccount(mailAccount, null, user, cid, con);
            // Check name
            if (!isValid(name)) {
                throw MailAccountExceptionCodes.INVALID_NAME.create(name);
            }
        }
        dropPOP3StorageFolders(user, cid);
        // Get ID
        final int id;
        if (mailAccount.isDefaultFlag()) {
            try {
                getDefaultMailAccount(user, cid, con);
                throw MailAccountExceptionCodes.NO_DUPLICATE_DEFAULT.create();
            } catch (final OXException e) {
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
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            }
        }
        PreparedStatement stmt = null;
        try {
            {
                stmt = con.prepareStatement(INSERT_MAIL_ACCOUNT);
                final String encryptedPassword;
                if (session == null) {
                    encryptedPassword = null;
                } else {
                    encryptedPassword = encrypt(mailAccount.getPassword(), session);
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
                    final String[] defaultFolderFullnames = defaultFolderNamesProvider.getDefaultFolderFullnames(mailAccount, true);
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
                final String replyTo = mailAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
                }
                /*
                 * Archive
                 */
                setOptionalString(stmt, pos++, mailAccount.getArchive());
                setOptionalString(stmt, pos++, mailAccount.getArchiveFullname());
                stmt.executeUpdate();
            }
            final String transportURL = mailAccount.generateTransportServerURL();
            if (null != transportURL) {
                stmt.close();
                final String encryptedTransportPassword;
                if (session == null) {
                    encryptedTransportPassword = null;
                } else {
                    encryptedTransportPassword = encrypt(mailAccount.getTransportPassword(), session);
                }
                // cid, id, user, name, url, login, password, send_addr, default_flag
                stmt = con.prepareStatement(INSERT_TRANSPORT_ACCOUNT);
                int pos = 1;
                stmt.setLong(pos++, cid);
                stmt.setLong(pos++, id);
                stmt.setLong(pos++, user);
                stmt.setString(pos++, name);
                stmt.setString(pos++, transportURL);
                if (null == mailAccount.getTransportLogin()) {
                    stmt.setString(pos++, "");
                } else {
                    stmt.setString(pos++, mailAccount.getTransportLogin());
                }
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
                final String replyTo = mailAccount.getReplyTo();
                if (isEmpty(replyTo)) {
                    stmt.setNull(pos++, TYPE_VARCHAR);
                } else {
                    stmt.setString(pos++, replyTo);
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
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
        /*
         * Automatically check Unified Mail existence
         */
        if (mailAccount.isUnifiedINBOXEnabled()) {
            final UnifiedInboxManagement management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
            if (null != management && !management.exists(user, cid, con)) {
                management.createUnifiedINBOX(user, cid, con);
            }
        }
        return id;
    }

    @Override
    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final Session session) throws OXException {
        final int cid = ctx.getContextId();
        final Connection con = Database.get(cid, true);
        final int retval;
        try {
            con.setAutoCommit(false);
            retval = insertMailAccount(mailAccount, user, ctx, session, con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final Exception e) {
            rollback(con);
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(cid, true, con);
        }
        return retval;
    }

    @Override
    public int[] getByHostNames(final Collection<String> hostNames, final int user, final int cid) throws OXException {
        final Connection con = Database.get(cid, false);
        try {
            return getByHostNames(hostNames, user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    private int[] getByHostNames(final Collection<String> hostNames, final int user, final int cid, final Connection con) throws OXException {
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
            final TIntList ids = new TIntArrayList(6);
            do {
                tmp.parseMailServerURL(result.getString(2));
                if (set.contains(tmp.getMailServer().toLowerCase(Locale.ENGLISH))) {
                    ids.add(result.getInt(1));
                }
            } while (result.next());
            if (ids.isEmpty()) {
                return new int[0];
            }
            final int[] array = ids.toArray();
            Arrays.sort(array);
            return array;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public int getByPrimaryAddress(final String primaryAddress, final int user, final int cid) throws OXException {
        final Connection con = Database.get(cid, false);
        try {
            return getByPrimaryAddress(primaryAddress, user, cid, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    private int getByPrimaryAddress(final String primaryAddress, final int user, final int cid, final Connection con) throws OXException {
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
                throw MailAccountExceptionCodes.CONFLICT_ADDR.create(primaryAddress, I(user), I(cid));
            }
            return id;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void checkDuplicateMailAccount(final MailAccountDescription mailAccount, final TIntSet excepts, final int user, final int cid, final Connection con) throws OXException {
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
                addr = InetAddress.getByName(IDNA.toASCII(server));
            } catch (final UnknownHostException e) {
                LOG.warn(e.getMessage(), e);
                addr = null;
            }
            final int port = mailAccount.getMailPort();
            final String login = mailAccount.getLogin();
            do {
                final int id = (int) result.getLong(1);
                if (null == excepts || !excepts.contains(id)) {
                    final AbstractMailAccount current = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount();
                    final String url = result.getString(2);
                    if (null != url) {
                        current.parseMailServerURL(url);
                        if (checkMailServer(server, addr, current) && checkProtocol(mailAccount.getMailProtocol(), current.getMailProtocol()) && current.getMailPort() == port && (null != login && login.equals(result.getString(3)))) {
                            throw MailAccountExceptionCodes.DUPLICATE_MAIL_ACCOUNT.create(I(user), I(cid));
                        }
                    }
                }
            } while (result.next());
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
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
            return addr.equals(InetAddress.getByName(IDNA.toASCII(mailServer)));
        } catch (final UnknownHostException e) {
            LOG.warn(e.getMessage(), e);
            /*
             * Check by server string
             */
            return server.equalsIgnoreCase(mailServer);
        }
    }

    private static boolean checkProtocol(final String protocol1, final String protocol2) {
        if (isEmpty(protocol1) || isEmpty(protocol2)) {
            return false;
        }
        return protocol1.equalsIgnoreCase(protocol2);
    }

    private void checkDuplicateTransportAccount(final MailAccountDescription mailAccount, final TIntSet excepts, final int user, final int cid, final Connection con) throws OXException {
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
                addr = InetAddress.getByName(IDNA.toASCII(server));
            } catch (final UnknownHostException e) {
                LOG.warn(e.getMessage(), e);
                addr = null;
            }
            final int port = mailAccount.getTransportPort();
            String login = mailAccount.getTransportLogin();
            if (null == login) {
                login = mailAccount.getLogin();
            }
            do {
                final int id = (int) result.getLong(1);
                if (null == excepts || !excepts.contains(id)) {
                    final AbstractMailAccount current = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount();
                    current.parseTransportServerURL(result.getString(2));
                    if (checkTransportServer(server, addr, current) && checkProtocol(mailAccount.getTransportProtocol(), current.getTransportProtocol()) && current.getTransportPort() == port && (null != login && login.equals(result.getString(3)))) {
                        throw MailAccountExceptionCodes.DUPLICATE_TRANSPORT_ACCOUNT.create(I(user), I(cid));
                    }
                }
            } while (result.next());
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
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
            return addr.equals(InetAddress.getByName(IDNA.toASCII(transportServer)));
        } catch (final UnknownHostException e) {
            LOG.warn(e.getMessage(), e);
            /*
             * Check by server string
             */
            return server.equalsIgnoreCase(transportServer);
        }
    }

    @Override
    public MailAccount getTransportAccountForID(final int id, final int user, final int cid) throws OXException {
        final MailAccount account = getMailAccount(id, user, cid);
        if (null == account.getTransportServer()) {
            return getDefaultMailAccount(user, cid);
        }
        return account;
    }

    @Override
    public boolean hasAccounts(final Session session) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int cid = session.getContextId();
        final int user = session.getUserId();
        try {
            con = Database.get(cid, false);
            stmt = con.prepareStatement(SELECT_EXISTS_FOR_USER1);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
            DBUtils.closeSQLStuff(rs, stmt);

            stmt = con.prepareStatement(SELECT_EXISTS_FOR_USER2);
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (con != null) {
                Database.back(cid, false, con);
            }
        }
    }

    @Override
    public void migratePasswords(final String oldSecret, final String newSecret, final Session session) throws OXException {
        // Clear possible cached MailAccess instances
        final int cid = session.getContextId();
        final int user = session.getUserId();
        cleanUp(user, cid);
        // Migrate password
        Connection con = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con = Database.get(cid, true);
            con.setAutoCommit(false); // BEGIN
            rollback = true;
            /*
             * Perform SELECT query
             */
            selectStmt = con.prepareStatement(SELECT_PASSWORD1);
            selectStmt.setInt(1, cid);
            selectStmt.setInt(2, user);
            rs = selectStmt.executeQuery();
            // Gather needed services
            final SecretEncryptionService<GenericProperty> encryptionService = ServerServiceRegistry.getInstance().getService(SecretEncryptionFactoryService.class).createService(STRATEGY);
            if (null == encryptionService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SecretEncryptionService.class.getName());
            }
            final CustomMailAccount parser = new CustomMailAccount();
            // Iterate mail accounts
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id != MailAccount.DEFAULT_ID) {
                        final String login = rs.getString(3);
                        parser.parseMailServerURL(rs.getString(4));
                        final String mailServer = parser.getMailServer();
                        try {
                            // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                            encryptionService.decrypt(session, password, new GenericProperty(id, session, login, mailServer));
                        } catch (final OXException x) {
                            // Decrypt with old -- encrypt with new
                            // We couldn't decrypt the password, so, let's try the oldSecret and do the migration
                            final SetableSession setableSession = SetableSessionFactory.getFactory().setableSessionFor(session);
                            setableSession.setPassword(oldSecret);
                            final String decrypted = encryptionService.decrypt(setableSession, password, new GenericProperty(id, setableSession, login, mailServer));
                            // Encrypt with new secret
                            final String transcribed = encryptionService.encrypt(session, decrypted);
                            // Add to batch update
                            if (null == updateStmt) {
                                updateStmt = con.prepareStatement(UPDATE_PASSWORD1);
                                updateStmt.setInt(2, cid);
                                updateStmt.setInt(4, user);
                            }
                            updateStmt.setString(1, transcribed);
                            updateStmt.setInt(3, id);
                            updateStmt.addBatch();
                        }
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            /*
             * Close stuff
             */
            DBUtils.closeSQLStuff(rs, selectStmt);
            /*
             * Perform other SELECT query
             */
            selectStmt = con.prepareStatement(SELECT_PASSWORD2);
            selectStmt.setInt(1, cid);
            selectStmt.setInt(2, user);
            rs = selectStmt.executeQuery();
            // Iterate transport accounts
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id != MailAccount.DEFAULT_ID) {
                        final String login = rs.getString(3);
                        parser.parseTransportServerURL(rs.getString(4));
                        final String transportServer = parser.getTransportServer();
                        try {
                            // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                            encryptionService.decrypt(session, password, new GenericProperty(id, session, login, transportServer));
                        } catch (final OXException x) {
                            // Decrypt with old -- encrypt with new
                            // We couldn't decrypt the password, so, let's try the oldSecret and do the migration
                            final SetableSession setableSession = SetableSessionFactory.getFactory().setableSessionFor(session);
                            setableSession.setPassword(oldSecret);
                            final String decrypted = encryptionService.decrypt(setableSession, password, new GenericProperty(id, setableSession, login, transportServer));
                            // Encrypt with new secret
                            final String transcribed = encryptionService.encrypt(session, decrypted);
                            // Add to batch update
                            if (null == updateStmt) {
                                updateStmt = con.prepareStatement(UPDATE_PASSWORD1);
                                updateStmt.setInt(2, cid);
                                updateStmt.setInt(4, user);
                            }
                            updateStmt.setString(1, transcribed);
                            updateStmt.setInt(3, id);
                            updateStmt.addBatch();
                        }
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            con.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(rs, selectStmt);
            DBUtils.closeSQLStuff(updateStmt);
            if (con != null) {
                DBUtils.autocommit(con);
                Database.back(cid, true, con);
            }
        }
    }

    @Override
    public void cleanUp(final String secret, final Session session) throws OXException {
        final int user = session.getUserId();
        final int cid = session.getContextId();
        // Clear possible cached MailAccess instances
        cleanUp(user, cid);
        // Migrate password
        Connection con = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        boolean modified = false;
        try {
            con = Database.get(cid, true);
            con.setAutoCommit(false); // BEGIN
            rollback = true;
            /*
             * Perform SELECT query
             */
            selectStmt = con.prepareStatement(SELECT_PASSWORD1);
            selectStmt.setInt(1, cid);
            selectStmt.setInt(2, user);
            rs = selectStmt.executeQuery();
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id != MailAccount.DEFAULT_ID) {
                        try {
                            // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                            MailPasswordUtil.decrypt(password, secret);
                        } catch (final GeneralSecurityException x) {
                            // We couldn't decrypt
                            if (null == updateStmt) {
                                updateStmt = con.prepareStatement(UPDATE_PASSWORD1);
                                updateStmt.setInt(2, cid);
                                updateStmt.setInt(4, user);
                            }
                            updateStmt.setString(1, "");
                            updateStmt.setInt(3, id);
                            updateStmt.addBatch();
                        }
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                modified = true;
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            /*
             * Close stuff
             */
            DBUtils.closeSQLStuff(rs, selectStmt);
            /*
             * Perform other SELECT query
             */
            selectStmt = con.prepareStatement(SELECT_PASSWORD2);
            selectStmt.setInt(1, cid);
            selectStmt.setInt(2, user);
            rs = selectStmt.executeQuery();
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id == MailAccount.DEFAULT_ID) {
                        continue;
                    }
                    try {
                        // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                        MailPasswordUtil.decrypt(password, secret);
                    } catch (final GeneralSecurityException x) {
                        // We couldn't decrypt
                        if (null == updateStmt) {
                            updateStmt = con.prepareStatement(UPDATE_PASSWORD2);
                            updateStmt.setInt(2, cid);
                            updateStmt.setInt(4, user);
                        }
                        updateStmt.setString(1, "");
                        updateStmt.setInt(3, id);
                        updateStmt.addBatch();
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                modified = false;
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            con.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(rs, selectStmt);
            DBUtils.closeSQLStuff(updateStmt);
            if (con != null) {
                DBUtils.autocommit(con);
                if (modified) {
                    Database.getDatabaseService().backWritable(cid, con);
                } else {
                    Database.getDatabaseService().backWritableAfterReading(cid, con);
                }
            }
        }
    }

    @Override
    public void removeUnrecoverableItems(final String secret, final Session session) throws OXException {
        final int user = session.getUserId();
        final int cid = session.getContextId();
        // Clear possible cached MailAccess instances
        cleanUp(user, cid);
        // Migrate password
        Connection con = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            con = Database.get(cid, true);
            con.setAutoCommit(false); // BEGIN
            rollback = true;
            /*
             * Perform SELECT query
             */
            selectStmt = con.prepareStatement(SELECT_PASSWORD1);
            selectStmt.setInt(1, cid);
            selectStmt.setInt(2, user);
            rs = selectStmt.executeQuery();
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id != MailAccount.DEFAULT_ID) {
                        try {
                            // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                            MailPasswordUtil.decrypt(password, secret);
                        } catch (final GeneralSecurityException x) {
                            // We couldn't decrypt
                            if (null == updateStmt) {
                                updateStmt = con.prepareStatement(DELETE_MAIL_ACCOUNT);
                                updateStmt.setInt(1, cid);
                                updateStmt.setInt(3, user);
                            }
                            updateStmt.setInt(2, id);
                            updateStmt.addBatch();
                        }
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            /*
             * Close stuff
             */
            DBUtils.closeSQLStuff(rs, selectStmt);
            /*
             * Perform other SELECT query
             */
            selectStmt = con.prepareStatement(SELECT_PASSWORD2);
            selectStmt.setInt(1, cid);
            selectStmt.setInt(2, user);
            rs = selectStmt.executeQuery();
            while (rs.next()) {
                final String password = rs.getString(2);
                if (!isEmpty(password)) {
                    final int id = rs.getInt(1);
                    if (id == MailAccount.DEFAULT_ID) {
                        continue;
                    }
                    try {
                        // If we can decrypt the password with the newSecret, we don't need to do anything about this account
                        MailPasswordUtil.decrypt(password, secret);
                    } catch (final GeneralSecurityException x) {
                        // We couldn't decrypt
                        if (null == updateStmt) {
                            updateStmt = con.prepareStatement(DELETE_TRANSPORT_ACCOUNT);
                            updateStmt.setInt(1, cid);
                            updateStmt.setInt(3, user);
                        }
                        updateStmt.setInt(2, id);
                        updateStmt.addBatch();
                    }
                }
            }
            if (null != updateStmt) {
                updateStmt.executeBatch();
                DBUtils.closeSQLStuff(updateStmt);
                updateStmt = null;
            }
            con.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con);
            }
            DBUtils.closeSQLStuff(rs, selectStmt);
            DBUtils.closeSQLStuff(updateStmt);
            if (con != null) {
                DBUtils.autocommit(con);
                Database.back(cid, true, con);
            }
        }
        cleanUp(user, cid);
    }

    private void cleanUp(final int user, final int cid) {
        final SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null != service) {
            final Session session = service.getAnyActiveSessionForUser(user, cid);
            if (null != session) {
                try {
                    final IMailAccessCache mac = MailAccess.getMailAccessCache();
                    final int[] ids = getUserMailAccountIDs(user, cid);
                    for (final int id : ids) {
                        while (mac.removeMailAccess(session, id) != null) {
                            // Nope...
                        }
                    }
                } catch (final Exception exc) {
                    LOG.error("Unable to clear cached mail accesses.", exc);
                }
            }
        }
    }

    private String encrypt(final String toCrypt, final Session session) throws OXException {
        if (null == toCrypt) {
            return null;
        }
        final SecretEncryptionService<GenericProperty> encryptionService = getService(SecretEncryptionFactoryService.class).createService(STRATEGY);
        return encryptionService.encrypt(session, toCrypt);
    }

    private static <S> S getService(final Class<? extends S> clazz) {
        return ServerServiceRegistry.getInstance().getService(clazz);
    }

    /*-
     * ++++++++++++++++++++++++++++++++++++ UTILITY METHOD(S) ++++++++++++++++++++++++++++++++++++
     */

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

    private static void setOptionalString(final PreparedStatement stmt, final int pos, final String string) throws SQLException {
        stmt.setString(pos, null == string ? "" : string);
    }

    private static String getOptionalString(final String string) {
        return (null == string || 0 == string.length()) ? null : string;
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
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

        final int len = name.length();
        boolean valid = true;
        boolean isWhitespace = true;
        for (int i = 0; valid && i < len; i++) {
            final char c = name.charAt(i);
            valid = (Arrays.binarySearch(CHARS_INVALID, c) < 0);
            isWhitespace = Strings.isWhitespace(c);
        }
        return !isWhitespace && valid;
    }

}
