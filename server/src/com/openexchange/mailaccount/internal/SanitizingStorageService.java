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

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountExceptionMessages;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.internal.Sanitizer;
import com.openexchange.tools.net.URIDefaults;

/**
 * {@link SanitizingStorageService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class SanitizingStorageService implements MailAccountStorageService {

    private static final int URI_ERROR_NUMBER = MailAccountExceptionMessages.URI_PARSE_FAILED.getDetailNumber();

    private final MailAccountStorageService storageService;

    /**
     * Initializes a new {@link SanitizingStorageService}.
     */
    SanitizingStorageService(final MailAccountStorageService storageService) {
        super();
        this.storageService = storageService;
    }

    private static boolean isURIError(final MailAccountException candidate) {
        return URI_ERROR_NUMBER == candidate.getDetailNumber();
    }

    public void invalidateMailAccount(final int id, final int user, final int cid) throws MailAccountException {
        storageService.invalidateMailAccount(id, user, cid);
    }

    public MailAccount getMailAccount(final int id, final int user, final int cid) throws MailAccountException {
        try {
            return storageService.getMailAccount(id, user, cid);
        } catch (final MailAccountException e) {
            if (!isURIError(e)) {
                throw e;
            }
            Sanitizer.sanitize(user, cid, storageService);
            return storageService.getMailAccount(id, user, cid);
        }
    }

    public MailAccount[] getUserMailAccounts(final int user, final int cid) throws MailAccountException {
        try {
            return storageService.getUserMailAccounts(user, cid);
        } catch (final MailAccountException e) {
            if (!isURIError(e)) {
                throw e;
            }
            Sanitizer.sanitize(user, cid, storageService);
            return storageService.getUserMailAccounts(user, cid);
        }
    }

    public MailAccount[] getUserMailAccounts(final int user, final int cid, final Connection con) throws MailAccountException {
        try {
            return storageService.getUserMailAccounts(user, cid, con);
        } catch (final MailAccountException e) {
            if (!isURIError(e)) {
                throw e;
            }
            Sanitizer.sanitize(user, cid, storageService);
            return storageService.getUserMailAccounts(user, cid, con);
        }
    }

    public MailAccount getDefaultMailAccount(final int user, final int cid) throws MailAccountException {
        return storageService.getDefaultMailAccount(user, cid);
    }

    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final String sessionPassword) throws MailAccountException {
        storageService.updateMailAccount(mailAccount, attributes, user, cid, sessionPassword);
    }

    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final String sessionPassword, final Connection con, final boolean changePrimary) throws MailAccountException {
        storageService.updateMailAccount(mailAccount, attributes, user, cid, sessionPassword, con, changePrimary);
    }

    public void updateMailAccount(final MailAccountDescription mailAccount, final int user, final int cid, final String sessionPassword) throws MailAccountException {
        storageService.updateMailAccount(mailAccount, user, cid, sessionPassword);
    }

    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final String sessionPassword) throws MailAccountException {
        return storageService.insertMailAccount(mailAccount, user, ctx, sessionPassword);
    }

    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final String sessionPassword, final Connection con) throws MailAccountException {
        return storageService.insertMailAccount(mailAccount, user, ctx, sessionPassword, con);
    }

    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid) throws MailAccountException {
        storageService.deleteMailAccount(id, properties, user, cid);
    }

    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid, final boolean deletePrimary) throws MailAccountException {
        storageService.deleteMailAccount(id, properties, user, cid, deletePrimary);
    }

    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid, final boolean deletePrimary, final Connection con) throws MailAccountException {
        storageService.deleteMailAccount(id, properties, user, cid, deletePrimary, con);
    }

    public MailAccount[] resolveLogin(final String login, final int cid) throws MailAccountException {
        return storageService.resolveLogin(login, cid);
    }

    public MailAccount[] resolveLogin(final String login, final InetSocketAddress server, final int cid) throws MailAccountException {
        return storageService.resolveLogin(login, server, cid);
    }

    public MailAccount[] resolvePrimaryAddr(final String primaryAddress, final int cid) throws MailAccountException {
        return storageService.resolvePrimaryAddr(primaryAddress, cid);
    }

    public int getByPrimaryAddress(final String primaryAddress, final int user, final int cid) throws MailAccountException {
        return storageService.getByPrimaryAddress(primaryAddress, user, cid);
    }

    public int[] getByHostNames(final Collection<String> hostNames, final int user, final int cid) throws MailAccountException {
        return storageService.getByHostNames(hostNames, user, cid);
    }

    public MailAccount getTransportAccountForID(final int id, final int user, final int cid) throws MailAccountException {
        try {
            return storageService.getTransportAccountForID(id, user, cid);
        } catch (final MailAccountException e) {
            if (!isURIError(e)) {
                throw e;
            }
            Sanitizer.sanitize(user, cid, storageService, URIDefaults.SMTP, "smtp://localhost:25");
            return storageService.getTransportAccountForID(id, user, cid);
        }
    }

    public String checkCanDecryptPasswords(final int user, final int cid, final String secret) throws MailAccountException {
        return storageService.checkCanDecryptPasswords(user, cid, secret);
    }

    public void migratePasswords(final int user, final int cid, final String oldSecret, final String newSecret) throws MailAccountException {
        storageService.migratePasswords(user, cid, oldSecret, newSecret);
    }

}
