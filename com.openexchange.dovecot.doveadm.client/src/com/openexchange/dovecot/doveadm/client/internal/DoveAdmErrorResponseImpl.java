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

package com.openexchange.dovecot.doveadm.client.internal;

import com.openexchange.dovecot.doveadm.client.DoveAdmErrorResponse;


/**
 * {@link DoveAdmErrorResponseImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DoveAdmErrorResponseImpl extends AbstractDoveAdmResponse implements DoveAdmErrorResponse {

    private final String type;
    private final int exitCode;

    /**
     * Initializes a new {@link DoveAdmErrorResponseImpl}.
     */
    DoveAdmErrorResponseImpl(String type, int exitCode, String optionalIdentifier) {
        super(TYPE_ERROR_RESPONSE, optionalIdentifier, true);
        this.type = type;
        this.exitCode = exitCode;
    }

    @Override
    public DoveAdmErrorResponse asErrorResponse() {
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(96);
        builder.append("{");
        builder.append("command=").append(TYPE_ERROR_RESPONSE).append(", ");
        if (type != null) {
            builder.append("type=").append(type).append(", ");
        }
        builder.append("exitCode=").append(exitCode).append(", ");
        String optionalIdentifier = getOptionalIdentifier();
        if (optionalIdentifier != null) {
            builder.append("optionalIdentifier=").append(optionalIdentifier);
        }
        builder.append("}");
        return builder.toString();
    }
}
