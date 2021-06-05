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

import java.util.List;
import com.openexchange.dovecot.doveadm.client.DoveAdmDataResponse;
import com.openexchange.dovecot.doveadm.client.Result;


/**
 * {@link DoveAdmDataResponseImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DoveAdmDataResponseImpl extends AbstractDoveAdmResponse implements DoveAdmDataResponse {

    private final List<Result> results;

    /**
     * Initializes a new {@link DoveAdmDataResponseImpl}.
     */
    DoveAdmDataResponseImpl(List<Result> results, String optionalIdentifier) {
        super(TYPE_DATA_RESPONSE, optionalIdentifier, false);
        this.results = results;
    }

    @Override
    public DoveAdmDataResponse asDataResponse() {
        return this;
    }

    @Override
    public List<Result> getResults() {
        return results;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(96);
        builder.append("{");
        builder.append("command=").append(getCommand()).append(", ");
        if (results != null) {
            builder.append("results=").append(results).append(", ");
        }
        String optionalIdentifier = getOptionalIdentifier();
        if (optionalIdentifier != null) {
            builder.append("optionalIdentifier=").append(getOptionalIdentifier());
        }
        builder.append("}");
        return builder.toString();
    }
}
