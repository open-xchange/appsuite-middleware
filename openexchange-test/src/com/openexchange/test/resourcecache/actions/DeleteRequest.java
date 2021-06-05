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

package com.openexchange.test.resourcecache.actions;

import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link DeleteRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DeleteRequest extends AbstractResourceCacheRequest<DeleteResponse> {

    private final String id;

    public DeleteRequest() {
        this(null);
    }

    public DeleteRequest(String id) {
        super("delete");
        this.id = id;
    }

    @Override
    protected Parameter[] getAdditionalParameters() {
        if (id == null) {
            return super.getAdditionalParameters();
        }

        return new Parameter[] { new URLParameter("id", id) };
    }

    @Override
    public AbstractAJAXParser<DeleteResponse> getParser() {
        return new AbstractAJAXParser<DeleteResponse>(true) {

            @Override
            protected DeleteResponse createResponse(Response response) throws JSONException {
                return new DeleteResponse(response);
            }
        };
    }

}
