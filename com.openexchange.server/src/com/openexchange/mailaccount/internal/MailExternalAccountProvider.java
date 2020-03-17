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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mailaccount.internal;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import com.google.common.collect.ImmutableMap;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.DefaultExternalAccount;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthAccountStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailExternalAccountProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class MailExternalAccountProvider implements ExternalAccountProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailExternalAccountProvider}.
     * 
     * @param services The service lookup-up instance
     */
    public MailExternalAccountProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public @NonNull ExternalAccountModule getModule() {
        return ExternalAccountModule.MAIL;
    }

    @Override
    public List<ExternalAccount> list(int contextId) throws OXException {
        List<ExternalAccount> list = new LinkedList<>();
        for (MailAccount mailAccount : getMailAccountStorageService().getUserMailAccounts(contextId)) {
            addMailAccount(contextId, mailAccount, list);
        }
        return list;
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId) throws OXException {
        List<ExternalAccount> list = new LinkedList<>();
        for (MailAccount mailAccount : getMailAccountStorageService().getUserMailAccounts(contextId)) {
            addMailAccount(contextId, providerId, mailAccount, list);
        }
        return list;
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId) throws OXException {
        List<ExternalAccount> list = new LinkedList<>();
        for (MailAccount mailAccount : getMailAccountStorageService().getUserMailAccounts(userId, contextId)) {
            addMailAccount(contextId, mailAccount, list);
        }
        return list;
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId) throws OXException {
        List<ExternalAccount> list = new LinkedList<>();
        for (MailAccount mailAccount : getMailAccountStorageService().getUserMailAccounts(userId, contextId)) {
            addMailAccount(contextId, providerId, mailAccount, list);
        }
        return list;
    }

    @Override
    public void delete(int id, int contextId, int userId) throws OXException {
        getMailAccountStorageService().deleteMailAccount(id, ImmutableMap.of(), userId, contextId);
    }

    @Override
    public void delete(int id, int contextId, int userId, Connection connection) throws OXException {
        getMailAccountStorageService().deleteMailAccount(id, ImmutableMap.of(), userId, contextId, false, connection);
    }

    ////////////////////////////////////////////// HELPERS ////////////////////////////////////////

    /**
     * Converts the specified mail account to an {@link ExternalAccount} and adds it to the list
     * 
     * @param mailAccount the {@link MailAccount} to convert and add
     * @param providerId The provider identifier
     * @param list The list to add it to
     * @throws OXException if an error is occurred
     */
    private void addMailAccount(int contextId, String providerId, MailAccount mailAccount, List<ExternalAccount> list) throws OXException {
        int unifiedId = getUnifiedInboxManagement().getUnifiedINBOXAccountID(mailAccount.getUserId(), contextId);
        if (mailAccount.getId() == 0 || mailAccount.getId() == unifiedId) {
            // Skip primary account and unified inbox
            return;
        }
        if (mailAccount.getMailOAuthId() > 0) {
            OAuthAccount account = getOAuthAccountStorage().getAccount(contextId, mailAccount.getUserId(), mailAccount.getMailOAuthId());
            if (false == account.getAPI().getServiceId().equals(providerId)) {
                return;
            }
        }
        list.add(new DefaultExternalAccount(mailAccount.getId(), contextId, mailAccount.getUserId(), providerId, ExternalAccountModule.MAIL));
    }

    /**
     * Converts the specified mail account to an {@link ExternalAccount} and adds it to the list
     * 
     * @param mailAccount the {@link MailAccount} to convert and add
     * @param list The list to add it to
     * @throws OXException if an error is occurred
     */
    private void addMailAccount(int contextId, MailAccount mailAccount, List<ExternalAccount> list) throws OXException {
        int unifiedId = getUnifiedInboxManagement().getUnifiedINBOXAccountID(mailAccount.getUserId(), contextId);
        if (mailAccount.getId() == 0 || mailAccount.getId() == unifiedId) {
            // Skip primary account and unified inbox
            return;
        }
        String providerId = mailAccount.getMailServer();
        if (mailAccount.getMailOAuthId() > 0) {
            OAuthAccount account = getOAuthAccountStorage().getAccount(contextId, mailAccount.getUserId(), mailAccount.getMailOAuthId());
            providerId = account.getAPI().getServiceId();
        }
        list.add(new DefaultExternalAccount(mailAccount.getId(), contextId, mailAccount.getUserId(), providerId, ExternalAccountModule.MAIL));
    }

    /**
     * Returns the {@link MailAccountStorageService}
     *
     * @return the {@link MailAccountStorageService}
     * @throws OXException if the service is absent
     */
    private MailAccountStorageService getMailAccountStorageService() throws OXException {
        return ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
    }

    /**
     * Returns the {@link OAuthAccountStorage}
     *
     * @return the {@link OAuthAccountStorage}
     * @throws OXException if the service is absent
     */
    private OAuthAccountStorage getOAuthAccountStorage() throws OXException {
        return services.getServiceSafe(OAuthAccountStorage.class);
    }

    private UnifiedInboxManagement getUnifiedInboxManagement() {
        return ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);

    }

}
