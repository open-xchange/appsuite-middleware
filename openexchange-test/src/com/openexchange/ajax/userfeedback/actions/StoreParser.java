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

package com.openexchange.ajax.userfeedback.actions;

import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * 
 * {@link StoreParser}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class StoreParser extends AbstractAJAXParser<StoreResponse> {

    /**
     * 
     * Initializes a new {@link StoreParser}.
     * 
     * @param failOnError
     */
    public StoreParser(boolean failOnError) {
        super(failOnError);
    }

    @Override
    public StoreResponse parse(String body) throws JSONException {
        return createResponse(getResponse(body));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected StoreResponse createResponse(final Response response) throws JSONException {
        return new StoreResponse(response);
    }
}
