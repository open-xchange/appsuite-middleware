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

package com.openexchange.ajax.framework;

import com.openexchange.ajax.container.Response;

/**
 * Common parser for delete responses.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CommonDeleteParser extends AbstractDeleteParser<CommonDeleteResponse> {

    public CommonDeleteParser(final boolean failOnError) {
        super(failOnError);
    }

    /**
     * Override this method to create your own response class.
     * 
     * @param response the json response container.
     * @return a {@link CommonDeleteResponse}
     */
    @Override
    protected CommonDeleteResponse instanciateResponse(final Response response) {
        return new CommonDeleteResponse(response);
    }
}
