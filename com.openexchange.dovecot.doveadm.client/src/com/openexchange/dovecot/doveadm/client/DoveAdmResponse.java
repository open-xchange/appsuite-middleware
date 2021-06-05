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

package com.openexchange.dovecot.doveadm.client;

/**
 * {@link DoveAdmResponse} - Represents a response from the Dovecot DoveAdm REST interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface DoveAdmResponse extends DoveAdmEntity {

    /** The command identifier to signal a regular DoveAdm data response */
    public static final String TYPE_DATA_RESPONSE = "doveadmResponse";

    /** The command identifier to signal a DoveAdm error response */
    public static final String TYPE_ERROR_RESPONSE = "error";

    /**
     * Checks if this DoveAdm response is an error.
     *
     * @return <code>true</code> if this DoveAdm response is an error; otherwise <code>false</code>
     */
    boolean isError();

    /**
     * Gets the error response representation from this response.
     * <p>
     * Only makes sense in case {@link #isError()} returns <code>true</code>.
     *
     * @return The error response representation or <code>null</code> if not applicable
     */
    DoveAdmErrorResponse asErrorResponse();

    /**
     * Gets the data response representation from this response.
     * <p>
     * Only makes sense in case {@link #isError()} returns <code>false</code>.
     *
     * @return The data response representation or <code>null</code> if not applicable
     */
    DoveAdmDataResponse asDataResponse();

}
