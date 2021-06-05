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

import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;

/**
 * {@link AccountQuotas} - A collection of account quotas with accompanying warnings.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class AccountQuotas implements Iterable<AccountQuota> {

    private final List<AccountQuota> accountQuotas;
    private final List<OXException> warnings;

    /**
     * Initializes a new listing containing a single element.
     *
     * @param accountQuota The account quota
     */
    public AccountQuotas(AccountQuota accountQuota) {
        super();
        this.accountQuotas = accountQuota == null ? ImmutableList.of() : ImmutableList.of(accountQuota);
        this.warnings = ImmutableList.of();
    }

    /**
     * Initializes a new listing.
     *
     * @param accountQuotas The collected account quotas or <code>null</code>
     * @param warnings The collected warnings or <code>null</code>
     */
    public AccountQuotas(List<AccountQuota> accountQuotas, List<OXException> warnings) {
        super();
        this.accountQuotas = accountQuotas == null ? ImmutableList.of() : ImmutableList.copyOf(accountQuotas);
        this.warnings = warnings == null ? ImmutableList.of() : ImmutableList.copyOf(warnings);
    }

    /**
     * Gets the collected warnings.
     *
     * @return The warnings or an empty list
     */
    public List<OXException> getWarnings() {
        return warnings;
    }

    /**
     * Gets the collected account quotas.
     *
     * @return The collected account quotas
     */
    public List<AccountQuota> getAccountQuotas() {
        return accountQuotas;
    }

    @Override
    public Iterator<AccountQuota> iterator() {
        return accountQuotas.iterator();
    }

    /**
     * Gets the number of collected account quotas.
     *
     * @return The number of account quotas
     */
    public int size() {
        return accountQuotas.size();
    }

}
