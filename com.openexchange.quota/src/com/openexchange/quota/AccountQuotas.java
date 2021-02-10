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
