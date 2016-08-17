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
     * Adds the given quota. A quota must only be added once for
     * every possible {@link QuotaType}.
     *
     * @see {@link Quota#UNLIMITED_AMOUNT}
     * @see {@link Quota#UNLIMITED_SIZE}
     * @param quota The quota, never <code>null</code>.
     * @return This {@link DefaultAccountQuota} instance.
     */
    public DefaultAccountQuota addQuota(Quota quota) {
        quotas.put(quota.getType(), quota);
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
