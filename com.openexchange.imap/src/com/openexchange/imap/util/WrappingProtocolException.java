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

package com.openexchange.imap.util;

import javax.mail.MessagingException;
import com.sun.mail.iap.ProtocolException;


/**
 * {@link WrappingProtocolException} - A {@link ProtocolException} wrapping a {@link MessagingException} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class WrappingProtocolException extends ProtocolException {

    private static final long serialVersionUID = 9034219079379775615L;

    private final MessagingException messagingException;

    /**
     * Initializes a new {@link WrappingProtocolException}.
     *
     * @param message The message
     * @param cause The wrapped {@code MessagingException} instance
     */
    public WrappingProtocolException(String message, MessagingException cause) {
        super(message, cause);
        this.messagingException = cause;
    }

    /**
     * Gets the {@code MessagingException} instance
     *
     * @return The {@code MessagingException} instance
     */
    public MessagingException getMessagingException() {
        return messagingException;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
