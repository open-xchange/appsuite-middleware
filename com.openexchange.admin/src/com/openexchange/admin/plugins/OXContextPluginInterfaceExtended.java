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

import java.util.Map;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link OXContextPluginInterfaceExtended} - The extended admin plug-in interface offering additional methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface OXContextPluginInterfaceExtended extends OXContextPluginInterface {

    /**
     * Define the operations which should be done before the real context delete process.
     *
     * @param ctx
     * @param auth
     * @return The undo information
     * @throws PluginException
     */
    Map<String, Object> undoableDelete(final Context ctx, final Credentials auth) throws PluginException;

    /**
     * Undos context deletion.
     *
     * @param ctx The context which could not be deleted
     * @param undoInfo The required information to undo the delete
     * @throws PluginException If operation fails
     */
    void undelete(Context ctx, Map<String, Object> undoInfo) throws PluginException;

}
