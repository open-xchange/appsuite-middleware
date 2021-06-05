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


/**
 * An {@link AccountQuota} instance encapsulates size and amount quotas and their
 * current usages for a certain account in a certain module. {@link DefaultAccountQuota}
 * can be used to create new instances.
 *
 * @see DefaultAccountQuota
 * @see QuotaType
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.6.1
 */
public interface AccountQuota {

    /**
     * Gets the identifier of the associated account.
     *
     * @return The identifier, never <code>null</code>
     */
    String getAccountID();

    /**
     * Gets the name of the associated account.
     *
     * @return The name, never <code>null</code>
     */
    String getAccountName();

    /**
     * Checks whether a {@link Quota} can be obtained via {@link #getQuota(QuotaType)} for the given type.
     *
     * @param type The quota type.
     * @return <code>true</code> if the quota is available, otherwise <code>false</code>.
     */
    boolean hasQuota(QuotaType type);

    /**
     * Gets the {@link Quota} for the given type.
     *
     * @param type The quota type.
     * @return The quota or <code>null</code>, if not available.
     */
    Quota getQuota(QuotaType type);

}
