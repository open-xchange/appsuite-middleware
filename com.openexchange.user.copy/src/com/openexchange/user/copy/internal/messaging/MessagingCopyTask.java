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

package com.openexchange.user.copy.internal.messaging;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.genconf.ConfAttribute;
import com.openexchange.user.copy.internal.genconf.GenconfCopyTool;
import com.openexchange.user.copy.internal.oauth.OAuthAccount;
import com.openexchange.user.copy.internal.oauth.OAuthCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link MessagingCopyTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MessagingCopyTask implements CopyUserTaskService {

    private static final String SELECT_MESSAGING_ACCOUNTS =
        "SELECT serviceId, account, confId, displayName FROM messagingAccount WHERE cid = ? AND user = ?";

    private static final String INSERT_MESSAGING_ACCOUNTS =
        "INSERT INTO messagingAccount (cid, user, serviceId, account, confId, displayName) VALUES (?, ?, ?, ?, ?, ?)";


    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName(),
            OAuthCopyTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return "messaging";
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public ObjectMapping<?> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Integer srcCtxId = copyTools.getSourceContextId();
        final Integer dstCtxId = copyTools.getDestinationContextId();
        final Integer srcUsrId = copyTools.getSourceUserId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();

        try {
            final boolean hasTable = DBUtils.tableExists(srcCon, "messagingAccount");
            if (hasTable) {
                final ObjectMapping<Integer> accountMapping = copyTools.checkAndExtractGenericMapping(OAuthAccount.class.getName());
                final List<MessagingAccount> accounts = loadMessagingAccountsFromDB(srcCon, i(srcCtxId), i(srcUsrId));
                fillMessagingAccountsWithConfig(accounts, srcCon, i(srcCtxId));
                exchangeIds(accounts, dstCon, i(dstCtxId));
                setAccountIdsForMessagingAccounts(accounts, accountMapping);
                writeMessagingAccountsToDB(accounts, dstCon, i(dstCtxId), i(dstUsrId));
            }
        }  catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        }

        return null;
    }

    private void setAccountIdsForMessagingAccounts(final List<MessagingAccount> srcAccounts, final ObjectMapping<Integer> accountMapping) {
        for (final MessagingAccount account : srcAccounts) {
            final List<ConfAttribute> stringAttributes = account.getStringAttributes();
            for (final ConfAttribute attribute : stringAttributes) {
                if (attribute.getName() != null && attribute.getName().equals("account")) {
                    try {
                        final int oldId = Integer.parseInt(attribute.getValue());
                        final Integer newId = accountMapping.getDestination(I(oldId));
                        if (newId != null) {
                            attribute.setValue(String.valueOf(newId));
                        }
                    } catch (final NumberFormatException e) {
                        // Skip this one
                    }

                    break;
                }
            }
        }
    }

    void fillMessagingAccountsWithConfig(final List<MessagingAccount> accounts, final Connection con, final int cid) throws OXException {
        for (final MessagingAccount account : accounts) {
            final int confId = account.getConfId();
            final List<ConfAttribute> boolAttributes = GenconfCopyTool.loadAttributesFromDB(con, confId, cid, ConfAttribute.BOOL);
            final List<ConfAttribute> stringAttributes = GenconfCopyTool.loadAttributesFromDB(con, confId, cid, ConfAttribute.STRING);
            account.setBoolAttributes(boolAttributes);
            account.setStringAttributes(stringAttributes);
        }
    }

    private void exchangeIds(final List<MessagingAccount> accounts, final Connection con, final int cid) throws OXException {
        for (final MessagingAccount account : accounts) {
            try {
                final int newConfigId = IDGenerator.getId(cid, Types.GENERIC_CONFIGURATION, con);
                account.setConfId(newConfigId);
                account.setId(newConfigId);
            } catch (final SQLException e) {
                throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
            }
        }
    }

    List<MessagingAccount> loadMessagingAccountsFromDB(final Connection con, final int cid, final int uid) throws OXException {
        final List<MessagingAccount> accounts = new ArrayList<MessagingAccount>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT_MESSAGING_ACCOUNTS);
            stmt.setInt(1, cid);
            stmt.setInt(2, uid);

            rs = stmt.executeQuery();
            while (rs.next()) {
                int i = 1;
                final MessagingAccount account = new MessagingAccount();
                account.setService(rs.getString(i++));
                account.setId(rs.getInt(i++));
                account.setConfId(rs.getInt(i++));
                account.setDisplayName(rs.getString(i++));

                accounts.add(account);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return accounts;
    }

    void writeMessagingAccountsToDB(final List<MessagingAccount> accounts, final Connection con, final int cid, final int uid) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_MESSAGING_ACCOUNTS);
            for (final MessagingAccount account : accounts) {
                GenconfCopyTool.writeAttributesToDB(account.getBoolAttributes(), con, account.getConfId(), cid, ConfAttribute.BOOL);
                GenconfCopyTool.writeAttributesToDB(account.getStringAttributes(), con, account.getConfId(), cid, ConfAttribute.STRING);

                int i = 1;
                stmt.setInt(i++, cid);
                stmt.setInt(i++, uid);
                stmt.setString(i++, account.getService());
                stmt.setInt(i++, account.getId());
                stmt.setInt(i++, account.getConfId());
                stmt.setString(i++, account.getDisplayName());

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
