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

import java.sql.Connection;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountExceptionMessages;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.UserService;

/**
 * {@link UnifiedINBOXManagementImpl} - The Unified INBOX management implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXManagementImpl implements UnifiedINBOXManagement {

    /**
     * Initializes a new {@link UnifiedINBOXManagementImpl}.
     */
    public UnifiedINBOXManagementImpl() {
        super();
    }

    public void createUnifiedINBOX(final int userId, final int contextId) throws MailAccountException {
        createUnifiedINBOX(userId, contextId, null);
    }

    public void createUnifiedINBOX(final int userId, final int contextId, final Connection con) throws MailAccountException {
        try {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);
            // Check if Unified INBOX account already exists for given user
            final MailAccount[] existingAccounts = storageService.getUserMailAccounts(userId, contextId);
            for (final MailAccount mailAccount : existingAccounts) {
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                    // User already has the Unified INBOX account set
                    throw MailAccountExceptionMessages.DUPLICATE_UNIFIED_INBOX_ACCOUNT.create(
                        Integer.valueOf(userId),
                        Integer.valueOf(contextId));
                }
            }
            final ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class, true);
            final Context ctx = contextService.getContext(contextId);
            // Create and fill appropriate description object
            final MailAccountDescription mailAccountDescription = new MailAccountDescription();
            mailAccountDescription.setName("Unified INBOX");
            mailAccountDescription.setConfirmedHam("confirmed-ham");
            mailAccountDescription.setConfirmedSpam("confirmed-spam");
            mailAccountDescription.setDefaultFlag(false);
            mailAccountDescription.setDrafts("drafts");
            final String login = getUserLogin(userId, ctx);
            mailAccountDescription.setLogin(login);
            mailAccountDescription.setMailPort(143);
            mailAccountDescription.setMailProtocol(UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX);
            mailAccountDescription.setMailSecure(false);
            mailAccountDescription.setMailServer("localhost");
            mailAccountDescription.setPassword("");
            mailAccountDescription.setPrimaryAddress(new StringBuilder(32).append(login).append("@unifiedinbox.com").toString());
            mailAccountDescription.setSent("sent");
            mailAccountDescription.setSpam("spam");
            mailAccountDescription.setSpamHandler("NoSpamHandler");
            // No transport settings
            mailAccountDescription.setTransportServer(null);
            mailAccountDescription.setTrash("trash");
            // Create it
            if (null == con) {
                storageService.insertMailAccount(mailAccountDescription, userId, ctx, null);
            } else {
                storageService.insertMailAccount(mailAccountDescription, userId, ctx, null, con);
            }
        } catch (final ServiceException e) {
            throw new MailAccountException(e);
        } catch (final MailAccountException e) {
            throw new MailAccountException(e);
        } catch (final ContextException e) {
            throw new MailAccountException(e);
        }

    }

    public void deleteUnifiedINBOX(final int userId, final int contextId) throws MailAccountException {
        deleteUnifiedINBOX(userId, contextId, null);
    }

    public void deleteUnifiedINBOX(final int userId, final int contextId, final Connection con) throws MailAccountException {
        try {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);
            // Determine the ID of the Unified INBOX account for given user
            final MailAccount[] existingAccounts = storageService.getUserMailAccounts(userId, contextId);
            int id = -1;
            for (int i = 0; i < existingAccounts.length && id < 0; i++) {
                final MailAccount mailAccount = existingAccounts[i];
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                    id = mailAccount.getId();
                }
            }
            // Delete the Unified INBOX account
            if (id >= 0) {
                if (null == con) {
                    storageService.deleteMailAccount(id, userId, contextId, false);
                } else {
                    storageService.deleteMailAccount(id, userId, contextId, false, con);
                }
            }
        } catch (final ServiceException e) {
            throw new MailAccountException(e);
        } catch (final MailAccountException e) {
            throw new MailAccountException(e);
        }
    }

    public boolean isEnabled(final int userId, final int contextId) throws MailAccountException {
        return isEnabled(userId, contextId, null);
    }

    public boolean isEnabled(final int userId, final int contextId, final Connection con) throws MailAccountException {
        try {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);
            // Look-up the Unified INBOX account for given user
            final MailAccount[] existingAccounts;
            if (null == con) {
                existingAccounts = storageService.getUserMailAccounts(userId, contextId);
            } else {
                existingAccounts = storageService.getUserMailAccounts(userId, contextId, con);
            }
            for (int i = 0; i < existingAccounts.length; i++) {
                final MailAccount mailAccount = existingAccounts[i];
                if (!UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol()) && mailAccount.isUnifiedINBOXEnabled()) {
                    return true;
                }
            }
            return false;
        } catch (final ServiceException e) {
            throw new MailAccountException(e);
        } catch (final MailAccountException e) {
            throw new MailAccountException(e);
        }
    }

    public int getUnifiedINBOXAccountID(final int userId, final int contextId) throws MailAccountException {
        return getUnifiedINBOXAccountID(userId, contextId, null);
    }

    public int getUnifiedINBOXAccountID(final int userId, final int contextId, final Connection con) throws MailAccountException {
        try {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                MailAccountStorageService.class,
                true);
            // Look-up the Unified INBOX account for given user
            final MailAccount[] existingAccounts;
            if (null == con) {
                existingAccounts = storageService.getUserMailAccounts(userId, contextId);
            } else {
                existingAccounts = storageService.getUserMailAccounts(userId, contextId, con);
            }
            for (int i = 0; i < existingAccounts.length; i++) {
                final MailAccount mailAccount = existingAccounts[i];
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                    return mailAccount.getId();
                }
            }
            return -1;
        } catch (final ServiceException e) {
            throw new MailAccountException(e);
        } catch (final MailAccountException e) {
            throw new MailAccountException(e);
        }
    }

    /*-
     * +++++++++++++++++++++++++++++++++++++++++++++ HELPERS +++++++++++++++++++++++++++++++++++++++++++++
     */

    private static String getUserLogin(final int userId, final Context ctx) throws MailAccountException {
        try {
            final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class, true);
            return userService.getUser(userId, ctx).getLoginInfo();
        } catch (final ServiceException e) {
            throw new MailAccountException(e);
        } catch (final UserException e) {
            throw new MailAccountException(e);
        }
    }

}
