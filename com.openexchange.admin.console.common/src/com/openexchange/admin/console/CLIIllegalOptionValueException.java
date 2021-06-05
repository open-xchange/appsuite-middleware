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
 * {@link CLIIllegalOptionValueException} - Indicates an illegal or missing value.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CLIIllegalOptionValueException extends CLIOptionException {

    private static final long serialVersionUID = 9131675571912299169L;

    private final CLIOption option;

    private final String value;

    public CLIIllegalOptionValueException(final CLIOption opt, final String value) {
        this(opt, value, null);
    }

    public CLIIllegalOptionValueException(final CLIOption opt, final String value, final Throwable cause) {
        super(MessageFormat.format(
            "Illegal value \"{0}\" for option {1}--{2}",
            value,
            (opt.shortForm() == null ? "" : "-" + opt.shortForm() + "/"),
            opt.longForm()), cause);
        this.option = opt;
        this.value = value;
    }

    /**
     * Gets the name of the option whose value was illegal.
     *
     * @return The name of the option whose value was illegal (e.g. "-u")
     */
    public CLIOption getOption() {
        return this.option;
    }

    /**
     * Gets the illegal value.
     *
     * @return The illegal value
     */
    public String getValue() {
        return this.value;
    }

}
