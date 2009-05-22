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

package com.openexchange.mailaccount.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.utils.ProviderUtility.toSocketAddr;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.Database;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountExceptionFactory;
import com.openexchange.mailaccount.MailAccountExceptionMessages;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.mailaccount.servlet.fields.GetSwitch;
import com.openexchange.mailaccount.servlet.fields.MailAccountGetSwitch;
import com.openexchange.mailaccount.servlet.fields.SetSwitch;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.PasswordUtil;
import com.openexchange.tools.Collections.SmartIntArray;

/**
 * {@link RdbMailAccountStorage} - The relational database implementation of mail account storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class RdbMailAccountStorage implements MailAccountStorageService {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(RdbMailAccountStorage.class);

    private static final String SELECT_MAIL_ACCOUNT = "SELECT name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, unified_inbox, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_TRANSPORT_ACCOUNT = "SELECT name, url, login, password, send_addr, default_flag FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_MAIL_ACCOUNTS = "SELECT id FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id";

    private static final String SELECT_BY_LOGIN = "SELECT id, user FROM user_mail_account WHERE cid = ? AND login = ?";

    private static final String SELECT_BY_PRIMARY_ADDR = "SELECT id, user FROM user_mail_account WHERE cid = ? AND primary_addr = ?";

    private static final String SELECT_ACCOUNT_BY_PRIMARY_ADDR = "SELECT id FROM user_mail_account WHERE cid = ? AND primary_addr = ? AND user = ?";

    private static final String DELETE_MAIL_ACCOUNT = "DELETE FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String DELETE_TRANSPORT_ACCOUNT = "DELETE FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_MAIL_ACCOUNT = "UPDATE user_mail_account SET name = ?, url = ?, login = ?, password = ?, primary_addr = ?, spam_handler = ?, trash = ?, sent = ?, drafts = ?, spam = ?, confirmed_spam = ?, confirmed_ham = ?, unified_inbox = ?, trash_fullname = ?, sent_fullname = ?, drafts_fullname = ?, spam_fullname = ?, confirmed_spam_fullname = ?, confirmed_ham_fullname = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String INSERT_MAIL_ACCOUNT = "INSERT INTO user_mail_account (cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, unified_inbox, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_TRANSPORT_ACCOUNT = "UPDATE user_transport_account SET name = ?, url = ?, login = ?, password = ?, send_addr = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String INSERT_TRANSPORT_ACCOUNT = "INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag) VALUES (?,?,?,?,?,?,?,?,?)";

    private static final String UPDATE_UNIFIED_INBOX_FLAG = "UPDATE user_mail_account SET unified_inbox = ? WHERE cid = ? AND id = ? AND user = ?";

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
            mailAccount.setUserId(user);
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

    /**
     * Initializes a new {@link RdbMailAccountStorage}.
     */
    RdbMailAccountStorage() {
        super();
    }

    public void deleteMailAccount(final int id, final int user, final int cid) throws MailAccountException {
        deleteMailAccount(id, user, cid, false);
    }

    public void deleteMailAccount(final int id, final int user, final int cid, final boolean deletePrimary) throws MailAccountException {
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
            deleteMailAccount(id, user, cid, deletePrimary, con);
        } finally {
            Database.back(cid, true, con);
        }
    }

    public void deleteMailAccount(final int id, final int user, final int cid, final boolean deletePrimary, final Connection con) throws MailAccountException {
        if (!deletePrimary && MailAccount.DEFAULT_ID == id) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NO_DEFAULT_DELETE, I(user), I(cid));
        }
        PreparedStatement stmt = null;
        try {
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
            closeSQLStuff(stmt);

            deleteProperties(cid, user, id, con);
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
        final AbstractMailAccount retval = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount();
        fillMailAccount(retval, id, user, cid);
        fillTransportAccount(retval, id, user, cid);
        return retval;
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
        for (int i = 0; i < idsAndUsers.length; i++) {
            final int[] idAndUser = idsAndUsers[i];
            final MailAccount candidate = getMailAccount(idAndUser[0], idAndUser[1], cid);
            if (server.equals(toSocketAddr(candidate.generateMailServerURL(), 143))) {
                l.add(candidate);
            }
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    public MailAccount[] resolvePrimaryAddr(final String primaryAddress, final InetSocketAddress server, final int cid) throws MailAccountException {
        final int[][] idsAndUsers = resolvePrimaryAddr2IDs(primaryAddress, cid);
        final List<MailAccount> l = new ArrayList<MailAccount>(idsAndUsers.length);
        for (int i = 0; i < idsAndUsers.length; i++) {
            final int[] idAndUser = idsAndUsers[i];
            final MailAccount candidate = getMailAccount(idAndUser[0], idAndUser[1], cid);
            if (server.equals(toSocketAddr(candidate.generateMailServerURL(), 143))) {
                l.add(candidate);
            }
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
        } catch (SQLException e) {
            rollback(con);
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(cid, true, con);
        }
    }

    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final String sessionPassword, final Connection con, final boolean changePrimary) throws MailAccountException {
        if (!changePrimary && (mailAccount.isDefaultFlag() || MailAccount.DEFAULT_ID == mailAccount.getId())) {
            if (!attributes.contains(Attribute.UNIFIED_INBOX_ENABLED_LITERAL)) {
                /*
                 * An attribute different from Attribute.UNIFIED_INBOX_ENABLED_LITERAL must not be changed
                 */
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NO_DEFAULT_UPDATE, I(user), I(cid));
            }
            /*
             * Ensure only Attribute.UNIFIED_INBOX_ENABLED_LITERAL should really be changed
             */
            final MailAccount storageVersion = getMailAccount(mailAccount.getId(), user, cid, con);
            /*
             * Initialize GET switches
             */
            final MailAccountGetSwitch storageGetSwitch = new MailAccountGetSwitch(storageVersion);
            final GetSwitch getSwitch = new GetSwitch(mailAccount);
            /*
             * Iterate attributes and compare their values except the one for Attribute.UNIFIED_INBOX_ENABLED_LITERAL
             */
            for (final Attribute attribute : attributes) {
                /*
                 * Check for an attribute different from Attribute.UNIFIED_INBOX_ENABLED_LITERAL
                 */
                if (!Attribute.UNIFIED_INBOX_ENABLED_LITERAL.equals(attribute)) {
                    final Object storageValue = attribute.doSwitch(storageGetSwitch);
                    final Object newValue = attribute.doSwitch(getSwitch);
                    if (!storageValue.equals(newValue)) {
                        /*
                         * An attribute different from Attribute.UNIFIED_INBOX_ENABLED_LITERAL must not be changed
                         */
                        throw MailAccountExceptionFactory.getInstance().create(
                            MailAccountExceptionMessages.NO_DEFAULT_UPDATE,
                            I(user),
                            I(cid));
                    }
                }
            }
            /*
             * OK, update UNIFIED_INBOX_ENABLED flag.
             */
            updateUnifiedINBOXEnabled(mailAccount.isUnifiedINBOXEnabled(), MailAccount.DEFAULT_ID, user, cid, con);
            /*
             * Automatically check Unified INBOX enablement
             */
            if (mailAccount.isUnifiedINBOXEnabled()) {
                final UnifiedINBOXManagement management = ServerServiceRegistry.getInstance().getService(UnifiedINBOXManagement.class);
                if (null != management && management.getUnifiedINBOXAccountID(user, cid, con) == -1) {
                    management.createUnifiedINBOX(user, cid, con);
                }
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
                                encryptedPassword = PasswordUtil.encrypt(mailAccount.getPassword(), sessionPassword);
                            } catch (final GeneralSecurityException e) {
                                throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(e, new Object[0]);
                            }
                            stmt.setObject(pos++, encryptedPassword);
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
                                    encryptedPassword = PasswordUtil.encrypt(mailAccount.getTransportPassword(), sessionPassword);
                                } catch (final GeneralSecurityException e) {
                                    throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(e, new Object[0]);
                                }
                            }
                            stmt.setObject(pos++, encryptedPassword);
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
            } catch (final SQLException e) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
            } finally {
                closeSQLStuff(null, stmt);
            }
            /*
             * Automatically check Unified INBOX enablement
             */
            if (attributes.contains(Attribute.UNIFIED_INBOX_ENABLED_LITERAL) && mailAccount.isUnifiedINBOXEnabled()) {
                final UnifiedINBOXManagement management = ServerServiceRegistry.getInstance().getService(UnifiedINBOXManagement.class);
                if (null != management && management.getUnifiedINBOXAccountID(user, cid, con) == -1) {
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

    private boolean prepareURL(final Set<Attribute> attributes, final Set<Attribute> compareWith, final Attribute urlAttribute) {
        final EnumSet<Attribute> copy = EnumSet.copyOf(attributes);
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
        Connection con = null;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        PreparedStatement stmt = null;
        try {
            {
                final String encryptedPassword;
                try {
                    encryptedPassword = PasswordUtil.encrypt(mailAccount.getPassword(), sessionPassword);
                } catch (final GeneralSecurityException e) {
                    throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(e, new Object[0]);
                }
                stmt = con.prepareStatement(UPDATE_MAIL_ACCOUNT);
                int pos = 1;
                stmt.setString(pos++, mailAccount.getName());
                stmt.setString(pos++, mailAccount.generateMailServerURL());
                stmt.setString(pos++, mailAccount.getLogin());
                stmt.setString(pos++, encryptedPassword);
                stmt.setString(pos++, mailAccount.getPrimaryAddress());
                final String sh = mailAccount.getSpamHandler();
                if (null == sh) {
                    stmt.setNull(pos++, Types.VARCHAR);
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
                stmt.setLong(pos++, cid);
                stmt.setLong(pos++, mailAccount.getId());
                stmt.setLong(pos++, user);
                stmt.executeUpdate();
            }
            final String transportURL = mailAccount.generateTransportServerURL();
            if (null != transportURL) {
                final String encryptedTransportPassword;
                try {
                    encryptedTransportPassword = PasswordUtil.encrypt(mailAccount.getTransportPassword(), sessionPassword);
                } catch (final GeneralSecurityException e) {
                    throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(e, new Object[0]);
                }
                stmt.close();
                stmt = con.prepareStatement(UPDATE_TRANSPORT_ACCOUNT);
                int pos = 1;
                stmt.setString(pos++, mailAccount.getName());
                stmt.setString(pos++, transportURL);
                stmt.setString(pos++, mailAccount.getLogin());
                stmt.setString(pos++, encryptedTransportPassword);
                stmt.setString(pos++, mailAccount.getPrimaryAddress());
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
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
        /*
         * Automatically check Unified INBOX enablement
         */
        if (mailAccount.isUnifiedINBOXEnabled()) {
            final UnifiedINBOXManagement management = ServerServiceRegistry.getInstance().getService(UnifiedINBOXManagement.class);
            if (null != management && management.getUnifiedINBOXAccountID(user, cid, con) == -1) {
                management.createUnifiedINBOX(user, cid, con);
            }
        }
    }

    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final String sessionPassword, final Connection con) throws MailAccountException {
        final int cid = ctx.getContextId();
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
                        encryptedPassword = PasswordUtil.encrypt(mailAccount.getPassword(), sessionPassword);
                    } catch (final GeneralSecurityException e) {
                        throw MailAccountExceptionMessages.PASSWORD_ENCRYPTION_FAILED.create(e, new Object[0]);
                    }
                }
                int pos = 1;
                // cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam,
                // confirmed_ham, spam_handler
                stmt.setLong(pos++, cid);
                stmt.setLong(pos++, id);
                stmt.setLong(pos++, user);
                stmt.setString(pos++, mailAccount.getName());
                stmt.setString(pos++, mailAccount.generateMailServerURL());
                stmt.setString(pos++, mailAccount.getLogin());
                if (mailAccount.isDefaultFlag()) {
                    stmt.setNull(pos++, Types.VARCHAR);
                } else {
                    stmt.setString(pos++, encryptedPassword);
                }
                stmt.setString(pos++, mailAccount.getPrimaryAddress());
                stmt.setInt(pos++, mailAccount.isDefaultFlag() ? 1 : 0);
                setOptionalString(stmt, pos++, mailAccount.getTrash());
                setOptionalString(stmt, pos++, mailAccount.getSent());
                setOptionalString(stmt, pos++, mailAccount.getDrafts());
                setOptionalString(stmt, pos++, mailAccount.getSpam());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedSpam());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedHam());
                final String sh = mailAccount.getSpamHandler();
                if (null == sh) {
                    stmt.setNull(pos++, Types.VARCHAR);
                } else {
                    stmt.setString(pos++, sh);
                }
                stmt.setInt(pos++, mailAccount.isUnifiedINBOXEnabled() ? 1 : 0);
                setOptionalString(stmt, pos++, mailAccount.getTrashFullname());
                setOptionalString(stmt, pos++, mailAccount.getSentFullname());
                setOptionalString(stmt, pos++, mailAccount.getDraftsFullname());
                setOptionalString(stmt, pos++, mailAccount.getSpamFullname());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedSpamFullname());
                setOptionalString(stmt, pos++, mailAccount.getConfirmedHamFullname());
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
                        encryptedTransportPassword = PasswordUtil.encrypt(mailAccount.getTransportPassword(), sessionPassword);
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
                stmt.setString(pos++, mailAccount.getName());
                stmt.setString(pos++, transportURL);
                stmt.setString(pos++, mailAccount.getTransportLogin());
                if (mailAccount.isDefaultFlag()) {
                    stmt.setNull(pos++, Types.VARCHAR);
                } else {
                    stmt.setString(pos++, encryptedTransportPassword);
                }
                stmt.setString(pos++, mailAccount.getPrimaryAddress());
                stmt.setInt(pos++, mailAccount.isDefaultFlag() ? 1 : 0);
                stmt.executeUpdate();
            }
            // Properties
            final Map<String, String> properties = mailAccount.getProperties();
            if (!properties.isEmpty()) {
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
            }
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
        /*
         * Automatically check Unified INBOX enablement
         */
        if (mailAccount.isUnifiedINBOXEnabled()) {
            final UnifiedINBOXManagement management = ServerServiceRegistry.getInstance().getService(UnifiedINBOXManagement.class);
            if (null != management && management.getUnifiedINBOXAccountID(user, cid, con) == -1) {
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
        } finally {
            autocommit(con);
            Database.back(cid, true, con);
        }
        return retval;
    }

    public int getByPrimaryAddress(final String primaryAddress, final int user, final int cid) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
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
                    MailAccountExceptionMessages.CONFLICT,
                    primaryAddress,
                    I(user),
                    I(cid));
            }
            return id;
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            Database.back(cid, false, con);
        }
    }

    public MailAccount getTransportAccountForID(final int id, final int user, final int cid) throws MailAccountException {
        final MailAccount account = getMailAccount(id, user, cid);
        if (null == account.getTransportServer()) {
            return getDefaultMailAccount(user, cid);
        }
        return account;
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

}
