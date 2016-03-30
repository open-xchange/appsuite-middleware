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

package com.openexchange.share.impl.quota;

import java.util.Collections;
import java.util.List;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.quota.AccountQuota;
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
    public List<AccountQuota> getFor(Session session) throws OXException {
        return Collections.singletonList(getFor(session, "0"));
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
