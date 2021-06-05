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

package com.openexchange.soap.cxf.interceptor;

import java.util.logging.Logger;
import org.apache.cxf.interceptor.LoggingMessage;

/**
 * {@link LoggingInInterceptor} - Extends {@link org.apache.cxf.interceptor.LoggingInInterceptor} by sanitizing possible passwords in logged
 * payloads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SuppressWarnings("deprecation")
public class LoggingInInterceptor extends org.apache.cxf.interceptor.LoggingInInterceptor {

    /**
     * Initializes a new {@link LoggingInInterceptor}.
     */
    public LoggingInInterceptor() {
        super();
    }

    @Override
    protected String formatLoggingMessage(final LoggingMessage loggingMessage) {
        // Do not process message
        // return super.formatLoggingMessage(com.openexchange.soap.cxf.interceptor.LoggingUtility.sanitizeLoggingMessage(loggingMessage));
        return super.formatLoggingMessage(loggingMessage);
    }

    @Override
    protected String transform(String originalLogString) {
        return super.transform(originalLogString);
    }

    @Override
    protected void log(Logger logger, String message) {
        super.log(logger, message);
    }

}
