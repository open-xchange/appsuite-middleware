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

package com.openexchange.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


/**
 * {@link ReservedOptions} - Enhances {@link Options} by allowing only those options to be added that are not already contained.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public class ReservedOptions extends Options {

    private static final long serialVersionUID = -5606926837108336821L;

    /**
     * Initializes a new {@link ReservedOptions} instance.
     */
    public ReservedOptions() {
        super();
    }

    @Override
    public Options addOption(final Option opt) {
        // Ensure not already contained
        String key = opt.getOpt();
        if (null == key) {
            key = opt.getLongOpt();
        }
        if (!hasOption(key)) {
            super.addOption(opt);
        }
        return this;
    }

}
