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

package com.openexchange.conversion.datahandler;

/**
 * {@link DataHandlers}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public final class DataHandlers {

    /** The identifier of the data handler to convert from an {@link OXException} to its JSON representation. */
    public static final String OXEXCEPTION2JSON = "com.openexchange.conversion.datahandler.exception2json";

    /** The identifier of the data handler to convert from an ox exception's JSON representation to {@link OXException}s. */
    public static final String JSON2OXEXCEPTION = "com.openexchange.conversion.datahandler.json2oxexception";
}
