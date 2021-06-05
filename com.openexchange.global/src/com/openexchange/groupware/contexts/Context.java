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

package com.openexchange.groupware.contexts;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The context stores all attributes that are necessary for components dealing with context specific data. This are especially which
 * database stores the data of the context, the unique numerical identifier used in the relational database to assign persistent stored data
 * to their contexts and is the base distinguished name used in the directory service to separate contexts. Objects implementing this
 * interface must implement {@link java.lang.Object#equals(java.lang.Object)} and {@link java.lang.Object#hashCode()} because this interface
 * is used as key for maps.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface Context extends FileStorageInfo, Serializable {

    /**
     * Returns the unique identifier of the context.
     *
     * @return unique identifier of the context.
     */
    int getContextId();

    /**
     * @return the name of the context.
     */
    String getName();

    /**
     * @return the login information of a context.
     */
    String[] getLoginInfo();

    /**
     * Returns the unique identifier of context's admin.
     *
     * @return unique identifier of the context's admin
     */
    int getMailadmin();

    /**
     * Returns if a context is enabled. All sessions that belong to a disabled context have to die as fast as possible to be able to
     * maintain these contexts.
     *
     * @return <code>true</code> if the context is enabled, <code>false</code> otherwise.
     */
    boolean isEnabled();

    /**
     * Returns if a context is being updated. This will be <code>true</code> if the schema is being updated the context is stored in.
     *
     * @return <code>true</code> if an update takes place.
     */
    boolean isUpdating();

    /**
     * Contexts can be put into read only mode if the master database server is not reachable. This method indicates if currently the master
     * is not reachable.
     *
     * @return <code>true</code> if the master database server is not reachable.
     */
    boolean isReadOnly();

    /**
     * Gets the context attributes as an unmodifiable map.
     * <p>
     * Each attribute may point to multiple values.
     *
     * @return The context attributes
     */
    Map<String, List<String>> getAttributes();

}
