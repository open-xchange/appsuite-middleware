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

package com.openexchange.imagetransformation;

import java.io.IOException;

/**
 * {@link ImageTransformationDeniedIOException} - The special I/O error signaling that image transformation has been denied.
 * <p>
 * Image either exceeds allowed max. size or resolution.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ImageTransformationDeniedIOException extends IOException {

    private static final long serialVersionUID = 6657928423486571747L;

    /**
     * Initializes a new {@link ImageTransformationDeniedIOException}.
     */
    public ImageTransformationDeniedIOException() {
        super();
    }

    /**
     * Initializes a new {@link ImageTransformationDeniedIOException}.
     *
     * @param message The detail message
     * @param cause The cause
     */
    public ImageTransformationDeniedIOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new {@link ImageTransformationDeniedIOException}.
     *
     * @param message The detail message
     */
    public ImageTransformationDeniedIOException(String message) {
        super(message);
    }

    /**
     * Initializes a new {@link ImageTransformationDeniedIOException}.
     *
     * @param cause The cause
     */
    public ImageTransformationDeniedIOException(Throwable cause) {
        super(cause);
    }

}
