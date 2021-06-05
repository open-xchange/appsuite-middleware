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

/**
 * {@link ImageConverterException}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10
 */
public class ImageConverterException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2558760477364199103L;

    /**
     * Initializes a new {@link ImageConverterException}.
     */
    public ImageConverterException() {
        super();

    }

    /**
     * Initializes a new {@link ImageConverterException}.
     * @param message
     */
    public ImageConverterException(String message) {
        super(message);

    }

    /**
     * Initializes a new {@link ImageConverterException}.
     * @param cause
     */
    public ImageConverterException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link ImageConverterException}.
     * @param message
     * @param cause
     */
    public ImageConverterException(String message, Throwable cause) {
        super(message, cause);
    }

}
