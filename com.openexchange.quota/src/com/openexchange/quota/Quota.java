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
     * @return A number > 0 or {@value #UNLIMITED} if unlimited.
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
        return (limit > 0) && (usage >= limit);
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
        return (usage + quotaToAdd) > limit;
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
