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
package com.openexchange.admin.console.context.extensioninterfaces;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;


/**
 * {@link ContextConsoleCommonInterface}
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface ContextConsoleCommonInterface {

    /**
     * This method adds the extension options which are provided by this plugin to the given
     * parser object
     *
     * @param parser
     * @throws OXConsolePluginException TODO
     */
    public void addExtensionOptions(final AdminParser parser) throws OXConsolePluginException;

    /**
     * This method read the options which were set with the {@link #addExtensionOptions(AdminParser)}
     * method and fills them into an extension object which is used by this class. The extension
     * object is then returned
     *
     * @param parser
     * @param ctx TODO
     * @param auth TODO
     * @throws OXConsolePluginException TODO
     */
    public void setAndFillExtension(final AdminParser parser, final Context ctx, Credentials auth) throws OXConsolePluginException;

}
