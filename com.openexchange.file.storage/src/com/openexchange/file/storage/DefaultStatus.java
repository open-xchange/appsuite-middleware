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

package com.openexchange.file.storage;

import java.util.Locale;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultStatus}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class DefaultStatus implements Status {

    private final String id;
    private final String message;
    private final OXException exception;

    /**
     * Initializes a new {@link DefaultStatus}.
     *
     *  @param id The ID of the status
     *  @param message The message related to the status
     */
    public DefaultStatus(String id, String message) {
        this.id = id;
        this.message = message;
        this.exception = null;
    }

    /**
     * Initializes a new {@link DefaultStatus}.
     *
     *  @param id The ID of the status
     *  @param message The message related to the status
     *  @param exception The exception
     */
    public DefaultStatus(String id, OXException exception) {
        this.id = id;
        this.exception = exception;
        this.message = null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getMessage(Locale locale) {
        if (message != null) {
            return message;
        }

        if (exception != null) {
            if (exception.getCause() instanceof OXException) {
               return ((OXException)exception.getCause()).getDisplayMessage(locale);
            }
            return exception.getDisplayMessage(locale);
        }
        return null;
    }

    @Override
    public OXException getError() {
        return exception;
    }
}
