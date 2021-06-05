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

package com.openexchange.groupware.upload.impl;

import static com.openexchange.groupware.upload.impl.UploadUtility.getSize;

/**
 * {@link UploadSizeExceededException} - The upload error with code MAX_UPLOAD_SIZE_EXCEEDED providing the possibility to convert bytes to a
 * human readable string; e.g. <code>88.3 MB</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UploadSizeExceededException extends UploadException {

    /**
     * No instance.
     */
    private UploadSizeExceededException(final int code, final String displayMessage, final Throwable cause, final Object[] displayArgs) {
        super(code, displayMessage, cause, displayArgs);
    }

    private static final long serialVersionUID = -6166524953168225923L;

    /**
     * Initializes a new {@link UploadException} for exceeded upload size.
     *
     * @param size The actual size in bytes
     * @param maxSize The max. allowed size in bytes
     * @param humanReadable <code>true</code> to convert bytes to a human readable string; otherwise <code>false</code>
     */
    public static UploadException create(final long size, final long maxSize, final boolean humanReadable) {
        return UploadException.UploadCode.MAX_UPLOAD_SIZE_EXCEEDED.create(
            humanReadable ? getSize(size, 2, false, true) : Long.valueOf(size),
            humanReadable ? getSize(maxSize, 2, false, true) : Long.valueOf(maxSize)).setAction(null);
    }

}
