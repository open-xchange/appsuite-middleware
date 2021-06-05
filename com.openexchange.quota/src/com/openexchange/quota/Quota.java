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
 * A {@link Quota} provides a limit and current usage according to its type.
 * There are convenience constants for unlimited quotas:
 * <ul>
 *   <li>{@link Quota#UNLIMITED_AMOUNT}</li>
 *   <li>{@link Quota#UNLIMITED_SIZE}</li>
 * </ul>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.6.1
 */
public class Quota {

    /**
     * The quota value for unlimited.
     */
    public static final long UNLIMITED = -1L;

    /**
     * Represents an unlimited quota of type {@link QuotaType#AMOUNT}.
     */
    public static final Quota UNLIMITED_AMOUNT = new Quota(QuotaType.AMOUNT, UNLIMITED, 0);

    /**
     * Represents an unlimited quota of type {@link QuotaType#SIZE}.
     */
    public static final Quota UNLIMITED_SIZE = new Quota(QuotaType.SIZE, UNLIMITED, 0);

    private final QuotaType type;

    private final long usage;

    private final long limit;

    /**
     * Initializes a new {@link Quota}.
     *
     * @param type The {@link Quota}, not <code>null</code>.
     * @param limit The limit in number of objects or number of bytes, according to the type.
     * @param usage The usage in number of objects or number of bytes, according to the type.
     */
    public Quota(QuotaType type, long limit, long usage) {
        super();
        this.type = type;
        this.limit = limit;
        this.usage = usage;
    }

    /**
     * Gets the type of this {@link Quota} instance.
     *
     * @return The type.
     */
    public QuotaType getType() {
        return type;
    }

    /**
     * Depending on this quotas {@link QuotaType} this method returns
     * <ul>
     *   <li>the max. number of objects that can be created or</li>
     *   <li>the max. number of bytes that can be allocated by the users data</li>
     * </ul>
     *
     * The limit applies to this account in this module.
     *
     * @return A number > 0, 0 for no quota at all or {@value #UNLIMITED} if unlimited.
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Depending on this quotas {@link QuotaType} this method returns
     * <ul>
     *   <li>the number of existing objects or</li>
     *   <li>the number of bytes occupied by the users data</li>
     * </ul>
     *
     * The limit applies to this account in this module.
     *
     * @return A number >= 0.
     */
    public long getUsage() {
        return usage;
    }

    /**
     * Returns if the currently used quota is exceeded.
     *
     * @return true if exceeded, otherwise false
     */
    public boolean isExceeded() {
        return (limit == 0) || ((limit > 0) && (usage >= limit));
    }

    /**
     * Returns if adding additional quota will exceed the limit.
     *
     * @return true if the quota limit will exceed, otherwise false
     */
    public boolean willExceed(long quotaToAdd) {
        if (isUnlimited()) {
            return false;
        }
        return (limit == 0) || ((usage + quotaToAdd) > limit);
    }

    /**
     * Returns if the limit is set to unlimited (com.openexchange.quota.Quota.UNLIMITED)
     *
     * @return true if UNLIMITED, otherwise false
     */
    public boolean isUnlimited() {
        return limit <= UNLIMITED;
    }
}
