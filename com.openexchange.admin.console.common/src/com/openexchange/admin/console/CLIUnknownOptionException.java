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

import java.text.MessageFormat;

/**
 * {@link CLIUnknownOptionException} - Indicates an unknown option.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CLIUnknownOptionException extends CLIOptionException {

    private static final long serialVersionUID = -5280553935783202280L;

    private String optionName = null;

    CLIUnknownOptionException(final String optionName) {
        this(optionName, MessageFormat.format("Unknown option ``{0}''", optionName));
    }

    CLIUnknownOptionException(final String optionName, final Throwable cause) {
        this(optionName, MessageFormat.format("Unknown option ``{0}''", optionName), cause);
    }

    CLIUnknownOptionException(final String optionName, final String msg) {
        this(optionName, msg, null);
    }

    CLIUnknownOptionException(final String optionName, final String msg, final Throwable cause) {
        super(msg, cause);
        this.optionName = optionName;
    }

    /**
     * Gets the name of the option that was unknown (e.g. <code>"-u"</code>).
     *
     * @return The name of the option that was unknown
     */
    public String getOptionName() {
        return this.optionName;
    }

}
