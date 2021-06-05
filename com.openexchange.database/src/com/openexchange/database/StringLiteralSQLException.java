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

package com.openexchange.database;

import java.sql.SQLException;


/**
 * {@link StringLiteralSQLException} - The generic {@link SQLException} class indicating a problem with a passed character string literal.
 * <p>
 * For instance {@link IncorrectStringSQLException} or {@link IllegalMixOfCollationsSQLException}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class StringLiteralSQLException extends SQLException {

    private static final long serialVersionUID = -9142992709020086710L;

    /**
     * Initializes a new {@link StringLiteralSQLException}.
     *
     * @param cause The causing SQL exception
     */
    public StringLiteralSQLException(SQLException cause) {
        super(cause.getMessage(), cause.getSQLState(), cause.getErrorCode(), cause);
    }

}
