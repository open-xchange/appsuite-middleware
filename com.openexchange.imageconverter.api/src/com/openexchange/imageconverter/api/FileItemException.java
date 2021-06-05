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

package com.openexchange.imageconverter.api;

import com.openexchange.exception.OXException;

/**
 * {@link FileItemException}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
public class FileItemException extends OXException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 331089657199858445L;

    /**
     * Initializes a new {@link FileItemException}.
     */
    public FileItemException() {
        super();
    }

    /**
     * Initializes a new {@link FileItemException}.
     * @param displayMessage
     * @param e
     */
    public FileItemException(final String displayMessage) {
        super();
        setDisplayMessage(displayMessage);
    }

    /**
     * Initializes a new {@link FileItemException}.
     * @param cause
     */
    public FileItemException(final Throwable e) {
        super(e);

    }

    /**
     * Initializes a new {@link FileItemException}.
     * @param displayMessage
     * @param e
     */
    public FileItemException(final String displayMessage, final Throwable e) {
        super(e);
        setDisplayMessage(displayMessage);
    }
}
