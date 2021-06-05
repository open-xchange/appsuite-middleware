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

package com.openexchange.quota;

import java.util.EnumMap;

/**
 * {@link DefaultAccountQuota} - A default implementation of {@link AccountQuota}.
 *
 * @see AccountQuota
 * @see Quota
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public class DefaultAccountQuota implements AccountQuota {

    private final EnumMap<QuotaType, Quota> quotas;

    private final String accountName;

    private final String accountID;

    /**
     * Initializes a new {@link DefaultAccountQuota}. The {@link #addQuota(Quota)} and
     * {@link #addQuota(QuotaType, long, long)} methods can be used to construct a
     * {@link DefaultAccountQuota} in a builder-style manner.
     *
     * @param accountID The according accounts id, never <code>null</code>.
     * @param accountName The according accounts name, never <code>null</code>.
     */
    public DefaultAccountQuota(String accountID, String accountName) {
        super();
        this.accountID = accountID;
        this.accountName = accountName;
        this.quotas = new EnumMap<QuotaType, Quota>(QuotaType.class);
    }

    /**
     * Adds the given quota; replacing any existing quota possibly previously added for same {@link QuotaType}.
     * <p>
     * A quota should only be added once for every possible {@link QuotaType}.
     *
     * @see {@link Quota#UNLIMITED_AMOUNT}
     * @see {@link Quota#UNLIMITED_SIZE}
     * @param quota The quota (or <code>null</code>, which is ignored).
     * @return This {@link DefaultAccountQuota} instance.
     */
    public DefaultAccountQuota addQuota(Quota quota) {
        if (null != quota) {
            quotas.put(quota.getType(), quota);
        }
        return this;
    }

    /**
     * Adds a new {@link Quota} instance for the given parameters. A quota must
     * only be added once for every possible {@link QuotaType}.
     *
     * @see {@link Quota#UNLIMITED_AMOUNT}
     * @see {@link Quota#UNLIMITED_SIZE}
     * @param type The quota type, never <code>null</code>.
     * @param limit The limit. Greater 0 or {@link Quota#UNLIMITED}.
     * @param usage The current usage. Greater or equals 0.
     * @return This {@link DefaultAccountQuota} instance.
     */
    public DefaultAccountQuota addQuota(final QuotaType type, final long limit, final long usage) {
        return addQuota(new Quota(type, limit, usage));
    }

    @Override
    public String getAccountID() {
        return accountID;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public boolean hasQuota(QuotaType type) {
        return quotas.containsKey(type);
    }

    @Override
    public Quota getQuota(QuotaType type) {
        return quotas.get(type);
    }

}
