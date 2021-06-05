/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.snippet.rdb.groupware;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.AccountQuotas;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.rdb.Services;


/**
 * {@link RdbSnippetQuotaProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class RdbSnippetQuotaProvider implements QuotaProvider {

    private static final String MODULE_ID = "rdb_snippet";
    private static final String PROP_NAME = "com.openexchange.snippet.quota.limit";

    private final AtomicReference<SnippetService> snippetServiceRef;

    /**
     * Initializes a new {@link RdbSnippetQuotaProvider}.
     */
    public RdbSnippetQuotaProvider() {
        super();
        this.snippetServiceRef = new AtomicReference<SnippetService>(null);
    }

    /**
     * Sets the associated snippet service
     *
     * @param quotaProvider The snippet service to set
     */
    public void setSnippetService(SnippetService snippetService) {
        this.snippetServiceRef.set(snippetService);
    }

    @Override
    public String getModuleID() {
        return MODULE_ID;
    }

    @Override
    public String getDisplayName() {
        return "Snippet";
    }

    private Quota getAmountQuota(Session session, ConfigViewFactory viewFactory) throws OXException {
        long limit = AmountQuotas.getConfiguredLimitByPropertyName(session, PROP_NAME, viewFactory);
        if (limit <= Quota.UNLIMITED) {
            return Quota.UNLIMITED_AMOUNT;
        }
        long usage = snippetServiceRef.get().getManagement(session).getOwnSnippetsCount();
        return new Quota(QuotaType.AMOUNT, limit, usage);
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if (!accountID.equals("0")) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, MODULE_ID);
        }

        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        if (viewFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }

        Quota amountQuota = getAmountQuota(session, viewFactory);
        return new DefaultAccountQuota(accountID, getDisplayName()).addQuota(amountQuota);
    }

    @Override
    public AccountQuotas getFor(Session session) throws OXException {
        return new AccountQuotas(getFor(session, "0"));
    }

}
