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

package com.openexchange.mail.categories;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link ReorganizeParameter} - The parameter to control whether re-organize should be performed or not.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ReorganizeParameter {

    private static final ReorganizeParameter DONT_REORGANIZE = new ReorganizeParameter(false);

    /**
     * Gets the parameter instance for given flag.
     *
     * @param reorganize <code>true</code> if re-organize is supposed to be performed; otherwise <code>false</code>
     * @return
     */
    public static ReorganizeParameter getParameterFor(boolean reorganize) {
        return reorganize ? new ReorganizeParameter(true) : DONT_REORGANIZE;
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private final boolean reorganize;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link ReorganizeParameter}.
     */
    private ReorganizeParameter(boolean reorganize) {
        super();
        this.reorganize = reorganize;
        this.warnings = reorganize ? new LinkedList<OXException>() : null;
    }

    /**
     * Checks whether re-organize is supposed to be performed.
     *
     * @return <code>true</code> to re-organize; otherwise <code>false</code>
     */
    public boolean isReorganize() {
        return reorganize;
    }

    /**
     * Gets the optional collection to add possible warnings to.
     *
     * @return The collection of warnings or <code>null</code>
     */
    public List<OXException> getWarnings() {
        return warnings;
    }

    /**
     * Checks if this instance has warnings.
     *
     * @return <code>true</code> for warnings; otherwise <code>false</code>
     */
    public boolean hasWarnings() {
        return null != warnings && !warnings.isEmpty();
    }

}
