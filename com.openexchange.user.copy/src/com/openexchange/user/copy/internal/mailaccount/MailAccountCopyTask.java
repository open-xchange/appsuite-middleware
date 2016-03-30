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

package com.openexchange.user.copy.internal.mailaccount;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link MailAccountCopyTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountCopyTask implements CopyUserTaskService {

    /**
     * Initializes a new {@link MailAccountCopyTask}.
     */
    public MailAccountCopyTask() {
        super();
    }

    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName()
        };
    }

    @Override
    public String getObjectName() {
        return MailAccount.class.getName();
    }

    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
        // Nothing to do
    }

    @Override
    public ObjectMapping<?> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final int srcContextId = copyTools.getSourceContextId().intValue();
        final int dstContextId = copyTools.getDestinationContextId().intValue();
        final int srcUserId = copyTools.getSourceUserId().intValue();
        final int dstUserId = copyTools.getDestinationUserId().intValue();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();
        /*
         * Read existing source mail/transport accounts
         */
        final TIntObjectMap<MailAccountData> srcMailAccounts = readMailAccounts(srcUserId, srcContextId, srcCon);
        final TIntObjectMap<TransportAccountData> srcTransportAccounts = readTransportAccounts(srcUserId, srcContextId, srcCon);
        /*
         * Special handling for default account
         */
        final MailAccountData defaultAccountData = srcMailAccounts.remove(0);
        if (null != defaultAccountData) {
            writeDataToDB(0, defaultAccountData, srcTransportAccounts.remove(0), dstUserId, dstContextId, dstCon);
        }
        /*
         * Get (sorted) mail account identifiers
         *
         * Every mail account has an optional transport account
         */
        final int[] ids = srcMailAccounts.keys();
        Arrays.sort(ids);
        for (final int id : ids) {
            final MailAccountData accountData = srcMailAccounts.remove(id);
            if (null != accountData) {
                writeDataToDB(-1, accountData, srcTransportAccounts.remove(id), dstUserId, dstContextId, dstCon);
            }
        }
        return null;
    }

    private void writeDataToDB(final int newId, final MailAccountData data, final TransportAccountData optData, final int user, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO user_mail_account " +
            		"(id, cid, user, name, url, login, password, primary_addr, default_flag, " +
            		"trash, sent, drafts, spam, confirmed_spam, confirmed_ham, " +
            		"spam_handler, unified_inbox, " +
            		"trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname, " +
            		"personal, replyTo) " +
            		"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            int nid = newId;
            if (nid < 0) {
                nid = IDGenerator.getId(contextId, com.openexchange.groupware.Types.MAIL_SERVICE, con);
            }
            int pos = 1;
            stmt.setInt(pos++, nid);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, user);
            stmt.setString(pos++, notNull(data.getName()));
            stmt.setString(pos++, notNull(data.getUrl()));
            stmt.setString(pos++, notNull(data.getLogin()));
            setStringOrNull(pos++, data.getPassword(), stmt);
            stmt.setString(pos++, notNull(data.getPrimaryAddr()));
            final int defaultFlag = data.isDefaultFlag() ? 1 : 0;
            stmt.setInt(pos++, defaultFlag);

            stmt.setString(pos++, notNull(data.getTrash()));
            stmt.setString(pos++, notNull(data.getSent()));
            stmt.setString(pos++, notNull(data.getDrafts()));
            stmt.setString(pos++, notNull(data.getSpam()));
            stmt.setString(pos++, notNull(data.getConfirmedSpam()));
            stmt.setString(pos++, notNull(data.getConfirmedHam()));

            setStringOrNull(pos++, data.getSpamHandler(), stmt);
            stmt.setInt(pos++, data.isUnifiedInbox() ? 1 : 0);

            stmt.setString(pos++, notNull(data.getTrashFullname()));
            stmt.setString(pos++, notNull(data.getSentFullname()));
            stmt.setString(pos++, notNull(data.getDraftsFullname()));
            stmt.setString(pos++, notNull(data.getSpamFullname()));
            stmt.setString(pos++, notNull(data.getConfirmedSpamFullname()));
            stmt.setString(pos++, notNull(data.getConfirmedHamFullname()));

            setStringOrNull(pos++, data.getPersonal(), stmt);
            setStringOrNull(pos++, data.getReplyTo(), stmt);
            stmt.executeUpdate();
            /*-
             *
             * ------------------ Transport data ------------------
             *
             */
            if (null != optData) {
                DBUtils.closeSQLStuff(stmt);
                stmt = con.prepareStatement("INSERT INTO user_transport_account " +
                        "(id, cid, user, name, url, login, password, send_addr, default_flag, " +
                        "unified_inbox, " +
                        "personal, replyTo) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
                pos = 1;
                stmt.setInt(pos++, nid);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, user);
                stmt.setString(pos++, notNull(optData.getName()));
                stmt.setString(pos++, notNull(optData.getUrl()));
                stmt.setString(pos++, notNull(optData.getLogin()));
                setStringOrNull(pos++, optData.getPassword(), stmt);
                stmt.setString(pos++, notNull(optData.getPrimaryAddr()));
                stmt.setInt(pos++, defaultFlag);

                stmt.setInt(pos++, data.isUnifiedInbox() ? 1 : 0);

                setStringOrNull(pos++, data.getPersonal(), stmt);
                setStringOrNull(pos++, data.getReplyTo(), stmt);
                stmt.executeUpdate();
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static void setStringOrNull(final int pos, final String s, final PreparedStatement stmt) throws SQLException {
        if (null == s) {
            stmt.setNull(pos, java.sql.Types.VARCHAR);
        } else {
            stmt.setString(pos, s);
        }
    }

    private static String notNull(final String s) {
        return null == s ? "" : s;
    }

    private TIntObjectMap<MailAccountData> readMailAccounts(final int user, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, name, url, login, password, primary_addr, default_flag, trash, sent, drafts, spam, confirmed_spam, confirmed_ham, spam_handler, unified_inbox, trash_fullname, sent_fullname, drafts_fullname, spam_fullname, confirmed_spam_fullname, confirmed_ham_fullname, personal, replyTo FROM user_mail_account WHERE cid = ? AND user = ? ORDER BY id");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, user);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return new TIntObjectHashMap<MailAccountData>(1);
            }
            final TIntObjectMap<MailAccountData> ret = new TIntObjectHashMap<MailAccountData>(16);
            do {
                final MailAccountData data = new MailAccountData();
                data.setCid(contextId);
                data.setUser(user);
                data.setConfirmedHam(rs.getString("confirmed_ham"));
                data.setConfirmedSpam(rs.getString("confirmed_spam"));
                data.setConfirmedHamFullname(rs.getString("confirmed_ham_fullname"));
                data.setConfirmedSpamFullname(rs.getString("confirmed_spam_fullname"));
                data.setDefaultFlag(rs.getInt("default_flag") > 0);
                data.setDrafts(rs.getString("drafts"));
                data.setDraftsFullname(rs.getString("drafts_fullname"));
                final int id = rs.getInt("id");
                data.setId(id);
                data.setLogin(rs.getString("login"));
                data.setPassword(rs.getString("password"));
                data.setName(rs.getString("name"));
                data.setPersonal(rs.getString("personal"));
                data.setPrimaryAddr(rs.getString("primary_addr"));
                data.setReplyTo(rs.getString("replyTo"));
                data.setSent(rs.getString("sent"));
                data.setSentFullname(rs.getString("sent_fullname"));
                data.setSpam(rs.getString("spam"));
                data.setSpamFullname(rs.getString("spam_fullname"));
                data.setSpamHandler(rs.getString("spam_handler"));
                data.setTrash(rs.getString("trash"));
                data.setTrashFullname(rs.getString("trash_fullname"));
                data.setUnifiedInbox(rs.getInt("unified_inbox") > 0);
                data.setUrl(rs.getString("url"));
                ret.put(id, data);
            } while (rs.next());
            return ret;
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private TIntObjectMap<TransportAccountData> readTransportAccounts(final int user, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT id, name, url, login, password, send_addr, default_flag, unified_inbox, personal, replyTo FROM user_transport_account WHERE cid = ? AND user = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, user);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return new TIntObjectHashMap<TransportAccountData>(1);
            }
            final TIntObjectMap<TransportAccountData> ret = new TIntObjectHashMap<TransportAccountData>(16);
            do {
                final TransportAccountData data = new TransportAccountData();
                data.setCid(contextId);
                data.setUser(user);
                data.setDefaultFlag(rs.getInt("default_flag") > 0);
                final int id = rs.getInt("id");
                data.setId(id);
                data.setLogin(rs.getString("login"));
                data.setPassword(rs.getString("password"));
                data.setName(rs.getString("name"));
                data.setPersonal(rs.getString("personal"));
                data.setPrimaryAddr(rs.getString("send_addr"));
                data.setReplyTo(rs.getString("replyTo"));
                data.setUnifiedInbox(rs.getInt("unified_inbox") > 0);
                data.setUrl(rs.getString("url"));
                ret.put(id, data);
            } while (rs.next());
            return ret;
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

}
