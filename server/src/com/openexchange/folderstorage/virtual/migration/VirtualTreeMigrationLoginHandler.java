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

package com.openexchange.folderstorage.virtual.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import com.openexchange.authentication.LoginException;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.database.DBPoolingException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.virtual.sql.Insert;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.server.OXException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link VirtualTreeMigrationLoginHandler} - Migration for virtual tree structure.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class VirtualTreeMigrationLoginHandler implements LoginHandlerService {

    /**
     * Initializes a new {@link VirtualTreeMigrationLoginHandler}.
     */
    public VirtualTreeMigrationLoginHandler() {
        super();
    }

    public void handleLogin(final LoginResult login) throws LoginException {
        final Session session = login.getSession();
        final int cid = session.getContextId();
        // Get a connection
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new LoginException(e);
        }
        try {
            con.setAutoCommit(false); // BEGIN
            handleLogin0(session, con);
            con.commit(); // COMMIT
        } catch (final SQLException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw new LoginException(FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage()));
        } catch (final LoginException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw e;
        } catch (final Exception e) {
            DBUtils.rollback(con); // ROLLBACK
            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            Database.back(cid, true, con);
        }
    }

    private void handleLogin0(final Session s, final Connection con) throws LoginException {
        try {
            /*
             * Check if migration has been performed for logged in user
             */
            if (!performMigration(s, con)) {
                return;
            }
            /*
             * Do migration
             */
            final ServerSession session = new ServerSessionAdapter(s);
            /*
             * Append mail folders of external mail accounts
             */
            if (session.getUserConfiguration().isMultipleMailAccounts()) {
                try {
                    /*
                     * Get external mail accounts on top level
                     */
                    final List<MailAccount> accounts;
                    {
                        final MailAccountStorageService storageService =
                            ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
                        final MailAccount[] mailAccounts = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
                        accounts = new ArrayList<MailAccount>(mailAccounts.length);
                        accounts.addAll(Arrays.asList(mailAccounts));
                        Collections.sort(accounts, new MailAccountComparator(session.getUser().getLocale()));
                    }
                    if (!accounts.isEmpty()) {
                        for (final MailAccount mailAccount : accounts) {
                            if (!mailAccount.isDefaultAccount()) {
                                final DummyFolder mailFolder = new DummyFolder();
                                mailFolder.setName(mailAccount.getName());
                                mailFolder.setID(MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID));
                                mailFolder.setTreeID("1");
                                mailFolder.setParentID(FolderStorage.ROOT_ID);
                                mailFolder.setSubscribed(true);
                                Insert.insertFolder(s.getContextId(), 1, s.getUserId(), mailFolder);
                            }
                        }
                    }
                } catch (final OXException e) {
                    throw new LoginException(e);
                } catch (final MailAccountException e) {
                    throw new LoginException(e);
                }
            }
            /*
             * Add default mail folders: Drafts, Sent, Spam and Trash
             */
            final MailAccess<?, ?> mailAccess;
            try {
                mailAccess = MailAccess.getInstance(session);
                mailAccess.connect(true);
            } catch (final MailException e) {
                throw new LoginException(e);
            }
            try {
                insertDefaultMailFolder(mailAccess.getFolderStorage().getDraftsFolder(), mailAccess, s);
                insertDefaultMailFolder(mailAccess.getFolderStorage().getSentFolder(), mailAccess, s);
                insertDefaultMailFolder(mailAccess.getFolderStorage().getSpamFolder(), mailAccess, s);
                insertDefaultMailFolder(mailAccess.getFolderStorage().getTrashFolder(), mailAccess, s);
            } catch (final MailException e) {
                throw new LoginException(e);
            } finally {
                mailAccess.close(true);
            }

            setMigrationPerformed(session, con);
        } catch (final FolderException e) {
            throw new LoginException(e);
        } catch (final OXException e) {
            throw new LoginException(e);
        }
    }

    private static void insertDefaultMailFolder(final String fullname, final MailAccess<?, ?> mailAccess, final Session s) throws MailException, FolderException {
        final MailFolder folder = mailAccess.getFolderStorage().getFolder(fullname);
        final String id = MailFolderUtility.prepareFullname(0, fullname);
        final DummyFolder mailFolder = new DummyFolder();
        mailFolder.setName(folder.getName());
        mailFolder.setID(id);
        mailFolder.setTreeID("1");
        mailFolder.setParentID("1");
        mailFolder.setSubscribed(true);
        Insert.insertFolder(s.getContextId(), 1, s.getUserId(), mailFolder);
    }

    public void handleLogout(final LoginResult logout) throws LoginException {
        // Nothing to do
    }

    private static final String VIRTUAL_MIGRATION = "virtual.migration";

    private static final String SQL_SELECT = "SELECT value FROM user_attribute WHERE cid = ? AND id = ? AND name = ?";

    private static boolean performMigration(final Session session, final Connection con) throws FolderException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT);
            stmt.setInt(1, session.getContextId());
            stmt.setInt(2, session.getUserId());
            stmt.setString(3, VIRTUAL_MIGRATION);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return Boolean.parseBoolean(rs.getString(1));
            }
            return false;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private static final String SQL_UPDATE = "UPDATE user_attribute SET value = ? WHERE cid = ? AND id = ? AND name = ?";

    private static void setMigrationPerformed(final Session session, final Connection con) throws FolderException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_UPDATE);
            stmt.setString(1, "true");
            stmt.setInt(2, session.getContextId());
            stmt.setInt(3, session.getUserId());
            stmt.setString(4, VIRTUAL_MIGRATION);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static final class MailAccountComparator implements Comparator<MailAccount> {

        private final Collator collator;

        public MailAccountComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        public int compare(final MailAccount o1, final MailAccount o2) {
            if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o1.getMailProtocol())) {
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                    return 0;
                }
                return -1;
            } else if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                return 1;
            }
            if (o1.isDefaultAccount()) {
                if (o2.isDefaultAccount()) {
                    return 0;
                }
                return -1;
            } else if (o2.isDefaultAccount()) {
                return 1;
            }
            return collator.compare(o1.getName(), o2.getName());
        }

    } // End of MailAccountComparator

}
