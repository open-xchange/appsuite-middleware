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

import java.util.ArrayList;
import com.openexchange.admin.rmi.dataobjects.Context;

/**
 * This interface must be implemented by a class in the console package of a plugin so that it
 * can extend the basic command line options. To offer your own implementation of this interface
 * to the core. The ServiceLoader mechanism of JDK 6 is used which requests a directory
 * META-INF/services under which a text file whose name is the full-qualified binary name
 * of this interface (com.openexchange.admin.console.context.ContextConsoleListInterface). And the
 * content of this file must be the full-qualified binary name of your implementation.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface ContextConsoleListInterface extends ContextConsoleCommonInterface {

    /**
     * This method can be implemented to set the names of the columns which should extend the normal output
     *
     * @return
     */
    public ArrayList<String> getColumnNamesHumanReadable();

    /**
     * This method can be implemented to set the names of the columns which should extend the csv output
     *
     * @return
     */
    public ArrayList<String> getColumnNamesCSV();

    /**
     * This method can be implemented to set the data in the normal output.
     * Note: If the data is empty null must be inserted in the array at that point.
     *
     * @return
     * @throws PluginException
     */
    public ArrayList<String> getHumanReadableData(final Context ctx) throws PluginException;

    /**
     * This method can be implemented to set the data in the CSV output.
     * Note: If the data is empty null must be inserted in the array at that point.
     *
     * @return
     * @throws PluginException
     */
    public ArrayList<String> getCSVData(final Context ctx) throws PluginException;

}
