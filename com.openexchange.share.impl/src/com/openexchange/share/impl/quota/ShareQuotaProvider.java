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

package com.openexchange.share.impl.quota;

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.AccountQuotas;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.impl.ConnectionHelper;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link ShareQuotaProvider}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public abstract class ShareQuotaProvider implements QuotaProvider {

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link ShareQuotaProvider}.
     *
     * @param services A service lookup reference
     */
    protected ShareQuotaProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the default amount quota limitation.
     *
     * @return The default quota limitation
     */
    protected abstract long getDefaultLimit();

    /**
     * Gets the used amount quota for a specific user.
     *
     * @param createdGuests The guest users that were already created by the user
     * @param connectionHelper A (started) connection helper
     * @param userID The identifier of the user to get the quota usage for
     * @return The used quota
     */
    protected abstract long getUsedQuota(User[] createdGuests) throws OXException;

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if (!"0".equals(accountID)) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, getModuleID());
        }
        Quota amountQuota;
        ConnectionHelper connectionHelper = new ConnectionHelper(session, services, false);
        try {
            amountQuota = getAmountQuota(connectionHelper, session);
        } finally {
            connectionHelper.finish();
        }
        return new DefaultAccountQuota(accountID, getDisplayName()).addQuota(amountQuota);
    }

    @Override
    public AccountQuotas getFor(Session session) throws OXException {
        return new AccountQuotas(getFor(session, "0"));
    }

    /**
     * Returns the quota available for the user associated to the session
     *
     * @param connectionHelper A (started) connection helper
     * @param session The session to get quota for
     * @return The amount quota for the session's user
     */
    private Quota getAmountQuota(ConnectionHelper connectionHelper, Session session) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }
        long limit = AmountQuotas.getLimit(session, getModuleID(), viewFactory, connectionHelper.getConnection(), getDefaultLimit());
        if (limit <= Quota.UNLIMITED) {
            return Quota.UNLIMITED_AMOUNT;
        }
        UserService userService = services.getService(UserService.class);
        Context context = services.getService(ContextService.class).getContext(session.getContextId());
        User[] createdGuests = userService.getGuestsCreatedBy(connectionHelper.getConnection(), context, session.getUserId());
        return new Quota(QuotaType.AMOUNT, limit, getUsedQuota(createdGuests));
    }

}
