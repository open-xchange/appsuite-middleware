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

package com.openexchange.mail.mime.utils;

import java.io.IOException;

/**
 * The Transfer Encoding is not supported.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class UnsupportedTransferEncodingException extends IOException {

    private static final long serialVersionUID = 597922586356765695L;

    /**
     * Constructs an UnsupportedTransferEncodingException without a detail message.
     */
    public UnsupportedTransferEncodingException() {
        super();
    }

    /**
     * Constructs an UnsupportedTransferEncodingException with a detail message.
     * @param s Describes the reason for the exception.
     */
    public UnsupportedTransferEncodingException(String s) {
        super(s);
    }

    /**
     * Constructs an UnsupportedTransferEncodingException with a detail message and a cause.
     *
     * @param s Describes the reason for the exception.
     * @param cause The initial cause
     */
    public UnsupportedTransferEncodingException(String s, Throwable cause) {
        super(s, cause);
    }
}
