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

package com.openexchange.api.client.common.calls;

import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpMethods;

/**
 * {@link AbstractPutCall} - Common abstract class for methods build on {@link HttpMethods#PUT}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @param <T> The class of the response
 * @since v7.10.5
 */
public abstract class AbstractPutCall<T> extends AbstractApiCall<T> {

    @Override
    @NonNull
    public HttpMethods getHttpMehtod() {
        return HttpMethods.PUT;
    }
}
