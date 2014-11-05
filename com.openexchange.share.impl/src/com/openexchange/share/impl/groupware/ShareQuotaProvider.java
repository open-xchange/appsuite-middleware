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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.impl.groupware;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.Validate;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
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
import com.openexchange.share.storage.ShareStorage;
import com.openexchange.share.storage.StorageParameters;

/**
 * {@link ShareQuotaProvider}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class ShareQuotaProvider implements QuotaProvider {

    private static final long DEFAULT_SHARE_QUOTA_LIMIT = 150;

    private static final String MODULE_ID = "share";

    private final ServiceLookup services;

    public ShareQuotaProvider(ServiceLookup services) {
        super();

        Validate.notNull(services, "ServiceLookup might not be null!");
        this.services = services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getModuleID() {
        return MODULE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return "Shares";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccountQuota> getFor(Session session) throws OXException {
        return Collections.singletonList(getFor(session, "0"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if (accountID.equals("0")) {
            ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
            if (viewFactory == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
            }

            DatabaseService databaseService = services.getService(DatabaseService.class);
            if (databaseService == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
            }

            Quota quota = getAmountQuota(session, databaseService.getReadOnly(session.getContextId()), StorageParameters.NO_PARAMETERS, viewFactory);

            return new DefaultAccountQuota(accountID, getDisplayName()).addQuota(quota);
        }
        throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, MODULE_ID);
    }

    /**
     * Returns the quota available for the user associated to the session
     *
     * @param session - the session to get quota for
     * @param connection - connection for quota retrieving
     * @param viewFactory - ConfigViewFactory
     * @return current existing/available Quota
     * @throws OXException
     */
    public Quota getAmountQuota(Session session, Connection connection, StorageParameters parameters, ConfigViewFactory viewFactory) throws OXException {
        long limit = AmountQuotas.getLimit(session, MODULE_ID, viewFactory, connection, DEFAULT_SHARE_QUOTA_LIMIT);
        if (limit == Quota.UNLIMITED) {
            return Quota.UNLIMITED_AMOUNT;
        }

        long usage = this.getUsedQuota(session.getContextId(), session.getUserId(), parameters);
        return new Quota(QuotaType.AMOUNT, limit, usage);
    }

    /**
     * Returns the currently existing number of shares for the given user.
     *
     * @param contextId - id of the relevant context
     * @param userId - id of the user to get the count for
     * @return int - number of currently existing shares
     * @throws OXException
     */
    private int getUsedQuota(int contextId, int userId, StorageParameters parameters) throws OXException {
        ShareStorage shareStorage = services.getService(ShareStorage.class);
        if (shareStorage == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ShareStorage.class.getName());
        }

        int usedCount = shareStorage.countShares(contextId, userId, parameters);
        return usedCount;
    }
}
