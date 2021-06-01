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

package com.openexchange.chronos.exception;

/**
 * {@link ProblemSeverity}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum ProblemSeverity {

    /**
     * The <i>trivial</i> problem severity.
     */
    TRIVIAL,

    /**
     * The <i>minor</i> problem severity.
     */
    MINOR,

    /**
     * The <i>normal</i> problem severity.
     */
    NORMAL,

    /**
     * The <i>major</i> problem severity.
     */
    MAJOR,

    /**
     * The <i>critical</i> problem severity.
     */
    CRITICAL,

    /**
     * The <i>blocker</i> problem severity.
     */
    BLOCKER

}
