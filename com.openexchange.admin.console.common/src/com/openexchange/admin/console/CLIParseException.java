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

package com.openexchange.admin.console;

import java.util.Arrays;

/**
 * {@link CLIParseException} - Parsing command line failed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CLIParseException extends CLIOptionException {

    private static final long serialVersionUID = 2206541577642270929L;

    /**
     * Initializes a new {@link CLIParseException}.
     *
     * @param commandLine The command line which could not be parsed
     */
    public CLIParseException(final String[] commandLine) {
        this(commandLine, null);
    }

    /**
     * Initializes a new {@link CLIParseException}.
     *
     * @param commandLine The command line which could not be parsed
     * @param cause The cause
     */
    public CLIParseException(final String[] commandLine, final Throwable cause) {
        super(getMessage(commandLine, cause), cause);
    }

    private static String getMessage(final String[] commandLine, final Throwable cause) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("Unable to parse command line: ");
        if (null != cause) {
            sb.append(cause.getMessage());
            sb.append("\nCommand line: ");
        }
        sb.append(Arrays.toString(commandLine));
        return sb.toString();
    }

}
