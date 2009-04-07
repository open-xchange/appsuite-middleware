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
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.Database;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountExceptionFactory;
import com.openexchange.mailaccount.MailAccountExceptionMessages;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.PasswordUtil;
import com.openexchange.tools.Collections.SmartIntArray;

/**
 * {@link RdbMailAccountStorage} - The relational database implementation of mail account storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class RdbMailAccountStorage implements MailAccountStorageService {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(RdbMailAccountStorage.class);

    private static final String SELECT_MAIL_ACCOUNT = "SELECT name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_TRANSPORT_ACCOUNT = "SELECT name, url, login, password, send_addr, default_flag FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String SELECT_MAIL_ACCOUNTS = "SELECT id FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id";

    private static final String SELECT_BY_LOGIN = "SELECT id, user FROM user_mail_account WHERE cid = ? AND login = ?";

    private static final String SELECT_BY_PRIMARY_ADDR = "SELECT id, user FROM user_mail_account WHERE cid = ? AND primary_addr = ?";

    private static final String SELECT_ACCOUNT_BY_PRIMARY_ADDR = "SELECT id FROM user_mail_account WHERE cid = ? AND primary_addr = ? AND user = ?";

    private static final String DELETE_MAIL_ACCOUNT = "DELETE FROM user_mail_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String DELETE_TRANSPORT_ACCOUNT = "DELETE FROM user_transport_account WHERE cid = ? AND id = ? AND user = ?";

    private static final String UPDATE_MAIL_ACCOUNT = "UPDATE user_mail_account SET name = ?, url = ?, login = ?, password = ?, primary_addr = ?, spam_handler = ?, trash = ?, sent = ?, drafts = ?, spam = ?, confirmed_spam = ?, confirmed_ham = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String INSERT_MAIL_ACCOUNT = "INSERT INTO user_mail_account (cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_TRANSPORT_ACCOUNT = "UPDATE user_transport_account SET name = ?, url = ?, login = ?, password = ?, send_addr = ? WHERE cid = ? AND id = ? AND user = ?";

    private static final String INSERT_TRANSPORT_ACCOUNT = "INSERT INTO user_transport_account (cid, id, user, name, url, login, password, send_addr, default_flag) VALUES (?,?,?,?,?,?,?,?,?)";

    private static void fillMailAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
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
            mailAccount.setMailServerURL(result.getString(2));
            mailAccount.setName(result.getString(1));
            final String pw = result.getString(4);
            if (result.wasNull()) {
                mailAccount.setPassword(null);
            } else {
                mailAccount.setPassword(pw);
            }
            mailAccount.setPrimaryAddress(result.getString(5));
            mailAccount.setTrash(result.getString(7));
            mailAccount.setSent(result.getString(8));
            mailAccount.setDrafts(result.getString(9));
            mailAccount.setSpam(result.getString(10));
            mailAccount.setConfirmedSpam(result.getString(11));
            mailAccount.setConfirmedHam(result.getString(12));
            mailAccount.setSpamHandler(result.getString(13));
            mailAccount.setUserId(user);
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            Database.back(cid, false, con);
        }
    }

    private static void fillTransportAccount(final AbstractMailAccount mailAccount, final int id, final int user, final int cid) throws MailAccountException {
        Connection con = null;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_TRANSPORT_ACCOUNT);
            stmt.setLong(1, cid);
            stmt.setLong(2, MailAccount.DEFAULT_ID);
            stmt.setLong(3, user);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NOT_FOUND, I(id), I(user), I(cid));
            }
            mailAccount.setTransportServerURL(result.getString(2));
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            Database.back(cid, false, con);
        }
    }

    /**
     * Initializes a new {@link RdbMailAccountStorage}.
     */
    RdbMailAccountStorage() {
        super();
    }

    public void deleteMailAccount(final int id, final int user, final int cid) throws MailAccountException {
        if (MailAccount.DEFAULT_ID == id) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.NO_DEFAULT_DELETE, I(user), I(cid));
        }
        Connection con = null;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(DELETE_MAIL_ACCOUNT);
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            stmt.executeUpdate();
            stmt.close();
            stmt = con.prepareStatement(DELETE_TRANSPORT_ACCOUNT);
            stmt.setLong(1, cid);
            stmt.setLong(2, id);
            stmt.setLong(3, user);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    public MailAccount getDefaultMailAccount(final int user, final int cid) throws MailAccountException {
        return getMailAccount(MailAccount.DEFAULT_ID, user, cid);
    }

    public MailAccount getMailAccount(final int id, final int user, final int cid) throws MailAccountException {
        final AbstractMailAccount retval = MailAccount.DEFAULT_ID == id ? new DefaultMailAccount() : new CustomMailAccount();
        fillMailAccount(retval, id, user, cid);
        fillTransportAccount(retval, id, user, cid);
        return retval;
    }

    public MailAccount[] getUserMailAccounts(final int user, final int cid) throws MailAccountException {
        final int[] ids;
        {
            Connection con = null;
            try {
                con = Database.get(cid, false);
            } catch (final DBPoolingException e) {
                throw new MailAccountException(e);
            }
            PreparedStatement stmt = null;
            ResultSet result = null;
            final SmartIntArray sia = new SmartIntArray(8);
            try {
                stmt = con.prepareStatement(SELECT_MAIL_ACCOUNTS);
                stmt.setLong(1, cid);
                stmt.setLong(2, user);
                result = stmt.executeQuery();
                if (!result.next()) {
                    return new MailAccount[0];
                }
                do {
                    sia.append(result.getInt(1));
                } while (result.next());
            } catch (final SQLException e) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
                Database.back(cid, false, con);
            }
            ids = sia.toArray();
        }
        final MailAccount[] retval = new MailAccount[ids.length];
        for (int i = 0; i < ids.length; i++) {
            retval[i] = getMailAccount(ids[i], user, cid);
        }
        return retval;
    }

    public MailAccount[] resolveLogin(final String login, final int cid) throws MailAccountException {
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
                    return new MailAccount[0];
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
        final MailAccount[] retval = new MailAccount[ids.length];
        for (int i = 0; i < ids.length; i++) {
            retval[i] = getMailAccount(ids[i], users[i], cid);
        }
        return retval;
    }

    public MailAccount[] resolveLogin(final String login, final InetSocketAddress server, final int cid) throws MailAccountException {
        final MailAccount[] tmp = resolveLogin(login, cid);
        final List<MailAccount> l = new ArrayList<MailAccount>(tmp.length);
        for (int i = 0; i < tmp.length; i++) {
            final MailAccount cur = tmp[i];
            if (server.equals(toSocketAddr(cur.getMailServerURL(), 143))) {
                l.add(cur);
            }
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    public MailAccount[] resolvePrimaryAddr(final String primaryAddress, final InetSocketAddress server, final int cid) throws MailAccountException {
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
                    return new MailAccount[0];
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
        final MailAccount[] tmp = new MailAccount[ids.length];
        for (int i = 0; i < ids.length; i++) {
            tmp[i] = getMailAccount(ids[i], users[i], cid);
        }
        final List<MailAccount> l = new ArrayList<MailAccount>(tmp.length);
        for (int i = 0; i < tmp.length; i++) {
            final MailAccount cur = tmp[i];
            if (server.equals(toSocketAddr(cur.getMailServerURL(), 143))) {
                l.add(cur);
            }
        }
        return l.toArray(new MailAccount[l.size()]);
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
            final String encryptedPassword = PasswordUtil.encrypt(mailAccount.getPassword(), sessionPassword);
            stmt = con.prepareStatement(UPDATE_MAIL_ACCOUNT);
            int pos = 1;
            stmt.setString(pos++, mailAccount.getName());
            stmt.setString(pos++, mailAccount.getMailServerURL());
            stmt.setString(pos++, mailAccount.getLogin());
            stmt.setString(pos++, encryptedPassword);
            stmt.setString(pos++, mailAccount.getPrimaryAddress());
            final String sh = mailAccount.getSpamHandler();
            if (null == sh) {
                stmt.setNull(pos++, Types.VARCHAR);
            } else {
                stmt.setString(pos++, sh);
            }
            stmt.setString(pos++, mailAccount.getTrash());
            stmt.setString(pos++, mailAccount.getSent());
            stmt.setString(pos++, mailAccount.getDrafts());
            stmt.setString(pos++, mailAccount.getSpam());
            stmt.setString(pos++, mailAccount.getConfirmedSpam());
            stmt.setString(pos++, mailAccount.getConfirmedHam());
            stmt.setLong(pos++, cid);
            stmt.setLong(pos++, mailAccount.getId());
            stmt.setLong(pos++, user);
            stmt.executeUpdate();
            stmt.close();
            stmt = con.prepareStatement(UPDATE_TRANSPORT_ACCOUNT);
            pos = 1;
            stmt.setString(pos++, mailAccount.getName());
            stmt.setString(pos++, mailAccount.getTransportServerURL());
            stmt.setString(pos++, mailAccount.getLogin());
            stmt.setString(pos++, encryptedPassword);
            stmt.setString(pos++, mailAccount.getPrimaryAddress());
            stmt.setLong(pos++, cid);
            stmt.setLong(pos++, mailAccount.getId());
            stmt.setLong(pos++, user);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final String sessionPassword) throws MailAccountException {
        final int cid = ctx.getContextId();
        final int id;
        if (mailAccount.isDefaultFlag()) {
            try {
                getDefaultMailAccount(user, cid);
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
                id = IDGenerator.getId(ctx, com.openexchange.groupware.Types.MAIL_SERVICE);
            } catch (final SQLException e) {
                throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
            }
        }
        Connection con = null;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new MailAccountException(e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_MAIL_ACCOUNT);
            final String encryptedPassword = sessionPassword == null ? null : PasswordUtil.encrypt(
                mailAccount.getPassword(),
                sessionPassword);
            int pos = 1;
            // cid, id, user, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam,
            // confirmed_ham, spam_handler
            stmt.setLong(pos++, cid);
            stmt.setLong(pos++, id);
            stmt.setLong(pos++, user);
            stmt.setString(pos++, mailAccount.getName());
            stmt.setString(pos++, mailAccount.getMailServerURL());
            stmt.setString(pos++, mailAccount.getLogin());
            if (mailAccount.isDefaultFlag()) {
                stmt.setNull(pos++, Types.VARCHAR);
            } else {
                stmt.setString(pos++, encryptedPassword);
            }
            stmt.setString(pos++, mailAccount.getPrimaryAddress());
            stmt.setInt(pos++, mailAccount.isDefaultFlag() ? 1 : 0);
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
            stmt.executeUpdate();
            stmt.close();
            // cid, id, user, name, url, login, password, send_addr, default_flag
            stmt = con.prepareStatement(INSERT_TRANSPORT_ACCOUNT);
            pos = 1;
            stmt.setLong(pos++, cid);
            stmt.setLong(pos++, id);
            stmt.setLong(pos++, user);
            stmt.setString(pos++, mailAccount.getName());
            stmt.setString(pos++, mailAccount.getTransportServerURL());
            stmt.setString(pos++, mailAccount.getLogin());
            if (mailAccount.isDefaultFlag()) {
                stmt.setNull(pos++, Types.VARCHAR);
            } else {
                stmt.setString(pos++, encryptedPassword);
            }
            stmt.setString(pos++, mailAccount.getPrimaryAddress());
            stmt.setInt(pos++, mailAccount.isDefaultFlag() ? 1 : 0);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
        return id;
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

}
