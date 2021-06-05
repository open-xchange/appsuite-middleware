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

package com.openexchange.api.client.common.parser;

import org.apache.http.protocol.HttpContext;
import com.openexchange.exception.OXException;

/**
 * {@link StringParser} - For responses that contain only one string in the <code>data</code> field
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class StringParser extends AbstractHttpResponseParser<String> {

    /** Simple class to delay initialization until needed */
    private static class InstanceHolder {

        static final StringParser INSTANCE = new StringParser();
    }

    /**
     * Initializes a new {@link StringParser}.
     */
    protected StringParser() {
        super();
    }

    /**
     * Get the instance of a {@link StringParser}
     *
     * @return A {@link StringParser}
     */
    public static StringParser getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException {
        return commonResponse.getData(String.class);
    }

}
