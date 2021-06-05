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

import java.util.HashMap;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.console.user.UserAbstraction.CSVConstants;
import com.openexchange.admin.rmi.dataobjects.Context;


/**
 * This interface must be implemented by a class in the console package of a plugin so that it
 * can extend the basic command line options. To offer your own implementation of this interface
 * to the core. The ServiceLoader mechanism of JDK 6 is used which requests a directory
 * META-INF/services under which a text file whose name is the full-qualified binary name
 * of this interface (com.openexchange.admin.console.context.ContextConsoleCreateInterface). And the
 * content of this file must be the full-qualified binary name of your implementation.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface ContextConsoleCreateInterface extends ContextConsoleCommonInterface {

    /**
     * This method processes a {@link HashMap} of CSVConstants with their name. This method can be used to
     * modify the map, so you can add or remove parameter which can be used in the CSV file. Or you can
     * change if a parameter is required or not
     *
     * @param constantsMap - the {@link HashMap}
     */
    public void processCSVConstants(HashMap<String, CSVConstants> constantsMap);

    /**
     * This method processes a single line from a CSV file and adds the results to the corresponding context
     * object
     *
     * @param nextLine
     * @param idarray
     * @param context
     * @throws OXConsolePluginException
     */
    public void applyExtensionValuesFromCSV(final String[] nextLine, final int[] idarray, final Context context) throws OXConsolePluginException;

}
