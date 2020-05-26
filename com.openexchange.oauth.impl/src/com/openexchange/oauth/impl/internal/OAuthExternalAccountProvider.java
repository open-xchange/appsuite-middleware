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

package com.openexchange.oauth.impl.internal;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.DefaultExternalAccount;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.oauth.AdministrativeOAuthAccount;
import com.openexchange.oauth.AdministrativeOAuthAccountStorage;
import com.openexchange.server.ServiceLookup;

/**
 * {@link OAuthExternalAccountProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class OAuthExternalAccountProvider implements ExternalAccountProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link OAuthExternalAccountProvider}.
     * 
     * @param services The service lookup-up instance
     */
    public OAuthExternalAccountProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public @NonNull ExternalAccountModule getModule() {
        return ExternalAccountModule.OAUTH;
    }

    @Override
    public List<ExternalAccount> list(int contextId) throws OXException {
        return parseAccounts(getStorage().listAccounts(contextId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId) throws OXException {
        return parseAccounts(getStorage().listAccounts(contextId, userId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId) throws OXException {
        return parseAccounts(getStorage().listAccounts(contextId, userId, providerId));
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId) throws OXException {
        return parseAccounts(getStorage().listAccounts(contextId, providerId));
    }

    @Override
    public boolean delete(int id, int contextId, int userId) throws OXException {
        return getStorage().deleteAccount(contextId, userId, id);
    }

    @Override
    public boolean delete(int id, int contextId, int userId, Connection connection) throws OXException {
        return getStorage().deleteAccount(contextId, userId, id, connection);
    }

    /**
     * Parses the specified oauth accounts to external accounts
     *
     * @param list the list of the accounts to parse
     * @return The external accounts
     */
    private List<ExternalAccount> parseAccounts(List<AdministrativeOAuthAccount> list) {
        List<ExternalAccount> accounts = new LinkedList<>();
        for (AdministrativeOAuthAccount account : list) {
            accounts.add(new DefaultExternalAccount(account.getId(), account.getContextId(), account.getUserId(), account.getServiceId(), getModule()));
        }
        return accounts;
    }

    /**
     * Returns the {@link AdministrativeOAuthAccountStorage}
     *
     * @return the {@link AdministrativeOAuthAccountStorage}
     * @throws OXException if the service is absent
     */
    private AdministrativeOAuthAccountStorage getStorage() throws OXException {
        return services.getServiceSafe(AdministrativeOAuthAccountStorage.class);
    }
}
