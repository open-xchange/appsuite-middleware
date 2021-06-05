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

package com.openexchange.groupware.contexts.impl;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link ContextExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ContextExceptionMessage implements LocalizableStrings {

    public final static String NOT_FOUND_MSG = "The account %1$d was not found.";
    public final static String UPDATE_MSG = "The server is currently down for maintenance. Please try again later.";
    public final static String NO_CONNECTION_TO_CONTEXT_MSG = "Could not connect to the context storage.";
    public final static String NO_MAPPING_MSG = "The account \"%1$s\" was not found.";

    // This exception is triggered by concurrent requests of clients trying to modify the same user attributes in the same moment.
    // This should happen in very rare conditions and is not visible to the client.
    public static final String CONCURRENT_ATTRIBUTES_UPDATE_DISPLAY = "Denied concurrent update of user attributes.";

    /**
     * Initializes a new {@link ContextExceptionMessage}.
     */
    private ContextExceptionMessage() {
        super();
    }

}
