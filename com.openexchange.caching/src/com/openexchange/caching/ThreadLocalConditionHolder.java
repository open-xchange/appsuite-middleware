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

package com.openexchange.caching;

import com.openexchange.caching.events.Condition;

/**
 * {@link ThreadLocalConditionHolder} - A condition holder using a {@link ThreadLocal} instance.
 * <p>
 * This class aims to offer an easy way to specify a thread-bound condition. Thus all fired cache events on behalf of that thread are
 * bound to the thread-local condition avoiding the need to pass a {@link Condition condition} instance around through multiple layers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ThreadLocalConditionHolder {

    private static final ThreadLocalConditionHolder INSTANCE = new ThreadLocalConditionHolder();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static ThreadLocalConditionHolder getInstance() {
        return INSTANCE;
    }

    // --------------------------------------------------------------------------------------------------

    private final ThreadLocal<Condition> condition;

    /**
     * Initializes a new {@link ThreadLocalSessionHolder}.
     */
    private ThreadLocalConditionHolder() {
        super();
        condition = new ThreadLocal<Condition>();
    }

    /**
     * Sets the specified <tt>Condition</tt> instance.
     *
     * @param condition The <tt>Condition</tt> instance
     */
    public void setCondition(Condition condition) {
        this.condition.set(condition);
    }

    /**
     * Clears the condition (if any)
     */
    public void clear() {
        condition.remove();
    }

    /**
     * Gets the <tt>Condition</tt> instance.
     *
     * @return The <tt>Condition</tt> instance
     */
    public Condition getCondition() {
        return condition.get();
    }

}
