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

package com.openexchange.admin.plugins;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link ContextDbLookupPluginInterface}
 *
 * Database lookup related plugin interface to be able to manipulate {@link Context} instances before or after they are looked up
 * from the database.<br>
 *<br>
 * <b>Note:</b> Only one registered implementation of this plugin can ensure deterministic behavior!
 *
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @since v7.10.3
 */
public interface ContextDbLookupPluginInterface {

    /**
     * Ability to manipulate the given {@link Context} as provided via provisioning API before it is being looked
     * up from the database.
     *
     * @param credentials The admin credentials
     * @param ctx The contexts
     * @throws PluginException When manipulating data fails
     */
    public void beforeContextDbLookup(final Credentials credentials, final Context[] ctx) throws PluginException;

    /**
     * Ability to manipulate the given {@link Context} loaded from the database before handing out via API
     *
     * @param credentials The admin credentials
     * @param ctx The contexts
     * @throws PluginException When manipulating data fails
     */
    public void afterContextDbLookup(final Credentials credentials, final Context[] ctx) throws PluginException;

    /**
     * Ability to manipulate the searchPattern before it is used to search Contexts within the database.
     *
     * @param credentials The admin credentials
     * @param searchPattern The search pattern
     * @return manipulated The manipulated search pattern
     * @throws PluginException When manipulating data fails
     */
    public String searchPatternDbLookup(final Credentials credentials, final String searchPattern) throws PluginException;
}
