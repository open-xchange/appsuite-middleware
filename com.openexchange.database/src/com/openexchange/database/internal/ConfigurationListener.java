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

package com.openexchange.database.internal;

/**
 * {@link ConfigurationListener}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public interface ConfigurationListener extends Comparable<ConfigurationListener> {

    /**
     * Notifies this listener about an updated configuration for JDBC
     *
     * @param configuration The new {@link Configuration}
     */
    void notify(Configuration configuration);

    /**
     * Gets the identifier of the pool to notify
     *
     * @return The pool ID
     */
    int getPoolId();

    /**
     * Gets the priority.
     * <ul>
     * <li><code>1<code></code> means highest, <b>reserved for configDB, do not use!</b></li>
     * <li><code>100</code> means lowest and is the default value</li>
     * </ul>
     *
     * @return The priority
     */
    default int getPriority() {
        return 100;
    }

    @Override
    default int compareTo(ConfigurationListener o) {
        int otherPriority = o.getPriority();
        int thisPriority = getPriority();
        return (thisPriority < otherPriority) ? -1 : ((otherPriority == thisPriority) ? 0 : 1);
    }

    /**
     * {@link ConfigDBListener} - Marker interface to avoid unnecessary reloading of user DBs
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.1
     */
    interface ConfigDBListener extends ConfigurationListener {
        // Marker interface to avoid unnecessary reloading of user DBs
    }
}
